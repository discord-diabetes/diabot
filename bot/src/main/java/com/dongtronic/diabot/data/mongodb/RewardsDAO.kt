package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.util.DiabotCollection
import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.logger
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.TimeUnit

class RewardsDAO private constructor() {
    private val mongo = MongoDB.getInstance().database
    val rewards: MongoCollection<RewardsDTO> =
            mongo.getCollection(DiabotCollection.REWARDS.getEnv(), RewardsDTO::class.java)
    val optOuts: MongoCollection<RewardOptOutsDTO> =
            mongo.getCollection(DiabotCollection.REWARDS_OPTOUT.getEnv(), RewardOptOutsDTO::class.java)

    private val guildRewardsCache: Cache<String, List<RewardsDTO>> = Caffeine.newBuilder()
            .expireAfterAccess(240, TimeUnit.MINUTES)
            .maximumWeight(2000)
            .weigher { _: String, rewards: List<RewardsDTO> -> rewards.size }
            .build()

    private val optOutCache: Cache<String, List<String>> = Caffeine.newBuilder()
            .expireAfterAccess(240, TimeUnit.MINUTES)
            .maximumWeight(2000)
            .weigher { _: String, optOuts: List<String> -> optOuts.size }
            .build()
    private val scheduler = Schedulers.boundedElastic()
    private val logger = logger()

    init {
        // Create two unique indexes:
        // `requiredRole` in the `rewards` collection
        // `guildId` in the `rewards-optout` collection
        val options = IndexOptions().unique(true)
        rewards.createIndex(descending(RewardsDTO::requiredRole), options).toMono()
                .subscribeOn(scheduler)
                .then(optOuts.createIndex(descending(RewardOptOutsDTO::guildId), options).toMono())
                .subscribe()
    }

    /**
     * Gets the opted-out users under a guild.
     *
     * @param guildId The guild to search under
     * @return [RewardOptOutsDTO]
     */
    fun getOptOuts(guildId: String): Mono<RewardOptOutsDTO> {
        val cached = optOutCache.getIfPresent(guildId)
        if (cached != null) {
            return RewardOptOutsDTO(guildId, cached).toMono()
        }

        return optOuts.find(filter(guildId))
                .toMono().subscribeOn(scheduler)
                .doOnNext { optOutCache.put(it.guildId, it.optOut) }
    }

    /**
     * Checks if a user is opted out of rewards in a guild.
     *
     * @param guildId The guild to query
     * @param userId The user to check for opted-out status
     * @return True if the user is opted out, false otherwise
     */
    fun isOptOut(guildId: String, userId: String): Mono<Boolean> {
        val cached = optOutCache.getIfPresent(guildId)
        if (cached != null) {
            return cached.contains(userId).toMono()
        }

        return optOuts.countDocuments(filterUser(guildId, userId))
                .toMono().subscribeOn(scheduler)
                // fetch all the opted-out users for this guild for caching
                .doOnNext { getOptOuts(guildId).subscribe() }
                .map { it != 0L }
    }

    /**
     * Gets all the rewards for a guild.
     *
     * @param guildId The guild to grab rewards for
     * @return A [List] of [RewardsDTO] for the guild
     */
    fun getRewards(guildId: String): Mono<List<RewardsDTO>> {
        val cached = guildRewardsCache.getIfPresent(guildId)
        if (cached != null) {
            return cached.toMono()
        }

        return rewards.find(filter(guildId))
                .toFlux().subscribeOn(scheduler)
                .collectList()
                .doOnNext { guildRewardsCache.put(guildId, it) }
    }

    /**
     * Inserts a reward into the database.
     * This should only be used if it is known that the reward does not exist already as it will error otherwise.
     *
     * @param dto The [RewardsDTO] to be inserted
     * @return The result of the insertion
     */
    fun importReward(dto: RewardsDTO): Mono<InsertOneResult> {
        return rewards.insertOne(dto).toMono()
                .subscribeOn(scheduler)
                .doOnNext { guildRewardsCache.invalidate(dto.guildId) }
    }

