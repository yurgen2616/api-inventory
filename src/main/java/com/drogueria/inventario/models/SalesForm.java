package com.drogueria.inventario.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sales_forms")
public class SalesForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
