package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.data.mongodb.QuoteIndexDTO
import com.dongtronic.diabot.util.logger
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.BsonType
import org.bson.conversions.Bson
import org.litote.kmongo.bson
import org.litote.kmongo.or
import org.litote.kmongo.project
import org.litote.kmongo.type
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Converts the quotes in MongoDB from `Int64` to `String` for the IDs' data type
 */
class MongoQuoteConversion(private val quotes: MongoCollection<QuoteDTO>, private val quoteIndexes: MongoCollection<QuoteIndexDTO>) {
    private val logger = logger()

    /**
     * Mongo filter for the old quote index format.
     * The quote index should be left as [Long] to use $inc in Mongo
     */
    private val oldQuoteIndexFilter: Bson = QuoteIndexDTO::guildId type BsonType.INT64

    /**
     * Mongo filter for the old quote format.
     */
    private val oldQuoteFilter: Bson = or(QuoteDTO::authorId type BsonType.INT64,
            QuoteDTO::channelId type BsonType.INT64,
            QuoteDTO::guildId type BsonType.INT64,
            QuoteDTO::messageId type BsonType.INT64,
            QuoteDTO::quoteId type BsonType.INT64)

    /**
     * Checks the quotes collection and migrates if there's any documents which need to be converted
     *
     * @return A [Tuple2] holding the amount of quotes (`t1`) and quote indexes (`t2`) that were converted.
     * If there were no documents which needed to be converted then the [Mono] will be empty
     */
    fun checkAndConvert(): Mono<Tuple2<Long, Long>> {
        return needsConversion().flatMap {
            if (it) {
                convertQuotes().zipWhen { convertIndexes() }
            } else {
                Mono.empty()
            }
        }
    }

    /**
     * Checks whether conversions are necessary for either the quotes or quote index collections.
     *
     * @return True if either the `quotes` or `quote-index` collections need any documents to be converted
     */
    fun needsConversion(): Mono<Boolean> {
        return quotes.countDocuments(oldQuoteFilter).toMono()
                .zipWhen { quoteIndexes.countDocuments(oldQuoteIndexFilter).toMono() }
                .doOnNext { logger.debug("Got ${it.t1} quote(s) and ${it.t2} quote index(es) that need conversion") }
                .map { it.t1 != 0L || it.t2 != 0L }
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