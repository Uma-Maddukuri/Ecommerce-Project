package com.datastax.tutorials.service.product;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

	@JsonProperty("product_id")
	private String productId;
	
	@JsonProperty("product_group")
	private String productGroup;
	
	private String name;
	
	private String brand;
	
	@JsonProperty("model_number")
	private String modelNumber;
	
	@JsonProperty("short_desc")
	private String shortDesc;
	
	@JsonProperty("long_desc")
	private String longDesc;
	
	private Map<String, String> specifications;
	
	@JsonProperty("linked_documents")
	private Map<String, String> linkedDocuments;
	
	private Set<String> images;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
    }

    public Map<String, String> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Map<String, String> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public Set<String> getImages() {
        return images;
    }

    public void setImages(Set<String> images) {
        this.images = images;
    }
	
}
