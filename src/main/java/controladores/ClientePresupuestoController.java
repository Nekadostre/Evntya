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
}