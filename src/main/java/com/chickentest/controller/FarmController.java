package com.chickentest.controller;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.dto.ArticleRequest;
import com.chickentest.dto.CategoryResponse;
import com.chickentest.dto.ArticleResponse;
import com.chickentest.exception.*; // Import all exceptions
import com.chickentest.repository.UserRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import com.chickentest.service.FarmService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/farm")
public class FarmController {

    private final FarmService farmService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MovementRepository movementRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(FarmController.class);

    @Autowired
    public FarmController(FarmService farmService,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          BCryptPasswordEncoder passwordEncoder, MovementRepository movementRepository) {
        this.farmService = farmService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.movementRepository = movementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/articles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArticleResponse>> articles(@AuthenticationPrincipal User user) {
        try {
            List<Article> articles = farmService.loadInventory(user);
            List<ArticleResponse> response = articles.stream().map(article -> new ArticleResponse(
                    article.getId(),
                    article.getName(),
                    article.getUnits(),
                    article.getPrice(),
                    article.getAge(),
                    article.getProduction(),
                    article.getCategory() != null ? article.getCategory().getId() : null,
                    article.getCategory() != null ? article.getCategory().getName() : null
            )).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error in articles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Removed old /buy endpoint:
    // @PostMapping("/buy")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<?> buyArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) { ... }

    // Removed old /sell endpoint:
    // @PostMapping("/sell")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<?> sellArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) { ... }

    @PostMapping("/update-balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateBalance(@AuthenticationPrincipal User user, // user principal is already non-null due to PreAuthorize
                                @RequestParam("newBalance") double newBalance) {
        try {
            farmService.updateUserBalance(user, newBalance);
            return ResponseEntity.ok("Balance updated successfully!");
        } catch (IllegalArgumentException iae) { // For negative balance or other invalid args from service
            logger.warn("Invalid argument while updating balance for user {}: {}", user.getUsername(), iae.getMessage());
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (FarmException fe) { // For user not found in service or other farm-related issues
            logger.warn("FarmException while updating balance for user {}: {}", user.getUsername(), fe.getMessage());
            // HttpStatus.NOT_FOUND might be appropriate if FarmException specifically means user not found by service.
            // Otherwise, BAD_REQUEST can cover general business rule violations.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fe.getMessage());
        } catch (Exception e) {
            logger.error("Error updating balance for user {}: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating balance: " + e.getMessage());
        }
    }

    @PostMapping("/articles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addArticle(@AuthenticationPrincipal User user, @RequestBody ArticleRequest articleRequest) {
        try {
            Article article = new Article();
            article.setName(articleRequest.getName());
            article.setUnits(articleRequest.getUnits());
            article.setPrice(articleRequest.getPrice());
            article.setAge(articleRequest.getAge());
            article.setProduction(articleRequest.getProduction());
            article.setUser(user);

            Category category = categoryRepository.findById(articleRequest.getCategoryId())
                    .orElseThrow(() -> new FarmException("Category not found"));
            article.setCategory(category);

            Article savedArticle = farmService.addArticle(article, user);
            if (savedArticle != null) { // Or check for specific success response/exception
                return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle); // Return CREATED and the article
            }
            // This part might be unreachable if service throws exceptions on failure.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add article. Please check the values and try again.");
        } catch (InsufficientBalanceException ibe) { // Assuming addArticle might involve cost
            logger.warn("Failed to add article due to insufficient balance for user {}: {}", user.getUsername(), ibe.getMessage());
            return ResponseEntity.badRequest().body(ibe.getMessage());
        } catch (FarmException fe) { // Covers category not found, etc. from service
            logger.warn("FarmException while adding article for user {}: {}", user.getUsername(), fe.getMessage());
            return ResponseEntity.badRequest().body(fe.getMessage());
        } catch (Exception e) {
            logger.error("Error adding article for user {}: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while adding the article.");
        }
    }

    @PostMapping("/articles/buy/{id}") // Path kept, method name can be simplified if desired, e.g. to buyArticle
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> buyArticle(@PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) { // Renamed method
        try {
            farmService.buy(id, quantity, user); // Service method now returns void and throws specific exceptions
            return ResponseEntity.ok("Article(s) bought successfully!");
        } catch (ArticleNotFoundException e) {
            logger.warn("Failed to buy article: Article not found. User: {}, Article ID: {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientBalanceException | MaxStockExceededException | InsufficientStockException e) { // Grouping relevant business exceptions for 400
            logger.warn("Failed to buy article for user {}: {} (Article ID: {})", user.getUsername(), e.getMessage(), id);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FarmException fe) { // Other farm-specific business errors
            logger.warn("FarmException while buying article for user {}: {} (Article ID: {})", user.getUsername(), fe.getMessage(), id);
            return ResponseEntity.badRequest().body(fe.getMessage());
        } catch (IllegalArgumentException iae) { // For positive quantity validation from service
            logger.warn("Invalid argument while buying article for user {}: {} (Article ID: {})", user.getUsername(), iae.getMessage(), id);
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
         catch (Exception e) {
            logger.error("Error buying article for user {}: {} (Article ID: {})", user.getUsername(), e.getMessage(), id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while buying the article.");
        }
    }

    @PostMapping("/articles/sell/{id}") // Path kept, method name can be simplified
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sellArticle(@PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) { // Renamed method
        try {
            farmService.sell(id, quantity, user); // Service method now returns void and throws specific exceptions
            return ResponseEntity.ok("Article(s) sold successfully!");
        } catch (ArticleNotFoundException e) {
            logger.warn("Failed to sell article: Article not found. User: {}, Article ID: {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientStockException e) { // Specific exception for selling
            logger.warn("Failed to sell article for user {}: {} (Article ID: {})", user.getUsername(), e.getMessage(), id);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FarmException fe) { // Other farm-specific business errors
            logger.warn("FarmException while selling article for user {}: {} (Article ID: {})", user.getUsername(), fe.getMessage(), id);
            return ResponseEntity.badRequest().body(fe.getMessage());
        } catch (IllegalArgumentException iae) { // For positive quantity validation from service
            logger.warn("Invalid argument while selling article for user {}: {} (Article ID: {})", user.getUsername(), iae.getMessage(), id);
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            logger.error("Error selling article for user {}: {} (Article ID: {})", user.getUsername(), e.getMessage(), id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while selling the article.");
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

    @GetMapping("/ai-report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> aiReport(@AuthenticationPrincipal User user) {
        try {
            List<Movement> movements = farmService.getMovements(user);
            String aiReport = farmService.generateAIReport(movements, user);
            return ResponseEntity.ok(aiReport);
        } catch (Exception e) {
            logger.error("Unexpected error in aiReport: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate AI report");
        }
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> categories() {
        try {
            List<CategoryResponse> response = farmService.getCategoryResponses().stream()
                    .map(cat -> new CategoryResponse(
                            cat.getId(),
                            cat.getName(),
                            cat.getDisplayName()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error in categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/articles/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @RequestBody Article article, @AuthenticationPrincipal User user) {
        try {
            article.setId(id);
            article.setUser(user); // Ensure user is always set!
            farmService.updateArticle(article);
            return ResponseEntity.ok("Article updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating article: " + e.getMessage());
        }
    }

}