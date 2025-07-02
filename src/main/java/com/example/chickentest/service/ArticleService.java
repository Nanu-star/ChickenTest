package com.example.chickentest.service;

import com.example.chickentest.entity.Article;
import com.example.chickentest.entity.Farm;
import com.example.chickentest.entity.User;
import com.example.chickentest.repository.ArticleRepository;
import com.example.chickentest.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FarmRepository farmRepository;

    // Method to create an article, ensuring farm ownership
    public Optional<Article> createArticle(String name, int stock, double price, User user, Long farmId) {
        Optional<Farm> farmOptional = farmRepository.findByIdAndOwner(farmId, user);
        if (farmOptional.isEmpty()) {
            return Optional.empty(); // Farm not found or user is not the owner
        }
        Farm farm = farmOptional.get();
        Article article = new Article(name, stock, price, user, farm);
        return Optional.of(articleRepository.save(article));
    }

    // Method to get articles for a specific farm, ensuring farm ownership
    public Optional<List<Article>> getArticlesByFarm(Long farmId, User user) {
        Optional<Farm> farmOptional = farmRepository.findByIdAndOwner(farmId, user);
        if (farmOptional.isEmpty()) {
            return Optional.empty(); // Farm not found or user is not the owner
        }
        return Optional.of(articleRepository.findByFarm(farmOptional.get()));
    }

    // Method to get a specific article by ID and farm, ensuring farm ownership
    public Optional<Article> getArticleByIdAndFarm(Long articleId, Long farmId, User user) {
        Optional<Farm> farmOptional = farmRepository.findByIdAndOwner(farmId, user);
        if (farmOptional.isEmpty()) {
            return Optional.empty(); // Farm not found or user is not the owner
        }
        return articleRepository.findByIdAndUserAndFarm(articleId, user, farmOptional.get());
    }

    // Method to update an article's stock (e.g., after a buy/sell operation)
    public Optional<Article> updateArticleStock(Long articleId, Long farmId, int quantityChange, User user) {
        Optional<Article> articleOptional = getArticleByIdAndFarm(articleId, farmId, user);
        if (articleOptional.isEmpty()) {
            return Optional.empty(); // Article not found or user is not owner of the farm
        }
        Article article = articleOptional.get();
        int newStock = article.getStock() + quantityChange;
        if (newStock < 0) {
            return Optional.empty(); // Not enough stock
        }
        article.setStock(newStock);
        return Optional.of(articleRepository.save(article));
    }
}
