package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import dao.LoginDAO;
import java.io.IOException;
import javafx.scene.layout.AnchorPane;

public class LoginController
{

    @FXML private AnchorPane anchorpane;
    
    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnIngresar;

    @FXML
    private void ingresar() throws IOException{
        String usuarioId = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        if (usuarioId.isEmpty() || contrasena.isEmpty()) 
        {
            lblMensaje.setText("Todos los campos son obligatorios.");
            return;
        }

        // Obtener nombre, apellidos y rol del usuario
        String[] datosUsuario = LoginDAO.validarLoginConDatos(usuarioId, contrasena);

        if (datosUsuario != null) 
        {
            String nombre = datosUsuario[0];
            String apellidos = datosUsuario[1];
            String rol = datosUsuario[2];
            
            App.setRoot("PanelPrincipal");
        }
    }    
}