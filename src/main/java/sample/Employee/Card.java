package sample.Employee;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.Main;
import sample.DatabaseConnection;


public class Card extends EmployeeUI implements Initializable {

    @FXML private Label ngayHienTaiLabel;
    @FXML private Label thongBaoLabel;

    // Tab Đăng ký thẻ tháng
    @FXML private TextField maTheThangTextField;
    @FXML private TextField hoTenKhachHangTextField;
    @FXML private TextField sdtKhachHangTextField;
    @FXML private TextField bienSoXeTextField;
    @FXML private DatePicker ngayBatDauDatePicker;
    @FXML private DatePicker ngayKetThucDatePicker;
    @FXML private ComboBox<String> nhanVienComboBox;

    // Tab Danh sách thẻ tháng
    @FXML private TableView<TheThangInfo> theThangTableView;
    @FXML private TableColumn<TheThangInfo, Integer> maTheThangColumn;
    @FXML private TableColumn<TheThangInfo, String> hoTenKHColumn;
    @FXML private TableColumn<TheThangInfo, String> sdtKHColumn;
    @FXML private TableColumn<TheThangInfo, String> bienSoXeColumn;
    @FXML private TableColumn<TheThangInfo, LocalDate> ngayBatDauColumn;
    @FXML private TableColumn<TheThangInfo, LocalDate> ngayKetThucColumn;
    @FXML private TableColumn<TheThangInfo, String> trangThaiColumn;

    // Biến kết nối cơ sở dữ liệu
    private Connection connection;

    // Hằng số
    private static final int MAX_BIEN_SO_LENGTH = 10;

    public class TheThangInfo {
        private final IntegerProperty maTheThang;
        private final StringProperty hoTenKH;
        private final StringProperty sdtKH;
//        private final StringProperty loaiXe;
        private final StringProperty bienSoXe;
        private final ObjectProperty<LocalDate> ngayBatDau;
        private final ObjectProperty<LocalDate> ngayKetThuc;
        private final StringProperty trangThai;

        public TheThangInfo(int maTheThang, String hoTenKH, String sdtKH, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai, String bienSoXe) {
            this.maTheThang = new SimpleIntegerProperty(maTheThang);
            this.hoTenKH = new SimpleStringProperty(hoTenKH);
            this.sdtKH = new SimpleStringProperty(sdtKH);
//            this.loaiXe = new SimpleStringProperty(loaiXe);
            this.bienSoXe = new SimpleStringProperty(bienSoXe);
            this.ngayBatDau = new SimpleObjectProperty<>(ngayBatDau);
            this.ngayKetThuc = new SimpleObjectProperty<>(ngayKetThuc);
            this.trangThai = new SimpleStringProperty(trangThai);
        }

        // Getters cho Property
        public IntegerProperty maTheThangProperty() {
            return maTheThang;
        }

        public StringProperty hoTenKHProperty() {
            return hoTenKH;
        }

        public StringProperty sdtKHProperty() {
            return sdtKH;
        }

//        public StringProperty loaiXeProperty() {
//            return loaiXe;
//        }
//
        public StringProperty bienSoXeProperty() {
            return bienSoXe;
        }

        public ObjectProperty<LocalDate> ngayBatDauProperty() {
            return ngayBatDau;
        }

        public ObjectProperty<LocalDate> ngayKetThucProperty() {
            return ngayKetThuc;
        }

        public StringProperty trangThaiProperty() {
            return trangThai;
        }

        // Getters và setters thông thường
        public int getMaTheThang() {
            return maTheThang.get();
        }

        public void setMaTheThang(int maTheThang) {
            this.maTheThang.set(maTheThang);
        }

        public String getHoTenKH() {
            return hoTenKH.get();
        }

        public void setHoTenKH(String hoTenKH) {
            this.hoTenKH.set(hoTenKH);
        }

        public String getSdtKH() {
            return sdtKH.get();
        }

        public void setSdtKH(String sdtKH) {
            this.sdtKH.set(sdtKH);
        }

//        public String getLoaiXe() {
//            return loaiXe.get();
//        }

//        public void setLoaiXe(String loaiXe) {
//            this.loaiXe.set(loaiXe);
//        }

        public String getBienSoXe() {
            return bienSoXe.get();
        }

        public void setBienSoXe(String bienSoXe) {
            this.bienSoXe.set(bienSoXe);
        }

        public LocalDate getNgayBatDau() {
            return ngayBatDau.get();
        }

