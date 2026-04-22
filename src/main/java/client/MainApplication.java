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
        // Đảm bảo có dấu "/" ở đầu đại diện cho thư mục resources
        java.net.URL resource = getClass().getResource("/view/login.fxml");
        if (resource == null) {
            System.err.println("Lỗi: Không tìm thấy file login.fxml tại /view/");
            return;
        }
        Parent root = FXMLLoader.load(resource);
        primaryStage.setTitle("Hệ thống Đấu giá");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}