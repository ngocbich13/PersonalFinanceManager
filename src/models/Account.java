package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp quản lý tài khoản và các giao dịch
 * Đây là lớp trung tâm chứa tất cả dữ liệu
 */
public class Account {
    private String name;                        // Tên tài khoản
    private double balance;                     // Số dư hiện tại
    private List<Transaction> transactions;     // Danh sách giao dịch
    private List<Category> categories;          // Danh sách danh mục
    
    /**
     * Constructor khởi tạo tài khoản
     */
    public Account(String name, double initialBalance) {
        this.name = name;
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
        initDefaultCategories();
    }
    
    /**
     * Khởi tạo các danh mục mặc định khi tạo tài khoản mới
     */
    private void initDefaultCategories() {
        // Danh mục thu nhập
        categories.add(new Category("Lương", "THU"));
        categories.add(new Category("Thưởng", "THU"));
        categories.add(new Category("Thu nhập phụ", "THU"));
        categories.add(new Category("Khác", "THU"));
        
        // Danh mục chi tiêu (có ngân sách mặc định)
        categories.add(new Category("Ăn uống", "CHI", 2000000));
        categories.add(new Category("Học tập", "CHI", 1000000));
        categories.add(new Category("Giải trí", "CHI", 500000));
        categories.add(new Category("Di chuyển", "CHI", 500000));
        categories.add(new Category("Mua sắm", "CHI", 1000000));
        categories.add(new Category("Sức khỏe", "CHI", 500000));
        categories.add(new Category("Khác", "CHI"));
    }
    
    /**
     * Thêm giao dịch mới và cập nhật số dư
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        
        // Cập nhật số dư dựa trên loại giao dịch
        if (transaction instanceof Income) {
            balance += transaction.getAmount();
        } else if (transaction instanceof Expense) {
            balance -= transaction.getAmount();
        }
    }
    
    /**
     * Xóa giao dịch theo ID và hoàn lại số dư
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    public boolean removeTransaction(String id) {
        Transaction trans = findTransactionById(id);
        if (trans != null) {
            // Hoàn lại số dư
            if (trans instanceof Income) {
                balance -= trans.getAmount();
            } else {
                balance += trans.getAmount();
            }
            transactions.remove(trans);
            return true;
        }
        return false;
    }
    
    /**
     * Tìm giao dịch theo ID
     * @return Transaction nếu tìm thấy, null nếu không
     */
    public Transaction findTransactionById(String id) {
        return transactions.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Lấy danh mục theo tên (không phân biệt hoa thường)
     */
    public Category getCategoryByName(String name) {
        return categories.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Lấy tất cả giao dịch trong khoảng thời gian
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate from, LocalDate to) {
        return transactions.stream()
            .filter(t -> !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy giao dịch theo danh mục
     */
    public List<Transaction> getTransactionsByCategory(String categoryName) {
        return transactions.stream()
            .filter(t -> t.getCategory().getName().equalsIgnoreCase(categoryName))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách giao dịch thu nhập
     */
    public List<Transaction> getIncomeTransactions() {
        return transactions.stream()
            .filter(t -> t instanceof Income)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách giao dịch chi tiêu
     */
    public List<Transaction> getExpenseTransactions() {
        return transactions.stream()
            .filter(t -> t instanceof Expense)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách danh mục theo loại
     */
    public List<Category> getCategoriesByType(String type) {
        return categories.stream()
            .filter(c -> c.getType().equals(type))
            .collect(Collectors.toList());
    }
    
    /**
     * Tạo ID tự động cho giao dịch mới
     * Format: T0001, T0002, T0003...
     */
    public String generateTransactionId() {
        return "T" + String.format("%04d", transactions.size() + 1);
    }
    
    /**
     * Thêm danh mục mới
     */
    public void addCategory(Category category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }
    
    // ===== GETTERS =====
    public String getName() { 
        return name; 
    }
    
    public double getBalance() { 
        return balance; 
    }
    
    public List<Transaction> getTransactions() { 
        return new ArrayList<>(transactions); // Trả về bản sao để bảo vệ dữ liệu
    }
    
    public List<Category> getCategories() { 
        return new ArrayList<>(categories); 
    }
    
    // ===== SETTERS =====
    public void setName(String name) { 
        this.name = name; 
    }
    
    /**
     * Set balance trực tiếp (dùng khi load từ file)
     */
    public void setBalance(double balance) { 
        this.balance = balance; 
    }
    
    /**
     * Hiển thị thông tin tài khoản
     */
    @Override
    public String toString() {
        return String.format("Tài khoản: %s | Số dư: %,.0f VND | Giao dịch: %d", 
            name, balance, transactions.size());
    }
}