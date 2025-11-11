package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class quản lý kết nối SQL Server cho toàn bộ ứng dụng
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    // Thông tin kết nối SQL Server (SQL Authentication - user/password)
    private static final String DB_URL = "jdbc:sqlserver://KIENDOAN:1433;databaseName=ParkingLot;encrypt=false;trustServerCertificate=true;";
    private static final String DB_USER = "kiendt";
    private static final String DB_PASSWORD = "123456";

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Kết nối SQL Server thành công (SQL Authentication)!");
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối SQL Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy instance duy nhất của DatabaseConnection (Singleton)
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Lấy connection hiện tại (không tạo kết nối lại, chỉ trả về connection đã có)
     */
    public Connection getConnection() {
        if (connection == null) {
            System.err.println("❌ Lỗi: Chưa khởi tạo kết nối! Gọi DatabaseConnection.getInstance() từ Main.java trước!");
        }
        return connection;
    }

    /**
     * Đóng kết nối
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Kết nối SQL Server đã đóng!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đóng kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra xem kết nối có sẵn không
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}