    /**
     * Inserts a guild's list of opt-outs into the database.
     * This should only be used if it is known that the guild does not exist already as it will error otherwise.
     *
     * @param dto The [RewardOptOutsDTO] to be inserted
     * @return The result of the insertion
     */
    fun importOptOuts(dto: RewardOptOutsDTO): Mono<InsertOneResult> {
        return optOuts.insertOne(dto).toMono()
                .subscribeOn(scheduler)
                .doOnNext { optOutCache.invalidate(dto.guildId) }
    }

    /**
     * Deletes a reward object from the database.
     *
     * @param guildId The guild ID
     * @param requiredRole The required role ID
     * @return The result of the deletion
     */
    fun deleteReward(guildId: String, requiredRole: String): Mono<DeleteResult> {
        return rewards.deleteOne(filterReward(requiredRole, guildId)).toMono()
                .subscribeOn(scheduler)
                .doOnNext { guildRewardsCache.invalidate(guildId) }
    }

    /**
     * Deletes a guild's optout object from the database.
     *
     * @param guildId The guild ID
     * @return The result of the deletion
     */
    fun deleteGuildOptOuts(guildId: String): Mono<DeleteResult> {
        return optOuts.deleteOne(filter(guildId)).toMono()
                .subscribeOn(scheduler)
                .doOnNext { optOutCache.invalidate(guildId) }
    }

    /**
     * Adds or removes a reward role for a base (required) role.
     *
     * @param guildId The guild these roles belong to
     * @param requiredRole The required role ID
     * @param rewardRole The reward role ID
     * @param add Whether this role should be added as a reward role
     * @return The updated [RewardsDTO]
     */
    fun changeRewardRole(
            guildId: String,
            requiredRole: String,
            rewardRole: String,
            add: Boolean = true
    ): Mono<RewardsDTO> {
        val upsertAfter = findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        val rewardFilter = filterReward(requiredRole, guildId)

        val update = if (add) {
            addToSet(RewardsDTO::roleRewards, rewardRole)
        } else {
            pull(RewardsDTO::roleRewards, rewardRole)
        }

        return rewards.findOneAndUpdate(rewardFilter, update, upsertAfter).toMono()
                .subscribeOn(scheduler)
                // delete document if rewards are empty
                .doOnNext { if (it.roleRewards.isEmpty()) deleteReward(it.guildId, it.requiredRole).subscribe() }
                .doOnNext { guildRewardsCache.invalidate(it.guildId) }
    }

    /**
     * Changes a user's opt-preference for a guild.
     *
     * @param guildId The guild to change this user's opt-preference under
     * @param userId The user to change to opt state for
     * @param optOut If the new opt state should be `opt-out`
     * @return The updated guild's [RewardOptOutsDTO]
     */
    fun changeOpt(
            guildId: String,
            userId: String,
            optOut: Boolean = true
    ): Mono<RewardOptOutsDTO> {
        val upsertAfter = findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        val guildFilter = filter(guildId)

        val update = if (optOut) {
            addToSet(RewardOptOutsDTO::optOut, userId)
        } else {
            pull(RewardOptOutsDTO::optOut, userId)
        }

        return optOuts.findOneAndUpdate(guildFilter, update, upsertAfter).toMono()
                .subscribeOn(scheduler)
                // delete document if nobody is opted out
                .doOnNext { if (it.optOut.isEmpty()) deleteGuildOptOuts(it.guildId).subscribe() }
                .doOnNext { optOutCache.put(it.guildId, it.optOut) }
    }

    companion object {
        val instance: RewardsDAO by lazy { RewardsDAO() }

        fun filter(guildId: String): Bson {
            return RewardsDTO::guildId eq guildId
        }

        fun filterReward(requiredRole: String, guildId: String? = null): Bson {
            var filter = RewardsDTO::requiredRole eq requiredRole
            if (guildId != null) {
                filter = and(filter, RewardsDTO::guildId eq guildId)
            }

            return filter
        }

        fun filterUser(guildId: String, userId: String): Bson {
            return and(RewardOptOutsDTO::guildId eq guildId, RewardOptOutsDTO::optOut `in` userId)
        }
    }
}
