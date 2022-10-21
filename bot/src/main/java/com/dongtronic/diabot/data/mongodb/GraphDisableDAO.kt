package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.util.DiabotCollection
import com.dongtronic.diabot.util.MongoDB
import com.dongtronic.diabot.util.logger
import com.mongodb.client.model.IndexOptions
import com.mongodb.reactivestreams.client.MongoCollection
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.replaceUpsert
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

class GraphDisableDAO private constructor() {
    private val mongo = MongoDB.getInstance().database
    val collection: MongoCollection<GraphDisableDTO> =
            mongo.getCollection(DiabotCollection.GRAPH_DISABLE.getEnv(), GraphDisableDTO::class.java)
    private val scheduler = Schedulers.boundedElastic()
    private val logger = logger()

    init {
        // Create a unique index
        val options = IndexOptions().unique(true)
        collection.createIndex(descending(GraphDisableDTO::guildId), options).toMono()
                .subscribeOn(scheduler)
                .subscribe()
    }

    /**
     * Checks if graphs are enabled for the given guild.
     *
     * @param guildId The guild ID to check graph status on
     * @return If graphs are enabled for the given guild
     */
    fun getGraphEnabled(guildId: String): Mono<Boolean> =
            collection.find(GraphDisableDTO::guildId eq guildId)
                    .toMono()
                    .hasElement()
                    // present element means graphs are disabled, so we must flip it
                    .map { !it }
                    .subscribeOn(scheduler)

    /**
     * Changes the graph enabled setting.
     *
     * @param guildId The guild ID
     * @param enabled The graph-enabled state of the guild. This can be null to toggle
     * @return The updated graph status
     */
    fun changeGraphEnabled(guildId: String, enabled: Boolean? = null): Mono<Boolean> {
        val filter = GraphDisableDTO::guildId eq guildId
        val desiredSetting = enabled?.toMono()
                // invert the current graph setting as the desired setting to toggle it
                ?: getGraphEnabled(guildId).map { !it }

        val update = desiredSetting.flatMap { desiredSetting ->
            if (desiredSetting) {
                collection.deleteOne(filter).toMono()
            } else {
                collection.replaceOne(filter, GraphDisableDTO(guildId), replaceUpsert()).toMono()
            }.map {
                // give the desired enabled-status as the result
                desiredSetting
            }
        }

        return update.subscribeOn(scheduler)
    }

    companion object {
        val instance: GraphDisableDAO by lazy { GraphDisableDAO() }
    }
}
