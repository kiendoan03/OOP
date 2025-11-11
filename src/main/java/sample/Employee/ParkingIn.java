package sample.Employee;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import javafx.util.Duration;
import sample.Main;
import sample.DatabaseConnection;

public class ParkingIn implements Initializable {

    @FXML private Label ngayHienTaiLabel;
    @FXML private Label thongBaoLabel;

    // Tab Nhận xe
    @FXML private ComboBox<String> loaiTheComboBox;
    @FXML private ComboBox<String> maTheComboBox;
    @FXML private ComboBox<String> loaiXeComboBox;
    @FXML private ComboBox<String> nhanVienComboBox;
    @FXML private TextField bienSoXeTextField;
    @FXML private TextField gioVaoTextField;
    @FXML private TextField theIdTextField;

    // Tab Xuất xe
    @FXML private ComboBox<String> maTheXuatComboBox;
    @FXML private TextField bienSoXeXuatTextField;
    @FXML private TextField loaiXeXuatTextField;
    @FXML private TextField gioVaoXuatTextField;
    @FXML private TextField gioRaTextField;
    @FXML private TextField phiGuiXeTextField;

    // Biến kết nối cơ sở dữ liệu
    private Connection connection;

    // Hằng số
    private static final int MAX_BIEN_SO_LENGTH = 10;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Khởi tạo kết nối CSDL
        initializeDatabase();

        // Hiển thị ngày hiện tại
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ngayHienTaiLabel.setText(currentDate.format(formatter));

