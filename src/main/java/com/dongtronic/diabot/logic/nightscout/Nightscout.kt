package com.dongtronic.diabot.logic.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDTO
import com.dongtronic.diabot.exceptions.NoNightscoutDataException
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.util.logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.litote.kmongo.MongoOperator
import reactor.core.publisher.Mono
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.Closeable
import java.io.IOException
import java.time.Instant

class Nightscout(baseUrl: String, token: String? = null) : Closeable {
    private val logger = logger()
    private val responseCache = hashMapOf<String, Response.Builder>()
    private val httpClient: OkHttpClient
    private val service: NightscoutService

    init {
        val client = OkHttpClient.Builder()
        if (token != null) {
            // creates an interceptor which adds the auth token to the parameters
            client.interceptors().add(Interceptor {
                val newUrl = it.request()
                        .url
                        .newBuilder()
                        .addQueryParameter("token", token)
                        .build()

                val newRequest = it.request()
                        .newBuilder()
                        .url(newUrl)
                        .build()

                return@Interceptor it.proceed(newRequest)
            })
        }

        client.interceptors().add(Interceptor { chain ->
            // intercept requests and provide cached responses / cache network responses
            val request = chain.request()

            return@Interceptor responseCache[request.toString()].let { cached ->
                if (cached == null) {
                    logger.debug("Performing network request for endpoint ${request.url.encodedPath}")
                    val networkResponse = chain.proceed(request)
                    val body = networkResponse.peekBody(1024*1024)
                    responseCache[request.toString()] = networkResponse.newBuilder().body(body)
                    networkResponse
                } else {
                    logger.debug("Providing cached response for endpoint ${request.url.encodedPath}")
                    cached.build()
                }
            }
        })

        httpClient = client.build()
        service = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addCallAdapterFactory(ReactorCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
                .build()
                .create(NightscoutService::class.java)
    }

    /**
     * Tests if the URL is a valid Nightscout instance
     *
     * @return true if the instance exists, false if not
     */
    fun isNightscoutInstance(): Mono<Boolean> {
        return service.getStatus().map { it.code() != 404 }
    }

    /**
     * Tests whether a Nightscout requires a token to be read.
     * This does not actually test whether or not the instance is *secured*, it tests whether a token is currently needed.
     * If this class was instantiated with the `token` parameter filled, and it grants read access to this NS, then this will be false.
     *
     * @return true if a token is required, false if not
     */
    fun needsNightscoutToken(): Mono<Boolean> {
        return service.getStatus().map { it.code() == 401 }
    }

    /**
     * Fetches a Nightscout's settings and puts the data in a [NightscoutDTO] instance
     *
     * @param dto NS data
     */
    fun getSettings(dto: NightscoutDTO = NightscoutDTO()): Mono<NightscoutDTO> {
        return service.getStatusJson().map { json ->
            val settings = json.path("settings")
            val ranges = json.path("thresholds")

            val title = settings.path("customTitle").asText()
            val units = settings.path("units").asText()
            val low = ranges.path("bgLow").asInt()
            val bottom = ranges.path("bgTargetBottom").asInt()
            val top = ranges.path("bgTargetTop").asInt()
            val high = ranges.path("bgHigh").asInt()

            dto.title = title
            dto.low = low
            dto.bottom = bottom
            dto.top = top
            dto.high = high
            dto.units = units

            return@map dto
        }
    }

    /**
     * Fetches the `pebble` endpoint for a Nightscout instance and puts the data in a [NightscoutDTO] instance
     *
     * @param dto NS DTO
     */
    fun getPebble(dto: NightscoutDTO = NightscoutDTO()): Mono<NightscoutDTO> {
        return service.getPebbleJson().map { json ->
            val bgsJson = json.get("bgs")?.get(0)

            if (bgsJson == null) {
                logger.warn("Failed to get bgs object from pebble Endpoint JSON:\n${json.toPrettyString()}")
                return@map dto
            }

            if (bgsJson.has("cob")) {
                dto.cob = bgsJson.get("cob").asInt()
            }
            if (bgsJson.has("iob")) {
                dto.iob = bgsJson.get("iob").textValue().toFloat()
            }
            val bgDelta = bgsJson.get("bgdelta").asText()
            if (dto.delta == null) {
                dto.deltaIsNegative = bgDelta.contains("-")
                dto.delta = BloodGlucoseConverter.convert(bgDelta.replace("-".toRegex(), ""), dto.units)
            }
            return@map dto
        }
    }

    /**
     * Fetches a Nightscout's most recent SGV and puts the data in a [NightscoutDTO] instance
     *
     * @param dto NS data
     * @throws IOException
     * @throws UnknownUnitException
     */
    fun getRecentSgv(dto: NightscoutDTO = NightscoutDTO()): Mono<NightscoutDTO> {
        val findParam = mapOf(find("sgv", operator = MongoOperator.exists))
        return service.getEntriesJson(extraParams = findParam).map { json ->
            if (json.isEmpty) {
                throw NoNightscoutDataException()
            }

            // Parse JSON and construct response
            val jsonObject = json.get(0)
            val sgv = jsonObject.path("sgv").asText()
            val timestamp = jsonObject.path("date").asLong()
            var trend = 0
            val direction: String
            if (jsonObject.has("trend")) {
                trend = jsonObject.path("trend").asInt()
            } else if (jsonObject.has("direction")) {
                direction = jsonObject.path("direction").asText()
                trend = TrendArrows.getTrend(direction).ordinal
            }

            var delta = ""
            if (jsonObject.has("delta")) {
                delta = jsonObject.path("delta").asText()
            }

            val convertedBg = BloodGlucoseConverter.convert(sgv, "mg")

            if (delta.isNotEmpty()) {
                val convertedDelta = BloodGlucoseConverter.convert(delta.replace("-".toRegex(), ""), "mg")
                dto.delta = convertedDelta
            }

            dto.glucose = convertedBg
            dto.deltaIsNegative = delta.contains("-")
            dto.dateTime = Instant.ofEpochMilli(timestamp)
            dto.trend = trend

            return@map dto
        }
    }

    override fun close() {
        logger.debug("Closing Nightscout API")
        responseCache.forEach { (_, response) ->
            // clear the cache bodies
            response.body(null)
        }
        responseCache.clear()
    }
}