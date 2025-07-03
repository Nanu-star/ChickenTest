package com.chickentest.service;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.repository.ArticleRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import com.chickentest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.chickentest.config.Constants;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.Report;
import com.chickentest.exception.InsufficientStockException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FarmServiceImplTest {
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private MovementRepository movementRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AiService aiService; // Added AiService mock

    @InjectMocks
    private FarmServiceImpl farmService;

    private User user;
    private Category eggCategory;
    private Category chickenCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testuser").balance(1000.0).build(); // Added balance for user

        eggCategory = new Category(1L, "EGG", "Egg");
        chickenCategory = new Category(2L, "CHICKEN", "Chicken");

        // Mock initCategories behavior
        when(categoryRepository.findByName("EGG")).thenReturn(eggCategory);
        when(categoryRepository.findByName("CHICKEN")).thenReturn(chickenCategory);
        farmService.initCategories(); // Call initCategories after mocks are set up
    }

    // Helper method to create a sample article
    private Article createArticle(Long id, String name, int units, double price, Category category) {
        return Article.builder()
                .id(id)
                .name(name)
                .units(units)
                .price(price)
                .age(0)
                .category(category)
                .user(user)
                .movements(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void addArticle_withNoMovements_savesArticleAndSetsUser() {
        Category egg = new Category();
        egg.setId(1L);
        egg.setName("Egg");
        Article article = Article.builder()
                .name("Test Article")
                .units(10)
                .price(5.0)
                .age(0)
                .category(egg)
                .build();
        when(categoryRepository.findById(egg.getId())).thenReturn(Optional.of(egg));
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        Article saved = farmService.addArticle(article, user);
        assertEquals(user, saved.getUser());
        verify(articleRepository, times(1)).save(article);
    }

    @Test
    void addArticle_withMovements_setsArticleAndUserOnMovements() {
        Category egg = new Category();
        egg.setId(1L);
        egg.setName("Egg");

        List<Movement> movements = new ArrayList<>();
        when(categoryRepository.findById(egg.getId())).thenReturn(Optional.of(egg));
        Article article = Article.builder()
                .name("Test Article")
                .units(10)
                .price(5.0)
                .age(0)
                .category(egg)
                .movements(movements)
                .build();
        Movement movement = Movement.builder().units(10).amount(50.0).build();
        movements.add(movement);
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        Article saved = farmService.addArticle(article, user);
        assertEquals(user, movement.getUser());
        assertEquals(article, movement.getArticle());
    }

    @Test
    void addArticle_withNullMovements_doesNotThrow() {
        Category egg = new Category();
        egg.setId(1L);
        egg.setName("Egg");

        Article article = Article.builder()
                .name("Test Article")
                .units(10)
                .price(5.0)
                .age(0)
                .category(egg)
                .movements(null)
                .build();
        when(categoryRepository.findById(egg.getId())).thenReturn(Optional.of(egg));
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        assertDoesNotThrow(() -> farmService.addArticle(article, user));
    }

    @Test
    void addArticle_withInvalidCategory_throwsException() {
        Article article = Article.builder()
                .name("Test Article")
                .units(10)
                .price(5.0)
                .age(0)
                .category(new Category(99L, "INVALID", "Invalid"))
                .build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(FarmException.class, () -> farmService.addArticle(article, user));
    }

    @Test
    void addArticle_withNullCategory_throwsException() {
        Article article = Article.builder()
                .name("Test Article")
                .units(10)
                .price(5.0)
                .age(0)
                .category(null) // Null category
                .build();
        // No need to mock categoryRepository.findById for this case if validation happens before
        assertThrows(FarmException.class, () -> farmService.addArticle(article, user), "Category not found with ID: null");
    }


    // --- Tests for buy method ---
    @Test
    void buy_successfulPurchase_updatesArticleAndCreatesMovement() {
        Article eggArticle = createArticle(1L, "Brown Egg", 50, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(50); // Current stock
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        boolean result = farmService.buy(1L, 10, user);

        assertTrue(result);
        assertEquals(60, eggArticle.getUnits()); // 50 + 10
        verify(movementRepository, times(1)).save(any(Movement.class));

        ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(movementCaptor.capture());
        Movement capturedMovement = movementCaptor.getValue();
        assertEquals(10, capturedMovement.getUnits());
        assertEquals(5.0, capturedMovement.getAmount()); // 10 * 0.5
        assertEquals(eggArticle, capturedMovement.getArticle());
        assertEquals(user, capturedMovement.getUser());
        assertEquals(MovementType.BUY, capturedMovement.getType());
    }

    @Test
    void buy_articleNotFound_throwsFarmException() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(FarmException.class, () -> {
            farmService.buy(1L, 10, user);
        });
        assertEquals("Article not found with id: 1", exception.getMessage());
    }

    @Test
    void buy_invalidQuantity_throwsFarmException() {
        Article eggArticle = createArticle(1L, "Brown Egg", 50, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));

        Exception exception = assertThrows(FarmException.class, () -> {
            farmService.buy(1L, 0, user); // Zero quantity
        });
        assertEquals("Quantity must be positive", exception.getMessage());

        Exception exceptionNegative = assertThrows(FarmException.class, () -> {
            farmService.buy(1L, -5, user); // Negative quantity
        });
        assertEquals("Quantity must be positive", exceptionNegative.getMessage());
    }

    @Test
    void buy_exceedsEggStockLimit_returnsFalse() {
        Article eggArticle = createArticle(1L, "Brown Egg", 50, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));
        // Current stock is 1990, buying 20 would exceed MAX_EGGS (2000)
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(1990);


        boolean result = farmService.buy(1L, 20, user); // Try to buy 20, (1990 + 20 = 2010 > 2000)
        assertFalse(result);
        assertEquals(50, eggArticle.getUnits()); // Units should not change
        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void buy_withinEggStockLimit_returnsTrue() {
        Article eggArticle = createArticle(1L, "Brown Egg", 50, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(1980); // Current stock
        when(articleRepository.save(any(Article.class))).thenReturn(eggArticle);


        boolean result = farmService.buy(1L, 10, user); // 1980 + 10 = 1990 <= 2000
        assertTrue(result);
        assertEquals(60, eggArticle.getUnits());
        verify(movementRepository, times(1)).save(any(Movement.class));
    }


    @Test
    void buy_exceedsChickenStockLimit_returnsFalse() {
        Article chickenArticle = createArticle(2L, "Hen", 30, 5.0, chickenCategory);
        when(articleRepository.findById(2L)).thenReturn(Optional.of(chickenArticle));
        // Current stock is 1490, buying 20 would exceed MAX_CHICKENS (1500)
        when(articleRepository.findTotalUnitsByCategory(chickenCategory)).thenReturn(1490);

        boolean result = farmService.buy(2L, 20, user); // Try to buy 20 (1490 + 20 = 1510 > 1500)
        assertFalse(result);
        assertEquals(30, chickenArticle.getUnits()); // Units should not change
        verify(movementRepository, never()).save(any(Movement.class));
    }

    // --- Tests for sell method ---
    @Test
    void sell_successfulSale_updatesArticleAndCreatesMovement() {
        Article eggArticle = createArticle(1L, "Brown Egg", 50, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Mock checkStockLimit to always return true for this successful sale test
        // We assume it's an egg, current total eggs 50, selling 10 means -10. 50 - 10 = 40. This is <= MAX_EGGS (2000)
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(50);


        boolean result = farmService.sell(1L, 10, user);

        assertTrue(result);
        assertEquals(40, eggArticle.getUnits()); // 50 - 10
        verify(movementRepository, times(1)).save(any(Movement.class));

        ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(movementCaptor.capture());
        Movement capturedMovement = movementCaptor.getValue();
        assertEquals(10, capturedMovement.getUnits());
        assertEquals(5.0, capturedMovement.getAmount()); // 10 * 0.5
        assertEquals(eggArticle, capturedMovement.getArticle());
        assertEquals(user, capturedMovement.getUser());
        assertEquals(MovementType.SALE, capturedMovement.getType());
    }

    @Test
    void sell_articleNotFound_throwsInsufficientStockExceptionWithMessage() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(InsufficientStockException.class, () -> {
            farmService.sell(1L, 10, user);
        });
        assertEquals("Article not found with ID: 1", exception.getMessage());
    }

    @Test
    void sell_insufficientStock_throwsInsufficientStockException() {
        Article eggArticle = createArticle(1L, "Brown Egg", 5, 0.5, eggCategory); // Only 5 units available
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));

        Exception exception = assertThrows(InsufficientStockException.class, () -> {
            farmService.sell(1L, 10, user); // Try to sell 10
        });
        assertEquals("Insufficient stock. Available: 5, requested: 10", exception.getMessage());
    }

    @Test
    void sell_invalidQuantity_throwsFarmException() {
        Article eggArticle = createArticle(1L, "Brown Egg", 50, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(eggArticle));

        Exception exception = assertThrows(FarmException.class, () -> {
            farmService.sell(1L, 0, user); // Zero quantity
        });
        assertEquals("Quantity must be positive", exception.getMessage());

        Exception exceptionNegative = assertThrows(FarmException.class, () -> {
            farmService.sell(1L, -5, user); // Negative quantity
        });
        assertEquals("Quantity must be positive", exceptionNegative.getMessage());
    }

    // --- Tests for hatchEggs / incrementEggAges ---
    @Test
    void hatchEggs_noEggsToAgeOrHatch_noChanges() {
        when(articleRepository.findByCategory(eggCategory)).thenReturn(new ArrayList<>());
        farmService.hatchEggs(); // This calls hatchEggsInternal -> incrementEggAges
        verify(articleRepository, never()).save(any(Article.class));
        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void hatchEggs_eggsAgeButDoNotHatch() {
        Article egg1 = createArticle(1L, "Egg Day 1", 10, 0.5, eggCategory);
        egg1.setAge(1); // Age is 1 day
        egg1.setLastAgedDate(LocalDate.now().minusDays(1)); // Last aged yesterday

        when(articleRepository.findByCategory(eggCategory)).thenReturn(List.of(egg1));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        farmService.hatchEggs();

        assertEquals(2, egg1.getAge());
        assertEquals(LocalDate.now(), egg1.getLastAgedDate());
        verify(articleRepository, times(1)).save(egg1);
        verify(movementRepository, never()).save(any(Movement.class)); // No new chicken
    }

    @Test
    void hatchEggs_eggsHatchIntoChickens() {
        Article eggToHatch = createArticle(1L, "Almost Ready Egg", 5, 0.5, eggCategory);
        eggToHatch.setAge(Constants.EGG_HATCH_DAYS - 1); // Will hatch today
        eggToHatch.setLastAgedDate(LocalDate.now().minusDays(1)); // Last aged yesterday

        when(articleRepository.findByCategory(eggCategory)).thenReturn(List.of(eggToHatch));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Capture the new chicken article
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);

        farmService.hatchEggs();

        // Verify egg is reset
        assertEquals(0, eggToHatch.getUnits());
        assertEquals(0, eggToHatch.getAge());
        verify(articleRepository, times(2)).save(articleCaptor.capture()); // Once for egg, once for new chicken

        Article newChicken = articleCaptor.getAllValues().stream()
                                          .filter(a -> a.getCategory().equals(chickenCategory))
                                          .findFirst().orElse(null);
        assertNotNull(newChicken);
        assertEquals(eggToHatch.getName(), newChicken.getName()); // Name should be the same
        assertEquals(5, newChicken.getUnits()); // Units from hatched eggs
        assertEquals(0, newChicken.getAge());
        assertEquals(chickenCategory, newChicken.getCategory());

        // Verify movement is created for new chicken
        ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository, times(1)).save(movementCaptor.capture());
        Movement systemMovement = movementCaptor.getValue();
        assertEquals(newChicken, systemMovement.getArticle());
        assertEquals(5, systemMovement.getUnits());
        assertEquals(MovementType.SYSTEM, systemMovement.getType());
        assertEquals("system", systemMovement.getUser().getUsername());
    }

    @Test
    void hatchEggs_eggLastAgedToday_doesNotAgeAgain() {
        Article egg1 = createArticle(1L, "Egg Aged Today", 10, 0.5, eggCategory);
        egg1.setAge(1);
        egg1.setLastAgedDate(LocalDate.now()); // Already aged today

        when(articleRepository.findByCategory(eggCategory)).thenReturn(List.of(egg1));

        farmService.hatchEggs();

        assertEquals(1, egg1.getAge()); // Age should not change
        verify(articleRepository, never()).save(egg1); // Should not be saved if not aged
    }

     @Test
    void hatchEggsInternal_callsIncrementEggAges() {
        // This is more of a sanity check that hatchEggsInternal calls the private method.
        // We rely on the tests for hatchEggs which uses hatchEggsInternal.
        // To properly test this in isolation, we would need to refactor incrementEggAges
        // to be package-private or use PowerMockito to mock private methods (which is often discouraged).

        // For now, we'll spy on the service to verify the internal call path.
        FarmServiceImpl spiedFarmService = Mockito.spy(farmService);
        Mockito.doNothing().when(spiedFarmService).incrementEggAges(); // Use the correct method name

        spiedFarmService.hatchEggsInternal();

        verify(spiedFarmService, times(1)).incrementEggAges();
    }

    // --- Tests for generateReport ---
    @Test
    void generateReport_calculatesCorrectTotals() {
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(100);
        when(articleRepository.findTotalUnitsByCategory(chickenCategory)).thenReturn(50);
        when(movementRepository.countProducedBatches(MovementType.SYSTEM)).thenReturn(5L); // Long
        when(movementRepository.calculateTotalSales(MovementType.SALE)).thenReturn(250.75);

        Report report = farmService.generateReport();

        assertNotNull(report.getDate());
        assertEquals(100, report.getTotalEggs());
        assertEquals(50, report.getTotalChickens());
        assertEquals(5, report.getProducedBatches());
        assertEquals(250.75, report.getTotalSales());
    }

    // --- Tests for generateAIReport ---
    @Test
    void generateAIReport_constructsCorrectPromptAndCallsAiService() {
        User testUser = User.builder().id(1L).username("aiUser").balance(500.0).role("ADMIN").build();
        List<Movement> movements = new ArrayList<>();
        Article eggArticle = createArticle(1L, "Egg", 10, 0.5, eggCategory);
        // Movement today, system, egg
        movements.add(Movement.builder().type(MovementType.SYSTEM).article(eggArticle).units(10).date(LocalDateTime.now()).build());
        // Movement today, sale, egg
        movements.add(Movement.builder().type(MovementType.SALE).article(eggArticle).units(5).date(LocalDateTime.now()).build());
        // Movement yesterday, sale, egg (should be ignored by "today" logic)
        movements.add(Movement.builder().type(MovementType.SALE).article(eggArticle).units(3).date(LocalDateTime.now().minusDays(1)).build());


        when(articleRepository.findTotalUnitsByCategory(chickenCategory)).thenReturn(150);
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(200);
        when(aiService.generateReport(anyString())).thenReturn("AI Generated Report Content");

        String reportContent = farmService.generateAIReport(movements, testUser);

        assertEquals("AI Generated Report Content", reportContent);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiService).generateReport(promptCaptor.capture());
        String capturedPrompt = promptCaptor.getValue();

        assertTrue(capturedPrompt.contains("- Current chicken stock: 150"));
        assertTrue(capturedPrompt.contains("- Current egg stock: 200"));
        assertTrue(capturedPrompt.contains("- Eggs produced today: 10")); // Only today's system movement
        assertTrue(capturedPrompt.contains("- Eggs sold today: 5"));   // Only today's sale movement
        assertTrue(capturedPrompt.contains("- User balance: 500.00")); // Formatted
        assertTrue(capturedPrompt.contains("- User role: ADMIN"));
    }

    @Test
    void generateAIReport_withNullMovementsAndUser_handlesGracefully() {
        when(articleRepository.findTotalUnitsByCategory(chickenCategory)).thenReturn(50);
        when(articleRepository.findTotalUnitsByCategory(eggCategory)).thenReturn(30);
        when(aiService.generateReport(anyString())).thenReturn("AI Fallback Report");

        String reportContent = farmService.generateAIReport(null, null);
        assertEquals("AI Fallback Report", reportContent);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiService).generateReport(promptCaptor.capture());
        String capturedPrompt = promptCaptor.getValue();

        assertTrue(capturedPrompt.contains("- Eggs produced today: 0"));
        assertTrue(capturedPrompt.contains("- Eggs sold today: 0"));
        assertTrue(capturedPrompt.contains("- User balance: 0.00"));
        assertTrue(capturedPrompt.contains("- User role: USER")); // Default role if user is null
    }


    // --- Tests for other simple methods ---
    @Test
    void loadInventory_returnsAllArticles() {
        List<Article> expectedArticles = List.of(createArticle(1L, "Egg", 10, 0.5, eggCategory));
        when(articleRepository.findAll()).thenReturn(expectedArticles);
        List<Article> actualArticles = farmService.loadInventory(user);
        assertEquals(expectedArticles, actualArticles);
    }

    @Test
    void getMovements_returnsUserMovements() {
        List<Movement> expectedMovements = List.of(Movement.builder().user(user).build());
        when(movementRepository.findAllByUser(user)).thenReturn(expectedMovements);
        List<Movement> actualMovements = farmService.getMovements(user);
        assertEquals(expectedMovements, actualMovements);
    }

    @Test
    void updateArticle_savesArticle() {
        Article articleToUpdate = createArticle(1L, "Old Name", 10, 0.5, eggCategory);
        when(articleRepository.save(articleToUpdate)).thenReturn(articleToUpdate);
        Article updatedArticle = farmService.updateArticle(articleToUpdate);
        assertEquals(articleToUpdate, updatedArticle);
        verify(articleRepository, times(1)).save(articleToUpdate);
    }

    @Test
    void deleteArticle_callsDeleteById() {
        Long articleIdToDelete = 1L;
        doNothing().when(articleRepository).deleteById(articleIdToDelete);
        farmService.deleteArticle(articleIdToDelete);
        verify(articleRepository, times(1)).deleteById(articleIdToDelete);
    }

    @Test
    void getArticle_existingId_returnsArticle() {
        Article expectedArticle = createArticle(1L, "Test Egg", 10, 0.5, eggCategory);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(expectedArticle));
        Article actualArticle = farmService.getArticle(1L);
        assertEquals(expectedArticle, actualArticle);
    }

    @Test
    void getArticle_nonExistingId_throwsFarmException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(FarmException.class, () -> {
            farmService.getArticle(99L);
        });
        assertEquals("Article not found with ID: 99", exception.getMessage());
    }

    @Test
    void getCategories_returnsAllCategories() {
        List<Category> expectedCategories = List.of(eggCategory, chickenCategory);
        when(categoryRepository.findAll()).thenReturn(expectedCategories);
        List<Category> actualCategories = farmService.getCategories();
        assertEquals(expectedCategories, actualCategories);
    }
}
