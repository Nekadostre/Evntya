package controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar la vista del login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/LoginView.fxml"));
        Parent root = loader.load();

        // Crear escena y aplicar CSS
        Scene scene = new Scene(root);
           scene.getStylesheets().add(getClass().getResource("/vista/login.css").toExternalForm());


        // Configurar y mostrar la ventana
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
