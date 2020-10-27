package com.dongtronic.diabot.logic.`fun`

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import reactor.core.publisher.Mono

object Awyisser {
    const val url = "http://awyisser.com/api/generator"
    private val httpClient = OkHttpClient()

    /**
     * Generate Awyiss comic with given input
     * @return URL to the generated image
     */
    fun generate(input: String): Mono<String> {
        val requestBody = FormBody.Builder()
                .add("phrase", input)
                .build()
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        return Mono.fromCallable {
            httpClient.newCall(request).execute()
        }.map {
            it.use { response ->
                if (response.isSuccessful) {
                    val body = response.body!!.string()
                    val json = jacksonObjectMapper().readTree(body)

                    json.get("link")?.textValue()
                            ?: throw Exception("Could not find awyiss image link")
                } else {
                    throw RequestStatusException(it.code)
                }
            }
        }
    }
}