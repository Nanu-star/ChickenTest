package com.example.chickentest.service;

import com.example.chickentest.entity.*;
import com.example.chickentest.repository.FarmRepository;
import com.example.chickentest.repository.MovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MovementService {

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private FarmRepository farmRepository;

    // Method to record a buy movement
    public Optional<Movement> recordBuyMovement(Long articleId, int quantity, double price, User user, Long farmId) {
        Optional<Farm> farmOptional = farmRepository.findByIdAndOwner(farmId, user);
        if (farmOptional.isEmpty()) {
            return Optional.empty(); // Farm not found or user is not the owner
        }
        Farm farm = farmOptional.get();

        Optional<Article> articleOptional = articleService.updateArticleStock(articleId, farmId, quantity, user);
        if (articleOptional.isEmpty()) {
            return Optional.empty(); // Article not found, not enough stock, or user not owner
        }
        Article article = articleOptional.get();
        Movement movement = new Movement(article, quantity, price, MovementType.BUY, user, farm);
        return Optional.of(movementRepository.save(movement));
    }

    // Method to record a sell movement
    public Optional<Movement> recordSellMovement(Long articleId, int quantity, double price, User user, Long farmId) {
        Optional<Farm> farmOptional = farmRepository.findByIdAndOwner(farmId, user);
        if (farmOptional.isEmpty()) {
            return Optional.empty(); // Farm not found or user is not the owner
        }
        Farm farm = farmOptional.get();

        // For selling, quantityChange is negative
        Optional<Article> articleOptional = articleService.updateArticleStock(articleId, farmId, -quantity, user);
        if (articleOptional.isEmpty()) {
            return Optional.empty(); // Article not found, not enough stock, or user not owner
        }
        Article article = articleOptional.get();
        Movement movement = new Movement(article, quantity, price, MovementType.SELL, user, farm);
        return Optional.of(movementRepository.save(movement));
    }

    // Method to get movements for a specific farm, ensuring farm ownership
    public Optional<List<Movement>> getMovementsByFarm(Long farmId, User user) {
        Optional<Farm> farmOptional = farmRepository.findByIdAndOwner(farmId, user);
        if (farmOptional.isEmpty()) {
            return Optional.empty(); // Farm not found or user is not the owner
        }
        return Optional.of(movementRepository.findByFarm(farmOptional.get()));
    }
}
