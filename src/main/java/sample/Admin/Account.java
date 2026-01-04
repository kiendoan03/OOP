package sample.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import sample.Main;
import sample.DatabaseConnection;

public class Account extends AdminUI implements Initializable {

    // FXML Controls
    @FXML
    private TableView<NhanVien> tableNhanVien;

    @FXML
    private TableColumn<NhanVien, Integer> colID;

    @FXML
    private TableColumn<NhanVien, String> colHoTen;

    @FXML
    private TableColumn<NhanVien, String> colNgaySinh;

    @FXML
    private TableColumn<NhanVien, Integer> colSDT;

    @FXML
    private TableColumn<NhanVien, String> colTaiKhoan;

    @FXML
    private TableColumn<NhanVien, String> colMatKhau;

    @FXML
    private TableColumn<NhanVien, String> colQuyen;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtHoTen;

    @FXML
    private TextField txtNgaySinh;

    @FXML
    private TextField txtSDT;

    @FXML
    private TextField txtTaiKhoan;

    @FXML
    private TextField txtMatKhau;

    @FXML
    private ComboBox<String> cbQuyen;

    @FXML
    private Button btnThem;

    @FXML
    private Button btnSua;

    @FXML
    private Button btnXoa;

    @FXML
    private Button btnLamMoi;

    // Database connection
    private Connection connection;
    private PreparedStatement pst;
    private ResultSet rs;

    // ObservableList to store data
    private ObservableList<NhanVien> listNhanVien = FXCollections.observableArrayList();

    // Initialize method required by Initializable interface
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the database connection
        initializeDatabase();

        // Load quyền options to ComboBox
        cbQuyen.getItems().addAll("Admin", "Nhân viên");
        cbQuyen.setValue("Nhân viên"); // Set default value

        // Initialize the TableView columns
        initColumns();

        // Load data from database
        loadDataFromDatabase();

