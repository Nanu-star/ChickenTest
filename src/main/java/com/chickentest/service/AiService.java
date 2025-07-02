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
    
    public String generateReport(String prompt) {
        try {
            String aiReport = openAiClient.generateReport(prompt);
            log.info("Successfully generated AI report.");
            return aiReport;
        } catch (RuntimeException e) {
            log.error("Error during AI report generation via OpenAiClient: {}", e.getMessage(), e);

            if (e.getMessage() != null && e.getMessage().contains("AI service is currently unavailable")) {
                return "AI service is currently unavailable";
            } else if (e.getMessage() != null && e.getMessage().contains("Error parsing AI model's response")) {
                return "Failed to parse response from AI Model.";
            } else if (e.getMessage() != null && (e.getMessage().startsWith("AI model error:") || e.getMessage().startsWith("Error from AI service:"))) {
                return e.getMessage();
            }
            return "An error occurred while generating the AI report: " + e.getMessage();
        }
    }
    
    // Helper to check if a Date is today
    private boolean isToday(LocalDateTime date) {
        if (date == null) return false;
        return date.equals(LocalDateTime.now());
    }
}
