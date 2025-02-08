package com.drogueria.inventario.services.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drogueria.inventario.models.Product;
import com.drogueria.inventario.models.ProductStock;
import com.drogueria.inventario.repositories.ProductRepository;
import com.drogueria.inventario.services.ProductService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import java.awt.Color;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product createProduct(Product product) {
        validateExpirationDate(product.getExpirationDate());
        product.setName(capitalizeFirstLetter(product.getName()));
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (productRepository.existsByBarcodeAndIdNot(product.getBarcode(), id)) {
            throw new RuntimeException("Barcode already exists");
        }

        validateExpirationDate(product.getExpirationDate());

        existingProduct.setName(capitalizeFirstLetter(product.getName()));
        existingProduct.setPurchasePrice(product.getPurchasePrice());
        existingProduct.setSalePrice(product.getSalePrice());
        existingProduct.setWholesalePrice(product.getWholesalePrice());
        existingProduct.setBarcode(product.getBarcode());
        existingProduct.setStock(product.getStock());
        existingProduct.setMinimumStock(product.getMinimumStock());
        existingProduct.setExpirationDate(product.getExpirationDate());
        existingProduct.setLocation(product.getLocation());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setDistributor(product.getDistributor());
        existingProduct.setSalesForm(product.getSalesForm());
        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrBarcodeContainingIgnoreCase(query, query);
    }

    @Override
    public void addStock(Long id, int quantity, double unitPrice, double unitSalePrice, LocalDate expirationDate) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero.");
        }
        if (unitSalePrice <= unitPrice) {
            throw new IllegalArgumentException("Sale price must be greater than purchase price.");
        }
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future.");
        }
    
        // Create new stock batch
        ProductStock newStock = new ProductStock();
        newStock.setProduct(product);
        newStock.setQuantity(quantity);
        newStock.setUnitPrice(unitPrice);
        newStock.setUnitSalePrice(unitSalePrice);  // Set new sale price
        newStock.setExpirationDate(expirationDate);
    
        // Add new batch to product
        product.getStocks().add(newStock);
    
        // Calculate new weighted average purchase price
        double totalValue = product.getStock() * product.getPurchasePrice() +
                quantity * unitPrice;
        int totalQuantity = product.getStock() + quantity;
    
        if (totalQuantity > 0) {
            product.setPurchasePrice(totalValue / totalQuantity);
        }
    
        // Calculate new weighted average sale price
        double totalSaleValue = product.getStock() * product.getSalePrice() +
                quantity * unitSalePrice;
        
        if (totalQuantity > 0) {
            product.setSalePrice(totalSaleValue / totalQuantity);
        }
    
        // Update base stock by adding new quantity
        product.setStock(product.getStock() + quantity);
    
        // Save product
        productRepository.save(product);
    }

    private void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future.");
        }
    }

    @Override
    public void exportProductsReport(String filePath) {
        // Retrieve all products
        List<Product> products = productRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream outputStream = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Products Report");

            // Define headers
            String[] headers = {
                    "ID",
                    "Barcode",
                    "Name",
                    "Purchase Price",
                    "Sale Price",
                    "Wholesale Price",
                    "Current Stock",
                    "Minimum Stock",
                    "Location",
                    "Category",
                    "Distributor",
                    "Sales Form",
                    "Expiration Date"
            };

            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();

            // Para un color RGB personalizado
            if (headerStyle instanceof XSSFCellStyle) {
                XSSFCellStyle xssfHeaderStyle = (XSSFCellStyle) headerStyle;
                xssfHeaderStyle.setFillForegroundColor(new XSSFColor(new Color(0, 129, 219), null));
                xssfHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            // Letra blanca
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Alineación centrada (opcional)
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Bordes (opcional, para un look más pulido)
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
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getBarcode());
                row.createCell(2).setCellValue(product.getName());
                row.createCell(3).setCellValue(product.getPurchasePrice());
                row.createCell(4).setCellValue(product.getSalePrice());
                row.createCell(5).setCellValue(product.getWholesalePrice());
                row.createCell(6).setCellValue(product.getStock());
                row.createCell(7).setCellValue(product.getMinimumStock());
                row.createCell(8).setCellValue(product.getLocation());
                row.createCell(9).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "N/A");
                row.createCell(10)
                        .setCellValue(product.getDistributor() != null ? product.getDistributor().getName() : "N/A");
                row.createCell(11)
                        .setCellValue(product.getSalesForm() != null ? product.getSalesForm().getName() : "N/A");
                row.createCell(12).setCellValue(
                        product.getExpirationDate() != null ? product.getExpirationDate().format(dateFormatter)
                                : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error exporting products report", e);
        }
    }

    @Override
    public List<Map<String, Object>> getExpirationWarnings(int monthsThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusMonths(monthsThreshold);
        List<Map<String, Object>> warnings = new ArrayList<>();

        // Buscar productos con stock bajo (mantener como estaba)
        List<Product> lowStockProducts = productRepository.findAll().stream()
                .filter(product -> product.getStock() <= product.getMinimumStock())
                .collect(Collectors.toList());

        lowStockProducts.forEach(product -> {
            Map<String, Object> warning = new HashMap<>();
            warning.put("type", "LOW_STOCK");
            warning.put("productName", product.getName());
            warning.put("currentStock", product.getStock());
            warning.put("minimumStock", product.getMinimumStock());
            warnings.add(warning);
        });

        // Buscar productos con lotes próximos a vencer
        productRepository.findAll().forEach(product -> {
            // Verificar lotes próximos a vencer, excluyendo lotes con cantidad 0
            List<ProductStock> expiringBatches = product.getStocks().stream()
                    .filter(stock -> stock.getQuantity() > 0) // Filtro de cantidad mayor a 0
                    .filter(stock -> stock.getExpirationDate().isBefore(thresholdDate))
                    .collect(Collectors.toList());

            // Enumerar los lotes
            for (int batchIndex = 0; batchIndex < expiringBatches.size(); batchIndex++) {
                ProductStock batch = expiringBatches.get(batchIndex);
                Map<String, Object> warning = new HashMap<>();
                warning.put("type", "EXPIRING_BATCH");
                warning.put("productName", product.getName());
                warning.put("batchId", batch.getId()); // ID del lote
                warning.put("batchNumber", batchIndex + 1); // Número de lote secuencial
                warning.put("batchQuantity", batch.getQuantity()); // Cantidad en el lote
                warning.put("expirationDate", batch.getExpirationDate());
                warning.put("daysUntilExpiration", ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpirationDate()));
                warnings.add(warning);
            }

            // Verificar fecha de vencimiento global del producto (si no tiene lotes)
            if (product.getStocks().isEmpty() &&
                    product.getExpirationDate() != null &&
                    product.getExpirationDate().isBefore(thresholdDate)) {
                Map<String, Object> warning = new HashMap<>();
                warning.put("type", "EXPIRING_PRODUCT");
                warning.put("productName", product.getName());
                warning.put("expirationDate", product.getExpirationDate());
                warning.put("daysUntilExpiration",
                        ChronoUnit.DAYS.between(LocalDate.now(), product.getExpirationDate()));
                warnings.add(warning);
            }
        });

        // Ordenar por tipo y fecha de vencimiento
        return warnings.stream()
                .sorted(Comparator
                        .comparing((Map<String, Object> w) -> (String) w.get("type"))
                        .thenComparing(w -> {
                            if (w.get("type").equals("EXPIRING_BATCH")) {
                                return ((LocalDate) w.get("expirationDate"));
                            } else if (w.get("type").equals("EXPIRING_PRODUCT")) {
                                return ((LocalDate) w.get("expirationDate"));
                            }
                            return LocalDate.MAX;
                        }))
                .collect(Collectors.toList());
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

}
