package services;

import models.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service xử lý lưu trữ và đọc dữ liệu từ file
 */
public class FileManager {
    private static final String DATA_DIR = "data";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.txt";
    private static final String ACCOUNT_FILE = DATA_DIR + "/account.txt";
    private static final String EXPORT_DIR = "exports";
    
    /**
     * Constructor - tạo thư mục data nếu chưa có
     */
    public FileManager() {
        createDirectoryIfNotExists(DATA_DIR);
    }
    
    /**
     * Tạo thư mục nếu chưa tồn tại
     */
    private void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }
    
    /**
     * Lưu toàn bộ dữ liệu tài khoản vào file
     */
    public void saveData(Account account) {
        try {
            // Lưu thông tin tài khoản
            saveAccountInfo(account);
            
            // Lưu danh sách giao dịch
            saveTransactions(account);
            
            System.out.println("✓ Đã lưu dữ liệu thành công!");
            
        } catch (IOException e) {
            System.err.println("✗ Lỗi khi lưu dữ liệu: " + e.getMessage());
        }
    }
    
    /**
     * Lưu thông tin tài khoản (tên và số dư)
     */
    private void saveAccountInfo(Account account) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ACCOUNT_FILE))) {
            writer.println(account.getName());
            writer.println(account.getBalance());
        }
    }
    
    /**
     * Lưu danh sách giao dịch
     * Format: TYPE|ID|DATE|AMOUNT|CATEGORY|NOTE
     */
    private void saveTransactions(Account account) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction t : account.getTransactions()) {
                writer.println(t.toFileString());
            }
        }
    }
    
    /**
     * Đọc dữ liệu từ file và khôi phục tài khoản
     */
    public Account loadData() {
        Account account = null;
        
        try {
            // Đọc thông tin tài khoản
            account = loadAccountInfo();
            
            // Đọc danh sách giao dịch
            loadTransactions(account);
            
            System.out.println("✓ Đã tải dữ liệu thành công!");
            System.out.println("  " + account);
            
        } catch (FileNotFoundException e) {
            System.out.println("⚠️  Chưa có dữ liệu cũ, tạo tài khoản mới.");
            account = new Account("Tài khoản của tôi", 0);
            
        } catch (IOException e) {
            System.err.println("✗ Lỗi khi đọc dữ liệu: " + e.getMessage());
            account = new Account("Tài khoản của tôi", 0);
        }
        
        return account;
    }
    
    /**
     * Đọc thông tin tài khoản từ file
     */
    private Account loadAccountInfo() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String name = reader.readLine();
            double balance = Double.parseDouble(reader.readLine());
            
            Account account = new Account(name, 0);  // Khởi tạo với balance = 0
            account.setBalance(balance);  // Set balance từ file
            
            return account;
        }
    }
    
    /**
     * Đọc danh sách giao dịch từ file
     */
    private void loadTransactions(Account account) throws IOException {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) {
            return;  // Không có file giao dịch
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                try {
                    Transaction transaction = parseTransactionFromLine(line, formatter, account);
                    if (transaction != null) {
                        // Thêm trực tiếp vào danh sách mà không cập nhật balance
                        // (vì balance đã được load từ account.txt)
                        account.getTransactions().add(transaction);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️  Bỏ qua dòng lỗi: " + line);
                }
            }
        }
    }
    
    /**
     * Parse một dòng text thành Transaction object
     * Format: TYPE|ID|DATE|AMOUNT|CATEGORY|NOTE
     */
    private Transaction parseTransactionFromLine(String line, DateTimeFormatter formatter, Account account) {
        String[] parts = line.split("\\|");
        
        if (parts.length < 6) {
            return null;
        }
        
        String type = parts[0];
        String id = parts[1];
        LocalDate date = LocalDate.parse(parts[2], formatter);
        double amount = Double.parseDouble(parts[3]);
        String categoryName = parts[4];
        String note = parts[5];
        
        // Lấy hoặc tạo category
        Category category = account.getCategoryByName(categoryName);
        if (category == null) {
            category = new Category(categoryName, type);
            account.addCategory(category);
        }
        
        // Tạo transaction tương ứng
        if (type.equals("THU")) {
            return new Income(id, date, amount, category, note);
        } else {
            return new Expense(id, date, amount, category, note);
        }
    }
    
    /**
     * Export báo cáo ra file CSV
     */
    public void exportCSV(String csvContent, String fileName) {
        try {
            createDirectoryIfNotExists(EXPORT_DIR);
            
            String filePath = EXPORT_DIR + "/" + fileName;
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.print(csvContent);
            }
            
            System.out.println("✓ Đã export file: " + filePath);
            
        } catch (IOException e) {
            System.err.println("✗ Lỗi khi export CSV: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xem có file dữ liệu cũ hay không
     */
    public boolean hasExistingData() {
        File accountFile = new File(ACCOUNT_FILE);
        File transactionFile = new File(TRANSACTIONS_FILE);
        return accountFile.exists() && transactionFile.exists();
    }
    
    /**
     * Xóa toàn bộ dữ liệu (reset)
     */
    public boolean clearAllData() {
        boolean success = true;
        
        File accountFile = new File(ACCOUNT_FILE);
        if (accountFile.exists()) {
            success &= accountFile.delete();
        }
        
        File transactionFile = new File(TRANSACTIONS_FILE);
        if (transactionFile.exists()) {
            success &= transactionFile.delete();
        }
        
        return success;
    }
    
    /**
     * Tạo bản backup dữ liệu
     */
    public void createBackup() {
        try {
            String timestamp = LocalDate.now().toString();
            
            // Backup account
            copyFile(ACCOUNT_FILE, DATA_DIR + "/account_backup_" + timestamp + ".txt");
            
            // Backup transactions
            copyFile(TRANSACTIONS_FILE, DATA_DIR + "/transactions_backup_" + timestamp + ".txt");
            
            System.out.println("✓ Đã tạo bản backup!");
            
        } catch (IOException e) {
            System.err.println("✗ Lỗi khi tạo backup: " + e.getMessage());
        }
    }
    
    /**
     * Copy file từ source sang destination
     */
    private void copyFile(String source, String destination) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(source));
             PrintWriter writer = new PrintWriter(new FileWriter(destination))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
        }
    }
}