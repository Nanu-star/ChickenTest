package com.chickentest.repository;

import com.chickentest.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByDisplayName(String displayName);
    Category findByName(String name);
}
