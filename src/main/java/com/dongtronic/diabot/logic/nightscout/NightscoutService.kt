package com.dongtronic.diabot.logic.nightscout

import com.fasterxml.jackson.databind.JsonNode
import okhttp3.ResponseBody
import org.litote.kmongo.MongoOperator
import reactor.core.publisher.Mono
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface NightscoutService {
    @GET(NightscoutEndpoints.STATUS)
    fun getStatus(): Mono<Response<ResponseBody>>

    @GET(NightscoutEndpoints.STATUS)
    fun getStatusJson(): Mono<JsonNode>

    @GET(NightscoutEndpoints.PEBBLE)
    fun getPebble(): Mono<Response<ResponseBody>>

    @GET(NightscoutEndpoints.PEBBLE)
    fun getPebbleJson(): Mono<JsonNode>

    @GET(NightscoutEndpoints.ENTRIES)
    fun getEntries(
            @Query("count")
            count: Int = 1,
            @QueryMap
            extraParams: Map<String, String>
    ): Mono<Response<ResponseBody>>

    @GET(NightscoutEndpoints.ENTRIES)
    fun getEntriesJson(
            @Query("count")
            count: Int = 1,
            @QueryMap
            extraParams: Map<String, String>
    ): Mono<JsonNode>

    companion object {
        /**
         * Generates a `find` query parameter for Nightscout
         *
         * @param keyName The name of the key to search under
         * @param value The value to search for
         * @param operator An optional MongoDB operator to fine-tune the search.
         * @return A pair containing the query parameter name and value
         */
        // this can't be included directly in NightscoutService due to an incompatibility between Retrofit and Kotlin
        // https://github.com/square/retrofit/issues/2734#issuecomment-381741519
        fun find(keyName: String, value: String = "", operator: MongoOperator? = null): Pair<String, String> {
            val keyBuilder = StringBuilder("find")
            keyBuilder.append("[").append(keyName).append("]")

            if (operator != null) {
                keyBuilder.append("[$").append(operator.name).append("]")
            }

            return keyBuilder.toString() to value
        }
    }
}

