package controladores;

import database.Conexion;
import java.io.IOException;
import java.sql.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import modelos.SesionTemporal;

public class PaquetesPresupuestoController implements Initializable {
    private static final boolean DEBUG_MODE = false; // Cambia a true solo cuando necesites debug
    
    private void debug(String mensaje) {
        if (DEBUG_MODE) {
            System.out.println(mensaje);
        }
    }

    private int idPaqueteSeleccionado = -1; 
    
    // CAMPOS FXML - IMPORTANTE: ListaPaquete1 y ListaPaquete2 ahora son VBox
    @FXML private Label lblNombreCliente;
    @FXML private VBox ListaPaquete1, ListaPaquete2; // ¡CAMBIADO DE ListView A VBox!
    @FXML private TextField txtPaq, txtExtras1, txtExtras2, txtExtras3, txtExtras4;
    @FXML private TextField txtTotPaq, txtPresupTot, txtTotExtras;
    @FXML private Label lblExtra1, lblExtra2, lblExtra3, lblExtra4;
    @FXML private Label lblPaq1, lblPaq2, lblPrecioPaq1, lblPrecioPaq2;

    // Sistema de notificaciones sutiles
    private Label lblNotificacion;

    private Extra[] extras = new Extra[4];

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
        debug("✅ PaquetesPresupuestoController inicializado");
        
        SesionTemporal sesion = SesionTemporal.getInstancia();
        
        if (sesion.hayClienteSeleccionado()) {
            lblNombreCliente.setText(sesion.getClienteNombreCompleto());
            debug("✅ Cliente cargado: " + sesion.getClienteNombreCompleto());
        } else {
            lblNombreCliente.setText("Cliente no seleccionado");
            debug("⚠️ No hay cliente seleccionado");
        }
        
