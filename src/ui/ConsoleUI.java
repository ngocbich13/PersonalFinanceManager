package ui;

import models.*;
import services.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Giao diện console đơn giản cho ứng dụng quản lý chi tiêu
 * Sử dụng ASCII thuần để tương thích với mọi font
 */
public class ConsoleUI {
    private Scanner scanner;
    private Account account;
    private ReportService reportService;
    private FileManager fileManager;
    private DateTimeFormatter dateFormatter;
    
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.fileManager = new FileManager();
        this.account = fileManager.loadData();
        this.reportService = new ReportService(account);
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
    
    public void start() {
        showWelcome();
        
        while (true) {
            showMenu();
            int choice = getIntInput("\n>> Chon chuc nang (0-7): ");
            
            System.out.println();
            
            switch (choice) {
                case 1: addTransaction(); break;
                case 2: viewTransactions(); break;
                case 3: viewReport(); break;
                case 4: searchTransactions(); break;
                case 5: deleteTransaction(); break;
                case 6: exportReport(); break;
                case 7: manageBudget(); break;
                case 0: 
                    exitApp();
                    return;
                default:
                    System.out.println("[X] Lua chon khong hop le!");
            }
            
            pauseScreen();
        }
    }
    
    private void showWelcome() {
        System.out.println("\n================================================");
        System.out.println("   CHAO MUNG DEN VOI QUAN LY CHI TIEU CA NHAN");
        System.out.println("================================================");
    }
    
    private void showMenu() {
        System.out.println("\n========================================");
        System.out.println("            MENU CHINH");
        System.out.println("========================================");
        System.out.printf("  So du:  %,20.0f VND\n", account.getBalance());
        System.out.println("========================================");
        System.out.println("  1. Them giao dich moi");
        System.out.println("  2. Xem danh sach giao dich");
        System.out.println("  3. Xem bao cao tong quan");
        System.out.println("  4. Tim kiem giao dich");
        System.out.println("  5. Xoa giao dich");
        System.out.println("  6. Export bao cao CSV");
        System.out.println("  7. Quan ly ngan sach");
        System.out.println("  0. Luu va thoat");
        System.out.println("========================================");
    }
    
    private void addTransaction() {
        System.out.println("\n========== THEM GIAO DICH MOI ==========");
        
        System.out.println("\nChon loai giao dich:");
        System.out.println("  1. Thu nhap");
        System.out.println("  2. Chi tieu");
        int type = getIntInput(">> Loai: ");
        
        if (type != 1 && type != 2) {
            System.out.println("[X] Loai khong hop le!");
            return;
        }
        
        String transType = (type == 1) ? "THU" : "CHI";
        
        double amount = getDoubleInput(">> So tien (VND): ");
        if (amount <= 0) {
            System.out.println("[X] So tien phai lon hon 0!");
            return;
        }
        
        Category category = selectCategory(transType);
        if (category == null) {
            return;
        }
        
        LocalDate date = getDateInput(">> Ngay (dd/MM/yyyy, Enter = hom nay): ", true);
        
        System.out.print(">> Ghi chu: ");
        String note = scanner.nextLine().trim();
        if (note.isEmpty()) {
            note = "Khong co ghi chu";
        }
        
        String id = account.generateTransactionId();
        Transaction transaction;
        
        if (type == 1) {
            transaction = new Income(id, date, amount, category, note);
        } else {
            transaction = new Expense(id, date, amount, category, note);
        }
        
        account.addTransaction(transaction);
        fileManager.saveData(account);
        
        System.out.println("\n[OK] Da them giao dich thanh cong!");
        System.out.println("---------------------------------------");
        System.out.println(transaction);
        System.out.printf("So du moi: %,.0f VND\n", account.getBalance());
        
        if (type == 2) {
            checkBudgetAlert(category, date);
        }
    }
    
