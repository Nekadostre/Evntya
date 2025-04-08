package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class PanelPrincipalController {

    public void abrirClientes(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/RegistroCliente.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Clientes");
        stage.show();
    }

    public void abrirReservas(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/VistaCalendario.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Reservas");
        stage.show();
    }

    public void abrirFacturacion(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/VistaFactura.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Facturación");
        stage.show();
    }
    
    @FXML
    private Label lblNombreUsuario;

    public void inicializarUsuario(String nombre, String apellidos) {
    lblNombreUsuario.setText("Bienvenido, " + nombre + " " + apellidos);
    }

    public void salirAplicacion(ActionEvent event) {
        System.exit(0);
    }
}
