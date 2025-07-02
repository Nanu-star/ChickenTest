package com.example.chickentest.controller;

import com.example.chickentest.entity.Article;
import com.example.chickentest.entity.User;
import com.example.chickentest.service.ArticleService;
import com.example.chickentest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/farms/{farmId}/articles")
public class ArticleRestController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    // DTO for creating an article
    public static class CreateArticleRequest {
        public String name;
        public int stock;
        public double price;
    }

    @PostMapping
    public ResponseEntity<?> createArticle(@PathVariable Long farmId, @RequestBody CreateArticleRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<Article> articleOpt = articleService.createArticle(req.name, req.stock, req.price, user, farmId);
        return articleOpt.map(article -> new ResponseEntity<>(article, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Farm not found or user does not own this farm."));
    }

    @GetMapping
    public ResponseEntity<?> getArticles(@PathVariable Long farmId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Article>> articlesOpt = articleService.getArticlesByFarm(farmId, user);
        return articlesOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Farm not found or user does not own this farm."));
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<?> getArticleById(@PathVariable Long farmId, @PathVariable Long articleId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<Article> articleOpt = articleService.getArticleByIdAndFarm(articleId, farmId, user);
        return articleOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Article or Farm not found, or user does not own this farm."));
    }
}
