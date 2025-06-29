package com.chickentest.controller;

import com.chickentest.domain.Article;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.repository.UserRepository;
import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Controller
public class FarmController {

    private final FarmService farmService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = Logger.getLogger(FarmController.class.getName());

    @Autowired
    public FarmController(FarmService farmService,
                          UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.farmService = farmService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public String register(@ModelAttribute @Valid User user, RedirectAttributes redirectAttributes) {
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

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        try {
            List<Article> articles = farmService.loadInventory(user);
            model.addAttribute("articles", articles);
            return "dashboard";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.severe("Unexpected error in dashboard: " + e.getMessage());
            throw new FarmException("An unexpected error occurred", e);
        }
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public String buy(@RequestParam Long articleId,
                      @RequestParam int cantidad,
                      @AuthenticationPrincipal User user,
                      RedirectAttributes redirectAttributes) {
        try {
            if (!farmService.buy(articleId, cantidad, user)) {
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
                       @RequestParam int cantidad,
                       @AuthenticationPrincipal User user,
                       RedirectAttributes redirectAttributes) {
        try {
            if (!farmService.sell(articleId, cantidad, user)) {
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
    public String report(Model model) {
        try {
            Report report = farmService.generateReport();
            model.addAttribute("reporte", report);
            return "movimientos-reporte";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.severe("Unexpected error in report: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while generating report", e);
        }
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String addArticle(@ModelAttribute Article article, RedirectAttributes redirectAttributes) {
        try {
            if (!farmService.addArticle(article)) {
                redirectAttributes.addFlashAttribute("error", "Stock limit exceeded");
            }
            return "redirect:/dashboard";
        } catch (FarmException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.severe("Unexpected error in addArticle: " + e.getMessage());
            throw new FarmException("An unexpected error occurred while adding article", e);
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