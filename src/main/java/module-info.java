module org.example.parityfolder {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.parityfolder to javafx.fxml;
    exports org.example.parityfolder;
}