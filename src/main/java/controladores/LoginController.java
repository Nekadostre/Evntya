package controladores;

import database.Conexion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import modelos.SesionTemporal;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

    public class LoginController {
        private static final boolean DEBUG_MODE = false; // Cambia a true solo cuando necesites debug

        private void debug(String mensaje) {
            if (DEBUG_MODE) {
                System.out.println(mensaje);
            }
        }

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;  // Coincide con el FXML
    @FXML private Label lblMensaje;
    @FXML private Button btnIngresar;

    @FXML
    public void initialize() {
        // Limpiar mensajes al inicializar
        if (lblMensaje != null) {
            lblMensaje.setText("");
        }
        
        // Enfocar el campo de usuario
        if (txtUsuario != null) {
            txtUsuario.requestFocus();
        }
        
        // Permitir login con Enter
        if (txtUsuario != null && txtContrasena != null) {
            txtUsuario.setOnAction(e -> txtContrasena.requestFocus());
            txtContrasena.setOnAction(e -> ingresar());
        }
    }

    // Método que coincide exactamente con el FXML onAction="#ingresar"
    @FXML
    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        // Validar campos vacíos
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, completa todos los campos.");
            return;
        }

        // Deshabilitar botón durante el proceso
        btnIngresar.setDisable(true);
        if (lblMensaje != null) {
            lblMensaje.setText("Verificando credenciales...");
            lblMensaje.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        }

        try {
            if (validarCredenciales(usuario, contrasena)) {
                if (lblMensaje != null) {
                    lblMensaje.setText("¡Acceso concedido! Cargando panel...");
                    lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
                
                // Pequeña pausa para mostrar el mensaje de éxito
                Thread.sleep(800);
                
                // Cambiar a panel principal
                App.changeView("PanelPrincipal");
            } else {
                mostrarError("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            mostrarError("Error de conexión: " + e.getMessage());
            e.printStackTrace();
        } finally {
            btnIngresar.setDisable(false);
        }
    }

    private boolean validarCredenciales(String usuario, String contrasena) throws SQLException {
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT id, nombre, apellidos, usuario, contrasena, rol FROM usuarios WHERE usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String contrasenaHashBD = rs.getString("contrasena");
                String contrasenaHashIngresada = hashSHA256(contrasena);

                debug("🔍 DEBUG Login:");
                debug("Usuario encontrado: " + rs.getString("usuario"));
                debug("Hash BD: " + contrasenaHashBD);
                debug("Hash ingresado: " + contrasenaHashIngresada);

                if (contrasenaHashBD.equals(contrasenaHashIngresada)) {
                    // ✅ GUARDAR INFORMACIÓN DEL USUARIO EN LA SESIÓN
                    SesionTemporal sesion = SesionTemporal.getInstancia();
                    
                    // Guardar datos del usuario logueado
                    sesion.setUsuarioLogueado(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("usuario"),
                        rs.getString("rol")
                    );
                    
                    debug("✅ Usuario logueado correctamente:");
                    debug("- ID: " + rs.getInt("id"));
                    debug("- Nombre: " + rs.getString("nombre") + " " + rs.getString("apellidos"));
                    debug("- Usuario: " + rs.getString("usuario"));
                    debug("- Rol: " + rs.getString("rol"));
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en validación de credenciales: " + e.getMessage());
            throw e;
        }
        return false;
    }

    private String hashSHA256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256", e);
        }
    }

    private void mostrarError(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        
        // Limpiar mensaje después de 4 segundos
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                javafx.application.Platform.runLater(() -> {
                    if (lblMensaje != null && lblMensaje.getText().equals(mensaje)) {
                        lblMensaje.setText("");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}