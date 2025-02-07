package com.drogueria.inventario.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double purchasePrice;

    @Column(nullable = false)
    private Double salePrice;

    @Column(nullable = false)
    private Double wholesalePrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer minimumStock;

    @Column(nullable = false)
    private String location;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "distributor_id", nullable = false)
    private Distributor distributor;

    @ManyToOne
    @JoinColumn(name = "sales_form_id", nullable = false)
    private SalesForm salesForm;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductStock> stocks = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStockAndPrice(int newQuantity, double newUnitPrice) {
        if (newQuantity <= 0 || newUnitPrice <= 0) {
            throw new IllegalArgumentException("Quantity and unit price must be greater than 0.");
        }

        int totalQuantity = this.stock + newQuantity;
        double totalCost = (this.stock * this.purchasePrice) + (newQuantity * newUnitPrice);
        double updatedPrice = totalCost / totalQuantity;

        this.stock = totalQuantity;
        this.purchasePrice = updatedPrice;
    }

}
