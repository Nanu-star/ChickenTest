package com.example.chickentest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

enum MovementType {
    BUY, SELL
}

@Entity
@Table(name = "movements")
@Data
@NoArgsConstructor
public class Movement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    private int quantity;
    private double price; // Price at the moment of the transaction
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private MovementType type; // BUY or SELL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    public Movement(Article article, int quantity, double price, MovementType type, User user, Farm farm) {
        this.article = article;
        this.quantity = quantity;
        this.price = price;
        this.date = LocalDateTime.now();
        this.type = type;
        this.user = user;
        this.farm = farm;
    }
}
