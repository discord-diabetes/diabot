package com.dongtronic.diabot.logic.`fun`

import com.dongtronic.diabot.data.`fun`.AwyisserRequestDTO
import com.dongtronic.diabot.exceptions.RequestStatusException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import reactor.core.publisher.Mono

object Awyisser {
    const val url = "https://awyisser.com/api/generator"
    private val httpClient = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    /**
     * Generate Awyiss comic with given input
     * @return URL to the generated image
     */
    fun generate(input: String): Mono<String> {
        val requestObject = AwyisserRequestDTO(value = input)
        val jsonRequest = mapper.writeValueAsString(requestObject)
        val mediaType = "application/json".toMediaType()

        val request = Request.Builder()
                .url(url)
                .post(jsonRequest.toRequestBody(mediaType))
                .build()

        return Mono.fromCallable {
            httpClient.newCall(request).execute()
        }.map {
            it.use { response ->
                if (response.isSuccessful) {
                    val body = response.body!!.string()
                    val json = jacksonObjectMapper().readTree(body)

                    json.get("image")?.textValue()
                            ?: throw Exception("Could not find awyiss image link")
                } else {
                    throw RequestStatusException(it.code)
                }
            }
        }
    }
}
