package com.chickentest.repository;

import com.chickentest.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chickentest.domain.Category;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT COALESCE(SUM(units), 0) FROM Article WHERE category = :category")
    int findTotalUnitsByCategory(@Param("category") Category category);
}
