package model;

import java.util.Date;

public class Report {
    private int id;
    private String reportType;
    private Date generatedOn;
    private String parameters;
    private String filePath;
    
    public Report() {
        this.generatedOn = new Date();
    }
    
    public Report(int id, String reportType, Date generatedOn, String parameters, String filePath) {
        this.id = id;
        this.reportType = reportType;
        this.generatedOn = generatedOn;
        this.parameters = parameters;
        this.filePath = filePath;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public Date getGeneratedOn() {
        return generatedOn;
    }
    
    public void setGeneratedOn(Date generatedOn) {
        this.generatedOn = generatedOn;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}