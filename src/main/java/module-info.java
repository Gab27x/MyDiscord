module org.example.mydiscord {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.mydiscord to javafx.fxml;
    exports org.example.mydiscord;
}