package com.chickentest.service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.chickentest.domain.Article;
import com.chickentest.domain.Movement;
import com.chickentest.domain.User;
import com.chickentest.dto.CategoryResponse;
import com.chickentest.domain.Report;
import com.chickentest.domain.Category;

public interface FarmService {

    @Transactional(readOnly = true)
    List<Article> loadInventory(User user);

    @Transactional(readOnly = true)
    List<Movement> getMovements(User user);

    String generateAIReport(List<Movement> movements, User user);

    @Transactional
    void buy(Long articleId, int quantity, User user); // Return type void

    @Transactional
    void sell(Long articleId, int quantity, User user); // Return type void

    @Transactional
    Article addArticle(Article article, User user);

    @Transactional
    Article updateArticle(Article article);

    @Transactional
    void deleteArticle(Long articleId);

    @Transactional(readOnly = true)
    Article getArticle(Long id);

    Report generateReport();

    @Transactional
    void hatchEggs();

    // void hatchEggsInternal(); // Removed

    @Transactional(readOnly = true)
    List<Category> getCategoryResponses();

    @Transactional(readOnly = true)
    List<Category> getCategories();

    @Transactional
    void updateUserBalance(User authenticatedUser, double newBalance); // Added
}

