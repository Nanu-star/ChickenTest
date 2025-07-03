package com.chickentest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
@Entity
@Table(name = "movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;
    
    @Column(nullable = false)
    private Integer units;
    
    @Column(nullable = false)
    private double amount;
    
    @JsonBackReference
    @ManyToOne
    private User user;

    public static Movement createMovement(Article article, int units, double amount, User user) {
        return Movement.builder()
                .article(article)
                .units(units)
                .amount(amount)
                .user(user)
                .date(LocalDateTime.now())
                .type(MovementType.BUY)
                .build();
    }

    public static Movement createMovement(Article article, LocalDateTime date, MovementType type, int units, double amount, User user) {
        return Movement.builder()
                .article(article)
                .date(date)
                .type(type)
                .units(units)
                .amount(amount)
                .user(user)
                .build();
    }

    public static Movement createMovement(Article article, LocalDateTime date, String type, int units, double amount, User user) {
        return Movement.builder()
                .article(article)
                .date(date)
                .type(MovementType.fromDescription(type))
                .units(units)
                .amount(amount)
                .user(user)
                .build();
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
