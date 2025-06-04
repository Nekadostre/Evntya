package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import modelos.SesionTemporal;

import java.io.IOException;

public class PanelPrincipalController {
    private static final boolean DEBUG_MODE = true;
    
    private void debug(String mensaje) {
        if (DEBUG_MODE) {
            System.out.println(mensaje);
        }
    }

    @FXML private Label lblNombreUsuario;
    
    @FXML
    public void initialize() {
        cargarDatosUsuario();
    }
    
    private void cargarDatosUsuario() {
        SesionTemporal sesion = SesionTemporal.getInstancia();
        if (sesion.hayUsuarioLogueado()) {
            String nombreCompleto = sesion.getNombreCompletoUsuario();
            String rol = sesion.getUsuarioRol();
            String rolCapitalizado = rol.substring(0, 1).toUpperCase() + rol.substring(1).toLowerCase();
            lblNombreUsuario.setText("Empleado: " + nombreCompleto + " (" + rolCapitalizado + ")");
            debug("✅ Panel Principal cargado para: " + nombreCompleto);
        } else {
            lblNombreUsuario.setText("Empleado: Usuario no identificado");
            debug("⚠️ No hay usuario en sesión");
        }
    }

    @FXML
    private void abrirClientes() {
        try {
            App.setRoot("Clientes");
            debug("📋 Navegando a Clientes");
        } catch (IOException e) {
            System.err.println("❌ Error al abrir Clientes: " + e.getMessage());
        }
    }

    @FXML
    private void abrirEventos() {
        try {
            App.setRoot("Eventos");
            debug("🎂 Navegando a Eventos");
        } catch (IOException e) {
            System.err.println("❌ Error al abrir Eventos: " + e.getMessage());
        }
    }

    @FXML
    private void abrirReservas() {
        try {
            App.setRoot("Reservas");
            debug("📅 Navegando a Reservas");
        } catch (IOException e) {
            System.err.println("❌ Error al abrir Reservas: " + e.getMessage());
        }
    }

    @FXML
    private void cerrarSesion() {
        try {
            // Limpiar completamente la sesión (incluyendo usuario)
            SesionTemporal.getInstancia().logout();
            debug("🚪 Sesión cerrada, regresando al login");
            App.setRoot("LoginView");
        } catch (IOException e) {
            System.err.println("❌ Error al cerrar sesión: " + e.getMessage());
        }
    }
}