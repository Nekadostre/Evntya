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

public class VistaPreviaContratoController {

    @FXML private Label lblFechaContrato;
    @FXML private Label lblNombreCliente;
    @FXML private TextField txtNombreFestejado;
    @FXML private Label lblFechaEvento;
    @FXML private RadioButton rbMatutino;
    @FXML private RadioButton rbVespertino;
    @FXML private ComboBox<String> cbFormaPago;
    @FXML private ComboBox<Integer> cbPlazos;
    @FXML private Label lblPaquete;
    @FXML private Label lblExtras;
    @FXML private Label lblTotal;

    private ToggleGroup horarioGroup;

    @FXML
    public void initialize() {
        horarioGroup = new ToggleGroup();
        rbMatutino.setToggleGroup(horarioGroup);
        rbVespertino.setToggleGroup(horarioGroup);

        cbFormaPago.getItems().addAll("Efectivo", "Transferencia", "Tarjeta", "Efectivo + Tarjeta", "Efectivo + Transferencia");
        cbPlazos.getItems().addAll(1, 2, 3);

        // Cargar datos simulados desde base de datos (deberías reemplazar esto con consulta real)
        lblFechaContrato.setText("Fecha de contrato: " + LocalDate.now());
        lblNombreCliente.setText("Nombre del cliente: Juan Pérez");
        txtNombreFestejado.setText("Sofía");
        lblFechaEvento.setText("Fecha del evento: 10/06/2025");
        rbVespertino.setSelected(true);
        cbFormaPago.getSelectionModel().select("Transferencia");
        cbPlazos.getSelectionModel().select(Integer.valueOf(2));
        lblPaquete.setText("Paquete seleccionado: Fiesta Premium");
        lblExtras.setText("Extras: Show, Trampolín");
        lblTotal.setText("Total: $5,800.00 MXN");
    }

    @FXML
    private void handleImprimir() {
        generarYGuardarPDF("contrato_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Contrato generado e impreso correctamente.", AlertType.INFORMATION);
    }

    @FXML
    private void handleEnviarCorreo() {
        generarYGuardarPDF("contrato_" + LocalDate.now() + ".pdf");
        mostrarAlerta("Contrato enviado por correo al cliente.", AlertType.INFORMATION);
    }

    @FXML
    private void handleSalir() throws Exception {
        // Limpiar campos modificables
        txtNombreFestejado.clear();
        horarioGroup.selectToggle(null);
        cbFormaPago.getSelectionModel().clearSelection();
        cbPlazos.getSelectionModel().clearSelection();

        // Regresar al panel principal
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
                    fos.write(("PDF de contrato generado el " + LocalDate.now()).getBytes());
                }
            }
        } catch (IOException e) {
            mostrarAlerta("Error al generar el PDF: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String mensaje, AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
