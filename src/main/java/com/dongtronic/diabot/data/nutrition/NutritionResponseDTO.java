
package com.dongtronic.diabot.data.nutrition;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "foods"
})
public class NutritionResponseDTO {

    @JsonProperty("foods")
    private List<Food> foods = new ArrayList<Food>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("foods")
    public List<Food> getFoods() {
        return foods;
    }

    @JsonProperty("foods")
    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Double getTotalCarbs() {
        return foods.stream().filter(food -> food.getTotalCarbohydrate() != null).mapToDouble(Food::getTotalCarbohydrate).sum();
    }

    public Double getTotalFats() {
        return foods.stream().filter(food -> food.getTotalFat() != null).mapToDouble(Food::getTotalFat).sum();
    }

    public Double getTotalSaturatedFats() {
        return foods.stream().filter(food -> food.getSaturatedFat() != null).mapToDouble(Food::getSaturatedFat).sum();
    }

    public Double getTotalFibers() {
        return foods.stream().filter(food -> food.getDietaryFiber() != null).mapToDouble(Food::getDietaryFiber).sum();
    }

    @Override
    public String toString() {
        StringBuilder returned = new StringBuilder();

        for (Food food : foods) {
            returned.append(food.getFoodName())
                    .append(" (").append(food.getServingQty())
                    .append(" ").append(food.getServingUnit())
                    .append(")\n");
        }

        return returned.toString();
    }
}
