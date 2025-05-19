package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import java.io.IOException;

public class EventosController {

    @FXML
    private Button btnPresupuesto;

    @FXML
    private Button BtnContrato;

    @FXML
    private Text lbEvento;

    @FXML
    private void buttonPresupuesto() throws IOException 
    {
        App.setRoot("ClientePresupuestoView");
    }

    @FXML
    private void buttonContrato() throws IOException 
    {
        App.setRoot("CalendarioContrato");
    }
    
    @FXML
    private void volverAlPanelPrincipal() throws IOException 
    {
    App.setRoot("PanelPrincipal");
    }

}
