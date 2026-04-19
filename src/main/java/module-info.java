module client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens client to javafx.fxml;
    exports client;
}