package com.chickentest.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

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
    private Date date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;
    
    @Column(nullable = false)
    private double amount;
    
    @Column(nullable = false)
    private String username;

    public static Movement createMovement(Article article, double amount, String username) {
        return Movement.builder()
                .article(article)
                .amount(amount)
                .username(username)
                .date(new Date())
                .type(MovementType.PURCHASE)
                .build();
    }

    public static Movement createMovement(Article article, Date date, MovementType type, double amount, String username) {
        return Movement.builder()
                .article(article)
                .date(date)
                .type(type)
                .amount(amount)
                .username(username)
                .build();
    }

    public static Movement createMovement(Article article, Date date, String type, double amount, String username) {
        return Movement.builder()
                .article(article)
                .date(date)
                .type(MovementType.fromDescription(type))
                .amount(amount)
                .username(username)
                .build();
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
