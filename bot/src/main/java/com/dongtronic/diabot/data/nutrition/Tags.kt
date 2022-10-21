package com.dongtronic.diabot.data.nutrition

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("item", "measure", "quantity", "food_group", "tag_id")
class Tags {
    @get:JsonProperty("item")
    @set:JsonProperty("item")
    @JsonProperty("item")
    var item: String? = null
    @get:JsonProperty("measure")
    @set:JsonProperty("measure")
    @JsonProperty("measure")
    var measure: String? = null
    @get:JsonProperty("quantity")
    @set:JsonProperty("quantity")
    @JsonProperty("quantity")
    var quantity: String? = null
    @get:JsonProperty("food_group")
    @set:JsonProperty("food_group")
    @JsonProperty("food_group")
    var foodGroup: Int? = null
    @get:JsonProperty("tag_id")
    @set:JsonProperty("tag_id")
    @JsonProperty("tag_id")
    var tagId: Int? = null
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
