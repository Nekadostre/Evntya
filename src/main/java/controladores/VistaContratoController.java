package controladores;

import database.Conexion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelos.SesionTemporal;
import utils.GeneradorPDF;

// ✅ IMPORTS DE JAVAMAIL HABILITADOS
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;
import modelos.Extra;

public class VistaContratoController {
    
    private static final Logger LOGGER = Logger.getLogger(VistaContratoController.class.getName());
    
    // ✅ CONFIGURACIÓN DE EMAIL HOSTINGER
    private static final String EMAIL_EMPRESA = "bienvenido@forgestudio.com.mx";
    private static final String PASSWORD_EMAIL = "Eventya321@";
    private static final String NOMBRE_EMPRESA = "SEGUNDO CASTILLO";
    
    // Elementos FXML
    @FXML private Label lblFechaContrato;
    @FXML private Label lblFechaEvento;
    @FXML private TextField txtFestejado;
    @FXML private Label lblNombre;
    @FXML private Label lblPaquete;
    @FXML private Label lblHorario;
    @FXML private Label lblPago;
    @FXML private Label lblPlazos;
    @FXML private Label lblExtras;
    @FXML private Label lblTotal;
    
    // Variables de control
    private final SesionTemporal sesion = SesionTemporal.getInstancia();
    private String nombreFestejado = "";
    private LocalDate fechaContrato = LocalDate.now();
    private String ultimoPDFGenerado = null;
    
    @FXML
public void initialize() {
    configurarFechas();
    cargarDatosSesion();
    
    // Agregar esta línea para depuración
    mostrarInfoCliente();
    
    if (txtFestejado != null) {
        txtFestejado.textProperty().addListener((observable, oldValue, newValue) -> {
            nombreFestejado = newValue != null ? newValue.trim() : "";
        });
    }
}
    
    public void refrescarDatos() {
        cargarDatosSesion();
    }
    
    private void mostrarInfoCliente() {
    try {
        System.out.println("\n=== INFORMACIÓN DEL CLIENTE ===");
        System.out.println("ID Cliente: " + sesion.getClienteId());
        System.out.println("Nombre: " + sesion.getClienteNombreCompleto());
        
        try {
            String email = sesion.getClienteEmail();
            System.out.println("Email desde sesión: " + email);
        } catch (Exception e) {
            System.out.println("Error obteniendo email desde sesión: " + e.getMessage());
        }
        
        // Verificar en BD
        String emailBD = obtenerEmailDesdeBD();
        System.out.println("Email desde BD: " + emailBD);
        System.out.println("===============================\n");
        
    } catch (Exception e) {
        System.err.println("Error mostrando info del cliente: " + e.getMessage());
    }
}

// ========== 3. AGREGAR ESTE MÉTODO NUEVO ==========
private String obtenerEmailDesdeBD() {
    try (Connection conn = Conexion.conectar()) {
        if (conn == null) {
            System.err.println("❌ No hay conexión a BD");
            return null;
        }
        
        // Obtener email directamente por ID del cliente
        String sql = "SELECT email FROM clientes WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sesion.getClienteId());
        
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            String email = rs.getString("email");
            System.out.println("📧 Email obtenido de BD: " + email);
            return email;
        } else {
            System.out.println("❌ No se encontró cliente con ID: " + sesion.getClienteId());
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error al consultar email en BD: " + e.getMessage());
        return null;
    }
}
    
