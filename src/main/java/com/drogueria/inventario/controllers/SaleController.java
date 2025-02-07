package com.drogueria.inventario.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.drogueria.inventario.models.Sale;
import com.drogueria.inventario.models.SaleDetail;
import com.drogueria.inventario.services.SaleService;
import com.drogueria.inventario.services.UserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/sales")
public class SaleController {
    @Autowired
    private SaleService saleService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody Map<String, List<SaleDetail>> saleData) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Como estás usando JpaUserDetailsService, el username puede ser email o nombre de usuario
        // El servicio ya maneja ambos casos
        Long userId = userService.getUserByUsername(username).getId();

        List<SaleDetail> details = saleData.get("details");
        return ResponseEntity.ok(saleService.createSale(details,userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Sale>> findSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Sale> sales = saleService.findSalesByDateRange(start, end);
        // Forzar la carga del usuario si es necesario
        sales.forEach(sale -> sale.getUser().getName()); // Esto fuerza la carga del usuario
        return ResponseEntity.ok(sales);
    }

    @PostMapping("/{saleId}/return-entire")
    public ResponseEntity<Void> returnEntireSale(@PathVariable Long saleId) {
        saleService.returnEntireSale(saleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{saleId}/return")
    public ResponseEntity<Boolean> processReturn(
            @PathVariable Long saleId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        boolean saleDeleted = saleService.processReturn(saleId, productId, quantity);
        return ResponseEntity.ok(saleDeleted);
    }

@GetMapping("/report")
public ResponseEntity<byte[]> exportSalesReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
    try {
        // Create a temporary file
        Path tempFile = Files.createTempFile("Reporte_Ventas_", ".xlsx");
        
        // Export the report
        saleService.exportSalesReport(start, end, tempFile.toString());
        
        // Read the file content
        byte[] content = Files.readAllBytes(tempFile);
        
        // Delete the temporary file
        Files.delete(tempFile);
        
        // Prepare HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("Reporte_Ventas.xlsx").build());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(content);
    } catch (IOException e) {
        throw new RuntimeException("Error exporting sales report", e);
    }
}
}
