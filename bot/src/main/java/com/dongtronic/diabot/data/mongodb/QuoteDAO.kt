package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.data.migration.MongoQuoteConversion
import com.dongtronic.diabot.util.*
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactor.awaitSingleOrNull
import net.dv8tion.jda.api.entities.TextChannel
import org.bson.conversions.Bson
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.updateOne
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

class QuoteDAO private constructor() {
    val collection: MongoCollection<QuoteDTO>
            = MongoDB.getInstance().database.getCollection(DiabotCollection.QUOTES.getEnv(), QuoteDTO::class.java)
    val quoteIndexes: MongoCollection<QuoteIndexDTO>
            = MongoDB.getInstance().database.getCollection(DiabotCollection.QUOTE_INDEX.getEnv(), QuoteIndexDTO::class.java)

    private val scheduler = Schedulers.boundedElastic()
    private val logger = logger()
    val enabledGuilds = System.getenv().getOrDefault("QUOTE_ENABLE_GUILDS", "").split(",")
    val maxQuotes = System.getenv().getOrDefault("QUOTE_MAX", "5000").toIntOrNull() ?: 5000

    init {
        // Create a unique index
        val options = IndexOptions().unique(true).name(QuoteDTO::guildId.name)
        quoteIndexes.createIndex(descending(QuoteDTO::guildId), options).toMono()
                .subscribeOn(scheduler)
                .subscribe()

        // Convert IDs stored in the collection from `Long` to `String`
        MongoQuoteConversion(collection, quoteIndexes).checkAndConvert()
                .errorOnEmpty()
                .subscribe({
                    logger.info("Converted ${it.t1} quote(s) and ${it.t2} quote index(es)!")
                }, {
                    if (it is NoSuchElementException) {
                        logger.info("No quotes needed to be converted")
                    } else {
                        logger.warn("Could not convert quotes", it)
                    }
                })
    }

    /**
     * Gets a [QuoteDTO] from a quote ID
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return [QuoteDTO] instance matching the quote ID
     */
    fun getQuote(guildId: String, quoteId: String): Mono<QuoteDTO> {
        return collection.findOne(filter(guildId, quoteId)).subscribeOn(scheduler)
    }

    /**
     * Returns all quotes defined under a guild matching the given filter, if any
     *
     * @param guildId guild ID
     * @param filter the mongo filter to match against, if any
     * @return all quotes matching the filter for the specified guild
     */
    fun getQuotes(guildId: String, filter: Bson? = null): Flux<QuoteDTO> {
        val joinedFilter = if (filter != null)
            and(filter(guildId), filter)
        else
            filter(guildId)

        return collection.findMany(joinedFilter).subscribeOn(scheduler)
    }

    /**
     * Returns a random quote defined under a guild matching the given filter, if any
     *
     * @param guildId guild ID
     * @param filter the mongo filter to match against, if any
     * @return a random quote matching the filter for the specified guild
     */
    fun getRandomQuote(guildId: String, filter: Bson? = null): Mono<QuoteDTO> {
        val joinedFilter = if (filter != null)
            and(filter(guildId), filter)
        else
            filter(guildId)

        return collection.findOneRandom(joinedFilter).subscribeOn(scheduler)
    }

    /**
     * Inserts a quote into the database.
     * If the provided quote's ID is null, this will create a copy of it with a valid quote ID
     *
     * @param quote the quote to insert
     * @param incrementId whether to increment the guild-wide quote ID index.
     * This will only have an effect if the quote already has a valid quote ID
     * @return the created [QuoteDTO] instance if successful
     */
    fun addQuote(quote: QuoteDTO, incrementId: Boolean = true): Mono<QuoteDTO> {
        val quoteWithId = if (incrementId || quote.quoteId == null) {
            incrementId(quote.guildId)
                    .onErrorMap { IllegalStateException("Could not find guild's quote index") }
                    .map {
                        if (quote.quoteId == null)
                            quote.copy(quoteId = it.toString())
                        else
                            quote
                    }
        } else {
            quote.toMono()
        }

        return quoteWithId.flatMap { quoteDTO ->
            collection.insertOne(quoteDTO).toMono().map {
                if (!it.wasAcknowledged())
                    throw IllegalStateException("Could not insert quote")

                quoteDTO
            }
        }.subscribeOn(scheduler)
    }

    /**
     * Updates the data of a [QuoteDTO] instance in the database
     *
     * @param quoteDTO quote DTO
     * @return the result of the update command
     */
    fun updateQuote(quoteDTO: QuoteDTO): Mono<UpdateResult> {
        return collection.updateOne(filter(quoteDTO.guildId, quoteDTO.quoteId), quoteDTO)
                .toMono().subscribeOn(scheduler)
    }

