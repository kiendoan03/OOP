package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage stg;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stg = primaryStage;

        // Khởi tạo kết nối database một lần duy nhất khi ứng dụng start
        DatabaseConnection.getInstance();

        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));

        primaryStage.setTitle("Quản lý bãi gửi xe");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 1130, 800));
        primaryStage.getStyle();
        primaryStage.show();
    }
    public void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        stg.getScene().setRoot(pane);
    }

    public static void main(String[] args) {
        launch();
    }
}