package com.dongtronic.diabot.util

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.match
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.reactivestreams.aggregate
import org.litote.kmongo.reactivestreams.find
import org.litote.kmongo.sample
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MongoDB {
    val client = KMongo.createClient(System.getenv("MONGO_URI"))
    val database: MongoDatabase = client.getDatabase("diabot")

    companion object {
        private var instance: MongoDB? = null

        fun getInstance(): MongoDB {
            if (instance == null) {
                instance = MongoDB()
            }
            return instance as MongoDB
        }
    }
}

/**
 * Collation for case-insensitive queries
 */
val caseCollation: Collation = Collation.builder()
        .collationStrength(CollationStrength.SECONDARY)
        .locale("en")
        .build()

fun <T> MongoCollection<T>.findOne(vararg filter: Bson): Mono<T> {
    return Mono.from(find(*filter).collation(caseCollation)).errorOnEmpty()
}

inline fun <reified T : Any> MongoCollection<T>.findOneRandom(vararg filter: Bson): Mono<T> {
    val filters = match(and(*filter))
    val count = sample(1)
    return Mono.from(aggregate<T>(filters, count).collation(caseCollation)).errorOnEmpty()
}

fun <T> MongoCollection<T>.findMany(vararg filter: Bson): Flux<T> {
    return Flux.from(find(*filter).collation(caseCollation)).errorOnEmpty()
}

fun <T> Mono<T>.errorOnEmpty(): Mono<T> {
    return switchIfEmpty(Mono.error(NoSuchElementException()))
}

fun <T> Flux<T>.errorOnEmpty(): Flux<T> {
    return switchIfEmpty(Flux.error(NoSuchElementException()))
}