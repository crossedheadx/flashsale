package com.example.flashsale.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer stock;

    @Version //optimistic locking
    private Long version;

    public Product() {}

    public Product(String name, Integer stock) {
        this.name = name;
        this.stock = stock;
    }

    public void decreaseStock(Integer quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException(
                "Insufficient Stock for product: " + this.name
            );
        }
        this.stock -= quantity;
    }

    // GETTER
    public String getName() {
        return this.name;
    }

    public Integer getStock() {
        return this.stock;
    }

    public Long getId() {
        return this.id;
    }

    // SETTER
    public void setName(String newName) {
        this.name = newName;
    }

    public void setStock(Integer newStock) {
        if (newStock < 0) {
            throw new IllegalStateException(
                "Stock value passed is below 0: " + Integer.toString(newStock)
            );
        }
        this.stock = newStock;
    }
}
