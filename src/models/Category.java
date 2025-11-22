package models;

/**
 * Lớp đại diện cho danh mục giao dịch
 * VD: Ăn uống, Học tập, Lương, Thưởng...
 */
public class Category {
    private String name;        // Tên danh mục
    private String type;        // Loại: "THU" hoặc "CHI"
    private double budget;      // Ngân sách cho danh mục (chỉ dùng cho CHI)
    
    /**
     * Constructor đầy đủ
     */
    public Category(String name, String type, double budget) {
        this.name = name;
        this.type = type;
        this.budget = budget;
    }
    
    /**
     * Constructor không có ngân sách (mặc định = 0)
     */
    public Category(String name, String type) {
        this(name, type, 0);
    }
    
    // ===== GETTERS =====
    public String getName() { 
        return name; 
    }
    
    public String getType() { 
        return type; 
    }
    
    public double getBudget() { 
        return budget; 
    }
    
    // ===== SETTERS =====
    public void setName(String name) { 
        this.name = name; 
    }
    
    public void setBudget(double budget) { 
        this.budget = budget; 
    }
    
    /**
     * Kiểm tra xem danh mục có ngân sách hay không
     */
    public boolean hasBudget() {
        return budget > 0;
    }
    
    /**
     * Hiển thị thông tin danh mục
     */
    @Override
    public String toString() {
        if (hasBudget()) {
            return String.format("%s (%s) - Ngân sách: %,.0f VND", name, type, budget);
        }
        return String.format("%s (%s)", name, type);
    }
    
    /**
     * So sánh 2 danh mục dựa trên tên (không phân biệt hoa thường)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return name.equalsIgnoreCase(category.name);
    }
    
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}