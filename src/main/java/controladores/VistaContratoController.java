package controladores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
/**
 * FXML Controller class
 *
 * @author pollix
 */
public class VistaContratoController implements Initializable {

    @FXML
    private Label lblPaquete;
    @FXML
    private Label lblExtras;
    @FXML
    private Label lblPlazos;
    @FXML
    private Label lblPago;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblHorario;
    @FXML
    private Label lblNombre;
    @FXML
    private TextField txtFestejado;
    @FXML
    private DatePicker fechaContratoPicker;
    @FXML
    private DatePicker fechaEventoPicker;
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    private void exit(ActionEvent event) throws IOException {
        String fechaevento = (fechaEventoPicker).getValue() !=null ? 
                fechaEventoPicker.getValue().toString() : "";
        String fechacontrato = (fechaContratoPicker).getValue() !=null ? 
                fechaContratoPicker.getValue().toString() : "";
        String festejado = txtFestejado.getText();
        
        //Codigo faltante...
        
        if (festejado.isEmpty() || fechacontrato.isEmpty() || fechaevento.isEmpty()) {
            mostrarAlerta("Los datos son obligatorios... " , Alert.AlertType.ERROR);
        } else {
            mostrarAlerta("El contrato se ha realizado correctamente.", Alert.AlertType.INFORMATION);
            App.setRoot("PanelPrincipal");
        }
    }

    @FXML
    private void regresar(ActionEvent event) throws IOException {
        App.setRoot("Contrato");
    }

    @FXML
    private void sendtomail(ActionEvent event) {
        generarYGuardarPDF("contrato_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Contrato enviado por correo al cliente.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void imprimir(ActionEvent event) {
        generarYGuardarPDF("contrato_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Contrato generado e impreso correctamente.", Alert.AlertType.INFORMATION);
    }
    
        private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
        
    private void generarYGuardarPDF(String nombreArchivo) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(nombreArchivo);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File archivo = fileChooser.showSaveDialog(new Stage());

            if (archivo != null) {
                try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    fos.write(("PDF de presupuesto generado el " + LocalDate.now()).getBytes());
                }
            }
        } catch (IOException e) {
            mostrarAlerta("Error al generar el PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

}
