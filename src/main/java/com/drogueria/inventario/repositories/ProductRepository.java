package com.drogueria.inventario.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drogueria.inventario.models.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseOrBarcodeContainingIgnoreCase(String name, String barcode);
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
