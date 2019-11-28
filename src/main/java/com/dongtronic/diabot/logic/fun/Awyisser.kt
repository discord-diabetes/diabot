package com.dongtronic.diabot.logic.`fun`

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.google.gson.JsonParser
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod

object Awyisser {
    const val url = "http://awyisser.com/api/generator"

    /**
     * Generate Awyiss comic with given input
     * @return URL to the generated image
     */
    fun generate(input: String): String {
        val client = HttpClient()
        val method = PostMethod(url)

        //Add any parameter if u want to send it with Post req.
        method.addParameter("phrase", input)

        val statusCode = client.executeMethod(method)

        if (statusCode == -1) {
            throw RequestStatusException(-1)
        }

        val json = method.responseBodyAsString

        val jsonObject = JsonParser().parse(json).asJsonObject
        return jsonObject.get("link").asString
    }
}