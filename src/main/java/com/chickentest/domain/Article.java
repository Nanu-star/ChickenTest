package com.chickentest.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int units;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private double price;
    
    @Column(nullable = false)
    private int age;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    
    
    @Column(nullable = false)
    private String production;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movement> movements;
    private Date creation;
    private String displayPrice;

    public Article(int units, String name, double price, Category category, String production, Date creation) {
        this.units = units;
        this.name = name;
        this.price = price;
        this.category = category;
        this.production = production;
        this.creation = creation;
    }

    public Article(Long id, int units, String name, double price, Category category, String production, Date creation) {
        this.id = id;
        this.units = units;
        this.name = name;
        this.price = price;
        this.category = category;
        this.production = production;
        this.creation = creation;
    }

    public Article(Long id, int units, String name, double price, Category category, int age, String production, Date creation) {
        this.id = id;
        this.units = units;
        this.name = name;
        this.price = price;
        this.category = category;
        this.age = age;
        this.production = production;
        this.creation = creation;
    }

    public Article(int units, String name, double price, Category category, int age, String production, Date creation, String displayPrice) {
        this.units = units;
        this.name = name;
        this.price = price;
        this.category = category;
        this.age = age;
        this.production = production;
        this.creation = creation;
        this.displayPrice = displayPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public String getDisplayPrice() {
        return displayPrice;
    }

    public void setDisplayPrice(String displayPrice) {
        this.displayPrice = displayPrice;
    }
}
