package com.datastax.tutorials.service.featured;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;


public interface FeaturedRepository extends CassandraRepository<FeaturedEntity, FeaturedPrimaryKey> {

    List<FeaturedEntity> findByKeyFeatureId(int featureId);
}