        public void setNgayBatDau(LocalDate ngayBatDau) {
            this.ngayBatDau.set(ngayBatDau);
        }

        public LocalDate getNgayKetThuc() {
            return ngayKetThuc.get();
        }

        public void setNgayKetThuc(LocalDate ngayKetThuc) {
            this.ngayKetThuc.set(ngayKetThuc);
        }

        public String getTrangThai() {
            return trangThai.get();
        }

        public void setTrangThai(String trangThai) {
            this.trangThai.set(trangThai);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Khởi tạo kết nối CSDL
        initializeDatabase();

        // Hiển thị ngày hiện tại
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ngayHienTaiLabel.setText(currentDate.format(formatter));



        // Khởi tạo DatePicker với ngày hiện tại và sau 30 ngày
        ngayBatDauDatePicker.setValue(LocalDate.now());
        ngayKetThucDatePicker.setValue(LocalDate.now().plusMonths(1));

        // Định dạng hiển thị ngày tháng cho DatePicker
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        ngayBatDauDatePicker.setConverter(converter);
        ngayKetThucDatePicker.setConverter(converter);

        // Khởi tạo ComboBox nhân viên
        loadNhanVienComboBox();


        // Khởi tạo TableView
        initializeTableView();

        // Tạo ID thẻ tháng mới
        suggestNewTheThangId();

        // Tải dữ liệu cho TableView
        loadTableViewData();

        // Thêm listener cho TableView để hiển thị thông tin khi chọn
        theThangTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showSelectedTheThang(newSelection);
                    }
                }
        );
    }

    private void initializeDatabase() {
        connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            System.out.println("✅ Sử dụng kết nối SQL Server từ DatabaseConnection!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể lấy kết nối đến cơ sở dữ liệu!");
        }
    }

