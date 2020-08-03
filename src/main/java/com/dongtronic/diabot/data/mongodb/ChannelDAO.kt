package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.logger
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
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

    fun getChannel(channelId: Long, guildId: Long? = null): Mono<ChannelDTO> {
        return collection.find(filter(channelId, guildId))
                .toMono().subscribeOn(scheduler)
    }

    fun hasAttribute(channelId: Long, attribute: ChannelDTO.ChannelAttribute): Mono<Boolean> {
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

    fun changeAttribute(
            guildId: Long,
            channelId: Long,
            attribute: ChannelDTO.ChannelAttribute,
            add: Boolean = true
    ): Mono<ChannelDTO> {
        val upsertAfter = findOneAndUpdateUpsert().returnDocument(ReturnDocument.AFTER)
        val channelFilter = filter(channelId, guildId)
        val shouldAdd = if (add) {
            Mono.just(add)
        } else {
            val filter = and(channelFilter, ChannelDTO::attributes `in` listOf(attribute))
            collection.countDocuments(filter).toMono().map { it == 0L }
        }

        val update = shouldAdd.map {
            if (it) {
                addToSet(ChannelDTO::attributes, attribute)
            } else {
                pull(ChannelDTO::attributes, attribute)
            }
        }

        return update.flatMap { collection.findOneAndUpdate(channelFilter, it, upsertAfter).toMono() }
                .subscribeOn(scheduler)
    }

    companion object {
        val instance: ChannelDAO by lazy { ChannelDAO() }

        fun filter(channelId: Long, guildId: Long? = null): Bson {
            var filter = ChannelDTO::channelId eq channelId
            if (guildId != null)
                filter = and(filter, ChannelDTO::guildId eq guildId)

            return filter
        }
    }
}