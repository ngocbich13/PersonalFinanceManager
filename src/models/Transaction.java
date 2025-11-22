package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Lớp trừu tượng đại diện cho một giao dịch tài chính
 * Đây là lớp cha cho Income và Expense
 */
public abstract class Transaction {
    // Thuộc tính protected để các lớp con có thể truy cập
    protected String id;              // Mã giao dịch duy nhất
    protected LocalDate date;         // Ngày thực hiện giao dịch
    protected double amount;          // Số tiền
    protected Category category;      // Danh mục của giao dịch
    protected String note;            // Ghi chú
    
    /**
     * Constructor khởi tạo giao dịch
     */
    public Transaction(String id, LocalDate date, double amount, Category category, String note) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.note = note;
    }
    
    /**
     * Phương thức trừu tượng - mỗi loại giao dịch sẽ implement riêng
     * @return Loại giao dịch: "THU" hoặc "CHI"
     */
    public abstract String getType();
    
    // ===== GETTERS =====
    public String getId() { 
        return id; 
    }
    
    public LocalDate getDate() { 
        return date; 
    }
    
    public double getAmount() { 
        return amount; 
    }
    
    public Category getCategory() { 
        return category; 
    }
    
    public String getNote() { 
        return note; 
    }
    
    // ===== SETTERS =====
    public void setAmount(double amount) { 
        this.amount = amount; 
    }
    
    public void setCategory(Category category) { 
        this.category = category; 
    }
    
    public void setNote(String note) { 
        this.note = note; 
    }
    
    /**
     * Chuyển đổi dữ liệu thành chuỗi để lưu vào file
     * Format: TYPE|ID|DATE|AMOUNT|CATEGORY|NOTE
     */
    public String toFileString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s|%s|%s|%.2f|%s|%s",
            getType(), 
            id, 
            date.format(formatter), 
            amount, 
            category.getName(), 
            note);
    }
    
    /**
     * Hiển thị thông tin giao dịch dạng dễ đọc
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("[%s] %s | %s | %,.0f VND | %s | %s",
            getType(), 
            id, 
            date.format(formatter), 
            amount, 
            category.getName(), 
            note);
    }
}