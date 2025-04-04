package sgref.controllers;

import javafx.event.ActionEvent;
import sgref.integraciones.FacturaGenerator;

public class FacturaController {

    public void handleGenerarFactura(ActionEvent event) {
        FacturaGenerator.generarFactura("Cliente Demo", "Paquete Básico", "Transferencia", 1500.00);
    }
}
