package com.ecommerce.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.identity.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByName(String name);
}
