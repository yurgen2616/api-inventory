package com.drogueria.inventario.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.drogueria.inventario.models.Product;
import com.drogueria.inventario.services.ProductService;

import jakarta.validation.Valid;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@Valid @PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.searchProducts(""));
    }

    @PutMapping("/{id}/add-stock")
    public ResponseEntity<Product> addStock(
            @PathVariable Long id,
            @RequestParam int quantity,
            @RequestParam double unitPrice, 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {
        productService.addStock(id, quantity, unitPrice,expirationDate);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/export")
public ResponseEntity<byte[]> exportProductsReport() {
    try {
        // Create a temporary file
        Path tempFile = Files.createTempFile("products_report_", ".xlsx");
        
        // Export the report
        productService.exportProductsReport(tempFile.toString());
        
        // Read the file content
        byte[] content = Files.readAllBytes(tempFile);
        
        // Delete the temporary file
        Files.delete(tempFile);
        
        // Prepare HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("products_report.xlsx").build());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(content);
    } catch (IOException e) {
        throw new RuntimeException("Error exporting products report", e);
    }
}


    @GetMapping("/expiration-warnings")
    public ResponseEntity<List<Map<String, Object>>> getExpirationWarnings(
        @RequestParam(defaultValue = "3") int monthsThreshold) {
        List<Map<String, Object>> warnings = productService.getExpirationWarnings(monthsThreshold);
        return ResponseEntity.ok(warnings);
    }


}
