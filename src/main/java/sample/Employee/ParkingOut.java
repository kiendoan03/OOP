package sample.Employee;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import sample.DatabaseConnection;

public class ParkingOut extends EmployeeUI implements Initializable {

    @FXML private ComboBox<String> loaiTheBox;
    @FXML private ComboBox<String> maTheBox;
    @FXML private Label loaiXeLabel;
    @FXML private Label bienSoXeLabel;
    @FXML private Label gioVaoLabel;
    @FXML private Label nhanVienLabel;
    @FXML private Button deleteVehicle;
    @FXML private Button lamMoi;

    private Connection connection;

    public void initializeDatabaseConnection() {
        connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            System.out.println("✅ Sử dụng kết nối SQL Server từ DatabaseConnection!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Thông báo", "Lỗi", "Không thể lấy kết nối database!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeDatabaseConnection();

        ObservableList<String> options = FXCollections.observableArrayList("Thẻ ngày", "Thẻ tháng");
        loaiTheBox.setItems(options);

        loaiTheBox.setOnAction(event -> handleLoaiTheChange());
        maTheBox.setOnAction(event -> handleMaTheChange());
    }

    private void handleLoaiTheChange() {
        String selectedValue = loaiTheBox.getValue();
        maTheBox.getItems().clear();
        clearLabels();

        if (selectedValue == null) return;

        try {
            if ("Thẻ ngày".equals(selectedValue)) {
                // Lấy danh sách thẻ ngày đang được sử dụng (có gio_vao, chưa có gio_ra)
                String query = "SELECT tn.the_ngay_id, t.bien_so_xe " +
                        "FROM the_ngay tn " +
                        "JOIN the t ON tn.the_id = t.the_id " +
                        "WHERE tn.gio_vao IS NOT NULL AND tn.gio_ra IS NULL";

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                ObservableList<String> options3 = FXCollections.observableArrayList();
                while (resultSet.next()) {
                    String display = "N" + resultSet.getInt("the_ngay_id") +
                            " - " + resultSet.getString("bien_so_xe");
                    options3.add(display);
                }
                maTheBox.setItems(options3);
                statement.close();

            } else if ("Thẻ tháng".equals(selectedValue)) {
                // Lấy danh sách thẻ tháng đang được sử dụng
                String query = "SELECT tt.the_thang_id, t.bien_so_xe, tt.ho_ten_khach_hang " +
                        "FROM the_thang tt " +
                        "JOIN the t ON tt.the_id = t.the_id " +
                        "WHERE tt.the_id IS NOT NULL";

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                ObservableList<String> options4 = FXCollections.observableArrayList();
                while (resultSet.next()) {
                    String display = "T" + resultSet.getInt("the_thang_id") +
                            " - " + resultSet.getString("bien_so_xe") +
                            " (" + resultSet.getString("ho_ten_khach_hang") + ")";
                    options4.add(display);
                }
                maTheBox.setItems(options4);
                statement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Thông báo", "Lỗi", "Không thể tải danh sách: " + e.getMessage());
        }
    }

    private void handleMaTheChange() {
        String selectedValue = maTheBox.getValue();
        if (selectedValue == null) return;

        String loaiThe = loaiTheBox.getValue();

        try {
            if ("Thẻ ngày".equals(loaiThe)) {
                // Lấy the_ngay_id từ chuỗi "N123 - 29A12345"
                int theNgayId = Integer.parseInt(selectedValue.split(" - ")[0].substring(1));

                String query = "SELECT t.bien_so_xe, lx.ten_loai, tn.gio_vao, nv.hoten " +
                        "FROM the_ngay tn " +
                        "JOIN the t ON tn.the_id = t.the_id " +
                        "JOIN loai_xe lx ON t.loai_xe_id = lx.loai_xe_id " +
                        "JOIN nhan_vien nv ON t.nhan_vien_id = nv.nhanvien_id " +
                        "WHERE tn.the_ngay_id = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, theNgayId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    bienSoXeLabel.setText(resultSet.getString("bien_so_xe"));
                    loaiXeLabel.setText(resultSet.getString("ten_loai"));

                    Timestamp gioVao = resultSet.getTimestamp("gio_vao");
                    if (gioVao != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        gioVaoLabel.setText(gioVao.toLocalDateTime().format(formatter));
                    }

                    nhanVienLabel.setText(resultSet.getString("hoten"));
                }
                statement.close();

            } else if ("Thẻ tháng".equals(loaiThe)) {
                // Lấy the_thang_id từ chuỗi "T123 - 29A12345 (Nguyen Van A)"
                int theThangId = Integer.parseInt(selectedValue.split(" - ")[0].substring(1));

                String query = "SELECT t.bien_so_xe, lx.ten_loai, tt.ho_ten_khach_hang, " +
                        "tt.ngay_bat_dau, tt.ngay_ket_thuc, nv.hoten " +
                        "FROM the_thang tt " +
                        "JOIN the t ON tt.the_id = t.the_id " +
                        "JOIN loai_xe lx ON t.loai_xe_id = lx.loai_xe_id " +
                        "JOIN nhan_vien nv ON t.nhan_vien_id = nv.nhanvien_id " +
                        "WHERE tt.the_thang_id = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, theThangId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    bienSoXeLabel.setText(resultSet.getString("bien_so_xe"));
                    loaiXeLabel.setText(resultSet.getString("ten_loai"));

                    Date ngayBatDau = resultSet.getDate("ngay_bat_dau");
                    Date ngayKetThuc = resultSet.getDate("ngay_ket_thuc");
                    gioVaoLabel.setText("Từ " + ngayBatDau + " đến " + ngayKetThuc);

                    nhanVienLabel.setText(resultSet.getString("hoten"));
                }
                statement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Thông báo", "Lỗi", "Không thể tải thông tin: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Thông báo", "Lỗi", "Định dạng mã thẻ không hợp lệ!");
        }
    }

    public void TraXe() {
        String loaiThe = loaiTheBox.getValue();
        String maThe = maTheBox.getValue();
        String bienSoXe = bienSoXeLabel.getText();

        // Kiểm tra dữ liệu đầu vào
        if (loaiThe == null || maThe == null || bienSoXe == null || bienSoXe.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thiếu thông tin", "Hãy chọn xe cần trả!");
            return;
        }

        try {
            connection.setAutoCommit(false);

            if ("Thẻ ngày".equals(loaiThe)) {
                int theNgayId = Integer.parseInt(maThe.split(" - ")[0].substring(1));

                // Lấy the_id từ the_ngay
                String getTheIdQuery = "SELECT the_id, gio_vao FROM the_ngay WHERE the_ngay_id = ?";
                PreparedStatement getTheIdStmt = connection.prepareStatement(getTheIdQuery);
                getTheIdStmt.setInt(1, theNgayId);
                ResultSet rs = getTheIdStmt.executeQuery();

                if (rs.next()) {
                    int theId = rs.getInt("the_id");
                    Timestamp gioVao = rs.getTimestamp("gio_vao");

                    // Cập nhật giờ ra
                    String updateQuery = "UPDATE the_ngay SET gio_ra = CURRENT_TIMESTAMP WHERE the_ngay_id = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                    updateStmt.setInt(1, theNgayId);
                    updateStmt.executeUpdate();

                    // Tính phí
                    LocalDateTime gioRa = LocalDateTime.now();
                    long soPhut = java.time.Duration.between(gioVao.toLocalDateTime(), gioRa).toMinutes();
                    double soGio = Math.ceil(soPhut / 60.0);

                    // Lấy giá cơ bản
                    String getPriceQuery = "SELECT lx.gia_co_ban FROM the t " +
                            "JOIN loai_xe lx ON t.loai_xe_id = lx.loai_xe_id WHERE t.the_id = ?";
                    PreparedStatement priceStmt = connection.prepareStatement(getPriceQuery);
                    priceStmt.setInt(1, theId);
                    ResultSet priceRs = priceStmt.executeQuery();

                    double phiGuiXe = 0;
                    if (priceRs.next()) {
                        phiGuiXe = priceRs.getDouble("gia_co_ban") * soGio;
                    }

                    // Xóa khỏi bảng the và reset the_id trong the_ngay
                    String deleteTheQuery = "DELETE FROM the WHERE the_id = ?";
                    PreparedStatement deleteStmt = connection.prepareStatement(deleteTheQuery);
                    deleteStmt.setInt(1, theId);
                    deleteStmt.executeUpdate();

                    String resetQuery = "UPDATE the_ngay SET the_id = NULL, gio_vao = NULL, gio_ra = NULL WHERE the_ngay_id = ?";
                    PreparedStatement resetStmt = connection.prepareStatement(resetQuery);
                    resetStmt.setInt(1, theNgayId);
                    resetStmt.executeUpdate();

                    connection.commit();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String message = String.format(
                            "Mã thẻ: N%d\nBiển số xe: %s\nLoại xe: %s\nGiờ vào: %s\nGiờ ra: %s\nSố giờ: %.0f giờ\nPhí gửi xe: %.0f VNĐ",
                            theNgayId, bienSoXe, loaiXeLabel.getText(),
                            gioVao.toLocalDateTime().format(formatter),
                            gioRa.format(formatter), soGio, phiGuiXe
                    );

                    showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xuất xe thành công!", message);
                    clearFields();
                }

            } else if ("Thẻ tháng".equals(loaiThe)) {
                int theThangId = Integer.parseInt(maThe.split(" - ")[0].substring(1));

                // Lấy the_id từ the_thang
                String getTheIdQuery = "SELECT the_id FROM the_thang WHERE the_thang_id = ?";
                PreparedStatement getTheIdStmt = connection.prepareStatement(getTheIdQuery);
                getTheIdStmt.setInt(1, theThangId);
                ResultSet rs = getTheIdStmt.executeQuery();

                if (rs.next()) {
                    int theId = rs.getInt("the_id");

                    // Xóa khỏi bảng the và reset the_id trong the_thang
                    String deleteTheQuery = "DELETE FROM the WHERE the_id = ?";
                    PreparedStatement deleteStmt = connection.prepareStatement(deleteTheQuery);
                    deleteStmt.setInt(1, theId);
                    deleteStmt.executeUpdate();

                    String resetQuery = "UPDATE the_thang SET the_id = NULL WHERE the_thang_id = ?";
                    PreparedStatement resetStmt = connection.prepareStatement(resetQuery);
                    resetStmt.setInt(1, theThangId);
                    resetStmt.executeUpdate();

                    connection.commit();

                    String message = String.format(
                            "Mã thẻ: T%d\nBiển số xe: %s\nLoại xe: %s\nLoại thẻ: Thẻ tháng\nPhí: Đã thanh toán theo tháng",
                            theThangId, bienSoXe, loaiXeLabel.getText()
                    );

                    showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xuất xe thành công!", message);
                    clearFields();
                }
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Thông báo", "Lỗi", "Không thể trả xe: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Thông báo", "Lỗi", "Định dạng mã thẻ không hợp lệ!");
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void reLoadData() {
        loaiTheBox.setValue(null);
        maTheBox.getItems().clear();
        clearLabels();
        handleLoaiTheChange();
    }

    private void clearLabels() {
        loaiXeLabel.setText("");
        bienSoXeLabel.setText("");
        gioVaoLabel.setText("");
        nhanVienLabel.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        loaiTheBox.setValue(null);
        maTheBox.setValue(null);
        clearLabels();
    }
}