package com.dongtronic.diabot.data.nutrition

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("foods")
class NutritionResponseDTO {
    @get:JsonProperty("foods")
    @set:JsonProperty("foods")
    @JsonProperty("foods")
    var foods: List<Food> = ArrayList()
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

    val totalCarbs: Double
        get() = foods.stream().filter { food: Food -> food.totalCarbohydrate != null }.mapToDouble { food -> food.totalCarbohydrate!!.toDouble() }.sum()

    val totalFats: Double
        get() = foods.stream().filter { food: Food -> food.totalFat != null }.mapToDouble { it.totalFat!!.toDouble() }.sum()

    val totalSaturatedFats: Double
        get() = foods.stream().filter { food: Food -> food.saturatedFat != null }.mapToDouble { it.saturatedFat!!.toDouble() }.sum()

    val totalFibers: Double
        get() = foods.stream().filter { food: Food -> food.dietaryFiber != null }.mapToDouble { it.dietaryFiber!!.toDouble() }.sum()

    override fun toString(): String {
        val returned = StringBuilder()
        for (food in foods) {
            returned.append(food.foodName)
                    .append(" (").append(food.servingQty)
                    .append(" ").append(food.servingUnit)
                    .append(")\n")
        }
        return returned.toString()
    }
}