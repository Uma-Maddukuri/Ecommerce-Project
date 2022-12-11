package com.datastax.tutorials.service.category;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class CategoryPrimaryKey {

    @PrimaryKeyColumn(
            name = "parent_id", 
            ordinal = 0, 
            type = PrimaryKeyType.PARTITIONED)
    private UUID parentId;

    @PrimaryKeyColumn(
            name = "category_id", 
            ordinal = 2, 
            type = PrimaryKeyType.CLUSTERED, 
            ordering = Ordering.ASCENDING)
    private UUID categoryId;

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }


    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
    
}
