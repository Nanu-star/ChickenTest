package com.chickentest.controller;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.exception.InsufficientBalanceException; // Added
import com.chickentest.exception.InsufficientStockException;
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
    public ResponseEntity<List<Article>> articles(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(farmService.loadInventory(user));
        } catch (Exception e) {
            logger.error("Unexpected error in articles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> buyArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) {
        try {
            boolean result = farmService.buy(id, quantity, user);
            if (result) {
                return ResponseEntity.ok("Purchase successful!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance or stock");
            }
        } catch (Exception e) {
            logger.error("Error during purchase: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during purchase: " + e.getMessage());
        }
    }

    @PostMapping("/sell")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sellArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) {
        try {
            boolean result = farmService.sell(id, quantity, user);
            if (result) {
                return ResponseEntity.ok("Sale successful!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock");
            }
        } catch (Exception e) {
            logger.error("Error during sale: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during sale: " + e.getMessage());
        }
    }

    @PostMapping("/update-balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateBalance(@AuthenticationPrincipal User user,
                                @RequestParam("newBalance") double newBalance) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found. Please log in again.");
        }
        try {
            if (newBalance < 0) {
                return ResponseEntity.badRequest().body("Balance cannot be negative.");
            } else {
                User currentUser = userRepository.findById(user.getId()).orElse(null);
                if (currentUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found in database. Please log in again.");
                }
                currentUser.setBalance(newBalance);
                userRepository.save(currentUser);
                return ResponseEntity.ok("Balance updated successfully!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating balance: " + e.getMessage());
        }
    }



    @PostMapping("/articles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addArticle(@AuthenticationPrincipal User user, @RequestBody Article article) {
        try {
            if (article.getCategory() != null && article.getCategory().getId() != null) {
                Category cat = categoryRepository.findById(article.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
                article.setCategory(cat);
            } else {
                return ResponseEntity.badRequest().body("Please select a valid category.");
            }
            article.setUser(user);
            if (farmService.addArticle(article, user)) {
                return ResponseEntity.ok("Article added successfully");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add article. Please check the values and try again.");
        } catch (InsufficientBalanceException ibe) {
            logger.warn("Failed to add article due to insufficient balance for user {}: {}", user.getUsername(), ibe.getMessage());
            return ResponseEntity.badRequest().body(ibe.getMessage());
        } catch (FarmException fe) {
            logger.warn("FarmException while adding article for user {}: {}", user.getUsername(), fe.getMessage());
            return ResponseEntity.badRequest().body(fe.getMessage());
        } catch (Exception e) {
            logger.error("Error adding article for user {}: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while adding the article.");
        }
    }

    @PostMapping("/articles/buy/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> buyArticleById(@PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) {
        try {
            farmService.buy(id, quantity, user);
            return ResponseEntity.ok("Article(s) bought successfully!");
        } catch (InsufficientBalanceException ibe) {
            logger.warn("Failed to buy article due to insufficient balance for user {}: {}", user.getUsername(), ibe.getMessage());
            return ResponseEntity.badRequest().body(ibe.getMessage());
        } catch (FarmException fe) {
            logger.warn("FarmException while buying article for user {}: {}", user.getUsername(), fe.getMessage());
            return ResponseEntity.badRequest().body(fe.getMessage());
        } catch (Exception e) {
            logger.error("Error buying article for user {}: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while buying the article.");
        }
    }

    @PostMapping("/articles/sell/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sellArticleById(@PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user) {
        try {
            farmService.sell(id, quantity, user);
            return ResponseEntity.ok("Article(s) sold successfully!");
        } catch (InsufficientStockException ise) {
            logger.warn("Failed to sell article due to insufficient stock for user {}: {}", user.getUsername(), ise.getMessage());
            return ResponseEntity.badRequest().body(ise.getMessage());
        } catch (FarmException fe) {
            logger.warn("FarmException while selling article for user {}: {}", user.getUsername(), fe.getMessage());
            return ResponseEntity.badRequest().body(fe.getMessage());
        } catch (Exception e) {
            logger.error("Error selling article for user {}: {}", user.getUsername(), e.getMessage(), e);
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
    public ResponseEntity<List<Category>> categories() {
        try {
            return ResponseEntity.ok(farmService.getCategories());
        } catch (Exception e) {
            logger.error("Unexpected error in categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }





    @GetMapping("/report")
    public String getFarmReport(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        List<Movement> movements = movementRepository.findAllByUser(user);
        return farmService.generateAIReport(movements, user);
    }

    // Helper DTO for report endpoint
    public static class ReportResponse {
        public Report report;
        public List<Movement> movements;
        public String message;

        public ReportResponse() {}

        public ReportResponse(String message) {
            this.message = message;
        }

        public ReportResponse(Report report, List<Movement> movements) {
            this.report = report;
            this.movements = movements;
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