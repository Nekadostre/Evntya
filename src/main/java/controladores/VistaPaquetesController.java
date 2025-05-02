package controladores;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class VistaPaquetesController implements Initializable {

    @FXML
    private Button btnRegresar;

    @FXML
    private Button btnSelecPaq1;

    @FXML
    private Button btnSelecPaq2;

    @FXML
    private ListView<String> ListaExtras;

    @FXML
    private ListView<String> ListaPaquete1;

    @FXML
    private ListView<String> ListaPaquete2;

    @FXML
    private Label lblExtras;

    @FXML
    private Label lblPaq1;

    @FXML
    private Label lblPaq2;

    @FXML
    private Label lblExtras11;

    @FXML
    private TextField txtPaq;

    @FXML
    private TextField txtExtras;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Llenar listas con datos predefinidos
        ListaExtras.getItems().addAll(
            "Decoración adicional",
            "Animador de globos",
            "Máquina de palomitas",
            "Show Infantil",
            "Trampolín"
        );

        ListaPaquete1.getItems().addAll(
            "Juegos: Alberca de pelotas, Trampolines, Cancha de Futbol",
            "Servicios: 5 horas de servicio, Mesa para pastel",
            "Precio: $4,500"
        );

        ListaPaquete2.getItems().addAll(
            "Juegos: Maquinita multijuegos, Juegos de mesa, Alberca de pelotas",
            "Servicios: Wi-Fi, Recepción con personaje",
            "Precio: $8,900"
        );

        System.out.println("VistaPaquetesController inicializado.");
    }

    @FXML
    private void handleRegresarButtonAction() {
        System.out.println("Botón Regresar presionado.");
        lblExtras.setText("Se presionó el botón Regresar.");
    }

    @FXML
    private void seleccionarPaquete1() {
        txtPaq.setText("Paquete 1");
        System.out.println("Paquete 1 seleccionado.");
    }

    @FXML
    private void seleccionarPaquete2() {
        txtPaq.setText("Paquete 2");
        System.out.println("Paquete 2 seleccionado.");
    }
}
