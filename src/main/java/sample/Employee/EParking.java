package sample.Employee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import sample.Main;

import java.io.IOException;

public class EParking extends EmployeeUI{
    @FXML
    private Button buttonParkingIn;
    public void ParkingIn(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("Parkingin.fxml");
    }

    @FXML
    private Button buttonParkingOut;
    public void ParkingOut(ActionEvent event) throws IOException {
        Main m = new Main();
        m.changeScene("ParkingOut.fxml");
    }
}
