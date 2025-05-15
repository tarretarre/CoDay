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

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public Product() {
    }

    public Product(String name, int pointsCost, String imageUrl, Company company) {
        this.name = name;
        this.pointsCost = pointsCost;
        this.imageUrl = imageUrl;
        this.company = company;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public Company getCompany() {
        return company;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
