package modelo;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author pollix
 */
public class MensajesDAO {
    public static void mostrarMensaje(String titulo, String contenido, int tipo ){
        Alert alert;
        if (tipo == 1){
            alert = new Alert(AlertType.INFORMATION);
        } else {
            alert = new Alert(AlertType.ERROR);
        }
        
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
        
    }
}
