package com.example.inventory_service.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "stock_items")
public class StockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    public StockEntity() {
        // default constructor for JPA
    }

    public StockEntity(String sku, String productName, Integer quantity) {
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
