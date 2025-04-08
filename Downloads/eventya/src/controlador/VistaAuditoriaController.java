package controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.HistorialLoginDAO;
import modelo.HistorialLogin;

public class VistaAuditoriaController {

    @FXML private TableView<HistorialLogin> tablaHistorial;
    @FXML private TableColumn<HistorialLogin, String> colUsuario;
    @FXML private TableColumn<HistorialLogin, String> colFecha;

    @FXML
    public void initialize() {
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        ObservableList<HistorialLogin> datos = FXCollections.observableArrayList(
            HistorialLoginDAO.obtenerHistorial()
        );

        tablaHistorial.setItems(datos);
    }
}