    /**
     * Deletes a quote from the database
     *
     * @param guildId guild ID
     * @param quoteId quote ID
     * @return the result of the delete command
     */
    fun deleteQuote(guildId: String, quoteId: String): Mono<DeleteResult> {
        return collection.deleteOne(filter(guildId, quoteId))
                .toMono().subscribeOn(scheduler)
    }

    /**
     * Gets the amount of quotes in this guild
     *
     * @param guildId guild ID
     * @return number of quotes for the specified guild
     */
    fun quoteAmount(guildId: String): Mono<Long> {
        return collection.countDocuments(filter(guildId))
                .toMono().subscribeOn(scheduler)
    }

    /**
     * Increments the guild-wide quote ID index by one and returns the index after incrementing
     *
     * @param guildId guild ID
     * @return the ID index of the specified guild after incrementing
     */
    fun incrementId(guildId: String): Mono<Long> {
        // insert if not existing and return the document after incrementing
        val options = FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER)

        return quoteIndexes.findOneAndUpdate(QuoteIndexDTO::guildId eq guildId,
                Updates.inc("quoteIndex", 1L), options)
                .toMono()
                .subscribeOn(scheduler)
                .map { it.quoteIndex }
    }

    companion object {
        private var instance: QuoteDAO? = null

        fun getInstance(): QuoteDAO {
            if (instance == null) {
                instance = QuoteDAO()
            }
            return instance as QuoteDAO
        }

        /**
         * Checks the restrictions for the guild which the channel belongs to.
         * This initiates a blocking call to quoteAmount().
         *
         * @param channel the channel to send messages to
         * @param warnDisabledGuild whether to send a warning message when the guild is not enabled for quotes
         * @param checkQuoteLimit whether to check if the guild has reached the max quote limit
         * @return true if the guild passed restrictions, false if not
         */
        fun checkRestrictions(channel: TextChannel,
                              warnDisabledGuild: Boolean = false,
                              checkQuoteLimit: Boolean = true): Boolean {
            if (!getInstance().enabledGuilds.contains(channel.guild.id)) {
                if (warnDisabledGuild) {
                    channel.sendMessage("This guild is not permitted to use the quoting system").queue()
                }
                return false
            }

            val numOfQuotes = getInstance().quoteAmount(channel.guild.id).block() ?: -1
            if (checkQuoteLimit && numOfQuotes >= getInstance().maxQuotes) {
                channel.sendMessage("Could not create quote as your guild has reached " +
                        "the max of ${getInstance().maxQuotes} quotes").queue()
                return false
            }
            return true
        }

        /**
         * Checks the restrictions for the guild which the channel belongs to.
         * This awaits the guild quote amount from [quoteAmount].
         *
         * @param channel the channel to send messages to
         * @param warnDisabledGuild whether to send a warning message when the guild is not enabled for quotes
         * @param checkQuoteLimit whether to check if the guild has reached the max quote limit
         * @return true if the guild passed restrictions, false if not
         */
        suspend fun awaitCheckRestrictions(channel: TextChannel,
                              warnDisabledGuild: Boolean = false,
                              checkQuoteLimit: Boolean = true): Boolean {
            if (!getInstance().enabledGuilds.contains(channel.guild.id)) {
                if (warnDisabledGuild) {
                    channel.sendMessage("This guild is not permitted to use the quoting system").queue()
                }
                return false
            }

            val numOfQuotes = getInstance().quoteAmount(channel.guild.id).awaitSingleOrNull() ?: -1
            if (checkQuoteLimit && numOfQuotes >= getInstance().maxQuotes) {
                channel.sendMessage("Could not create quote as your guild has reached " +
                        "the max of ${getInstance().maxQuotes} quotes").queue()
                return false
            }
            return true
        }

        /**
         * Creates a Mongo query filter for the specified guild ID and (optionally) quote ID.
         * If quote ID is null then this query will only search for quotes under a guild.
         *
         * @param guildId guild ID
         * @param quoteId quote ID, optional
         * @return MongoDB query filter for the provided guild ID and quote ID, if any
         */
        fun filter(guildId: String, quoteId: String? = null): Bson {
            val guildFilter = QuoteDTO::guildId.eq(guildId)

            if (quoteId != null) {
                return and(guildFilter, QuoteDTO::quoteId.eq(quoteId))
            }

            return guildFilter
        }
    }
}