package com.dongtronic.nightscout

import com.fasterxml.jackson.databind.JsonNode
import okhttp3.ResponseBody
import reactor.core.publisher.Mono
import retrofit2.Response
import retrofit2.http.*

interface NightscoutService {
    @GET(NightscoutEndpoints.STATUS)
    fun getStatusResponse(): Mono<Response<ResponseBody>>

    @GET(NightscoutEndpoints.STATUS)
    fun getStatusJson(): Mono<JsonNode>

    @GET(NightscoutEndpoints.PEBBLE)
    fun getPebbleResponse(): Mono<Response<ResponseBody>>

    @GET(NightscoutEndpoints.PEBBLE)
    fun getPebbleJson(): Mono<JsonNode>

    @GET(NightscoutEndpoints.ENTRIES)
    fun getEntriesResponse(
        @QueryMap
        extraParams: Map<String, String> = emptyMap()
    ): Mono<Response<ResponseBody>>

    @GET(NightscoutEndpoints.ENTRIES)
    fun getEntriesJson(
        @Query("count")
        count: Int = 1,
        @QueryMap
        extraParams: Map<String, String> = emptyMap()
    ): Mono<JsonNode>

    @GET(NightscoutEndpoints.ENTRIES)
    fun getEntriesJson(
        @QueryMap
        extraParams: Map<String, String> = emptyMap()
    ): Mono<JsonNode>

    @Headers("Accept: application/json")
    @GET(NightscoutEndpoints.ENTRIES_SPEC)
    fun getEntriesSpecJson(
        @Path("spec")
        spec: String = "",
        @Query("count")
        count: Int = 1
    ): Mono<JsonNode>
}
