package controlador;

import javafx.event.ActionEvent;
import controlador.FacturaGenerator;

public class FacturaViewController {

    public void handleGenerarFactura(ActionEvent event) {
        FacturaGenerator.generarFactura("Eduardo Pute", "Paquete Básico", "Transferencia", 1500.00);
    }
}
