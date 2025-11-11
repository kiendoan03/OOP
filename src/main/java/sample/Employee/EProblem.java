package sample.Employee;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sample.Main;
import sample.DatabaseConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EProblem extends EmployeeUI implements Initializable {

    private Connection connection;

    // FXML Components
    @FXML
    private TableView<SuCoDisplay> suCoTable;
    @FXML
    private TableColumn<SuCoDisplay, Integer> colSuCoId;
    @FXML
    private TableColumn<SuCoDisplay, String> colBienSoXe;
    @FXML
    private TableColumn<SuCoDisplay, String> colNhanVien;
    @FXML
    private TableColumn<SuCoDisplay, String> colNgayGio;
    @FXML
    private TableColumn<SuCoDisplay, String> colMoTa;

    @FXML
    private TextField suCoIdTextField;
    @FXML
    private ComboBox<String> theComboBox;
    @FXML
    private ComboBox<String> nhanVienComboBox;
    @FXML
    private TextArea moTaTextArea;
    @FXML
    private DatePicker ngayPicker;
    @FXML
    private TextField gioTextField;
    @FXML
    private Button themButton;
    @FXML
    private Button suaButton;
    @FXML
    private Button xoaButton;
    @FXML
    private Button lamMoiButton;

    // Data
    private ObservableList<SuCoDisplay> suCoList = FXCollections.observableArrayList();
    private SuCoDisplay selectedSuCo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize connection nếu cần
        initializeConnection();

        setupTableColumns();
        setupEventHandlers();
        loadComboBoxData();
        loadSuCoData();

        // Set default values
        ngayPicker.setValue(java.time.LocalDate.now());
        gioTextField.setText(java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
//        suCoIdTextField.setDisable(true); // Disable ID field khi thêm mới
    }

    // Method để initialize connection - customize theo project anh
//    private void initializeConnection() {
//        try {
//            // Thay đổi connection string theo database anh
//            String url = "jdbc:mysql://localhost:3306/newdoanhdt";
//            String username = "root";
//            String password = "nam160403";
//            connection = DriverManager.getConnection(url, username, password);
//        } catch (SQLException e) {
//            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể kết nối database: " + e.getMessage());
//        }
//    }
    private void initializeConnection() {
        connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            System.out.println("✅ Sử dụng kết nối SQL Server từ DatabaseConnection!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể lấy kết nối SQL Server!");
        }
    }

    // Method showAlert - add vào class này
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupTableColumns() {
        colSuCoId.setCellValueFactory(new PropertyValueFactory<>("suCoId"));
        colBienSoXe.setCellValueFactory(new PropertyValueFactory<>("bienSoXe"));
        colNhanVien.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        colNgayGio.setCellValueFactory(new PropertyValueFactory<>("ngayGioString"));
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));

        suCoTable.setItems(suCoList);
    }

    private void setupEventHandlers() {
        suCoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedSuCo = newSelection;
                fillFormFields();
            }
        });
    }

    private void loadComboBoxData() {
        loadTheComboBox();
        loadNhanVienComboBox();
    }

    private void loadTheComboBox() {
        try {
            String query = "SELECT t.the_id, t.bien_so_xe, tn.the_ngay_id, tt.the_thang_id " +
                    "FROM the t " +
                    "LEFT JOIN the_ngay tn ON t.the_id = tn.the_id " +
                    "LEFT JOIN the_thang tt ON t.the_id = tt.the_id";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ObservableList<String> options = FXCollections.observableArrayList();
            while (rs.next()) {
                int theId = rs.getInt("the_id");
                String bienSoXe = rs.getString("bien_so_xe");
                int theNgayId = rs.getInt("the_ngay_id");

                Integer theThangId = null;
                int tempThangId = rs.getInt("the_thang_id");
                if (!rs.wasNull()) {
                    theThangId = tempThangId;
                }

                String displayText;
                if (theThangId != null) {
                    displayText = theId + " - " + bienSoXe + " (Tháng ID: " + theThangId + ")";
                } else {
                    displayText = theId + " - " + bienSoXe + " (Ngày ID: " + theNgayId + ")";
                }
                options.add(displayText);
            }
            theComboBox.setItems(options);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách thẻ: " + e.getMessage());
        }
    }

    private void loadNhanVienComboBox() {
        try {
            String query = "SELECT nhanvien_id, hoten FROM nhan_vien WHERE quyen_tk = 'Nhân viên'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ObservableList<String> options = FXCollections.observableArrayList();
            while (rs.next()) {
                options.add(rs.getInt("nhanvien_id") + " - " + rs.getString("hoten"));
            }
            nhanVienComboBox.setItems(options);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách nhân viên: " + e.getMessage());
        }
    }

    private void loadSuCoData() {
        suCoList.clear();
        try {
            String query = "SELECT sc.su_co_id, sc.the_id, sc.nhan_vien_id, sc.ngay_gio, sc.mo_ta, " +
                    "t.bien_so_xe, nv.hoten " +
                    "FROM su_co sc " +
                    "JOIN the t ON sc.the_id = t.the_id " +
                    "JOIN nhan_vien nv ON sc.nhan_vien_id = nv.nhanvien_id " +
                    "ORDER BY sc.ngay_gio DESC";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                SuCoDisplay suCo = new SuCoDisplay(
                        rs.getInt("su_co_id"),
                        rs.getInt("the_id"),
                        rs.getInt("nhan_vien_id"),
                        rs.getTimestamp("ngay_gio").toLocalDateTime(),
                        rs.getString("mo_ta"),
                        rs.getString("bien_so_xe"),
                        rs.getString("hoten")
                );
                suCoList.add(suCo);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu sự cố: " + e.getMessage());
        }
    }

    @FXML
    private void handleThemSuCo(ActionEvent event) {
        if (!validateInputForAdd()) return;

        try {
            String query;
            PreparedStatement pstmt;

            if (suCoIdTextField.getText().trim().isEmpty()) {
                // Auto increment ID
                query = "INSERT INTO su_co (the_id, nhan_vien_id, ngay_gio, mo_ta) VALUES (?, ?, ?, ?)";
                pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, extractIdFromComboBox(theComboBox.getValue()));
                pstmt.setInt(2, extractIdFromComboBox(nhanVienComboBox.getValue()));
                pstmt.setTimestamp(3, Timestamp.valueOf(getSelectedDateTime()));
                pstmt.setString(4, moTaTextArea.getText().trim());
            } else {
                // Specify ID
                query = "INSERT INTO su_co (su_co_id, the_id, nhan_vien_id, ngay_gio, mo_ta) VALUES (?, ?, ?, ?, ?)";
                pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, Integer.parseInt(suCoIdTextField.getText().trim()));
                pstmt.setInt(2, extractIdFromComboBox(theComboBox.getValue()));
                pstmt.setInt(3, extractIdFromComboBox(nhanVienComboBox.getValue()));
                pstmt.setTimestamp(4, Timestamp.valueOf(getSelectedDateTime()));
                pstmt.setString(5, moTaTextArea.getText().trim());
            }

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm sự cố thành công!");
            clearForm();
            loadSuCoData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm sự cố: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "ID sự cố phải là số!");
        }
    }

    @FXML
    private void handleSuaSuCo(ActionEvent event) {
        if (selectedSuCo == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn sự cố cần sửa!");
            return;
        }

        if (!validateInput()) return;

        try {
            String query = "UPDATE su_co SET the_id = ?, nhan_vien_id = ?, ngay_gio = ?, mo_ta = ? WHERE su_co_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);

            pstmt.setInt(1, extractIdFromComboBox(theComboBox.getValue()));
            pstmt.setInt(2, extractIdFromComboBox(nhanVienComboBox.getValue()));
            pstmt.setTimestamp(3, Timestamp.valueOf(getSelectedDateTime()));
            pstmt.setString(4, moTaTextArea.getText().trim());
            pstmt.setInt(5, selectedSuCo.getSuCoId());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật sự cố thành công!");
            clearForm();
            loadSuCoData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật sự cố: " + e.getMessage());
        }
    }

    @FXML
    private void handleXoaSuCo(ActionEvent event) {
        if (selectedSuCo == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn sự cố cần xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc muốn xóa sự cố này?");
        confirmAlert.setContentText("Hành động này không thể hoàn tác!");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                String query = "DELETE FROM su_co WHERE su_co_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, selectedSuCo.getSuCoId());

                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa sự cố thành công!");
                clearForm();
                loadSuCoData();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa sự cố: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLamMoi(ActionEvent event) {
        clearForm();
        loadSuCoData();
    }

    private void fillFormFields() {
        if (selectedSuCo != null) {
            // Set ID field
            suCoIdTextField.setText(String.valueOf(selectedSuCo.getSuCoId()));

            // Set ComboBox values
            for (String item : theComboBox.getItems()) {
                if (item.startsWith(selectedSuCo.getTheId() + " -")) {
                    theComboBox.setValue(item);
                    break;
                }
            }

            for (String item : nhanVienComboBox.getItems()) {
                if (item.startsWith(selectedSuCo.getNhanVienId() + " -")) {
                    nhanVienComboBox.setValue(item);
                    break;
                }
            }

            // Set date and time
            ngayPicker.setValue(selectedSuCo.getNgayGio().toLocalDate());
            gioTextField.setText(selectedSuCo.getNgayGio().format(DateTimeFormatter.ofPattern("HH:mm")));
            moTaTextArea.setText(selectedSuCo.getMoTa());
        }
    }

    private void clearForm() {
        suCoIdTextField.clear();
        theComboBox.setValue(null);
        nhanVienComboBox.setValue(null);
        ngayPicker.setValue(java.time.LocalDate.now());
        gioTextField.setText(java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        moTaTextArea.clear();
        selectedSuCo = null;
        suCoTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (theComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thẻ!");
            return false;
        }

        if (nhanVienComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn nhân viên!");
            return false;
        }

        if (ngayPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngày!");
            return false;
        }

        if (gioTextField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giờ!");
            return false;
        }

        if (moTaTextArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mô tả sự cố!");
            return false;
        }

        // Validate time format
        try {
            java.time.LocalTime.parse(gioTextField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Định dạng giờ không hợp lệ! (HH:mm)");
            return false;
        }

        return true;
    }

    private boolean validateInputForAdd() {
        // Validate ID nếu có nhập
        if (!suCoIdTextField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(suCoIdTextField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "ID sự cố phải là số!");
                return false;
            }
        }

        return validateInput();
    }

    private LocalDateTime getSelectedDateTime() {
        java.time.LocalDate date = ngayPicker.getValue();
        java.time.LocalTime time = java.time.LocalTime.parse(gioTextField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        return LocalDateTime.of(date, time);
    }

    private int extractIdFromComboBox(String value) {
        if (value == null) return -1;
        return Integer.parseInt(value.split(" - ")[0]);
    }

    // Inner class for display - có thể tách ra file riêng nếu cần
    public static class SuCoDisplay {
        private int suCoId;
        private int theId;
        private int nhanVienId;
        private LocalDateTime ngayGio;
        private String moTa;
        private String bienSoXe;
        private String tenNhanVien;

        public SuCoDisplay(int suCoId, int theId, int nhanVienId, LocalDateTime ngayGio,
                           String moTa, String bienSoXe, String tenNhanVien) {
            this.suCoId = suCoId;
            this.theId = theId;
            this.nhanVienId = nhanVienId;
            this.ngayGio = ngayGio;
            this.moTa = moTa;
            this.bienSoXe = bienSoXe;
            this.tenNhanVien = tenNhanVien;
        }

        // Getters
        public int getSuCoId() {
            return suCoId;
        }

        public int getTheId() {
            return theId;
        }

        public int getNhanVienId() {
            return nhanVienId;
        }

        public LocalDateTime getNgayGio() {
            return ngayGio;
        }

        public String getMoTa() {
            return moTa;
        }

        public String getBienSoXe() {
            return bienSoXe;
        }

        public String getTenNhanVien() {
            return tenNhanVien;
        }

        public String getNgayGioString() {
            return ngayGio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("EmployeeUI.fxml");
    }
}
