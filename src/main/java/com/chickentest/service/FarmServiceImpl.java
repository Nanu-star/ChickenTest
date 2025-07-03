package com.chickentest.service;

import com.chickentest.config.Constants;
import com.chickentest.domain.*;
import com.chickentest.exception.FarmException;
import com.chickentest.exception.InsufficientStockException;
import com.chickentest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FarmServiceImpl implements FarmService {
    private static final Logger logger = LoggerFactory.getLogger(FarmServiceImpl.class);

    private final ArticleRepository articleRepository;
    private final MovementRepository movementRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    @org.springframework.beans.factory.annotation.Value("${farm.max-eggs:2000}")
    private int maxEggs;

    @org.springframework.beans.factory.annotation.Value("${farm.max-chickens:1500}")
    private int maxChickens;

    @org.springframework.beans.factory.annotation.Value("${farm.egg-hatch-days:3}")
    private int eggHatchDays;

    private Category chickensCategory;
    private Category eggsCategory;
    private User systemUser; // Added for system user

    @Autowired
    public FarmServiceImpl(ArticleRepository articleRepository,
                           MovementRepository movementRepository,
                           CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           AiService aiService) {
        this.articleRepository = articleRepository;
        this.movementRepository = movementRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @PostConstruct
    void initializeFarmData() { // Renamed and expanded
        chickensCategory = categoryRepository.findByName("CHICKEN");
        eggsCategory = categoryRepository.findByName("EGG");
        if (chickensCategory == null || eggsCategory == null) {
            throw new FarmException("Required categories (CHICKEN, EGG) not found in database. Please initialize them.");
        }

        // Initialize system user
        this.systemUser = userRepository.findByUsername("system")
            .orElseGet(() -> {
                User sysUser = new User();
                sysUser.setUsername("system");
                // In a real app, password should be securely hashed if login was possible,
                // or made clear it's a system-only, non-loginable account.
                sysUser.setPassword("---system_account_no_login---");
                sysUser.setRole("SYSTEM"); // Assuming a "SYSTEM" role exists or can be handled
                sysUser.setEnabled(false); // Typically system users don't log in
                sysUser.setBalance(0); // System user shouldn't have a balance for transactions
                logger.info("Creating system user 'system'.");
                return userRepository.save(sysUser);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> loadInventory(User user) {
        return articleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movement> getMovements(User user) {
        return movementRepository.findAllByUser(user);
    }

    @Override
    @Transactional
    public void buy(Long articleId, int quantity, User authenticatedUser) { // Return void, User is authenticatedPrincipal
        validatePositiveQuantity(quantity);

        Article article = articleRepository.findByIdForUpdate(articleId) // Use locking
                .orElseThrow(() -> new ArticleNotFoundException("Article to buy not found with id: " + articleId));

        if (!checkStockLimitCanAdd(article, quantity)) {
            throw new MaxStockExceededException("Buying " + quantity + " units of " + article.getName() +
                    " (ID: " + articleId + ") would exceed maximum stock limit for category " + article.getCategory().getName() + ".");
        }

        double amount = article.getPrice() * quantity;

        // Manage User balance explicitly
        User managedUser = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new FarmException("Authenticated user not found in database with ID: " + authenticatedUser.getId()));

        if (managedUser.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance. Current: " + managedUser.getBalance() +
                    ", required: " + amount + " to buy " + quantity + " of " + article.getName());
        }
        managedUser.setBalance(managedUser.getBalance() - amount);
        // userRepository.save(managedUser); // JPA tracks changes to managedUser, will save on transaction commit. Explicit save is optional.

        performTransaction(article, quantity, amount, MovementType.BUY, managedUser); // Pass managedUser
        logger.info("User {} bought {} units of article {} (ID: {}) for {}", managedUser.getUsername(), quantity, article.getName(), articleId, amount);
    }

    @Override
    @Transactional
    public void sell(Long articleId, int quantity, User authenticatedUser) { // Return void
        validatePositiveQuantity(quantity);

        Article article = articleRepository.findByIdForUpdate(articleId) // Use locking
                .orElseThrow(() -> new ArticleNotFoundException("Article to sell not found with ID: " + articleId));

        if (article.getUnits() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + article.getName() + " (ID: " + articleId + "). Available: " +
                    article.getUnits() + ", requested: " + quantity);
        }

        double amount = article.getPrice() * quantity;

        // Manage User balance explicitly
        User managedUser = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new FarmException("Authenticated user not found in database with ID: " + authenticatedUser.getId()));

        managedUser.setBalance(managedUser.getBalance() + amount);
        // userRepository.save(managedUser); // JPA tracks changes, will save on transaction commit.

        performTransaction(article, quantity, amount, MovementType.SALE, managedUser); // Pass managedUser
        logger.info("User {} sold {} units of article {} (ID: {}) for {}", managedUser.getUsername(), quantity, article.getName(), articleId, amount);
    }

    @Override
    @Transactional
    public Article addArticle(Article article, User user) {
        // Validar y setear categoría real desde DB
        if (article.getCategory() == null || article.getCategory().getId() == null) {
            throw new FarmException("Category not found with ID: " + (article.getCategory() == null ? "null" : article.getCategory().getId()));
        }
        Category cat = categoryRepository.findById(article.getCategory().getId())
                .orElseThrow(() -> new FarmException("Category not found with ID: " + article.getCategory().getId()));
        article.setCategory(cat);

        // Setear usuario real
        article.setUser(user);
        // Inicializar movements si es null
        if (article.getMovements() == null) article.setMovements(new ArrayList<>());

        // Setear referencias en movements
        for (Movement movement : article.getMovements()) {
            movement.setId(null);
            movement.setArticle(article);
            movement.setUser(user);
        }

        // Guardar article (si tenés cascade en movements, se guardan juntos)
        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public Article getArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new FarmException("Article not found with ID: " + id));
    }

    @Override
    public Article updateArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public Report generateReport() {
        return Report.builder()
                .date(LocalDateTime.now().toString())
                .totalEggs(articleRepository.findTotalUnitsByCategory(eggsCategory))
                .totalChickens(articleRepository.findTotalUnitsByCategory(chickensCategory))
                .producedBatches(movementRepository.countProducedBatches(MovementType.SYSTEM).intValue())
                .totalSales(movementRepository.calculateTotalSales(MovementType.SALE))
                .build();
    }

    @Override
    @Transactional
    public void hatchEggs() { // This is the main entry point for the hatching process
        incrementEggAges();
    }

    // hatchEggsInternal() removed as it's redundant with hatchEggs() being the public API.

    private void incrementEggAges() { // Made private, as it's an internal part of hatchEggs process
        LocalDate today = LocalDate.now();
        List<Article> eggs = articleRepository.findByCategory(eggsCategory);
        for (Article egg : eggs) {
            if (egg.getLastAgedDate() == null || egg.getLastAgedDate().isBefore(today)) {
                egg.setAge(egg.getAge() + 1);
                egg.setLastAgedDate(today);
                if (egg.getAge() < eggHatchDays) { // Use injected value
                    articleRepository.save(egg);
                } else {
                    int hatchedUnits = egg.getUnits();
                    egg.setUnits(0);
                    egg.setAge(0);
                    articleRepository.save(egg);

                    Article chicken = Article.builder()
                            .category(chickensCategory)
                            .name(egg.getName())
                            .age(0)
                            .units(hatchedUnits)
                            .price(egg.getPrice())
                            .createdAt(LocalDateTime.now())
                            .build();
                    articleRepository.save(chicken);

                    Movement movement = Movement.builder()
                            .article(chicken)
                            .date(LocalDateTime.now())
                            .type(MovementType.SYSTEM)
                            .units(hatchedUnits)
                            .amount(chicken.getPrice() * hatchedUnits)
                            .user(this.systemUser) // Use initialized system user
                            .build();
                    movementRepository.save(movement);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    private boolean checkStockLimitCanAdd(Article article, int quantityChange) { // Renamed and clarified quantity
        Long articleCategoryId = article.getCategory().getId();
        if (articleCategoryId.equals(eggsCategory.getId())) {
            int currentEggs = articleRepository.findTotalUnitsByCategory(eggsCategory);
            return currentEggs + quantityChange <= maxEggs; // Use injected value
        } else if (articleCategoryId.equals(chickensCategory.getId())) {
            int currentChickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
            return currentChickens + quantityChange <= maxChickens; // Use injected value
        }
        return true; // No limit for other categories
    }

    private void performTransaction(Article article, int quantity, double transactionAmount, MovementType type, User user) {
        // Modifica stock
        int updatedUnits = type == MovementType.BUY
                ? article.getUnits() + quantity
                : article.getUnits() - quantity;

        article.setUnits(updatedUnits);

        Movement movement = Movement.createMovement(article, quantity, transactionAmount, user);
        movement.setType(type);

        articleRepository.save(article);
        // userRepository.save(user); // Removed: User is now managed and saved implicitly by JPA if changes occurred within the transaction.
        movementRepository.save(movement);
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) throw new FarmException("Quantity must be positive");
    }

    // ---------- AI Report ----------
    @Override
    public String generateAIReport(List<Movement> movements, User user) {
        int totalChickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
        int eggsProducedToday = 0, eggsSoldToday = 0;
        int currentEggStock = articleRepository.findTotalUnitsByCategory(eggsCategory);

        if (movements != null) {
            eggsProducedToday = movements.stream()
                    .filter(m -> m.getType() == MovementType.SYSTEM)
                    .filter(m -> m.getArticle().getCategory().getId().equals(eggsCategory.getId())) // Use ID
                    .filter(m -> isToday(m.getDate()))
                    .mapToInt(Movement::getUnits)
                    .sum();
            eggsSoldToday = movements.stream()
                    .filter(m -> m.getType() == MovementType.SALE)
                    .filter(m -> m.getArticle().getCategory().getId().equals(eggsCategory.getId())) // Use ID
                    .filter(m -> isToday(m.getDate()))
                    .mapToInt(Movement::getUnits)
                    .sum();
        }

        double userBalance = user != null ? user.getBalance() : 0.0;
        String userRole = user != null ? user.getRole() : "USER";

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
                userRole);

        return aiService.generateReport(prompt);
    }

    private boolean isToday(LocalDateTime date) {
        // Solo compara la fecha, ignora hora/min
        if (date == null) return false;
        LocalDate now = LocalDate.now();
        return date.toLocalDate().isEqual(now);
    }

    @Override
    @Transactional
    public void updateUserBalance(User authenticatedUser, double newBalance) {
        if (newBalance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative. Provided: " + newBalance);
        }
        User managedUser = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new FarmException("User not found with ID: " + authenticatedUser.getId() + " while trying to update balance."));

        managedUser.setBalance(newBalance);
        userRepository.save(managedUser); // Explicit save here as it's the primary purpose of the method
        logger.info("User {} balance updated to {}", managedUser.getUsername(), newBalance);
    }
}
