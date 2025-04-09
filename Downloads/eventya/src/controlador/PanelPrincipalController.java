package controlador;

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
    public void abrirClientes(ActionEvent event) throws Exception {
        abrirVentana("/vista/RegistroCliente.fxml", "Clientes");
    }

    @FXML
    public void abrirReservas(ActionEvent event) throws Exception {
        abrirVentana("/vista/VistaCalendario.fxml", "Reservas");
    }

    @FXML
    public void abrirFacturacion(ActionEvent event) throws Exception {
        abrirVentana("/vista/VistaFactura.fxml", "Facturación");
    }

    public void abrirPaquetes(ActionEvent event) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/vista/VistaPaquetes.fxml"));
    Stage stage = new Stage();
    stage.setScene(new Scene(root));
    stage.setTitle("Gestión de Paquetes");
    stage.show();
}

    
    @FXML
    private void abrirUsuarios(ActionEvent event) {
    System.out.println("Abrir Usuarios aún no implementado.");
}

    @FXML
    private void abrirAuditoria(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/VistaAuditoria.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Auditoría de Inicio de Sesión");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error al abrir la auditoría.");
    }
}

    private void abrirVentana(String rutaFXML, String titulo) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(rutaFXML));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(titulo);
        stage.show();
    }

    @FXML
    public void salirAplicacion(ActionEvent event) {
        System.exit(0);
    }
}
