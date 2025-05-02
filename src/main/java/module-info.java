module com.mycompany.test {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens controladores to javafx.fxml;
    exports controladores;
}
