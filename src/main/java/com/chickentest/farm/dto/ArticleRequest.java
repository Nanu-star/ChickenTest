package com.chickentest.farm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ArticleRequest {
  @NotBlank private String name;

  @Min(0)
  private int units;

  @Positive private double price;

  @Min(0)
  private int age;

  @NotNull private Long categoryId;
  private String production;
}
