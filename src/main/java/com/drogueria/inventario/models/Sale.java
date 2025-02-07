package com.drogueria.inventario.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // En Sale.java
    @JsonManagedReference // Agregar esta anotación
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();

    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password", "roles" }) // Agregar esta anotación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addDetail(SaleDetail detail) {
        detail.setSale(this); // Establece la relación bidireccional
        this.details.add(detail);
    }
}
