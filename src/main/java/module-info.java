module com.example.eventya {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires org.apache.pdfbox;

    opens controladores to javafx.fxml;
    exports controladores;
    exports modelos;
}
