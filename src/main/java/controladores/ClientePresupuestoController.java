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
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, String> colRfc;
    @FXML private TextField campoBuscar;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colRfc.setCellValueFactory(new PropertyValueFactory<>("rfc"));
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
                        rs.getString("apellidos"),
                        rs.getString("rfc"),
                        rs.getString("telefono"),
                        rs.getString("correo")
                );
                listaClientes.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tablaClientes.setItems(listaClientes);
    }

    @FXML
    private void buscarClientes() {
        String texto = campoBuscar.getText().trim();
        listaClientes.clear();

        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT * FROM clientes WHERE nombre LIKE ? OR apellidos LIKE ? OR rfc LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + texto + "%");
            stmt.setString(2, "%" + texto + "%");
            stmt.setString(3, "%" + texto + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente c = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("rfc"),
                        rs.getString("telefono"),
                        rs.getString("correo")
                );
                listaClientes.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tablaClientes.setItems(listaClientes);
    }

    @FXML
    private void seleccionarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            DatosContratoTemporal.cliente = seleccionado;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Cliente seleccionado correctamente");
            alert.setContentText("Nombre: " + seleccionado.getNombreCompleto());
            alert.showAndWait();
        }
    }
}  
