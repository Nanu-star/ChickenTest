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

    private static final int MAX_EGGS = 2000;
    private static final int MAX_CHICKENS = 1500;
    private Category chickensCategory;
    private Category eggsCategory;

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
    void initCategories() { // Changed from private to package-private
        chickensCategory = categoryRepository.findByName("CHICKEN");
        eggsCategory = categoryRepository.findByName("EGG");
        if (chickensCategory == null || eggsCategory == null) {
            throw new FarmException("Required categories not found in database");
        }
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
    public boolean buy(Long articleId, int quantity, User user) {
        validatePositiveQuantity(quantity);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new FarmException("Article not found with id: " + articleId));
        double amount = article.getPrice() * quantity;

        if (!checkStockLimit(article, quantity)) return false;

        performTransaction(article, quantity, amount, MovementType.BUY, user);
        return true;
    }

    @Override
    @Transactional
    public boolean sell(Long articleId, int quantity, User user) {
        validatePositiveQuantity(quantity);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new InsufficientStockException("Article not found with ID: " + articleId));
        if (article.getUnits() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + article.getUnits() + ", requested: " + quantity);
        }
        double amount = article.getPrice() * quantity;

        if (!checkStockLimit(article, -quantity))
            throw new InsufficientStockException("Stock limit would be violated by this sale");

        performTransaction(article, quantity, amount, MovementType.SALE, user);
        logger.info("Sale successful");
        return true;
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
    public void hatchEggs() {
        incrementEggAges();
    }

    @Override
    @Transactional
    public void hatchEggsInternal() {
        incrementEggAges();
    }

    void incrementEggAges() { // Changed from private to package-private
        LocalDate today = LocalDate.now();
        List<Article> eggs = articleRepository.findByCategory(eggsCategory);
        for (Article egg : eggs) {
            if (egg.getLastAgedDate() == null || egg.getLastAgedDate().isBefore(today)) {
                egg.setAge(egg.getAge() + 1);
                egg.setLastAgedDate(today);
                if (egg.getAge() < Constants.EGG_HATCH_DAYS) {
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
                            .user(User.builder().username("system").build())
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

    private boolean checkStockLimit(Article article, int quantity) {
        Long articleCategoryId = article.getCategory().getId();
        if (articleCategoryId.equals(eggsCategory.getId())) {
            int currentEggs = articleRepository.findTotalUnitsByCategory(eggsCategory);
            return currentEggs + quantity <= MAX_EGGS;
        } else if (articleCategoryId.equals(chickensCategory.getId())) {
            int currentChickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
            return currentChickens + quantity <= MAX_CHICKENS;
        }
        return true;
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
        userRepository.save(user);
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
                    .filter(m -> m.getArticle().getCategory().getName().equalsIgnoreCase("EGG"))
                    .filter(m -> isToday(m.getDate()))
                    .mapToInt(Movement::getUnits)
                    .sum();
            eggsSoldToday = movements.stream()
                    .filter(m -> m.getType() == MovementType.SALE)
                    .filter(m -> m.getArticle().getCategory().getName().equalsIgnoreCase("EGG"))
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
}