    private void cargarDatosSesion() {
        try {
            System.out.println("=== CARGANDO DATOS EN VISTA PREVIA ===");
            
            // Cargar datos del cliente
            if (sesion.hayClienteSeleccionado()) {
                String nombreCliente = sesion.getClienteNombreCompleto();
                if (lblNombre != null) {
                    lblNombre.setText(nombreCliente);
                }
            } else {
                if (lblNombre != null) {
                    lblNombre.setText("No hay cliente seleccionado");
                }
                mostrarError("Error", "No hay cliente seleccionado en la sesión");
                return;
            }
            
            // Cargar datos del paquete
            if (sesion.hayPaqueteSeleccionado()) {
                String textoPaquete = sesion.getPaqueteNombre() + " - $" + 
                                    String.format("%.2f", sesion.getPaquetePrecio());
                if (lblPaquete != null) {
                    lblPaquete.setText(textoPaquete);
                }
            } else {
                if (lblPaquete != null) {
                    lblPaquete.setText("No hay paquete seleccionado");
                }
            }
            
            // Cargar horario del evento
            String horario = null;
            try {
                horario = sesion.getHorarioEvento();
            } catch (Exception e) {
                try {
                    horario = sesion.getHorarioPresupuesto();
                } catch (Exception e2) {
                    // Horario no disponible
                }
            }
            
            if (lblHorario != null) {
                if (horario != null && !horario.trim().isEmpty()) {
                    String horarioMostrar = "";
                    if (horario.toLowerCase().contains("matutino") || horario.toLowerCase().contains("mañana")) {
                        horarioMostrar = "MATUTINO";
                    } else if (horario.toLowerCase().contains("vespertino") || horario.toLowerCase().contains("tarde")) {
                        horarioMostrar = "VESPERTINO";
                    } else {
                        horarioMostrar = horario.toUpperCase();
                    }
                    lblHorario.setText(horarioMostrar);
                } else {
                    lblHorario.setText("No especificado");
                }
            }
            
            // Cargar forma de pago
            String formaPago = null;
            try {
                formaPago = sesion.getFormaPagoPresupuesto();
            } catch (Exception e) {
                // Forma de pago no disponible
            }
            
            if (lblPago != null) {
                if (formaPago != null && !formaPago.trim().isEmpty()) {
                    lblPago.setText(formaPago);
                } else {
                    lblPago.setText("No especificado");
                }
            }
            
            // Cargar plazos
            String plazos = null;
            try {
                plazos = sesion.getPlazosPresupuesto();
            } catch (Exception e) {
                // Plazos no disponibles
            }
            
            if (lblPlazos != null) {
                if (plazos != null && !plazos.trim().isEmpty()) {
                    lblPlazos.setText(plazos);
                } else {
                    lblPlazos.setText("No especificado");
                }
            }
            
            // Cargar extras
            if (lblExtras != null) {
                String extrasTexto = cargarExtrasCorregido();
                lblExtras.setText(extrasTexto);
            }
            
            // Cargar total
            double total = sesion.getTotalGeneral();
            if (lblTotal != null) {
                lblTotal.setText(String.format("%.2f", total));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de la sesión", e);
            mostrarError("Error", "Error al cargar los datos: " + e.getMessage());
        }
    }
    
    private String cargarExtrasCorregido() {
        try {
            double totalGeneral = sesion.getTotalGeneral();
            double precioPaquete = sesion.getPaquetePrecio();
            double diferencia = totalGeneral - precioPaquete;
            
            // Buscar extras en base de datos
            String extrasDesdeDB = detectarExtrasDesdePresupuesto();
            if (extrasDesdeDB != null && !extrasDesdeDB.trim().isEmpty() && 
                !extrasDesdeDB.equals("Sin extras")) {
                return extrasDesdeDB;
            }
            
            // Intentar con métodos de sesión
            try {
                if (sesion.tieneExtras() && sesion.getExtrasSeleccionados() != null) {
                    StringBuilder extrasTexto = new StringBuilder();
                    boolean hayExtras = false;
                    
                    for (Extra extra : sesion.getExtrasSeleccionados()) {
                        if (extra.getCantidad() > 0) {
                            if (hayExtras) extrasTexto.append("; ");
                            extrasTexto.append(extra.getNombre())
                                      .append(" x").append(extra.getCantidad())
                                      .append(" ($").append(String.format("%.2f", extra.getPrecio() * extra.getCantidad()))
                                      .append(")");
                            hayExtras = true;
                        }
                    }
                    
                    if (hayExtras) {
                        return extrasTexto.toString();
                    }
                }
            } catch (Exception e) {
                // Continuar con otros métodos
            }
            
            // Detectar por diferencia de precio
            if (Math.abs(diferencia) >= 0.01) {
                return "Extras incluidos (Total: $" + String.format("%.2f", diferencia) + ")";
            }
            
            return "Sin extras";
            
        } catch (Exception e) {
            return "Error al cargar extras";
        }
    }
    
    private String detectarExtrasDesdePresupuesto() {
        try (Connection conn = Conexion.conectar()) {
            if (conn == null) return null;
            
            String nombreCliente = sesion.getClienteNombreCompleto();
            String sql = "SELECT extras_detalle, extras, total_extras, total_general, paquete_precio " +
                        "FROM presupuestos WHERE cliente_nombre = ? ORDER BY fecha_creacion DESC LIMIT 1";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombreCliente);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String extrasDetalle = rs.getString("extras_detalle");
                String extras = rs.getString("extras");
                double totalExtras = rs.getDouble("total_extras");
                
                if (extrasDetalle != null && !extrasDetalle.trim().isEmpty() && 
                    !extrasDetalle.equals("Sin extras")) {
                    return extrasDetalle;
                }
                
                if (extras != null && !extras.trim().isEmpty() && 
                    !extras.equals("Sin extras")) {
                    return extras;
                }
                
                if (totalExtras > 0.01) {
                    return "Extras incluidos (Total: $" + String.format("%.2f", totalExtras) + ")";
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar extras: " + e.getMessage());
        }
        
        return null;
    }
    
    private void configurarFechas() {
        fechaContrato = LocalDate.now();
        if (lblFechaContrato != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblFechaContrato.setText(fechaContrato.format(formatter));
        }
        
        if (sesion.getFechaEvento() != null && lblFechaEvento != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblFechaEvento.setText(sesion.getFechaEvento().format(formatter));
        }
    }
    
    @FXML
    private void nomFestejado() {
        if (txtFestejado != null) {
            nombreFestejado = txtFestejado.getText().trim();
        }
    }
    
    @FXML
    private void imprimirContrato() {
        if (!validarDatos()) {
            return;
        }
        
        try {
            // Crear carpeta Contratos si no existe
            File carpetaContratos = new File("Contratos");
            if (!carpetaContratos.exists()) {
                carpetaContratos.mkdirs();
                System.out.println("✅ Carpeta 'Contratos' creada");
            }
            
            // Generar nombre del archivo
            String nombreCliente = sesion.getClienteNombreCompleto().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nombreArchivo = "Contrato_" + nombreCliente + "_" + fecha + ".pdf";
            
            // Ruta completa donde se guardará el archivo
            String rutaCompleta = "Contratos/" + nombreArchivo;
            File archivoContrato = new File(rutaCompleta);
            
            // Generar el PDF usando la utilidad existente
            File pdfGenerado = GeneradorPDF.generarContratoPDF(
                sesion, nombreFestejado, fechaContrato, archivoContrato.getAbsolutePath()
            );
            
            // Guardar la ruta del PDF generado para envío por email
            ultimoPDFGenerado = pdfGenerado.getAbsolutePath();
            
            // Guardar en base de datos
            guardarContratoEnBD(pdfGenerado.getAbsolutePath());
            
            Alert exito = new Alert(Alert.AlertType.INFORMATION);
            exito.setTitle("Contrato Generado");
            exito.setHeaderText("✅ Contrato PDF generado exitosamente");
            exito.setContentText("El contrato se ha guardado como PDF en:\n" + pdfGenerado.getAbsolutePath() + 
                                "\n\n✅ También se registró en la base de datos y se creó la reserva automáticamente." +
                                "\n\n📁 Ubicación: Carpeta 'Contratos'");
            exito.show();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar contrato PDF", e);
            mostrarError("Error", "Error al generar el contrato PDF: " + e.getMessage());
        }
    }
    
@FXML
private void sendtomail() {
    System.out.println("📧 === INICIANDO ENVÍO DE CONTRATO POR EMAIL ===");
    
    try {
        if (!validarDatos()) {
            return;
        }

        SesionTemporal sesion = SesionTemporal.getInstancia();
        
        // ========== OBTENER EMAIL DIRECTAMENTE DE LA BD CON COLUMNA CORRECTA ==========
        String emailCliente = verificarEmailEnBD(sesion.getClienteId());
        
        System.out.println("🔍 DEBUG: Email obtenido de BD: '" + emailCliente + "'");
        
        if (emailCliente != null && !emailCliente.trim().isEmpty() && 
            !emailCliente.equalsIgnoreCase("NULL") && 
            !emailCliente.equals("Sin email") && 
            !emailCliente.equals("No registrado") &&
            esEmailValido(emailCliente.trim())) {
            
            // ========== EMAIL VÁLIDO ENCONTRADO - USAR DIRECTAMENTE ==========
            emailCliente = emailCliente.trim();
            System.out.println("✅ USANDO EMAIL DE BD SIN PREGUNTAR: " + emailCliente);
            
        } else {
            // ========== NO HAY EMAIL VÁLIDO - SOLICITAR MANUALMENTE ==========
            System.out.println("❌ NO HAY EMAIL VÁLIDO EN BD, SOLICITANDO MANUALMENTE");
            System.out.println("Motivo: Email obtenido = '" + emailCliente + "'");
            
            emailCliente = solicitarEmailManual("");
            if (emailCliente == null) return; // Usuario canceló
        }

        System.out.println("📧 Email final a usar: '" + emailCliente + "'");

        // ========== VERIFICAR O GENERAR PDF ==========
        if (ultimoPDFGenerado == null || !new File(ultimoPDFGenerado).exists()) {
            System.out.println("📄 Generando PDF para envío...");
            
            if (!generarPDFTemporal()) {
                mostrarError("Error", "No se pudo generar el PDF para enviar");
                return;
            }
        }

        System.out.println("📄 PDF listo para envío: " + ultimoPDFGenerado);

        // ========== ENVIAR EMAIL ==========
        boolean emailEnviado = enviarEmailConContrato(emailCliente, ultimoPDFGenerado);

        if (emailEnviado) {
            Alert exito = new Alert(Alert.AlertType.INFORMATION);
            exito.setTitle("Email Enviado");
            exito.setHeaderText("✅ Contrato enviado exitosamente");
            exito.setContentText("El contrato se ha enviado por email a:\n" + emailCliente + 
                                "\n\nCliente: " + sesion.getClienteNombreCompleto() +
                                "\n\nEl cliente recibirá el contrato en su bandeja de entrada.");
            exito.show();
            
            System.out.println("✅ EMAIL ENVIADO EXITOSAMENTE");
        } else {
            mostrarError("Error de Email", "No se pudo enviar el email. Verifique la configuración de correo.");
        }

    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error al enviar email", e);
        mostrarError("Error", "Error inesperado al enviar email: " + e.getMessage());
    }
}

    
// ========== MÉTODO PARA VERIFICAR EMAIL DIRECTAMENTE EN BD ==========
private String verificarEmailEnBD(int clienteId) {
    try (Connection conn = Conexion.conectar()) {
        // USAR 'correo' en lugar de 'email'
        String sql = "SELECT correo FROM clientes WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, clienteId);
        
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            String email = rs.getString("correo"); // COLUMNA CORRECTA
            System.out.println("📧 Email directo de BD: '" + email + "'");
            return email;
        } else {
            System.out.println("❌ No se encontró cliente con ID: " + clienteId);
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error consultando BD: " + e.getMessage());
        return null;
    }
}
    // ========== MÉTODO: SOLICITAR EMAIL MANUAL ==========

private String solicitarEmailManual(String emailSugerido) {
    TextInputDialog dialog = new TextInputDialog(emailSugerido);
    dialog.setTitle("Enviar Contrato por Email");
    dialog.setHeaderText("Ingrese el email del cliente:");
    dialog.setContentText("Email:");
    
    Optional<String> result = dialog.showAndWait();
    if (result.isPresent() && !result.get().trim().isEmpty()) {
        String email = result.get().trim();
        if (esEmailValido(email)) {
            return email;
        } else {
            mostrarError("Email Inválido", "Por favor ingrese un email válido.");
            return null;
        }
    }
    
    return null; // Usuario canceló
}
    
    // ========== MÉTODO: VALIDAR EMAIL ==========
private boolean esEmailValido(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    return email.matches(emailPattern);
}
    
    // ========== MÉTODO: GENERAR PDF TEMPORAL ==========
    private boolean generarPDFTemporal() {
        try {
            // Crear carpeta Contratos si no existe (usar la misma carpeta principal)
            File carpetaContratos = new File("Contratos");
            if (!carpetaContratos.exists()) {
                carpetaContratos.mkdirs();
                System.out.println("✅ Carpeta 'Contratos' creada");
            }
            
            // Generar nombre del archivo con timestamp para evitar conflictos
            String nombreCliente = sesion.getClienteNombreCompleto().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
            String nombreArchivo = "Contrato_" + nombreCliente + "_" + fecha + "_" + timestamp + ".pdf";
            
            String rutaCompleta = "Contratos/" + nombreArchivo;
            
            File pdfGenerado = GeneradorPDF.generarContratoPDF(
                sesion, nombreFestejado, fechaContrato, rutaCompleta
            );
            
            ultimoPDFGenerado = pdfGenerado.getAbsolutePath();
            System.out.println("✅ PDF temporal generado en: " + ultimoPDFGenerado);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF temporal", e);
            return false;
        }
    }
    
    // ========== MÉTODO: ENVIAR EMAIL CON CONTRATO ==========
    private boolean enviarEmailConContrato(String emailCliente, String rutaPDF) {
        try {
            System.out.println("📧 Configurando email con Hostinger...");
            
            // Configuración del servidor SMTP (Hostinger)
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.hostinger.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.hostinger.com");
            
            // Debug y timeouts
            props.put("mail.debug", "false");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
            
            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_EMPRESA, PASSWORD_EMAIL);
                }
            });
            
            // Crear mensaje
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(EMAIL_EMPRESA, NOMBRE_EMPRESA));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailCliente));
            mensaje.setSubject("Contrato de Evento - " + sesion.getClienteNombreCompleto() + " - " + NOMBRE_EMPRESA);
            
            // Crear contenido multipart
            Multipart multipart = new MimeMultipart();
            
            // Parte 1: Contenido HTML del email
            MimeBodyPart textoParte = new MimeBodyPart();
            String contenidoHTML = crearContenidoEmailContrato();
            textoParte.setContent(contenidoHTML, "text/html; charset=utf-8");
            multipart.addBodyPart(textoParte);
            
            // Parte 2: Adjuntar PDF del contrato
            MimeBodyPart adjuntoParte = new MimeBodyPart();
            File archivoPDF = new File(rutaPDF);
            if (archivoPDF.exists()) {
                adjuntoParte.attachFile(archivoPDF);
                adjuntoParte.setFileName("Contrato_" + sesion.getClienteNombreCompleto().replace(" ", "_") + ".pdf");
                multipart.addBodyPart(adjuntoParte);
            }
            
            mensaje.setContent(multipart);
            
            // Enviar email
            System.out.println("📧 Enviando email via Hostinger...");
            Transport.send(mensaje);
            
            System.out.println("✅ Email de contrato enviado exitosamente a: " + emailCliente);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email de contrato: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== MÉTODO: CREAR CONTENIDO HTML DEL EMAIL ==========
    private String crearContenidoEmailContrato() {
        String horario = "";
        try {
            horario = sesion.getHorarioEvento();
            if (horario == null || horario.trim().isEmpty()) {
                horario = sesion.getHorarioPresupuesto();
            }
            if (horario == null) horario = "No especificado";
        } catch (Exception e) {
            horario = "No especificado";
        }
        
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String fechaEvento = "";
        try {
            if (sesion.getFechaEvento() != null) {
                fechaEvento = sesion.getFechaEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                fechaEvento = "Por definir";
            }
        } catch (Exception e) {
            fechaEvento = "Por definir";
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 700px; margin: 0 auto; background-color: white; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%); color: white; padding: 40px 30px; text-align: center; }");
        html.append(".content { padding: 40px 30px; }");
        html.append(".section { margin-bottom: 30px; padding: 20px; background-color: #f8f9fa; border-radius: 10px; border-left: 4px solid #e74c3c; }");
        html.append(".section h3 { margin-top: 0; color: #2c3e50; font-size: 18px; }");
        html.append(".contrato-section { background: linear-gradient(135deg, #e74c3c, #c0392b); color: white; padding: 25px; text-align: center; border-radius: 10px; margin: 20px 0; }");
        html.append(".info-box { background-color: #d4edda; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; }");
        html.append(".important-note { background-color: #fff3cd; padding: 15px; border-radius: 8px; border-left: 4px solid #ffc107; margin: 20px 0; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1 style='margin: 0; font-size: 32px;'>📄 ").append(NOMBRE_EMPRESA).append("</h1>");
        html.append("<p style='margin: 10px 0 0 0; font-size: 18px; opacity: 0.9;'>Contrato de Evento</p>");
        html.append("</div>");
        
        // Contenido
        html.append("<div class='content'>");
        
        html.append("<h2 style='color: #2c3e50; margin-bottom: 20px;'>¡Felicidades ").append(sesion.getClienteNombreCompleto()).append("!</h2>");
        
        html.append("<p style='font-size: 16px; color: #495057; line-height: 1.6;'>Su contrato de evento ha sido generado exitosamente. En el archivo adjunto encontrará todos los detalles legales y términos del servicio contratado.</p>");
        
        // Detalles del contrato
        html.append("<div class='contrato-section'>");
        html.append("<h2 style='margin: 0; font-size: 24px;'>📋 CONTRATO CONFIRMADO</h2>");
        html.append("<p style='margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;'>Su evento está oficialmente reservado</p>");
        html.append("</div>");
        
        // Detalles del evento
        html.append("<div class='section'>");
        html.append("<h3>🎉 Detalles del Evento</h3>");
        html.append("<p><strong>Cliente:</strong> ").append(sesion.getClienteNombreCompleto()).append("</p>");
        html.append("<p><strong>Festejado:</strong> ").append(nombreFestejado.isEmpty() ? "Por definir" : nombreFestejado).append("</p>");
        html.append("<p><strong>Fecha del evento:</strong> ").append(fechaEvento).append("</p>");
        html.append("<p><strong>Horario:</strong> ").append(horario).append("</p>");
        html.append("<p><strong>Paquete contratado:</strong> ").append(sesion.getPaqueteNombre()).append("</p>");
        
        // Mostrar extras si los hay
        String extrasTexto = cargarExtrasCorregido();
        if (!extrasTexto.equals("Sin extras")) {
            html.append("<p><strong>Servicios adicionales:</strong> ").append(extrasTexto).append("</p>");
        }
        
        html.append("</div>");
        
        // Información financiera
        html.append("<div class='section'>");
        html.append("<h3>💰 Información Financiera</h3>");
        html.append("<p><strong>Monto total del contrato:</strong> $").append(String.format("%.2f", sesion.getTotalGeneral())).append(" MXN</p>");
        
        String formaPago = "";
        try {
            formaPago = sesion.getFormaPagoPresupuesto();
            if (formaPago != null && !formaPago.trim().isEmpty()) {
                html.append("<p><strong>Forma de pago:</strong> ").append(formaPago).append("</p>");
            }
        } catch (Exception e) {
            // Forma de pago no disponible
        }
        
        String plazos = "";
        try {
            plazos = sesion.getPlazosPresupuesto();
            if (plazos != null && !plazos.trim().isEmpty()) {
                html.append("<p><strong>Plazos de pago:</strong> ").append(plazos).append("</p>");
            }
        } catch (Exception e) {
            // Plazos no disponibles
        }
        
        html.append("</div>");
        
        // Nota importante
        html.append("<div class='important-note'>");
        html.append("<h3 style='color: #856404; margin: 0 0 10px 0;'>⚠️ Información Importante</h3>");
        html.append("<p style='color: #856404; margin: 0;'>El contrato adjunto debe ser firmado y devuelto para confirmar definitivamente la reserva. Mantenga una copia para sus records.</p>");
        html.append("</div>");
        
        // Información de contacto
        html.append("<div class='info-box'>");
        html.append("<h3 style='color: #155724; margin-top: 0;'>📞 Contacto Directo</h3>");
        html.append("<p style='color: #155724; margin: 5px 0;'><strong>Email:</strong> ").append(EMAIL_EMPRESA).append("</p>");
        html.append("<p style='color: #155724; margin: 5px 0;'><strong>Empresa:</strong> ").append(NOMBRE_EMPRESA).append("</p>");
        html.append("<p style='color: #155724; margin: 15px 0 0 0; font-weight: bold;'>¡Estamos aquí para hacer de su evento algo especial!</p>");
        html.append("</div>");
        
        html.append("<p style='text-align: center; color: #7f8c8d; margin: 30px 0 20px 0;'>Gracias por confiar en <strong>").append(NOMBRE_EMPRESA).append("</strong> para su evento especial.</p>");
        
        html.append("<p style='text-align: center;'>Saludos cordiales,<br>");
        html.append("<strong style='color: #e74c3c; font-size: 18px;'>Equipo ").append(NOMBRE_EMPRESA).append("</strong></p>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>");
        html.append("<p>Este contrato fue generado automáticamente el ");
        html.append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")));
        html.append("</p>");
        html.append("<p>© ").append(java.time.Year.now()).append(" ").append(NOMBRE_EMPRESA).append(" - Todos los derechos reservados</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    @FXML
    private void exit() {
        try {
            sesion.reset();
            App.setRoot("PanelPrincipal");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al salir", e);
            mostrarError("Error", "Error al regresar al menú principal");
        }
    }
    
    @FXML
    private void regresar() {
        try {
            App.setRoot("CalendarioContrato");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al regresar", e);
            mostrarError("Error", "Error al regresar al calendario");
        }
    }
    
    private boolean validarDatos() {
        StringBuilder errores = new StringBuilder();
        
        if (!sesion.hayClienteSeleccionado()) {
            errores.append("• No hay cliente seleccionado\n");
        }
        
        if (!sesion.hayPaqueteSeleccionado()) {
            errores.append("• No hay paquete seleccionado\n");
        }
        
        if (sesion.getFechaEvento() == null) {
            errores.append("• No hay fecha de evento seleccionada\n");
        }
        
        if (nombreFestejado == null || nombreFestejado.trim().isEmpty()) {
            errores.append("• Falta el nombre del festejado\n");
        }
        
        if (fechaContrato == null) {
            errores.append("• Falta la fecha del contrato\n");
        }
        
        if (errores.length() > 0) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Datos incompletos");
            alerta.setHeaderText("Faltan los siguientes datos:");
            alerta.setContentText(errores.toString());
            alerta.show();
            return false;
        }
        
        return true;
    }
    
   // ========== MÉTODO CORREGIDO: GUARDAR CONTRATO CON PDF COMO BLOB ==========
