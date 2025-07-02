package com.chickentest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import com.chickentest.config.Constants;

import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.exception.InsufficientStockException;
import com.chickentest.exception.InsufficientBalanceException; // Added
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

// import java.util.Date; // No longer needed
import java.util.List;
import javax.annotation.PostConstruct;
// import java.time.ZoneId; // No longer needed
import java.time.LocalDateTime; // Added for LocalDateTime.now()

@Service
public class FarmServiceImpl implements FarmService {
    private static final Logger logger = LoggerFactory.getLogger(FarmServiceImpl.class);
    private static final String ERROR_PREFIX = "[FarmService] ";

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
    public FarmServiceImpl(
            ArticleRepository articleRepository,
            MovementRepository movementRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository, AiService aiService) {
        this.articleRepository = articleRepository;
        this.movementRepository = movementRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @PostConstruct
    private void initCategories() {
        chickensCategory = categoryRepository.findByName("CHICKEN");
        if (chickensCategory == null) {
            throw new FarmException(ERROR_PREFIX + "Required category 'CHICKEN' not found in database");
        }
        eggsCategory = categoryRepository.findByName("EGG");
        if (eggsCategory == null) {
            throw new FarmException(ERROR_PREFIX + "Required category 'EGG' not found in database");
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
        List<Movement> movements = movementRepository.findByUsername(user.getUsername());
        aiService.generateAIReport(movements, user);
        return movements;
    }

    

    
    @Override
    @Transactional
    public boolean buy(Long articleId, int quantity, User user) {
        validatePositiveQuantity(quantity);
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new FarmException("Article not found with id: " + articleId));
        double amount = article.getPrice() * quantity;

        if (!checkStockLimit(article, quantity)) {
            return false;
        }

        performTransaction(article, quantity, amount, MovementType.PURCHASE, user);
        return true;
    }

    @Override
    @Transactional
    public boolean sell(Long articleId, int quantity, User user) {
        validatePositiveQuantity(quantity);
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new InsufficientStockException("Article not found with ID: " + articleId));

        if (article.getUnits() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Available: " + article.getUnits() + ", requested: " + quantity);
        }

        double amount = article.getPrice() * quantity;

        if (!checkStockLimit(article, -quantity)) {
            throw new InsufficientStockException("Stock limit would be violated by this sale");
        }

        performTransaction(article, quantity, amount, MovementType.SALE, user);
        logger.info("Sale successful");
        return true;
    }

    @Override
    @Transactional
    public boolean addArticle(Article article, User user) {
        logger.info("addArticle called with article: {} and user: {}", article, user);
        logger.debug("Validating article category");
        validateArticleCategory(article);
        logger.debug("Checking stock limit for article: {}", article);
        if (!checkStockLimit(article, article.getUnits())) {
            throw new FarmException("Stock limit exceeded for this category");
        }

        // Ensure user is set on the article if it's a new one being added to their inventory
        // This might be redundant if 'article.user' is already set by the controller, but good for safety.
        if (article.getUser() == null) {
            article.setUser(user);
        }

        // Set creation date if it's a new article
        if (article.getCreation() == null) {
            article.setCreation(LocalDateTime.now());
        }

        // Calculate cost
        double cost = article.getPrice() * article.getUnits();

        // Perform the transaction, which now handles balance, article persistence, and movement creation
        performTransaction(article, article.getUnits(), cost, MovementType.PURCHASE, user);

        // The article is saved within performTransaction, so no need to save it here again
        // Balance is handled in performTransaction
        // Movement is created and saved in performTransaction

        return true;
    }


    @Override
    @Transactional
    public Article getArticle(Long id) {
        return articleRepository.findById(id)
            .orElseThrow(() -> new FarmException(ERROR_PREFIX + "Article not found with ID: " + id));
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
            .producedBatches(movementRepository.countProducedBatches(MovementType.PRODUCTION).intValue())
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

    private void incrementEggAges() {
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
                        .creation(LocalDateTime.now()) // Added creation timestamp for new chicken
                        .build();
                    articleRepository.save(chicken);

                    Movement movement = Movement.builder()
                        .article(chicken)
                        .date(LocalDateTime.now())
                        .type(MovementType.PRODUCTION)
                        .units(hatchedUnits)
                        .amount(chicken.getPrice() * hatchedUnits)
                        .username("system")
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
        // For PURCHASE: increase stock (add units). For SALE: decrease stock (subtract units)
        int updatedUnits = type == MovementType.PURCHASE ? article.getUnits() + quantity : article.getUnits() - quantity;

/*
        if (type == MovementType.PURCHASE) {
            if (user.getBalance() < transactionAmount) {
                throw new InsufficientBalanceException("Insufficient balance. Required: " + transactionAmount + ", Available: " + user.getBalance());
            }
            user.setBalance(user.getBalance() - transactionAmount);
        } else if (type == MovementType.SALE) {
            user.setBalance(user.getBalance() + transactionAmount);
        }
*/
        article.setUnits(updatedUnits); // Set units after potential exceptions for balance

        Movement movement = Movement.createMovement(article, quantity, transactionAmount, user.getUsername());
        movement.setType(type); // Ensure type is set correctly on the movement if createMovement defaults it

        articleRepository.save(article);
        userRepository.save(user);
        movementRepository.save(movement);
    }
    
    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new FarmException("Quantity must be positive");
        }
    }
    private void validateArticleCategory(Article article) {
        if (article.getCategory() == null) {
            throw new FarmException("Article category must be specified");
        }
    }

    // Helper to check if a Date is today - No longer used after Movement.date changed to LocalDateTime
    // private boolean isToday(Date date) {
    //     if (date == null) return false;
    //     LocalDate movementDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    //     return movementDate.equals(LocalDate.now());
    // }
}

