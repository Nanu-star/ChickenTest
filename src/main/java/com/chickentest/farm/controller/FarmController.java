package com.chickentest.farm.controller;

import com.chickentest.farm.domain.Article;
import com.chickentest.farm.domain.Category;
import com.chickentest.farm.domain.Movement;
import com.chickentest.farm.dto.ArticleRequest;
import com.chickentest.farm.dto.ArticleResponse;
import com.chickentest.farm.dto.CategoryResponse;
import com.chickentest.farm.exception.ArticleNotFoundException;
import com.chickentest.farm.exception.FarmException;
import com.chickentest.farm.exception.InsufficientBalanceException;
import com.chickentest.farm.exception.InsufficientStockException;
import com.chickentest.farm.exception.MaxStockExceededException;
import com.chickentest.farm.repository.CategoryRepository;
import com.chickentest.farm.service.FarmService;
import com.chickentest.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/farm")
public class FarmController {

  private final FarmService farmService;
  private final CategoryRepository categoryRepository;
  private static final Logger logger = LoggerFactory.getLogger(FarmController.class);

  @Autowired
  public FarmController(FarmService farmService, CategoryRepository categoryRepository) {
    this.farmService = farmService;
    this.categoryRepository = categoryRepository;
  }

  @GetMapping("/articles")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<ArticleResponse>> articles(@AuthenticationPrincipal User user) {
    try {
      List<Article> articles = farmService.loadInventory(user);
      List<ArticleResponse> response =
          articles.stream()
              .map(
                  article -> {
                    Category category = article.getCategory();
                    return new ArticleResponse(
                        article.getId(),
                        article.getName(),
                        article.getUnits(),
                        article.getPrice(),
                        article.getAge(),
                        article.getProduction(),
                        category != null ? category.getId() : null,
                        category != null ? category.getName() : null);
                  })
              .collect(Collectors.toList());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Unexpected error in articles: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/update-balance")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> updateBalance(
      @AuthenticationPrincipal User user, @RequestParam("newBalance") double newBalance) {
    try {
      farmService.updateUserBalance(user, newBalance);
      return ResponseEntity.ok("Balance updated successfully!");
    } catch (IllegalArgumentException iae) {
      logger.warn(
          "Invalid argument while updating balance for user {}: {}",
          user.getUsername(),
          iae.getMessage());
      return ResponseEntity.badRequest().body(iae.getMessage());
    } catch (FarmException fe) {
      logger.warn(
          "FarmException while updating balance for user {}: {}",
          user.getUsername(),
          fe.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fe.getMessage());
    }
  }

  @PostMapping("/articles")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> addArticle(
      @AuthenticationPrincipal User user, @Valid @RequestBody ArticleRequest articleRequest) {
    try {
      Article article = new Article();
      article.setName(articleRequest.getName());
      article.setUnits(articleRequest.getUnits());
      article.setPrice(articleRequest.getPrice());
      article.setAge(articleRequest.getAge());
      article.setProduction(articleRequest.getProduction());
      article.setUser(user);

      Category category =
          categoryRepository
              .findById(articleRequest.getCategoryId())
              .orElseThrow(() -> new FarmException("Category not found"));
      article.setCategory(category);

      Article savedArticle = farmService.addArticle(article, user);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);
    } catch (InsufficientBalanceException ibe) {
      logger.warn(
          "Failed to add article due to insufficient balance for user {}: {}",
          user.getUsername(),
          ibe.getMessage());
      return ResponseEntity.badRequest().body(ibe.getMessage());
    } catch (FarmException fe) {
      logger.warn(
          "FarmException while adding article for user {}: {}",
          user.getUsername(),
          fe.getMessage());
      return ResponseEntity.badRequest().body(fe.getMessage());
    } catch (Exception e) {
      logger.error("Error adding article for user {}: {}", user.getUsername(), e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while adding the article.");
    }
  }

  @PostMapping("/articles/buy/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> buyArticle(
      @PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) {
    try {
      farmService.buy(
          id, quantity, user); // Service method now returns void and throws specific exceptions
      return ResponseEntity.ok("Article(s) bought successfully!");
    } catch (ArticleNotFoundException e) {
      logger.warn(
          "Failed to buy article: Article not found. User: {}, Article ID: {}",
          user.getUsername(),
          id);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (InsufficientBalanceException
        | MaxStockExceededException
        | InsufficientStockException e) {
      logger.warn(
          "Failed to buy article for user {}: {} (Article ID: {})",
          user.getUsername(),
          e.getMessage(),
          id);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (FarmException fe) {
      logger.warn(
          "FarmException while buying article for user {}: {} (Article ID: {})",
          user.getUsername(),
          fe.getMessage(),
          id);
      return ResponseEntity.badRequest().body(fe.getMessage());
    } catch (IllegalArgumentException iae) {
      logger.warn(
          "Invalid argument while buying article for user {}: {} (Article ID: {})",
          user.getUsername(),
          iae.getMessage(),
          id);
      return ResponseEntity.badRequest().body(iae.getMessage());
    } catch (Exception e) {
      logger.error(
          "Error buying article for user {}: {} (Article ID: {})",
          user.getUsername(),
          e.getMessage(),
          id,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while buying the article.");
    }
  }

  @PostMapping("/articles/sell/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> sellArticle(
      @PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) {
    try {
      farmService.sell(
          id, quantity, user); // Service method now returns void and throws specific exceptions
      return ResponseEntity.ok("Article(s) sold successfully!");
    } catch (ArticleNotFoundException e) {
      logger.warn(
          "Failed to sell article: Article not found. User: {}, Article ID: {}",
          user.getUsername(),
          id);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (InsufficientStockException e) {
      logger.warn(
          "Failed to sell article for user {}: {} (Article ID: {})",
          user.getUsername(),
          e.getMessage(),
          id);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (FarmException fe) {
      logger.warn(
          "FarmException while selling article for user {}: {} (Article ID: {})",
          user.getUsername(),
          fe.getMessage(),
          id);
      return ResponseEntity.badRequest().body(fe.getMessage());
    } catch (IllegalArgumentException iae) {
      logger.warn(
          "Invalid argument while selling article for user {}: {} (Article ID: {})",
          user.getUsername(),
          iae.getMessage(),
          id);
      return ResponseEntity.badRequest().body(iae.getMessage());
    } catch (Exception e) {
      logger.error(
          "Error selling article for user {}: {} (Article ID: {})",
          user.getUsername(),
          e.getMessage(),
          id,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while selling the article.");
    }
  }

  @GetMapping("/movements")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<Movement>> movements(@AuthenticationPrincipal User user) {
    try {
      return ResponseEntity.ok(farmService.getMovements(user));
    } catch (Exception e) {
      logger.error("Unexpected error in movements: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/categories")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<CategoryResponse>> categories() {
    try {
      List<CategoryResponse> response =
          farmService.getCategories().stream()
              .map(cat -> new CategoryResponse(cat.getId(), cat.getName(), cat.getDisplayName()))
              .collect(Collectors.toList());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Unexpected error in categories: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/articles/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> updateArticle(
      @PathVariable Long id, @RequestBody Article article, @AuthenticationPrincipal User user) {
    article.setId(id);
    article.setUser(user); // Ensure user is always set!
    farmService.updateArticle(article);
    return ResponseEntity.ok("Article updated successfully");
  }
}
