package client;

import client.service.ClientNetworkService;
import client.util.StageUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        System.out.println(getClass().getResource("/view/login.fxml"));

        Image icon = new Image(getClass().getResourceAsStream("/image/LOGO.png"));
        Parent root = loader.load();

        primaryStage.getIcons().add(icon);
        StageUtils.setMaximizedScene(primaryStage, root);
        primaryStage.show();
    }
    @Override
    public void stop() {
        ClientNetworkService.getInstance().closeConnection();
        System.exit(0);
    }
}