package sample.Employee;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import sample.DatabaseConnection;

public class ParkingOut extends EmployeeUI implements Initializable{

    @FXML
    private ComboBox<String> loaiTheBox;
    @FXML
    private ComboBox<String> maTheBox;
    @FXML
    private Label loaiXeLabel;
    @FXML
    private Label khuLabel;
    @FXML
    private Label bienSoXeLabel;
    @FXML
    private Label ngayNhanLabel;
    @FXML
    private Button deleteVehicle;
    @FXML
    private Button lamMoi;

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

        loaiTheBox.setOnAction(event -> {
            String selectedValue = loaiTheBox != null ? loaiTheBox.getValue() : null;
            if ("Thẻ ngày".equals(selectedValue)) {
                try {
                    // Truy vấn dữ liệu từ cơ sở dữ liệu
                    String query = "SELECT MaTheNgay, LoaiThe FROM the_ngay WHERE (MaTheNgay IS NOT NULL) AND (LoaiThe IS NOT NULL)";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    // Khởi tạo danh sách options
                    ObservableList<String> options3 = FXCollections.observableArrayList();

                    // Lấy dữ liệu từ ResultSet và thêm vào danh sách
                    while (resultSet.next()) {
                        options3.add(resultSet.getString("MaTheNgay"));
                    }

                    // Đưa danh sách options vào ComboBox
                    maTheBox.setItems(options3);

                    // Xoá giá trị của các box
                    loaiXeLabel.setText(null);
                    bienSoXeLabel.setText(null);
                    khuLabel.setText(null);
                    ngayNhanLabel.setText(null);

                    statement.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR,"Thông báo","Lỗi", String.valueOf(e));
                }

                maTheBox.setOnAction(event1 -> {
                    String selectedValue1 = maTheBox.getValue() != null ? maTheBox.getValue() : null;
                    try {
                        String query = "SELECT LoaiXe, BienSoXe, Khu, NgayNhan FROM the_ngay NATURAL JOIN nhan_tra_xe_ngay WHERE MaTheNgay = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, selectedValue1);
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            String loaiXe = resultSet.getString("LoaiXe");
                            String bienSoXe = resultSet.getString("BienSoXe");
                            String khu = resultSet.getString("Khu");
                            Date ngayNhan = resultSet.getDate("NgayNhan");
                            loaiXeLabel.setText(loaiXe);
                            bienSoXeLabel.setText(bienSoXe);
                            khuLabel.setText(khu);
                            ngayNhanLabel.setText(String.valueOf(ngayNhan.toLocalDate()));
                        }

                        statement.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR,"Thông báo","Lỗi", String.valueOf(e));
                    }
                });

            } else if ("Thẻ tháng".equals(selectedValue)) {
                try {
                    // Xoá dữ liệu trong maTheBox
                    maTheBox.getItems().clear();
                    loaiXeLabel.setText(null);
                    bienSoXeLabel.setText(null);
                    khuLabel.setText(null);
                    ngayNhanLabel.setText(null);

                    // Truy vấn dữ liệu từ cơ sở dữ liệu
                    String query = "SELECT MaTheThang,LoaiThe FROM the_thang NATURAL JOIN nhan_tra_xe_thang WHERE (MaTheThang IS NOT NULL) AND (LoaiThe IS NOT NULL)";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    // Khởi tạo danh sách options
                    ObservableList<String> options4 = FXCollections.observableArrayList();

                    // Lấy dữ liệu từ ResultSet và thêm vào danh sách
                    while (resultSet.next()) {
                        options4.add(resultSet.getString("MaTheThang"));
                    }

                    // Đưa danh sách options vào ComboBox
                    maTheBox.setItems(options4);

                    statement.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR,"Thông báo","Lỗi", String.valueOf(e));
                }
                maTheBox.setOnAction(event1 -> {
                    String selectedValue1 = maTheBox.getValue() != null ? maTheBox.getValue() : null;
                    try {
                        String query = "SELECT LoaiXe, BienSoXe, Khu, NgayNhan FROM the_thang NATURAL JOIN nhan_tra_xe_thang WHERE MaTheThang = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, selectedValue1);
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            String loaiXe = resultSet.getString("LoaiXe");
                            String bienSoXe = resultSet.getString("BienSoXe");
                            String khu = resultSet.getString("Khu");
                            Date ngayNhan = resultSet.getDate("NgayNhan");
                            loaiXeLabel.setText(loaiXe);
                            bienSoXeLabel.setText(bienSoXe);
                            khuLabel.setText(khu);
                            ngayNhanLabel.setText(String.valueOf(ngayNhan.toLocalDate()));
                        }

                        statement.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR,"Thông báo","Lỗi", String.valueOf(e));
                    }
                });
            }
        });
    }

    public void TraXe(){
        String loaiThe = loaiTheBox.getValue() != null ? loaiTheBox.getValue() : null;
        String maThe = maTheBox.getValue() != null ? maTheBox.getValue() : null;
        String loaiXe = loaiXeLabel.getText();
        String khu = khuLabel.getText();
        String bienSoXe = bienSoXeLabel.getText();
        String ngayNhan = ngayNhanLabel.getText();

        // Kiểm tra dữ liệu đầu vào
        if (loaiThe == null || loaiXe == null || bienSoXe.isEmpty() || maThe == null || khu == null || ngayNhan == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thiếu thông tin", "Hãy điền dữ liệu vào các ô còn trống !");
            return;
        }
        try {
            if (loaiThe.equals("Thẻ ngày")) {
                String query = "UPDATE the_ngay SET LoaiThe = null, LoaiXe = null, BienSoXe = null WHERE MaTheNgay = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, maThe);

                String query1 = "DELETE FROM nhan_tra_xe_ngay WHERE MaTheNgay = ?";
                PreparedStatement statement1 = connection.prepareStatement(query1);
                statement1.setString(1,maThe);

                String query2 = "UPDATE thong_ke_the_ngay SET NgayTra = CAST(GETDATE() AS DATE), GioTra = CAST(GETDATE() AS TIME) WHERE MaThe = ?";
                PreparedStatement statement2 = connection.prepareStatement(query2);
                statement2.setString(1,maThe);

                // Thực thi câu truy vấn
                int rowsAffected = statement.executeUpdate();
                int rowsAffected1 = statement1.executeUpdate();
                int rowsAffected2 = statement2.executeUpdate();

                String query3 = "SELECT ngayTra, gioTra FROM thong_ke_the_ngay WHERE MaThe = ?";
                PreparedStatement statement3 = connection.prepareStatement(query3);
                statement3.setString(1,maThe);
                ResultSet resultSet3 = statement3.executeQuery();

                if (resultSet3.next()) {
                    String ngayTra = resultSet3.getString("NgayTra");
                    String gioTra = resultSet3.getString("GioTra");
                    if (rowsAffected > 0 && rowsAffected1 > 0 && rowsAffected2 > 0) {
                        String message = "";
                        message += "Mã thẻ: " + maThe + "\n";
                        message += "\n\tLoại xe: " + loaiXe + "\n";
                        message += "\n\tKhu: " + khu + "\n";
                        message += "\n\tBiển số xe: " + bienSoXe + "\n";
                        message += "\n\tLoại thẻ: " + loaiThe + "\n";
                        message += "\n\tNgày trả: " + ngayTra + "\n";
                        message += "\n\tGiờ trả: " + gioTra + "\n";
                        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xuất hoá đơn thành công!", message);
                        clearFields();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thông báo", "Cập nhật dữ liệu thất bại!", "Có lỗi xảy ra trong quá trình thực hiện.");
                    }
                }

                statement.close();
                statement1.close();
                statement2.close();
                statement3.close();
            }

            else if(loaiThe.equals("Thẻ tháng")) {
                String query = "UPDATE the_thang SET Do = '0' WHERE MaTheThang = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, maThe);

                String query1 = "DELETE FROM nhan_tra_xe_thang WHERE MaTheThang = ?";
                PreparedStatement statement1 = connection.prepareStatement(query1);
                statement1.setString(1, maThe);

                String query2 = "UPDATE thong_ke_the_thang SET NgayTra = CAST(GETDATE() AS DATE), GioTra = CAST(GETDATE() AS TIME) WHERE MaThe = ?";
                PreparedStatement statement2 = connection.prepareStatement(query2);
                statement2.setString(1, maThe);

                // Thực thi câu truy vấn
                int rowsAffected = statement.executeUpdate();
                int rowsAffected1 = statement1.executeUpdate();
                int rowsAffected2 = statement2.executeUpdate();

                String query3 = "SELECT ngayTra, gioTra FROM thong_ke_the_thang WHERE MaThe = ?";
                PreparedStatement statement3 = connection.prepareStatement(query3);
                statement3.setString(1, maThe);
                ResultSet resultSet3 = statement3.executeQuery();

                if (resultSet3.next()) {
                    String ngayTra = resultSet3.getString("NgayTra");
                    String gioTra = resultSet3.getString("GioTra");
                    if (rowsAffected > 0 && rowsAffected1 > 0 && rowsAffected2 > 0) {
                        String message = "";
                        message += "Mã thẻ: " + maThe + "\n";
                        message += "\n\tLoại xe: " + loaiXe + "\n";
                        message += "\n\tKhu: " + khu + "\n";
                        message += "\n\tBiển số xe: " + bienSoXe + "\n";
                        message += "\n\tLoại thẻ: " + loaiThe + "\n";
                        message += "\n\tNgày trả: " + ngayTra + "\n";
                        message += "\n\tGiờ trả: " + gioTra + "\n";
                        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xuất hoá đơn thành công!", message);
                        clearFields();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thông báo", "Cập nhật dữ liệu thất bại!", "Có lỗi xảy ra trong quá trình thực hiện.");
                    }
                }

                statement.close();
                statement1.close();
                statement2.close();
                statement3.close();
            }

        } catch(SQLException e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Thông báo","Lỗi", String.valueOf(e));
        }
    }

    public void reLoadData() {
        loaiTheBox.setValue(null);
        maTheBox.setValue(null);
        loaiXeLabel.setText(null);
        khuLabel.setText(null);
        bienSoXeLabel.setText(null);
        ngayNhanLabel.setText(null);
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
        loaiXeLabel.setText(null);
        khuLabel.setText(null);
        bienSoXeLabel.setText(null);
        ngayNhanLabel.setText(null);
    }
}

