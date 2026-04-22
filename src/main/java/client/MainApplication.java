package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.*;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        System.out.println(getClass().getResource("/view/login.fxml"));

        Image icon = new Image(getClass().getResourceAsStream("/image/LOGO.png"));

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
    }
}