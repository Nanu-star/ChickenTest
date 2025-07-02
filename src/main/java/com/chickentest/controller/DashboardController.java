package com.chickentest.controller;

import com.chickentest.domain.User;
import com.chickentest.repository.UserRepository;
import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final FarmService farmService;
    private final UserRepository userRepository;

    @Autowired
    public DashboardController(FarmService farmService, UserRepository userRepository) {
        this.farmService = farmService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showDashboard(@AuthenticationPrincipal User user, Model model) {
        try {
            model.addAttribute("articles", farmService.loadInventory(user));
            model.addAttribute("movements", farmService.getMovements(user));
            model.addAttribute("username", user.getUsername());
            model.addAttribute("user", user);
            model.addAttribute("balance", user.getBalance());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }
        return "dashboard";
    }

    @GetMapping("/buy")
    public String buyArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user, Model model) {
        try {
            if (farmService.buy(id, quantity, user)) {
                model.addAttribute("success", "Purchase successful!");
            } else {
                model.addAttribute("error", "Insufficient balance or stock");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error during purchase: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/update-balance")
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
                // It's generally better to fetch the user from the repository again
                // to ensure you have the latest version before updating.
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

    @GetMapping("/sell")
    public String sellArticle(@RequestParam Long id, @RequestParam int quantity, @AuthenticationPrincipal User user, Model model) {
        try {
            if (farmService.sell(id, quantity, user)) {
                model.addAttribute("success", "Sale successful!");
            } else {
                model.addAttribute("error", "Insufficient stock");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error during sale: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
