package com.chickentest.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.chickentest.config.AppSettings;
import com.chickentest.farm.domain.Article;
import com.chickentest.farm.domain.Category;
import com.chickentest.farm.domain.Movement;
import com.chickentest.farm.domain.MovementType;
import com.chickentest.farm.repository.ArticleRepository;
import com.chickentest.farm.repository.CategoryRepository;
import com.chickentest.farm.repository.MovementRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SystemUserInitializerTest {

  private UserRepository userRepository;
  private ArticleRepository articleRepository;
  private CategoryRepository categoryRepository;
  private MovementRepository movementRepository;
  private SystemUserInitializer initializer;
  private BCryptPasswordEncoder encoder;
  private Category eggs;
  private Category chickens;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    articleRepository = mock(ArticleRepository.class);
    categoryRepository = mock(CategoryRepository.class);
    movementRepository = mock(MovementRepository.class);
    encoder = new BCryptPasswordEncoder();
    eggs = mock(Category.class);
    chickens = mock(Category.class);

    when(categoryRepository.findByName("EGG")).thenReturn(eggs);
    when(categoryRepository.findByName("CHICKEN")).thenReturn(chickens);

    AppSettings settings = new AppSettings();
    settings.setSecurity(new AppSettings.Security(new AppSettings.Admin("system", "admin")));

    initializer =
        new SystemUserInitializer(
            userRepository,
            encoder,
            articleRepository,
            categoryRepository,
            movementRepository,
            settings);
  }

  @Test
  void firstStart_createsSystemUser_withSampleBalance_articles_andCreationMovements() {
    when(userRepository.findByUsername("system")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });
    when(articleRepository.findAllByUserId(1L)).thenReturn(List.of());
    when(articleRepository.save(any(Article.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    initializer.createSystemUser();

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, atLeastOnce()).save(userCaptor.capture());
    User savedAdmin = userCaptor.getValue();
    assertEquals("system", savedAdmin.getUsername());
    assertTrue(encoder.matches("admin", savedAdmin.getPassword()));
    assertEquals(SystemUserInitializer.SAMPLE_BALANCE, savedAdmin.getBalance());

    ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository, times(2)).save(articleCaptor.capture());
    List<Article> articles = articleCaptor.getAllValues();

    Article sampleEggs = articles.get(0);
    assertEquals(SystemUserInitializer.SAMPLE_EGGS_NAME, sampleEggs.getName());
    assertEquals(SystemUserInitializer.SAMPLE_EGGS_UNITS, sampleEggs.getUnits());
    assertEquals(SystemUserInitializer.SAMPLE_EGGS_PRICE, sampleEggs.getPrice());
    assertSame(eggs, sampleEggs.getCategory());
    assertEquals("sample", sampleEggs.getProduction());
    assertSame(savedAdmin, sampleEggs.getUser());

    Article sampleChickens = articles.get(1);
    assertEquals(SystemUserInitializer.SAMPLE_CHICKENS_NAME, sampleChickens.getName());
    assertEquals(SystemUserInitializer.SAMPLE_CHICKENS_UNITS, sampleChickens.getUnits());
    assertEquals(SystemUserInitializer.SAMPLE_CHICKENS_PRICE, sampleChickens.getPrice());
    assertSame(chickens, sampleChickens.getCategory());

    ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
    verify(movementRepository, times(2)).save(movementCaptor.capture());
    List<Movement> movements = movementCaptor.getAllValues();

    Movement eggsMovement = movements.get(0);
    assertEquals(MovementType.SYSTEM, eggsMovement.getType());
    assertEquals(SystemUserInitializer.SAMPLE_EGGS_UNITS, eggsMovement.getUnits());
    assertEquals(
        SystemUserInitializer.SAMPLE_EGGS_UNITS * SystemUserInitializer.SAMPLE_EGGS_PRICE,
        eggsMovement.getAmount());
    assertSame(sampleEggs, eggsMovement.getArticle());
    assertSame(savedAdmin, eggsMovement.getUser());

    Movement chickensMovement = movements.get(1);
    assertEquals(MovementType.SYSTEM, chickensMovement.getType());
    assertEquals(SystemUserInitializer.SAMPLE_CHICKENS_UNITS, chickensMovement.getUnits());
    assertSame(sampleChickens, chickensMovement.getArticle());
  }

  @Test
  void restart_withSampleDataAndMovements_doesNotDuplicateAnything() {
    User existing = new User();
    existing.setId(1L);
    existing.setUsername("system");
    existing.setBalance(SystemUserInitializer.SAMPLE_BALANCE);
    existing.setPassword(encoder.encode("admin"));
    when(userRepository.findByUsername("system")).thenReturn(Optional.of(existing));

    Article sampleEggs = Article.builder().name(SystemUserInitializer.SAMPLE_EGGS_NAME).build();
    Article sampleChickens =
        Article.builder().name(SystemUserInitializer.SAMPLE_CHICKENS_NAME).build();
    when(articleRepository.findAllByUserId(1L)).thenReturn(List.of(sampleEggs, sampleChickens));
    when(movementRepository.existsByArticleIdAndType(any(), eq(MovementType.SYSTEM)))
        .thenReturn(true);

    initializer.createSystemUser();

    verify(articleRepository, never()).save(any(Article.class));
    verify(movementRepository, never()).save(any(Movement.class));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void existingSampleArticleWithoutCreationMovement_getsOne_withoutNewArticle() {
    User existing = new User();
    existing.setId(1L);
    existing.setUsername("system");
    existing.setBalance(SystemUserInitializer.SAMPLE_BALANCE);
    existing.setPassword(encoder.encode("admin"));
    when(userRepository.findByUsername("system")).thenReturn(Optional.of(existing));

    Article sampleEggs = Article.builder().name(SystemUserInitializer.SAMPLE_EGGS_NAME).build();
    Article sampleChickens =
        Article.builder().name(SystemUserInitializer.SAMPLE_CHICKENS_NAME).build();
    when(articleRepository.findAllByUserId(1L)).thenReturn(List.of(sampleEggs, sampleChickens));
    when(movementRepository.existsByArticleIdAndType(any(), eq(MovementType.SYSTEM)))
        .thenReturn(false);

    initializer.createSystemUser();

    verify(articleRepository, never()).save(any(Article.class));
    verify(movementRepository, times(2)).save(any(Movement.class));
  }
}
