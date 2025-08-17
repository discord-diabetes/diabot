package com.dongtronic.diabot.logic.nutrition

import com.dongtronic.diabot.data.nutrition.NutritionRequestDTO
import com.dongtronic.diabot.data.nutrition.NutritionResponseDTO
import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.util.logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import reactor.core.publisher.Mono

object NutritionixCommunicator {
    private val appid = System.getenv("nutritionixappid")
    private val secret = System.getenv("nutritionixsecret")
    private const val baseUrl = "https://trackapi.nutritionix.com/v2"
    private val mapper = jacksonObjectMapper()
    private val client = OkHttpClient()
    private val logger = logger()

    fun getNutritionInfo(input: String): Mono<NutritionResponseDTO> {
        val url = "$baseUrl/natural/nutrients"

        return Mono.fromCallable {
            val requestObject = NutritionRequestDTO(input)

            val jsonRequest = mapper.writeValueAsString(requestObject)

            val mediaType = "application/json".toMediaType()

            val request = Request.Builder()
            request.url(url)
            request.post(jsonRequest.toRequestBody(mediaType))
            request.addHeader("x-app-id", appid)
            request.addHeader("x-app-key", secret)
            request.addHeader("Accept", mediaType.toString())

            client.newCall(request.build()).execute()
        }.map {
            it.use { response ->
                val bodyString = response.body.string()
                if (response.code != 200) {
                    logger.warn("Nutritionix communication error. Status ${response.message}\n$bodyString")
                    throw RequestStatusException(response.code)
                }

                mapper.readValue(bodyString, NutritionResponseDTO::class.java)
            }
        }
    }
}
