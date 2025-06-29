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

    @Autowired
    public FarmService(ArticleRepository articleRepository, MovementRepository movementRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.movementRepository = movementRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
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
    public boolean buy(Long articleId, int cantidad, User user) {
        try {
            Optional<Article> optionalArticle = articleRepository.findById(articleId);
            if (!optionalArticle.isPresent()) {
                throw new RuntimeException("Article not found with id: " + articleId);
            }
            Article article = optionalArticle.get();
            
            double monto = article.getPrice() * cantidad;
            
            if (monto > user.getBalance()) {
                return false;
            }
            
            if (!checkStockLimit(article, cantidad)) {
                return false;
            }
            
            // Update article units
            article.setUnits(article.getUnits() + cantidad);
            articleRepository.save(article);
            
            // Update user balance
            user.setBalance(user.getBalance() - monto);
            userRepository.save(user);
            
            // Create and save movement
            Movement movimiento = Movement.createMovement(article, monto, user.getUsername());
            movimiento.setType(MovementType.PURCHASE);
            movementRepository.save(movimiento);
            
            return true;
        } catch (Exception e) {
            throw new FarmException("Error during buy operation: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean sell(Long articleId, int cantidad, User user) {
        try {
            Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            if (article.getUnits() < cantidad) {
                return false;
            }
            
            double monto = article.getPrice() * cantidad;
            int nuevoStock = article.getUnits() - cantidad;
            
            article.setUnits(nuevoStock);
            double nuevoSaldo = user.getBalance() + monto;
            
            Movement movimiento = Movement.createMovement(article, monto, user.getUsername());
            movimiento.setType(MovementType.SALE);
            
            articleRepository.save(article);
            movementRepository.save(movimiento);
            user.setBalance(nuevoSaldo);
            userRepository.save(user);
            
            return true;
        } catch (Exception e) {
            throw new FarmException("Error during sell operation: " + e.getMessage(), e);
        }
    }

    public boolean addArticle(Article articulo) throws Exception {
        if (!checkStockLimit(articulo, articulo.getUnits())) {
            return false;
        }
        
        articleRepository.save(articulo);
        return true;
    }

    public Article getArticle(Long id) throws Exception {
        return articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article not found"));
    }

    public void updateArticle(Article articulo) throws Exception {
        articleRepository.save(articulo);
    }

    public void deleteArticle(Long id) throws Exception {
        articleRepository.deleteById(id);
    }

    public Report generateReport() {
        Report report = Report.builder()
            .date(LocalDate.now().toString())
            .totalEggs(articleRepository.findTotalUnitsByCategory(categoryRepository.findByDisplayName("Eggs")))
            .totalChickens(articleRepository.findTotalUnitsByCategory(categoryRepository.findByDisplayName("Chickens")))
            .producedBatches(movementRepository.countProducedBatches(MovementType.PRODUCTION).intValue())
            .totalSales(movementRepository.calculateTotalSales(MovementType.SALE))
            .build();
        return report;
    }
    private boolean checkStockLimit(Article article, int cantidad) throws Exception {
        int chickens = articleRepository.findTotalUnitsByCategory(categoryRepository.findByDisplayName("Chickens"));
        int eggs = articleRepository.findTotalUnitsByCategory(categoryRepository.findByDisplayName("Eggs"));

        if (article.getCategory().equals(categoryRepository.findByDisplayName("Eggs")) && article.getUnits() + eggs > 2000) {
            return false;
        }
        if (article.getCategory().equals(categoryRepository.findByDisplayName("Chickens")) && article.getUnits() + chickens > 1500) {
            return false;
        }
        return true;
    }

    private Article generateDailyBatch(int CHICKEN) {
        int huevos = CHICKEN * 3;
        Article article = new Article();
        article.setCategory(categoryRepository.findByDisplayName("Eggs"));
        article.setUnits(huevos);
        article.setProduction("chickentest");
        article.setPrice(1.5);
        article.setAge(0);
        return article;
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
