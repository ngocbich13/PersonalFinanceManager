package services;

import models.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý báo cáo và thống kê
 */
public class ReportService {
    private Account account;
    
    public ReportService(Account account) {
        this.account = account;
    }
    
    /**
     * Tính tổng thu nhập trong khoảng thời gian
     */
    public double getTotalIncome(LocalDate from, LocalDate to) {
        return account.getTransactionsByDateRange(from, to).stream()
            .filter(t -> t instanceof Income)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    /**
     * Tính tổng chi tiêu trong khoảng thời gian
     */
    public double getTotalExpense(LocalDate from, LocalDate to) {
        return account.getTransactionsByDateRange(from, to).stream()
            .filter(t -> t instanceof Expense)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    /**
     * Tính chênh lệch (thu - chi)
     */
    public double getNetAmount(LocalDate from, LocalDate to) {
        return getTotalIncome(from, to) - getTotalExpense(from, to);
    }
    
    /**
     * Tạo báo cáo tổng quan theo khoảng thời gian
     */
    public String generateSummaryReport(LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        double totalIncome = getTotalIncome(from, to);
        double totalExpense = getTotalExpense(from, to);
        double netAmount = totalIncome - totalExpense;
        
        sb.append("\n================================================\n");
        sb.append(String.format("  BAO CAO TU %s DEN %s\n", 
            from.format(formatter), to.format(formatter)));
        sb.append("================================================\n");
        sb.append(String.format("  Tong thu:         %,15.0f VND\n", totalIncome));
        sb.append(String.format("  Tong chi:         %,15.0f VND\n", totalExpense));
        sb.append("------------------------------------------------\n");
        
        String status = netAmount >= 0 ? "Thang du:  " : "Tham hut: ";
        sb.append(String.format("  %s      %,15.0f VND\n", status, Math.abs(netAmount)));
        sb.append(String.format("  So du hien tai:   %,15.0f VND\n", account.getBalance()));
        sb.append("================================================\n");
        
        return sb.toString();
    }
    
    /**
     * Tạo báo cáo chi tiết theo danh mục
     */
    public String generateCategoryReport(LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n================================================\n");
        sb.append("        BAO CAO CHI TIEU THEO DANH MUC\n");
        sb.append("================================================\n");
        
        // Nhóm chi tiêu theo danh mục
        Map<String, Double> expenseByCategory = account.getTransactionsByDateRange(from, to).stream()
            .filter(t -> t instanceof Expense)
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        // Tính tổng chi tiêu
        double totalExpense = expenseByCategory.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        // Hiển thị từng danh mục
        for (Category category : account.getCategoriesByType("CHI")) {
            double spent = expenseByCategory.getOrDefault(category.getName(), 0.0);
            double budget = category.getBudget();
            
            sb.append(String.format("  %-15s: %,12.0f VND", 
                category.getName(), spent));
            
            // Hiển thị ngân sách và cảnh báo
            if (budget > 0) {
                double percentage = (spent / budget) * 100;
                sb.append(String.format(" / %,10.0f VND", budget));
                
                if (percentage > 100) {
                    sb.append(" [VUOT!]");
                } else if (percentage > 80) {
                    sb.append(" [Gan dat]");
                } else {
                    sb.append(" [OK]");
                }
            }
            
            // Hiển thị phần trăm so với tổng chi tiêu
            if (totalExpense > 0) {
                double percentOfTotal = (spent / totalExpense) * 100;
                sb.append(String.format(" (%.1f%%)", percentOfTotal));
            }
            
            sb.append("\n");
        }
        
        sb.append("================================================\n");
        
        return sb.toString();
    }
    
    /**
     * Kiểm tra và trả về danh sách cảnh báo ngân sách
     */
    public List<String> checkBudgetWarnings(LocalDate from, LocalDate to) {
        List<String> warnings = new ArrayList<>();
        
        for (Category category : account.getCategoriesByType("CHI")) {
            if (category.hasBudget()) {
                double spent = account.getTransactionsByDateRange(from, to).stream()
                    .filter(t -> t instanceof Expense)
                    .filter(t -> t.getCategory().equals(category))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
                
                double percentage = (spent / category.getBudget()) * 100;
                
                if (percentage > 100) {
                    warnings.add(String.format("⚠️  '%s' vượt %.0f%% ngân sách", 
                        category.getName(), percentage - 100));
                } else if (percentage > 80) {
                    warnings.add(String.format("⚡ '%s' đã dùng %.0f%% ngân sách", 
                        category.getName(), percentage));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Tạo báo cáo top danh mục chi tiêu nhiều nhất
     */
    public String generateTopExpenseCategories(LocalDate from, LocalDate to, int topN) {
        Map<String, Double> expenseByCategory = account.getTransactionsByDateRange(from, to).stream()
            .filter(t -> t instanceof Expense)
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        // Sắp xếp và lấy top N
        List<Map.Entry<String, Double>> topCategories = expenseByCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(topN)
            .collect(Collectors.toList());
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n🏆 TOP %d DANH MỤC CHI NHIỀU NHẤT:\n", topN));
        
        int rank = 1;
        for (Map.Entry<String, Double> entry : topCategories) {
            sb.append(String.format("  %d. %-15s: %,12.0f VND\n", 
                rank++, entry.getKey(), entry.getValue()));
        }
        
        return sb.toString();
    }
    
    /**
     * Export dữ liệu sang định dạng CSV
     */
    public String exportToCSV(LocalDate from, LocalDate to) {
        StringBuilder csv = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Header
        csv.append("Loại,ID,Ngày,Số tiền (VND),Danh mục,Ghi chú\n");
        
        // Sắp xếp theo ngày
        List<Transaction> sortedTransactions = account.getTransactionsByDateRange(from, to);
        sortedTransactions.sort(Comparator.comparing(Transaction::getDate));
        
        // Data rows
        for (Transaction t : sortedTransactions) {
            csv.append(String.format("%s,%s,%s,%.0f,%s,\"%s\"\n",
                t.getType(),
                t.getId(),
                t.getDate().format(formatter),
                t.getAmount(),
                t.getCategory().getName(),
                t.getNote().replace("\"", "\"\"")));  // Escape quotes trong CSV
        }
        
        return csv.toString();
    }
    
    /**
     * Lấy thống kê tổng quan
     */
    public Map<String, Object> getStatistics(LocalDate from, LocalDate to) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Transaction> transactions = account.getTransactionsByDateRange(from, to);
        
        stats.put("totalTransactions", transactions.size());
        stats.put("totalIncome", getTotalIncome(from, to));
        stats.put("totalExpense", getTotalExpense(from, to));
        stats.put("netAmount", getNetAmount(from, to));
        stats.put("averageExpensePerDay", getTotalExpense(from, to) / (to.toEpochDay() - from.toEpochDay() + 1));
        
        return stats;
    }
}