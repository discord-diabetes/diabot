
package com.dongtronic.diabot.data.nutrition;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "item",
    "measure",
    "quantity",
    "food_group",
    "tag_id"
})
public class Tags {

    @JsonProperty("item")
    private String item;
    @JsonProperty("measure")
    private String measure;
    @JsonProperty("quantity")
    private String quantity;
    @JsonProperty("food_group")
    private Integer foodGroup;
    @JsonProperty("tag_id")
    private Integer tagId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("item")
    public String getItem() {
        return item;
    }

    @JsonProperty("item")
    public void setItem(String item) {
        this.item = item;
    }

    @JsonProperty("measure")
    public String getMeasure() {
        return measure;
    }

    @JsonProperty("measure")
    public void setMeasure(String measure) {
        this.measure = measure;
    }

    @JsonProperty("quantity")
    public String getQuantity() {
        return quantity;
    }

    @JsonProperty("quantity")
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @JsonProperty("food_group")
    public Integer getFoodGroup() {
        return foodGroup;
    }

    @JsonProperty("food_group")
    public void setFoodGroup(Integer foodGroup) {
        this.foodGroup = foodGroup;
    }

    @JsonProperty("tag_id")
    public Integer getTagId() {
        return tagId;
    }

    @JsonProperty("tag_id")
    public void setTagId(Integer tagId) {
        this.tagId = tagId;
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
