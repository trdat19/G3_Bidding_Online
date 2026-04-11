module com.example.g3_biddingonline {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.g3_biddingonline to javafx.fxml;
    exports com.example.g3_biddingonline;
}