package ui;

/**
 * Class chính để khởi động ứng dụng
 * Quản lý chi tiêu cá nhân - Personal Finance Manager
 * 
 * @author Your Name
 * @version 1.0
 */
public class Main {
    
    /**
     * Phương thức main - điểm bắt đầu của chương trình
     */
    public static void main(String[] args) {
        // Khởi tạo và chạy giao diện console
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}