package com.dongtronic.diabot.data.nutrition

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("serving_weight", "measure", "seq", "qty")
class AltMeasure {
    @get:JsonProperty("serving_weight")
    @set:JsonProperty("serving_weight")
    @JsonProperty("serving_weight")
    var servingWeight: Float? = null
    @get:JsonProperty("measure")
    @set:JsonProperty("measure")
    @JsonProperty("measure")
    var measure: String? = null
    @get:JsonProperty("seq")
    @set:JsonProperty("seq")
    @JsonProperty("seq")
    var seq: Int? = null
    @get:JsonProperty("qty")
    @set:JsonProperty("qty")
    @JsonProperty("qty")
    var qty: Int? = null
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
