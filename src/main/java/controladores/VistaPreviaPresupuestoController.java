package controladores;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class VistaPreviaPresupuestoController 
{

    @FXML private Label lblNombreCliente;
    @FXML private Label lblPaquete;
    @FXML private Label lblExtras;
    @FXML private Label lblTotal;
    @FXML private RadioButton rbMatutino;
    @FXML private RadioButton rbVespertino;
    @FXML private ComboBox<Integer> cbPlazos;
    @FXML private ComboBox<String> cbMetodoPago;

    private ToggleGroup horarioGroup;

    @FXML
    public void initialize() {
      horarioGroup = new ToggleGroup();
      rbMatutino.setToggleGroup(horarioGroup);
      rbVespertino.setToggleGroup(horarioGroup);
      cbPlazos.getItems().addAll(1, 2, 3);
      cbMetodoPago.getItems().addAll("Efectivo", "Transferencia", "Tarjeta", 
                                       "Efectivo + Tarjeta", "Efectivo + Transferencia");
      lblNombreCliente.setText("Juan Pérez");
      lblPaquete.setText("Paquete seleccionado: Fiesta Básica");
      lblExtras.setText("Extras: Show infantil, Trampolín");
      lblTotal.setText("Total: $4,500.00 MXN");
    }

    @FXML
    private void handleImprimir() 
    {
        generarYGuardarPDF("presupuesto_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Presupuesto generado e impreso correctamente.", AlertType.INFORMATION);
    }

    @FXML
    private void handleEnviarCorreo() 
    {
        generarYGuardarPDF("presupuesto_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Presupuesto enviado por correo al cliente.", AlertType.INFORMATION);
    }

   @FXML
    private void handleSalir() throws IOException 
    {
    lblNombreCliente.setText("");
    lblPaquete.setText("");
    lblExtras.setText("");
    lblTotal.setText("Total: $0.00 MXN");
    cbPlazos.getSelectionModel().clearSelection();
    cbMetodoPago.getSelectionModel().clearSelection();
    if (rbMatutino.isSelected() || rbVespertino.isSelected()) {
        rbMatutino.setSelected(false);
        rbVespertino.setSelected(false);
    }
    App.setRoot("MenuPrincipal");
    }


    private void generarYGuardarPDF(String nombreArchivo) 
    {
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
            mostrarAlerta("Error al generar el PDF: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String mensaje, AlertType tipo) 
    {
        Alert alerta = new Alert(tipo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
