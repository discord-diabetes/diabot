
package com.dongtronic.diabot.data.nutrition;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "food_name",
        "brand_name",
        "serving_qty",
        "serving_unit",
        "serving_weight_grams",
        "nf_calories",
        "nf_total_fat",
        "nf_saturated_fat",
        "nf_cholesterol",
        "nf_sodium",
        "nf_total_carbohydrate",
        "nf_dietary_fiber",
        "nf_sugars",
        "nf_protein",
        "nf_potassium",
        "nf_p",
        "full_nutrients",
        "nix_brand_name",
        "nix_brand_id",
        "nix_item_name",
        "nix_item_id",
        "upc",
        "consumed_at",
        "metadata",
        "source",
        "ndb_no",
        "tags",
        "alt_measures",
        "lat",
        "lng",
        "meal_type",
        "photo",
        "sub_recipe"
})
public class Food {

    @JsonProperty("food_name")
    private String foodName;
    @JsonProperty("brand_name")
    private Object brandName;
    @JsonProperty("serving_qty")
    private Integer servingQty;
    @JsonProperty("serving_unit")
    private String servingUnit;
    @JsonProperty("serving_weight_grams")
    private Integer servingWeightGrams;
    @JsonProperty("nf_calories")
    private Float calories;
    @JsonProperty("nf_total_fat")
    private Float totalFat;
    @JsonProperty("nf_saturated_fat")
    private Float saturatedFat;
    @JsonProperty("nf_cholesterol")
    private Integer cholesterol;
    @JsonProperty("nf_sodium")
    private Float sodium;
    @JsonProperty("nf_total_carbohydrate")
    private Float totalCarbohydrate;
    @JsonProperty("nf_dietary_fiber")
    private Float dietaryFiber;
    @JsonProperty("nf_sugars")
    private Float sugars;
    @JsonProperty("nf_protein")
    private Float protein;
    @JsonProperty("nf_potassium")
    private Float potassium;
    @JsonProperty("nf_p")
    private Float nfP;
    @JsonProperty("full_nutrients")
    private List<FullNutrient> fullNutrients = new ArrayList<>();
    @JsonProperty("nix_brand_name")
    private Object nixBrandName;
    @JsonProperty("nix_brand_id")
    private Object nixBrandId;
    @JsonProperty("nix_item_name")
    private Object nixItemName;
    @JsonProperty("nix_item_id")
    private Object nixItemId;
    @JsonProperty("upc")
    private Object upc;
    @JsonProperty("consumed_at")
    private String consumedAt;
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("source")
    private Integer source;
    @JsonProperty("ndb_no")
    private Integer ndbNo;
    @JsonProperty("tags")
    private Tags tags;
    @JsonProperty("alt_measures")
    private List<AltMeasure> altMeasures = new ArrayList<>();
    @JsonProperty("lat")
    private Object lat;
    @JsonProperty("lng")
    private Object lng;
    @JsonProperty("meal_type")
    private Integer mealType;
    @JsonProperty("photo")
    private Photo photo;
    @JsonProperty("sub_recipe")
    private Object subRecipe;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("food_name")
    public String getFoodName() {
        return foodName;
    }

    @JsonProperty("food_name")
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    @JsonProperty("brand_name")
    public Object getBrandName() {
        return brandName;
    }

    @JsonProperty("brand_name")
    public void setBrandName(Object brandName) {
        this.brandName = brandName;
    }

    @JsonProperty("serving_qty")
    public Integer getServingQty() {
        return servingQty;
    }

    @JsonProperty("serving_qty")
    public void setServingQty(Integer servingQty) {
        this.servingQty = servingQty;
    }

    @JsonProperty("serving_unit")
    public String getServingUnit() {
        return servingUnit;
    }