private void guardarContratoEnBD(String rutaArchivo) {
    try (Connection conn = Conexion.conectar()) {
        
        // ✅ LEER EL ARCHIVO PDF COMO BYTES
        byte[] pdfBytes = null;
        try {
            java.nio.file.Path pdfPath = java.nio.file.Paths.get(rutaArchivo);
            pdfBytes = java.nio.file.Files.readAllBytes(pdfPath);
            System.out.println("✅ PDF leído para base de datos: " + pdfBytes.length + " bytes");
        } catch (Exception e) {
            System.err.println("❌ Error leyendo PDF: " + e.getMessage());
            // Continuar sin el archivo, pero guardar el registro
        }
        
        // ✅ SQL CON BLOB - AGREGAMOS archivo_pdf_contenido
        String sql = "INSERT INTO contratos (cliente_id, paquete_id, fecha_contrato, fecha_evento, " +
                    "nombre_festejado, horario, total, estado, metodo_pago, archivo_ruta, " +
                    "archivo_pdf_contenido, fecha_creacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'firmado', ?, ?, ?, NOW())";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sesion.getClienteId());
        stmt.setInt(2, sesion.getPaqueteId());
        stmt.setDate(3, java.sql.Date.valueOf(fechaContrato));
        stmt.setDate(4, java.sql.Date.valueOf(sesion.getFechaEvento()));
        stmt.setString(5, nombreFestejado);
        
        String horario = "";
        try {
            horario = sesion.getHorarioEvento();
            if (horario == null || horario.trim().isEmpty()) {
                horario = sesion.getHorarioPresupuesto();
            }
        } catch (Exception e) {
            horario = "matutino"; // valor por defecto
        }
        stmt.setString(6, horario);
        
        stmt.setDouble(7, sesion.getTotalGeneral());
        
        String metodoPago = "";
        try {
            metodoPago = sesion.getFormaPagoPresupuesto();
            if (metodoPago == null || metodoPago.trim().isEmpty()) {
                metodoPago = "No especificado";
            }
        } catch (Exception e) {
            metodoPago = "No especificado";
        }
        stmt.setString(8, metodoPago);
        stmt.setString(9, rutaArchivo);
        
        // ✅ AQUÍ GUARDAMOS EL PDF COMPLETO COMO BLOB
        if (pdfBytes != null) {
            stmt.setBytes(10, pdfBytes);
        } else {
            stmt.setNull(10, java.sql.Types.LONGVARBINARY);
        }
        
        int filasAfectadas = stmt.executeUpdate();
        if (filasAfectadas > 0) {
            crearReservaDesdeContrato();
            System.out.println("✅ Contrato guardado en base de datos");
            System.out.println("📁 Ruta del archivo: " + rutaArchivo);
            
            if (pdfBytes != null) {
                System.out.println("💾 Tamaño del PDF en BD: " + pdfBytes.length + " bytes");
                System.out.println("✅ ¡PDF disponible desde cualquier computadora!");
            }
            
            mostrarExtrasEnConsola();
            mostrarUbicacionArchivo(rutaArchivo);
        }
        
    } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Error al guardar contrato en BD", e);
        mostrarError("Error de base de datos", "No se pudo guardar el contrato en la base de datos: " + e.getMessage());
    }
}

