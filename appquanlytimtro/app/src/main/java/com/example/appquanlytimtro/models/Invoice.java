package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Invoice {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("invoiceNumber")
    private String invoiceNumber;
    
    @SerializedName("booking")
    private String bookingId;
    
    @SerializedName("tenant")
    private String tenantId;
    
    @SerializedName("landlord")
    private String landlordId;
    
    @SerializedName("room")
    private String roomId;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("period")
    private Period period;
    
    @SerializedName("items")
    private List<InvoiceItem> items;
    
    @SerializedName("subtotal")
    private double subtotal;
    
    @SerializedName("tax")
    private Tax tax;
    
    @SerializedName("discount")
    private double discount;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("dueDate")
    private String dueDate;
    
    @SerializedName("paidDate")
    private String paidDate;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("paymentReference")
    private String paymentReference;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("issuedDate")
    private String issuedDate;
    
    @SerializedName("issuedBy")
    private String issuedById;
    
    @SerializedName("pdfFile")
    private PdfFile pdfFile;
    
    @SerializedName("emailSent")
    private EmailSent emailSent;
    
    @SerializedName("reminders")
    private List<Reminder> reminders;
    
    @SerializedName("latePayment")
    private LatePayment latePayment;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    // Related objects
    @SerializedName("booking")
    private Booking booking;
    
    @SerializedName("tenant")
    private User tenant;
    
    @SerializedName("landlord")
    private User landlord;
    
    @SerializedName("room")
    private Room room;
    
    @SerializedName("issuedBy")
    private User issuedBy;

    // Constructors
    public Invoice() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getIssuedById() {
        return issuedById;
    }

    public void setIssuedById(String issuedById) {
        this.issuedById = issuedById;
    }

    public PdfFile getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(PdfFile pdfFile) {
        this.pdfFile = pdfFile;
    }

    public EmailSent getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(EmailSent emailSent) {
        this.emailSent = emailSent;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    public LatePayment getLatePayment() {
        return latePayment;
    }

    public void setLatePayment(LatePayment latePayment) {
        this.latePayment = latePayment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getTenant() {
        return tenant;
    }

    public void setTenant(User tenant) {
        this.tenant = tenant;
    }

    public User getLandlord() {
        return landlord;
    }

    public void setLandlord(User landlord) {
        this.landlord = landlord;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(User issuedBy) {
        this.issuedBy = issuedBy;
    }

    // Nested classes
    public static class Period {
        @SerializedName("startDate")
        private String startDate;
        
        @SerializedName("endDate")
        private String endDate;
        
        @SerializedName("month")
        private String month;
        
        @SerializedName("year")
        private int year;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }
    }

    public static class InvoiceItem {
        @SerializedName("description")
        private String description;
        
        @SerializedName("quantity")
        private int quantity;
        
        @SerializedName("unitPrice")
        private double unitPrice;
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("taxRate")
        private double taxRate;
        
        @SerializedName("taxAmount")
        private double taxAmount;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getTaxRate() {
            return taxRate;
        }

        public void setTaxRate(double taxRate) {
            this.taxRate = taxRate;
        }

        public double getTaxAmount() {
            return taxAmount;
        }

        public void setTaxAmount(double taxAmount) {
            this.taxAmount = taxAmount;
        }
    }

    public static class Tax {
        @SerializedName("rate")
        private double rate;
        
        @SerializedName("amount")
        private double amount;

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    public static class PdfFile {
        @SerializedName("url")
        private String url;
        
        @SerializedName("fileName")
        private String fileName;
        
        @SerializedName("generatedAt")
        private String generatedAt;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(String generatedAt) {
            this.generatedAt = generatedAt;
        }
    }

    public static class EmailSent {
        @SerializedName("sent")
        private boolean sent;
        
        @SerializedName("sentAt")
        private String sentAt;
        
        @SerializedName("sentTo")
        private String sentTo;
        
        @SerializedName("deliveryStatus")
        private String deliveryStatus;

        public boolean isSent() {
            return sent;
        }

        public void setSent(boolean sent) {
            this.sent = sent;
        }

        public String getSentAt() {
            return sentAt;
        }

        public void setSentAt(String sentAt) {
            this.sentAt = sentAt;
        }

        public String getSentTo() {
            return sentTo;
        }

        public void setSentTo(String sentTo) {
            this.sentTo = sentTo;
        }

        public String getDeliveryStatus() {
            return deliveryStatus;
        }

        public void setDeliveryStatus(String deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
        }
    }

    public static class Reminder {
        @SerializedName("sentAt")
        private String sentAt;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("method")
        private String method;

        public String getSentAt() {
            return sentAt;
        }

        public void setSentAt(String sentAt) {
            this.sentAt = sentAt;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    public static class LatePayment {
        @SerializedName("penaltyRate")
        private double penaltyRate;
        
        @SerializedName("penaltyAmount")
        private double penaltyAmount;
        
        @SerializedName("appliedAt")
        private String appliedAt;

        public double getPenaltyRate() {
            return penaltyRate;
        }

        public void setPenaltyRate(double penaltyRate) {
            this.penaltyRate = penaltyRate;
        }

        public double getPenaltyAmount() {
            return penaltyAmount;
        }

        public void setPenaltyAmount(double penaltyAmount) {
            this.penaltyAmount = penaltyAmount;
        }

        public String getAppliedAt() {
            return appliedAt;
        }

        public void setAppliedAt(String appliedAt) {
            this.appliedAt = appliedAt;
        }
    }
}
