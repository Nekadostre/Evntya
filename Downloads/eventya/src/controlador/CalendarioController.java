package controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author rye
 */
public class CalendarioController implements Initializable {

    @FXML
    private Label lblMesAnio;
    @FXML
    private Button btnNext;
    @FXML
    private GridPane gridCalendario;
    @FXML
    private ListView<?> listaEventos;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void mesSiguiente(ActionEvent event) {
    }
    
}
