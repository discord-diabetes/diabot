package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.logger
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

class ChannelDAO private constructor() {
    private val mongo = MongoDB.getInstance().database
    val collection: MongoCollection<ChannelDTO> = mongo.getCollection("channels", ChannelDTO::class.java)
    private val scheduler = Schedulers.boundedElastic()
    private val logger = logger()

    init {
        // Create a unique index
        val options = IndexOptions().unique(true)
        collection.createIndex(descending(ChannelDTO::channelId), options).toMono()
                .subscribeOn(scheduler)
                .subscribe()
    }

    /**
     * Gets all of the channel objects stored in the database for a guild.
     *
     * @param guildId The guild's ID
     * @return [ChannelDTO]s for this guild
     */
    fun getChannels(guildId: String): Flux<ChannelDTO> {
        return collection.find(filterGuild(guildId))
                .toFlux().subscribeOn(scheduler)
    }

    /**
     * Gets a single channel object stored in the database from a channel and, optionally, a guild ID.
     *
     * @param channelId The channel's ID
     * @param guildId Optional. The guild's ID
     * @return A [ChannelDTO] for this channel
     */
    fun getChannel(channelId: String, guildId: String? = null): Mono<ChannelDTO> {
        return collection.find(filter(channelId, guildId))
                .toMono().subscribeOn(scheduler)
    }

    /**
     * Checks if a channel has an attribute attached to it.
     *
     * @param channelId The channel ID to check under
     * @param attribute The attribute to check for
     * @return True if the channel has the given attribute
     */
    fun hasAttribute(channelId: String, attribute: ChannelDTO.ChannelAttribute): Mono<Boolean> {
        return collection.countDocuments(and(filter(channelId), ChannelDTO::attributes `in` listOf(attribute)))
                .toMono().subscribeOn(scheduler)
                .map { it != 0L }
    }

    /**
     * Inserts a new channel into the database.
     * This should only be used if it is known that the channel does not exist already as it will error otherwise.
     *
     * @param dto The [ChannelDTO] to be inserted
     * @return The result of the insertion
     */
    fun addChannel(dto: ChannelDTO): Mono<InsertOneResult> {
        return collection.insertOne(dto).toMono()
                .subscribeOn(scheduler)
    }

    /**
     * Deletes a channel from the database.
     *
     * @param channelId The channel ID to delete
     * @return The result of the deletion
     */
    fun deleteChannel(channelId: String): Mono<DeleteResult> {
        return collection.deleteOne(filter(channelId)).toMono()
                .subscribeOn(scheduler)
    }

    /**
     * Adds or removes an attribute from a channel.
     * If the channel does not exist in the database already then a [ChannelDTO] will be created.
     *
     * @param guildId The guild ID for this channel
     * @param channelId The channel ID
     * @param attribute The attribute to add or remove
     * @param add Whether to add the given attribute or remove it.
     * @return The updated [ChannelDTO]
     */
    fun changeAttribute(
            guildId: String,
            channelId: String,
            attribute: ChannelDTO.ChannelAttribute,
            add: Boolean = true
    ): Mono<ChannelDTO> {
        val upsertAfter = findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        val channelFilter = filter(channelId, guildId)
        val update = if (add) {
            addToSet(ChannelDTO::attributes, attribute)
        } else {
            pull(ChannelDTO::attributes, attribute)
        }

        return collection.findOneAndUpdate(channelFilter, update, upsertAfter).toMono()
                .subscribeOn(scheduler)
                // delete document if channel attributes are empty
                .doOnNext { if (it.attributes.isEmpty()) deleteChannel(it.channelId).subscribe() }
    }

    companion object {
        val instance: ChannelDAO by lazy { ChannelDAO() }

        fun filter(channelId: String, guildId: String? = null): Bson {
            var filter = ChannelDTO::channelId eq channelId
            if (guildId != null)
                filter = and(filter, filterGuild(guildId))

            return filter
        }

        fun filterGuild(guildId: String): Bson {
            return ChannelDTO::guildId eq guildId
        }
    }
}