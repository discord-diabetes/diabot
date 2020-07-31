package com.dongtronic.diabot.logic.nutrition

import com.dongtronic.diabot.data.nutrition.NutritionRequestDTO
import com.dongtronic.diabot.data.nutrition.NutritionResponseDTO
import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.util.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object NutritionixCommunicator {
    private val appid = System.getenv("nutritionixappid")
    private val secret = System.getenv("nutritionixsecret")
    private const val baseUrl = "https://trackapi.nutritionix.com/v2"
    private val mapper = ObjectMapper()
    private val logger by Logger()

    public fun getNutritionInfo(input: String): NutritionResponseDTO {
        val url = "$baseUrl/natural/nutrients"

        val requestObject = NutritionRequestDTO(input)

        val jsonRequest = mapper.writeValueAsString(requestObject)

        val client = HttpClients.createDefault()
        val request = RequestBuilder.post()

        request.entity = StringEntity(jsonRequest)

        //Add any parameter if u want to send it with Post req.
        request.setUri(url)

        request.addHeader("x-app-id", appid)
        request.addHeader("x-app-key", secret)
        request.addHeader("Accept", "application/json")
        request.addHeader("Content-Type", "application/json")

        val response = client.execute(request.build())

        if (response.statusLine.statusCode != 200) {
            logger.warn("Nutritionix communication error. Status ${response.statusLine}\n${EntityUtils.toString(response.entity)}")
            throw RequestStatusException(response.statusLine.statusCode)
        }

        return mapper.readValue<NutritionResponseDTO>(EntityUtils.toString(response.entity), NutritionResponseDTO::class.java)
    }
}