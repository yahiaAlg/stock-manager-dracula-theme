package util;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import model.Order;
import model.OrderItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class PDFGenerator {
    private PDDocument document;
    private PDPage page;
    private PDPageContentStream contentStream;
    private float margin = 50;
    private float yPosition;
    private float width;
    private float height;
    
    // Fonts for different languages
    private PDFont regularFont;
    private PDFont boldFont;
    private PDFont arabicFont;
    private PDFont arabicBoldFont;
    
    private ResourceBundle messages;
    private String language;
    
    public PDFGenerator(String language, Locale locale) throws IOException {
        this.document = new PDDocument();
        this.page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        this.width = page.getMediaBox().getWidth();
        this.height = page.getMediaBox().getHeight();
        this.yPosition = height - margin;
        
        this.contentStream = new PDPageContentStream(document, page);
        this.language = language;
        
        // Load language-specific resource bundle
        this.messages = ResourceBundle.getBundle("resources.Messages", locale);
        
        // Initialize fonts based on language
        initializeFonts();
    }
    
    private void initializeFonts() throws IOException {
        // Default Latin fonts
        this.regularFont = PDType1Font.HELVETICA;
        this.boldFont = PDType1Font.HELVETICA_BOLD;
        
        // Load Arabic fonts if needed
        if ("ar".equals(language)) {
            // Try multiple locations for the Arabic font files
            File[] possibleFontLocations = {
                new File("fonts/NotoSansArabic-Regular.ttf"),
                new File("resources/fonts/NotoSansArabic-Regular.ttf"),
                new File("resources/fonts/Amiri-Regular.ttf"),
                new File("fonts/Amiri-Regular.ttf")
            };
            
            File[] possibleBoldFontLocations = {
                new File("fonts/NotoSansArabic-Bold.ttf"),
                new File("resources/fonts/NotoSansArabic-Bold.ttf"),
                new File("resources/fonts/Amiri-Bold.ttf"),
                new File("fonts/Amiri-Bold.ttf")
            };
            
            boolean foundFont = false;
            for (File fontFile : possibleFontLocations) {
                if (fontFile.exists()) {
                    this.arabicFont = PDType0Font.load(document, fontFile);
                    foundFont = true;
                    break;
                }
            }
            
            boolean foundBoldFont = false;
            for (File fontFile : possibleBoldFontLocations) {
                if (fontFile.exists()) {
                    this.arabicBoldFont = PDType0Font.load(document, fontFile);
                    foundBoldFont = true;
                    break;
                }
            }
            
            if (!foundFont || !foundBoldFont) {
                System.err.println("Arabic font files not found. Using fallback fonts.");
                // Use default fonts as fallback if Arabic fonts aren't found
                if (!foundFont) this.arabicFont = this.regularFont;
                if (!foundBoldFont) this.arabicBoldFont = this.boldFont;
            }
        }
    }   
    
    public File generateOrderTicket(Order order) throws IOException {
        try {
            drawTitle();
            yPosition -= 30;
            
            drawOrderDetails(order);
            yPosition -= 20;
            
            drawCustomerInfo(order);
            yPosition -= 20;
            
            drawOrderItems(order);
            yPosition -= 20;
            
            drawTotalAmount(order);
            drawFooter();
            
            contentStream.close();
            
            // Create tickets directory if it doesn't exist
            File ticketsDir = new File("tickets");
            if (!ticketsDir.exists()) {
                ticketsDir.mkdir();
            }
            
            // Generate filename with timestamp to avoid conflicts
            SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = filenameDateFormat.format(new Date());
            String fileName = "order_" + order.getId() + "_" + timestamp + ".pdf";
            File pdfFile = new File(ticketsDir, fileName);
            
            document.save(pdfFile.getAbsolutePath());
            document.close();
            
            return pdfFile;
        } catch (IOException e) {
            contentStream.close();
            throw e;
        }
    }
    
    private void drawTitle() throws IOException {
        String title = messages.getString("orders.pdf.title").replace("{0}", "12345");
        
        // Choose font based on language
        PDFont titleFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        float fontSize = 16;
        
        float titleWidth = getStringWidth(title, titleFont, fontSize);
        float titleX = (width - titleWidth) / 2;
        
        // For Arabic, we need to handle right-to-left text
        if ("ar".equals(language)) {
            // Draw RTL text
            drawRTLText(title, titleX, yPosition, titleFont, fontSize);
        } else {
            contentStream.beginText();
            contentStream.setFont(titleFont, fontSize);
            contentStream.newLineAtOffset(titleX, yPosition);
            contentStream.showText(title);
            contentStream.endText();
        }
    }
    
    private void drawOrderDetails(Order order) throws IOException {
        String sectionTitle = messages.getString("orders.pdf.orderDetails");
        PDFont sectionFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        
        drawSectionHeader(sectionTitle, sectionFont);
        yPosition -= 15;
        
        // Draw order ID
        String orderIdLabel = messages.getString("orders.pdf.orderId") + ": ";
        String orderId = String.valueOf(order.getId());
        drawLabelAndValue(orderIdLabel, orderId);
        yPosition -= 10;
        
        // Draw order date
        String orderDateLabel = messages.getString("orders.pdf.orderDate") + ": ";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String orderDate = dateFormat.format(order.getOrderDate());
        drawLabelAndValue(orderDateLabel, orderDate);
        yPosition -= 10;
        
        // Draw status
        String statusLabel = messages.getString("orders.pdf.status") + ": ";
        String status = order.getStatus();
        drawLabelAndValue(statusLabel, status);
    }
    
    private void drawCustomerInfo(Order order) throws IOException {
        String sectionTitle = messages.getString("orders.pdf.customerInfo");
        PDFont sectionFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        
        drawSectionHeader(sectionTitle, sectionFont);
        yPosition -= 15;
        
        // Draw customer ID
        String customerIdLabel = messages.getString("orders.pdf.customerId") + ": ";
        String customerId = String.valueOf(order.getCustomerId());
        drawLabelAndValue(customerIdLabel, customerId);
        yPosition -= 10;
        
        // Draw customer name
        String customerNameLabel = messages.getString("orders.pdf.customerName") + ": ";
        String customerName = order.getCustomerName();
        drawLabelAndValue(customerNameLabel, customerName);
    }
    
    private void drawOrderItems(Order order) throws IOException {
        String sectionTitle = messages.getString("orders.pdf.orderItems");
        PDFont sectionFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        
        drawSectionHeader(sectionTitle, sectionFont);
        yPosition -= 20;
        
        // Draw table header
        float[] columnWidths = {40, 200, 80, 100, 120};
        float startX = margin;
        float currentX = startX;
        
        PDFont headerFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        float fontSize = 10;
        
        // Draw column headers
        String[] headers = {
            messages.getString("orders.pdf.columnNo"),
            messages.getString("orders.pdf.columnProduct"),
            messages.getString("orders.pdf.columnQty"),
            messages.getString("orders.pdf.columnUnitPrice"),
            messages.getString("orders.pdf.columnSubtotal")
        };
        
        for (int i = 0; i < headers.length; i++) {
            if ("ar".equals(language)) {
                // For Arabic, we need right-to-left alignment
                float textWidth = getStringWidth(headers[i], headerFont, fontSize);
                float textX = currentX + columnWidths[i] - textWidth - 5; // 5 is padding
                drawRTLText(headers[i], textX, yPosition, headerFont, fontSize);
            } else {
                contentStream.beginText();
                contentStream.setFont(headerFont, fontSize);
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
            }
            currentX += columnWidths[i];
        }
        
        // Draw horizontal line
        contentStream.moveTo(margin, yPosition - 5);
        contentStream.lineTo(width - margin, yPosition - 5);
        contentStream.stroke();
        
        yPosition -= 20;
        
        // Draw table rows
        PDFont textFont = "ar".equals(language) ? arabicFont : regularFont;
        int itemNumber = 1;
        
        for (OrderItem item : order.getOrderItems()) {
            currentX = startX;
            
            // No.
            drawTableCell(String.valueOf(itemNumber), currentX, columnWidths[0], textFont);
            currentX += columnWidths[0];
            
            // Product
            drawTableCell(item.getProductName(), currentX, columnWidths[1], textFont);
            currentX += columnWidths[1];
            
            // Qty
            drawTableCell(String.valueOf(item.getQuantity()), currentX, columnWidths[2], textFont);
            currentX += columnWidths[2];
            
            // Unit Price
            String unitPrice = String.format("%.2f", item.getUnitPrice());
            drawTableCell(unitPrice, currentX, columnWidths[3], textFont);
            currentX += columnWidths[3];
            
            // Subtotal
            String subtotal = String.format("%.2f", item.getQuantity() * item.getUnitPrice());
            drawTableCell(subtotal, currentX, columnWidths[4], textFont);
            
            yPosition -= 15;
            itemNumber++;
        }
    }
    
    private void drawTableCell(String text, float x, float width, PDFont font) throws IOException {
        float fontSize = 10;
        
        if ("ar".equals(language)) {
            float textWidth = getStringWidth(text, font, fontSize);
            float textX = x + width - textWidth - 5; // 5 is padding for right alignment
            drawRTLText(text, textX, yPosition, font, fontSize);
        } else {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x + 5, yPosition); // 5 is padding for left alignment
            contentStream.showText(text);
            contentStream.endText();
        }
    }
    
    private void drawTotalAmount(Order order) throws IOException {
        String totalLabel = messages.getString("orders.pdf.totalAmount");
        String currency = messages.getString("orders.pdf.currency");
        
        double total = 0;
        for (OrderItem item : order.getOrderItems()) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        
        String totalText = totalLabel.replace("{0}", String.format("%.2f %s", total, currency));
        PDFont totalFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        float fontSize = 12;
        
        if ("ar".equals(language)) {
            float textWidth = getStringWidth(totalText, totalFont, fontSize);
            float textX = width - margin - textWidth;
            drawRTLText(totalText, textX, yPosition, totalFont, fontSize);
        } else {
            contentStream.beginText();
            contentStream.setFont(totalFont, fontSize);
            contentStream.newLineAtOffset(width - margin - getStringWidth(totalText, totalFont, fontSize), yPosition);
            contentStream.showText(totalText);
            contentStream.endText();
        }
    }
    
    private void drawFooter() throws IOException {
        yPosition = margin + 20;
        
        String generatedText = messages.getString("orders.pdf.generatedOn")
                .replace("{0}", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        
        PDFont footerFont = "ar".equals(language) ? arabicFont : regularFont;
        float fontSize = 8;
        
        if ("ar".equals(language)) {
            float textWidth = getStringWidth(generatedText, footerFont, fontSize);
            float textX = width - margin - textWidth;
            drawRTLText(generatedText, textX, yPosition, footerFont, fontSize);
        } else {
            contentStream.beginText();
            contentStream.setFont(footerFont, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(generatedText);
            contentStream.endText();
        }
    }
    
    private void drawSectionHeader(String text, PDFont font) throws IOException {
        float fontSize = 12;
        
        if ("ar".equals(language)) {
            // For Arabic, we align text to the right
            float textWidth = getStringWidth(text, font, fontSize);
            float textX = width - margin - textWidth;
            drawRTLText(text, textX, yPosition, font, fontSize);
            
            // Draw underline
            contentStream.moveTo(margin, yPosition - 5);
            contentStream.lineTo(width - margin, yPosition - 5);
            contentStream.stroke();
        } else {
            // For LTR languages
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(text);
            contentStream.endText();
            
            // Draw underline
            contentStream.moveTo(margin, yPosition - 5);
            contentStream.lineTo(width - margin, yPosition - 5);
            contentStream.stroke();
        }
    }
    
    private void drawLabelAndValue(String label, String value) throws IOException {
        PDFont labelFont = "ar".equals(language) ? arabicBoldFont : boldFont;
        PDFont valueFont = "ar".equals(language) ? arabicFont : regularFont;
        float fontSize = 10;
        
        if ("ar".equals(language)) {
            // For Arabic (RTL), we position differently
            float labelWidth = getStringWidth(label, labelFont, fontSize);
            float valueWidth = getStringWidth(value, valueFont, fontSize);
            float totalWidth = labelWidth + valueWidth;
            
            // Draw the value first (RTL order)
            float valueX = width - margin - valueWidth;
            drawRTLText(value, valueX, yPosition, valueFont, fontSize);
            
            // Then draw the label
            float labelX = valueX - labelWidth;
            drawRTLText(label, labelX, yPosition, labelFont, fontSize);
        } else {
            // For LTR languages
            contentStream.beginText();
            contentStream.setFont(labelFont, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(label);
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(valueFont, fontSize);
            contentStream.newLineAtOffset(margin + getStringWidth(label, labelFont, fontSize), yPosition);
            contentStream.showText(value);
            contentStream.endText();
        }
    }
    
    // Helper method to draw right-to-left text for Arabic
    private void drawRTLText(String text, float x, float y, PDFont font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        
        // Enable right-to-left text direction
        if ("ar".equals(language) && font instanceof PDType0Font) {
            // Set text rendering mode for RTL
            contentStream.setTextMatrix(1, 0, 0, 1, x, y); // Reset text matrix
            
            // PDFBox doesn't have great built-in RTL support, so we need to reverse the string
            // and handle bidirectional text properly
            StringBuilder reverseText = new StringBuilder(text);
            // For proper Arabic text rendering, we'd need more sophisticated handling
            // but this is a start for getting the direction right
            contentStream.showText(reverseText.toString());
        } else {
            contentStream.showText(text);
        }
        contentStream.endText();
    }   

    public float getStringWidth(String text, PDFont font, float fontSize) throws IOException {
        // If the font doesn't support some characters, we need to handle them separately
        try {
            return font.getStringWidth(text) / 1000 * fontSize;
        } catch (IllegalArgumentException e) {
            // For characters not supported by the font, use a fallback approach
            float width = 0;
            for (int i = 0; i < text.length(); i++) {
                try {
                    String character = text.substring(i, i + 1);
                    width += font.getStringWidth(character) / 1000 * fontSize;
                } catch (IllegalArgumentException ex) {
                    // For unsupported characters, use an average width
                    width += fontSize * 0.5; // Approximate width for unsupported characters
                }
            }
            return width;
        }
    }
    
    public void saveDocument(String filePath) throws IOException {
        document.save(filePath);
        document.close();
    }
}