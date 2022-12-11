package com.datastax.tutorials.service.featured;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("featured_product_groups")
public class FeaturedEntity implements Serializable {

    private static final long serialVersionUID = -8646621243907313642L;

    @PrimaryKey
	private FeaturedPrimaryKey key;
	
    @Column("name")
    @CassandraType(type = Name.TEXT)
    private String name;
    
    @Column("image")
    @CassandraType(type = Name.TEXT)
    private String image;

    @Column("parent_id")
    @CassandraType(type = Name.UUID)
    private UUID parentId;

    @Column("price")
    @CassandraType(type = Name.DECIMAL)
    private Double price;

    public FeaturedPrimaryKey getKey() {
        return key;
    }

    public void setKey(FeaturedPrimaryKey key) {
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

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
