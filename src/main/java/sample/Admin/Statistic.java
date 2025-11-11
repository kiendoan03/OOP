package sample.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.Main;
import sample.DatabaseConnection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Statistic implements Initializable {



    @FXML private Label ngayHienTaiLabel;
    @FXML private Label thongBaoLabel;

    @FXML private ComboBox<String> loaiThoiGianComboBox;
    @FXML private DatePicker tuNgayDatePicker;
    @FXML private DatePicker denNgayDatePicker;

    @FXML private Label tongTheNgayLabel;
    @FXML private Label doanhThuTheNgayLabel;
    @FXML private Label theThangMoiLabel;
    @FXML private Label tongDoanhThuLabel;


    @FXML private TableView<ThongKeDoanhThuInfo> doanhThuTableView;
    @FXML private TableColumn<ThongKeDoanhThuInfo, String> thoiGianColumn;
    @FXML private TableColumn<ThongKeDoanhThuInfo, Integer> soTheNgayColumn;
    @FXML private TableColumn<ThongKeDoanhThuInfo, Double> doanhThuTheNgayColumn;
    @FXML private TableColumn<ThongKeDoanhThuInfo, Integer> soTheThangColumn;
    @FXML private TableColumn<ThongKeDoanhThuInfo, Double> tongDoanhThuColumn;

    // Kết nối CSDL
    private Connection connection;

    // Định dạng số tiền
    private final DecimalFormat currencyFormat = new DecimalFormat("#,###");

    public class ThongKeDoanhThuInfo {
        private final StringProperty thoiGian;
        private final IntegerProperty soTheNgay;
        private final DoubleProperty doanhThuTheNgay;
        private final IntegerProperty soTheThang;
        private final DoubleProperty tongDoanhThu;

        public ThongKeDoanhThuInfo(String thoiGian, int soTheNgay, double doanhThuTheNgay,
                                   int soTheThang, double tongDoanhThu) {
            this.thoiGian = new SimpleStringProperty(thoiGian);
            this.soTheNgay = new SimpleIntegerProperty(soTheNgay);
            this.doanhThuTheNgay = new SimpleDoubleProperty(doanhThuTheNgay);
            this.soTheThang = new SimpleIntegerProperty(soTheThang);
            this.tongDoanhThu = new SimpleDoubleProperty(tongDoanhThu);
        }

        // Getters cho Property
        public StringProperty thoiGianProperty() {
            return thoiGian;
        }

        public IntegerProperty soTheNgayProperty() {
            return soTheNgay;
        }

        public DoubleProperty doanhThuTheNgayProperty() {
            return doanhThuTheNgay;
        }

        public IntegerProperty soTheThangProperty() {
            return soTheThang;
        }

        public DoubleProperty tongDoanhThuProperty() {
            return tongDoanhThu;
        }

        // Getters và setters thông thường
        public String getThoiGian() {
            return thoiGian.get();
        }

        public void setThoiGian(String thoiGian) {
            this.thoiGian.set(thoiGian);
        }

        public int getSoTheNgay() {
            return soTheNgay.get();
        }

        public void setSoTheNgay(int soTheNgay) {
            this.soTheNgay.set(soTheNgay);
        }

        public double getDoanhThuTheNgay() {
            return doanhThuTheNgay.get();
        }

        public void setDoanhThuTheNgay(double doanhThuTheNgay) {
            this.doanhThuTheNgay.set(doanhThuTheNgay);
        }

        public int getSoTheThang() {
            return soTheThang.get();
        }

        public void setSoTheThang(int soTheThang) {
            this.soTheThang.set(soTheThang);
        }

        public double getTongDoanhThu() {
            return tongDoanhThu.get();
        }

        public void setTongDoanhThu(double tongDoanhThu) {
            this.tongDoanhThu.set(tongDoanhThu);
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

        // Khởi tạo ComboBox loại thời gian
        ObservableList<String> loaiThoiGianOptions = FXCollections.observableArrayList(
                "Ngày", "Tháng", "Năm", "Tùy chỉnh"
        );
        loaiThoiGianComboBox.setItems(loaiThoiGianOptions);
        loaiThoiGianComboBox.setValue("Tháng");

        // Thiết lập DatePicker mặc định
        tuNgayDatePicker.setValue(LocalDate.now().withDayOfMonth(1)); // Ngày đầu tháng
        denNgayDatePicker.setValue(LocalDate.now()); // Ngày hiện tại

        // Xử lý sự kiện khi chọn loại thời gian
        loaiThoiGianComboBox.setOnAction(event -> handleLoaiThoiGianChange());

        // Khởi tạo TableView
        initializeTableView();

        // Thống kê mặc định
        handleThongKe(null);
    }

    private void initializeDatabase() {
        connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            System.out.println("✅ Sử dụng kết nối SQL Server từ DatabaseConnection!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể lấy kết nối đến cơ sở dữ liệu!");
        }
    }

    private void initializeTableView() {
        thoiGianColumn.setCellValueFactory(cellData -> cellData.getValue().thoiGianProperty());
        soTheNgayColumn.setCellValueFactory(cellData -> cellData.getValue().soTheNgayProperty().asObject());

        // Định dạng cột tiền tệ
        doanhThuTheNgayColumn.setCellValueFactory(cellData -> cellData.getValue().doanhThuTheNgayProperty().asObject());
        doanhThuTheNgayColumn.setCellFactory(column -> {
            return new TableCell<ThongKeDoanhThuInfo, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(currencyFormat.format(item) + " VNĐ");
                    }
                }
            };
        });

        soTheThangColumn.setCellValueFactory(cellData -> cellData.getValue().soTheThangProperty().asObject());

        // Định dạng cột tiền tệ
        tongDoanhThuColumn.setCellValueFactory(cellData -> cellData.getValue().tongDoanhThuProperty().asObject());
        tongDoanhThuColumn.setCellFactory(column -> {
            return new TableCell<ThongKeDoanhThuInfo, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(currencyFormat.format(item) + " VNĐ");
                    }
                }
            };
        });
    }

    private void handleLoaiThoiGianChange() {
        String loaiThoiGian = loaiThoiGianComboBox.getValue();
        LocalDate today = LocalDate.now();

        switch (loaiThoiGian) {
            case "Ngày":
                tuNgayDatePicker.setValue(today);
                denNgayDatePicker.setValue(today);
                tuNgayDatePicker.setDisable(true);
                denNgayDatePicker.setDisable(true);
                break;
            case "Tháng":
                tuNgayDatePicker.setValue(today.withDayOfMonth(1));
                denNgayDatePicker.setValue(today);
                tuNgayDatePicker.setDisable(true);
                denNgayDatePicker.setDisable(true);
                break;
            case "Năm":
                tuNgayDatePicker.setValue(today.withDayOfYear(1));
                denNgayDatePicker.setValue(today);
                tuNgayDatePicker.setDisable(true);
                denNgayDatePicker.setDisable(true);
                break;
            case "Tùy chỉnh":
                tuNgayDatePicker.setDisable(false);
                denNgayDatePicker.setDisable(false);
                break;
        }
    }

    @FXML
    private void handleThongKe(ActionEvent event) {
        String loaiThoiGian = loaiThoiGianComboBox.getValue();
        LocalDate tuNgay = tuNgayDatePicker.getValue();
        LocalDate denNgay = denNgayDatePicker.getValue();

        if (tuNgay == null || denNgay == null) {
            thongBaoLabel.setText("Vui lòng chọn khoảng thời gian!");
            return;
        }

        if (tuNgay.isAfter(denNgay)) {
            thongBaoLabel.setText("Ngày bắt đầu không thể sau ngày kết thúc!");
            return;
        }

        try {
            thongKeDoanhThu(loaiThoiGian, tuNgay, denNgay);
        } catch (SQLException e) {
            thongBaoLabel.setText("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void thongKeDoanhThu(String loaiThoiGian, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        ObservableList<ThongKeDoanhThuInfo> thongKeList = FXCollections.observableArrayList();

        // Tạo series cho biểu đồ
        XYChart.Series<String, Number> doanhThuTheNgaySeries = new XYChart.Series<>();
        doanhThuTheNgaySeries.setName("Doanh thu thẻ ngày");

        XYChart.Series<String, Number> soTheThangSeries = new XYChart.Series<>();
        soTheThangSeries.setName("Số thẻ tháng mới");

        // Tổng cộng
        int tongSoTheNgay = 0;
        double tongDTTheNgay = 0;
        int tongSoTheThang = 0;
        double tongDT = 0;

        switch (loaiThoiGian) {
            case "Ngày":
                // Thống kê theo ngày
                String queryNgay = "SELECT COUNT(*) AS so_the_ngay, SUM(gia) AS tong_gia " +
                        "FROM thong_ke WHERE loai_the = 'ngay' AND CAST(ngay_giao_dich AS DATE) = ?";

                PreparedStatement stmtTheNgay = connection.prepareStatement(queryNgay);
                stmtTheNgay.setDate(1, java.sql.Date.valueOf(tuNgay));
                ResultSet rsTheNgay = stmtTheNgay.executeQuery();

                int soTheNgay = 0;
                int doanhThuTheNgay = 0;

                // Tính toán doanh thu dựa trên giá
                if (rsTheNgay.next()) {
                    soTheNgay = rsTheNgay.getInt("so_the_ngay");
                    doanhThuTheNgay = rsTheNgay.getInt("tong_gia");  // lấy tổng giá
                }

                // Đếm số thẻ tháng mới đăng ký trong ngày
                String queryTheThang = "SELECT COUNT(*) AS so_the_thang, SUM(gia) AS tong_gia " +
                        "FROM thong_ke WHERE loai_the = 'thang' AND CAST(ngay_giao_dich AS DATE) = ?";

                PreparedStatement stmtTheThang = connection.prepareStatement(queryTheThang);
                stmtTheThang.setDate(1, java.sql.Date.valueOf(tuNgay));
                ResultSet rsTheThang = stmtTheThang.executeQuery();

                int soTheThang = 0;
                int doanhThuTheThang = 0;

                // Tính toán doanh thu dựa trên giá
                if (rsTheThang.next()) {
                    soTheThang = rsTheThang.getInt("so_the_thang");
                    doanhThuTheThang = rsTheThang.getInt("tong_gia");  // lấy tổng giá
                }

                double tongDoanhThu = doanhThuTheNgay + doanhThuTheThang;

                String formattedDate = tuNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                ThongKeDoanhThuInfo infoNgay = new ThongKeDoanhThuInfo(
                        formattedDate, soTheNgay, doanhThuTheNgay, soTheThang, tongDoanhThu
                );
                thongKeList.add(infoNgay);

                // Cập nhật tổng
                tongSoTheNgay = soTheNgay;
                tongDTTheNgay = doanhThuTheNgay;
                tongSoTheThang = soTheThang;
                tongDT = tongDoanhThu;

                // Thêm vào biểu đồ
                doanhThuTheNgaySeries.getData().add(new XYChart.Data<>(formattedDate, doanhThuTheNgay));
                soTheThangSeries.getData().add(new XYChart.Data<>(formattedDate, soTheThang));
                break;

            case "Tháng":
                // Thống kê theo tháng hiện tại, chia thành các tuần
                LocalDate startDate = tuNgay;
                LocalDate endDate = startDate.plusDays(6);
                int weekCounter = 1;

                while (!startDate.isAfter(denNgay)) {
                    if (endDate.isAfter(denNgay)) {
                        endDate = denNgay;
                    }

                    // Thống kê thẻ ngày theo tuần
                    String queryTuan = "SELECT COUNT(*) AS so_the_ngay, SUM(gia) AS tong_gia " +
                            "FROM thong_ke WHERE loai_the = 'ngay' AND CAST(ngay_giao_dich AS DATE) BETWEEN ? AND ?";

                    PreparedStatement stmtTuan = connection.prepareStatement(queryTuan);
                    stmtTuan.setDate(1, java.sql.Date.valueOf(startDate));
                    stmtTuan.setDate(2, java.sql.Date.valueOf(endDate));
                    ResultSet rsTuan = stmtTuan.executeQuery();

                    int soTheNgayTuan = 0;
                    double doanhThuTheNgayTuan = 0;

                    if (rsTuan.next()) {
                        soTheNgayTuan = rsTuan.getInt("so_the_ngay");
                        doanhThuTheNgayTuan = rsTuan.getDouble("tong_gia");
                    }

                    // Đếm số thẻ tháng mới đăng ký trong tuần
                    String queryTheThangTuan = "SELECT COUNT(*) AS so_the_thang, SUM(gia) AS tong_gia " +
                            "FROM thong_ke WHERE loai_the = 'thang' AND CAST(ngay_giao_dich AS DATE) BETWEEN ? AND ?";

                    PreparedStatement stmtTheThangTuan = connection.prepareStatement(queryTheThangTuan);
                    stmtTheThangTuan.setDate(1, java.sql.Date.valueOf(startDate));
                    stmtTheThangTuan.setDate(2, java.sql.Date.valueOf(endDate));
                    ResultSet rsTheThangTuan = stmtTheThangTuan.executeQuery();

                    int soTheThangTuan = 0;
                    int doanhThuTheThangTuan = 0;

                    // Tính toán doanh thu dựa trên giá
                    if (rsTheThangTuan.next()) {
                        soTheThangTuan = rsTheThangTuan.getInt("so_the_thang");
                        doanhThuTheThangTuan = rsTheThangTuan.getInt("tong_gia");
                    }

                    double tongDoanhThuTuan = doanhThuTheNgayTuan + doanhThuTheThangTuan;

                    String tuanLabel = "Tuần " + weekCounter + " (" +
                            startDate.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " +
                            endDate.format(DateTimeFormatter.ofPattern("dd/MM")) + ")";

                    ThongKeDoanhThuInfo infoTuan = new ThongKeDoanhThuInfo(
                            tuanLabel, soTheNgayTuan, doanhThuTheNgayTuan, soTheThangTuan, tongDoanhThuTuan
                    );
                    thongKeList.add(infoTuan);

                    // Cập nhật tổng
                    tongSoTheNgay += soTheNgayTuan;
                    tongDTTheNgay += doanhThuTheNgayTuan;
                    tongSoTheThang += soTheThangTuan;
                    tongDT += tongDoanhThuTuan;

                    // Thêm vào biểu đồ
                    doanhThuTheNgaySeries.getData().add(new XYChart.Data<>(tuanLabel, doanhThuTheNgayTuan));
                    soTheThangSeries.getData().add(new XYChart.Data<>(tuanLabel, soTheThangTuan));

                    // Cập nhật khoảng thời gian cho tuần tiếp theo
                    startDate = endDate.plusDays(1);
                    endDate = startDate.plusDays(6);
                    weekCounter++;
                }
                break;

            case "Năm":
                // Tương tự, sửa lại phần thống kê theo năm
                YearMonth startMonth = YearMonth.from(tuNgay);
                YearMonth endMonth = YearMonth.from(denNgay);

                while (!startMonth.isAfter(endMonth)) {
                    LocalDate firstDayOfMonth = startMonth.atDay(1);
                    LocalDate lastDayOfMonth = startMonth.atEndOfMonth();

                    // Thống kê thẻ ngày theo tháng với giá từ loại xe
                    String queryThang = "SELECT COUNT(*) AS so_the_ngay, SUM(gia) AS tong_gia " +
                            "FROM thong_ke WHERE loai_the = 'ngay' AND CAST(ngay_giao_dich AS DATE) BETWEEN ? AND ?";

                    PreparedStatement stmtThang = connection.prepareStatement(queryThang);
                    stmtThang.setDate(1, java.sql.Date.valueOf(firstDayOfMonth));
                    stmtThang.setDate(2, java.sql.Date.valueOf(lastDayOfMonth));
                    ResultSet rsThang = stmtThang.executeQuery();

                    int soTheNgayThang = 0;
                    double doanhThuTheNgayThang = 0;

                    if (rsThang.next()) {
                        soTheNgayThang = rsThang.getInt("so_the_ngay");
                        doanhThuTheNgayThang= rsThang.getDouble("tong_gia");
                    }

                    // Đếm số thẻ tháng mới đăng ký trong tháng
                    String queryTheThangThang = "SELECT COUNT(*) AS so_the_thang, SUM(gia) AS tong_gia " +
                    "FROM thong_ke WHERE loai_the = 'thang' AND CAST(ngay_giao_dich AS DATE) BETWEEN ? AND ?";

                    PreparedStatement stmtTheThangThang = connection.prepareStatement(queryTheThangThang);
                    stmtTheThangThang.setDate(1, java.sql.Date.valueOf(firstDayOfMonth));
                    stmtTheThangThang.setDate(2, java.sql.Date.valueOf(lastDayOfMonth));
                    ResultSet rsTheThangThang = stmtTheThangThang.executeQuery();

                    int soTheThangThang = 0;
                    int doanhThuTheThangThang = 0;

                    // Tính toán doanh thu dựa trên giá
                    if (rsTheThangThang.next()) {
                        soTheThangThang = rsTheThangThang.getInt("so_the_thang");
                        doanhThuTheThangThang = rsTheThangThang.getInt("tong_gia");
                    }

                    double tongDoanhThuThang = doanhThuTheNgayThang + doanhThuTheThangThang;

                    String thangLabel = "Tháng " + startMonth.getMonthValue() + "/" + startMonth.getYear();

                    ThongKeDoanhThuInfo infoThang = new ThongKeDoanhThuInfo(
                            thangLabel, soTheNgayThang, doanhThuTheNgayThang, soTheThangThang, tongDoanhThuThang
                    );
                    thongKeList.add(infoThang);

                    // Cập nhật tổng
                    tongSoTheNgay += soTheNgayThang;
                    tongDTTheNgay += doanhThuTheNgayThang;
                    tongSoTheThang += soTheThangThang;
                    tongDT += tongDoanhThuThang;

                    // Thêm vào biểu đồ
                    doanhThuTheNgaySeries.getData().add(new XYChart.Data<>(thangLabel, doanhThuTheNgayThang));
                    soTheThangSeries.getData().add(new XYChart.Data<>(thangLabel, soTheThangThang));

                    // Chuyển đến tháng tiếp theo
                    startMonth = startMonth.plusMonths(1);
                }
                break;

            case "Tùy chỉnh":
                // Tương tự, sửa lại phần thống kê tùy chỉnh
                String queryTuyChon = "SELECT COUNT(*) AS so_the_ngay, SUM(gia) AS tong_gia " +
                        "FROM thong_ke WHERE loai_the = 'ngay' AND CAST(ngay_giao_dich AS DATE) BETWEEN ? AND ?";

                PreparedStatement stmtTuyChon = connection.prepareStatement(queryTuyChon);
                stmtTuyChon.setDate(1, java.sql.Date.valueOf(tuNgay));
                stmtTuyChon.setDate(2, java.sql.Date.valueOf(denNgay));
                ResultSet rsTuyChon = stmtTuyChon.executeQuery();

                int soTheNgayTuyChon = 0;
                double doanhThuTheNgayTuyChon = 0;

                if (rsTuyChon.next()) {
                    soTheNgayTuyChon = rsTuyChon.getInt("so_the_ngay");
                    doanhThuTheNgayTuyChon= rsTuyChon.getDouble("tong_gia");
                }

                // Đếm số thẻ tháng mới đăng ký trong khoảng thời gian
                String queryTheThangTuyChon = "SELECT COUNT(*) AS so_the_thang, SUM(gia) AS tong_gia " +
                        "FROM thong_ke WHERE loai_the = 'thang' AND CAST(ngay_giao_dich AS DATE) BETWEEN ? AND ?";

                PreparedStatement stmtTheThangTuyChon = connection.prepareStatement(queryTheThangTuyChon);
                stmtTheThangTuyChon.setDate(1, java.sql.Date.valueOf(tuNgay));
                stmtTheThangTuyChon.setDate(2, java.sql.Date.valueOf(denNgay));
                ResultSet rsTheThangTuyChon = stmtTheThangTuyChon.executeQuery();

                int soTheThangTuyChon = 0;
                int doanhThuTheThangTuyChon = 0;

                // Tính toán doanh thu dựa trên giá
                if (rsTheThangTuyChon.next()) {
                    soTheThangTuyChon = rsTheThangTuyChon.getInt("so_the_thang");
                    doanhThuTheThangTuyChon = rsTheThangTuyChon.getInt("tong_gia");
                }

                double tongDoanhThuTuyChon = doanhThuTheNgayTuyChon + doanhThuTheThangTuyChon;

                String tuChonLabel = tuNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " - " +
                        denNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                ThongKeDoanhThuInfo infoTuyChon = new ThongKeDoanhThuInfo(
                        tuChonLabel, soTheNgayTuyChon, doanhThuTheNgayTuyChon, soTheThangTuyChon, tongDoanhThuTuyChon
                );
                thongKeList.add(infoTuyChon);

                // Cập nhật tổng
                tongSoTheNgay = soTheNgayTuyChon;
                tongDTTheNgay = doanhThuTheNgayTuyChon;
                tongSoTheThang = soTheThangTuyChon;
                tongDT = tongDoanhThuTuyChon;

                // Thêm vào biểu đồ
                doanhThuTheNgaySeries.getData().add(new XYChart.Data<>(tuChonLabel, doanhThuTheNgayTuyChon));
                soTheThangSeries.getData().add(new XYChart.Data<>(tuChonLabel, soTheThangTuyChon));
                break;
        }

        // Cập nhật TableView
        doanhThuTableView.setItems(thongKeList);


        // Cập nhật tổng
        tongTheNgayLabel.setText(String.valueOf(tongSoTheNgay));
        doanhThuTheNgayLabel.setText(currencyFormat.format(tongDTTheNgay) + " VNĐ");
        theThangMoiLabel.setText(String.valueOf(tongSoTheThang));
        tongDoanhThuLabel.setText(currencyFormat.format(tongDT) + " VNĐ");

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("AdminUI.fxml");
    }
}
