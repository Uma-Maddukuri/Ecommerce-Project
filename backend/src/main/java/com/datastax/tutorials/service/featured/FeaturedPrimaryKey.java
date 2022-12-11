package com.datastax.tutorials.service.featured;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class FeaturedPrimaryKey {
    @PrimaryKeyColumn(
            name = "feature_id", 
            ordinal = 0, 
            type = PrimaryKeyType.PARTITIONED)
    private int featureId;

    @PrimaryKeyColumn(
            name = "category_id", 
            ordinal = 1, 
            type = PrimaryKeyType.CLUSTERED, 
            ordering = Ordering.ASCENDING)
    private UUID categoryId;
    public int getFeatureId() {
        return featureId;
    }

    public void setFeaturedId(int featureId) {
        this.featureId = featureId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
}
