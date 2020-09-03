package com.dongtronic.diabot.logic.nightscout

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface NightscoutService {
    @GET(NightscoutEndpoints.STATUS)
    fun getStatus(): Call<ResponseBody>

    @GET(NightscoutEndpoints.PEBBLE)
    fun getPebble(): Call<ResponseBody>

    @GET(NightscoutEndpoints.ENTRIES)
    fun getEntries(
            @Query("count")
            count: Int = 1,
            @QueryMap
            extraParams: Map<String, String>
    ): Call<ResponseBody>
}