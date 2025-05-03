package controladores;

import database.Conexion;
import java.io.IOException;
import java.sql.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class presupuesto implements Initializable {

    // Componentes FXML (asegúrate de que todos los campos estén declarados)
    @FXML private ListView<String> ListaPaquete1, ListaPaquete2;
    @FXML private TextField txtPaq, txtExtras1, txtExtras2, txtExtras3, txtExtras4;
    @FXML private TextField txtTotPaq, txtPresupTot, txtTotExtras;
    @FXML private Label lblExtra1, lblExtra2, lblExtra3, lblExtra4;
    @FXML private Button btnSelecPaq1, btnSelecPaq2, btnQuitarPaq1, btnQuitarPaq2;
    @FXML private Button btnAgregarExtra1, btnAgregarExtra2, btnAgregarExtra3, btnAgregarExtra4;
    @FXML private Button btnQuitarExtra1, btnQuitarExtra2, btnQuitarExtra3, btnQuitarExtra4;
    @FXML private Button btnSiguiente, btnRegresar;

    // Datos de extras
    private Extra[] extras = new Extra[4];

    // Clase interna para manejar extras
    private class Extra {
        String nombre;
        int precio;
        int cantidad;
        
        public Extra(String nombre, int precio) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = 0;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarUI();
        cargarDatos();
    }
    // Métodos para seleccionar paquetes

    // Métodos para quitar paquetes (CORREGIDOS)
    @FXML 
    private void quitarPaquete1() { 
        limpiarPaquete("1"); 
    }
    
    @FXML 
    private void quitarPaquete2() { 
        limpiarPaquete("2"); 
    }

    private void limpiarPaquete(String idPaq) {
        if (txtPaq.getText().contains("Paquete " + idPaq)) {
            txtPaq.clear();
            txtTotPaq.clear();
            actualizarPresupuestoTotal();
        }
    }

    private void inicializarUI() {
        // Configurar ListViews
        ListaPaquete1.setMouseTransparent(true);
        ListaPaquete1.setFocusTraversable(false);
        ListaPaquete2.setMouseTransparent(true);
        ListaPaquete2.setFocusTraversable(false);
        
        // Inicializar campos de extras
        txtExtras1.setText("0");
        txtExtras2.setText("0");
        txtExtras3.setText("0");
        txtExtras4.setText("0");
        
        // Configurar textos iniciales
        lblExtra1.setText("Cargando extra 1...");
        lblExtra2.setText("Cargando extra 2...");
        lblExtra3.setText("Cargando extra 3...");
        lblExtra4.setText("Cargando extra 4...");
    }

    private void cargarDatos() {
        cargarPaquetes();
        cargarExtras();
    }

    private void cargarPaquetes() {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre, descripcion, precio FROM paquetes");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                double precio = rs.getDouble("precio");
                
                ListView<String> lista = nombre.equalsIgnoreCase("Paquete 1") ? ListaPaquete1 : ListaPaquete2;
                
                lista.getItems().add("🔹 " + nombre);
                for (String detalle : descripcion.split(", ")) {
                    lista.getItems().add("  • " + detalle);
                }
                lista.getItems().add("💰 Precio: $" + precio);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los paquetes: " + e.getMessage());
        }
    }

    private void cargarExtras() {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre, precio FROM extras ORDER BY nombre LIMIT 4");
             ResultSet rs = stmt.executeQuery()) {
            
            int i = 0;
            Label[] labels = {lblExtra1, lblExtra2, lblExtra3, lblExtra4};
            
            while (rs.next() && i < 4) {
                String nombre = rs.getString("nombre");
                int precio = rs.getInt("precio");
                
                extras[i] = new Extra(nombre, precio);
                labels[i].setText(nombre + " ($" + precio + ")");
                i++;
            }
            
            // Rellenar con valores por defecto si hay menos de 4 extras
            while (i < 4) {
                extras[i] = new Extra("Extra no disponible", 0);
                labels[i].setText("Extra no disponible");
                i++;
            }
            
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los extras: " + e.getMessage());
            for (int i = 0; i < 4; i++) {
                extras[i] = new Extra("Error al cargar", 0);
                lblExtra1.setText("Error al cargar extras");
                lblExtra2.setText("Error al cargar extras");
                lblExtra3.setText("Error al cargar extras");
                lblExtra4.setText("Error al cargar extras");
            }
        }
    }

    // Métodos para manejar selección de paquetes
    @FXML private void seleccionarPaquete1() { seleccionarPaquete("Paquete 1"); }
    @FXML private void seleccionarPaquete2() { seleccionarPaquete("Paquete 2"); }

    private void seleccionarPaquete(String nombrePaquete) {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, precio FROM paquetes WHERE nombre = ?")) {
            
            stmt.setString(1, nombrePaquete);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                txtPaq.setText(nombrePaquete + " (ID: " + rs.getInt("id") + ")");
                txtTotPaq.setText("$" + rs.getDouble("precio"));
                actualizarPresupuestoTotal();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo seleccionar el paquete: " + e.getMessage());
        }
    }

    // Métodos para manejar extras
    @FXML private void handleAgregarExtra1() { actualizarExtra(0, 1); }
    @FXML private void handleAgregarExtra2() { actualizarExtra(1, 1); }
    @FXML private void handleAgregarExtra3() { actualizarExtra(2, 1); }
    @FXML private void handleAgregarExtra4() { actualizarExtra(3, 1); }

    @FXML private void handleQuitarExtra1() { actualizarExtra(0, -1); }
    @FXML private void handleQuitarExtra2() { actualizarExtra(1, -1); }
    @FXML private void handleQuitarExtra3() { actualizarExtra(2, -1); }
    @FXML private void handleQuitarExtra4() { actualizarExtra(3, -1); }

    private void actualizarExtra(int index, int cambio) {
        Extra extra = extras[index];
        extra.cantidad = Math.max(0, extra.cantidad + cambio);
        
        switch(index) {
            case 0: txtExtras1.setText(String.valueOf(extra.cantidad)); break;
            case 1: txtExtras2.setText(String.valueOf(extra.cantidad)); break;
            case 2: txtExtras3.setText(String.valueOf(extra.cantidad)); break;
            case 3: txtExtras4.setText(String.valueOf(extra.cantidad)); break;
        }
        
        actualizarTotalExtras();
        actualizarPresupuestoTotal();
    }

    private void actualizarTotalExtras() {
        int total = 0;
        for (int i = 0; i < 4; i++) {
            total += extras[i].cantidad * extras[i].precio;
        }
        txtTotExtras.setText("$" + total);
    }

    private void actualizarPresupuestoTotal() {
        try {
            double totalPaquete = txtTotPaq.getText().isEmpty() ? 0 : 
                Double.parseDouble(txtTotPaq.getText().replace("$", ""));
            double totalExtras = txtTotExtras.getText().isEmpty() ? 0 : 
                Double.parseDouble(txtTotExtras.getText().replace("$", ""));
            txtPresupTot.setText(String.format("$%.2f", totalPaquete + totalExtras));
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Error en el cálculo del total");
        }
    }

    // Métodos de navegación
    @FXML private void irASiguienteVista() throws IOException {
        if (validarSeleccion()) {
            guardarSeleccion();
            App.setRoot("Eventos2");
        }
    }

    @FXML private void handleRegresarButtonAction() throws IOException {
        App.setRoot("Eventos");
    }

    private boolean validarSeleccion() {
        if (txtPaq.getText().isEmpty()) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un paquete para continuar");
            return false;
        }
        return true;
    }

    private void guardarSeleccion() {
        // Aquí puedes implementar la lógica para guardar la selección
        // en tu modelo de datos o base de datos
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}