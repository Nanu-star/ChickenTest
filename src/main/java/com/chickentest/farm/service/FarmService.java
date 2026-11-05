package com.chickentest.farm.service;

import com.chickentest.farm.domain.Article;
import com.chickentest.farm.domain.Category;
import com.chickentest.farm.domain.Movement;
import com.chickentest.farm.domain.Report;
import com.chickentest.user.User;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface FarmService {

  @Transactional(readOnly = true)
  List<Article> loadInventory(User user);

  @Transactional(readOnly = true)
  List<Movement> getMovements(User user);

  @Transactional
  void buy(Long articleId, int quantity, User user);

  @Transactional
  void sell(Long articleId, int quantity, User user);

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

  @Transactional(readOnly = true)
  List<Category> getCategories();

  @Transactional
  void updateUserBalance(User authenticatedUser, double newBalance);
}
