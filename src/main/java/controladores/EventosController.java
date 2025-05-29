package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import java.io.IOException;

public class EventosController {
    private static final boolean DEBUG_MODE = false;
    
    private void debug(String mensaje) {
        if (DEBUG_MODE) {
            System.out.println(mensaje);
        }
    }

    @FXML
    private Button btnPresupuesto;

    @FXML
    private Button BtnContrato;

    @FXML
    private Text lbEvento;

    @FXML
    private void buttonPresupuesto() throws IOException {
        debug("🔄 Navegando a ClientePresupuestoView...");
        App.setRoot("ClientePresupuestoView");
    }

    @FXML
    private void buttonContrato() throws IOException {
        debug("🔄 Navegando a CalendarioContrato...");
        App.setRoot("CalendarioContrato");
    }
    
    @FXML
    private void volverAlPanelPrincipal() throws IOException {
        debug("🔄 Regresando al Panel Principal...");
        App.setRoot("PanelPrincipal");
    }
}