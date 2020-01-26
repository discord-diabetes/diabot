package com.dongtronic.diabot.logic.`fun`

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.google.gson.JsonParser
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object Awyisser {
    const val url = "http://awyisser.com/api/generator"

    /**
     * Generate Awyiss comic with given input
     * @return URL to the generated image
     */
    fun generate(input: String): String {
        val client = HttpClients.createDefault()
        val request = RequestBuilder.post()

        //Add any parameter if u want to send it with Post req.
        request.addParameter("phrase", input)

        val response = client.execute(request.build())

        if (response.statusLine.statusCode == -1) {
            throw RequestStatusException(-1)
        }

        val json = EntityUtils.toString(response.entity)

        val jsonObject = JsonParser().parse(json).asJsonObject
        return jsonObject.get("link").asString
    }
}