package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.*;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Button button = new Button("Hello");

        Scene scene = new Scene(button, 400, 500);

        primaryStage.setTitle("AduApplication");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}