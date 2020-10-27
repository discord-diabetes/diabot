package com.dongtronic.diabot.data.nutrition

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("thumb", "highres", "is_user_uploaded")
class Photo {
    @get:JsonProperty("thumb")
    @set:JsonProperty("thumb")
    @JsonProperty("thumb")
    var thumb: String? = null
    @get:JsonProperty("highres")
    @set:JsonProperty("highres")
    @JsonProperty("highres")
    var highres: String? = null
    @get:JsonProperty("is_user_uploaded")
    @set:JsonProperty("is_user_uploaded")
    @JsonProperty("is_user_uploaded")
    var isUserUploaded: Boolean? = null
    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }
}