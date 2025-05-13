module com.example.eventya {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens controladores to javafx.fxml;
    opens modelos to javafx.base;
    opens vistas to javafx.fxml;

    exports controladores;
    exports modelos;
}
