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
import org.mockito.MockitoAnnotations;
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

    @InjectMocks
    private FarmServiceImpl farmService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testuser").build();
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
}
