package com.drogueria.inventario.utils;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.drogueria.inventario.models.Sale;
import com.drogueria.inventario.models.SaleDetail;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SaleReceiptGenerator {
    // Modern color scheme
    private static final DeviceRgb PRIMARY_BLUE = new DeviceRgb(41, 128, 185);    // Strong blue
    private static final DeviceRgb SECONDARY_BLUE = new DeviceRgb(52, 152, 219);  // Light blue
    private static final DeviceRgb DARK_GREY = new DeviceRgb(52, 73, 94);         // Dark grey
    private static final DeviceRgb LIGHT_GREY = new DeviceRgb(236, 240, 241);     // Light grey
    private static final DeviceRgb TEXT_GREY = new DeviceRgb(74, 85, 104);        // Text grey
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);          // White
    
    // Constantes de estilo
    private static final float CELL_PADDING = 8f;
    private static final float HEADER_PADDING = 8f;

    public static byte[] generateReceipt(Sale sale) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            addModernHeader(document);
            addSaleInfoCard(document, sale);
            addModernItemsTable(document, sale);
            addModernTotal(document, sale);
            addModernFooter(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private static void addModernHeader(Document document) {
        Table headerTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);
        
        Cell headerCell = new Cell()
                .add(new Paragraph("DROGUERIA FREILLER JOSE")
                        .setFontSize(28)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(DARK_GREY))
                .add(new Paragraph("FACTURA")
                        .setFontSize(18)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(5)
                        .setFontColor(SECONDARY_BLUE))
                .setBorder(null)
                .setPadding(HEADER_PADDING);

        headerTable.addCell(headerCell);
        document.add(headerTable);
    }

    private static void addSaleInfoCard(Document document, Sale sale) {
        Table infoCard = new Table(2)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Aplicar estilo a la tabla de información
        Cell wrapperCell = new Cell(1, 2)
                .add(createInfoContent(sale))
                .setBackgroundColor(LIGHT_GREY)
                .setBorder(null)
                .setPadding(0);

        infoCard.addCell(wrapperCell);
        document.add(infoCard);
    }

    private static Table createInfoContent(Sale sale) {
        Table content = new Table(2)
                .setWidth(UnitValue.createPercentValue(100));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        content.addCell(createModernCell("N° de Venta", true).setFontColor(PRIMARY_BLUE));
        content.addCell(createModernCell(sale.getId().toString(), true).setFontColor(DARK_GREY));
        content.addCell(createModernCell("Fecha", true));
        content.addCell(createModernCell(sale.getCreatedAt().format(formatter), false));
        content.addCell(createModernCell("Vendedor", true));
        content.addCell(createModernCell(
                sale.getUser().getName() + " " + sale.getUser().getLastName(),
                false));

        return content;
    }

    private static void addModernItemsTable(Document document, Sale sale) {
        Table tableWrapper = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        Table itemsTable = new Table(4)
                .setWidth(UnitValue.createPercentValue(100));

        // Header
        Cell headerWrapper = new Cell(1, 4)
                .add(createHeaderRow())
                .setBackgroundColor(PRIMARY_BLUE)
                .setBorder(null)
                .setPadding(0);

        itemsTable.addCell(headerWrapper);

        // Contenido
        boolean alternate = false;
        for (SaleDetail detail : sale.getDetails()) {
            DeviceRgb bgColor = alternate ? LIGHT_GREY : WHITE;
            
            itemsTable.addCell(createModernCell(detail.getProduct().getName(), false)
                    .setBackgroundColor(bgColor));
            itemsTable.addCell(createModernCell(detail.getQuantity().toString(), false)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(bgColor));
            itemsTable.addCell(createModernCell(
                    String.format(Locale.US, "$ %.2f", detail.getPrice()), false)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(bgColor));
            itemsTable.addCell(createModernCell(
                    String.format(Locale.US, "$ %.2f", detail.getSubtotal()), false)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(bgColor));
            
            alternate = !alternate;
        }

        // Agregar tabla de items al wrapper
        Cell tableCell = new Cell()
                .add(itemsTable)
                .setBorder(new SolidBorder(LIGHT_GREY, 1))
                .setPadding(0);

        tableWrapper.addCell(tableCell);
        document.add(tableWrapper);
    }

    private static Table createHeaderRow() {
        Table headerRow = new Table(4)
                .setWidth(UnitValue.createPercentValue(100));

        headerRow.addCell(createModernHeaderCell("PRODUCTO"));
        headerRow.addCell(createModernHeaderCell("CANTIDAD"));
        headerRow.addCell(createModernHeaderCell("PRECIO UNIT."));
        headerRow.addCell(createModernHeaderCell("SUBTOTAL"));

        return headerRow;
    }

    private static void addModernTotal(Document document, Sale sale) {
        Table totalsWrapper = new Table(1)
                .setWidth(UnitValue.createPercentValue(40))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(20);

        Table totalsTable = new Table(2)
                .setWidth(UnitValue.createPercentValue(100));

        totalsTable.addCell(createModernCell("TOTAL", true)
                .setFontSize(14)
                .setFontColor(WHITE)
                .setBackgroundColor(DARK_GREY));
        totalsTable.addCell(createModernCell(
                String.format(Locale.US, "$ %.2f", sale.getTotal()), true)
                .setFontSize(14)
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(DARK_GREY));

        Cell wrapperCell = new Cell()
                .add(totalsTable)
                .setBorder(null)
                .setPadding(0);

        totalsWrapper.addCell(wrapperCell);
        document.add(totalsWrapper);
    }

    private static void addModernFooter(Document document) {
        Paragraph footer = new Paragraph("¡Gracias por su compra!")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontSize(14)
                .setFontColor(SECONDARY_BLUE);
        document.add(footer);
    }

    private static Cell createModernHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setFontColor(WHITE)
                .setBold()
                .setPadding(HEADER_PADDING)
                .setBorder(null)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static Cell createModernCell(String text, boolean isBold) {
        Cell cell = new Cell()
                .add(new Paragraph(text))
                .setPadding(CELL_PADDING)
                .setBorder(new SolidBorder(LIGHT_GREY, 1))
                .setFontColor(TEXT_GREY);

        if (isBold) {
            cell.setBold();
        }

        return cell;
    }
}