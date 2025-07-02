package com.example.chickentest.repository;

import com.example.chickentest.entity.Article;
import com.example.chickentest.entity.Farm;
import com.example.chickentest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByUserAndFarm(User user, Farm farm);
    Optional<Article> findByIdAndUserAndFarm(Long id, User user, Farm farm);
    List<Article> findByFarm(Farm farm); // For listing articles of a farm
}