        inicializarUI();
        cargarDatos();
        crearNotificacionSutil();
    }

    private void crearNotificacionSutil() {
        // Crear label para notificaciones sutiles
        lblNotificacion = new Label();
        lblNotificacion.setStyle(
            "-fx-background-color: rgba(46, 204, 113, 0.9); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 15 25; " +
            "-fx-background-radius: 25px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3); " +
            "-fx-alignment: center;"
        );
        lblNotificacion.setVisible(false);
        lblNotificacion.setOpacity(0);
        
        // Agregar al contenedor principal
        javafx.application.Platform.runLater(() -> {
            try {
                if (lblNombreCliente.getScene() != null && lblNombreCliente.getScene().getRoot() != null) {
                    if (lblNombreCliente.getScene().getRoot() instanceof javafx.scene.layout.AnchorPane) {
                        javafx.scene.layout.AnchorPane root = (javafx.scene.layout.AnchorPane) lblNombreCliente.getScene().getRoot();
                        
                        // Posicionar la notificación en la parte superior central
                        javafx.scene.layout.AnchorPane.setTopAnchor(lblNotificacion, 20.0);
                        javafx.scene.layout.AnchorPane.setLeftAnchor(lblNotificacion, 0.0);
                        javafx.scene.layout.AnchorPane.setRightAnchor(lblNotificacion, 0.0);
                        
                        root.getChildren().add(lblNotificacion);
                        debug("✅ Sistema de notificaciones sutiles configurado");
                    }
                }
            } catch (Exception e) {
                debug("⚠️ No se pudo configurar el sistema de notificaciones: " + e.getMessage());
            }
        });
    }

    private void mostrarNotificacionSutil(String mensaje, boolean esExito) {
        if (lblNotificacion == null) return;
        
        // Configurar estilo según el tipo
        String color = esExito ? "rgba(46, 204, 113, 0.9)" : "rgba(231, 76, 60, 0.9)";
        String icono = esExito ? "✅ " : "⚠️ ";
        
        lblNotificacion.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 15 25; " +
            "-fx-background-radius: 25px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3); " +
            "-fx-alignment: center;"
        );
        
        lblNotificacion.setText(icono + mensaje);
        lblNotificacion.setVisible(true);
        
        // Animación de entrada
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), lblNotificacion);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Auto-ocultar después de 3 segundos
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), lblNotificacion);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> lblNotificacion.setVisible(false));
            fadeOut.play();
        });
        pause.play();
    }

    private void inicializarUI() {
        // Ya no necesitamos configurar ListViews porque ahora son VBox
        // Los VBox se configuran automáticamente
        
        // Inicializar campos de extras
        if (txtExtras1 != null) txtExtras1.setText("0");
        if (txtExtras2 != null) txtExtras2.setText("0");
        if (txtExtras3 != null) txtExtras3.setText("0");
        if (txtExtras4 != null) txtExtras4.setText("0");

        // Hacer campos no editables
        if (txtExtras1 != null) txtExtras1.setEditable(false);
        if (txtExtras2 != null) txtExtras2.setEditable(false);
        if (txtExtras3 != null) txtExtras3.setEditable(false);
        if (txtExtras4 != null) txtExtras4.setEditable(false);

        // Inicializar labels de extras
        if (lblExtra1 != null) lblExtra1.setText("Cargando extra 1...");
        if (lblExtra2 != null) lblExtra2.setText("Cargando extra 2...");
        if (lblExtra3 != null) lblExtra3.setText("Cargando extra 3...");
        if (lblExtra4 != null) lblExtra4.setText("Cargando extra 4...");
        
        // Inicializar totales
        if (txtTotExtras != null) txtTotExtras.setText("0.00");
        if (txtPresupTot != null) txtPresupTot.setText("0.00");
        
        debug("✅ UI inicializada correctamente");
    }

    private void cargarDatos() {
        cargarPaquetes();
        cargarExtras();
    }

    private void cargarPaquetes() {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre, descripcion, precio FROM paquetes");
             ResultSet rs = stmt.executeQuery()) {

            debug("🔄 Cargando paquetes desde BD...");

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion"); 
                double precio = rs.getDouble("precio");

                debug("📦 Paquete encontrado: " + nombre + " - $" + precio);

                String detalleHoras = "";
                List<String> detalles = new ArrayList<>();
                
                if (descripcion != null && !descripcion.trim().isEmpty()) {
                    for (String detalle : descripcion.split(",")) {
                        if (detalle.toLowerCase().contains("servicio") || detalle.toLowerCase().contains("hora")) {
                            if (detalle.contains(":")) {
                                detalleHoras = detalle.substring(detalle.indexOf(":")).trim();
                                detalleHoras = "Servicios" + detalleHoras;
                            } else {
                                detalleHoras = detalle.trim();
                            }
                        } else {
                            detalles.add("• " + detalle.trim());
                        }
                    }
                } else {
                    detalles.add("• Información no disponible");
                }
                
                if (!listContainsCaseInsensitive(detalles, "cancha")) {
                    detalles.add(0, "• Cancha");
                }
                
                if (!listContainsCaseInsensitive(detalles, "juegos de mesa")) {
                    detalles.add("• Juegos de mesa");
                }

                if (nombre.equalsIgnoreCase("Paquete 1")) {
                    if (lblPaq1 != null) {
                        lblPaq1.setText(nombre + (detalleHoras.isEmpty() ? "" : " (" + detalleHoras + ")"));
                    }
                    // CAMBIO CRÍTICO: Usar VBox en lugar de ListView
                    if (ListaPaquete1 != null) {
                        ListaPaquete1.getChildren().clear();
                        for (String detalle : detalles) {
                            Label servicioLabel = new Label(detalle);
                            servicioLabel.setStyle("-fx-font-size: 12px;");
                            servicioLabel.getStyleClass().add("label");
                            ListaPaquete1.getChildren().add(servicioLabel);
                        }
                    }
                    if (lblPrecioPaq1 != null) {
                        lblPrecioPaq1.setText("Precio: " + String.format("%.2f", precio));
                    }
                } else if (nombre.equalsIgnoreCase("Paquete 2")) {
                    if (lblPaq2 != null) {
                        lblPaq2.setText(nombre + (detalleHoras.isEmpty() ? "" : " (" + detalleHoras + ")"));
                    }
                    // CAMBIO CRÍTICO: Usar VBox en lugar de ListView
                    if (ListaPaquete2 != null) {
                        ListaPaquete2.getChildren().clear();
                        for (String detalle : detalles) {
                            Label servicioLabel = new Label(detalle);
                            servicioLabel.setStyle("-fx-font-size: 12px;");
                            servicioLabel.getStyleClass().add("label");
                            ListaPaquete2.getChildren().add(servicioLabel);
                        }
                    }
                    if (lblPrecioPaq2 != null) {
                        lblPrecioPaq2.setText("Precio: " + String.format("%.2f", precio));
                    }
                }
            }
            
            debug("✅ Paquetes cargados correctamente");

        } catch (SQLException e) {
            System.err.println("❌ Error al cargar paquetes: " + e.getMessage());
            mostrarNotificacionSutil("Error al cargar paquetes: " + e.getMessage(), false);
        }
    }

    private boolean listContainsCaseInsensitive(List<String> list, String searchStr) {
        for (String item : list) {
            if (item.toLowerCase().contains(searchStr.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void cargarExtras() {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre, precio FROM extras ORDER BY nombre LIMIT 4");
             ResultSet rs = stmt.executeQuery()) {

            debug("🔄 Cargando extras desde BD...");

            int i = 0;
            Label[] labels = {lblExtra1, lblExtra2, lblExtra3, lblExtra4};

            while (rs.next() && i < 4) {
                String nombre = rs.getString("nombre");
                int precio = rs.getInt("precio");

                extras[i] = new Extra(nombre, precio);
                if (labels[i] != null) {
                    labels[i].setText(nombre + " (" + String.format("%.2f", (double)precio) + ")");
                }
                
                debug("🎁 Extra cargado: " + nombre + " - $" + precio);
                i++;
            }

            while (i < 4) {
                extras[i] = new Extra("Extra no disponible", 0);
                if (labels[i] != null) {
                    labels[i].setText("Extra no disponible");
                }
                i++;
            }
            
            debug("✅ Extras cargados correctamente");

        } catch (SQLException e) {
            System.err.println("❌ Error al cargar extras: " + e.getMessage());
            mostrarNotificacionSutil("Error al cargar extras: " + e.getMessage(), false);
            
            // Establecer valores por defecto en caso de error
            if (lblExtra1 != null) lblExtra1.setText("Error al cargar extras");
            if (lblExtra2 != null) lblExtra2.setText("Error al cargar extras");
            if (lblExtra3 != null) lblExtra3.setText("Error al cargar extras");
            if (lblExtra4 != null) lblExtra4.setText("Error al cargar extras");
        }
    }

    private void actualizarExtra(int index, int cambio) {
        if (index < 0 || index >= extras.length || extras[index] == null) return;
        
        Extra extra = extras[index];
        
        // LÍMITE MÁXIMO DE 4 POR EXTRA
        extra.cantidad = Math.max(0, Math.min(extra.cantidad + cambio, 4));

        TextField[] textFields = {txtExtras1, txtExtras2, txtExtras3, txtExtras4};
        if (textFields[index] != null) {
            textFields[index].setText(String.valueOf(extra.cantidad));
        }

        actualizarTotalExtras();
        actualizarPresupuestoTotal();
        
        // Mostrar mensaje si se alcanza el límite
        if (cambio > 0 && extra.cantidad == 4) {
            mostrarNotificacionSutil("Límite máximo alcanzado para " + extra.nombre + " (4 unidades)", false);
        }
        
        debug("🔄 Extra actualizado: " + extra.nombre + " x" + extra.cantidad);
    }

    private void actualizarTotalExtras() {
        double total = 0;
        for (int i = 0; i < 4; i++) {
            if (extras[i] != null) {
                total += extras[i].cantidad * extras[i].precio;
            }
        }
        if (txtTotExtras != null) {
            txtTotExtras.setText(String.format("%.2f", total));
        }
    }

    private void actualizarPresupuestoTotal() {
        try {
            double totalPaquete = 0;
            if (txtTotPaq != null && !txtTotPaq.getText().isEmpty()) {
                totalPaquete = Double.parseDouble(txtTotPaq.getText().replace("$", ""));
            }
            
            double totalExtras = 0;
            if (txtTotExtras != null && !txtTotExtras.getText().isEmpty()) {
                totalExtras = Double.parseDouble(txtTotExtras.getText().replace("$", ""));
            }
            
            if (txtPresupTot != null) {
                txtPresupTot.setText(String.format("%.2f", totalPaquete + totalExtras));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error en cálculo del total: " + e.getMessage());
            mostrarNotificacionSutil("Error en el cálculo del total", false);
        }
    }

    // ========== MÉTODOS FXML PARA PAQUETES ==========
    @FXML 
    private void seleccionarPaquete1(ActionEvent event) { 
        debug("📦 Seleccionando Paquete 1...");
        seleccionarPaquete("Paquete 1"); 
    }
    
    @FXML 
    private void seleccionarPaquete2(ActionEvent event) { 
        debug("📦 Seleccionando Paquete 2...");
        seleccionarPaquete("Paquete 2"); 
    }

    private void seleccionarPaquete(String nombrePaquete) {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, precio FROM paquetes WHERE nombre = ?")) {

            stmt.setString(1, nombrePaquete);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                idPaqueteSeleccionado = rs.getInt("id");
                double precio = rs.getDouble("precio");
                
                if (txtPaq != null) {
                    txtPaq.setText(nombrePaquete + " (ID: " + idPaqueteSeleccionado + ")");
                }
                if (txtTotPaq != null) {
                    txtTotPaq.setText(String.format("%.2f", precio));
                }
                
                actualizarPresupuestoTotal();
                
                debug("✅ Paquete seleccionado: " + nombrePaquete + " - $" + precio);
                mostrarNotificacionSutil("Paquete seleccionado: " + nombrePaquete, true);
                
            } else {
                debug("❌ No se encontró el paquete: " + nombrePaquete);
                mostrarNotificacionSutil("No se encontró el paquete: " + nombrePaquete, false);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al seleccionar paquete: " + e.getMessage());
            mostrarNotificacionSutil("Error al seleccionar paquete: " + e.getMessage(), false);
        }
    }

    // ========== MÉTODOS FXML PARA EXTRAS ==========
    @FXML private void handleAgregarExtra1(ActionEvent event) { actualizarExtra(0, 1); }
    @FXML private void handleAgregarExtra2(ActionEvent event) { actualizarExtra(1, 1); }
    @FXML private void handleAgregarExtra3(ActionEvent event) { actualizarExtra(2, 1); }
    @FXML private void handleAgregarExtra4(ActionEvent event) { actualizarExtra(3, 1); }

    @FXML private void handleQuitarExtra1(ActionEvent event) { actualizarExtra(0, -1); }
    @FXML private void handleQuitarExtra2(ActionEvent event) { actualizarExtra(1, -1); }
    @FXML private void handleQuitarExtra3(ActionEvent event) { actualizarExtra(2, -1); }
    @FXML private void handleQuitarExtra4(ActionEvent event) { actualizarExtra(3, -1); }

    // ========== MÉTODOS FXML PARA NAVEGACIÓN ==========
   @FXML
private void irAVistaPrevia(ActionEvent event) {
    System.out.println("🔄 === INICIANDO NAVEGACIÓN A VISTA PREVIA ===");
    
    try {
        // PASO 1: Validación básica
        System.out.println("🔄 PASO 1: Validando selección...");
        if (!validarSeleccion()) {
            System.out.println("❌ PASO 1 FALLÓ: Validación de selección");
            return;
        }
        System.out.println("✅ PASO 1: Validación exitosa");
        
        // PASO 2: Guardar selección
        System.out.println("🔄 PASO 2: Guardando selección...");
        try {
            guardarSeleccion();
            System.out.println("✅ PASO 2: Selección guardada");
        } catch (Exception e) {
            System.err.println("❌ PASO 2 FALLÓ: Error al guardar selección: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacionSutil("Error al guardar la selección: " + e.getMessage(), false);
            return;
        }
        
        // PASO 3: Verificar datos en sesión DESPUÉS de guardar
        System.out.println("🔄 PASO 3: Verificando datos en sesión...");
        SesionTemporal sesion = SesionTemporal.getInstancia();
        
        if (sesion == null) {
            System.err.println("❌ PASO 3 FALLÓ: SesionTemporal es null");
            mostrarNotificacionSutil("Error: Sesión temporal no disponible", false);
            return;
        }
        
        // Verificar cliente
        if (!sesion.hayClienteSeleccionado()) {
            System.err.println("❌ PASO 3 FALLÓ: No hay cliente seleccionado");
            System.err.println("   Cliente ID: " + sesion.getClienteId());
            System.err.println("   Cliente Nombre: " + sesion.getClienteNombreCompleto());
            mostrarNotificacionSutil("Error: No hay cliente en la sesión", false);
            return;
        }
        
        // Verificar paquete
        if (!sesion.hayPaqueteSeleccionado()) {
            System.err.println("❌ PASO 3 FALLÓ: No hay paquete seleccionado");
            System.err.println("   Paquete ID: " + sesion.getPaqueteId());
            System.err.println("   Paquete Nombre: " + sesion.getPaqueteNombre());
            System.err.println("   Paquete Precio: " + sesion.getPaquetePrecio());
            mostrarNotificacionSutil("Error: No hay paquete en la sesión", false);
            return;
        }
        
        System.out.println("✅ PASO 3: Datos verificados en sesión");
        System.out.println("   - Cliente: " + sesion.getClienteNombreCompleto());
        System.out.println("   - Paquete: " + sesion.getPaqueteNombre());
        System.out.println("   - Total: $" + sesion.getTotalGeneral());
        
        // PASO 4: Mostrar resumen completo para debug
        if (DEBUG_MODE) {
            sesion.mostrarResumen();
        }
        
        // PASO 5: Intentar navegación
        System.out.println("🔄 PASO 5: Iniciando navegación...");
        
        try {
            System.out.println("🔄 PASO 5a: Llamando App.setRoot(\"VistaPreviaPresupuesto\")...");
            
            // Usar el método corregido de App
            App.setRoot("VistaPreviaPresupuesto");
            
            System.out.println("✅ PASO 5: Navegación completada exitosamente");
            
        } catch (IOException ioException) {
            System.err.println("❌ PASO 5 FALLÓ: IOException durante navegación");
            System.err.println("   Mensaje: " + ioException.getMessage());
            ioException.printStackTrace();
            
            // Mostrar información de debug
            System.err.println("🔍 DEBUG INFO:");
            System.err.println("   - ¿Existe VistaPreviaPresupuesto.fxml? Verificar en /vistas/");
            System.err.println("   - ¿Está el controlador bien configurado?");
            System.err.println("   - ¿Hay errores en el FXML?");
            
            mostrarNotificacionSutil("Error al cargar la vista previa. Verifica que el archivo FXML existe.", false);
            
        } catch (Exception navException) {
            System.err.println("❌ PASO 5 FALLÓ: Excepción general durante navegación");
            System.err.println("   Tipo: " + navException.getClass().getSimpleName());
            System.err.println("   Mensaje: " + navException.getMessage());
            navException.printStackTrace();
            mostrarNotificacionSutil("Error de navegación: " + navException.getMessage(), false);
        }
        
    } catch (Exception generalException) {
        System.err.println("❌ ERROR GENERAL EN irAVistaPrevia:");
        System.err.println("   Tipo: " + generalException.getClass().getSimpleName());
        System.err.println("   Mensaje: " + generalException.getMessage());
        generalException.printStackTrace();
        mostrarNotificacionSutil("Error crítico: " + generalException.getMessage(), false);
    }
    
    System.out.println("🔄 === FIN DE NAVEGACIÓN A VISTA PREVIA ===");
}

    @FXML 
    private void handleRegresarButtonAction(ActionEvent event) {
        try {
            debug("🔄 Regresando a Eventos...");
            App.setRoot("Eventos");
        } catch (IOException e) {
            System.err.println("❌ Error al regresar: " + e.getMessage());
            mostrarNotificacionSutil("Error al regresar: " + e.getMessage(), false);
        }
    }
    
    private void verificarEstadoCompleto() {
    System.out.println("========== VERIFICACIÓN COMPLETA ==========");
    
    SesionTemporal sesion = SesionTemporal.getInstancia();
    
    // Usuario
    System.out.println("👤 USUARIO:");
    System.out.println("   - Logueado: " + sesion.hayUsuarioLogueado());
    System.out.println("   - Nombre: " + sesion.getNombreCompletoUsuario());
    System.out.println("   - ID: " + sesion.getUsuarioId());
    
    // Cliente
    System.out.println("🧑‍💼 CLIENTE:");
    System.out.println("   - Seleccionado: " + sesion.hayClienteSeleccionado());
    System.out.println("   - Nombre: " + sesion.getClienteNombreCompleto());
    System.out.println("   - ID: " + sesion.getClienteId());
    
    // Paquete
    System.out.println("📦 PAQUETE:");
    System.out.println("   - Seleccionado: " + sesion.hayPaqueteSeleccionado());
    System.out.println("   - Nombre: " + sesion.getPaqueteNombre());
    System.out.println("   - ID: " + sesion.getPaqueteId());
    System.out.println("   - Precio: $" + sesion.getPaquetePrecio());
    
    // Extras
    System.out.println("✨ EXTRAS:");
    System.out.println("   - Tiene extras: " + sesion.tieneExtras());
    System.out.println("   - Total extras: $" + sesion.getTotalExtras());
    System.out.println("   - Resumen: " + sesion.getResumenExtras());
    
    // Total
    System.out.println("💰 TOTAL GENERAL: $" + sesion.getTotalGeneral());
    
    System.out.println("==========================================");
}

    // ========== MÉTODOS AUXILIARES ==========
    private boolean validarSeleccion() {
        debug("🔍 Iniciando validación de selección...");
        
        // Validar sesión temporal
        SesionTemporal sesion = SesionTemporal.getInstancia();
        if (sesion == null) {
            mostrarNotificacionSutil("Error: Sesión temporal no disponible", false);
            return false;
        }
        
        // Validar cliente
        if (!sesion.hayClienteSeleccionado()) {
            mostrarNotificacionSutil("Error: No hay cliente seleccionado en la sesión", false);
            debug("❌ Validación fallida: No hay cliente");
            return false;
        }
        debug("✅ Cliente validado: " + sesion.getClienteNombreCompleto());
        
        // Validar paquete seleccionado
        if (txtPaq == null || txtPaq.getText() == null || txtPaq.getText().trim().isEmpty()) {
            mostrarNotificacionSutil("Debe seleccionar un paquete para continuar", false);
            debug("❌ Validación fallida: No hay paquete seleccionado");
            return false;
        }
        debug("✅ Paquete seleccionado: " + txtPaq.getText());
        
        // Validar que el ID del paquete esté establecido
        if (idPaqueteSeleccionado <= 0) {
            mostrarNotificacionSutil("Error interno: ID de paquete no válido", false);
            debug("❌ Validación fallida: ID de paquete inválido: " + idPaqueteSeleccionado);
            return false;
        }
        debug("✅ ID de paquete válido: " + idPaqueteSeleccionado);
        
        // Validar precio del paquete
        if (txtTotPaq == null || txtTotPaq.getText() == null || txtTotPaq.getText().trim().isEmpty()) {
            mostrarNotificacionSutil("Error: Precio del paquete no establecido", false);
            debug("❌ Validación fallida: Precio del paquete vacío");
            return false;
        }
        
        try {
            String precioTexto = txtTotPaq.getText().replace("$", "").trim();
            double precio = Double.parseDouble(precioTexto);
            if (precio <= 0) {
                mostrarNotificacionSutil("Error: El precio del paquete debe ser mayor a 0", false);
                debug("❌ Validación fallida: Precio inválido: " + precio);
                return false;
            }
            debug("✅ Precio del paquete válido: $" + precio);
        } catch (NumberFormatException e) {
            mostrarNotificacionSutil("Error: Formato de precio inválido", false);
            debug("❌ Validación fallida: Error al parsear precio: " + e.getMessage());
            return false;
        }
        
        debug("✅ Todas las validaciones pasaron correctamente");
        return true;
    }

    private void guardarSeleccion() {
        SesionTemporal sesion = SesionTemporal.getInstancia();
        
        try {
            debug("🔄 Iniciando guardado de selección...");
            
            // Verificar que hay cliente
            if (!sesion.hayClienteSeleccionado()) {
                throw new RuntimeException("No hay cliente seleccionado en la sesión");
            }
            debug("✅ Cliente verificado: " + sesion.getClienteNombreCompleto());
            
            // Guardar paquete
            String nombrePaquete = txtPaq.getText();
            if (nombrePaquete == null || nombrePaquete.trim().isEmpty()) {
                throw new RuntimeException("No se ha seleccionado ningún paquete");
            }
            
            String precioTexto = txtTotPaq.getText().replace("$", "");
            double precioPaquete = Double.parseDouble(precioTexto);
            
            debug("🔄 Guardando paquete: " + nombrePaquete + " - $" + precioPaquete);
            sesion.setPaquete(idPaqueteSeleccionado, nombrePaquete, precioPaquete);
            debug("✅ Paquete guardado en sesión");
            
            // Guardar extras
            List<modelos.Extra> extrasParaGuardar = new ArrayList<>();
            for (int i = 0; i < extras.length; i++) {
                if (extras[i] != null && extras[i].cantidad > 0) {
                    modelos.Extra extraObj = new modelos.Extra(extras[i].nombre, extras[i].precio);
                    extraObj.setCantidad(extras[i].cantidad);
                    extrasParaGuardar.add(extraObj);
                    debug("🎁 Extra agregado: " + extras[i].nombre + " x" + extras[i].cantidad);
                }
            }
            sesion.setExtrasSeleccionados(extrasParaGuardar);
            debug("✅ " + extrasParaGuardar.size() + " extras guardados en sesión");
            
            // Verificar que los datos se guardaron correctamente
            if (!sesion.hayPaqueteSeleccionado()) {
                throw new RuntimeException("Error: El paquete no se guardó correctamente en la sesión");
            }
            
            debug("✅ Datos guardados correctamente en sesión temporal");
            debug("📊 Total general calculado: $" + sesion.getTotalGeneral());
            
            if (DEBUG_MODE) {
                sesion.mostrarResumen();
            }
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Error al parsear precio: " + e.getMessage());
            throw new RuntimeException("Error en el formato del precio: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("❌ Error al guardar selección: " + e.getMessage());
            throw new RuntimeException("Error al procesar la selección: " + e.getMessage());
        }
    }
}