package com.drogueria.inventario.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.drogueria.inventario.models.Product;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    Product getProductById(Long id);
    List<Product> searchProducts(String query);
    void addStock(Long productId, int quantity, double unitPrice, double unitSalePrice, LocalDate expirationDate);
    void exportProductsReport(String filePath);
        List<Map<String, Object>> getExpirationWarnings(int monthsThreshold);
}
