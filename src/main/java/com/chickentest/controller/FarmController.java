package com.chickentest.controller;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
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
import java.util.logging.Logger;

@Controller
public class FarmController {

    private final FarmService farmService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = Logger.getLogger(FarmController.class.getName());

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
            logger.severe("Unexpected error in articles: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while loading articles", e);
        }
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
            logger.severe("Error loading categories: " + e.getMessage());
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
    } catch (Exception e) {
        logger.severe("Error adding article: " + e.getMessage());
        model.addAttribute("error", "An error occurred while adding the article");
        model.addAttribute("categories", farmService.getCategories());
        return "add-article";
    }
}

    @PostMapping("/dashboard/articles/buy/{id}")
    @PreAuthorize("isAuthenticated()")
    public String buyArticle(@PathVariable Long id, @RequestParam int quantity, Model model) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (farmService.buy(id, quantity, user)) {
                return "redirect:/dashboard/articles?success";
            }
            model.addAttribute("error", "Insufficient balance or stock limit exceeded");
            return "articles";
        } catch (Exception e) {
            logger.severe("Error buying article: " + e.getMessage());
            model.addAttribute("error", "An error occurred while buying the article");
            return "articles";
        }
    }

    @PostMapping("/dashboard/articles/sell/{id}")
    @PreAuthorize("isAuthenticated()")
    public String sellArticle(@PathVariable Long id, @RequestParam int quantity, Model model) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (farmService.sell(id, quantity, user)) {
                return "redirect:/dashboard/articles?success";
            }
            model.addAttribute("error", "Insufficient units available");
            return "articles";
        } catch (Exception e) {
            logger.severe("Error selling article: " + e.getMessage());
            model.addAttribute("error", "An error occurred while selling the article");
            return "articles";
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
            logger.severe("Unexpected error in movements: " + e.getMessage());
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
            logger.severe("Unexpected error in categories: " + e.getMessage());
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
            logger.severe("Error registering user: " + e.getMessage());
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
            logger.severe("Unexpected error in buy: " + e.getMessage());
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
            if (!farmService.sell(articleId, quantity, user)) {
                redirectAttributes.addFlashAttribute("error", "Not enough items in stock to sell");
            }
            return "redirect:/dashboard";
        } catch (FarmException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.severe("Unexpected error in sell: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while selling", e);
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
            logger.severe("Unexpected error in report: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while generating report", e);
        }
    }


    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable Long id, Model model) {
        try {
            Article article = farmService.getArticle(id);
            model.addAttribute("articulo", article);
            return "modificar-articulo";
        } catch (Exception e) {
            throw new RuntimeException("Error loading article", e);
        }
    }

    @PostMapping("/update")
    public String updateArticle(@ModelAttribute Article article) {
        try {
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
            logger.severe("Unexpected error in deleteArticle: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while deleting article", e);
        }
    }
}