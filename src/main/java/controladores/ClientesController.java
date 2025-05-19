package controladores;

import database.Conexion;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelos.ClienteConPresupuesto;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientesController {
        
    @FXML
    private TableView<ClienteConPresupuesto> tablaClientes;

    @FXML
    private TableColumn<ClienteConPresupuesto, String> colNombre;

    @FXML
    private TableColumn<ClienteConPresupuesto, String> colApellidos;

    @FXML
    private TableColumn<ClienteConPresupuesto, String> colFecha;

    @FXML
    private TableColumn<ClienteConPresupuesto, String> colPaquete;

    @FXML
    private TableColumn<ClienteConPresupuesto, Double> colMonto;

    @FXML
    private TableColumn<ClienteConPresupuesto, Button> colAcciones;

    private final ObservableList<ClienteConPresupuesto> lista = FXCollections.observableArrayList();

    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colPaquete.setCellValueFactory(new PropertyValueFactory<>("paquete"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colAcciones.setCellValueFactory(new PropertyValueFactory<>("botonEliminar"));

        cargarDatos();
    }

    private void cargarDatos() {
        lista.clear();
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT c.id AS cliente_id, c.nombres, c.apellido_paterno, c.apellido_materno, " +
                         "p.id AS presupuesto_id, p.fecha, p.monto, pk.nombre AS paquete " +
                         "FROM clientes c " +
                         "JOIN presupuestos p ON c.id = p.cliente_id " +
                         "JOIN paquetes pk ON p.paquete_id = pk.id " +
                         "ORDER BY p.fecha DESC LIMIT 20";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int clienteId = rs.getInt("cliente_id");
                int presupuestoId = rs.getInt("presupuesto_id");
                String nombre = rs.getString("nombres");
                String apellidos = rs.getString("apellido_paterno") + " " + rs.getString("apellido_materno");
                String fecha = rs.getString("fecha");
                String paquete = rs.getString("paquete");
                double monto = rs.getDouble("monto");

                Button botonEliminar = new Button("Eliminar");
                botonEliminar.setOnAction((ActionEvent e) -> eliminarRegistro(clienteId, presupuestoId));

                lista.add(new ClienteConPresupuesto(nombre, apellidos, fecha, paquete, monto, botonEliminar));
            }

            tablaClientes.setItems(lista);

        } catch (Exception e) {
        }
    }
  
    private void eliminarRegistro(int clienteId, int presupuestoId) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText("¿Deseas eliminar este cliente y su presupuesto?");
        alerta.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK")) {
                try (Connection conn = Conexion.conectar()) {
                    PreparedStatement ps1 = conn.prepareStatement("DELETE FROM presupuestos WHERE id = ?");
                    ps1.setInt(1, presupuestoId);
                    ps1.executeUpdate();

                    PreparedStatement ps2 = conn.prepareStatement("DELETE FROM clientes WHERE id = ?");
                    ps2.setInt(1, clienteId);
                    ps2.executeUpdate();

                    cargarDatos();
                } catch (Exception e) {
                }
            }
        });
    }
    
    @FXML private void accionRegresar() throws IOException {
        App.setRoot("PanelPrincipal");
    }
} 