        // Thiết lập giới hạn kí tự cho biển số xe
        bienSoXeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_BIEN_SO_LENGTH) {
                bienSoXeTextField.setText(oldValue);
            }
        });

        // Khởi tạo ComboBox loại thẻ
        ObservableList<String> loaiTheOptions = FXCollections.observableArrayList("Thẻ ngày", "Thẻ tháng");
        loaiTheComboBox.setItems(loaiTheOptions);

        // Khởi tạo ComboBox loại xe
        ObservableList<String> loaiXeOptions = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ten_loai FROM loai_xe");
            while (rs.next()) {
                loaiXeOptions.add(rs.getString("ten_loai"));
            }
            loaiXeComboBox.setItems(loaiXeOptions);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách loại xe: " + e.getMessage());
        }

        // Khởi tạo ComboBox nhân viên
        loadNhanVienComboBox();

        // Xử lý sự kiện khi chọn loại thẻ
        loaiTheComboBox.setOnAction(event -> handleLoaiTheChange());

        // Xử lý sự kiện khi chọn mã thẻ
        maTheComboBox.setOnAction(event -> handleMaTheChange());

        // Xử lý sự kiện khi chọn mã thẻ xuất
        maTheXuatComboBox.setOnAction(event -> handleMaTheXuatChange());

        // Hiển thị giờ vào hiện tại
        updateCurrentTime();

        // Tải danh sách mã thẻ đang sử dụng để xuất xe
        loadMaTheXuatComboBox();

        // Tạo ID thẻ mới
        suggestNewTheId();
    }

    private void initializeDatabase() {
        connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            System.out.println("✅ Sử dụng kết nối SQL Server từ DatabaseConnection!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể lấy kết nối đến cơ sở dữ liệu!");
        }
    }
    private void loadNhanVienComboBox() {
        ObservableList<String> nhanVienOptions = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT nhanvien_id, hoten FROM nhan_vien WHERE quyen_tk = 'Nhân viên'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                nhanVienOptions.add(rs.getInt("nhanvien_id") + " - " + rs.getString("hoten"));
            }
            nhanVienComboBox.setItems(nhanVienOptions);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách nhân viên: " + e.getMessage());
        }
    }

    private void handleLoaiTheChange() {
        String selectedLoaiThe = loaiTheComboBox.getValue();
        maTheComboBox.getItems().clear();
        bienSoXeTextField.clear();

        if (selectedLoaiThe == null) return;

        try {
            if ("Thẻ ngày".equals(selectedLoaiThe)) {
                // Lấy danh sách thẻ ngày còn trống
                String query = "SELECT the_ngay_id FROM the_ngay WHERE the_id IS NULL";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                ObservableList<String> maTheOptions = FXCollections.observableArrayList();
                while (rs.next()) {
                    maTheOptions.add("N" + rs.getString("the_ngay_id"));
                }

                maTheComboBox.setItems(maTheOptions);
                bienSoXeTextField.setEditable(true);

            } else if ("Thẻ tháng".equals(selectedLoaiThe)) {
                // Lấy danh sách thẻ tháng đã đăng ký nhưng chưa sử dụng
                String query = "SELECT tt.the_thang_id, tt.ho_ten_khach_hang, tt.sdt_khach_hang " +
                        "FROM the_thang tt LEFT JOIN the t ON tt.the_id = t.the_id " +
                        "WHERE t.the_id IS NULL AND CAST(GETDATE() AS DATE) BETWEEN tt.ngay_bat_dau AND tt.ngay_ket_thuc";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                ObservableList<String> maTheOptions = FXCollections.observableArrayList();
                while (rs.next()) {
                    maTheOptions.add("T" + rs.getString("the_thang_id") + " - " + rs.getString("ho_ten_khach_hang"));
                }

                maTheComboBox.setItems(maTheOptions);
            }

            // Tạo ID thẻ mới sau khi chọn loại thẻ
            suggestNewTheId();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách mã thẻ: " + e.getMessage());
        }
    }


    private void handleMaTheChange() {
        String selectedMaThe = maTheComboBox.getValue();

        if (selectedMaThe == null) return;

        if (selectedMaThe.startsWith("T")) {
            // Nếu là thẻ tháng
            try {
                int theThangId = Integer.parseInt(selectedMaThe.substring(1).split(" - ")[0]);

                // Thay đổi truy vấn này để không sử dụng tt.loai_xe_id
                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT tt.ho_ten_khach_hang, tt.sdt_khach_hang " +
                                "FROM the_thang tt " +
                                "WHERE tt.the_thang_id = ?"
                );
                pstmt.setInt(1, theThangId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Hiển thị thông tin khách hàng (nếu cần)
                    String hoTenKH = rs.getString("ho_ten_khach_hang");
                    String sdtKH = rs.getString("sdt_khach_hang");

                    // Cho phép người dùng chọn loại xe (vì loại xe không được lưu trong thẻ tháng)
                    loaiXeComboBox.setDisable(false);
                    loaiXeComboBox.setValue(null);
                    bienSoXeTextField.setEditable(true);
                    bienSoXeTextField.clear();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin thẻ tháng: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void handleMaTheXuatChange() {
        String selectedMaThe = maTheXuatComboBox.getValue();

        if (selectedMaThe == null) return;

        try {
            String[] parts = selectedMaThe.split(" - ");
            int theId = Integer.parseInt(parts[0]);

            // Truy vấn thông tin xe cần xuất
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT t.bien_so_xe, lx.ten_loai, tn.gio_vao, tt.the_thang_id " +
                            "FROM the t " +
                            "LEFT JOIN loai_xe lx ON t.loai_xe_id = lx.loai_xe_id " +
                            "LEFT JOIN the_ngay tn ON t.the_id = tn.the_id " +
                            "LEFT JOIN the_thang tt ON t.the_id = tt.the_id " +
                            "WHERE t.the_id = ?"
            );
            pstmt.setInt(1, theId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                bienSoXeXuatTextField.setText(rs.getString("bien_so_xe"));
                loaiXeXuatTextField.setText(rs.getString("ten_loai"));

                boolean isTheThang = rs.getObject("the_thang_id") != null;

                if (isTheThang) {
                    // Nếu là thẻ tháng, không cần tính phí
                    gioVaoXuatTextField.setText("Thẻ tháng");
                    gioRaTextField.setText("Thẻ tháng");
                    phiGuiXeTextField.setText("0 VNĐ");
                } else {
                    // Nếu là thẻ ngày, tính phí bình thường
                    Timestamp gioVao = rs.getTimestamp("gio_vao");
                    if (gioVao != null) {
                        gioVaoXuatTextField.setText(gioVao.toString());

                        // Hiển thị giờ ra hiện tại
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        gioRaTextField.setText(now.format(formatter));

                        // Tính phí tự động
                        tinhPhiGuiXe(theId, gioVao.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin xe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void tinhPhiGuiXe(int theId, LocalDateTime gioVao) {
        try {
            // Lấy thông tin loại xe
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT lx.gia_co_ban, t.loai_xe_id " +
                            "FROM the t " +
                            "JOIN loai_xe lx ON t.loai_xe_id = lx.loai_xe_id " +
                            "WHERE t.the_id = ?"
            );
            pstmt.setInt(1, theId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double giaCoBan = rs.getDouble("gia_co_ban");

                // Tính phí (đơn giản: phí = giá cơ bản * số giờ)
                double phi = giaCoBan;

                // Hiển thị phí
                phiGuiXeTextField.setText(String.format("%.0f VNĐ", phi));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tính phí gửi xe: " + e.getMessage());
        }
    }

    private void updateCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        gioVaoTextField.setText(now.format(formatter));
    }

    private void loadMaTheXuatComboBox() {
        try {
            String query = "SELECT t.the_id, t.bien_so_xe, tn.the_ngay_id, tt.the_thang_id " +
                    "FROM the t " +
                    "LEFT JOIN the_ngay tn ON t.the_id = tn.the_id " +
                    "LEFT JOIN the_thang tt ON t.the_id = tt.the_id " +
                    "WHERE tn.gio_ra IS NULL";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ObservableList<String> options = FXCollections.observableArrayList();
            while (rs.next()) {
                int theId = rs.getInt("the_id");
                String bienSoXe = rs.getString("bien_so_xe");
                int theNgayId = rs.getInt("the_ngay_id");

                // Kiểm tra the_thang_id
                Integer theThangId = null;
                int tempThangId = rs.getInt("the_thang_id");
                if (!rs.wasNull()) {
                    theThangId = tempThangId;
                }

                String displayText;
                if (theThangId != null) {
                    displayText = theId + " - " + bienSoXe + " ( T" + theThangId + " )";
                } else {
                    displayText = theId + " - " + bienSoXe + " ( N" + theNgayId + " )";
                }

                options.add(displayText);
            }
            maTheXuatComboBox.setItems(options);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách thẻ: " + e.getMessage());
        }
    }



    private boolean checkTheIdExists(int theId) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM the WHERE the_id = ?"
            );
            pstmt.setInt(1, theId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void suggestNewTheId() {
        try {
            // Lấy ID thẻ lớn nhất trong cơ sở dữ liệu
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(the_id) AS max_id FROM the");

            int newId = 1; // Giá trị mặc định nếu chưa có thẻ nào
            if (rs.next() && rs.getObject("max_id") != null) {
                newId = rs.getInt("max_id") + 1;
            }

            // Đặt giá trị mới cho trường nhập ID
            theIdTextField.setText(String.valueOf(newId));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleNhapXe(ActionEvent event) {
        // Kiểm tra các trường nhập liệu
        if (loaiTheComboBox.getValue() == null || maTheComboBox.getValue() == null ||
                loaiXeComboBox.getValue() == null || bienSoXeTextField.getText().isEmpty() ||
                nhanVienComboBox.getValue() == null || theIdTextField.getText().isEmpty()) {

            thongBaoLabel.setText("Vui lòng nhập đầy đủ thông tin!");
// Tạo một transition 3 giây
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> thongBaoLabel.setText("")); // Xóa nội dung sau 3 giây
            pause.play();

            return;
        }

        // Lấy thông tin từ form
        String loaiThe = loaiTheComboBox.getValue();
        String maThe = maTheComboBox.getValue();
        String loaiXe = loaiXeComboBox.getValue();
        String bienSoXe = bienSoXeTextField.getText();
        String nhanVien = nhanVienComboBox.getValue();

        // Lấy ID nhân viên và the_id
        int nhanVienId = Integer.parseInt(nhanVien.split(" - ")[0]);
        int theId;

        try {
            theId = Integer.parseInt(theIdTextField.getText());

            // Kiểm tra ID thẻ đã tồn tại chưa
            if (checkTheIdExists(theId)) {
                thongBaoLabel.setText("Lỗi: Mã thẻ (ID) đã tồn tại!");

                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(e -> thongBaoLabel.setText("")); // Xóa nội dung sau 3 giây
                pause.play();

                return;
            }

            // Lấy ID loại xe
            PreparedStatement pstmtLoaiXe = connection.prepareStatement("SELECT loai_xe_id, gia_co_ban FROM loai_xe WHERE ten_loai = ?");
            pstmtLoaiXe.setString(1, loaiXe);
            ResultSet rsLoaiXe = pstmtLoaiXe.executeQuery();

            if (rsLoaiXe.next()) {
                int loaiXeId = rsLoaiXe.getInt("loai_xe_id");
                int giaCoBan = rsLoaiXe.getInt("gia_co_ban");

                connection.setAutoCommit(false);

                // Thêm vào bảng 'the' với ID cụ thể
                PreparedStatement pstmtThe = connection.prepareStatement(
                        "INSERT INTO the (the_id, bien_so_xe, loai_xe_id, nhan_vien_id) VALUES (?, ?, ?, ?)"
                );
                pstmtThe.setInt(1, theId);
                pstmtThe.setString(2, bienSoXe);
                pstmtThe.setInt(3, loaiXeId);
                pstmtThe.setInt(4, nhanVienId);
                pstmtThe.executeUpdate();

                PreparedStatement pstmtThongKe = connection.prepareStatement(
                        "INSERT INTO thong_ke (ngay_giao_dich, loai_the, gia, nhan_vien_id) VALUES (CURRENT_TIMESTAMP, ?, ?, ?)"
                );
                pstmtThongKe.setString(1, "ngay"); // hoặc loaiThe.toLowerCase()
                pstmtThongKe.setInt(2, giaCoBan);
                pstmtThongKe.setInt(3, nhanVienId);
                pstmtThongKe.executeUpdate();

                if ("Thẻ ngày".equals(loaiThe)) {
                    // Trường hợp thẻ ngày
                    String maTheNgay = maThe.substring(1); // Bỏ chữ 'N' đầu tiên

                    // Cập nhật bảng the_ngay
                    PreparedStatement pstmtTheNgay = connection.prepareStatement(
                            "UPDATE the_ngay SET the_id = ?, gio_vao = CURRENT_TIMESTAMP WHERE the_ngay_id = ?"
                    );
                    pstmtTheNgay.setInt(1, theId);
                    pstmtTheNgay.setInt(2, Integer.parseInt(maTheNgay));
                    pstmtTheNgay.executeUpdate();

                } else if ("Thẻ tháng".equals(loaiThe)) {
                    // Trường hợp thẻ tháng
                    String maTheThang = maThe.substring(1).split(" - ")[0]; // Bỏ chữ 'T' đầu tiên và lấy ID

                    // Cập nhật bảng the_thang
                    PreparedStatement pstmtTheThang = connection.prepareStatement(
                            "UPDATE the_thang SET the_id = ? WHERE the_thang_id = ?"
                    );
                    pstmtTheThang.setInt(1, theId);
                    pstmtTheThang.setInt(2, Integer.parseInt(maTheThang));
                    pstmtTheThang.executeUpdate();
                }

                connection.commit();

                // Hiển thị thông báo thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã nhập xe thành công!\n\n" +
                        "Loại thẻ: " + loaiThe + "\n" +
                        "Mã thẻ ID: " + theId + "\n" +
                        "Biển số xe: " + bienSoXe + "\n" +
                        "Loại xe: " + loaiXe);

                // Tùy chỉnh nút OK
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Đóng");

                // Hiển thị dialog và đợi người dùng đóng
                alert.showAndWait();

                // Làm mới form
                handleLamMoi(null);

                // Tải lại danh sách mã thẻ xuất
                loadMaTheXuatComboBox();
            } else {
                thongBaoLabel.setText("Lỗi: Không tìm thấy loại xe!");
            }

        } catch (NumberFormatException e) {
            thongBaoLabel.setText("Lỗi: Mã thẻ (ID) phải là số!");
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            thongBaoLabel.setText("Lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLamMoi(ActionEvent event) {
        loaiTheComboBox.setValue(null);
        maTheComboBox.getItems().clear();
        loaiXeComboBox.setValue(null);
        bienSoXeTextField.clear();
        theIdTextField.clear();
        updateCurrentTime();
        thongBaoLabel.setText("");
        suggestNewTheId();
    }

    @FXML
    private void handleXuatXe(ActionEvent event) throws SQLException {
        String selectedMaThe = maTheXuatComboBox.getValue();
        if (selectedMaThe == null) {
            thongBaoLabel.setText("Vui lòng chọn thẻ xe cần xuất!");
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> thongBaoLabel.setText("")); // Xóa nội dung sau 3 giây
            pause.play();
            return;
        }

        int theId = Integer.parseInt(selectedMaThe.split(" - ")[0]);

        // Kiểm tra xem thẻ có đang gặp sự cố không
        try (PreparedStatement pstmtCheckSuCo = connection.prepareStatement(
                "SELECT COUNT(*) FROM su_co WHERE the_id = ?")) {
            pstmtCheckSuCo.setInt(1, theId);
            ResultSet rsSuCo = pstmtCheckSuCo.executeQuery();
            if (rsSuCo.next() && rsSuCo.getInt(1) > 0) {
                thongBaoLabel.setText("Xe của thẻ xe này đang gặp sự cố, không thể xuất xe!");
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(e -> thongBaoLabel.setText("")); // Xóa nội dung sau 3 giây
                pause.play();
                return;
            }
        }

        // Lưu thông tin xe đang xuất để hiển thị trong thông báo
        String bienSoXe = bienSoXeXuatTextField.getText();
        String loaiXe = loaiXeXuatTextField.getText();
        String phiGuiXe = phiGuiXeTextField.getText();
        String gioVao = gioVaoXuatTextField.getText();
        String gioRa = gioRaTextField.getText();

        try {
            connection.setAutoCommit(false);

            // Kiểm tra xem đây là thẻ ngày hay thẻ tháng
            PreparedStatement pstmtCheckTheNgay = connection.prepareStatement(
                    "SELECT the_ngay_id FROM the_ngay WHERE the_id = ?"
            );
            pstmtCheckTheNgay.setInt(1, theId);
            ResultSet rsTheNgay = pstmtCheckTheNgay.executeQuery();

            boolean isTheNgay = rsTheNgay.next();

            PreparedStatement pstmtCheckTheThang = connection.prepareStatement(
                    "SELECT the_thang_id FROM the_thang WHERE the_id = ?"
            );
            pstmtCheckTheThang.setInt(1, theId);
            ResultSet rsTheThang = pstmtCheckTheThang.executeQuery();

            boolean isTheThang = rsTheThang.next();

            // Xử lý theo từng loại thẻ
            if (isTheNgay) {
                // Đối với thẻ ngày, cập nhật giờ ra
                String phiText = phiGuiXeTextField.getText().replaceAll("[^0-9]", "");
                double phi = Double.parseDouble(phiText);

                PreparedStatement pstmtUpdateTheNgay = connection.prepareStatement(
                        "UPDATE the_ngay SET gio_ra = CURRENT_TIMESTAMP WHERE the_id = ?"
                );
//                pstmtUpdateTheNgay.setDouble(1, phi);
                pstmtUpdateTheNgay.setInt(1, theId);
                pstmtUpdateTheNgay.executeUpdate();

                // Thiết lập the_id = NULL trong bảng the_ngay
                PreparedStatement pstmtNullifyTheNgay = connection.prepareStatement(
                        "UPDATE the_ngay SET the_id = NULL, gio_vao = NULL, gio_ra = NULL WHERE the_id = ?"
                );
                pstmtNullifyTheNgay.setInt(1, theId);
                pstmtNullifyTheNgay.executeUpdate();
            }

            if (isTheThang) {
                // Đối với thẻ tháng, chỉ cần bỏ liên kết
                PreparedStatement pstmtNullifyTheThang = connection.prepareStatement(
                        "UPDATE the_thang SET the_id = NULL WHERE the_id = ?"
                );
                pstmtNullifyTheThang.setInt(1, theId);
                pstmtNullifyTheThang.executeUpdate();
            }

            // Xóa bản ghi từ bảng 'the'
            PreparedStatement pstmtDeleteThe = connection.prepareStatement(
                    "DELETE FROM the WHERE the_id = ?"
            );
            pstmtDeleteThe.setInt(1, theId);
            pstmtDeleteThe.executeUpdate();

            connection.commit();

            // Hiển thị thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã xuất xe thành công!\n\n" +
                    "Loại thẻ: " + (isTheNgay ? "Thẻ ngày" : "Thẻ tháng") + "\n" +
                    "Biển số xe: " + bienSoXe + "\n" +
                    "Loại xe: " + loaiXe + "\n" +
                    (isTheNgay ? "Giờ vào: " + gioVao + "\n" +
                            "Giờ ra: " + gioRa + "\n" +
                            "Phí gửi xe: " + phiGuiXe : "Phí gửi xe: Đã thanh toán theo tháng"));

            // Tùy chỉnh nút OK
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Đóng");

            // Hiển thị dialog và đợi người dùng đóng
            alert.showAndWait();

            // Làm mới form
            handleLamMoiXuat(null);

            // Tải lại danh sách thẻ xuất
            loadMaTheXuatComboBox();

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            thongBaoLabel.setText("Lỗi: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            thongBaoLabel.setText("Lỗi: Không thể chuyển đổi giá trị phí!");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLamMoiXuat(ActionEvent event) {
        maTheXuatComboBox.setValue(null);
        bienSoXeXuatTextField.clear();
        loaiXeXuatTextField.clear();
        gioVaoXuatTextField.clear();
        gioRaTextField.clear();
        phiGuiXeTextField.clear();
        thongBaoLabel.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public class XeInfo {
        private final IntegerProperty maThe;
        private final StringProperty loaiThe;
        private final StringProperty bienSoXe;
        private final StringProperty loaiXe;
        private final ObjectProperty<LocalDateTime> gioVao;
        private final StringProperty nhanVien;

        public XeInfo(int maThe, String loaiThe, String bienSoXe, String loaiXe, LocalDateTime gioVao, String nhanVien) {
            this.maThe = new SimpleIntegerProperty(maThe);
            this.loaiThe = new SimpleStringProperty(loaiThe);
            this.bienSoXe = new SimpleStringProperty(bienSoXe);
            this.loaiXe = new SimpleStringProperty(loaiXe);
            this.gioVao = new SimpleObjectProperty<>(gioVao);
            this.nhanVien = new SimpleStringProperty(nhanVien);
        }
    }


    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("EmployeeUI.fxml");
    }
}