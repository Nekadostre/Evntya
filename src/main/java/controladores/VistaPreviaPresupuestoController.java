package controladores;

import dao.PresupuestoDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelos.ClienteTemporal;
import modelos.PaqueteTemporal;
import modelos.ClienteTemporal;
import modelos.PaqueteTemporal;
import modelos.PresupuestoTemporal;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import modelos.PresupuestoTemporal;






/**
 * FXML Controller class
 *
 * @author pollix
 */

public class VistaPreviaPresupuestoController implements Initializable {

    @FXML
    private Label lblNombre;
    @FXML
    private Label lblHorario;
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

@Override
public void initialize(URL url, ResourceBundle rb) {
    ClienteTemporal cliente = ClienteTemporal.getInstancia();
    PaqueteTemporal paquete = PaqueteTemporal.getInstancia();
    System.out.println("Paquete cargado: " + paquete.getNombre());
System.out.println("Extras cargados: " + paquete.getExtras());


    lblNombre.setText(cliente.getNombre() + " " + cliente.getApellido());
    lblPaquete.setText(paquete.getNombre());
    lblExtras.setText(paquete.getExtras());
    lblTotal.setText(String.format("%.2f", paquete.getTotal()));
    lblHorario.setText("-");
    lblPlazos.setText("-");
    lblPago.setText("-");
} 

    @FXML
    private void handleReturn(ActionEvent event) throws IOException {
        App.setRoot("PaquetesPresupuesto"); 
    }

  @FXML
private void handleImprimir(ActionEvent event) throws SQLException {
    try {
        PresupuestoDAO.guardarPresupuesto();
        generarPresupuestoDesdeVista();  
    } catch (IOException e) {
        mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}



    @FXML
    private void handleEnviarCorreo(ActionEvent event) {
        generarYGuardarPDF("presupuesto_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Presupuesto enviado por correo al cliente.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSalir(ActionEvent event) throws IOException {
        App.setRoot("PanelPrincipal"); 
    }
    
    private void generarPresupuestoDesdeVista() throws IOException {
        File plantilla = new File("src/main/resources/pdf/presupuesto_template.pdf");
        File output;
        try (PDDocument doc = PDDocument.load(plantilla)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            ClienteTemporal cliente = ClienteTemporal.getInstancia();
            PaqueteTemporal paquete = PaqueteTemporal.getInstancia();
            PresupuestoTemporal presupuesto = PresupuestoTemporal.getInstancia();
            if (form != null) {
                form.getField("nombre").setValue(cliente.getNombre() + " " + cliente.getApellido());
                form.getField("horario").setValue(presupuesto.getHorario());
                form.getField("paquete").setValue(paquete.getNombre());
                form.getField("extras").setValue(paquete.getExtras());
                form.getField("plazos").setValue(presupuesto.getPlazos());
                form.getField("pago").setValue(presupuesto.getFormaPago());
                form.getField("total").setValue(String.format("$%.2f MXN", paquete.getTotal()));
                
                form.flatten(); 
            }   
            output = new File(System.getProperty("user.home") + "/Downloads/presupuesto_" + System.currentTimeMillis() + ".pdf");
            doc.save(output);
        }
        mostrarAlerta("Presupuesto guardado en: " + output.getAbsolutePath(), Alert.AlertType.INFORMATION);
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
    
    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
}
