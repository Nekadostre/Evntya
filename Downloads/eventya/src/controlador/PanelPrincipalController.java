package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PanelPrincipalController {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Button btnFacturacion;

    @FXML
    private Button btnUsuarios;

    @FXML
    private Button btnAuditoria;

    private String rolUsuario;

    /**
     * Inicializa el panel principal con los datos del usuario.
     */
    public void inicializarUsuario(String nombre, String apellidos, String rol) {
        lblNombreUsuario.setText("Bienvenido, " + nombre + " " + apellidos);
        this.rolUsuario = rol;
        configurarAccesoPorRol();
    }

    /**
     * Desactiva funciones que no le corresponden al rol del usuario.
     */
    private void configurarAccesoPorRol() {
        if (rolUsuario.equalsIgnoreCase("vendedor")) {
            btnFacturacion.setDisable(true);
            btnUsuarios.setDisable(true);
            btnAuditoria.setDisable(true);
        }
    }

    @FXML
    private void abrirClientes(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/vistas_cliente/RegistroCliente.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Clientes");
        stage.show();
    }

    @FXML
    private void abrirReservas(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/VistaCalendario/VistaCalendario.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Reservas");
        stage.show();
    }

    @FXML
    private void abrirFacturacion(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/vistas_panel/VistaFactura.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Facturación");
        stage.show();
    }

    @FXML
    private void abrirUsuarios(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/vistas_admin/VistaUsuarios.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestión de Usuarios");
        stage.show();
    }

    @FXML
    private void abrirAuditoria(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/VistaAuditoria.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Historial de Inicios de Sesión");
        stage.show();
    }

    @FXML
    private void salirAplicacion(ActionEvent event) {
        System.exit(0);
    }
}
