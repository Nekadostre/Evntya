package controlador;

import javafx.event.ActionEvent;
import controlador.FacturaGenerator;

public class FacturaController {

    public void handleGenerarFactura(ActionEvent event) {
        FacturaGenerator.generarFactura("Cliente Demo", "Paquete Básico", "Transferencia", 1500.00);
    }
}
