package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import database.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistroClienteController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtRFC;

    @FXML
    private Label lblMensaje;

    @FXML
    private void guardarCliente(ActionEvent event) throws SQLException{
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String correo = txtCorreo.getText();
        String telefono = txtTelefono.getText();
        String rfc = txtRFC.getText().toUpperCase();

        if (nombre.isEmpty() || apellidos.isEmpty() || telefono.isEmpty() || rfc.isEmpty()) {
            lblMensaje.setText("Nombre, apellidos, teléfono y RFC son obligatorios.");
            return;
        }

        if (!validarRFC(rfc)) {
            lblMensaje.setText("El RFC no tiene un formato válido.");
            return;
        }

        Connection conn = Conexion.conectar();
        if (conn != null) {
            String sql = "INSERT INTO clientes (nombre, apellidos, correo, telefono, rfc) VALUES (?, ?, ?, ?, ?)";
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nombre);
                stmt.setString(2, apellidos);

                if (correo.isEmpty()) {
                    stmt.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(3, correo);
                }

                stmt.setString(4, telefono);
                stmt.setString(5, rfc);

                stmt.executeUpdate();

                mostrarAlerta(AlertType.INFORMATION, "Registro exitoso", "El cliente ha sido registrado correctamente.");
                limpiarCampos();
                lblMensaje.setText("");

            } catch (SQLException e) {
                mostrarAlerta(AlertType.ERROR, "Error", "No se pudo registrar el cliente.");
            }
        } else {
            mostrarAlerta(AlertType.ERROR, "Error de conexión", "No se pudo conectar a la base de datos.");
        }
    }

    private boolean validarRFC(String rfc) {
        return rfc.matches("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtRFC.clear();
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}