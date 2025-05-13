package controladores;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import modelos.Cliente;
import modelos.ClienteTemporal;
import database.Conexion;
import java.io.IOException;
import javafx.scene.input.KeyCode;


import java.sql.*;

public class ClientePresupuestoController {

    @FXML private TextField txtNombre, txtApellidos, txtCorreo, txtTelefono, txtRFC;
    @FXML private Label lblMensaje;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colNombre, colApellido, colRFC;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colApellido.setCellValueFactory(data -> data.getValue().apellidoProperty());
        colRFC.setCellValueFactory(data -> data.getValue().rfcProperty());
        cargarClientes();

        FilteredList<Cliente> filtro = new FilteredList<>(listaClientes, p -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            filtro.setPredicate(cliente -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filtroMin = newVal.toLowerCase();
                return cliente.getNombre().toLowerCase().contains(filtroMin) ||
                       cliente.getApellido().toLowerCase().contains(filtroMin) ||
                       cliente.getRfc().toLowerCase().contains(filtroMin);
            });
        });

        SortedList<Cliente> ordenado = new SortedList<>(filtro);
        ordenado.comparatorProperty().bind(tablaClientes.comparatorProperty());
        tablaClientes.setItems(ordenado);
        
        txtBuscar.setOnKeyPressed(event -> {
        switch (event.getCode()) {
            case ENTER:
                ObservableList<Cliente> resultados = tablaClientes.getItems();
                if (resultados.size() == 1) {
                    tablaClientes.getSelectionModel().select(0);
                    seleccionarCliente();
                }
                break;
        }
});


    }

    @FXML
    private void guardarCliente() {
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String correo = txtCorreo.getText();
        String telefono = txtTelefono.getText();
        String rfc = txtRFC.getText().toUpperCase();

        if (nombre.isEmpty() || apellidos.isEmpty() || telefono.isEmpty() || rfc.isEmpty()) {
            mostrarAlerta("Nombre, apellidos, teléfono y RFC son obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        if (!correo.isEmpty() && !correo.contains("@")) {
            mostrarAlerta("El correo debe contener '@' y ser válido.", Alert.AlertType.ERROR);
            return;
        }

        if (!validarRFC(rfc)) {
            mostrarAlerta("El RFC o CURP no tiene un formato válido.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = Conexion.conectar()) {
            String sql = "INSERT INTO clientes (nombre, apellidos, correo, telefono, rfc) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, correo.isEmpty() ? null : correo);
            stmt.setString(4, telefono);
            stmt.setString(5, rfc);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    ClienteTemporal.getInstancia().setDatos(id, nombre, apellidos, rfc);
                    mostrarAlerta("Cliente registrado y seleccionado.", Alert.AlertType.INFORMATION);
                }
                limpiarCampos();
                cargarClientes();
            }
        } catch (SQLException e) {
    mostrarAlerta("No se pudo registrar el cliente: " + e.getMessage(), Alert.AlertType.ERROR);
}

    }

    @FXML
private void seleccionarCliente() {
    Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
    if (seleccionado != null) {
        ClienteTemporal.getInstancia().setDatos(
            seleccionado.getId(), seleccionado.getNombre(), seleccionado.getApellido(), seleccionado.getRfc()
        );

        try {
            App.setRoot("Presupuesto");
        } catch (IOException e) {
            mostrarAlerta("Error al cargar la vista de paquetes.", Alert.AlertType.ERROR);
        }

    } else {
        mostrarAlerta("Seleccione un cliente de la tabla.", Alert.AlertType.WARNING);
    }
}

@FXML
private void handleRegresar() {
    try {
        App.setRoot("Eventos");
    } catch (IOException e) {
        mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error", "No se pudo volver a la vista anterior.");
    }
}



    private void cargarClientes() {
    listaClientes.clear();
    try (Connection conn = Conexion.conectar();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clientes ORDER BY nombre ASC")) {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            listaClientes.add(new Cliente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("rfc")
            ));
        }
    } catch (SQLException e) {
        mostrarAlerta("Error al cargar clientes.", Alert.AlertType.ERROR);
    }
}


    private boolean validarRFC(String rfc) {
        String regexRFC = "^([A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{0,3})$";
        String regexCURP = "^[A-Z][AEIOU][A-Z]{2}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$";
        return rfc.matches(regexRFC) || rfc.matches(regexCURP);
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtRFC.clear();
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Aviso del sistema");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAlertaPersonalizada(Alert.AlertType alertType, String error, String no_se_pudo_volver_a_la_vista_anterior) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
