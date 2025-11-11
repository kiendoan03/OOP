package sample.Admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import sample.Main;

import java.io.IOException;
import java.util.Optional;

public class AdminUI {

    @FXML
    private Button buttonAProblem;
    public void AProblem(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("AProblem.fxml");
    }

    @FXML
    private Button buttonAccount;
    public void Account(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("Account.fxml");
    }


    @FXML
    private Button buttonStatistic;
    public void Statistic(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("Statistic.fxml");
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
}