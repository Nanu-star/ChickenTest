package com.chickentest.service;

import com.chickentest.config.Constants;
import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.repository.ArticleRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // Changed from java.util.Date
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FarmServiceSchedulingTest {

    @Autowired
    private FarmService farmService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MovementRepository movementRepository;

    private Category eggs;
    private Category chickens;

    @BeforeEach
    void setup() {
        eggs = categoryRepository.findByDisplayName(Constants.Category.EGGS);
        chickens = categoryRepository.findByDisplayName(Constants.Category.CHICKENS);
        articleRepository.deleteAll();
        movementRepository.deleteAll();
        Article egg = new Article();
        egg.setName("Batch A");
        egg.setCategory(eggs);
        egg.setUnits(10);
        egg.setPrice(1.0);
        egg.setAge(Constants.EGG_HATCH_DAYS - 1);
        egg.setProduction("test");
        egg.setCreation(LocalDateTime.now()); // Changed from new Date()
        articleRepository.save(egg);
    }

    @Test
    void testHatchEggs() {
        farmService.hatchEggs();

        List<Article> eggList = articleRepository.findByCategory(eggs);
        assertEquals(1, eggList.size());
        Article egg = eggList.get(0);
        assertEquals(0, egg.getUnits());
        assertEquals(0, egg.getAge());

        List<Article> chickenList = articleRepository.findByCategory(chickens);
        assertEquals(1, chickenList.size());
        Article chicken = chickenList.get(0);
        assertEquals(10, chicken.getUnits());

        List<Movement> movements = movementRepository.findAll();
        assertEquals(1, movements.size());
        Movement m = movements.get(0);
        assertEquals(MovementType.SYSTEM, m.getType());
        assertEquals(10, m.getUnits());
    }
}
