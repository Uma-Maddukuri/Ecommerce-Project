package com.datastax.tutorials.service.price;

import java.util.List;
import java.util.Optional;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends CassandraRepository<PriceEntity, PricePrimaryKey> {

    List<PriceEntity> findByKeyProductId(String productId);

    Optional<PriceEntity> findByKeyProductIdAndKeyStoreId(String productId, String storeId);
  
    void deleteByKeyProductId(String productId);

    void deleteByKeyProductIdAndKeyStoreId(String productId, String storeId);

}
