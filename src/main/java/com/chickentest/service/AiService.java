package com.chickentest.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.chickentest.config.Constants;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.exception.InsufficientStockException;
import com.chickentest.repository.ArticleRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import com.chickentest.repository.UserRepository;
import com.chickentest.service.FarmService;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiService {
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final OpenAiClient openAiClient;
    private Category chickensCategory;
    private Category eggsCategory;

    @Autowired
    public AiService(ArticleRepository articleRepository, CategoryRepository categoryRepository, OpenAiClient openAiClient) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.openAiClient = openAiClient;
    }

    @PostConstruct
    public void initCategories() {
        chickensCategory = categoryRepository.findByName("CHICKEN");
        if (chickensCategory == null) {
            log.warn("CHICKEN category not found in database!");
        }
        eggsCategory = categoryRepository.findByName("EGG");
        if (eggsCategory == null) {
            log.warn("EGG category not found in database!");
        }
    }
    

    public ResponseEntity<String> generateAIReport(List<Movement> movements, User user) {
        int totalChickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
        int eggsProducedToday = 0;
        int eggsSoldToday = 0;
        int currentEggStock = articleRepository.findTotalUnitsByCategory(eggsCategory);
        
        // Assuming movements, user are not null and have necessary getters
        // This is just a simplified version of your stream logic for the example
        if (movements != null) {
            eggsProducedToday = movements.stream()
        .filter(m -> m.getType() == MovementType.SYSTEM) // Assuming MovementType enum exists
        .filter(m -> m.getArticle().getCategory().getName().equalsIgnoreCase("EGG"))
        .filter(m -> isToday(m.getDate()))
        .mapToInt(Movement::getUnits)
        .sum();
            eggsSoldToday = movements.stream()
        .filter(m -> m.getType() == MovementType.SALE) // Assuming MovementType enum exists
        .filter(m -> m.getArticle().getCategory().getName().equalsIgnoreCase("EGG"))
        .filter(m -> isToday(m.getDate()))
        .mapToInt(Movement::getUnits)
        .sum();
        }
        double userBalance = 0.0;
        String userRole = "USER";
        if (user != null) {
             userBalance = user.getBalance(); // Assuming getBalance exists
             userRole = user.getRole();     // Assuming getRole exists
        }


        String prompt = String.format(
            "Here is my farm data:\n" +
            "- Current chicken stock: %d\n" +
            "- Current egg stock: %d\n" +
            "- Eggs produced today: %d\n" +
            "- Eggs sold today: %d\n" +
            "- User balance: %.2f\n" +
            "- User role: %s\n" +
            "Generate a brief report, highlighting strengths and weaknesses, possible improvements, and important alerts.",
            totalChickens,
            currentEggStock,
            eggsProducedToday,
            eggsSoldToday,
            userBalance,
            userRole
        );

        try {
            String aiReport = openAiClient.generateReport(prompt);
            log.info("Successfully generated AI report.");
            return ResponseEntity.ok(aiReport);
        }  catch (RuntimeException e) {
            log.error("Error during AI report generation via OpenAiClient: {}", e.getMessage(), e);
            
            if (e.getMessage() != null && e.getMessage().contains("AI service is currently unavailable")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
            } else if (e.getMessage() != null && e.getMessage().contains("Error parsing AI model's response")) {
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse response from AI Model.");
            } else if (e.getMessage() != null && (e.getMessage().startsWith("AI model error:") || e.getMessage().startsWith("Error from AI service:"))) {
                return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An error occurred while generating the AI report: " + e.getMessage());
        }
    }
    
    // Helper to check if a Date is today
    private boolean isToday(LocalDateTime date) {
        if (date == null) return false;
        return date.equals(LocalDateTime.now());
    }
}
