module G3_BiddingOnline {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.zaxxer.hikari;

    opens client to javafx.graphics, javafx.fxml;
    opens client.controller to javafx.fxml;
    exports client;
}