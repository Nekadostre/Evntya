module App.java  
{
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.base;

    opens controladores to javafx.fxml;
    exports controladores;
}
