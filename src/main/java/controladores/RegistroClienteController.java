package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import database.Conexion;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistroClienteController 
    {

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
private void guardarCliente(ActionEvent event) throws SQLException {
    String nombre = txtNombre.getText();
    String apellidos = txtApellidos.getText();
    String correo = txtCorreo.getText();
    String telefono = txtTelefono.getText();
    String rfc = txtRFC.getText().toUpperCase();

    if (nombre.isEmpty() || apellidos.isEmpty() || telefono.isEmpty() || rfc.isEmpty()) {
        mostrarAlertaPersonalizada(AlertType.ERROR, "Campos obligatorios", "Nombre, apellidos, teléfono y RFC son obligatorios.");
        return;
    }

    if (!correo.isEmpty() && !correo.contains("@")) {
        mostrarAlertaPersonalizada(AlertType.ERROR, "Correo inválido", "El correo debe contener '@' y ser válido.");
        return;
    }

    if (!validarRFC(rfc)) {
        mostrarAlertaPersonalizada(AlertType.ERROR, "RFC inválido", "El RFC o CURP no tiene un formato válido.");
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
            mostrarAlertaPersonalizada(AlertType.INFORMATION, "Registro exitoso", "El cliente ha sido registrado correctamente.");
            limpiarCampos();

        } catch (SQLException e) {
            mostrarAlertaPersonalizada(AlertType.ERROR, "Error", "No se pudo registrar el cliente.");
        }
    } else {
            mostrarAlertaPersonalizada(AlertType.ERROR, "Error de conexión", "No se pudo conectar a la base de datos.");
    }
}

    private boolean validarRFC(String rfc) 
    {
    String regexRFC = "^([A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{0,3})$";
    String regexCURP = "^[A-Z][AEIOU][A-Z]{2}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$";
    
    return rfc.matches(regexRFC) || rfc.matches(regexCURP);
}

    
     @FXML
    private void volverAlPanelPrincipal() throws IOException 
    {
    App.setRoot("PanelPrincipal");
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtRFC.clear();
    }

    private void mostrarAlertaPersonalizada(Alert.AlertType tipo, String titulo, String mensaje) {
    Alert alerta = new Alert(tipo);
    alerta.setTitle("Aviso del sistema");
    alerta.setHeaderText(null);

    // Crear contenido enriquecido (HTML básico con estilo CSS inline)
    Label label = new Label();
    label.setStyle("-fx-font-size: 14px; -fx-font-weight: normal; -fx-text-fill: #333;");
    label.setText(mensaje);

    // Ícono según tipo
    switch (tipo) {
        case INFORMATION:
            alerta.setGraphic(new ImageView(getClass().getResource("/img/info.png").toString()));
            break;
        case ERROR:
            alerta.setGraphic(new ImageView(getClass().getResource("/img/error.png").toString()));
            break;
        case WARNING:
            alerta.setGraphic(new ImageView(getClass().getResource("/img/warning.png").toString()));
            break;
        default:
            alerta.setGraphic(null);
    }

    alerta.getDialogPane().setContent(label);
    alerta.showAndWait();
}

}