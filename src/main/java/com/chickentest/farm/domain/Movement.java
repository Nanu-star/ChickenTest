package com.chickentest.farm.domain;

import com.chickentest.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  @JsonBackReference("article-movements")
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

  @JsonBackReference @ManyToOne private User user;

  public static Movement createMovement(
      Article article, LocalDateTime date, MovementType type, int units, double amount, User user) {
    return Movement.builder()
        .article(article)
        .date(date)
        .type(type)
        .units(units)
        .amount(amount)
        .user(user)
        .build();
  }
}
