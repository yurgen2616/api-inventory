package com.drogueria.inventario.services;

import java.time.LocalDateTime;
import java.util.List;

import com.drogueria.inventario.models.Sale;
import com.drogueria.inventario.models.SaleDetail;


public interface SaleService {
    Sale createSale(List<SaleDetail> saleDetails, Long userId);
    List<Sale> findSalesByDateRange(LocalDateTime start, LocalDateTime end);
    boolean processReturn(Long saleId, Long productId, Integer quantity);
    void exportSalesReport(LocalDateTime start, LocalDateTime end, String filePath);
    void returnEntireSale(Long saleId);
    byte[] generateSaleReceipt(Long saleId);
}
