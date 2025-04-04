package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLDocumentController {

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnexit;

    @FXML
    private TextField txtidUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private void validarUsuario(ActionEvent event) throws Exception {
        String usuario = txtidUsuario.getText().toLowerCase();
        String contrasena = txtContrasena.getText();

        if (contrasena.matches("[0-9]{8}")) {
            if ("admin".equals(usuario) && "12345678".equals(contrasena)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/PanelPrincipal.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Panel Principal");
                stage.show();

                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            } else {
                System.out.println("Credenciales incorrectas");
            }
        } else {
            System.out.println("La contraseña debe tener 8 dígitos numéricos");
        }
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }
}
