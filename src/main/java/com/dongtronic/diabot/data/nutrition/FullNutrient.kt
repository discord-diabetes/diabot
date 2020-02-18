package com.dongtronic.diabot.data.nutrition

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("attr_id", "value")
class FullNutrient {
    @get:JsonProperty("attr_id")
    @set:JsonProperty("attr_id")
    @JsonProperty("attr_id")
    var attrId: Int? = null
    @get:JsonProperty("value")
    @set:JsonProperty("value")
    @JsonProperty("value")
    var value: Int? = null
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