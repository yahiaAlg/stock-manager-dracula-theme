package util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.Order;
import model.OrderItem;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PDFGenerator {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Standard margins
    private static final float MARGIN = 50;
    private static final float TABLE_MARGIN = 10;
    
    // Current Y position tracker
    private static float currentY;
    
    // Directory for storing tickets
    private static final String TICKETS_DIRECTORY = "tickets";
    
    /**
     * Generates a PDF ticket for an order
     * @param order The order to generate a PDF for
     * @return The generated PDF file
     */
    public static File generateOrderTicket(Order order) throws IOException {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        // Create tickets directory if it doesn't exist
        File ticketsDir = new File(TICKETS_DIRECTORY);
        if (!ticketsDir.exists()) {
            if (!ticketsDir.mkdirs()) {
                throw new IOException("Failed to create tickets directory");
            }
        }
        
        // Create a timestamp for the filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pdfFile = new File(ticketsDir, "Order_" + order.getId() + "_" + timestamp + ".pdf");
        
        // Create PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        // Calculate starting Y position (top of the page)
        currentY = page.getMediaBox().getHeight() - MARGIN;
        
        // Create content stream for writing
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Add title
        drawTitle(contentStream, "Order Ticket #" + order.getId());
        
        // Order details section
        drawSectionHeader(contentStream, "Order Details");
        addOrderDetails(contentStream, order);
        
        // Customer details section
        drawSectionHeader(contentStream, "Customer Information");
        addCustomerInfo(contentStream, order);
        
        // Order items section
        drawSectionHeader(contentStream, "Order Items");
        addOrderItems(contentStream, order);
        
        // Total section
        currentY -= 20;
        drawText(contentStream, "Total Amount: $" + String.format("%.2f", order.getTotalAmount()), 
                 MARGIN, currentY, PDType1Font.HELVETICA_BOLD, 12);
        
        // Footer with timestamp
        String footer = "Generated on: " + DATE_FORMAT.format(new Date());
        drawText(contentStream, footer, 
                 page.getMediaBox().getWidth() - MARGIN - getStringWidth(footer, PDType1Font.HELVETICA, 10), 
                 MARGIN, PDType1Font.HELVETICA, 10);
        
        // Close content stream
        contentStream.close();
        
        // Save document
        document.save(pdfFile);
        document.close();
        
        return pdfFile;
    }
    
    /**
     * Draws a title at the current Y position
     */
    private static void drawTitle(PDPageContentStream contentStream, String title) throws IOException {
        drawText(contentStream, title, MARGIN, currentY, PDType1Font.HELVETICA_BOLD, 18);
        currentY -= 30; // Space after title
    }
    
    /**
     * Draws a section header at the current Y position
     */
    private static void drawSectionHeader(PDPageContentStream contentStream, String header) throws IOException {
        drawText(contentStream, header, MARGIN, currentY, PDType1Font.HELVETICA_BOLD, 14);
        currentY -= 20; // Space after section header
    }
    
    /**
     * Adds order details to the document
     */
    private static void addOrderDetails(PDPageContentStream contentStream, Order order) throws IOException {
        // Order ID
        drawRowText(contentStream, "Order ID:", String.valueOf(order.getId()));
        
        // Order Date
        drawRowText(contentStream, "Order Date:", DATE_FORMAT.format(order.getOrderDate()));
        
        // Order Status
        drawRowText(contentStream, "Status:", order.getStatus());
        
        currentY -= 10; // Add some space after the details section
    }
    
    /**
     * Adds customer information to the document
     */
    private static void addCustomerInfo(PDPageContentStream contentStream, Order order) throws IOException {
        // Customer ID
        drawRowText(contentStream, "Customer ID:", String.valueOf(order.getCustomerId()));
        
        // Customer Name
        drawRowText(contentStream, "Customer Name:", order.getCustomerName());
        
        currentY -= 10; // Add some space after the customer info section
    }
    
    /**
     * Adds order items to the document as a table
     */
    private static void addOrderItems(PDPageContentStream contentStream, Order order) throws IOException {
        // Define column widths as percentages of usable width
        float pageWidth = PDRectangle.A4.getWidth();
        float tableWidth = pageWidth - (2 * MARGIN);
        
        float[] columnWidthsPercentage = {10, 50, 10, 15, 15}; // Percentage of table width
        float[] columnWidths = new float[columnWidthsPercentage.length];
        
        for (int i = 0; i < columnWidthsPercentage.length; i++) {
            columnWidths[i] = tableWidth * (columnWidthsPercentage[i] / 100f);
        }
        
        // Table header positions
        float startX = MARGIN;
        float startY = currentY;
        
        // Draw table header
        float nextX = startX;
        
        // Draw table headers with gray background
        // Using non-deprecated version with normalized RGB values (0.0-1.0)
        contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f); // Dark gray
        contentStream.addRect(startX, startY - 15, tableWidth, 15);
        contentStream.fill();
        
        // Reset color to black for text
        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        
        String[] headers = {"No.", "Product", "Qty", "Unit Price", "Subtotal"};
        
        for (int i = 0; i < headers.length; i++) {
            drawText(contentStream, headers[i], nextX + TABLE_MARGIN, startY - 12, PDType1Font.HELVETICA_BOLD, 10);
            nextX += columnWidths[i];
        }
        
        // Move to the next row
        currentY = startY - 15;
        
        // Draw table rows
        for (int i = 0; i < order.getOrderItems().size(); i++) {
            OrderItem item = order.getOrderItems().get(i);
            
            startY = currentY;
            nextX = startX;
            
            // Row background (alternating)
            if (i % 2 == 1) {
                contentStream.setNonStrokingColor(0.95f, 0.95f, 0.95f); // Light gray
                contentStream.addRect(startX, startY - 15, tableWidth, 15);
                contentStream.fill();
                contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f); // Reset to black
            }
            
            // Item number
            drawText(contentStream, String.valueOf(i + 1), nextX + TABLE_MARGIN, startY - 12, PDType1Font.HELVETICA, 10);
            nextX += columnWidths[0];
            
            // Product name
            drawText(contentStream, item.getProductName(), nextX + TABLE_MARGIN, startY - 12, PDType1Font.HELVETICA, 10);
            nextX += columnWidths[1];
            
            // Quantity
            drawText(contentStream, String.valueOf(item.getQuantity()), nextX + TABLE_MARGIN, startY - 12, PDType1Font.HELVETICA, 10);
            nextX += columnWidths[2];
            
            // Unit price
            drawText(contentStream, "$" + String.format("%.2f", item.getUnitPrice()), nextX + TABLE_MARGIN, startY - 12, PDType1Font.HELVETICA, 10);
            nextX += columnWidths[3];
            
            // Subtotal
            drawText(contentStream, "$" + String.format("%.2f", item.getSubtotal()), nextX + TABLE_MARGIN, startY - 12, PDType1Font.HELVETICA, 10);
            
            // Move to the next row
            currentY = startY - 15;
        }
    }
    
    /**
     * Helper method to draw a row with label and value
     */
    private static void drawRowText(PDPageContentStream contentStream, String label, String value) throws IOException {
        float centerX = MARGIN + 100; // Position for value column
        
        // Draw label
        drawText(contentStream, label, MARGIN, currentY, PDType1Font.HELVETICA_BOLD, 12);
        
        // Draw value
        drawText(contentStream, value, centerX, currentY, PDType1Font.HELVETICA, 12);
        
        // Move to next row
        currentY -= 20;
    }
    
    /**
     * Helper method to draw text at a specific position
     */
    private static void drawText(PDPageContentStream contentStream, String text, float x, float y, 
                              PDType1Font font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
    
    /**
     * Helper method to calculate approximate string width
     */
    private static float getStringWidth(String text, PDType1Font font, float fontSize) {
        // This is a simple approximation
        return text.length() * fontSize * 0.5f;
    }
}