package model;

import java.util.Date;

public class InventoryAdjustment {
    private int id;
    private int productId;
    private Date date;
    private int changeQty;
    private String reason;
    
    // UI helper property
    private String productName;
    
    public InventoryAdjustment() {
        this.date = new Date();
    }
    
    public InventoryAdjustment(int id, int productId, Date date, int changeQty, String reason) {
        this.id = id;
        this.productId = productId;
        this.date = date;
        this.changeQty = changeQty;
        this.reason = reason;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public int getChangeQty() {
        return changeQty;
    }
    
    public void setChangeQty(int changeQty) {
        this.changeQty = changeQty;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
}