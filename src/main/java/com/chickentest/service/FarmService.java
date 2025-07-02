package com.chickentest.service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.chickentest.domain.Article;
import com.chickentest.domain.Movement;
import com.chickentest.domain.User;
import com.chickentest.domain.Report;
import com.chickentest.domain.Category;

public interface FarmService {

    @Transactional(readOnly = true)
    List<Article> loadInventory(User user);

    @Transactional(readOnly = true)
    List<Movement> getMovements(User user);

    String generateAIReport(List<Movement> movements, User user);

    @Transactional
    boolean buy(Long articleId, int quantity, User user);

    @Transactional
    boolean sell(Long articleId, int quantity, User user);

    @Transactional
    boolean addArticle(Article article, User user);

    @Transactional
    Article updateArticle(Article article);

    @Transactional
    void deleteArticle(Long articleId);

    @Transactional(readOnly = true)
    Article getArticle(Long id);

    Report generateReport();

    @Transactional
    void hatchEggs();

    @Transactional
    void hatchEggsInternal();

    @Transactional(readOnly = true)
    List<Category> getCategories();
}

