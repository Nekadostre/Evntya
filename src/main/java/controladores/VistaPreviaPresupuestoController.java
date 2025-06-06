package controladores;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Properties;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import java.util.List;                   
import java.util.ArrayList;
import modelos.SesionTemporal;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Optional;
import java.awt.Desktop;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;     
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.collections.FXCollections; 
import javafx.collections.ObservableList;
import modelos.Extra;
import database.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;  
import java.sql.Statement;
import java.sql.SQLException;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class VistaPreviaPresupuestoController implements Initializable {
    
    // Variables FXML
    @FXML private Label lblNombre;
    @FXML private Label lblPaquete;
    @FXML private TextArea txtExtras;
    @FXML private Label lblTotal;
    @FXML private RadioButton mautinoRadio;
    @FXML private RadioButton vespertinoRadio;
    @FXML private MenuButton plazosItem;
    @FXML private MenuButton metodoPagoItem;
    @FXML private ListView<String> listaExtras;
    @FXML private Label lblSinExtras;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("VistaPreviaController: INICIANDO INICIALIZACIÓN ");
        
        try {
            // Configurar radio buttons
            System.out.println("VistaPreviaController: PASO 1 - Configurando radio buttons...");
            
            if (mautinoRadio != null && vespertinoRadio != null) {
                ToggleGroup group = new ToggleGroup();
                mautinoRadio.setToggleGroup(group);
                vespertinoRadio.setToggleGroup(group);
                mautinoRadio.setSelected(true);
                System.out.println("VistaPreviaController: ✅ Radio buttons configurados");
            }

            // Configurar MenuButtons
            System.out.println("VistaPreviaController: PASO 2 - Configurando MenuButtons...");
            
            if (plazosItem != null && plazosItem.getItems() != null) {
                for (MenuItem item : plazosItem.getItems()) {
                    final String texto = item.getText();
                    item.setOnAction(e -> plazosItem.setText(texto));
                }
                System.out.println("VistaPreviaController: ✅ Plazos configurado");
            }

            if (metodoPagoItem != null && metodoPagoItem.getItems() != null) {
                for (MenuItem item : metodoPagoItem.getItems()) {
                    final String texto = item.getText();
                    item.setOnAction(e -> metodoPagoItem.setText(texto));
                }
                System.out.println("VistaPreviaController: ✅ Método de pago configurado");
            }

            // Configurar ListView de extras
            System.out.println("VistaPreviaController: PASO 3 - Configurando ListView de extras...");
            configurarListaExtras();

            // Cargar datos
            System.out.println("VistaPreviaController: PASO 4 - Cargando datos...");
            cargarDatos();
            
            System.out.println("VistaPreviaController:  INICIALIZACIÓN COMPLETADA ");
            
        } catch (Exception e) {
            System.err.println("VistaPreviaController: ❌ ERROR EN INICIALIZACIÓN: " + e.getMessage());
            configurarValoresPorDefecto();
        }
    }
    
    // ========== MÉTODO DE DEBUG: VERIFICAR PRESUPUESTOS EN BD ==========
