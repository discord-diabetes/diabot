package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.util.Logger
import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.findOne
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import kotlin.reflect.KProperty

class NightscoutDAO private constructor() {
    private val mongo = MongoDB.getInstance().database
    val collection: MongoCollection<NightscoutUserDTO> = mongo.getCollection("nightscout", NightscoutUserDTO::class.java)
    private val scheduler = Schedulers.boundedElastic()
    private val logger by Logger()

    init {
        // Create a unique index
        val options = IndexOptions().unique(true)
        collection.createIndex(descending(NightscoutUserDTO::userId), options).toMono()
                .subscribeOn(scheduler)
                .subscribe()
    }

    /**
     * Gets a [NightscoutUserDTO] from a user ID
     *
     * @param userId The user ID to look up
     * @return [NightscoutUserDTO] for this user
     */
    fun getUser(userId: Long): Mono<NightscoutUserDTO> {
        return collection.findOne(filter(userId)).subscribeOn(scheduler)
    }

    /**
     * Inserts a new user's NS data into the database.
     * This should only be used if it is known that the user does not exist already as it will error otherwise.
     *
     * @param dto The [NightscoutUserDTO] to be inserted
     * @return The result of the insertion
     */
    fun addUser(dto: NightscoutUserDTO): Mono<InsertOneResult> {
        return collection.insertOne(dto).toMono()
                .subscribeOn(scheduler)
    }

    /**
     * Deletes stored NS data belonging to the given user.
     * If no properties are given, all of their NS data will be deleted from the database.
     *
     * @param dto The user's DTO.
     * @param fields Which fields to delete. If this is not provided then all of the user's data will be deleted.
     * @return Either a [UpdateResult] or [DeleteResult] representing the result of data deletion.
     * If `fields` is blank this will return [DeleteResult]. If not blank, [UpdateResult].
     */
    fun deleteUser(dto: NightscoutUserDTO, vararg fields: KProperty<*>): Mono<*> {
        return deleteUser(dto.userId, *fields)
    }

    /**
     * Deletes stored NS data belonging to the given user.
     * If no properties are given, all of their NS data will be deleted from the database.
     *
     * @param userId The user's ID.
     * @param fields Which fields to delete. If this is not provided then all of the user's data will be deleted.
     * @return Either a [UpdateResult] or [DeleteResult] representing the result of data deletion.
     * If `fields` is blank this will return [DeleteResult]. If not blank, [UpdateResult].
     */
    fun deleteUser(userId: Long, vararg fields: KProperty<*>): Mono<*> {
        if (fields.isNullOrEmpty()) {
            return collection.deleteOne(filter(userId)).toMono()
                    .subscribeOn(scheduler)
        }

        val unsets = fields.map { unset(it) }
        val combined = combine(unsets)
        return collection.updateOne(filter(userId), combined).toMono()
                .subscribeOn(scheduler)
    }

    /**
     * Sets a user's NS URL.
     *
     * @param userId The user's ID
     * @param url The user's Nightscout URL
     * @return The result of setting their URL
     */
    fun setUrl(userId: Long, url: String): Mono<UpdateResult> {
        return collection.updateOne(NightscoutUserDTO::userId eq userId,
                setValue(NightscoutUserDTO::url, url), upsert())
                .toMono()
                .subscribeOn(scheduler)
    }

    /**
     * Changes a user's NS privacy settings for a guild.
     *
     * @param userId The user ID to change privacy under.
     * @param guildId The guild ID to change privacy under.
     * @param public Whether or not this guild should be set as public or private.
     * If this is null, the privacy will be toggled instead.
     * @return This user's new privacy setting for the given guild.
     */
    fun changePrivacy(userId: Long, guildId: Long, public: Boolean? = null): Mono<Boolean> {
        val upsertAfter = findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        val userFilter = filter(userId)
        val newState = if (public != null) {
            Mono.just(public)
        } else {
            val filter = and(userFilter, NightscoutUserDTO::publicGuilds `in` listOf(guildId))
            collection.countDocuments(filter).toMono().map { it == 0L }
        }

        val update = newState.map {
            if (it) {
                addToSet(NightscoutUserDTO::publicGuilds, guildId)
            } else {
                pull(NightscoutUserDTO::publicGuilds, guildId)
            }
        }

        return update.flatMap { collection.findOneAndUpdate(filter(userId), it, upsertAfter).toMono() }
                .map { it.publicGuilds.contains(guildId) }
                .subscribeOn(scheduler)
    }

    /**
     * Sets a token to be used when fetching data from a user's Nightscout.
     *
     * @param userId The user's ID
     * @param token The Nightscout token. If null, the token key will be deleted.
     * @return The result of setting the user's Nightscout token
     */
    fun setToken(userId: Long, token: String?): Mono<UpdateResult> {
        if (token == null) {
            // delete the key instead of setting it to null
            return deleteUser(userId, NightscoutUserDTO::token).ofType(UpdateResult::class.java)
        }

        return collection.updateOne(NightscoutUserDTO::userId eq userId,
                setValue(NightscoutUserDTO::token, token), upsert())
                .toMono()
                .subscribeOn(scheduler)
    }

    companion object {
        val instance: NightscoutDAO by lazy { NightscoutDAO() }

        fun filter(userId: Long): Bson {
            return NightscoutUserDTO::userId eq userId
        }
    }
}