        // Add listener for table row selection
        tableNhanVien.setOnMouseClicked(this::handleTableClick);
    }

    // Initialize database connection
    private void initializeDatabase() {
        connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            System.out.println("Sử dụng kết nối SQL Server từ DatabaseConnection!");
        }
    }

    // Initialize TableView columns
    private void initColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("nhanvienId"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colNgaySinh.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colTaiKhoan.setCellValueFactory(new PropertyValueFactory<>("taiKhoan"));
        colMatKhau.setCellValueFactory(new PropertyValueFactory<>("matKhau"));
        colQuyen.setCellValueFactory(new PropertyValueFactory<>("quyenTk"));
    }

    // Load data from database to TableView
    private void loadDataFromDatabase() {
        try {
            listNhanVien.clear();

            String query = "SELECT * FROM nhan_vien";
            pst = connection.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                NhanVien nhanVien = new NhanVien(
                        rs.getInt("nhanvien_id"),
                        rs.getString("hoten"),
                        rs.getString("ngaysinh"),
                        rs.getInt("sdt"),
                        rs.getString("tai_khoan"),
                        rs.getString("mat_khau"),
                        rs.getString("quyen_tk")
                );

                listNhanVien.add(nhanVien);
            }

            tableNhanVien.setItems(listNhanVien);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load data", e.getMessage());
        } finally {
            closeResources(null, pst, rs);
        }
    }

    // Handle table row click
    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            NhanVien selectedNhanVien = tableNhanVien.getSelectionModel().getSelectedItem();

            if (selectedNhanVien != null) {
                txtID.setText(String.valueOf(selectedNhanVien.getNhanvienId()));
                txtHoTen.setText(selectedNhanVien.getHoTen());
                txtNgaySinh.setText(selectedNhanVien.getNgaySinh());
                txtSDT.setText(String.valueOf(selectedNhanVien.getSdt()));
                txtTaiKhoan.setText(selectedNhanVien.getTaiKhoan());
                txtMatKhau.setText(selectedNhanVien.getMatKhau());
                cbQuyen.setValue(selectedNhanVien.getQuyenTk());
            }
        }
    }

    // Add new employee
    @FXML
    private void handleThemAction(ActionEvent event) {
        try {
            // Validate input
            if (validateInput()) {
                // Check if ID already exists
                if (checkIfIdExists(Integer.parseInt(txtID.getText()))) {
                    showAlert(Alert.AlertType.ERROR, "Error", "ID already exists",
                            "An employee with this ID already exists. Please use a different ID.");
                    return;
                }

                String query = "INSERT INTO nhan_vien (nhanvien_id, hoten, ngaysinh, sdt, tai_khoan, mat_khau, quyen_tk) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                pst = connection.prepareStatement(query);
                pst.setInt(1, Integer.parseInt(txtID.getText()));
                pst.setString(2, txtHoTen.getText());
                pst.setString(3, txtNgaySinh.getText());
                pst.setInt(4, Integer.parseInt(txtSDT.getText()));
                pst.setString(5, txtTaiKhoan.getText());
                pst.setString(6, txtMatKhau.getText());
                pst.setString(7, cbQuyen.getValue());

                pst.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee Added",
                        "Employee has been successfully added to the database.");

                // Refresh the table
                loadDataFromDatabase();

                // Clear input fields
                clearFields();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add employee", e.getMessage());
        } finally {
            closeResources(null, pst, null);
        }
    }

    // Update existing employee
    @FXML
    private void handleSuaAction(ActionEvent event) {
        try {
            // Validate input
            if (validateInput()) {
                // Check if ID exists
                if (!checkIfIdExists(Integer.parseInt(txtID.getText()))) {
                    showAlert(Alert.AlertType.ERROR, "Error", "ID not found",
                            "No employee with this ID exists in the database.");
                    return;
                }

                String query = "UPDATE nhan_vien SET hoten = ?, ngaysinh = ?, sdt = ?, tai_khoan = ?, mat_khau = ?, quyen_tk = ? " +
                        "WHERE nhanvien_id = ?";

                pst = connection.prepareStatement(query);
                pst.setString(1, txtHoTen.getText());
                pst.setString(2, txtNgaySinh.getText());
                pst.setInt(3, Integer.parseInt(txtSDT.getText()));
                pst.setString(4, txtTaiKhoan.getText());
                pst.setString(5, txtMatKhau.getText());
                pst.setString(6, cbQuyen.getValue());
                pst.setInt(7, Integer.parseInt(txtID.getText()));

                pst.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee Updated",
                        "Employee information has been successfully updated.");

                // Refresh the table
                loadDataFromDatabase();

                // Clear input fields
                clearFields();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update employee", e.getMessage());
        } finally {
            closeResources(null, pst, null);
        }
    }

    // Delete employee
    @FXML
    private void handleXoaAction(ActionEvent event) {
        try {
            // Check if ID is provided
            if (txtID.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "No ID specified",
                        "Please select an employee to delete.");
                return;
            }

            // Check if ID exists
            if (!checkIfIdExists(Integer.parseInt(txtID.getText()))) {
                showAlert(Alert.AlertType.ERROR, "Error", "ID not found",
                        "No employee with this ID exists in the database.");
                return;
            }

            String query = "DELETE FROM nhan_vien WHERE nhanvien_id = ?";

            pst = connection.prepareStatement(query);
            pst.setInt(1, Integer.parseInt(txtID.getText()));

            pst.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee Deleted",
                    "Employee has been successfully removed from the database.");

            // Refresh the table
            loadDataFromDatabase();

            // Clear input fields
            clearFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete employee", e.getMessage());
        } finally {
            closeResources(null, pst, null);
        }
    }

    // Clear all input fields
    @FXML
    private void handleLamMoiAction(ActionEvent event) {
        clearFields();
    }

    // Clear input fields helper method
    private void clearFields() {
        txtID.clear();
        txtHoTen.clear();
        txtNgaySinh.clear();
        txtSDT.clear();
        txtTaiKhoan.clear();
        txtMatKhau.clear();
        cbQuyen.setValue("Nhân viên");
        tableNhanVien.getSelectionModel().clearSelection();
    }

    // Check if an employee ID already exists
    private boolean checkIfIdExists(int id) throws SQLException {
        String query = "SELECT COUNT(*) FROM nhan_vien WHERE nhanvien_id = ?";
        PreparedStatement checkStatement = null;
        ResultSet checkResult = null;

        try {
            checkStatement = connection.prepareStatement(query);
            checkStatement.setInt(1, id);

            checkResult = checkStatement.executeQuery();

            if (checkResult.next()) {
                return checkResult.getInt(1) > 0;
            }

            return false;
        } finally {
            closeResources(null, checkStatement, checkResult);
        }
    }

    // Validate input fields
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (txtID.getText().isEmpty()) {
            errorMessage.append("ID is required.\n");
        } else {
            try {
                Integer.parseInt(txtID.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("ID must be a number.\n");
            }
        }

        if (txtHoTen.getText().isEmpty()) {
            errorMessage.append("Full name is required.\n");
        }

        if (txtNgaySinh.getText().isEmpty()) {
            errorMessage.append("Birth date is required.\n");
        }

        if (txtSDT.getText().isEmpty()) {
            errorMessage.append("Phone number is required.\n");
        } else {
            try {
                Integer.parseInt(txtSDT.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("Phone number must be a number.\n");
            }
        }

        if (txtTaiKhoan.getText().isEmpty()) {
            errorMessage.append("Account is required.\n");
        }

        if (txtMatKhau.getText().isEmpty()) {
            errorMessage.append("Password is required.\n");
        }

        if (cbQuyen.getValue() == null) {
            errorMessage.append("Permission is required.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please correct the following errors:",
                    errorMessage.toString());
            return false;
        }

        return true;
    }

    // Show alert dialog
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Close database resources
    private void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            // We don't close the connection here as it's used throughout the application
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Employee model class
    public static class NhanVien {
        private int nhanvienId;
        private String hoTen;
        private String ngaySinh;
        private int sdt;
        private String taiKhoan;
        private String matKhau;
        private String quyenTk;

        public NhanVien(int nhanvienId, String hoTen, String ngaySinh, int sdt, String taiKhoan, String matKhau, String quyenTk) {
            this.nhanvienId = nhanvienId;
            this.hoTen = hoTen;
            this.ngaySinh = ngaySinh;
            this.sdt = sdt;
            this.taiKhoan = taiKhoan;
            this.matKhau = matKhau;
            this.quyenTk = quyenTk;
        }

        // Getters and Setters
        public int getNhanvienId() {
            return nhanvienId;
        }

        public void setNhanvienId(int nhanvienId) {
            this.nhanvienId = nhanvienId;
        }

        public String getHoTen() {
            return hoTen;
        }

        public void setHoTen(String hoTen) {
            this.hoTen = hoTen;
        }

        public String getNgaySinh() {
            return ngaySinh;
        }

        public void setNgaySinh(String ngaySinh) {
            this.ngaySinh = ngaySinh;
        }

        public int getSdt() {
            return sdt;
        }

        public void setSdt(int sdt) {
            this.sdt = sdt;
        }

        public String getTaiKhoan() {
            return taiKhoan;
        }

        public void setTaiKhoan(String taiKhoan) {
            this.taiKhoan = taiKhoan;
        }

        public String getMatKhau() {
            return matKhau;
        }

        public void setMatKhau(String matKhau) {
            this.matKhau = matKhau;
        }

        public String getQuyenTk() {
            return quyenTk;
        }

        public void setQuyenTk(String quyenTk) {
            this.quyenTk = quyenTk;
        }
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("AdminUI.fxml");
    }
}

