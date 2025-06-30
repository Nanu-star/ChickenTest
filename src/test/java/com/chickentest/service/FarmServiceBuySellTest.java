package com.chickentest.service;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.User;
import com.chickentest.exception.InsufficientStockException;
import com.chickentest.repository.ArticleRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import com.chickentest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FarmServiceBuySellTest {
    @Autowired
    private FarmService farmService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private MovementRepository movementRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    private Category chickens;
    private User user;
    private Article chicken;

    @BeforeEach
    void setup() {
        chickens = categoryRepository.findByName("CHICKEN");
        articleRepository.deleteAll();
        movementRepository.deleteAll();
        userRepository.deleteAll();
        user = new User();
        user.setUsername("tester");
        user.setPassword("pass");
        user.setBalance(0);
        userRepository.save(user);
        chicken = new Article();
        chicken.setName("Hen");
        chicken.setCategory(chickens);
        chicken.setUnits(5);
        chicken.setPrice(2.0);
        chicken.setAge(0);
        chicken.setProduction("test");
        chicken.setCreation(new Date());
        articleRepository.save(chicken);
    }

    @Test
    void testBuyIncreasesStockAndCreatesMovement() {
        boolean result = farmService.buy(chicken.getId(), 3, user);
        assertTrue(result);
        Article updated = articleRepository.findById(chicken.getId()).orElseThrow();
        assertEquals(8, updated.getUnits());
        List<Movement> movements = movementRepository.findAll();
        assertEquals(1, movements.size());
        Movement m = movements.get(0);
        assertEquals(MovementType.PURCHASE, m.getType());
        assertEquals(3, m.getUnits());
    }

    @Test
    void testSellReducesStockAndUpdatesBalance() {
        boolean result = farmService.sell(chicken.getId(), 2, user);
        assertTrue(result);
        Article updated = articleRepository.findById(chicken.getId()).orElseThrow();
        assertEquals(3, updated.getUnits());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(4.0, updatedUser.getBalance());
        List<Movement> movements = movementRepository.findAll();
        assertEquals(1, movements.size());
        Movement m = movements.get(0);
        assertEquals(MovementType.SALE, m.getType());
        assertEquals(2, m.getUnits());
    }

    @Test
    void testSellInsufficientStockThrowsException() {
        assertThrows(InsufficientStockException.class, () -> farmService.sell(chicken.getId(), 10, user));
    }
}
