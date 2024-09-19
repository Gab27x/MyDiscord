module org.example.mydiscord {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.mydiscord to javafx.fxml;
    exports org.example.mydiscord;
}