package com.datastax.tutorials.service.product;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("product")
public class ProductEntity implements Serializable {
    
    private static final long serialVersionUID = 2835469764140679456L;

    @PrimaryKey("product_id")
    @CassandraType(type = Name.TEXT)
    private String productId;
    
    @Column("brand")
    @CassandraType(type = Name.TEXT)
    private String brand;
    
    @Column("images")
    @CassandraType(type = CassandraType.Name.SET, typeArguments = Name.TEXT)
    private Set<String> images;
    
    @Column("linked_documents")
    @CassandraType(type = CassandraType.Name.MAP, typeArguments = { Name.TEXT, Name.TEXT })
    private Map<String, String> linkedDocuments;
    
    @Column("long_desc")
    @CassandraType(type = Name.TEXT)
    private String longDescription;
    
    @Column("model_number")
    @CassandraType(type = Name.TEXT)
    private String modelNumber;
    
    @Column("name")
    @CassandraType(type = Name.TEXT)
    private String name;
    
    @Column("product_group")
    @CassandraType(type = Name.TEXT)
    private String productGroup;
    
    @Column("short_desc")
    @CassandraType(type = Name.TEXT)
    private String shortDescription;
    
    @Column("specifications")
    @CassandraType(type = CassandraType.Name.MAP, typeArguments = { Name.TEXT, Name.TEXT })
    private Map<String, String> specifications;

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

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    
}
