package com.drogueria.inventario.services.impl;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drogueria.inventario.models.Product;
import com.drogueria.inventario.models.ProductStock;
import com.drogueria.inventario.models.Sale;
import com.drogueria.inventario.models.SaleDetail;
import com.drogueria.inventario.models.User;
import com.drogueria.inventario.repositories.ProductRepository;
import com.drogueria.inventario.repositories.SaleRepository;
import com.drogueria.inventario.repositories.UserRepository;
import com.drogueria.inventario.services.SaleService;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.ss.usermodel.Row;

@Service
public class SaleServiceImpl implements SaleService {
    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public Sale createSale(List<SaleDetail> saleDetails, Long userId) {
        Sale sale = new Sale();
        double total = 0.0;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        sale.setUser(user);

        for (SaleDetail detail : saleDetails) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Calcular el stock total disponible (stock base + lotes)
            int stockFromLots = product.getStocks().stream()
                    .mapToInt(ProductStock::getQuantity)
                    .sum();
            int totalAvailableStock = product.getStock() + stockFromLots;

            if (totalAvailableStock < detail.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            int remainingQuantity = detail.getQuantity();

            // Primero intentar usar el stock de los lotes
            if (!product.getStocks().isEmpty()) {
                List<ProductStock> sortedStocks = product.getStocks().stream()
                        .sorted(Comparator.comparing(ProductStock::getExpirationDate))
                        .collect(Collectors.toList());

                List<ProductStock> stocksToRemove = new ArrayList<>();

                for (ProductStock stock : sortedStocks) {
                    if (remainingQuantity <= 0)
                        break;

                    int stockToUse = Math.min(stock.getQuantity(), remainingQuantity);
                    stock.setQuantity(stock.getQuantity() - stockToUse);
                    remainingQuantity -= stockToUse;

                    // Actualizar el stock base al descontar del lote
                    product.setStock(product.getStock() - stockToUse);

                    if (stock.getQuantity() == 0) {
                        stocksToRemove.add(stock);
                    }
                }

                product.getStocks().removeAll(stocksToRemove);
            }

            // Si aún queda cantidad por descontar, usar el stock base
            if (remainingQuantity > 0) {
                if (product.getStock() < remainingQuantity) {
                    throw new RuntimeException("Insufficient base stock for product: " + product.getName());
                }
                product.setStock(product.getStock() - remainingQuantity);
            }

            detail.setProduct(product);
            detail.setSale(sale);
            detail.setPurchasePriceAtSale(product.getPurchasePrice());
            detail.setSubtotal(detail.getQuantity() * detail.getPrice());
            total += detail.getSubtotal();

            productRepository.save(product);
            sale.addDetail(detail);
        }

        sale.setTotal(total);
        return saleRepository.save(sale);
    }