    @JsonProperty("serving_unit")
    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
    }

    @JsonProperty("serving_weight_grams")
    public Integer getServingWeightGrams() {
        return servingWeightGrams;
    }

    @JsonProperty("serving_weight_grams")
    public void setServingWeightGrams(Integer servingWeightGrams) {
        this.servingWeightGrams = servingWeightGrams;
    }

    @JsonProperty("nf_calories")
    public Float getCalories() {
        return calories;
    }

    @JsonProperty("nf_calories")
    public void setCalories(Float calories) {
        this.calories = calories;
    }

    @JsonProperty("nf_total_fat")
    public Float getTotalFat() {
        return totalFat;
    }

    @JsonProperty("nf_total_fat")
    public void setTotalFat(Float totalFat) {
        this.totalFat = totalFat;
    }

    @JsonProperty("nf_saturated_fat")
    public Float getSaturatedFat() {
        return saturatedFat;
    }

    @JsonProperty("nf_saturated_fat")
    public void setSaturatedFat(Float saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    @JsonProperty("nf_cholesterol")
    public Integer getCholesterol() {
        return cholesterol;
    }

    @JsonProperty("nf_cholesterol")
    public void setCholesterol(Integer cholesterol) {
        this.cholesterol = cholesterol;
    }

    @JsonProperty("nf_sodium")
    public Float getSodium() {
        return sodium;
    }

    @JsonProperty("nf_sodium")
    public void setSodium(Float sodium) {
        this.sodium = sodium;
    }

    @JsonProperty("nf_total_carbohydrate")
    public Float getTotalCarbohydrate() {
        return totalCarbohydrate;
    }

    @JsonProperty("nf_total_carbohydrate")
    public void setTotalCarbohydrate(Float totalCarbohydrate) {
        this.totalCarbohydrate = totalCarbohydrate;
    }

    @JsonProperty("nf_dietary_fiber")
    public Float getDietaryFiber() {
        return dietaryFiber;
    }

    @JsonProperty("nf_dietary_fiber")
    public void setDietaryFiber(Float dietaryFiber) {
        this.dietaryFiber = dietaryFiber;
    }

    @JsonProperty("nf_sugars")
    public Float getSugars() {
        return sugars;
    }

    @JsonProperty("nf_sugars")
    public void setSugars(Float sugars) {
        this.sugars = sugars;
    }

    @JsonProperty("nf_protein")
    public Float getProtein() {
        return protein;
    }

    @JsonProperty("nf_protein")
    public void setProtein(Float protein) {
        this.protein = protein;
    }

    @JsonProperty("nf_potassium")
    public Float getPotassium() {
        return potassium;
    }

    @JsonProperty("nf_potassium")
    public void setPotassium(Float potassium) {
        this.potassium = potassium;
    }

    @JsonProperty("nf_p")
    public Float getNfP() {
        return nfP;
    }

    @JsonProperty("nf_p")
    public void setNfP(Float nfP) {
        this.nfP = nfP;
    }

    @JsonProperty("full_nutrients")
    public List<FullNutrient> getFullNutrients() {
        return fullNutrients;
    }

    @JsonProperty("full_nutrients")
    public void setFullNutrients(List<FullNutrient> fullNutrients) {
        this.fullNutrients = fullNutrients;
    }

    @JsonProperty("nix_brand_name")
    public Object getNixBrandName() {
        return nixBrandName;
    }

    @JsonProperty("nix_brand_name")
    public void setNixBrandName(Object nixBrandName) {
        this.nixBrandName = nixBrandName;
    }

    @JsonProperty("nix_brand_id")
    public Object getNixBrandId() {
        return nixBrandId;
    }

    @JsonProperty("nix_brand_id")
    public void setNixBrandId(Object nixBrandId) {
        this.nixBrandId = nixBrandId;
    }

    @JsonProperty("nix_item_name")
    public Object getNixItemName() {
        return nixItemName;
    }

    @JsonProperty("nix_item_name")
    public void setNixItemName(Object nixItemName) {
        this.nixItemName = nixItemName;
    }

    @JsonProperty("nix_item_id")
    public Object getNixItemId() {
        return nixItemId;
    }

    @JsonProperty("nix_item_id")
    public void setNixItemId(Object nixItemId) {
        this.nixItemId = nixItemId;
    }

    @JsonProperty("upc")
    public Object getUpc() {
        return upc;
    }

    @JsonProperty("upc")
    public void setUpc(Object upc) {
        this.upc = upc;
    }

    @JsonProperty("consumed_at")
    public String getConsumedAt() {
        return consumedAt;
    }

    @JsonProperty("consumed_at")
    public void setConsumedAt(String consumedAt) {
        this.consumedAt = consumedAt;
    }

    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("source")
    public Integer getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(Integer source) {
        this.source = source;
    }

    @JsonProperty("ndb_no")
    public Integer getNdbNo() {
        return ndbNo;
    }

    @JsonProperty("ndb_no")
    public void setNdbNo(Integer ndbNo) {
        this.ndbNo = ndbNo;
    }

    @JsonProperty("tags")
    public Tags getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(Tags tags) {
        this.tags = tags;
    }

    @JsonProperty("alt_measures")
    public List<AltMeasure> getAltMeasures() {
        return altMeasures;
    }

    @JsonProperty("alt_measures")
    public void setAltMeasures(List<AltMeasure> altMeasures) {
        this.altMeasures = altMeasures;
    }

    @JsonProperty("lat")
    public Object getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(Object lat) {
        this.lat = lat;
    }

    @JsonProperty("lng")
    public Object getLng() {
        return lng;
    }

    @JsonProperty("lng")
    public void setLng(Object lng) {
        this.lng = lng;
    }

    @JsonProperty("meal_type")
    public Integer getMealType() {
        return mealType;
    }

    @JsonProperty("meal_type")
    public void setMealType(Integer mealType) {
        this.mealType = mealType;
    }

    @JsonProperty("photo")
    public Photo getPhoto() {
        return photo;
    }

    @JsonProperty("photo")
    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    @JsonProperty("sub_recipe")
    public Object getSubRecipe() {
        return subRecipe;
    }

    @JsonProperty("sub_recipe")
    public void setSubRecipe(Object subRecipe) {
        this.subRecipe = subRecipe;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
