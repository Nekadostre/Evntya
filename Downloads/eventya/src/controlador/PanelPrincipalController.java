package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PanelPrincipalController {

    public void abrirClientes(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/vistas_cliente/RegistroCliente.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Clientes");
        stage.show();
    }

    public void abrirReservas(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/VistaCalendario/VistaCalendario.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Reservas");
        stage.show();
    }

    public void abrirFacturacion(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/vistas_panel/VistaFactura.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Facturación");
        stage.show();
    }

    public void salirAplicacion(ActionEvent event) {
        System.exit(0);
    }
}
