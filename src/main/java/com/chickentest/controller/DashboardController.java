package com.chickentest.controller;

import com.chickentest.domain.Article;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final FarmService farmService;
    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());

    @Autowired
    public DashboardController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = (User) userDetails;
            List<Article> articles = farmService.loadInventory(user);
            model.addAttribute("articles", articles);
            model.addAttribute("username", user.getUsername());
            model.addAttribute("user", user);
            return "dashboard";
        } catch (FarmException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            logger.severe("Unexpected error in dashboard: " + e.getMessage());
            throw new FarmException("An unexpected error occurred", e);
        }
    }
}
