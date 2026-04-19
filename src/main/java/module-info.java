module client {
    requires javafx.controls;
    requires javafx.fxml;
<<<<<<< HEAD
=======
    requires java.sql;
>>>>>>> cc24ab1a490f8327e96db13cc7dbc03ad46ad134
    requires java.desktop;


    opens client to javafx.fxml;
    exports client;
}