    @Override
    public List<Sale> findSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByCreatedAtBetween(start, end);
    }

    @Transactional
    @Override
    public boolean processReturn(Long saleId, Long productId, Integer quantity) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        SaleDetail detail = sale.getDetails().stream()
                .filter(d -> d.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in sale"));

        if (quantity > detail.getQuantity()) {
            throw new RuntimeException("Invalid return quantity");
        }

        Product product = detail.getProduct();
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        // Subtract returned quantity
        detail.setQuantity(detail.getQuantity() - quantity);
        detail.setSubtotal(detail.getQuantity() * detail.getPrice());
        sale.setTotal(sale.getTotal() - (quantity * detail.getPrice()));

        // Remove detail if quantity becomes zero
        if (detail.getQuantity() == 0) {
            sale.getDetails().remove(detail);
        }

        // Check if sale should be deleted
        if (sale.getDetails().isEmpty()) {
            saleRepository.delete(sale);
            return true; // Sale was deleted
        } else {
            saleRepository.save(sale);
            return false; // Sale was not deleted
        }
    }

    @Transactional
    @Override
    public void returnEntireSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        // Process return for each product in the sale
        List<SaleDetail> detailsCopy = new ArrayList<>(sale.getDetails());
        for (SaleDetail detail : detailsCopy) {
            Product product = detail.getProduct();
            product.setStock(product.getStock() + detail.getQuantity());
            productRepository.save(product);
        }

        // Delete the entire sale
        saleRepository.delete(sale);
    }

    @Override
    public void exportSalesReport(LocalDateTime start, LocalDateTime end, String filePath) {
        List<Sale> sales = findSalesByDateRange(start, end);
    
        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream outputStream = new FileOutputStream(filePath)) {
    
            Sheet sheet = workbook.createSheet("Reporte de Ventas");
    
            // Updated headers array with new "Vendedor" column
            String[] headers = {
                    "ID Venta",
                    "Vendedor",                  // New column
                    "Fecha de Venta",
                    "Total Sale Amount",
                    "Producto",
                    "Código de Barras",
                    "Cantidad",
                    "Precio de Venta Unitario",
                    "Precio de Compra Unitario",
                    "Total de Venta",
                    "Ganancias Producto"
            };
    
            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
    
            if (headerStyle instanceof XSSFCellStyle) {
                XSSFCellStyle xssfHeaderStyle = (XSSFCellStyle) headerStyle;
                xssfHeaderStyle.setFillForegroundColor(new XSSFColor(new Color(0, 129, 219), null));
                xssfHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
    
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
    
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
    
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
    
            // Populate data rows
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
            // Totals tracking
            double totalSalesAmount = 0.0;
            double totalSalesMargin = 0.0;
    
            for (Sale sale : sales) {
                for (SaleDetail detail : sale.getDetails()) {
                    Row row = sheet.createRow(rowNum++);
    
                    // ID Venta
                    row.createCell(0).setCellValue(sale.getId() != null ? sale.getId() : 0);
                    
                    // Vendedor (User full name)
                    String vendedor = (sale.getUser() != null) 
                        ? sale.getUser().getName() + " " + sale.getUser().getLastName() 
                        : "N/A";
                    row.createCell(1).setCellValue(vendedor);
                    
                    // Rest of the columns (shifted one position to the right)
                    row.createCell(2).setCellValue(sale.getCreatedAt() != null ? sale.getCreatedAt().format(dateFormatter) : "");
                    row.createCell(3).setCellValue(sale.getTotal());
                    row.createCell(4).setCellValue(detail.getProduct() != null ? detail.getProduct().getName() : "N/A");
                    row.createCell(5).setCellValue(detail.getProduct() != null ? detail.getProduct().getBarcode() : "N/A");
                    row.createCell(6).setCellValue(detail.getQuantity());
                    row.createCell(7).setCellValue(detail.getPrice());
                    
                    double purchasePrice = detail.getPurchasePriceAtSale() != null ? detail.getPurchasePriceAtSale() : 0.0;
                    row.createCell(8).setCellValue(purchasePrice);
                    row.createCell(9).setCellValue(detail.getSubtotal());
                    
                    double productMargin = detail.getSubtotal() - (detail.getQuantity() * purchasePrice);
                    row.createCell(10).setCellValue(productMargin);
    
                    // Calculate sale margin
                    double saleMargin = sale.getTotal() - (sale.getDetails().stream()
                            .mapToDouble(d -> d.getQuantity() *
                                    (d.getPurchasePriceAtSale() != null ? d.getPurchasePriceAtSale() : 0.0))
                            .sum());
    
                    // Update totals
                    totalSalesAmount += sale.getTotal();
                    totalSalesMargin += saleMargin;
                }
            }
    
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Sales Summary");
    
            // Summary headers
            String[] summaryHeaders = { "Metric", "Value" };
            Row summaryHeaderRow = summarySheet.createRow(0);
            for (int i = 0; i < summaryHeaders.length; i++) {
                Cell cell = summaryHeaderRow.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
    
            // Summary rows
            String[][] summaryData = {
                    { "Total Sales Amount", String.format("%.2f", totalSalesAmount) },
                    { "Total Sales Margin", String.format("%.2f", totalSalesMargin) },
                    { "Number of Sales", String.valueOf(sales.size()) }
            };
    
            for (int i = 0; i < summaryData.length; i++) {
                Row summaryRow = summarySheet.createRow(i + 1);
                summaryRow.createCell(0).setCellValue(summaryData[i][0]);
                summaryRow.createCell(1).setCellValue(summaryData[i][1]);
            }
    
            // Auto-size columns for both sheets
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);
    
            // Write to file
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error exporting sales report", e);
        }
    }
}
