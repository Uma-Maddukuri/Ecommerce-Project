package com.datastax.tutorials.service.price;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

@Table("price")
public class PriceEntity {
    
    @PrimaryKey
    private PricePrimaryKey key;
    
    @Column("value")
    @CassandraType(type = Name.DECIMAL)
    private Double value;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public PricePrimaryKey getKey() {
        return key;
    }

    public void setKey(PricePrimaryKey key) {
        this.key = key;
    }

}