private void debugVerificarPresupuestosEnBD() {
    try (Connection conn = Conexion.conectar()) {
        System.out.println("🔍 === DEBUG: VERIFICANDO PRESUPUESTOS EN BASE DE DATOS ===");
        
        String sql = """
            SELECT 
                p.id,
                p.cliente_id,
                p.cliente_nombre,
                p.numero_presupuesto,
                p.fecha_creacion,
                p.total_general,
                p.nombre_archivo_pdf,
                p.ruta_archivo_pdf,
                CASE 
                    WHEN p.archivo_pdf_contenido IS NOT NULL THEN 'SÍ'
                    ELSE 'NO'
                END as tiene_pdf_blob,
                LENGTH(p.archivo_pdf_contenido) as tamaño_pdf
            FROM presupuestos p
            WHERE p.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 1 DAY)
            ORDER BY p.fecha_creacion DESC
            LIMIT 10
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        int contador = 0;
        while (rs.next()) {
            contador++;
            System.out.println("📋 PRESUPUESTO #" + contador);
            System.out.println("   ID: " + rs.getInt("id"));
            System.out.println("   Cliente ID: " + rs.getInt("cliente_id"));
            System.out.println("   Cliente: " + rs.getString("cliente_nombre"));
            System.out.println("   Número: " + rs.getString("numero_presupuesto"));
            System.out.println("   Fecha: " + rs.getTimestamp("fecha_creacion"));
            System.out.println("   Total: $" + rs.getDouble("total_general"));
            System.out.println("   Nombre PDF: " + rs.getString("nombre_archivo_pdf"));
            System.out.println("   Ruta PDF: " + rs.getString("ruta_archivo_pdf"));
            System.out.println("   Tiene PDF BLOB: " + rs.getString("tiene_pdf_blob"));
            
            Object tamaño = rs.getObject("tamaño_pdf");
            if (tamaño != null) {
                System.out.println("   Tamaño PDF: " + tamaño + " bytes");
            } else {
                System.out.println("   Tamaño PDF: NULL");
            }
            System.out.println("   ─────────────────────");
        }
        
        if (contador == 0) {
            System.out.println("❌ NO SE ENCONTRARON PRESUPUESTOS RECIENTES");
            
            // Verificar si existen presupuestos en general
            String sqlTotal = "SELECT COUNT(*) as total FROM presupuestos";
            PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal);
            ResultSet rsTotal = stmtTotal.executeQuery();
            
            if (rsTotal.next()) {
                int total = rsTotal.getInt("total");
                System.out.println("📊 Total de presupuestos en BD: " + total);
                
                if (total > 0) {
                    // Mostrar los últimos 3 presupuestos sin filtro de fecha
                    String sqlUltimos = """
                        SELECT cliente_nombre, fecha_creacion, total_general 
                        FROM presupuestos 
                        ORDER BY fecha_creacion DESC 
                        LIMIT 3
                        """;
                    PreparedStatement stmtUltimos = conn.prepareStatement(sqlUltimos);
                    ResultSet rsUltimos = stmtUltimos.executeQuery();
                    
                    System.out.println("📋 Últimos presupuestos:");
                    while (rsUltimos.next()) {
                        System.out.println("   • " + rsUltimos.getString("cliente_nombre") + 
                                         " - " + rsUltimos.getTimestamp("fecha_creacion") + 
                                         " - $" + rsUltimos.getDouble("total_general"));
                    }
                }
            }
        } else {
            System.out.println("✅ ENCONTRADOS " + contador + " PRESUPUESTOS RECIENTES");
        }
        
        System.out.println("🔍 === FIN DEBUG PRESUPUESTOS ===");
        
    } catch (SQLException e) {
        System.err.println("❌ Error en debug presupuestos: " + e.getMessage());
    }
}

    private void configurarListaExtras() {
        try {
            if (listaExtras != null) {
                // Configurar la lista para que se vea bonita
                listaExtras.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: transparent;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
                
                // Configurar el comportamiento de las celdas
                listaExtras.setCellFactory(listView -> new javafx.scene.control.ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            setText(item);
                            setStyle(
                                "-fx-font-size: 13px;" +
                                "-fx-padding: 8px 12px;" +
                                "-fx-text-fill: #2c3e50;" +
                                "-fx-font-family: 'Segoe UI';"
                            );
                        }
                    }
                });
                
                System.out.println("VistaPreviaController: ✅ ListView de extras configurado");
            }
        } catch (Exception e) {
            System.err.println("VistaPreviaController: ❌ Error configurando ListView: " + e.getMessage());
        }
    }
    
    private void configurarValoresPorDefecto() {
        try {
            if (lblNombre != null) lblNombre.setText("Error al cargar");
            if (lblPaquete != null) lblPaquete.setText("Error al cargar");
            if (txtExtras != null) txtExtras.setText("Sin extras");
            if (lblTotal != null) lblTotal.setText("0.00");
        } catch (Exception e) {
            System.err.println("VistaPreviaController: ❌ ERROR CRÍTICO: " + e.getMessage());
        }
    }
    
    private void cargarDatos() {
        try {
            System.out.println("VistaPreviaController: 🔄 Obteniendo SesionTemporal...");
            SesionTemporal sesion = SesionTemporal.getInstancia();
            
            if (sesion == null) {
                System.out.println("VistaPreviaController: ❌ SesionTemporal es null");
                configurarValoresPorDefecto();
                return;
            }

            // Cargar cliente
            if (lblNombre != null) {
                if (sesion.hayClienteSeleccionado()) {
                    String cliente = sesion.getClienteNombreCompleto();
                    lblNombre.setText(cliente);
                    System.out.println("VistaPreviaController: ✅ Cliente: " + cliente);
                } else {
                    lblNombre.setText("Sin cliente");
                }
            }

            // Cargar paquete
            if (lblPaquete != null) {
                if (sesion.hayPaqueteSeleccionado()) {
                    String paquete = sesion.getPaqueteNombre();
                    lblPaquete.setText(paquete);
                    System.out.println("VistaPreviaController: ✅ Paquete: " + paquete);
                } else {
                    lblPaquete.setText("Sin paquete");
                }
            }

            // Cargar total
            if (lblTotal != null) {
                if (sesion.hayPaqueteSeleccionado()) {
                    double total = sesion.getTotalGeneral();
                    lblTotal.setText(String.format("%.2f", total));
                    System.out.println("VistaPreviaController: ✅ Total: $" + total);
                } else {
                    lblTotal.setText("0.00");
                }
            }

            // Cargar extras en ListView
            cargarExtrasEnLista(sesion);
            
        } catch (Exception e) {
            System.err.println("VistaPreviaController: ❌ Error al cargar datos: " + e.getMessage());
            configurarValoresPorDefecto();
        }
    }
    
    // MÉTODO: CARGAR EXTRAS EN LISTA 
    private void cargarExtrasEnLista(SesionTemporal sesion) {
        System.out.println("🔍 === CARGANDO EXTRAS EN LISTA ===");
        
        try {
            // Verificar que los componentes existen
            if (listaExtras == null) {
                System.err.println("❌ listaExtras es NULL");
                return;
            }
            
            if (lblSinExtras == null) {
                System.err.println("❌ lblSinExtras es NULL");
                return;
            }
            
            System.out.println("✅ Componentes UI encontrados");
            
            // Limpiar lista
            listaExtras.getItems().clear();
            System.out.println("✅ Lista limpiada");
            
            // Verificar si hay extras
            boolean hayExtras = sesion.tieneExtras();
            System.out.println("📦 ¿Hay extras?: " + hayExtras);
            
            if (hayExtras) {
                System.out.println("🔄 Cargando extras desde sesión...");
                
                // Obtener lista de extras
                java.util.List<modelos.Extra> extras = sesion.getExtrasSeleccionados();
                System.out.println("📊 Número de extras en sesión: " + (extras != null ? extras.size() : "NULL"));
                
                if (extras != null) {
                    int extrasAgregados = 0;
                    
                    for (modelos.Extra extra : extras) {
                        if (extra != null && extra.getCantidad() > 0) {
                            String textoExtra = String.format("• %s x%d - $%.2f MXN", 
                                extra.getNombre(), 
                                extra.getCantidad(), 
                                extra.getPrecio() * extra.getCantidad());
                            
                            listaExtras.getItems().add(textoExtra);
                            extrasAgregados++;
                            
                            System.out.println("✅ Extra agregado: " + textoExtra);
                        }
                    }
                    
                    // Agregar total de extras si hay
                    if (extrasAgregados > 0) {
                        double totalExtras = sesion.getTotalExtras();
                        listaExtras.getItems().add(""); // Línea en blanco
                        listaExtras.getItems().add("🎯 TOTAL EXTRAS: $" + String.format("%.2f", totalExtras) + " MXN");
                        System.out.println("✅ Total extras agregado: $" + totalExtras);
                    }
                    
                    // Mostrar lista, ocultar mensaje
                    listaExtras.setVisible(true);
                    lblSinExtras.setVisible(false);
                    
                    System.out.println("✅ Lista visible con " + listaExtras.getItems().size() + " elementos");
                    
                    // DEBUG: Imprimir todos los elementos de la lista
                    System.out.println("📋 Contenido de la lista:");
                    for (int i = 0; i < listaExtras.getItems().size(); i++) {
                        System.out.println("  [" + i + "] " + listaExtras.getItems().get(i));
                    }
                    
                } else {
                    System.err.println("❌ Lista de extras es NULL");
                    mostrarSinExtras();
                }
                
            } else {
                System.out.println("ℹ️ No hay extras, mostrando mensaje");
                mostrarSinExtras();
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR al cargar extras: " + e.getMessage());
            mostrarSinExtras();
        }
        
        System.out.println("🔍 FIN CARGA DE EXTRAS ");
    }

    // MÉTODO AUXILIAR PARA MOSTRAR "SIN EXTRAS" 
    private void mostrarSinExtras() {
        try {
            if (listaExtras != null) {
                listaExtras.setVisible(false);
            }
            
            if (lblSinExtras != null) {
                lblSinExtras.setVisible(true);
                lblSinExtras.setText("Sin extras seleccionados");
            }
            
            System.out.println("✅ Mensaje 'Sin extras' mostrado");
            
        } catch (Exception e) {
            System.err.println("❌ Error al mostrar 'Sin extras': " + e.getMessage());
        }
    }

    @FXML
    private void handleReturn(ActionEvent event) {
        System.out.println("VistaPreviaController: 🔄 Regresando a PaquetesPresupuesto...");
        try {
            App.setRoot("PaquetesPresupuesto");
        } catch (IOException e) {
            System.err.println("VistaPreviaController: ❌ Error al regresar: " + e.getMessage());
        }
    }

    @FXML
    private void handleImprimir(ActionEvent event) {
        System.out.println("VistaPreviaController: 🔄 Generando PDF...");
        try {
            if (!validarDatosCompletos()) {
                mostrarAlerta("Por favor complete todos los campos antes de guardar.", Alert.AlertType.WARNING);
                return;
            }

            boolean pdfGenerado = generarPDF();

            if (pdfGenerado) {
                mostrarAlerta("PDF generado exitosamente en la carpeta 'Presupuestos' y guardado en base de datos.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error al generar el PDF.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("VistaPreviaController: ❌ Error al imprimir: " + e.getMessage());
            mostrarAlerta("Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEnviarCorreo(ActionEvent event) {
        System.out.println("VistaPreviaController: 🔄 Enviando email...");
        try {
            if (!validarDatosCompletos()) {
                mostrarAlerta("Por favor complete todos los campos antes de enviar el email.", Alert.AlertType.WARNING);
                return;
            }

            SesionTemporal sesion = SesionTemporal.getInstancia();
            String emailCliente = "";
            
            // Verificar si el cliente tiene email registrado
            if (sesion.hayClienteSeleccionado()) {
                String emailRegistrado = sesion.getClienteEmail();
                
                if (emailRegistrado != null && !emailRegistrado.trim().isEmpty() && 
                    !emailRegistrado.equals("Sin email") && !emailRegistrado.equals("No registrado")) {
                    
                    // Cliente tiene email, preguntar si usarlo o ingresar otro
                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacion.setTitle("Enviar Presupuesto por Email");
                    confirmacion.setHeaderText("Email del cliente encontrado");
                    confirmacion.setContentText("¿Desea enviar el presupuesto a:\n" + emailRegistrado + "\n\n¿O prefiere ingresar otro email?");
                    
                    confirmacion.getButtonTypes().clear();
                    confirmacion.getButtonTypes().addAll(
                        new javafx.scene.control.ButtonType("Usar email registrado", javafx.scene.control.ButtonBar.ButtonData.YES),
                        new javafx.scene.control.ButtonType("Ingresar otro email", javafx.scene.control.ButtonBar.ButtonData.NO),
                        new javafx.scene.control.ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE)
                    );
                    
                    Optional<javafx.scene.control.ButtonType> resultado = confirmacion.showAndWait();
                    
                    if (resultado.isPresent()) {
                        if (resultado.get().getButtonData() == javafx.scene.control.ButtonBar.ButtonData.YES) {
                            // Usar email registrado
                            emailCliente = emailRegistrado;
                            System.out.println("✅ Usando email registrado: " + emailCliente);
                        } else if (resultado.get().getButtonData() == javafx.scene.control.ButtonBar.ButtonData.NO) {
                            // Pedir otro email
                            emailCliente = solicitarEmailManual(emailRegistrado);
                            if (emailCliente == null) return; // Usuario canceló
                        } else {
                            // Usuario canceló
                            return;
                        }
                    } else {
                        return; // Usuario cerró el diálogo
                    }
                } else {
                    // Cliente no tiene email, solicitar uno
                    System.out.println("ℹ️ Cliente sin email registrado, solicitando...");
                    emailCliente = solicitarEmailManual("");
                    if (emailCliente == null) return; // Usuario canceló
                }
            } else {
                mostrarAlerta("No hay cliente seleccionado.", Alert.AlertType.WARNING);
                return;
            }

            // Validar formato de email básico
            if (!esEmailValido(emailCliente)) {
                mostrarAlerta("Por favor ingrese un email válido.", Alert.AlertType.WARNING);
                return;
            }

            // Generar PDF primero
            boolean pdfGenerado = generarPDF();

            if (!pdfGenerado) {
                mostrarAlerta("Error al generar el PDF para enviar.", Alert.AlertType.ERROR);
                return;
            }

            // Enviar email
            boolean emailEnviado = enviarEmailConPresupuesto(emailCliente);

            if (emailEnviado) {
                // Actualizar el registro del presupuesto con el email enviado
                actualizarEmailEnviado(emailCliente);
                mostrarAlerta("✅ Presupuesto enviado exitosamente a:\n" + emailCliente, Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("❌ Error al enviar el email. Verifique la configuración de correo.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("VistaPreviaController: ❌ Error al enviar email: " + e.getMessage());
            mostrarAlerta("Error al enviar email: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // MÉTODO AUXILIAR: SOLICITAR EMAIL MANUAL
    private String solicitarEmailManual(String emailSugerido) {
        TextInputDialog dialog = new TextInputDialog(emailSugerido);
        dialog.setTitle("Enviar Presupuesto por Email");
        dialog.setHeaderText("Ingrese el email del cliente:");
        dialog.setContentText("Email:");
        
        // Personalizar el diálogo
        dialog.getDialogPane().setPrefWidth(400);
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            return result.get().trim();
        }
        
        return null; // Usuario canceló o no ingresó nada
    }

    // MÉTODO AUXILIAR: VALIDAR EMAIL
    private boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Validación básica de email
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

    // ========== MÉTODO AUXILIAR: ACTUALIZAR EMAIL ENVIADO ==========
    private void actualizarEmailEnviado(String emailCliente) {
        try (java.sql.Connection conn = database.Conexion.conectar()) {
            SesionTemporal sesion = SesionTemporal.getInstancia();
            
            String sql = "UPDATE presupuestos SET ultimo_envio_email = NOW(), email_enviado_a = ? " +
                        "WHERE cliente_id = ? AND DATE(fecha_creacion) = CURDATE() " +
                        "ORDER BY fecha_creacion DESC LIMIT 1";
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailCliente);
            stmt.setInt(2, sesion.getClienteId());
            
            int filasActualizadas = stmt.executeUpdate();
            if (filasActualizadas > 0) {
                System.out.println("✅ Registro de envío de email actualizado");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar registro de email: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalir(ActionEvent event) {
        System.out.println("VistaPreviaController: 🔄 Saliendo...");
        try {
            App.setRoot("PanelPrincipal");
        } catch (IOException e) {
            System.err.println("VistaPreviaController: ❌ Error al salir: " + e.getMessage());
        }
    }

    @FXML
    private void radioMat(ActionEvent event) {
        if (mautinoRadio != null && mautinoRadio.isSelected()) {
            if (vespertinoRadio != null) vespertinoRadio.setSelected(false);
            try {
                SesionTemporal.getInstancia().setHorarioPresupuesto("Matutino");
            } catch (Exception e) {
                System.err.println("VistaPreviaController: Error al guardar horario: " + e.getMessage());
            }
        }
    }

    @FXML
    private void radioVesp(ActionEvent event) {
        if (vespertinoRadio != null && vespertinoRadio.isSelected()) {
            if (mautinoRadio != null) mautinoRadio.setSelected(false);
            try {
                SesionTemporal.getInstancia().setHorarioPresupuesto("Vespertino");
            } catch (Exception e) {
                System.err.println("VistaPreviaController: Error al guardar horario: " + e.getMessage());
            }
        }
    }

    // MÉTODOS AUXILIARES
    private boolean validarDatosCompletos() {
        try {
            if (lblNombre == null || lblNombre.getText().equals("-") || 
                lblNombre.getText().equals("Sin cliente") ||
                lblNombre.getText().equals("Error al cargar")) {
                return false;
            }

            if (lblPaquete == null || lblPaquete.getText().equals("-") || 
                lblPaquete.getText().equals("Sin paquete") ||
                lblPaquete.getText().equals("Error al cargar")) {
                return false;
            }

            if (plazosItem == null || plazosItem.getText().equals("Seleccionar plazos")) {
                return false;
            }

            if (metodoPagoItem == null || metodoPagoItem.getText().equals("Seleccionar método")) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

 // === MÉTODO COMPLETO: GENERAR PDF ===
// REEMPLAZA COMPLETAMENTE EL MÉTODO generarPDF() en VistaPreviaPresupuestoController.java

private boolean generarPDF() {
    System.out.println("VistaPreviaController: 🔄 Iniciando generación de PDF COMPLETA...");
    
    try {
        SesionTemporal sesion = SesionTemporal.getInstancia();
        
        // Verificaciones previas
        if (sesion == null) {
            System.err.println("❌ SesionTemporal es null");
            mostrarAlerta("Error: No se encontraron datos de la sesión", Alert.AlertType.ERROR);
            return false;
        }
        
        if (!sesion.hayClienteSeleccionado()) {
            System.err.println("❌ No hay cliente seleccionado");
            mostrarAlerta("Error: No hay cliente seleccionado", Alert.AlertType.ERROR);
            return false;
        }
        
        if (!sesion.hayPaqueteSeleccionado()) {
            System.err.println("❌ No hay paquete seleccionado");
            mostrarAlerta("Error: No hay paquete seleccionado", Alert.AlertType.ERROR);
            return false;
        }
        
        // Verificar y crear carpeta
        verificarRutaDesktop();
        File carpeta = crearCarpetaPresupuestos();
        if (carpeta == null) {
            System.err.println("❌ No se pudo crear carpeta para PDF");
            return false;
        }
        
        System.out.println("✅ Carpeta para PDFs: " + carpeta.getAbsolutePath());
        
        // Generar nombre del archivo
        String nombreCliente = sesion.getClienteNombreCompleto()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", "_")
            .trim();
            
        if (nombreCliente.isEmpty()) {
            nombreCliente = "Cliente_Sin_Nombre";
        }
        
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nombreArchivo = "Presupuesto_" + nombreCliente + "_" + fecha + ".pdf";
        
        File archivo = new File(carpeta, nombreArchivo);
        
        // Verificar si el archivo ya existe y crear uno único
        int contador = 1;
        while (archivo.exists()) {
            String nombreArchivoNuevo = "Presupuesto_" + nombreCliente + "_" + fecha + "_" + contador + ".pdf";
            archivo = new File(carpeta, nombreArchivoNuevo);
            contador++;
            if (contador > 100) {
                System.err.println("❌ Demasiados archivos con el mismo nombre");
                mostrarAlerta("Error: Demasiados archivos con el mismo nombre", Alert.AlertType.ERROR);
                return false;
            }
        }
        
        System.out.println("✅ Archivo destino: " + archivo.getAbsolutePath());
        
        // ========== GENERAR PDF CON iText ==========
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, new FileOutputStream(archivo));
        documento.open();
        
        // Configurar fuentes
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLUE);
        Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
        Font pequenaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        
        // === HEADER DEL PDF ===
        Paragraph titulo = new Paragraph("🎉 SEGUNDO CASTILLO", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(5);
        documento.add(titulo);
        
        Paragraph subtitulo = new Paragraph("PRESUPUESTO DE EVENTO", subtituloFont);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        documento.add(subtitulo);
        
        // === INFORMACIÓN DEL CLIENTE ===
        documento.add(new Paragraph("INFORMACIÓN DEL CLIENTE", subtituloFont));
        documento.add(new Paragraph("Nombre: " + sesion.getClienteNombreCompleto(), normalFont));
        documento.add(new Paragraph("Teléfono: " + sesion.getClienteTelefono(), normalFont));
        documento.add(new Paragraph("Email: " + sesion.getClienteEmail(), normalFont));
        documento.add(new Paragraph("RFC: " + sesion.getClienteRfc(), normalFont));
        documento.add(new Paragraph("Fecha del presupuesto: " + fecha, normalFont));
        documento.add(new Paragraph(" ", normalFont));
        
        // === PAQUETE SELECCIONADO ===
        documento.add(new Paragraph("PAQUETE SELECCIONADO", subtituloFont));
        documento.add(new Paragraph("Nombre: " + sesion.getPaqueteNombre(), boldFont));
        documento.add(new Paragraph("Precio: $" + String.format("%.2f", sesion.getPaquetePrecio()) + " MXN", normalFont));
        documento.add(new Paragraph(" ", normalFont));
        
        // === SERVICIOS ADICIONALES ===
        if (sesion.tieneExtras()) {
            documento.add(new Paragraph("SERVICIOS ADICIONALES", subtituloFont));
            
            java.util.List<modelos.Extra> extras = sesion.getExtrasSeleccionados();
            for (modelos.Extra extra : extras) {
                if (extra.getCantidad() > 0) {
                    documento.add(new Paragraph("• " + extra.getNombre(), boldFont));
                    documento.add(new Paragraph("  Cantidad: " + extra.getCantidad() + 
                                               " | Precio unitario: $" + String.format("%.2f", extra.getPrecio()) + 
                                               " | Subtotal: $" + String.format("%.2f", extra.getPrecio() * extra.getCantidad()) + " MXN", normalFont));
                    documento.add(new Paragraph(" ", pequenaFont));
                }
            }
            
            documento.add(new Paragraph("Total Extras: $" + String.format("%.2f", sesion.getTotalExtras()) + " MXN", boldFont));
            documento.add(new Paragraph(" ", normalFont));
        } else {
            documento.add(new Paragraph("SERVICIOS ADICIONALES", subtituloFont));
            documento.add(new Paragraph("Sin servicios adicionales seleccionados", normalFont));
            documento.add(new Paragraph(" ", normalFont));
        }
        
        // === DETALLES DEL EVENTO ===
        documento.add(new Paragraph("DETALLES DEL EVENTO", subtituloFont));
        String horario = mautinoRadio.isSelected() ? "Matutino" : "Vespertino";
        documento.add(new Paragraph("Horario: " + horario, normalFont));
        documento.add(new Paragraph("Plazos de pago: " + plazosItem.getText(), normalFont));
        documento.add(new Paragraph("Método de pago: " + metodoPagoItem.getText(), normalFont));
        documento.add(new Paragraph(" ", normalFont));
        
        // === RESUMEN FINANCIERO ===
        documento.add(new Paragraph("RESUMEN FINANCIERO", subtituloFont));
        documento.add(new Paragraph("Paquete base: $" + String.format("%.2f", sesion.getPaquetePrecio()) + " MXN", normalFont));
        
        if (sesion.tieneExtras()) {
            documento.add(new Paragraph("Servicios adicionales: $" + String.format("%.2f", sesion.getTotalExtras()) + " MXN", normalFont));
        }
        
        documento.add(new Paragraph(" ", normalFont));
        
        // TOTAL DESTACADO
        Paragraph total = new Paragraph("TOTAL GENERAL: $" + String.format("%.2f", sesion.getTotalGeneral()) + " MXN", 
                                       new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.RED));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        documento.add(total);
        
        // === INFORMACIÓN ADICIONAL ===
        documento.add(new Paragraph(" ", normalFont));
        documento.add(new Paragraph(" ", normalFont));
        documento.add(new Paragraph("INFORMACIÓN ADICIONAL", subtituloFont));
        documento.add(new Paragraph("• Este presupuesto es válido por 30 días", normalFont));
        documento.add(new Paragraph("• Se requiere el 50% de anticipo para confirmar la reserva", normalFont));
        documento.add(new Paragraph("• El evento incluye todos los servicios especificados", normalFont));
        documento.add(new Paragraph("• Los precios están expresados en pesos mexicanos (MXN)", normalFont));
        
        // === PIE DE PÁGINA ===
        documento.add(new Paragraph(" ", normalFont));
        documento.add(new Paragraph(" ", normalFont));
        Paragraph contactInfo = new Paragraph("CONTACTO: bienvenido@forgestudio.com.mx", normalFont);
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        documento.add(contactInfo);
        
        Paragraph pie = new Paragraph("Presupuesto generado el: " + 
                                     LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                                     pequenaFont);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);
        
        // Cerrar documento
        documento.close();
        
        System.out.println("✅ PDF generado exitosamente: " + archivo.getAbsolutePath());
        
        // === GUARDAR EN BASE DE DATOS ===
        boolean guardadoEnBD = guardarPresupuestoEnBD(sesion, archivo.getAbsolutePath(), nombreArchivo);
        
        if (guardadoEnBD) {
            System.out.println("✅ Presupuesto guardado en base de datos");
        } else {
            System.err.println("⚠️ PDF generado pero no se pudo guardar en BD");
        }
        
        // === ABRIR PDF ===
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
                System.out.println("✅ PDF abierto automáticamente");
            } else {
                System.out.println("⚠️ Desktop no soportado, PDF guardado en: " + archivo.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("⚠️ No se pudo abrir automáticamente: " + e.getMessage());
        }
        
        return true;
        
    } catch (DocumentException | FileNotFoundException e) {
        System.err.println("❌ Error al generar PDF: " + e.getMessage());
        mostrarAlerta("Error al generar PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        return false;
    } catch (Exception e) {
        System.err.println("❌ Error general al generar PDF: " + e.getMessage());
        mostrarAlerta("Error inesperado al generar PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        return false;
    }
}

// === MÉTODO MODIFICADO: CREAR CARPETA EN DESKTOP COMO PRIORIDAD ===
private File crearCarpetaPresupuestos() {
    System.out.println("🔄 Intentando crear carpeta de presupuestos en Desktop...");
    
    // Lista de ubicaciones con DESKTOP como PRIMERA OPCIÓN
    String[] ubicacionesPosibles = {
        System.getProperty("user.home") + "/Desktop/Presupuestos",     // 🎯 DESKTOP PRIMERO
        System.getProperty("user.home") + "\\Desktop\\Presupuestos",   // 🎯 DESKTOP (Windows)
        System.getProperty("user.home") + "/Documents/Presupuestos",   // Documentos
        System.getProperty("user.dir") + "/Presupuestos",              // Carpeta actual
        "Presupuestos",                                                // Carpeta relativa
        System.getProperty("java.io.tmpdir") + "/Presupuestos"         // Temporal como último recurso
    };
    
    for (String ubicacion : ubicacionesPosibles) {
        try {
            File carpeta = new File(ubicacion);
            
            System.out.println("🔍 Probando ubicación: " + carpeta.getAbsolutePath());
            
            // Si la carpeta ya existe y tiene permisos
            if (carpeta.exists()) {
                if (carpeta.canWrite()) {
                    System.out.println("✅ Carpeta existente con permisos: " + carpeta.getAbsolutePath());
                    return carpeta;
                } else {
                    System.out.println("❌ Carpeta existe pero sin permisos de escritura: " + ubicacion);
                    continue;
                }
            }
            
            // Intentar crear la carpeta y sus directorios padre si no existen
            boolean carpetaCreada = carpeta.mkdirs();
            
            if (carpetaCreada || carpeta.exists()) {
                // Verificar permisos de escritura
                if (carpeta.canWrite()) {
                    System.out.println("✅ Carpeta creada exitosamente en DESKTOP: " + carpeta.getAbsolutePath());
                    
                    // Mostrar mensaje de confirmación al usuario
                    if (ubicacion.contains("Desktop")) {
                        System.out.println("🎉 PDFs se guardarán en el Desktop: " + carpeta.getAbsolutePath());
                    }
                    
                    return carpeta;
                } else {
                    System.out.println("❌ Carpeta creada pero sin permisos de escritura: " + ubicacion);
                    continue;
                }
            } else {
                System.out.println("❌ No se pudo crear carpeta: " + ubicacion);
                continue;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error con ubicación " + ubicacion + ": " + e.getMessage());
            continue;
        }
    }
    
    // Si llegamos aquí, ninguna ubicación funcionó
    System.err.println("❌ No se pudo crear carpeta en ninguna ubicación, incluyendo Desktop");
    
    mostrarAlerta(
        "No se pudo crear la carpeta de presupuestos en el Desktop.\n\n" +
        "Ubicaciones intentadas:\n" +
        "• Desktop del usuario (prioridad)\n" +
        "• Documentos del usuario\n" +
        "• Carpeta actual del programa\n" +
        "• Directorio de trabajo\n" +
        "• Carpeta temporal\n\n" +
        "Verifica los permisos de tu sistema o ejecuta como administrador.",
        Alert.AlertType.ERROR
    );
    
    return null;
}

// === MÉTODO ADICIONAL: VERIFICAR Y MOSTRAR RUTA DEL DESKTOP ===
private void verificarRutaDesktop() {
    try {
        String rutaDesktop = System.getProperty("user.home") + "/Desktop";
        File desktop = new File(rutaDesktop);
        
        System.out.println("=== VERIFICACIÓN DEL DESKTOP ===");
        System.out.println("Ruta del Desktop: " + desktop.getAbsolutePath());
        System.out.println("Desktop existe: " + desktop.exists());
        System.out.println("Puede escribir: " + desktop.canWrite());
        System.out.println("Puede leer: " + desktop.canRead());
        
        // Probar con la ruta de Windows también
        String rutaDesktopWindows = System.getProperty("user.home") + "\\Desktop";
        File desktopWindows = new File(rutaDesktopWindows);
        System.out.println("Ruta Desktop Windows: " + desktopWindows.getAbsolutePath());
        System.out.println("Desktop Windows existe: " + desktopWindows.exists());
        
        System.out.println("================================");
        
    } catch (Exception e) {
        System.err.println("Error verificando Desktop: " + e.getMessage());
    }
}

// === MÉTODO AUXILIAR: OBTENER INFORMACIÓN DEL SISTEMA ===
private void mostrarInformacionSistema() {
    System.out.println("=== INFORMACIÓN DEL SISTEMA ===");
    System.out.println("Usuario: " + System.getProperty("user.name"));
    System.out.println("Directorio home: " + System.getProperty("user.home"));
    System.out.println("Directorio actual: " + System.getProperty("user.dir"));
    System.out.println("Directorio temporal: " + System.getProperty("java.io.tmpdir"));
    System.out.println("Sistema operativo: " + System.getProperty("os.name"));
    System.out.println("Versión Java: " + System.getProperty("java.version"));
    System.out.println("===============================");
}

    /// ========== MÉTODO MEJORADO: GUARDAR PDF COMO BLOB EN BD ==========
// === MÉTODO CORREGIDO: GUARDAR PRESUPUESTO EN BD ===
// REEMPLAZA EL MÉTODO guardarPresupuestoEnBD() en VistaPreviaPresupuestoController.java

private boolean guardarPresupuestoEnBD(SesionTemporal sesion, String rutaPDF, String nombreArchivo) {
    int intentos = 0;
    int maxIntentos = 3;
    
    while (intentos < maxIntentos) {
        try (java.sql.Connection conn = database.Conexion.conectar()) {
            System.out.println("💾 Guardando presupuesto con PDF en base de datos... (Intento " + (intentos + 1) + ")");
            
            // Leer el archivo PDF como bytes
            byte[] pdfBytes = null;
            try {
                java.nio.file.Path pdfPath = java.nio.file.Paths.get(rutaPDF);
                pdfBytes = java.nio.file.Files.readAllBytes(pdfPath);
                System.out.println("✅ PDF leído: " + pdfBytes.length + " bytes");
            } catch (Exception e) {
                System.err.println("❌ Error leyendo PDF: " + e.getMessage());
                return false;
            }
            
            // Generar número de presupuesto único
            String numeroPresupuesto = generarNumeroPresupuesto();
            
            // Preparar resumen de extras
            String extrasDetalle = "";
            double totalExtras = 0.0;
            
            if (sesion.tieneExtras()) {
                StringBuilder extrasBuilder = new StringBuilder();
                java.util.List<modelos.Extra> extras = sesion.getExtrasSeleccionados();
                
                for (modelos.Extra extra : extras) {
                    if (extra.getCantidad() > 0) {
                        if (extrasBuilder.length() > 0) {
                            extrasBuilder.append("; ");
                        }
                        double subtotal = extra.getPrecio() * extra.getCantidad();
                        extrasBuilder.append(extra.getNombre())
                                   .append(" x").append(extra.getCantidad())
                                   .append(" ($").append(String.format("%.2f", subtotal)).append(")");
                        totalExtras += subtotal;
                    }
                }
                extrasDetalle = extrasBuilder.toString();
            }
            
            // SQL COMPLETO
            String sql = "INSERT INTO presupuestos (" +
                        "numero_presupuesto, cliente_id, cliente_nombre, cliente_rfc, cliente_telefono, cliente_email, " +
                        "paquete_id, paquete_nombre, paquete_precio, " +
                        "extras_detalle, total_extras, total_general, " +
                        "horario, plazos_pago, metodo_pago, " +
                        "nombre_archivo_pdf, ruta_archivo_pdf, archivo_pdf_contenido, " +
                        "fecha_creacion, valido_hasta, estado" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            
            // Llenar parámetros
            stmt.setString(1, numeroPresupuesto);
            stmt.setInt(2, sesion.getClienteId());
            stmt.setString(3, sesion.getClienteNombreCompleto());
            stmt.setString(4, sesion.getClienteRfc());
            stmt.setString(5, sesion.getClienteTelefono());
            stmt.setString(6, sesion.getClienteEmail());
            stmt.setInt(7, sesion.getPaqueteId());
            stmt.setString(8, sesion.getPaqueteNombre());
            stmt.setDouble(9, sesion.getPaquetePrecio());
            stmt.setString(10, extrasDetalle);
            stmt.setDouble(11, totalExtras);
            stmt.setDouble(12, sesion.getTotalGeneral());
            stmt.setString(13, mautinoRadio.isSelected() ? "Matutino" : "Vespertino");
            stmt.setString(14, plazosItem.getText());
            stmt.setString(15, metodoPagoItem.getText());
            stmt.setString(16, nombreArchivo);
            stmt.setString(17, rutaPDF);
            stmt.setBytes(18, pdfBytes); // PDF COMPLETO COMO BLOB
            stmt.setTimestamp(19, java.sql.Timestamp.valueOf(LocalDate.now().atStartOfDay()));
            stmt.setDate(20, java.sql.Date.valueOf(LocalDate.now().plusDays(30)));
            stmt.setString(21, "Pendiente");
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                java.sql.ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int presupuestoId = generatedKeys.getInt(1);
                    
                    // Guardar extras individuales
                    if (sesion.tieneExtras()) {
                        guardarExtrasIndividuales(conn, presupuestoId, sesion.getExtrasSeleccionados());
                    }
                    
                    System.out.println("✅ Presupuesto Y PDF guardados con número: " + numeroPresupuesto);
                    System.out.println("✅ Tamaño del PDF guardado: " + pdfBytes.length + " bytes");
                    System.out.println("✅ ID del presupuesto: " + presupuestoId);
                    
                    // ⭐ VERIFICAR QUE SE GUARDÓ CORRECTAMENTE
                    verificarPresupuestoGuardado(sesion);
                    
                    return true;
                }
            }
            
            return false;
            
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            intentos++;
            System.err.println("⚠️ Número de presupuesto duplicado, reintentando... (" + intentos + "/" + maxIntentos + ")");
            
            if (intentos >= maxIntentos) {
                System.err.println("❌ Error: No se pudo generar un número único después de " + maxIntentos + " intentos");
                return false;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar presupuesto en BD: " + e.getMessage());
            e.printStackTrace(); // ⭐ AGREGAR STACK TRACE PARA DEBUG
            return false;
        }
    }
    
    return false;
}

    // MÉTODO: GUARDAR EXTRAS INDIVIDUALES
    private void guardarExtrasIndividuales(java.sql.Connection conn, int presupuestoId, List<modelos.Extra> extras) {
        try {
            String sqlExtras = "INSERT INTO presupuesto_extras (presupuesto_id, extra_id, cantidad) VALUES (?, ?, ?)";
            java.sql.PreparedStatement stmtExtras = conn.prepareStatement(sqlExtras);
            
            for (modelos.Extra extra : extras) {
                if (extra.getCantidad() > 0) {
                    stmtExtras.setInt(1, presupuestoId);
                    stmtExtras.setInt(2, extra.getId());
                    stmtExtras.setInt(3, extra.getCantidad());
                    stmtExtras.addBatch();
                }
            }
            
            int[] resultados = stmtExtras.executeBatch();
            System.out.println("✅ " + resultados.length + " extras guardados individualmente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar extras individuales: " + e.getMessage());
        }
    }
    
    public static boolean descargarPDFDesdeBD(int presupuestoId, String rutaDestino) {
    try (java.sql.Connection conn = database.Conexion.conectar()) {
        
        String sql = "SELECT archivo_pdf_contenido, nombre_archivo_pdf FROM presupuestos WHERE id = ?";
        java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, presupuestoId);
        
        java.sql.ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            byte[] pdfBytes = rs.getBytes("archivo_pdf_contenido");
            String nombreArchivo = rs.getString("nombre_archivo_pdf");
            
            if (pdfBytes != null && pdfBytes.length > 0) {
                // Crear archivo
                java.io.File archivo = new java.io.File(rutaDestino, nombreArchivo);
                java.nio.file.Files.write(archivo.toPath(), pdfBytes);
                
                System.out.println("✅ PDF descargado: " + archivo.getAbsolutePath());
                return true;
            }
        }
        
        return false;
        
    } catch (Exception e) {
        System.err.println("❌ Error descargando PDF: " + e.getMessage());
        return false;
    }
}

    // ========== MÉTODO: GENERAR NÚMERO DE PRESUPUESTO ==========
    private String generarNumeroPresupuesto() {
        try (java.sql.Connection conn = database.Conexion.conectar()) {
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String numeroPresupuesto;
            int contador = 1;
            boolean numeroExiste = true;
            
            // Buscar un número disponible
            while (numeroExiste && contador <= 999) {
                numeroPresupuesto = String.format("PRES-%s-%03d", fechaHoy, contador);
                
                // Verificar si el número ya existe
                String sqlVerificar = "SELECT COUNT(*) as existe FROM presupuestos WHERE numero_presupuesto = ?";
                java.sql.PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar);
                stmtVerificar.setString(1, numeroPresupuesto);
                java.sql.ResultSet rs = stmtVerificar.executeQuery();
                
                if (rs.next()) {
                    numeroExiste = rs.getInt("existe") > 0;
                    if (!numeroExiste) {
                        System.out.println("✅ Número de presupuesto generado: " + numeroPresupuesto);
                        return numeroPresupuesto;
                    }
                }
                
                contador++;
            }
            
            // Si llegamos aquí, usar timestamp como fallback
            long timestamp = System.currentTimeMillis();
            String numeroFallback = "PRES-" + fechaHoy + "-" + String.valueOf(timestamp).substring(7);
            System.out.println("⚠️ Usando número fallback: " + numeroFallback);
            return numeroFallback;
            
        } catch (Exception e) {
            System.err.println("❌ Error generando número de presupuesto: " + e.getMessage());
            
            // Fallback con timestamp
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long timestamp = System.currentTimeMillis();
            return "PRES-" + fechaHoy + "-" + String.valueOf(timestamp).substring(7);
        }
    }

    // ========== MÉTODO: ENVIAR EMAIL CON PRESUPUESTO ==========
    private boolean enviarEmailConPresupuesto(String emailCliente) {
        try {
            System.out.println("VistaPreviaController: 🔄 Configurando email con Hostinger...");
            
            // Configuración del servidor SMTP (Hostinger)
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.hostinger.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.hostinger.com");
            
            // Debug y timeouts
            props.put("mail.debug", "false"); // Cambia a true si necesitas debug
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
            
            // CONFIGURACIÓN DE HOSTINGER - Cambiar por tus credenciales
            final String emailEmpresa = "bienvenido@forgestudio.com.mx"; 
            final String passwordEmpresa = "Eventya321@"; 
            
            // Crear sesión
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailEmpresa, passwordEmpresa);
                }
            });
            
            // Crear mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailEmpresa, "SEGUNDO CASTILLO"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailCliente));
            
            SesionTemporal sesion = SesionTemporal.getInstancia();
            String nombreCliente = sesion.getClienteNombreCompleto();
            
            message.setSubject("Presupuesto de Evento - " + nombreCliente + " - SEGUNDO CASTILLO");
            
            // Crear contenido HTML del email
            String contenidoHTML = crearContenidoEmailHTML(sesion, nombreCliente);
            
            // Configurar como HTML
            message.setContent(contenidoHTML, "text/html; charset=utf-8");
            
            // Enviar
            System.out.println("VistaPreviaController: 🔄 Enviando email via Hostinger...");
            Transport.send(message);
            
            System.out.println("VistaPreviaController: ✅ Email enviado exitosamente a: " + emailCliente);
            return true;
            
        } catch (UnsupportedEncodingException | MessagingException e) {
            System.err.println("VistaPreviaController: ❌ Error al enviar email: " + e.getMessage());
            
            // Mostrar información específica para Hostinger
            if (e.getMessage().contains("Authentication") || e.getMessage().contains("Username")) {
                System.err.println("\n🔧 CONFIGURACIÓN DE HOSTINGER:");
                System.err.println("1. Verifica que el email existe en tu panel de Hostinger");
                System.err.println("2. Asegúrate de que la contraseña sea correcta");
                System.err.println("3. Verifica que el dominio esté configurado correctamente");
                System.err.println("4. Email actual: bienvenido@forgestudio.com.mx");
            }
            
            return false;
        }
    }
    
    // MÉTODO: CREAR CONTENIDO HTML DEL EMAIL 
    private String crearContenidoEmailHTML(SesionTemporal sesion, String nombreCliente) {
        String horario = mautinoRadio.isSelected() ? "Matutino" : "Vespertino";
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 700px; margin: 0 auto; background-color: white; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }");
        html.append(".content { padding: 40px 30px; }");
        html.append(".section { margin-bottom: 30px; padding: 20px; background-color: #f8f9fa; border-radius: 10px; border-left: 4px solid #3498db; }");
        html.append(".section h3 { margin-top: 0; color: #2c3e50; font-size: 18px; }");
        html.append(".extra-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #e9ecef; }");
        html.append(".extra-item:last-child { border-bottom: none; }");
        html.append(".extra-name { font-weight: bold; color: #495057; }");
        html.append(".extra-quantity { color: #6c757d; font-size: 14px; }");
        html.append(".extra-price { font-weight: bold; color: #28a745; }");
        html.append(".total-section { background: linear-gradient(135deg, #27ae60, #2ecc71); color: white; padding: 25px; text-align: center; border-radius: 10px; margin: 20px 0; }");
        html.append(".info-box { background-color: #d1ecf1; padding: 20px; border-radius: 8px; border-left: 4px solid #0c5460; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1 style='margin: 0; font-size: 32px;'>🎉 SEGUNDO CASTILLO</h1>");
        html.append("<p style='margin: 10px 0 0 0; font-size: 18px; opacity: 0.9;'>Presupuesto de Evento</p>");
        html.append("</div>");
        
        // Contenido
        html.append("<div class='content'>");
        
        html.append("<h2 style='color: #2c3e50; margin-bottom: 20px;'>Estimado/a ").append(nombreCliente).append("</h2>");
        html.append("<p style='font-size: 16px; color: #495057; line-height: 1.6;'>Gracias por confiar en nosotros para tu evento. A continuación te presentamos el presupuesto detallado:</p>");
        
        // Detalles del evento
        html.append("<div class='section'>");
        html.append("<h3>📅 Detalles del Evento</h3>");
        html.append("<p><strong>Fecha del presupuesto:</strong> ").append(fecha).append("</p>");
        html.append("<p><strong>Horario preferido:</strong> ").append(horario).append("</p>");
        html.append("<p><strong>Paquete seleccionado:</strong> ").append(sesion.getPaqueteNombre()).append("</p>");
        html.append("<p><strong>Precio del paquete:</strong> $").append(String.format("%.2f", sesion.getPaquetePrecio())).append(" MXN</p>");
        html.append("</div>");
        
        // Extras mejorados
        if (sesion.tieneExtras()) {
            html.append("<div class='section'>");
            html.append("<h3>✨ Extras Incluidos</h3>");
            
            java.util.List<modelos.Extra> extras = sesion.getExtrasSeleccionados();
            for (modelos.Extra extra : extras) {
                if (extra.getCantidad() > 0) {
                    html.append("<div class='extra-item'>");
                    html.append("<div>");
                    html.append("<span class='extra-name'>").append(extra.getNombre()).append("</span>");
                    html.append("<div class='extra-quantity'>Cantidad: ").append(extra.getCantidad()).append("</div>");
                    html.append("</div>");
                    html.append("<div class='extra-price'>$").append(String.format("%.2f", extra.getPrecio() * extra.getCantidad())).append(" MXN</div>");
                    html.append("</div>");
                }
            }
            
            // Subtotal de extras
            html.append("<div style='margin-top: 15px; padding-top: 15px; border-top: 2px solid #3498db; text-align: right;'>");
            html.append("<strong style='color: #2c3e50; font-size: 16px;'>Subtotal Extras: $").append(String.format("%.2f", sesion.getTotalExtras())).append(" MXN</strong>");
            html.append("</div>");
            html.append("</div>");
        }
        
        // Términos de pago
        html.append("<div class='section'>");
        html.append("<h3>💳 Términos de Pago</h3>");
        html.append("<p><strong>Plazos:</strong> ").append(plazosItem.getText()).append("</p>");
        html.append("<p><strong>Método de pago:</strong> ").append(metodoPagoItem.getText()).append("</p>");
        html.append("</div>");
        
        // Total
        html.append("<div class='total-section'>");
        html.append("<h2 style='margin: 0; font-size: 28px;'>💰 TOTAL: $").append(String.format("%.2f", sesion.getTotalGeneral())).append(" MXN</h2>");
        html.append("</div>");
        
        // Notas importantes
        html.append("<div class='info-box'>");
        html.append("<h3 style='color: #0c5460; margin-top: 0;'>📋 Información Importante</h3>");
        html.append("<ul style='color: #155724; line-height: 1.8;'>");
        html.append("<li>Este presupuesto es válido por 30 días</li>");
        html.append("<li>Se requiere el 50% de anticipo para confirmar la reserva</li>");
        html.append("<li>El evento incluye todos los servicios especificados</li>");
        html.append("<li>Para dudas o cambios, contáctanos inmediatamente</li>");
        html.append("</ul>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='text-align: center; margin-top: 40px; padding-top: 30px; border-top: 1px solid #dee2e6;'>");
        html.append("<h3 style='color: #2c3e50; margin-bottom: 10px;'>¡Gracias por elegirnos!</h3>");
        html.append("<p style='color: #6c757d; font-size: 16px; margin: 5px 0;'>SEGUNDO CASTILLO - Hacemos de tu evento algo especial</p>");
        html.append("<p style='color: #adb5bd; font-size: 12px; margin-top: 20px;'>Este es un email automático, por favor no responder directamente.</p>");
        html.append("</div>");
        
        html.append("</div></div></body></html>");
        
        return html.toString();
    }
    
    // ========== MÉTODO PARA VERIFICAR QUE EL PRESUPUESTO SE GUARDÓ CORRECTAMENTE ==========
private void verificarPresupuestoGuardado(SesionTemporal sesion) {
    try (Connection conn = Conexion.conectar()) {
        System.out.println("\n🔍 === VERIFICANDO PRESUPUESTO RECIÉN GUARDADO ===");
        
        String sql = """
            SELECT 
                id,
                numero_presupuesto,
                cliente_nombre,
                fecha_creacion,
                total_general,
                CASE WHEN archivo_pdf_contenido IS NOT NULL THEN LENGTH(archivo_pdf_contenido) ELSE 0 END as tamaño_pdf
            FROM presupuestos 
            WHERE cliente_id = ? 
            ORDER BY fecha_creacion DESC 
            LIMIT 1
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sesion.getClienteId());
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("✅ PRESUPUESTO ENCONTRADO EN BD:");
            System.out.println("   ID: " + rs.getInt("id"));
            System.out.println("   Número: " + rs.getString("numero_presupuesto"));
            System.out.println("   Cliente: " + rs.getString("cliente_nombre"));
            System.out.println("   Fecha: " + rs.getTimestamp("fecha_creacion"));
            System.out.println("   Total: $" + rs.getDouble("total_general"));
            System.out.println("   Tamaño PDF: " + rs.getLong("tamaño_pdf") + " bytes");
            
            // Verificar cliente en tabla clientes
            String sqlCliente = "SELECT id, nombre, apellido_paterno FROM clientes WHERE id = ?";
            PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente);
            stmtCliente.setInt(1, sesion.getClienteId());
            ResultSet rsCliente = stmtCliente.executeQuery();
            
            if (rsCliente.next()) {
                System.out.println("   Cliente en BD: " + rsCliente.getString("nombre") + " " + rsCliente.getString("apellido_paterno"));
                System.out.println("✅ ¡PRESUPUESTO GUARDADO CORRECTAMENTE!");
            } else {
                System.out.println("❌ PROBLEMA: Cliente no existe en tabla clientes");
            }
            
        } else {
            System.out.println("❌ PROBLEMA: NO SE ENCONTRÓ EL PRESUPUESTO RECIÉN GUARDADO");
            System.out.println("   Cliente ID buscado: " + sesion.getClienteId());
            
            // Verificar si hay algún presupuesto reciente
            String sqlReciente = "SELECT COUNT(*) as total FROM presupuestos WHERE fecha_creacion >= DATE_SUB(NOW(), INTERVAL 1 HOUR)";
            PreparedStatement stmtReciente = conn.prepareStatement(sqlReciente);
            ResultSet rsReciente = stmtReciente.executeQuery();
            
            if (rsReciente.next()) {
                int total = rsReciente.getInt("total");
                System.out.println("   Presupuestos creados en la última hora: " + total);
            }
        }
        
        System.out.println("🔍 === FIN VERIFICACIÓN ===\n");
        
    } catch (SQLException e) {
        System.err.println("❌ Error en verificación: " + e.getMessage());
    }
}

    
    @FXML
    private void accionRegresar(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/PaquetesPresupuesto.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setFullScreen(true); // si quieres pantalla completa
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        try {
            Alert alerta = new Alert(tipo);
            alerta.setTitle("SEGUNDO CASTILLO");
            alerta.setHeaderText(null);
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        } catch (Exception e) {
            System.err.println("VistaPreviaController: Error al mostrar alerta: " + e.getMessage());
        }
    }
}