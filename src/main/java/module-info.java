module client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires client;

    opens client to javafx.graphics;
    // Phải mở package chứa Controller thì JavaFX mới không báo lỗi IllegalAccessException
    opens client.controller to javafx.fxml;

    // Nếu bạn để file FXML trong package client.view, hãy mở thêm dòng này (cho chắc chắn)
    opens client to javafx.fxml;

    exports client;
}