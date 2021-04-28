package com.dongtronic.diabot.data.nutrition

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("food_name", "brand_name", "serving_qty", "serving_unit", "serving_weight_grams", "nf_calories", "nf_total_fat", "nf_saturated_fat", "nf_cholesterol", "nf_sodium", "nf_total_carbohydrate", "nf_dietary_fiber", "nf_sugars", "nf_protein", "nf_potassium", "nf_p", "full_nutrients", "nix_brand_name", "nix_brand_id", "nix_item_name", "nix_item_id", "upc", "consumed_at", "metadata", "source", "ndb_no", "tags", "alt_measures", "lat", "lng", "meal_type", "photo", "sub_recipe")
class Food {
    @get:JsonProperty("food_name")
    @set:JsonProperty("food_name")
    @JsonProperty("food_name")
    var foodName: String? = null
    @get:JsonProperty("brand_name")
    @set:JsonProperty("brand_name")
    @JsonProperty("brand_name")
    var brandName: Any? = null
    @get:JsonProperty("serving_qty")
    @set:JsonProperty("serving_qty")
    @JsonProperty("serving_qty")
    var servingQty: Int? = null
    @get:JsonProperty("serving_unit")
    @set:JsonProperty("serving_unit")
    @JsonProperty("serving_unit")
    var servingUnit: String? = null
    @get:JsonProperty("serving_weight_grams")
    @set:JsonProperty("serving_weight_grams")
    @JsonProperty("serving_weight_grams")
    var servingWeightGrams: Int? = null
    @get:JsonProperty("nf_calories")
    @set:JsonProperty("nf_calories")
    @JsonProperty("nf_calories")
    var calories: Float? = null
    @get:JsonProperty("nf_total_fat")
    @set:JsonProperty("nf_total_fat")
    @JsonProperty("nf_total_fat")
    var totalFat: Float? = null
    @get:JsonProperty("nf_saturated_fat")
    @set:JsonProperty("nf_saturated_fat")
    @JsonProperty("nf_saturated_fat")
    var saturatedFat: Float? = null
    @get:JsonProperty("nf_cholesterol")
    @set:JsonProperty("nf_cholesterol")
    @JsonProperty("nf_cholesterol")
    var cholesterol: Int? = null
    @get:JsonProperty("nf_sodium")
    @set:JsonProperty("nf_sodium")
    @JsonProperty("nf_sodium")
    var sodium: Float? = null
    @get:JsonProperty("nf_total_carbohydrate")
    @set:JsonProperty("nf_total_carbohydrate")
    @JsonProperty("nf_total_carbohydrate")
    var totalCarbohydrate: Float? = null
    @get:JsonProperty("nf_dietary_fiber")
    @set:JsonProperty("nf_dietary_fiber")
    @JsonProperty("nf_dietary_fiber")
    var dietaryFiber: Float? = null
    @get:JsonProperty("nf_sugars")
    @set:JsonProperty("nf_sugars")
    @JsonProperty("nf_sugars")
    var sugars: Float? = null
    @get:JsonProperty("nf_protein")
    @set:JsonProperty("nf_protein")
    @JsonProperty("nf_protein")
    var protein: Float? = null
    @get:JsonProperty("nf_potassium")
    @set:JsonProperty("nf_potassium")
    @JsonProperty("nf_potassium")
    var potassium: Float? = null
    @get:JsonProperty("nf_p")
    @set:JsonProperty("nf_p")
    @JsonProperty("nf_p")
    var nfP: Float? = null
    @get:JsonProperty("full_nutrients")
    @set:JsonProperty("full_nutrients")
    @JsonProperty("full_nutrients")
    var fullNutrients: List<FullNutrient> = ArrayList()
    @get:JsonProperty("nix_brand_name")
    @set:JsonProperty("nix_brand_name")
    @JsonProperty("nix_brand_name")
    var nixBrandName: Any? = null
    @get:JsonProperty("nix_brand_id")
    @set:JsonProperty("nix_brand_id")
    @JsonProperty("nix_brand_id")
    var nixBrandId: Any? = null
    @get:JsonProperty("nix_item_name")
    @set:JsonProperty("nix_item_name")
    @JsonProperty("nix_item_name")
    var nixItemName: Any? = null
    @get:JsonProperty("nix_item_id")
    @set:JsonProperty("nix_item_id")
    @JsonProperty("nix_item_id")
    var nixItemId: Any? = null
    @get:JsonProperty("upc")
    @set:JsonProperty("upc")
    @JsonProperty("upc")
    var upc: Any? = null
    @get:JsonProperty("consumed_at")
    @set:JsonProperty("consumed_at")
    @JsonProperty("consumed_at")
    var consumedAt: String? = null
    @get:JsonProperty("metadata")
    @set:JsonProperty("metadata")
    @JsonProperty("metadata")
    var metadata: Metadata? = null
    @get:JsonProperty("source")
    @set:JsonProperty("source")
    @JsonProperty("source")
    var source: Int? = null
    @get:JsonProperty("ndb_no")
    @set:JsonProperty("ndb_no")
    @JsonProperty("ndb_no")
    var ndbNo: Int? = null
    @get:JsonProperty("tags")
    @set:JsonProperty("tags")
    @JsonProperty("tags")
    var tags: Tags? = null
    @get:JsonProperty("alt_measures")
    @set:JsonProperty("alt_measures")
    @JsonProperty("alt_measures")
    var altMeasures: List<AltMeasure> = ArrayList()
    @get:JsonProperty("lat")
    @set:JsonProperty("lat")
    @JsonProperty("lat")
    var lat: Any? = null
    @get:JsonProperty("lng")
    @set:JsonProperty("lng")
    @JsonProperty("lng")
    var lng: Any? = null
    @get:JsonProperty("meal_type")
    @set:JsonProperty("meal_type")
    @JsonProperty("meal_type")
    var mealType: Int? = null
    @get:JsonProperty("photo")
    @set:JsonProperty("photo")
    @JsonProperty("photo")
    var photo: Photo? = null
    @get:JsonProperty("sub_recipe")
    @set:JsonProperty("sub_recipe")
    @JsonProperty("sub_recipe")
    var subRecipe: Any? = null
    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any?> = HashMap()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any?> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any?) {
        additionalProperties[name] = value
    }
}
