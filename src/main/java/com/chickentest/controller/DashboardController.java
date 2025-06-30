package com.chickentest.controller;

import com.chickentest.domain.User;
import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final FarmService farmService;

    @Autowired
    public DashboardController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    public String showDashboard(@AuthenticationPrincipal User user, Model model) {
        try {
            model.addAttribute("articles", farmService.loadInventory(user));
            model.addAttribute("movements", farmService.getMovements(user));
            model.addAttribute("username", user.getUsername());
            model.addAttribute("user", user);
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
