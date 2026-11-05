package com.chickentest.farm.service;

import com.chickentest.farm.domain.Article;
import com.chickentest.farm.domain.Category;
import com.chickentest.farm.domain.Movement;
import com.chickentest.farm.domain.MovementType;
import com.chickentest.farm.domain.Report;
import com.chickentest.farm.exception.ArticleNotFoundException;
import com.chickentest.farm.exception.FarmException;
import com.chickentest.farm.exception.InsufficientBalanceException;
import com.chickentest.farm.exception.InsufficientStockException;
import com.chickentest.farm.exception.MaxStockExceededException;
import com.chickentest.farm.repository.ArticleRepository;
import com.chickentest.farm.repository.CategoryRepository;
import com.chickentest.farm.repository.MovementRepository;
import com.chickentest.user.User;
import com.chickentest.user.UserRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FarmServiceImpl implements FarmService {
  private static final Logger logger = LoggerFactory.getLogger(FarmServiceImpl.class);

  private final ArticleRepository articleRepository;
  private final MovementRepository movementRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final FarmMetricsService farmMetricsService;

  @Value("${farm.max-eggs:2000}")
  private int maxEggs;

  @Value("${farm.max-chickens:1500}")
  private int maxChickens;

  @Value("${farm.egg-hatch-days:3}")
  private int eggHatchDays;

  private Category chickensCategory;
  private Category eggsCategory;

  @Autowired
  public FarmServiceImpl(
      ArticleRepository articleRepository,
      MovementRepository movementRepository,
      CategoryRepository categoryRepository,
      UserRepository userRepository,
      FarmMetricsService farmMetricsService) {
    this.articleRepository = articleRepository;
    this.movementRepository = movementRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
    this.farmMetricsService = farmMetricsService;
  }

  @PostConstruct
  void initializeFarmData() {
    chickensCategory = categoryRepository.findByName("CHICKEN");
    eggsCategory = categoryRepository.findByName("EGG");
    if (chickensCategory == null || eggsCategory == null) {
      throw new FarmException(
          "Required categories (CHICKEN, EGG) not found in database. Please initialize them.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Article> loadInventory(User user) {
    return articleRepository.findAllByUserId(user.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Movement> getMovements(User user) {
    return movementRepository.findAllByUser(user);
  }

  @Override
  @Transactional
  public void buy(Long articleId, int quantity, User authenticatedUser) {
    validatePositiveQuantity(quantity);

    Article article =
        articleRepository
            .findByIdForUpdate(articleId) // Use locking
            .orElseThrow(
                () ->
                    new ArticleNotFoundException("Article to buy not found with id: " + articleId));

    if (!checkStockLimitCanAdd(article, quantity)) {
      throw new MaxStockExceededException(
          "Buying "
              + quantity
              + " units of "
              + article.getName()
              + " (ID: "
              + articleId
              + ") would exceed maximum stock limit for category "
              + article.getCategory().getName()
              + ".");
    }

    double amount = article.getPrice() * quantity;

    User managedUser = findManagedUser(authenticatedUser.getId());

    if (managedUser.getBalance() < amount) {
      throw new InsufficientBalanceException(
          "Insufficient balance. Current: "
              + managedUser.getBalance()
              + ", required: "
              + amount
              + " to buy "
              + quantity
              + " of "
              + article.getName());
    }
    managedUser.setBalance(managedUser.getBalance() - amount);
    // userRepository.save(managedUser); // JPA tracks changes to managedUser, will save on
    // transaction commit. Explicit save is optional.

    performTransaction(
        article, quantity, amount, MovementType.BUY, managedUser); // Pass managedUser
    logger.info(
        "User {} bought {} units of article {} (ID: {}) for {}",
        managedUser.getUsername(),
        quantity,
        article.getName(),
        articleId,
        amount);
    farmMetricsService.incrementBuy();
  }

  @Override
  @Transactional
  public void sell(Long articleId, int quantity, User authenticatedUser) {
    validatePositiveQuantity(quantity);

    Article article =
        articleRepository
            .findByIdForUpdate(articleId) // Use locking
            .orElseThrow(
                () ->
                    new ArticleNotFoundException(
                        "Article to sell not found with ID: " + articleId));

    if (article.getUnits() < quantity) {
      throw new InsufficientStockException(
          "Insufficient stock for "
              + article.getName()
              + " (ID: "
              + articleId
              + "). Available: "
              + article.getUnits()
              + ", requested: "
              + quantity);
    }

    double amount = article.getPrice() * quantity;

    User managedUser = findManagedUser(authenticatedUser.getId());

    managedUser.setBalance(managedUser.getBalance() + amount);
    // userRepository.save(managedUser); // JPA tracks changes, will save on transaction commit.

    performTransaction(
        article, quantity, amount, MovementType.SALE, managedUser); // Pass managedUser
    logger.info(
        "User {} sold {} units of article {} (ID: {}) for {}",
        managedUser.getUsername(),
        quantity,
        article.getName(),
        articleId,
        amount);
    farmMetricsService.incrementSale();
  }

  @Override
  @Transactional
  public Article addArticle(Article article, User user) {
    // Validar y setear categoría real desde DB
    if (article.getCategory() == null || article.getCategory().getId() == null) {
      throw new FarmException(
          "Category not found with ID: "
              + (article.getCategory() == null ? "null" : article.getCategory().getId()));
    }
    Category cat =
        categoryRepository
            .findById(article.getCategory().getId())
            .orElseThrow(
                () ->
                    new FarmException(
                        "Category not found with ID: " + article.getCategory().getId()));
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
    Article savedArticle = articleRepository.save(article);
    // Guardar article (si tenés cascade en movements, se guardan juntos)
    farmMetricsService.incrementAddArticle();
    return savedArticle;
  }

  @Override
  @Transactional
  public Article getArticle(Long id) {
    return articleRepository
        .findById(id)
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

  private void incrementEggAges() {
    LocalDate today = LocalDate.now();
    List<Article> eggs = articleRepository.findByCategory(eggsCategory);
    User systemUser = null;
    for (Article egg : eggs) {
      if (egg.getLastAgedDate() == null || egg.getLastAgedDate().isBefore(today)) {
        egg.setAge(egg.getAge() + 1);
        egg.setLastAgedDate(today);
        if (egg.getAge() < eggHatchDays) {
          articleRepository.save(egg);
        } else {
          int hatchedUnits = egg.getUnits();
          egg.setUnits(0);
          egg.setAge(0);
          articleRepository.save(egg);
          farmMetricsService.countHatchedEggs(hatchedUnits);
          farmMetricsService.countHatchEvent();
          Article chicken =
              Article.builder()
                  .category(chickensCategory)
                  .name(egg.getName())
                  .age(0)
                  .units(hatchedUnits)
                  .price(egg.getPrice())
                  .createdAt(LocalDateTime.now())
                  .build();
          articleRepository.save(chicken);
          if (systemUser == null) {
            systemUser = findSystemUser();
          }

          Movement movement =
              Movement.builder()
                  .article(chicken)
                  .date(LocalDateTime.now())
                  .type(MovementType.SYSTEM)
                  .units(hatchedUnits)
                  .amount(chicken.getPrice() * hatchedUnits)
                  .user(systemUser)
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

  private boolean checkStockLimitCanAdd(Article article, int quantityChange) {
    Long articleCategoryId = article.getCategory().getId();
    if (articleCategoryId.equals(eggsCategory.getId())) {
      int currentEggs = articleRepository.findTotalUnitsByCategory(eggsCategory);
      return currentEggs + quantityChange <= maxEggs;
    } else if (articleCategoryId.equals(chickensCategory.getId())) {
      int currentChickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
      return currentChickens + quantityChange <= maxChickens;
    }
    return true; // No limit for other categories
  }

  private void performTransaction(
      Article article, int quantity, double transactionAmount, MovementType type, User user) {
    // Modifica stock
    int updatedUnits =
        type == MovementType.BUY ? article.getUnits() + quantity : article.getUnits() - quantity;

    article.setUnits(updatedUnits);

    if (user == null)
      throw new IllegalArgumentException("User must not be null when recording BUY/SALE movements");
    Movement movement =
        Movement.createMovement(
            article, LocalDateTime.now(), type, quantity, transactionAmount, user);

    articleRepository.save(article);
    movementRepository.save(movement);
  }

  private void validatePositiveQuantity(int quantity) {
    if (quantity <= 0) throw new FarmException("Quantity must be positive");
  }

  private User findManagedUser(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new FarmException("User not found with ID: " + id));
  }

  private User findSystemUser() {
    return userRepository
        .findByUsername("system")
        .orElseThrow(() -> new FarmException("No existe el usuario 'system' en la base."));
  }

  @Override
  @Transactional
  public void updateUserBalance(User authenticatedUser, double newBalance) {
    if (newBalance < 0) {
      throw new IllegalArgumentException("Balance cannot be negative. Provided: " + newBalance);
    }
    User managedUser = findManagedUser(authenticatedUser.getId());

    managedUser.setBalance(newBalance);
    userRepository.save(
        managedUser); // Explicit save here as it's the primary purpose of the method
    logger.info("User {} balance updated to {}", managedUser.getUsername(), newBalance);
  }
}
