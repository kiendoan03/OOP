package sample.Employee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import sample.Main;
import sample.Login;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class EmployeeUI {

    @FXML
    private Button buttonParking;
    public void Parking(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("ParkingIn.fxml");
    }


    @FXML
    private Button buttonCard;
    public void Card(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("Card.fxml");
    }

    @FXML
    private Button buttonEProblem;
    public void EProblem(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("EProblem.fxml");
    }
    @FXML
    private Button Logout;
    public void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất không?");

        ButtonType buttonTypeYes = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get()==buttonTypeYes){
            Main m = new Main();
            m.changeScene("login.fxml");
        }else{
            return;
        }
    }
    public void Information() throws SQLException, IOException {
        Login check = new Login();
        String tK = check.getUsernameText();

    }
}