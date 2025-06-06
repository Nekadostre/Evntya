package controladores;

import database.Conexion;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import modelos.Cliente;
import modelos.SesionTemporal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;

public class ClientePresupuestoController {
    private static final boolean DEBUG_MODE = false; // Cambia a true solo cuando necesites debug
    
    private void debug(String mensaje) {
        if (DEBUG_MODE) {
            System.out.println(mensaje);
        }
    }

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellidoPaterno;
    @FXML private TableColumn<Cliente, String> colApellidoMaterno;
    @FXML private TableColumn<Cliente, String> colRFC;
    @FXML private TableColumn<Cliente, String> colTelefono;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtRFC;
    @FXML private TextField txtBuscar;

    private Label lblNotificacion;
    private VBox contenedorPrincipal;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private int totalClientes = 0;

    @FXML
    public void initialize() {
        debug("🔧 Inicializando ClientePresupuestoController (Pantalla Completa)...");
        
        configurarTabla();
        cargarClientes();
        cargarClienteSeleccionado();
        configurarValidaciones();
        crearNotificacionSutil();
        limpiarArchivosTemporales(); // Limpiar archivos temporales antiguos
        
        debug("✅ ClientePresupuestoController inicializado correctamente");
    }

    private void crearNotificacionSutil() {
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
        
        try {
            javafx.application.Platform.runLater(() -> {
                if (txtNombre.getScene() != null && txtNombre.getScene().getRoot() != null) {
                    if (txtNombre.getScene().getRoot() instanceof javafx.scene.layout.AnchorPane) {
                        javafx.scene.layout.AnchorPane root = (javafx.scene.layout.AnchorPane) txtNombre.getScene().getRoot();
                        
                        // Posicionar la notificación en la parte superior central
                        javafx.scene.layout.AnchorPane.setTopAnchor(lblNotificacion, 20.0);
                        javafx.scene.layout.AnchorPane.setLeftAnchor(lblNotificacion, 0.0);
                        javafx.scene.layout.AnchorPane.setRightAnchor(lblNotificacion, 0.0);
                        
                        root.getChildren().add(lblNotificacion);
                        debug("✅ Sistema de notificaciones sutiles configurado");
                    }
                }
            });
        } catch (Exception e) {
            debug("⚠️ No se pudo configurar el sistema de notificaciones: " + e.getMessage());
        }
    }

