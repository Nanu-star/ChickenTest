package com.chickentest.user;

import com.chickentest.config.AppSettings;
import com.chickentest.farm.domain.Article;
import com.chickentest.farm.domain.Category;
import com.chickentest.farm.domain.Movement;
import com.chickentest.farm.domain.MovementType;
import com.chickentest.farm.repository.ArticleRepository;
import com.chickentest.farm.repository.CategoryRepository;
import com.chickentest.farm.repository.MovementRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario admin de la granja (credenciales en app.security.admin.*) y, si no existen, sus
 * artículos de muestra con su movimiento de creación ({@link MovementType#SYSTEM}). Es idempotente:
 * se puede arrancar la aplicación varias veces sin duplicar nada. Si cambia la contraseña en la
 * configuración, se actualiza al arrancar.
 */
@Component
public class SystemUserInitializer {

  static final double SAMPLE_BALANCE = 100000.0;

  static final String SAMPLE_EGGS_NAME = "Huevos sample admin";
  static final int SAMPLE_EGGS_UNITS = 300;
  static final double SAMPLE_EGGS_PRICE = 25.0;

  static final String SAMPLE_CHICKENS_NAME = "Gallinas sample admin";
  static final int SAMPLE_CHICKENS_UNITS = 50;
  static final double SAMPLE_CHICKENS_PRICE = 200.0;

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final ArticleRepository articleRepository;
  private final CategoryRepository categoryRepository;
  private final MovementRepository movementRepository;
  private final AppSettings appSettings;

  public SystemUserInitializer(
      UserRepository userRepository,
      BCryptPasswordEncoder passwordEncoder,
      ArticleRepository articleRepository,
      CategoryRepository categoryRepository,
      MovementRepository movementRepository,
      AppSettings appSettings) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.articleRepository = articleRepository;
    this.categoryRepository = categoryRepository;
    this.movementRepository = movementRepository;
    this.appSettings = appSettings;
  }

  @PostConstruct
  public void createSystemUser() {
    if (appSettings.getSecurity() == null || appSettings.getSecurity().admin() == null) {
      throw new IllegalStateException(
          "Falta la configuración app.security.admin.username / app.security.admin.password");
    }
    AppSettings.Admin admin = appSettings.getSecurity().admin();

    User systemUser =
        userRepository
            .findByUsername(admin.username())
            .orElseGet(
                () -> {
                  User user = new User();
                  user.setUsername(admin.username());
                  user.setPassword(passwordEncoder.encode(admin.password()));
                  user.setBalance(0);
                  user.setRole("SYSTEM");
                  return userRepository.save(user);
                });

    if (!passwordEncoder.matches(admin.password(), systemUser.getPassword())) {
      systemUser.setPassword(passwordEncoder.encode(admin.password()));
      userRepository.save(systemUser);
    }

    seedSampleFarm(systemUser);
  }

  void seedSampleFarm(User admin) {
    if (admin.getBalance() == 0) {
      admin.setBalance(SAMPLE_BALANCE);
      userRepository.save(admin);
    }

    Category eggs = categoryRepository.findByName("EGG");
    Category chickens = categoryRepository.findByName("CHICKEN");

    seedArticle(admin, SAMPLE_EGGS_NAME, eggs, SAMPLE_EGGS_UNITS, SAMPLE_EGGS_PRICE);
    seedArticle(
        admin, SAMPLE_CHICKENS_NAME, chickens, SAMPLE_CHICKENS_UNITS, SAMPLE_CHICKENS_PRICE);
  }

  /**
   * Crea el artículo de muestra si no existe y, si no tiene movimiento de creación, lo registra.
   * Así también se completan las muestras de bases de datos creadas antes de añadir movimientos.
   */
  private void seedArticle(User owner, String name, Category category, int units, double price) {
    Article article =
        articleRepository.findAllByUserId(owner.getId()).stream()
            .filter(existing -> name.equals(existing.getName()))
            .findFirst()
            .orElseGet(
                () ->
                    articleRepository.save(
                        Article.builder()
                            .name(name)
                            .category(category)
                            .user(owner)
                            .units(units)
                            .price(price)
                            .age(0)
                            .production("sample")
                            .build()));

    if (!movementRepository.existsByArticleIdAndType(article.getId(), MovementType.SYSTEM)) {
      movementRepository.save(
          Movement.createMovement(
              article, LocalDateTime.now(), MovementType.SYSTEM, units, units * price, owner));
    }
  }
}
