package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modelo.LoginDAO;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnIngresar;

    @FXML
    private void ingresar(ActionEvent event) {
        String usuarioId = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        if (usuarioId.isEmpty() || contrasena.isEmpty()) {
            lblMensaje.setText("Todos los campos son obligatorios.");
            return;
        }

        // Obtener nombre, apellidos y rol del usuario
        String[] datosUsuario = LoginDAO.validarLoginConDatos(usuarioId, contrasena);

        if (datosUsuario != null) {
            String nombre = datosUsuario[0];
            String apellidos = datosUsuario[1];
            String rol = datosUsuario[2];


            // Registrar en historial
            LoginDAO.registrarHistorialLogin(usuarioId);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/PanelPrincipal.fxml"));
                Parent root = loader.load();

                // Pasar datos al panel principal
                PanelPrincipalController controller = loader.getController();
                controller.inicializarUsuario(nombre, apellidos, rol);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Panel Principal");
                stage.show();

                // Cerrar login
                Stage loginStage = (Stage) btnIngresar.getScene().getWindow();
                loginStage.close();

            } catch (Exception e) {
                e.printStackTrace();
                lblMensaje.setText("Error al cargar el panel.");
            }

        } else {
            lblMensaje.setText("Credenciales incorrectas.");
        }
    }
}
