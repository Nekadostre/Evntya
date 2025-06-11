package controladores;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import utils.ResponsiveHelper;

public class EjemploControlador implements Initializable {
    
    @FXML private VBox contenedorPrincipal;
    @FXML private HBox botonesContainer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hacer componentes responsivos
        ResponsiveHelper.makeResponsive(contenedorPrincipal);
        ResponsiveHelper.makeResponsive(botonesContainer);
        
        // Ajustar layout según dispositivo
        if (ResponsiveHelper.shouldUseCompactMode()) {
            setupCompactLayout();
        } else {
            setupFullLayout();
        }
    }
    
    private void setupCompactLayout() {
        // Layout para móvil/tablet
        contenedorPrincipal.setSpacing(10);
        botonesContainer.setSpacing(5);
    }
    
    private void setupFullLayout() {
        // Layout para desktop
        contenedorPrincipal.setSpacing(20);
        botonesContainer.setSpacing(15);
    }
}