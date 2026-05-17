package client.util;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class StageUtils {
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 700;

    private StageUtils() {
    }

    public static void setMaximizedScene(Stage stage, Parent root) {
        Scene currentScene = stage.getScene();

        if (currentScene == null) {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()));
        } else {
            currentScene.setRoot(root);
        }

        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMaximized(true);
        Platform.runLater(() -> stage.setMaximized(true));
    }
}
