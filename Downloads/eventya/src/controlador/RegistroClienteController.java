/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author rye
 */
public class RegistroClienteController implements Initializable {

    @FXML
    private VBox cont_RegClientes;
    @FXML
    private Label lb_RegClientes;
    @FXML
    private VBox cont_form1;
    @FXML
    private Label lb_Nombre;
    @FXML
    private TextField txt_Nombre;
    @FXML
    private Label lb_Apellido_paterno;
    @FXML
    private TextField txt_AP;
    @FXML
    private Label lb_AM;
    @FXML
    private TextField txt_AM;
    @FXML
    private Label lb_NumTEL;
    @FXML
    private TextField txt_NumTEL;
    @FXML
    private Label lb_RFC;
    @FXML
    private TextField txt_RFC;
    @FXML
    private Button btn_Rgistar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
