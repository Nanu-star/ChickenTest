package com.chickentest.service;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.User;
import com.chickentest.exception.InsufficientStockException;
import com.chickentest.exception.InsufficientBalanceException; // Added
import com.chickentest.repository.ArticleRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import com.chickentest.repository.UserRepository;
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
        user.setBalance(100.0); // Initial balance
        userRepository.save(user);
        chicken = new Article();
        chicken.setName("Hen");
        chicken.setCategory(chickens);
        chicken.setUnits(5);
        chicken.setPrice(2.0);
        chicken.setAge(0);
        chicken.setProduction("test");
        chicken.setCreation(LocalDateTime.now()); // Changed from new Date()
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
        assertEquals(6.0, m.getAmount()); // Assert movement amount

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(94.0, updatedUser.getBalance()); // Assert balance deduction
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

    @Test
    void testBuyInsufficientBalanceThrowsException() {
        user.setBalance(5.0); // Price is 2.0, buy 3 units = 6.0 cost
        userRepository.save(user);
        assertThrows(InsufficientBalanceException.class, () -> farmService.buy(chicken.getId(), 3, user));
    }

    @Test
    void testAddArticleSuccessful() {
        // User has 100.0 balance. New article costs 10 * 5.0 = 50.0
        Article newArticle = Article.builder()
                .name("New Eggs")
                .category(chickens) // Assuming 'chickens' category can be used, or create an 'eggs' category
                .units(10)
                .price(5.0)
                .age(0)
                .user(user) // Set the user
                .creation(LocalDateTime.now())
                .production("test batch")
                .build();

        boolean result = farmService.addArticle(newArticle, user);
        assertTrue(result);

        // Verify article is saved
        assertNotNull(newArticle.getId());
        Article savedArticle = articleRepository.findById(newArticle.getId()).orElseThrow();
        assertEquals(10, savedArticle.getUnits());

        // Verify movement
        List<Movement> movements = movementRepository.findByUsername(user.getUsername());
        assertEquals(1, movements.size()); // Expect 1 movement related to this add
        Movement m = movements.get(0);
        assertEquals(MovementType.PURCHASE, m.getType());
        assertEquals(10, m.getUnits());
        assertEquals(50.0, m.getAmount());
        assertEquals(newArticle.getId(), m.getArticle().getId());


        // Verify balance deduction
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(50.0, updatedUser.getBalance()); // 100.0 - 50.0 = 50.0
    }

    @Test
    void testAddArticleInsufficientBalanceThrowsException() {
        user.setBalance(10.0); // User has 10.0
        userRepository.save(user);

        Article newArticle = Article.builder()
                .name("Expensive Eggs")
                .category(chickens) // Same category for simplicity
                .units(5)
                .price(5.0) // Total cost = 25.0
                .age(0)
                .user(user)
                .creation(LocalDateTime.now())
                .production("test batch")
                .build();

        assertThrows(InsufficientBalanceException.class, () -> farmService.addArticle(newArticle, user));
    }
}
