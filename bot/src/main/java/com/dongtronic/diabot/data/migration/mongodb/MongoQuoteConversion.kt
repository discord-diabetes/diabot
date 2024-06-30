package com.dongtronic.diabot.data.migration.mongodb

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.data.mongodb.QuoteIndexDTO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.BsonType
import org.bson.conversions.Bson
import org.litote.kmongo.bson
import org.litote.kmongo.or
import org.litote.kmongo.project
import org.litote.kmongo.type
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Converts the quotes in MongoDB from `Int64` to `String` for the IDs' data type
 */
@ChangeLog(order = "008")
class MongoQuoteConversion {
    private val logger = logger()
    private val quotes: MongoCollection<QuoteDTO> = QuoteDAO.getInstance().collection
    private val quoteIndexes: MongoCollection<QuoteIndexDTO> = QuoteDAO.getInstance().quoteIndexes

    /**
     * Mongo filter for the old quote index format.
     * The quote index should be left as [Long] to use $inc in Mongo
     */
    private val oldQuoteIndexFilter: Bson = QuoteIndexDTO::guildId type BsonType.INT64

    /**
     * Mongo filter for the old quote format.
     */
    private val oldQuoteFilter: Bson = or(
        QuoteDTO::authorId type BsonType.INT64,
        QuoteDTO::channelId type BsonType.INT64,
        QuoteDTO::guildId type BsonType.INT64,
        QuoteDTO::messageId type BsonType.INT64,
        QuoteDTO::quoteId type BsonType.INT64
    )

    /**
     * Checks the quotes collection and migrates if there's any documents which need to be converted from using Int64 to String
     */
    @ChangeSet(order = "001", id = "quoteIdTypesFromInt64ToString", author = "Garlic")
    fun checkAndConvert() {
        val quotes = convertQuotes().block()!!
        val indexes = convertIndexes().block()!!

        logger.info("Converted $quotes quote(s) and $indexes quote index(es)!")
    }

    /**
     * Converts the `quote-indexes` collection.
     *
     * @return The amount of quote indexes that were converted
     */
    fun convertIndexes(): Mono<Long> {
        return quoteIndexes.updateMany(oldQuoteIndexFilter, updatePipeline(QuoteIndexDTO::class)).toMono()
            .doOnNext { logger.debug("Converted ${it.modifiedCount} quote indexes") }
            .map { it.modifiedCount }
    }

    /**
     * Converts the `quotes` collection.
     *
     * @return The amount of quotes that were converted
     */
    fun convertQuotes(): Mono<Long> {
        return quotes.updateMany(oldQuoteFilter, updatePipeline(QuoteDTO::class)).toMono()
            .doOnNext { logger.debug("Converted ${it.modifiedCount} quotes") }
            .map { it.modifiedCount }
    }

    /**
     * Creates a MonboDB update pipeline for `idToStringConvertUpdates()` using a KClass's properties.
     *
     * @param kclass The [KClass] to create an update pipeline for
     * @return An update pipeline for the class's properties.
     */
    fun <T : Any> updatePipeline(kclass: KClass<T>): List<Bson> {
        return listOf(project(idToStringConvertUpdates(kclass.memberProperties)))
    }

    /**
     * Generates the update expressions for converting ID keys from Longs to Strings.
     *
     * Example:
     *
     * `"quoteId": { $toString: "$quoteId" }`
     *
     * `"author": 1`
     *
     * @param properties The list of KProperties to make update expressions for
     * @return A map containing each [KProperty] given, joined with their necessary update expression
     */
    fun <T, U> idToStringConvertUpdates(properties: Collection<KProperty1<T, U>>): Map<KProperty1<T, U>, Serializable> {
        return properties.associateWith {
            if (it.name.endsWith("Id")) {
                return@associateWith """{ ${'$'}toString: "${'$'}${it.name}" }""".bson
            } else {
                return@associateWith 1
            }
        }
    }
}
