package models;

import java.time.LocalDate;

/**
 * Lớp đại diện cho giao dịch chi tiêu
 * Kế thừa từ Transaction
 */
public class Expense extends Transaction {
    
    /**
     * Constructor khởi tạo chi tiêu
     */
    public Expense(String id, LocalDate date, double amount, Category category, String note) {
        super(id, date, amount, category, note);
    }
    
    /**
     * Override phương thức trừu tượng từ lớp cha
     * @return "CHI" để phân biệt với thu nhập
     */
    @Override
    public String getType() {
        return "CHI";
    }
}