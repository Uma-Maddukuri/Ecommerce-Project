package com.datastax.tutorials.service.category;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("category")
public class CategoryEntity implements Serializable {

    private static final long serialVersionUID = 4504093417018484534L;
    
    @PrimaryKey
    private CategoryPrimaryKey key;
    
    @Column("name")
    @CassandraType(type = Name.TEXT)
    private String name;
    
    @Column("image")
    @CassandraType(type = Name.TEXT)
    private String image;
    
    @Column("products")
    @CassandraType(type = CassandraType.Name.LIST, typeArguments = Name.TEXT)
    private List<String> products;

    public CategoryPrimaryKey getKey() {
        return key;
    }

    public void setKey(CategoryPrimaryKey key) {
        this.key = key;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }

}