    private Category selectCategory(String type) {
        System.out.println("\nDanh muc co san:");
        
        List<Category> categories = account.getCategoriesByType(type);
        
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, categories.get(i).getName());
        }
        
        int choice = getIntInput(">> Chon danh muc: ");
        
        if (choice < 1 || choice > categories.size()) {
            System.out.println("[X] Danh muc khong hop le!");
            return null;
        }
        
        return categories.get(choice - 1);
    }
    
    private void viewTransactions() {
        System.out.println("\n========== DANH SACH GIAO DICH ==========");
        
        List<Transaction> transactions = account.getTransactions();
        
        if (transactions.isEmpty()) {
            System.out.println("\nChua co giao dich nao.");
            return;
        }
        
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        
        System.out.println("\nTong so: " + transactions.size() + " giao dich");
        System.out.println("---------------------------------------");
        
        for (Transaction t : transactions) {
            System.out.println(t);
        }
        
        System.out.println("---------------------------------------");
    }
    
    private void viewReport() {
        System.out.println("\n========== BAO CAO TONG QUAN ==========");
        
        System.out.println("\nChon khoang thoi gian:");
        System.out.println("  1. Thang nay");
        System.out.println("  2. Thang truoc");
        System.out.println("  3. Tuy chinh");
        
        int choice = getIntInput(">> Chon: ");
        
        LocalDate from, to;
        
        switch (choice) {
            case 1:
                YearMonth thisMonth = YearMonth.now();
                from = thisMonth.atDay(1);
                to = thisMonth.atEndOfMonth();
                break;
                
            case 2:
                YearMonth lastMonth = YearMonth.now().minusMonths(1);
                from = lastMonth.atDay(1);
                to = lastMonth.atEndOfMonth();
                break;
                
            case 3:
                from = getDateInput(">> Tu ngay (dd/MM/yyyy): ", false);
                to = getDateInput(">> Den ngay (dd/MM/yyyy): ", false);
                break;
                
            default:
                System.out.println("[X] Lua chon khong hop le!");
                return;
        }
        
        System.out.println(reportService.generateSummaryReport(from, to));
        System.out.println(reportService.generateCategoryReport(from, to));
        
        List<String> warnings = reportService.checkBudgetWarnings(from, to);
        if (!warnings.isEmpty()) {
            System.out.println("\n[!] CANH BAO NGAN SACH:");
            for (String warning : warnings) {
                System.out.println("  " + warning);
            }
        }
    }
    
    private void searchTransactions() {
        System.out.println("\n========== TIM KIEM GIAO DICH ==========");
        
        System.out.println("\nChon cach tim kiem:");
        System.out.println("  1. Theo danh muc");
        System.out.println("  2. Theo khoang thoi gian");
        System.out.println("  3. Theo loai (Thu/Chi)");
        
        int choice = getIntInput(">> Chon: ");
        List<Transaction> results = new ArrayList<>();
        
        switch (choice) {
            case 1:
                System.out.print(">> Ten danh muc: ");
                String catName = scanner.nextLine().trim();
                results = account.getTransactionsByCategory(catName);
                break;
                
            case 2:
                LocalDate from = getDateInput(">> Tu ngay (dd/MM/yyyy): ", false);
                LocalDate to = getDateInput(">> Den ngay (dd/MM/yyyy): ", false);
                results = account.getTransactionsByDateRange(from, to);
                break;
                
            case 3:
                System.out.println("  1. Thu nhap");
                System.out.println("  2. Chi tieu");
                int type = getIntInput(">> Chon: ");
                if (type == 1) {
                    results = account.getIncomeTransactions();
                } else if (type == 2) {
                    results = account.getExpenseTransactions();
                }
                break;
                
            default:
                System.out.println("[X] Lua chon khong hop le!");
                return;
        }
        
        if (results.isEmpty()) {
            System.out.println("\nKhong tim thay giao dich nao.");
        } else {
            System.out.println("\n[OK] Tim thay " + results.size() + " giao dich:");
            System.out.println("---------------------------------------");
            for (Transaction t : results) {
                System.out.println(t);
            }
        }
    }
    
    private void deleteTransaction() {
        System.out.println("\n========== XOA GIAO DICH ==========");
        
        System.out.print(">> Nhap ID giao dich can xoa: ");
        String id = scanner.nextLine().trim().toUpperCase();
        
        Transaction trans = account.findTransactionById(id);
        
        if (trans == null) {
            System.out.println("[X] Khong tim thay giao dich voi ID: " + id);
            return;
        }
        
        System.out.println("\nGiao dich se bi xoa:");
        System.out.println("---------------------------------------");
        System.out.println(trans);
        System.out.println("---------------------------------------");
        
        System.out.print("\n[!] Xac nhan xoa? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            account.removeTransaction(id);
            fileManager.saveData(account);
            System.out.println("[OK] Da xoa giao dich thanh cong!");
            System.out.printf("So du moi: %,.0f VND\n", account.getBalance());
        } else {
            System.out.println("[X] Da huy thao tac xoa.");
        }
    }
    
    private void exportReport() {
        System.out.println("\n========== EXPORT BAO CAO CSV ==========");
        
        LocalDate from = getDateInput(">> Tu ngay (dd/MM/yyyy): ", false);
        LocalDate to = getDateInput(">> Den ngay (dd/MM/yyyy): ", false);
        
        String csv = reportService.exportToCSV(from, to);
        String fileName = "report_" + LocalDate.now() + ".csv";
        
        fileManager.exportCSV(csv, fileName);
        
        System.out.println("File da duoc luu tai: exports/" + fileName);
    }
    
    private void manageBudget() {
        System.out.println("\n========== QUAN LY NGAN SACH ==========");
        
        List<Category> expenseCategories = account.getCategoriesByType("CHI");
        
        System.out.println("\nDanh muc chi tieu:");
        for (int i = 0; i < expenseCategories.size(); i++) {
            Category cat = expenseCategories.get(i);
            System.out.printf("  %d. %-15s - Ngan sach: %,12.0f VND\n", 
                i + 1, cat.getName(), cat.getBudget());
        }
        
        int choice = getIntInput("\n>> Chon danh muc de cap nhat (0 = Huy): ");
        
        if (choice == 0) {
            return;
        }
        
        if (choice < 1 || choice > expenseCategories.size()) {
            System.out.println("[X] Lua chon khong hop le!");
            return;
        }
        
        Category category = expenseCategories.get(choice - 1);
        System.out.printf("\nNgan sach hien tai: %,.0f VND\n", category.getBudget());
        
        double newBudget = getDoubleInput(">> Ngan sach moi (VND): ");
        
        if (newBudget < 0) {
            System.out.println("[X] Ngan sach khong hop le!");
            return;
        }
        
        category.setBudget(newBudget);
        fileManager.saveData(account);
        System.out.println("[OK] Da cap nhat ngan sach thanh cong!");
    }
    
    private void checkBudgetAlert(Category category, LocalDate date) {
        if (!category.hasBudget()) {
            return;
        }
        
        YearMonth month = YearMonth.from(date);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        
        double spent = account.getTransactionsByDateRange(from, to).stream()
            .filter(t -> t instanceof Expense)
            .filter(t -> t.getCategory().equals(category))
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double percentage = (spent / category.getBudget()) * 100;
        
        if (percentage > 100) {
            System.out.printf("\n[!] CANH BAO: Danh muc '%s' da vuot %.0f%% ngan sach!\n", 
                category.getName(), percentage - 100);
        } else if (percentage > 80) {
            System.out.printf("\n[!] CHU Y: Danh muc '%s' da dung %.0f%% ngan sach.\n", 
                category.getName(), percentage);
        }
    }
    
    private void exitApp() {
        System.out.println("Dang luu du lieu...");
        fileManager.saveData(account);
        System.out.println("\nCam on ban da su dung! Hen gap lai!");
    }
    
    private void pauseScreen() {
        System.out.print("\nNhan Enter de tiep tuc...");
        scanner.nextLine();
    }
    
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[X] Vui long nhap so nguyen hop le!");
            }
        }
    }
    
    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("[X] Vui long nhap so hop le!");
            }
        }
    }
    
    private LocalDate getDateInput(String prompt, boolean allowEmpty) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                
                if (allowEmpty && input.isEmpty()) {
                    return LocalDate.now();
                }
                
                return LocalDate.parse(input, dateFormatter);
                
            } catch (DateTimeParseException e) {
                System.out.println("[X] Dinh dang ngay khong hop le! Dung: dd/MM/yyyy");
            }
        }
    }
}