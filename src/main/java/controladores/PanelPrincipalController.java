package controladores;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PanelPrincipalController {

    @FXML
    private Label lblNombreUsuario;
    public void inicializarUsuario(String nombre, String apellidos, String rol) {
    lblNombreUsuario.setText("Bienvenido, " + nombre + " " + apellidos + " (" + rol + ")");
}
    @FXML
    private void abrirClientes() throws IOException{
    App.setRoot("Clientes");
}
    @FXML
    private void abrirReservas() throws IOException {
    App.setRoot("Reservas");
    }
    
    @FXML
    private void abrirEventos() throws IOException {
    App.setRoot("Eventos");
    }
   

    @FXML
    private void cerrarSesion() {
    Stage stage = (Stage) lblNombreUsuario.getScene().getWindow();
    stage.close();
    }

   private void abrirVentana(String rutaFXML, String titulo) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(titulo);
        stage.show();
    } catch (IOException e) {
    }
}


    @FXML
    public void salirAplicacion(ActionEvent event) {
        System.exit(0);
    }
}