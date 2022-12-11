package com.datastax.tutorials.service.category;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CategoryRepository extends CassandraRepository<CategoryEntity, CategoryPrimaryKey> {
    
    List<CategoryEntity> findByKeyParentId(UUID parentId);
    
    List<CategoryEntity> findByKeyParentIdAndKeyCategoryId(UUID parentId, UUID categoryId);
    
}
