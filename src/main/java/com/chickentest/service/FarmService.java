package com.chickentest.service;

import com.chickentest.config.Constants;
import com.chickentest.domain.Article;
import com.chickentest.domain.Category;
import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.Report;
import com.chickentest.domain.User;
import com.chickentest.exception.FarmException;
import com.chickentest.repository.ArticleRepository;
import com.chickentest.repository.CategoryRepository;
import com.chickentest.repository.MovementRepository;
import com.chickentest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FarmService {

    private final ArticleRepository articleRepository;
    private final MovementRepository movementRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final Category chickensCategory;
    private final Category eggsCategory;

    @Autowired
    public FarmService(ArticleRepository articleRepository,
                       MovementRepository movementRepository,
                       CategoryRepository categoryRepository,
                       UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.movementRepository = movementRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.chickensCategory = categoryRepository.findByDisplayName("Chickens");
        this.eggsCategory = categoryRepository.findByDisplayName("Eggs");
    }

    @Transactional(readOnly = true)
    public List<Article> loadInventory(User user) {
        List<Article> articles = articleRepository.findAll();
        boolean diario = false;
        
        for (Article article : articles) {
            if (article.getAge() == 0 && article.getProduction().toLowerCase().contains("chickentest")) {
                diario = true;
            }
        }
        
        Category chickensCategory = categoryRepository.findByDisplayName(Constants.Category.CHICKENS);
        int chickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
        if (!diario && chickens != 0) {
            Article dailyBatch = generateDailyBatch(chickens);
            articles.add(dailyBatch);
        }
        
        return articles;
    }
    
    @Transactional(readOnly = true)
    public List<Movement> getMovements(User user) {
        return movementRepository.findByUsername(user.getUsername());
    }

    @Transactional
    public boolean buy(Long articleId, int quantity, User user) {
        try {
            if (quantity <= 0) {
                throw new FarmException("Quantity must be positive");
            }
            Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new FarmException("Article not found with id: " + articleId));
            double amount = article.getPrice() * quantity;

            if (amount > user.getBalance()) {
                return false;
            }

            if (!checkStockLimit(article, quantity)) {
                return false;
            }

            performTransaction(article, quantity, amount, MovementType.PURCHASE, user);
            return true;
        } catch (Exception e) {
            throw new FarmException("Error during buy operation: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean sell(Long articleId, int quantity, User user) {
        try {
            if (quantity <= 0) {
                throw new FarmException("Quantity must be positive");
            }
            Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new FarmException("Article not found"));

            if (article.getUnits() < quantity) {
                return false;
            }

            double amount = article.getPrice() * quantity;

            performTransaction(article, quantity, amount, MovementType.SALE, user);
            return true;
        } catch (Exception e) {
            throw new FarmException("Error during sell operation: " + e.getMessage(), e);
        }
    }

    public boolean addArticle(Article article) throws Exception {
        if (!checkStockLimit(article, article.getUnits())) {
            return false;
        }

        articleRepository.save(article);
        return true;
    }

    public Article getArticle(Long id) throws Exception {
        return articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article not found"));
    }

    public void updateArticle(Article article) throws Exception {
        articleRepository.save(article);
    }

    public void deleteArticle(Long id) throws Exception {
        articleRepository.deleteById(id);
    }

    public Report generateReport() {
        Report report = Report.builder()
            .date(LocalDate.now().toString())
            .totalEggs(articleRepository.findTotalUnitsByCategory(eggsCategory))
            .totalChickens(articleRepository.findTotalUnitsByCategory(chickensCategory))
            .producedBatches(movementRepository.countProducedBatches(MovementType.PRODUCTION).intValue())
            .totalSales(movementRepository.calculateTotalSales(MovementType.SALE))
            .build();
        return report;
    }
    private boolean checkStockLimit(Article article, int quantity) {
        int chickens = articleRepository.findTotalUnitsByCategory(chickensCategory);
        int eggs = articleRepository.findTotalUnitsByCategory(eggsCategory);

        if (article.getCategory().equals(eggsCategory) && eggs + quantity > 2000) {
            return false;
        }
        if (article.getCategory().equals(chickensCategory) && chickens + quantity > 1500) {
            return false;
        }
        return true;
    }

    private Article generateDailyBatch(int CHICKEN) {
        int huevos = CHICKEN * 3;
        Article article = new Article();
        article.setCategory(eggsCategory);
        article.setUnits(huevos);
        article.setProduction("chickentest");
        article.setPrice(1.5);
        article.setAge(0);
        return article;
    }

    private void performTransaction(Article article, int quantity, double amount, MovementType type, User user) {
        int updatedUnits = type == MovementType.PURCHASE ? article.getUnits() + quantity : article.getUnits() - quantity;
        article.setUnits(updatedUnits);

        double updatedBalance = type == MovementType.PURCHASE ? user.getBalance() - amount : user.getBalance() + amount;
        user.setBalance(updatedBalance);

        Movement movement = Movement.createMovement(article, quantity, amount, user.getUsername());
        movement.setType(type);

        articleRepository.save(article);
        userRepository.save(user);
        movementRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
