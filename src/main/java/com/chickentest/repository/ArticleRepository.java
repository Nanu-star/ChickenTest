package com.chickentest.repository;

import com.chickentest.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chickentest.domain.Category;
import com.chickentest.domain.User;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT COALESCE(SUM(units), 0) FROM Article WHERE category = :category")
    int findTotalUnitsByCategory(@Param("category") Category category);

    java.util.List<Article> findByCategory(Category category);

    java.util.Optional<Article> findByNameAndCategory(String name, Category category);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // It's generally better to use the JpaRepository findById if you just need to lock.
    // However, if you need a custom query or to ensure it's part of this interface explicitly:
    @Query("SELECT a FROM Article a WHERE a.id = :id")
    Optional<Article> findByIdForUpdate(@Param("id") Long id);

    List<Article> findAllByUserId(Long userId);
}
