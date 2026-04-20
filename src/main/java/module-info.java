module G3_BiddingOnline {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens client to javafx.graphics, javafx.fxml;
    opens client.controller to javafx.fxml;
    exports client;
}