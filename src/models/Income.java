package models;

import java.time.LocalDate;

/**
 * Lớp đại diện cho giao dịch thu nhập
 * Kế thừa từ Transaction
 */
public class Income extends Transaction {
    
    /**
     * Constructor khởi tạo thu nhập
     */
    public Income(String id, LocalDate date, double amount, Category category, String note) {
        super(id, date, amount, category, note);
    }
    
    /**
     * Override phương thức trừu tượng từ lớp cha
     * @return "THU" để phân biệt với chi tiêu
     */
    @Override
    public String getType() {
        return "THU";
    }
}