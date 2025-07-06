package com.chickentest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Long id;
    private String name;
    private int units;
    private double price;
    private int age;
    private String production;
    private Long categoryId;
    private String categoryName;
}
