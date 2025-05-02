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
    App.setRoot("RegistroCliente");
}

    @FXML
    public void abrirReservas(ActionEvent event) throws Exception {
        abrirVentana("/vista/calendario.fxml", "Reservas");
    }
    
    @FXML
    public void abrirEventos(ActionEvent event) throws Exception {
        abrirVentana("/vista/VistaEventos.fxml", "Reservas");
    }

    public void abrirPaquetes(ActionEvent event) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/VistaPaquetes.fxml"));
    Stage stage = new Stage();
    stage.setScene(new Scene(root));
    stage.setTitle("Gestión de Paquetes");
    stage.show();
    }
    
    @FXML
    private void cerrarSesion() {
    // Cierra la ventana actual
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
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @FXML
    public void salirAplicacion(ActionEvent event) {
        System.exit(0);
    }
}