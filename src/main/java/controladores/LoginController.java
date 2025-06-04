package controladores;

import database.Conexion;
import javafx.application.Platform;
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
    private static final boolean DEBUG_MODE = false;
    private void debug(String mensaje) {
        if (DEBUG_MODE) {
        }
    }

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena; 
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

        debug("Usuario ingresado: " + usuario);
        debug("Contraseña ingresada: " + (contrasena.isEmpty() ? "VACÍA" : "***"));

        // Validar campos vacíos
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            debug("❌ Campos vacíos detectados");
            mostrarError("Por favor, completa todos los campos.");
            return;
        }

        // Deshabilitar botón durante el proceso
        btnIngresar.setDisable(true);
        if (lblMensaje != null) {
            lblMensaje.setText("Verificando credenciales...");
            lblMensaje.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        }

        // Ejecutar validación en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                debug("🔍 Iniciando validación de credenciales");
                boolean loginExitoso = validarCredenciales(usuario, contrasena);
                
                // Regresar al hilo de JavaFX para actualizar la UI
                Platform.runLater(() -> {
                    if (loginExitoso) {
                        debug("✅ Login exitoso, cambiando a panel principal");
                        if (lblMensaje != null) {
                            lblMensaje.setText("¡Acceso concedido! Cargando panel...");
                            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                        
                        // Cambiar a panel principal después de un pequeño delay
                        Platform.runLater(() -> {
                            try {
                                debug("🚀 Ejecutando cambio de vista a PanelPrincipal");
                                App.setRoot("PanelPrincipal");
                                debug("✅ Cambio de vista exitoso");
                            } catch (IOException e) {
                                debug("❌ Error al cambiar vista: " + e.getMessage());
                                e.printStackTrace();
                                mostrarError("Error al cargar el panel principal: " + e.getMessage());
                            }
                        });
                        
                    } else {
                        debug("❌ Login fallido");
                        mostrarError("Usuario o contraseña incorrectos.");
                    }
                    btnIngresar.setDisable(false);
                });
                
            } catch (SQLException e) {
                debug("❌ Error de base de datos: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarError("Error de conexión: " + e.getMessage());
                    btnIngresar.setDisable(false);
                });
            }
        }).start();
    }

    private boolean validarCredenciales(String usuario, String contrasena) throws SQLException {
        debug("🔍 Conectando a base de datos para validar credenciales");
        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                debug("❌ No se pudo conectar a la base de datos");
                throw new SQLException("No se pudo conectar a la base de datos");
            }
            
            debug("✅ Conexión a BD exitosa");
            String sql = "SELECT id, nombre, apellidos, usuario, contrasena, rol FROM usuarios WHERE usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                debug("✅ Usuario encontrado en BD: " + rs.getString("usuario"));
                String contrasenaHashBD = rs.getString("contrasena");
                String contrasenaHashIngresada = hashSHA256(contrasena);

                debug("Hash BD: " + contrasenaHashBD);
                debug("Hash ingresado: " + contrasenaHashIngresada);
                debug("Hashes coinciden: " + contrasenaHashBD.equals(contrasenaHashIngresada));

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
                    
                    debug("✅ Usuario logueado y guardado en sesión:");
                    debug("- ID: " + rs.getInt("id"));
                    debug("- Nombre: " + rs.getString("nombre") + " " + rs.getString("apellidos"));
                    debug("- Usuario: " + rs.getString("usuario"));
                    debug("- Rol: " + rs.getString("rol"));
                    debug("- Sesión válida: " + sesion.hayUsuarioLogueado());
                    
                    return true;
                } else {
                    debug("❌ Contraseñas no coinciden");
                }
            } else {
                debug("❌ Usuario no encontrado en BD");
            }
        } catch (SQLException e) {
            debug("❌ Error SQL: " + e.getMessage());
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
        debug("⚠️ Mostrando error: " + mensaje);
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        
        // Limpiar mensaje después de 4 segundos
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                Platform.runLater(() -> {
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