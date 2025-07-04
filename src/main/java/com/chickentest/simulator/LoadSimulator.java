package com.chickentest.simulator;

import com.chickentest.domain.*;
import com.chickentest.repository.UserRepository;
import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoadSimulator implements CommandLineRunner {

    @Autowired
    private FarmService farmService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🐣 Iniciando simulación ChickenTest...");

        User systemUser = createOrGetUser("system");
        initializeSystemStock(systemUser);

        for (int i = 1; i <= 5; i++) {
            String username = "user" + i;
            User user = createOrGetUser(username);
            farmService.updateUserBalance(user, 5000.0);

            List<Category> categories = farmService.getCategories();
            Category chickenCat = categories.stream().filter(c -> c.getName().equals("CHICKEN")).findFirst().orElseThrow();
            Category eggCat = categories.stream().filter(c -> c.getName().equals("EGG")).findFirst().orElseThrow();

            // Artículos iniciales vacíos (cosméticos, ya que stock se guarda en los originales)
            farmService.addArticle(Article.builder()
                    .name("Gallinas " + username)
                    .category(chickenCat)
                    .price(250.0)
                    .units(0)
                    .build(), user);

            farmService.addArticle(Article.builder()
                    .name("Huevos " + username)
                    .category(eggCat)
                    .price(30.0)
                    .units(0)
                    .build(), user);

            // Compran al sistema
            buyFromSystemStock(eggCat.getId(), 10, user);
            buyFromSystemStock(chickenCat.getId(), 2, user);

            // Venta realista: buscar el artículo que *sí tiene stock* y vender desde ahí
            List<Article> inventory = farmService.loadInventory(user);
            inventory.stream()
                    .filter(a -> a.getCategory().getId().equals(eggCat.getId()))
                    .filter(a -> a.getUnits() >= 5)
                    .findFirst()
                    .ifPresentOrElse(articleConHuevos -> {
                        try {
                            farmService.sell(articleConHuevos.getId(), 5, user);
                            System.out.println("💸 " + user.getUsername() + " vendió 5 huevos.");
                        } catch (Exception e) {
                            System.out.println("❌ Venta fallida para " + user.getUsername() + ": " + e.getMessage());
                        }
                    }, () -> {
                        System.out.println("⚠️ " + user.getUsername() + " no tiene suficientes huevos para vender.");
                    });
        }

        System.out.println("✅ Simulación ChickenTest completada.");
    }

    private User createOrGetUser(String username) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setPassword("hashed");
                    u.setBalance(0.0);
                    return userRepository.save(u);
                });
    }

    private void initializeSystemStock(User systemUser) {
        List<Category> categories = farmService.getCategories();
        Category chickenCat = categories.stream().filter(c -> c.getName().equals("CHICKEN")).findFirst().orElseThrow();
        Category eggCat = categories.stream().filter(c -> c.getName().equals("EGG")).findFirst().orElseThrow();

        List<Article> existing = farmService.loadInventory(systemUser);

        boolean hasEggs = existing.stream().anyMatch(a -> a.getCategory().equals(eggCat) && a.getUnits() > 0);
        boolean hasChickens = existing.stream().anyMatch(a -> a.getCategory().equals(chickenCat) && a.getUnits() > 0);

        if (!hasEggs) {
            farmService.addArticle(Article.builder()
                    .name("Stock inicial huevos")
                    .category(eggCat)
                    .units(500)
                    .price(25.0)
                    .build(), systemUser);
            System.out.println("🥚 Stock inicial de huevos creado");
        }

        if (!hasChickens) {
            farmService.addArticle(Article.builder()
                    .name("Stock inicial gallinas")
                    .category(chickenCat)
                    .units(100)
                    .price(200.0)
                    .build(), systemUser);
            System.out.println("🐔 Stock inicial de gallinas creado");
        }
    }

    private void buyFromSystemStock(Long categoryId, int quantity, User buyer) {
        List<Article> all = farmService.loadInventory(buyer);
        Article articleToBuy = all.stream()
                .filter(a -> a.getCategory().getId().equals(categoryId))
                .filter(a -> a.getUser().getUsername().equals("system"))
                .filter(a -> a.getUnits() >= quantity)
                .findFirst()
                .orElse(null);

        if (articleToBuy != null) {
            try {
                farmService.buy(articleToBuy.getId(), quantity, buyer);
                System.out.println("🛒 " + buyer.getUsername() + " compró " + quantity + " de " + articleToBuy.getName());
            } catch (Exception e) {
                System.out.println("❌ Compra fallida para " + buyer.getUsername() + ": " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No hay stock del sistema para categoría ID " + categoryId + " (" + buyer.getUsername() + ")");
        }
    }
}
