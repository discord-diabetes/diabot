package com.dongtronic.diabot.logic.nightscout

import com.dongtronic.diabot.data.redis.NightscoutDTO
import com.dongtronic.diabot.exceptions.NightscoutStatusException
import com.dongtronic.diabot.exceptions.NoNightscoutDataException
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.util.LimitedSystemDnsResolver
import com.dongtronic.diabot.util.Logger
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object NightscoutCommunicator {
    private val logger by Logger()
    private val httpClient: HttpClient
    private val requestConfig: RequestConfig
    private val defaultQuery: Array<NameValuePair> = arrayOf(
            BasicNameValuePair("find[sgv][\$exists]", ""),
            BasicNameValuePair("count", "1"))

    init {
        httpClient = HttpClientBuilder
                .create()
                // Limit to two IP addresses per hostname
                .setDnsResolver(LimitedSystemDnsResolver(2))
                .build()

        requestConfig = RequestConfig
                .custom()
                // Set timeouts to 8 seconds
                .setSocketTimeout(8000)
                .setConnectionRequestTimeout(8000)
                .setConnectTimeout(8000)
                .build()
    }

    /**
     * Tests whether a Nightscout requires a token to be read
     *
     * @return true if a token is required, false if not
     */
    fun needsNightscoutToken(domain: String): Boolean {
        try {
            getJson("$domain/api/v1/status", null)
        } catch (exception: NightscoutStatusException) {
            // If an unauthorized error occurs when trying to retrieve the status page, a token is needed
            if (exception.status == 401) {
                return true
            }
        }

        return false
    }

    /**
     * Tests if the domain provided is a valid Nightscout instance
     *
     * @return true if the instance exists, false if not
     */
    fun isNightscoutInstance(domain: String): Boolean {

        val request = RequestBuilder.get()
        val url = "$domain/api/v1/status"
        request.setUri(url)
        request.config = requestConfig

        val response = httpClient.execute(request.build())
        val statusCode = response.statusLine.statusCode
        if (statusCode == 404) {
            return false
        }
        return true
    }

    /**
     * Loads a URL and gets its output as a string.
     *
     * @param url URL to load.
     * @param token Nightscout token, if needed.
     * @param query Parameters to use for the request.
     * @return The URL's contents
     */
    fun getJson(url: String, token: String?, vararg query: NameValuePair): String {
        val request = RequestBuilder.get()

        if (token != null) {
            request.addParameter("token", token)
        }

        request.addParameters(*query)
        request.setUri(url)
        request.config = requestConfig

        val response = httpClient.execute(request.build())
        val statusCode = response.statusLine.statusCode

        if (statusCode != 200) {
            throw NightscoutStatusException(statusCode)
        }

        val body = EntityUtils.toString(response.entity)

        if (body.isEmpty()) {
            throw NoNightscoutDataException()
        }

        return body
    }

    /**
     * Parses the `pebble` NS endpoint
     *
     * @param url NS URL
     * @param token NS token, if any.
     * @param dto NS DTO
     */
    fun processPebble(url: String, token: String?, dto: NightscoutDTO) {
        val endpoint = url.replace("/api/v1/", "/pebble")
        val json = getJson(endpoint, token)

        val bgsJson: JsonObject

        if (JsonParser().parse(json).asJsonObject.has("bgs"))
            bgsJson = JsonParser().parse(json).asJsonObject.get("bgs").asJsonArray.get(0).asJsonObject
        else {
            logger.warn("Failed to get bgs Object from pebbleEndpoint JSON:\n$json")
            return
        }

        if (bgsJson.has("cob")) {
            dto.cob = bgsJson.get("cob").asInt
        }
        if (bgsJson.has("iob")) {
            dto.iob = bgsJson.get("iob").asFloat
        }
        val bgDelta = bgsJson.get("bgdelta").asString
        if (dto.delta == null) {
            dto.deltaIsNegative = bgDelta.contains("-")
            dto.delta = BloodGlucoseConverter.convert(bgDelta.replace("-".toRegex(), ""), dto.units)
        }
    }

    /**
     * Loads Nightscout's `entries.json` API endpoint and gets its contents.
     *
     * @param url NS base URL to load
     * @param token NS token, if necessary
     * @return The `entries.json` endpoint's contents.
     * @throws MalformedJsonException
     */
    @Throws(MalformedJsonException::class)
    fun getGlucoseJson(url: String, token: String?): String {
        val endpoint = "$url/entries.json"
        val json = getJson(endpoint, token, *defaultQuery)

        val jsonArray = JsonParser().parse(json).asJsonArray
        val arraySize = jsonArray.size()

        // Throw an exception if the endpoint is empty of SGV entries
        if (arraySize == 0) {
            throw NoNightscoutDataException()
        }

        return json
    }

    /**
     * Fetches a Nightscout's entries and puts the data in a [NightscoutDTO] instance
     *
     * @param url NS base URL to load
     * @param token NS token, if necessary
     * @param dto NS data
     * @throws IOException
     * @throws UnknownUnitException
     */
    @Throws(IOException::class, UnknownUnitException::class)
    fun getEntries(url: String, token: String?, dto: NightscoutDTO) {
        val json = getGlucoseJson(url, token)

        if (json.isEmpty()) {
            throw NoNightscoutDataException()
        }

        // Parse JSON and construct response
        val jsonObject = JsonParser().parse(json).asJsonArray.get(0).asJsonObject
        val sgv = jsonObject.get("sgv").asString
        val timestamp = jsonObject.get("date").asLong
        var trend = 0
        val direction: String
        if (jsonObject.has("trend")) {
            trend = jsonObject.get("trend").asInt
        } else if (jsonObject.has("direction")) {
            direction = jsonObject.get("direction").asString
            trend = when (direction.toUpperCase()) {
                "NONE" -> 0
                "DOUBLEUP" -> 1
                "SINGLEUP" -> 2
                "FORTYFIVEUP" -> 3
                "FLAT" -> 4
                "FORTYFIVEDOWN" -> 5
                "SINGLEDOWN" -> 6
                "DOUBLEDOWN" -> 7
                "NOT COMPUTABLE" -> 8
                "RATE OUT OF RANGE" -> 9
                else -> {
                    throw IllegalArgumentException("Unknown direction $direction")
                }
            }
        }

        var delta = ""
        if (jsonObject.has("delta")) {
            delta = jsonObject.get("delta").asString
        }
        val dateTime = getTimestamp(timestamp)

        val convertedBg = BloodGlucoseConverter.convert(sgv, "mg")

        if (delta.isNotEmpty()) {
            val convertedDelta = BloodGlucoseConverter.convert(delta.replace("-".toRegex(), ""), "mg")
            dto.delta = convertedDelta
        }

        dto.glucose = convertedBg
        dto.deltaIsNegative = delta.contains("-")
        dto.dateTime = dateTime
        dto.trend = trend
    }

    /**
     * Fetches a Nightscout's settings and puts the data in a [NightscoutDTO] instance
     *
     * @param url NS base URL to load
     * @param token NS token, if necessary
     * @param dto NS data
     */
    fun getSettings(url: String, token: String?, dto: NightscoutDTO) {
        val endpoint = "$url/status.json"
        val json = getJson(endpoint, token)

        val jsonObject = JsonParser().parse(json).asJsonObject
        val settings = jsonObject.get("settings").asJsonObject
        val ranges = settings.get("thresholds").asJsonObject

        val title = settings.get("customTitle").asString
        val units = settings.get("units").asString
        val low = ranges.get("bgLow").asInt
        val bottom = ranges.get("bgTargetBottom").asInt
        val top = ranges.get("bgTargetTop").asInt
        val high = ranges.get("bgHigh").asInt

        dto.title = title
        dto.low = low
        dto.bottom = bottom
        dto.top = top
        dto.high = high
        dto.units = units
    }

    /**
     * Converts a unix epoch in milliseconds to a [ZonedDateTime] instance
     *
     * @param epoch Unix epoch (in milliseconds)
     * @return [ZonedDateTime]
     */
    fun getTimestamp(epoch: Long?): ZonedDateTime {
        val i = Instant.ofEpochSecond(epoch!! / 1000)
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC)
    }
}