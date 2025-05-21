package controladores;

import database.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modelos.Cliente;
import modelos.DatosContratoTemporal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientePresupuestoController {

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

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidoPaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        colApellidoMaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        colRFC.setCellValueFactory(new PropertyValueFactory<>("rfc"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        cargarClientes();
    }

    private void cargarClientes() {
        listaClientes.clear();
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT * FROM clientes";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido_paterno"),
                    rs.getString("apellido_materno"),
                    rs.getString("rfc"),
                    rs.getString("telefono")
                );

                listaClientes.add(c);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los clientes: " + e.getMessage());
        }
        tablaClientes.setItems(listaClientes);
    }

    @FXML
private void buscarClientes() {
    String texto = txtBuscar.getText().trim();
    listaClientes.clear();

    try (Connection conn = Conexion.conectar()) {
        String sql = "SELECT * FROM clientes WHERE nombre LIKE ? OR apellido_paterno LIKE ? OR apellido_materno LIKE ? OR rfc LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + texto + "%");
        stmt.setString(2, "%" + texto + "%");
        stmt.setString(3, "%" + texto + "%");
        stmt.setString(4, "%" + texto + "%");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Cliente c = new Cliente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("rfc"),
                rs.getString("telefono")
            );
            listaClientes.add(c);
        }
    } catch (SQLException e) {
        mostrarAlerta("Error", "No se pudo buscar: " + e.getMessage());
    }
    tablaClientes.setItems(listaClientes);
}


    @FXML
    private void seleccionarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            DatosContratoTemporal.cliente = seleccionado;
            mostrarAlerta("Cliente seleccionado", "Nombre: " + seleccionado.getNombreCompleto());
        } else {
            mostrarAlerta("Atención", "Debes seleccionar un cliente de la tabla.");
        }
    }

    @FXML
    private void guardarCliente() {
        String nombre = txtNombre.getText().trim();
        String apellidoPaterno = txtApellidoPaterno.getText().trim();
        String apellidoMaterno = txtApellidoMaterno.getText().trim(); // opcional
        String rfc = txtRFC.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();

        if (nombre.isEmpty() || apellidoPaterno.isEmpty() || rfc.isEmpty()) {
            mostrarAlerta("Campos obligatorios", "Nombre, apellido paterno y RFC son obligatorios.");
            return;
        }

        try (Connection conn = Conexion.conectar()) {
            String sql = "INSERT INTO clientes (nombre, apellido_paterno, apellido_materno, rfc, telefono, correo) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, apellidoPaterno);
            stmt.setString(3, apellidoMaterno.isEmpty() ? null : apellidoMaterno);
            stmt.setString(4, rfc);
            stmt.setString(5, telefono);
            stmt.setString(6, correo.isEmpty() ? null : correo);

            stmt.executeUpdate();

            mostrarAlerta("Éxito", "Cliente registrado correctamente.");
            cargarClientes();
        } catch (SQLException e) {
            mostrarAlerta("Error de base de datos", e.getMessage());
        }
    }


    @FXML
    private void irAPaquetesPresupuesto() {
        try {
            if (DatosContratoTemporal.cliente == null) {
                mostrarAlerta("Error", "Primero selecciona un cliente.");
                return;
            }
            App.setRoot("PaquetesPresupuesto");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cambiar a la vista de paquetes.");
        }
    }

    @FXML
    private void handleRegresar() {
        try {
            App.setRoot("Eventos");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo regresar al menú de eventos.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
