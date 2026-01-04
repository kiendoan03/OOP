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

        DatabaseConnection.getInstance();

        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));

        primaryStage.setTitle("Quản lý bãi gửi xe");
        primaryStage.setResizable(false);
        
        Scene scene = new Scene(root, 1130, 800);
        scene.getStylesheets().add(getClass().getResource("/Css/global.css").toExternalForm());
        
        primaryStage.setScene(scene);
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