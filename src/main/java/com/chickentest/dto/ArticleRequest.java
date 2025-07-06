package com.chickentest.dto;

import lombok.Data;

@Data
public class ArticleRequest {
    private String name;
    private int units;
    private double price;
    private int age;
    private Long categoryId;
    private String production;
}
