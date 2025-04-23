package com.example.coday.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int pointsCost;

    public Product() {
    }

    public Product(String name, int pointsCost) {
        this.name = name;
        this.pointsCost = pointsCost;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPointsCost() {
        return pointsCost;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPointsCost(int pointsCost) {
        this.pointsCost = pointsCost;
    }
}
