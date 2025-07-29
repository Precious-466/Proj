module apassignment1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;  // add this if missing

    opens com.example.apassignment1 to javafx.fxml;
    exports com.example.apassignment1;
}
