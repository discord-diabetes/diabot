package com.dongtronic.diabot.util

import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.conversions.Bson
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.reactivestreams.find
import org.litote.kmongo.reactivestreams.findOne
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
fun <T> MongoCollection<T>.findOne(vararg filter: Bson): Mono<T> {
    return Mono.from(findOne(*filter)).errorOnEmpty()
}

fun <T> MongoCollection<T>.findMany(vararg filter: Bson): Flux<T> {
    return Flux.from(find(*filter)).errorOnEmpty()
}

fun <T> Mono<T>.errorOnEmpty(): Mono<T> {
    return switchIfEmpty(Mono.error(NoSuchElementException()))
}

fun <T> Flux<T>.errorOnEmpty(): Flux<T> {
    return switchIfEmpty(Flux.error(NoSuchElementException()))
}