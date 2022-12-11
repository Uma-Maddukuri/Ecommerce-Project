package com.datastax.tutorials.service.product;

import org.springframework.data.cassandra.repository.CassandraRepository;

public interface ProductRepository  extends CassandraRepository<ProductEntity, String> {
}