    private void mostrarNotificacionSutil(String mensaje, boolean esExito) {
        if (lblNotificacion == null) return;
        
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
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), lblNotificacion);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), lblNotificacion);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> lblNotificacion.setVisible(false));
            fadeOut.play();
        });
        pause.play();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidoPaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        colApellidoMaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        colRFC.setCellValueFactory(new PropertyValueFactory<>("rfc"));
        colTelefono.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            String telefono = cliente.getTelefono();
            return new javafx.beans.property.SimpleStringProperty(
                telefono != null && !telefono.isEmpty() ? telefono : "Sin teléfono"
            );
        });
        
        configurarEstilosTabla();
        
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                debug("🔍 Cliente seleccionado en tabla: " + newSelection.getNombreCompleto());
            }
        });
    }

    private void configurarEstilosTabla() {
        colNombre.setResizable(true);
        colApellidoPaterno.setResizable(true);
        colApellidoMaterno.setResizable(true);
        colRFC.setResizable(true);
        colTelefono.setResizable(true);
        colTelefono.setStyle("-fx-alignment: CENTER;");
        colRFC.setStyle("-fx-alignment: CENTER;");
        tablaClientes.getStyleClass().add("tabla-clientes-fullscreen");
    }
    
    private void configurarValidaciones() {
        txtNombre.textProperty().addListener((obs, oldText, newText) -> {
            validarCampo(txtNombre, newText != null && !newText.trim().isEmpty());
        });
        
        txtApellidoPaterno.textProperty().addListener((obs, oldText, newText) -> {
            validarCampo(txtApellidoPaterno, newText != null && !newText.trim().isEmpty());
        });
        
        txtTelefono.textProperty().addListener((obs, oldText, newText) -> {
            boolean valido = newText != null && newText.trim().length() >= 10;
            validarCampo(txtTelefono, valido);
        });
        
        txtRFC.textProperty().addListener((obs, oldText, newText) -> {
            boolean valido = newText != null && newText.trim().length() >= 10;
            validarCampo(txtRFC, valido);
        });
    }
    
    private void validarCampo(TextField campo, boolean esValido) {
        campo.getStyleClass().removeAll("error", "valid");
        
        if (!campo.getText().trim().isEmpty()) {
            if (esValido) {
                campo.getStyleClass().add("valid");
            } else {
                campo.getStyleClass().add("error");
            }
        }
    }
    
    private void cargarClienteSeleccionado() 
    {
        SesionTemporal sesion = SesionTemporal.getInstancia();
        if (sesion.hayClienteSeleccionado()) {
            for (Cliente cliente : listaClientes) {
                if (cliente.getId() == sesion.getClienteId()) {
                    tablaClientes.getSelectionModel().select(cliente);
                    tablaClientes.scrollTo(cliente);
                    break;
                }
            }
        }
    }

    private void cargarClientes() 
    {
        listaClientes.clear();
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT id, nombre, apellido_paterno, apellido_materno, rfc, telefono, correo FROM clientes ORDER BY nombre, apellido_paterno";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) 
            {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellidoPaterno = rs.getString("apellido_paterno");
                String apellidoMaterno = rs.getString("apellido_materno");
                String rfc = rs.getString("rfc");
                String telefono = rs.getString("telefono");
                String correo = rs.getString("correo");
                
                Cliente c = new Cliente(id, nombre, apellidoPaterno, apellidoMaterno, rfc, "", telefono, correo);
                listaClientes.add(c);
            }
            
            totalClientes = listaClientes.size();
            debug("✅ Se cargaron " + totalClientes + " clientes");
            actualizarContadorClientes();
            
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar clientes: " + e.getMessage());
            mostrarNotificacionSutil("Error al cargar clientes: " + e.getMessage(), false);
        }
        tablaClientes.setItems(listaClientes);
    }
    
    private void actualizarContadorClientes() {
        debug("📊 Total de clientes: " + totalClientes);
    }

    @FXML
    private void buscarClientes() 
        {
        String texto = txtBuscar.getText().trim();
        
        if (texto.isEmpty()) {
            cargarClientes();
            return;
        }
        listaClientes.clear();
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT id, nombre, apellido_paterno, apellido_materno, rfc, telefono, correo FROM clientes WHERE " +
                        "nombre LIKE ? OR " +
                        "apellido_paterno LIKE ? OR " +
                        "apellido_materno LIKE ? OR " +
                        "rfc LIKE ? OR " +
                        "telefono LIKE ? " +
                        "ORDER BY nombre, apellido_paterno";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            String busqueda = "%" + texto + "%";
            stmt.setString(1, busqueda);
            stmt.setString(2, busqueda);
            stmt.setString(3, busqueda);
            stmt.setString(4, busqueda);
            stmt.setString(5, busqueda);
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido_paterno"),
                    rs.getString("apellido_materno"),
                    rs.getString("rfc"),
                    "",
                    rs.getString("telefono"),
                    rs.getString("correo")
                );
                listaClientes.add(c);
            }
            
            debug("🔍 Búsqueda completada: " + listaClientes.size() + " resultados para '" + texto + "'");
            
        } catch (SQLException e) {
            System.err.println("❌ Error en búsqueda: " + e.getMessage());
            mostrarNotificacionSutil("Error en búsqueda: " + e.getMessage(), false);
        }
        tablaClientes.setItems(listaClientes);
    }

    @FXML
    private void seleccionarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            SesionTemporal sesion = SesionTemporal.getInstancia();
            sesion.setCliente(seleccionado);
            
            debug("✅ Cliente seleccionado: " + seleccionado.getNombreCompleto() + 
                             " - Teléfono: " + seleccionado.getTelefono());
            
            // Marcar visualmente el cliente seleccionado
            marcarClienteSeleccionado(seleccionado);
            
            // Mostrar notificación sutil en lugar de popup
            mostrarNotificacionSutil(
                "Cliente seleccionado: " + seleccionado.getNombreCompleto() + 
                " | Tel: " + seleccionado.getTelefono(), 
                true
            );
        } else {
            mostrarNotificacionSutil("Debes seleccionar un cliente de la tabla", false);
        }
    }
    
    private void marcarClienteSeleccionado(Cliente cliente) {
        // Agregar clase CSS para resaltar el cliente seleccionado
        tablaClientes.getSelectionModel().select(cliente);
        tablaClientes.scrollTo(cliente);
    }

    @FXML
    private void guardarCliente() {
        String nombre = txtNombre.getText().trim();
        String apellidoPaterno = txtApellidoPaterno.getText().trim();
        String apellidoMaterno = txtApellidoMaterno.getText().trim();
        String rfc = txtRFC.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();

        if (nombre.isEmpty()) 
        {
            mostrarNotificacionSutil("El nombre es obligatorio", false);
            txtNombre.requestFocus();
            return;
        }
        
        if (apellidoPaterno.isEmpty()) 
        {
            mostrarNotificacionSutil("El apellido paterno es obligatorio", false);
            txtApellidoPaterno.requestFocus();
            return;
        }
        
        if (rfc.isEmpty() || rfc.length() < 10) 
        {
            mostrarNotificacionSutil("El RFC o CURP debe tener al menos 10 caracteres", false);
            txtRFC.requestFocus();
            return;
        }
        
        if (telefono.isEmpty() || telefono.length() < 10) 
        {
            mostrarNotificacionSutil("El teléfono debe tener al menos 10 dígitos", false);
            txtTelefono.requestFocus();
            return;
        }

        try (Connection conn = Conexion.conectar()) 
        {
            String sql = "INSERT INTO clientes (nombre, apellido_paterno, apellido_materno, rfc, telefono, correo) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nombre);
            stmt.setString(2, apellidoPaterno);
            stmt.setString(3, apellidoMaterno.isEmpty() ? null : apellidoMaterno);
            stmt.setString(4, rfc);
            stmt.setString(5, telefono);
            stmt.setString(6, correo.isEmpty() ? null : correo);
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int clienteId = generatedKeys.getInt(1);
                    
                    Cliente nuevoCliente = new Cliente(clienteId, nombre, apellidoPaterno, apellidoMaterno, rfc, "", telefono, correo);
                    SesionTemporal.getInstancia().setCliente(nuevoCliente);
                    
                    debug("✅ Nuevo cliente registrado: " + nuevoCliente.getNombreCompleto() + 
                                     " - Teléfono: " + nuevoCliente.getTelefono());
                }
            }

            mostrarNotificacionSutil
            (
                "Cliente registrado correctamente: " + nombre + " " + apellidoPaterno + 
                " | Ya puedes continuar con el presupuesto", 
                true
            );
            
            limpiarFormulario();
            cargarClientes();
            
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar cliente: " + e.getMessage());
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                mostrarNotificacionSutil("Ya existe un cliente con ese RFC o datos similares", false);
            } else {
                mostrarNotificacionSutil("Error de base de datos: " + e.getMessage(), false);
            }
        }
    }
    
    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellidoPaterno.clear();
        txtApellidoMaterno.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtRFC.clear();
        txtNombre.getStyleClass().removeAll("error", "valid");
        txtApellidoPaterno.getStyleClass().removeAll("error", "valid");
        txtTelefono.getStyleClass().removeAll("error", "valid");
        txtRFC.getStyleClass().removeAll("error", "valid");
    }

    @FXML
    private void irAPaquetesPresupuesto() {
        try {
            SesionTemporal sesion = SesionTemporal.getInstancia();
            if (!sesion.hayClienteSeleccionado()) {
                mostrarNotificacionSutil("Primero debes seleccionar un cliente o registrar uno nuevo", false);
                return;
            }
            debug("✅ Navegando con cliente: " + sesion.getClienteNombreCompleto());
            App.changeView("PaquetesPresupuesto");
        } catch (Exception e) {
            System.err.println("❌ Error completo: " + e.getMessage());
            
            mostrarNotificacionSutil("No se pudo continuar al siguiente paso: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleRegresar() {
        try {
            App.changeView("Eventos");
        } catch (Exception e) {
            System.err.println("❌ Error al regresar: " + e.getMessage());
            mostrarNotificacionSutil("No se pudo regresar al menú de eventos", false);
        }
    }

    // ========== MÉTODOS PARA VER PDF ==========

    @FXML
    private void verPDF() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        
        if (clienteSeleccionado == null) {
            mostrarNotificacionSutil("Debes seleccionar un cliente de la tabla", false);
            return;
        }
        
        debug("🔍 Buscando PDFs para cliente: " + clienteSeleccionado.getNombreCompleto());
        mostrarNotificacionSutil("Buscando presupuestos del cliente...", true);
        
        // Buscar presupuestos del cliente en la base de datos
        java.util.List<PresupuestoInfo> presupuestos = buscarPresupuestosCliente(clienteSeleccionado.getId());
        
        if (presupuestos.isEmpty()) {
            mostrarNotificacionSutil("No se encontraron presupuestos para este cliente", false);
            return;
        }
        
        if (presupuestos.size() == 1) {
            // Solo un presupuesto, abrirlo directamente
            abrirPDF(presupuestos.get(0));
        } else {
            // Múltiples presupuestos, mostrar lista para seleccionar
            mostrarListaPresupuestos(presupuestos);
        }
    }

    // ========== CLASE AUXILIAR PARA INFORMACIÓN DE PRESUPUESTOS ==========
    public static class PresupuestoInfo {
        private int id;
        private String numeroPresupuesto;
        private String fechaCreacion;
        private double totalGeneral;
        private String nombreArchivo;
        private String estado;
        
        public PresupuestoInfo(int id, String numeroPresupuesto, String fechaCreacion, 
                              double totalGeneral, String nombreArchivo, String estado) {
            this.id = id;
            this.numeroPresupuesto = numeroPresupuesto;
            this.fechaCreacion = fechaCreacion;
            this.totalGeneral = totalGeneral;
            this.nombreArchivo = nombreArchivo;
            this.estado = estado;
        }
        
        // Getters
        public int getId() { return id; }
        public String getNumeroPresupuesto() { return numeroPresupuesto; }
        public String getFechaCreacion() { return fechaCreacion; }
        public double getTotalGeneral() { return totalGeneral; }
        public String getNombreArchivo() { return nombreArchivo; }
        public String getEstado() { return estado; }
        
        @Override
        public String toString() {
            return String.format("%s - $%.2f MXN (%s) - %s", 
                    numeroPresupuesto, totalGeneral, estado, fechaCreacion);
        }
    }

    // ========== BUSCAR PRESUPUESTOS DEL CLIENTE ==========
    private java.util.List<PresupuestoInfo> buscarPresupuestosCliente(int clienteId) {
        java.util.List<PresupuestoInfo> presupuestos = new java.util.ArrayList<>();
        
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT id, numero_presupuesto, fecha_creacion, total_general, " +
                        "nombre_archivo_pdf, estado FROM presupuestos " +
                        "WHERE cliente_id = ? " +
                        "ORDER BY fecha_creacion DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                PresupuestoInfo info = new PresupuestoInfo(
                    rs.getInt("id"),
                    rs.getString("numero_presupuesto"),
                    rs.getTimestamp("fecha_creacion").toString(),
                    rs.getDouble("total_general"),
                    rs.getString("nombre_archivo_pdf"),
                    rs.getString("estado")
                );
                presupuestos.add(info);
            }
            
            debug("✅ Encontrados " + presupuestos.size() + " presupuestos para cliente ID: " + clienteId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar presupuestos: " + e.getMessage());
            mostrarNotificacionSutil("Error al buscar presupuestos: " + e.getMessage(), false);
        }
        
        return presupuestos;
    }

    // ========== MOSTRAR LISTA DE PRESUPUESTOS PARA SELECCIONAR ==========
    private void mostrarListaPresupuestos(java.util.List<PresupuestoInfo> presupuestos) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Seleccionar Presupuesto");
        alert.setHeaderText("Este cliente tiene múltiples presupuestos:");
        
        // Crear ComboBox con los presupuestos
        ComboBox<PresupuestoInfo> comboPresupuestos = new ComboBox<>();
        comboPresupuestos.getItems().addAll(presupuestos);
        comboPresupuestos.getSelectionModel().selectFirst();
        comboPresupuestos.setPrefWidth(500);
        
        alert.getDialogPane().setContent(comboPresupuestos);
        
        // Configurar botones
        alert.getButtonTypes().setAll(
            new ButtonType("Ver PDF", ButtonBar.ButtonData.OK_DONE),
            new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        
        // Mostrar diálogo
        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                PresupuestoInfo seleccionado = comboPresupuestos.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    abrirPDF(seleccionado);
                }
            }
        });
    }

    // ========== ABRIR PDF (PRINCIPAL) ==========
    private void abrirPDF(PresupuestoInfo presupuesto) {
        debug("🔄 Intentando abrir PDF: " + presupuesto.getNombreArchivo());
        
        try {
            // Paso 1: Intentar abrir desde Desktop (donde se guardan los nuevos)
            boolean abiertoDesdeDesktop = intentarAbrirDesdeDesktop(presupuesto.getNombreArchivo());
            
            if (abiertoDesdeDesktop) {
                mostrarNotificacionSutil("PDF abierto desde Desktop", true);
                return;
            }
            
            // Paso 2: Extraer desde base de datos
            debug("🔄 PDF no encontrado en Desktop, extrayendo desde base de datos...");
            boolean extraidoDesdeBD = extraerPDFDesdeBD(presupuesto);
            
            if (extraidoDesdeBD) {
                mostrarNotificacionSutil("PDF extraído y abierto desde base de datos", true);
            } else {
                mostrarNotificacionSutil("No se pudo abrir el PDF", false);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al abrir PDF: " + e.getMessage());
            mostrarNotificacionSutil("Error al abrir PDF: " + e.getMessage(), false);
        }
    }

    // ========== INTENTAR ABRIR DESDE DESKTOP ==========
    private boolean intentarAbrirDesdeDesktop(String nombreArchivo) {
        try {
            // Buscar en Desktop
            String rutaDesktop = System.getProperty("user.home") + "/Desktop/Presupuestos";
            java.io.File carpetaDesktop = new java.io.File(rutaDesktop);
            
            if (!carpetaDesktop.exists()) {
                // Probar con separador de Windows
                rutaDesktop = System.getProperty("user.home") + "\\Desktop\\Presupuestos";
                carpetaDesktop = new java.io.File(rutaDesktop);
            }
            
            if (!carpetaDesktop.exists()) {
                debug("📁 Carpeta de presupuestos no existe en Desktop");
                return false;
            }
            
            // Buscar el archivo específico
            java.io.File archivoPDF = new java.io.File(carpetaDesktop, nombreArchivo);
            
            if (!archivoPDF.exists()) {
                // Buscar archivos similares (por si el nombre cambió ligeramente)
                java.io.File[] archivos = carpetaDesktop.listFiles((dir, name) -> 
                    name.toLowerCase().endsWith(".pdf") && 
                    name.toLowerCase().contains(nombreArchivo.toLowerCase().substring(0, 
                        Math.min(nombreArchivo.length(), 10)))
                );
                
                if (archivos != null && archivos.length > 0) {
                    archivoPDF = archivos[0]; // Tomar el primer archivo que coincida
                    debug("📄 Archivo encontrado con nombre similar: " + archivoPDF.getName());
                } else {
                    debug("📄 Archivo no encontrado en Desktop: " + nombreArchivo);
                    return false;
                }
            }
            
            // Abrir el archivo
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivoPDF);
                debug("✅ PDF abierto desde Desktop: " + archivoPDF.getAbsolutePath());
                return true;
            } else {
                debug("❌ Desktop no soportado en este sistema");
                return false;
            }
            
        } catch (Exception e) {
            debug("❌ Error al abrir desde Desktop: " + e.getMessage());
            return false;
        }
    }

    // ========== EXTRAER PDF DESDE BASE DE DATOS ==========
    private boolean extraerPDFDesdeBD(PresupuestoInfo presupuesto) {
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT archivo_pdf_contenido, nombre_archivo_pdf FROM presupuestos WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, presupuesto.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                byte[] pdfBytes = rs.getBytes("archivo_pdf_contenido");
                String nombreArchivo = rs.getString("nombre_archivo_pdf");
                
                if (pdfBytes != null && pdfBytes.length > 0) {
                    // Crear carpeta temporal si no existe
                    String rutaTemp = System.getProperty("java.io.tmpdir") + "/PresupuestosPDFs";
                    java.io.File carpetaTemp = new java.io.File(rutaTemp);
                    carpetaTemp.mkdirs();
                    
                    // Crear archivo temporal
                    java.io.File archivoTemp = new java.io.File(carpetaTemp, nombreArchivo);
                    
                    // Escribir bytes al archivo
                    java.nio.file.Files.write(archivoTemp.toPath(), pdfBytes);
                    
                    // Abrir el archivo
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(archivoTemp);
                        debug("✅ PDF extraído y abierto desde BD: " + archivoTemp.getAbsolutePath());
                        
                        // Opcional: Programar eliminación del archivo temporal después de un tiempo
                        programarLimpiezaArchivo(archivoTemp);
                        
                        return true;
                    } else {
                        debug("❌ Desktop no soportado");
                        return false;
                    }
                } else {
                    debug("❌ PDF no tiene contenido en la base de datos");
                    return false;
                }
            } else {
                debug("❌ No se encontró el presupuesto en la base de datos");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al extraer PDF desde BD: " + e.getMessage());
            return false;
        }
    }

    // ========== PROGRAMAR LIMPIEZA DE ARCHIVOS TEMPORALES ==========
    private void programarLimpiezaArchivo(java.io.File archivo) {
        // Crear un timer para eliminar el archivo temporal después de 5 minutos
        java.util.Timer timer = new java.util.Timer(true);
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    if (archivo.exists()) {
                        archivo.delete();
                        debug("🗑️ Archivo temporal eliminado: " + archivo.getName());
                    }
                } catch (Exception e) {
                    debug("⚠️ No se pudo eliminar archivo temporal: " + e.getMessage());
                }
            }
        }, 5 * 60 * 1000); // 5 minutos
    }

    // ========== MÉTODO AUXILIAR PARA LIMPIAR ARCHIVOS TEMPORALES ANTIGUOS ==========
    public void limpiarArchivosTemporales() {
        try {
            String rutaTemp = System.getProperty("java.io.tmpdir") + "/PresupuestosPDFs";
            java.io.File carpetaTemp = new java.io.File(rutaTemp);
            
            if (carpetaTemp.exists()) {
                java.io.File[] archivos = carpetaTemp.listFiles();
                if (archivos != null) {
                    long ahora = System.currentTimeMillis();
                    int eliminados = 0;
                    
                    for (java.io.File archivo : archivos) {
                        // Eliminar archivos más antiguos de 1 hora
                        if (ahora - archivo.lastModified() > 3600000) {
                            if (archivo.delete()) {
                                eliminados++;
                            }
                        }
                    }
                    
                    if (eliminados > 0) {
                        debug("🗑️ Eliminados " + eliminados + " archivos temporales antiguos");
                    }
                }
            }
        } catch (Exception e) {
            debug("⚠️ Error al limpiar archivos temporales: " + e.getMessage());
        }
    }
}