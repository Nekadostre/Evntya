package controladores;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelos.DatosContratoTemporal;
import modelos.PaqueteTemporal;

/**
 * FXML Controller class
 *
 * @author pollix
 */

public class VistaPreviaPresupuestoController implements Initializable {
    @FXML private Label lblNombre;
    @FXML private Label lblPaquete;
    @FXML private Label lblExtras;
    @FXML private ComboBox<String> comboHorario;
    @FXML private ComboBox<String> comboPlazos;
    @FXML private ComboBox<String> comboMetodoPago;

    @FXML
    public void initialize() {
        if (modelos.DatosContratoTemporal.cliente != null) {
            lblNombre.setText(modelos.DatosContratoTemporal.cliente.getNombreCompleto());
        } else {
            lblNombre.setText("-");
        }

        lblPaquete.setText(modelos.PaqueteTemporal.getInstancia().getNombre());

        String extras = modelos.PaqueteTemporal.getInstancia().getResumenExtras();
        lblExtras.setText(extras.isEmpty() ? "Sin extras seleccionados" : extras);

        comboHorario.getItems().addAll("Matutino", "Vespertino");
        comboHorario.getSelectionModel().selectFirst();

        comboPlazos.getItems().addAll("1 pago", "2 pagos", "3 pagos");
        comboPlazos.getSelectionModel().selectFirst();

        comboMetodoPago.getItems().addAll(
            "Efectivo",
            "Transferencia",
            "Tarjeta",
            "Efectivo + Transferencia",
            "Efectivo + Tarjeta"
        );
        comboMetodoPago.getSelectionModel().selectFirst();
    }


    @FXML private Label lblNombreCliente;
    @FXML private Label lblHorario;
    @FXML private Label lblPlazos;
    @FXML private Label lblPago;
    @FXML private Label lblTotal;
    @FXML private RadioButton mautinoRadio;
    @FXML private RadioButton vespertinoRadio;
    @FXML private MenuButton plazosItem;
    @FXML private MenuButton metodoPagoItem;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ToggleGroup group = new ToggleGroup();
        mautinoRadio.setToggleGroup(group);
        vespertinoRadio.setToggleGroup(group);

        for (MenuItem item : plazosItem.getItems()) {
            item.setOnAction(event -> plazosItem.setText(item.getText()));
        }

        for (MenuItem item : metodoPagoItem.getItems()) {
            item.setOnAction(event -> metodoPagoItem.setText(item.getText()));
        }

        // 👇 Cargar datos aquí
        cargarDatosDesdeTemporales();
    }
 
    
    /**
     * Initializes the controller class.
     * @param nombre
     * @param apellido
     * @param Horario
     * @param Paquete
     * @param Extras
     * @param Plazos
     * @param Pago
     * @param Total
     * @throws java.sql.SQLException
     */
    
    private void cargarDatosDesdeTemporales() {
    try {
        // Cargar cliente
        var cliente = DatosContratoTemporal.cliente;
        lblNombreCliente.setText(DatosContratoTemporal.cliente.getNombreCompleto());
        lblPaquete.setText(PaqueteTemporal.getInstancia().getNombrePaquete());
        lblExtras.setText(PaqueteTemporal.getInstancia().getResumenExtras());


        // Cargar paquete y extras
        var paquete = PaqueteTemporal.getInstancia();
        lblPaquete.setText(paquete.getNombrePaquete());
        lblExtras.setText(paquete.getExtras());
        lblTotal.setText(String.format("$%.2f", paquete.getTotal()));

        // Valores por defecto
        lblHorario.setText("No seleccionado");
        lblPlazos.setText("No seleccionado");
        lblPago.setText("No seleccionado");

    } catch (Exception e) {
        mostrarAlerta("Error al cargar datos: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}

   
    public void mostrarDatos(String nombre, String apellido, String Horario, String Paquete, String Extras, String Plazos, String Pago, String Total ) throws SQLException {
        lblNombre.setText(nombre);
        lblHorario.setText(Horario);
        lblPaquete.setText(Paquete);
        lblExtras.setText(Extras);
        lblPlazos.setText(Plazos);
        lblPago.setText(Pago);
        lblTotal.setText(Total);
    }    

    @FXML
    private void handleReturn(ActionEvent event) throws IOException {
        App.setRoot("Presupuesto"); 
    }

    @FXML
    private void handleImprimir(ActionEvent event) {
        generarYGuardarPDF("presupuesto_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Presupuesto generado e impreso correctamente.", Alert.AlertType.INFORMATION);
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

    @FXML
    private void radioMat(ActionEvent event) {
        if (mautinoRadio.isSelected()){
            vespertinoRadio.setSelected(false);
        }
           
    }

    @FXML
    private void radioVesp(ActionEvent event) {
            if (vespertinoRadio.isSelected()) {
        mautinoRadio.setSelected(false);
    }
    }

    
}