// ========== MÉTODO NUEVO: DESCARGAR CONTRATO DESDE BD ==========
public static boolean descargarContratoDesdeBD(int contratoId, String rutaDestino) {
    try (Connection conn = Conexion.conectar()) {
        
        String sql = "SELECT archivo_pdf_contenido, archivo_ruta, cliente_id, nombre_festejado " +
                    "FROM contratos WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, contratoId);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            byte[] pdfBytes = rs.getBytes("archivo_pdf_contenido");
            String rutaOriginal = rs.getString("archivo_ruta");
            String nombreFestejado = rs.getString("nombre_festejado");
            
            if (pdfBytes != null && pdfBytes.length > 0) {
                // Generar nombre del archivo
                String nombreArchivo = "Contrato_" + nombreFestejado.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_") + ".pdf";
                
                // Crear archivo
                java.io.File archivo = new java.io.File(rutaDestino, nombreArchivo);
                java.nio.file.Files.write(archivo.toPath(), pdfBytes);
                
                System.out.println("✅ Contrato descargado: " + archivo.getAbsolutePath());
                return true;
            } else {
                System.err.println("❌ No hay contenido PDF en la base de datos para el contrato ID: " + contratoId);
                return false;
            }
        } else {
            System.err.println("❌ No se encontró el contrato con ID: " + contratoId);
            return false;
        }
        
    } catch (Exception e) {
        System.err.println("❌ Error descargando contrato: " + e.getMessage());
        return false;
    }
}
   
   
// ========== MÉTODO NUEVO: LISTAR CONTRATOS CON PDF ==========
public static void listarContratosConPDF() {
    try (Connection conn = Conexion.conectar()) {
        
        String sql = "SELECT c.id, c.nombre_festejado, cl.nombre as cliente_nombre, " +
                    "c.fecha_evento, c.total, " +
                    "CASE WHEN c.archivo_pdf_contenido IS NOT NULL " +
                    "     THEN 'PDF Disponible' " +
                    "     ELSE 'Sin PDF' " +
                    "END as estado_pdf " +
                    "FROM contratos c " +
                    "JOIN clientes cl ON c.cliente_id = cl.id " +
                    "ORDER BY c.fecha_creacion DESC";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        System.out.println("\n=== CONTRATOS EN BASE DE DATOS ===");
        while (rs.next()) {
            System.out.printf("ID: %d | Cliente: %s | Festejado: %s | Fecha: %s | Total: $%.2f | %s%n",
                rs.getInt("id"),
                rs.getString("cliente_nombre"),
                rs.getString("nombre_festejado"),
                rs.getDate("fecha_evento"),
                rs.getDouble("total"),
                rs.getString("estado_pdf")
            );
        }
        System.out.println("=====================================\n");
        
    } catch (Exception e) {
        System.err.println("❌ Error listando contratos: " + e.getMessage());
    }
} 
    
   private void mostrarExtrasEnConsola() {
    try {
        String extrasDetalle = "";
        double totalExtras = 0.0;
        
        // Obtener extras de la sesión
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
        
        // Si no hay extras en sesión, intentar obtener del presupuesto
        if (extrasDetalle.isEmpty()) {
            try (Connection conn = Conexion.conectar()) {
                String sqlExtras = "SELECT extras_detalle, extras FROM presupuestos WHERE cliente_id = ? ORDER BY fecha_creacion DESC LIMIT 1";
                PreparedStatement stmtExtras = conn.prepareStatement(sqlExtras);
                stmtExtras.setInt(1, sesion.getClienteId());
                ResultSet rsExtras = stmtExtras.executeQuery();
                
                if (rsExtras.next()) {
                    String extrasDetalleDB = rsExtras.getString("extras_detalle");
                    String extrasDB = rsExtras.getString("extras");
                    
                    if (extrasDetalleDB != null && !extrasDetalleDB.trim().isEmpty()) {
                        extrasDetalle = extrasDetalleDB;
                    } else if (extrasDB != null && !extrasDB.trim().isEmpty()) {
                        extrasDetalle = extrasDB;
                    }
                    
                    System.out.println("✅ Extras obtenidos del presupuesto: " + extrasDetalle);
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Error al obtener extras del presupuesto: " + e.getMessage());
            }
        }
        
        // Mostrar información en consola
        System.out.println("✨ Extras del contrato: " + (extrasDetalle.isEmpty() ? "Sin extras" : extrasDetalle));
        if (totalExtras > 0) {
            System.out.println("💰 Total extras: $" + totalExtras);
        }
        System.out.println("💰 Total general del contrato: $" + sesion.getTotalGeneral());
        
    } catch (Exception e) {
        System.err.println("⚠️ Error al mostrar información de extras: " + e.getMessage());
    }
}
    
    // ========== MÉTODO: MOSTRAR UBICACIÓN DEL ARCHIVO ==========
    private void mostrarUbicacionArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                File carpetaPadre = archivo.getParentFile();
                
                // Intentar abrir la carpeta en el explorador de archivos
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                        desktop.open(carpetaPadre);
                        System.out.println("📂 Carpeta 'Contratos' abierta en el explorador");
                    }
                }
            }
        } catch (Exception e) {
            // Si no se puede abrir la carpeta, solo mostrar la ruta en consola
            System.out.println("📁 Archivo guardado en: " + rutaArchivo);
        }
    }
    
    private void crearReservaDesdeContrato() {
        try (Connection conn = Conexion.conectar()) {
            String sqlVerificar = "SELECT COUNT(*) FROM reservas WHERE fecha = ? AND horario = ?";
            PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar);
            stmtVerificar.setDate(1, java.sql.Date.valueOf(sesion.getFechaEvento()));
            
            String horario = "";
            try {
                horario = sesion.getHorarioEvento();
                if (horario == null || horario.trim().isEmpty()) {
                    horario = sesion.getHorarioPresupuesto();
                }
                if (horario == null || horario.trim().isEmpty()) {
                    horario = "matutino";
                }
            } catch (Exception e) {
                horario = "matutino";
            }
            
            stmtVerificar.setString(2, horario.toLowerCase());
            
            ResultSet rs = stmtVerificar.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                String sqlReserva = "INSERT INTO reservas (cliente_id, paquete_id, fecha, horario, estado, metodo_pago, total) " +
                                   "VALUES (?, ?, ?, ?, 'pagado', ?, ?)";
                
                PreparedStatement stmtReserva = conn.prepareStatement(sqlReserva);
                stmtReserva.setInt(1, sesion.getClienteId());
                stmtReserva.setInt(2, sesion.getPaqueteId());
                stmtReserva.setDate(3, java.sql.Date.valueOf(sesion.getFechaEvento()));
                stmtReserva.setString(4, horario.toLowerCase());
                
                String metodoPago = "";
                try {
                    metodoPago = sesion.getFormaPagoPresupuesto();
                    if (metodoPago == null || metodoPago.trim().isEmpty()) {
                        metodoPago = "No especificado";
                    }
                } catch (Exception e) {
                    metodoPago = "No especificado";
                }
                stmtReserva.setString(5, metodoPago);
                
                stmtReserva.setDouble(6, sesion.getTotalGeneral());
                
                stmtReserva.executeUpdate();
                System.out.println("✅ Reserva creada automáticamente");
            }
            
        } catch (SQLException e) {
            System.out.println("⚠️ Error al crear reserva automática: " + e.getMessage());
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}