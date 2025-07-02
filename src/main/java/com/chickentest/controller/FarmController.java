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
import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class FarmController {

    private final FarmService farmService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(FarmController.class);

    @Autowired
    public FarmController(FarmService farmService,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.farmService = farmService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard/articles")
    @PreAuthorize("isAuthenticated()")
    public String articles(@AuthenticationPrincipal User user, Model model) {
        try {
            List<Article> articles = farmService.loadInventory(user);
            model.addAttribute("articles", articles);
            return "articles";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.error("Unexpected error in articles: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while loading articles", e);
        }
    }

    @GetMapping("/farm/buy")
    @PreAuthorize("isAuthenticated()")
    public String buyArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            if (farmService.buy(id, quantity, user)) {
                redirectAttributes.addFlashAttribute("success", "Purchase successful!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Insufficient balance or stock");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error during purchase: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/farm/sell")
    @PreAuthorize("isAuthenticated()")
    public String sellArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            if (farmService.sell(id, quantity, user)) {
                redirectAttributes.addFlashAttribute("success", "Sale successful!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Insufficient stock");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error during sale: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/farm/update-balance")
    @PreAuthorize("isAuthenticated()")
    public String updateBalance(@AuthenticationPrincipal User user,
                                @RequestParam("newBalance") double newBalance,
                                RedirectAttributes redirectAttributes) {
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found. Please log in again.");
            return "redirect:/login";
        }

        try {
            if (newBalance < 0) {
                redirectAttributes.addFlashAttribute("error", "Balance cannot be negative.");
            } else {
                User currentUser = userRepository.findById(user.getId()).orElse(null);
                if (currentUser == null) {
                    redirectAttributes.addFlashAttribute("error", "User not found in database. Please log in again.");
                    return "redirect:/login";
                }
                currentUser.setBalance(newBalance);
                userRepository.save(currentUser);
                redirectAttributes.addFlashAttribute("success", "Balance updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating balance: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard/articles/add")
    @PreAuthorize("isAuthenticated()")
    public String showAddArticleForm(@AuthenticationPrincipal User user, Model model) {
        try {
            model.addAttribute("article", new Article()); // Ensure form backing object exists
            List<Category> categories = farmService.getCategories();
            model.addAttribute("categories", categories);
            return "add-article";
        } catch (Exception e) {
            logger.error("Error loading categories: " + e.getMessage());
            model.addAttribute("error", "Failed to load categories");
            return "error";
        }
    }

    @PostMapping("/dashboard/articles/add")
@PreAuthorize("isAuthenticated()")
public String addArticle(@AuthenticationPrincipal User user,
                        @ModelAttribute("article") Article article,
                        Model model) {
    try {
        // Set the real Category entity from the selected id
        if (article.getCategory() != null && article.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(article.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            article.setCategory(cat);
        } else {
            model.addAttribute("error", "Please select a valid category.");
            model.addAttribute("categories", farmService.getCategories());
            return "add-article";
        }
        article.setUser(user);
        if (farmService.addArticle(article, user)) {
            return "redirect:/dashboard/articles?success";
        }
        // If addArticle failed, show error and return to form
        model.addAttribute("error", "Failed to add article. Please check the values and try again.");
        model.addAttribute("categories", farmService.getCategories());
        return "add-article";
    } catch (InsufficientBalanceException ibe) {
        logger.warn("Failed to add article due to insufficient balance for user {}: {}", user.getUsername(), ibe.getMessage());
        model.addAttribute("error", ibe.getMessage());
        model.addAttribute("article", article); // Keep the user's input
        model.addAttribute("categories", farmService.getCategories());
        return "add-article";
    } catch (FarmException fe) {
        logger.warn("FarmException while adding article for user {}: {}", user.getUsername(), fe.getMessage());
        model.addAttribute("error", fe.getMessage());
        model.addAttribute("article", article); // Keep the user's input
        model.addAttribute("categories", farmService.getCategories());
        return "add-article";
    } catch (Exception e) {
        logger.error("Error adding article for user {}: {}", user.getUsername(), e.getMessage(), e);
        model.addAttribute("error", "An unexpected error occurred while adding the article.");
        model.addAttribute("article", article); // Keep the user's input
        model.addAttribute("categories", farmService.getCategories());
        return "add-article";
    }
}

    @PostMapping("/dashboard/articles/buy/{id}")
    @PreAuthorize("isAuthenticated()")
    public String buyArticle(@PathVariable Long id, @RequestParam int quantity, @AuthenticationPrincipal User user, Model model, RedirectAttributes redirectAttributes) {
        try {
            farmService.buy(id, quantity, user);
            redirectAttributes.addFlashAttribute("successMessage", "Article(s) bought successfully!");
            return "redirect:/dashboard/articles";
        } catch (InsufficientBalanceException ibe) {
            logger.warn("Failed to buy article due to insufficient balance for user {}: {}", user.getUsername(), ibe.getMessage());
            // For redirect, use FlashAttributes. If staying on the same page (not typical for POST-redirect-GET), use Model.
            redirectAttributes.addFlashAttribute("errorMessage", ibe.getMessage());
            return "redirect:/dashboard/articles"; // Or a specific error page/view if preferred
        } catch (FarmException fe) {
            logger.warn("FarmException while buying article for user {}: {}", user.getUsername(), fe.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", fe.getMessage());
            return "redirect:/dashboard/articles";
        } catch (Exception e) {
            logger.error("Error buying article for user {}: {}", user.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while buying the article.");
            return "redirect:/dashboard/articles";
        }
    }

    @PostMapping("/dashboard/articles/sell/{id}")
    @PreAuthorize("isAuthenticated()")
    public String sellArticle(@PathVariable Long id,
                              @RequestParam int quantity,
                              @AuthenticationPrincipal User user, // Changed from SecurityContextHolder
                              Model model,
                              RedirectAttributes redirectAttributes) { // Added RedirectAttributes
        try {
            farmService.sell(id, quantity, user);
            redirectAttributes.addFlashAttribute("successMessage", "Article(s) sold successfully!"); // Changed to flash message
            return "redirect:/dashboard/articles"; // Redirect to dashboard
        } catch (InsufficientStockException ise) {
            logger.warn("Failed to sell article due to insufficient stock for user {}: {}", user.getUsername(), ise.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ise.getMessage());
            return "redirect:/dashboard/articles";
        } catch (FarmException fe) {
            logger.warn("FarmException while selling article for user {}: {}", user.getUsername(), fe.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", fe.getMessage());
            return "redirect:/dashboard/articles";
        } catch (Exception e) {
            logger.error("Error selling article for user {}: {}", user.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while selling the article.");
            return "redirect:/dashboard/articles";
        }
    }

    @GetMapping("/dashboard/movements")
    @PreAuthorize("isAuthenticated()")
    public String movements(@AuthenticationPrincipal User user, Model model) {
        try {
            List<Movement> movements = farmService.getMovements(user);
            model.addAttribute("movements", movements);
            return "movements";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.error("Unexpected error in movements: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while loading movements", e);
        }
    }

    @GetMapping("/dashboard/categories")
    @PreAuthorize("isAuthenticated()")
    public String categories(Model model) {
        try {
            List<Category> categories = farmService.getCategories();
            model.addAttribute("categories", categories);
            return "categories";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.error("Unexpected error in categories: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while loading categories", e);
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser.isPresent()) {
                throw new FarmException("Username already exists");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return "redirect:/login";
        } catch (FarmException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            logger.error("Error registering user: " + e.getMessage());
            throw new FarmException("An unexpected error occurred during registration", e);
        }
    }



    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public String buy(@RequestParam Long articleId,
                      @RequestParam int quantity,
                      @AuthenticationPrincipal User user,
                      RedirectAttributes redirectAttributes) {
        try {
            if (!farmService.buy(articleId, quantity, user)) {
                redirectAttributes.addFlashAttribute("error", "Insufficient balance or stock limit reached");
            }
            return "redirect:/dashboard";
        } catch (FarmException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Unexpected error in buy: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while buying", e);
        }
    }

    @PostMapping("/sell")
    @PreAuthorize("isAuthenticated()")
    public String sell(@RequestParam Long articleId,
                       @RequestParam int quantity,
                       @AuthenticationPrincipal User user,
                       RedirectAttributes redirectAttributes) {
        try {
            farmService.buy(articleId, quantity, user);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase successful!");
            return "redirect:/dashboard";
        } catch (InsufficientBalanceException ibe) {
            logger.warn("Failed to buy due to insufficient balance for user {}: {}", user.getUsername(), ibe.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ibe.getMessage());
            return "redirect:/dashboard";
        } catch (FarmException fe) {
            logger.warn("FarmException while buying for user {}: {}", user.getUsername(), fe.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", fe.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Unexpected error in buy for user {}: {}", user.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during purchase.");
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/report")
    @PreAuthorize("isAuthenticated()")
    public String report(@AuthenticationPrincipal User user, Model model) {
        try {
            Report report = farmService.generateReport();
            List<Movement> movements = farmService.getMovements(user);
            model.addAttribute("report", report);
            model.addAttribute("movements", movements);
            return "movimientos-reporte";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.error("Unexpected error in report: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while generating report", e);
        }
    }


    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editArticle(@PathVariable Long id, Model model) {
        try {
            // TODO: Add ownership check or admin role check if necessary
            Article article = farmService.getArticle(id);
            model.addAttribute("articulo", article);
            return "modificar-articulo";
        } catch (Exception e) {
            throw new RuntimeException("Error loading article", e);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public String updateArticle(@ModelAttribute Article article) {
        try {
            // TODO: Add ownership check or admin role check if necessary
            // Consider fetching the existing article by article.getId() to verify ownership
            // before calling farmService.updateArticle(article).
            farmService.updateArticle(article);
            return "redirect:/dashboard";
        } catch (Exception e) {
            throw new RuntimeException("Error updating article", e);
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            farmService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("success", "Article deleted successfully");
            return "redirect:/dashboard";
        } catch (FarmException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Unexpected error in deleteArticle: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while deleting article", e);
        }
    }
}