//    private void loadLoaiXeComboBox() {
//        ObservableList<String> loaiXeOptions = FXCollections.observableArrayList();
//        try {
//            Statement stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT ten_loai FROM loai_xe");
//            while (rs.next()) {
//                loaiXeOptions.add(rs.getString("ten_loai"));
//            }
//            loaiXeComboBox.setItems(loaiXeOptions);
//        } catch (SQLException e) {
//            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách loại xe: " + e.getMessage());
//        }
//    }

    private void loadNhanVienComboBox() {
        ObservableList<String> nhanVienOptions = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nhanvien_id, hoten FROM nhan_vien");
            while (rs.next()) {
                nhanVienOptions.add(rs.getInt("nhanvien_id") + " - " + rs.getString("hoten"));
            }
            nhanVienComboBox.setItems(nhanVienOptions);

            // Chọn nhân viên đầu tiên (nếu có)
            if (!nhanVienOptions.isEmpty()) {
                nhanVienComboBox.setValue(nhanVienOptions.get(0));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách nhân viên: " + e.getMessage());
        }
    }

    private void initializeTableView() {
        maTheThangColumn.setCellValueFactory(cellData -> cellData.getValue().maTheThangProperty().asObject());
        hoTenKHColumn.setCellValueFactory(cellData -> cellData.getValue().hoTenKHProperty());
        sdtKHColumn.setCellValueFactory(cellData -> cellData.getValue().sdtKHProperty());
//        loaiXeColumn.setCellValueFactory(cellData -> cellData.getValue().loaiXeProperty());
        bienSoXeColumn.setCellValueFactory(cellData -> cellData.getValue().bienSoXeProperty());

        // Định dạng hiển thị ngày
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Cấu hình cột ngày bắt đầu
        ngayBatDauColumn.setCellValueFactory(cellData -> cellData.getValue().ngayBatDauProperty());
        ngayBatDauColumn.setCellFactory(column -> new TableCell<TheThangInfo, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        // Cấu hình cột ngày kết thúc
        ngayKetThucColumn.setCellValueFactory(cellData -> cellData.getValue().ngayKetThucProperty());
        ngayKetThucColumn.setCellFactory(column -> new TableCell<TheThangInfo, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        // Cấu hình cột trạng thái
        trangThaiColumn.setCellValueFactory(cellData -> cellData.getValue().trangThaiProperty());
        trangThaiColumn.setCellFactory(column -> new TableCell<TheThangInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Còn hiệu lực".equals(item)) {
                        setStyle("-fx-text-fill: green;");
                    } else if ("Hết hiệu lực".equals(item)) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: blue;");
                    }
                }
            }
        });
    }

    private void suggestNewTheThangId() {
        try {
            // Lấy ID thẻ tháng lớn nhất trong cơ sở dữ liệu
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(the_thang_id) AS max_id FROM the_thang");

            int newId = 1; // Giá trị mặc định nếu chưa có thẻ nào
            if (rs.next() && rs.getObject("max_id") != null) {
                newId = rs.getInt("max_id") + 1;
            }

            // Đặt giá trị mới cho trường nhập ID
            maTheThangTextField.setText(String.valueOf(newId));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTableViewData() {
        try {
            ObservableList<TheThangInfo> theThangList = FXCollections.observableArrayList();

            // Sửa truy vấn để lấy dữ liệu từ đúng bảng
            String query = "SELECT tt.the_thang_id, tt.ho_ten_khach_hang, tt.sdt_khach_hang, " +
                    "tt.ngay_bat_dau, tt.ngay_ket_thuc, tt.the_id, tt.bien_so " +
                    "FROM the_thang tt " +
                    "LEFT JOIN the t ON tt.the_id = t.the_id ";



            query += " ORDER BY tt.the_thang_id DESC";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int maTheThang = rs.getInt("the_thang_id");
                String hoTenKH = rs.getString("ho_ten_khach_hang");
                String sdtKH = rs.getString("sdt_khach_hang");

//                // Lấy biển số xe từ bảng the (nếu có)
                String bienSoXe = rs.getString("bien_so");
                if (bienSoXe == null) {
                    bienSoXe = "Chưa đăng ký";
                }
//
//                // Lấy loại xe từ bảng loai_xe thông qua bảng the (nếu có)
//                String loaiXe = rs.getString("ten_loai");
//                if (loaiXe == null) {
//                    loaiXe = "Chưa xác định";
//                }

                LocalDate ngayBatDau = rs.getDate("ngay_bat_dau").toLocalDate();
                LocalDate ngayKetThuc = rs.getDate("ngay_ket_thuc").toLocalDate();

                Object theIdObj = rs.getObject("the_id");

                // Xác định trạng thái
                String trangThai;
                if (theIdObj != null) {
                    trangThai = "Đang sử dụng";
                } else if (LocalDate.now().isAfter(ngayKetThuc)) {
                    trangThai = "Hết hiệu lực";
                } else if (LocalDate.now().isBefore(ngayBatDau)) {
                    trangThai = "Chưa kích hoạt";
                } else {
                    trangThai = "Còn hiệu lực";
                }

                TheThangInfo theThang = new TheThangInfo(maTheThang, hoTenKH, sdtKH, ngayBatDau, ngayKetThuc, trangThai, bienSoXe);
                theThangList.add(theThang);
            }

            theThangTableView.setItems(theThangList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSelectedTheThang(TheThangInfo theThang) {
        maTheThangTextField.setText(String.valueOf(theThang.getMaTheThang()));
        hoTenKhachHangTextField.setText(theThang.getHoTenKH());
        sdtKhachHangTextField.setText(theThang.getSdtKH());
//        loaiXeComboBox.setValue(theThang.getLoaiXe());
        bienSoXeTextField.setText(theThang.getBienSoXe());
        ngayBatDauDatePicker.setValue(theThang.getNgayBatDau());
        ngayKetThucDatePicker.setValue(theThang.getNgayKetThuc());
    }

    @FXML
    private void handleDangKy(ActionEvent event) {
        // Kiểm tra các trường nhập liệu
        if (maTheThangTextField.getText().isEmpty() ||
                hoTenKhachHangTextField.getText().isEmpty() ||
                sdtKhachHangTextField.getText().isEmpty() ||
                bienSoXeTextField.getText().isEmpty() ||
                ngayBatDauDatePicker.getValue() == null ||
                ngayKetThucDatePicker.getValue() == null ||
                nhanVienComboBox.getValue() == null) {

            thongBaoLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra ngày
        LocalDate ngayBatDau = ngayBatDauDatePicker.getValue();
        LocalDate ngayKetThuc = ngayKetThucDatePicker.getValue();

        if (ngayBatDau.isAfter(ngayKetThuc)) {
            thongBaoLabel.setText("Ngày bắt đầu không thể sau ngày kết thúc!");
            return;
        }

        try {
            int maTheThang = Integer.parseInt(maTheThangTextField.getText());

            // Kiểm tra ID đã tồn tại
            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM the_thang WHERE the_thang_id = ?"
            );
            checkStmt.setInt(1, maTheThang);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                // Nếu ID đã tồn tại, hỏi người dùng có muốn cập nhật không
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Xác nhận cập nhật");
                alert.setHeaderText("Thẻ tháng với ID này đã tồn tại");
                alert.setContentText("Bạn có muốn cập nhật thông tin cho thẻ tháng này không?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Người dùng chọn cập nhật
                    updateTheThang(maTheThang);
                }
                return;
            }

            // Lấy thông tin từ form
            String hoTenKH = hoTenKhachHangTextField.getText();
            String sdtKH = sdtKhachHangTextField.getText();
            String bienSo = bienSoXeTextField.getText();
            String nhanVien = nhanVienComboBox.getValue();
            int nhanVienId = Integer.parseInt(nhanVien.split(" - ")[0]);

            connection.setAutoCommit(false);

            // Thêm vào bảng the_thang
            PreparedStatement pstmtTheThang = connection.prepareStatement(
                    "INSERT INTO the_thang (the_thang_id, ho_ten_khach_hang, sdt_khach_hang, ngay_bat_dau, ngay_ket_thuc, bien_so) " +
                            "VALUES (?, ?, ?, ?, ?, ?)"
            );
            pstmtTheThang.setInt(1, maTheThang);
            pstmtTheThang.setString(2, hoTenKH);
            pstmtTheThang.setString(3, sdtKH);
            pstmtTheThang.setDate(4, java.sql.Date.valueOf(ngayBatDau));
            pstmtTheThang.setDate(5, java.sql.Date.valueOf(ngayKetThuc));
            pstmtTheThang.setString(6, bienSo);
            pstmtTheThang.executeUpdate();

            // Thêm vào bảng thong_ke với giá cố định cho thẻ tháng
            PreparedStatement pstmtThongKe = connection.prepareStatement(
                    "INSERT INTO thong_ke (ngay_giao_dich, loai_the, gia, nhan_vien_id) VALUES (CURRENT_TIMESTAMP, ?, ?, ?)"
            );
            pstmtThongKe.setString(1, "thang"); // hoặc "Thẻ tháng" nếu bạn muốn đồng nhất
            pstmtThongKe.setDouble(2, 70000);
            pstmtThongKe.setInt(3, nhanVienId);
            pstmtThongKe.executeUpdate();

            connection.commit();
            thongBaoLabel.setText("Đăng ký thẻ tháng thành công!");

            // Hiển thị thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã đăng ký thẻ tháng thành công!\n\n" +
                    "Mã thẻ tháng: " + maTheThang + "\n" +
                    "Khách hàng: " + hoTenKH + "\n" +
                    "Hiệu lực: " + ngayBatDau.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " đến " + ngayKetThuc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Tùy chỉnh nút OK
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Đóng");

            // Hiển thị dialog và đợi người dùng đóng
            alert.showAndWait();

            // Làm mới form
            handleLamMoi(null);

            // Tải lại dữ liệu
            loadTableViewData();

        } catch (NumberFormatException e) {
            thongBaoLabel.setText("Lỗi: Mã thẻ tháng phải là số!");
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

    private void updateTheThang(int maTheThang) {
        try {
            // Lấy thông tin từ form
            String hoTenKH = hoTenKhachHangTextField.getText();
            String sdtKH = sdtKhachHangTextField.getText();
            String bienSo = bienSoXeTextField.getText();
            LocalDate ngayBatDau = ngayBatDauDatePicker.getValue();
            LocalDate ngayKetThuc = ngayKetThucDatePicker.getValue();

            connection.setAutoCommit(false);
            // Cập nhật thông tin thẻ tháng
            PreparedStatement pstmtUpdate = connection.prepareStatement(
                    "UPDATE the_thang SET ho_ten_khach_hang = ?, sdt_khach_hang = ?, bien_so = ?," +
                            "ngay_bat_dau = ?, ngay_ket_thuc = ? WHERE the_thang_id = ?"
            );
            pstmtUpdate.setString(1, hoTenKH);
            pstmtUpdate.setString(2, sdtKH);
            pstmtUpdate.setString(3, bienSo);
            pstmtUpdate.setDate(4, java.sql.Date.valueOf(ngayBatDau));
            pstmtUpdate.setDate(5, java.sql.Date.valueOf(ngayKetThuc));
            pstmtUpdate.setInt(6, maTheThang);

            int rowsAffected = pstmtUpdate.executeUpdate();
            connection.commit();

            if (rowsAffected > 0) {
                // Hiển thị thông báo thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã cập nhật thẻ tháng thành công!\n\n" +
                        "Mã thẻ tháng: " + maTheThang + "\n" +
                        "Khách hàng: " + hoTenKH + "\n" +
                        "Hiệu lực: " + ngayBatDau.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " đến " + ngayKetThuc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                // Tùy chỉnh nút OK
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Đóng");

                // Hiển thị dialog và đợi người dùng đóng
                alert.showAndWait();

                // Làm mới form
                handleLamMoi(null);

                // Tải lại dữ liệu
                loadTableViewData();
            } else {
                thongBaoLabel.setText("Không tìm thấy thẻ tháng để cập nhật!");
            }
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

        suggestNewTheThangId();


        hoTenKhachHangTextField.clear();
        sdtKhachHangTextField.clear();
//        loaiXeComboBox.setValue(null);
        bienSoXeTextField.clear();


        ngayBatDauDatePicker.setValue(LocalDate.now());
        ngayKetThucDatePicker.setValue(LocalDate.now().plusMonths(1));


        thongBaoLabel.setText("");


        theThangTableView.getSelectionModel().clearSelection();
    }


    @FXML
    private void handleLamMoiDanhSach() {
        loadTableViewData();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleGiaHanThe(ActionEvent event) {
        TheThangInfo selectedTheThang = theThangTableView.getSelectionModel().getSelectedItem();
        
        if (selectedTheThang == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thẻ tháng cần gia hạn!");
            return;
        }

        try {
            int maTheThang = selectedTheThang.getMaTheThang();
            LocalDate ngayKetThucCu = selectedTheThang.getNgayKetThuc();
            LocalDate ngayKetThucMoi = ngayKetThucCu.plusMonths(1);

            // Cập nhật ngày kết thúc
            PreparedStatement pstmtUpdate = connection.prepareStatement(
                    "UPDATE the_thang SET ngay_ket_thuc = ? WHERE the_thang_id = ?"
            );
            pstmtUpdate.setDate(1, java.sql.Date.valueOf(ngayKetThucMoi));
            pstmtUpdate.setInt(2, maTheThang);

            int rowsAffected = pstmtUpdate.executeUpdate();

            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã gia hạn thẻ tháng thành công!\n\n" +
                        "Mã thẻ tháng: " + maTheThang + "\n" +
                        "Ngày hết hạn mới: " + ngayKetThucMoi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Đóng");
                alert.showAndWait();


                loadTableViewData();
                theThangTableView.getSelectionModel().clearSelection();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thẻ tháng để gia hạn!");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gia hạn thẻ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHuyThe(ActionEvent event) {
        TheThangInfo selectedTheThang = theThangTableView.getSelectionModel().getSelectedItem();
        
        if (selectedTheThang == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thẻ tháng cần hủy!");
            return;
        }

        int maTheThang = selectedTheThang.getMaTheThang();

        try {

            PreparedStatement pstmtCheck = connection.prepareStatement(
                    "SELECT the_id FROM the_thang WHERE the_thang_id = ?"
            );
            pstmtCheck.setInt(1, maTheThang);
            ResultSet rsCheck = pstmtCheck.executeQuery();

            if (rsCheck.next()) {
                Integer theId = null;
                int tempId = rsCheck.getInt("the_id");
                if (!rsCheck.wasNull()) {
                    theId = tempId;
                }


                if (theId != null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", 
                            "Không thể hủy thẻ tháng này vì thẻ đang được sử dụng!\n\n" +
                            "Vui lòng xuất xe trước khi hủy thẻ.");
                    return;
                }
            }


            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận hủy thẻ");
            confirmAlert.setHeaderText("Bạn có chắc muốn hủy thẻ tháng này?");
            confirmAlert.setContentText("Khách hàng: " + selectedTheThang.getHoTenKH() + "\n" +
                    "Mã thẻ: " + selectedTheThang.getMaTheThang() + "\n\n" +
                    "Hành động này sẽ hủy thẻ và không thể hoàn tác!");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                PreparedStatement pstmtDelete = connection.prepareStatement(
                        "DELETE FROM the_thang WHERE the_thang_id = ?"
                );
                pstmtDelete.setInt(1, maTheThang);

                int rowsAffected = pstmtDelete.executeUpdate();

                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Đã hủy thẻ tháng thành công!\n\n" +
                            "Mã thẻ tháng: " + maTheThang + "\n" +
                            "Khách hàng: " + selectedTheThang.getHoTenKH());

                    ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Đóng");
                    alert.showAndWait();


                    loadTableViewData();
                    theThangTableView.getSelectionModel().clearSelection();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thẻ tháng để hủy!");
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy thẻ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("EmployeeUI.fxml");
    }
}

