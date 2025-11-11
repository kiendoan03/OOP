module sample.test {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens sample to javafx.fxml;
    exports sample;
    exports sample.Admin;
    opens sample.Admin to javafx.fxml;
    exports sample.Employee;
    opens sample.Employee to javafx.fxml;
}