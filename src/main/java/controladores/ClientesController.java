package controladores;

import database.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import modelos.ClienteContrato;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Imports para manejo de archivos
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;

// Imports para iText PDF
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;

// Imports para JavaMail
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

// Imports para modelos existentes
import modelos.Cliente;
import modelos.Extra;
import java.util.ArrayList;
import java.util.List;

public class ClientesController {

    @FXML private TableView<ClienteContrato> tablaClientes;
    @FXML private TableColumn<ClienteContrato, String> colNombre;
    @FXML private TableColumn<ClienteContrato, String> colApellido;
    @FXML private TableColumn<ClienteContrato, String> colTelefono;
    @FXML private TableColumn<ClienteContrato, String> colEmail;
    @FXML private TableColumn<ClienteContrato, String> colFechaContrato;
    @FXML private TableColumn<ClienteContrato, String> colPaquete;
    @FXML private TableColumn<ClienteContrato, String> colMonto;
    @FXML private TableColumn<ClienteContrato, String> colEstado;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    
    // ELEMENTOS DEL PANEL LATERAL SIMPLIFICADO
    @FXML private VBox infoClienteSeleccionado;
    @FXML private Label lblClienteSeleccionado;
    @FXML private Label lblNombreSeleccionado;
    @FXML private Separator separadorInfo;
    @FXML private VBox panelMensajes;
    @FXML private Label lblTituloMensaje;
    @FXML private ScrollPane scrollMensajes;
    @FXML private VBox contenedorMensajes;

    // CONFIGURACIÓN DE EMAIL - CAMBIAR POR TUS DATOS REALES
    private static final String EMAIL_EMPRESA = "bienvenido@forgestudio.com.mx";
    private static final String PASSWORD_EMAIL = "Eventya321@";
    private static final String NOMBRE_EMPRESA = "ForgeStudio";

    private ObservableList<ClienteContrato> listaClientes = FXCollections.observableArrayList();
    private ClienteContrato clienteActualmenteSeleccionado = null;

    @FXML
    public void initialize() {
        configurarTabla();
        limpiarPresupuestosVencidos();
        cargarDatos();
        configurarSeleccionTabla();
    }

    private void configurarTabla() {
        // Configurar columnas
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFechaContrato.setCellValueFactory(new PropertyValueFactory<>("fechaContrato"));
        colPaquete.setCellValueFactory(new PropertyValueFactory<>("paquete"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar anchos
        colNombre.setPrefWidth(140);
        colApellido.setPrefWidth(160);
        colTelefono.setPrefWidth(150);
        colEmail.setPrefWidth(250);
        colFechaContrato.setPrefWidth(120);
        colPaquete.setPrefWidth(170);
        colMonto.setPrefWidth(120);
        colEstado.setPrefWidth(140);
        
        tablaClientes.getStyleClass().add("tabla-clientes-moderna");
        System.out.println("✅ Tabla configurada correctamente");
    }

    private void configurarSeleccionTabla() {
        // Escuchar cambios en la selección de la tabla
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                clienteActualmenteSeleccionado = newSelection;
                actualizarInfoClienteSeleccionado(newSelection);
            } else {
                clienteActualmenteSeleccionado = null;
                ocultarInfoClienteSeleccionado();
            }
        });
    }

    private void actualizarInfoClienteSeleccionado(ClienteContrato cliente) {
        if (infoClienteSeleccionado != null && lblNombreSeleccionado != null) {
            lblNombreSeleccionado.setText(cliente.getNombreCompleto() + "\n" + 
                                         cliente.getPaquete() + " - " + cliente.getMontoTotal());
            infoClienteSeleccionado.setVisible(true);
            separadorInfo.setVisible(true);
        }
    }

    private void ocultarInfoClienteSeleccionado() {
        if (infoClienteSeleccionado != null) {
            infoClienteSeleccionado.setVisible(false);
            separadorInfo.setVisible(false);
        }
    }

    private void limpiarPresupuestosVencidos() {
        try (Connection conn = Conexion.conectar()) {
            System.out.println("🧹 Limpiando presupuestos vencidos (> 1 mes)...");
            
            String sqlLimpiar = """
                DELETE p, c FROM presupuestos p
                INNER JOIN clientes c ON p.cliente_id = c.id
                WHERE p.fecha_creacion < DATE_SUB(NOW(), INTERVAL 1 MONTH)
                AND NOT EXISTS (
                    SELECT 1 FROM contratos cont WHERE cont.cliente_id = c.id
                )
                """;
            
            PreparedStatement stmt = conn.prepareStatement(sqlLimpiar);
            int eliminados = stmt.executeUpdate();
            
            if (eliminados > 0) {
                System.out.println("🗑️ Se eliminaron " + eliminados + " presupuestos/clientes vencidos");
            } else {
                System.out.println("✅ No hay presupuestos vencidos para eliminar");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al limpiar presupuestos vencidos: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        listaClientes.clear();
        
        try (Connection conn = Conexion.conectar()) {
            System.out.println("=== CARGANDO CLIENTES CON PRESUPUESTOS ACTIVOS ===");
            
            String sql = """
                SELECT 
                    c.id as cliente_id,
                    c.nombre,
                    c.apellido_paterno,
                    c.apellido_materno,
                    c.telefono,
                    c.correo as email,
                    p.fecha_creacion as fecha_presupuesto,
                    paq.nombre as paquete_nombre,
                    p.total_general as monto_presupuesto,
                    CASE 
                        WHEN EXISTS(SELECT 1 FROM contratos cont WHERE cont.cliente_id = c.id) THEN 'Contratado'
                        ELSE 'Presupuesto'
                    END as estado_cliente,
                    DATEDIFF(NOW(), p.fecha_creacion) as dias_desde_presupuesto
                FROM clientes c
                INNER JOIN presupuestos p ON c.id = p.cliente_id
                LEFT JOIN paquetes paq ON p.paquete_id = paq.id
                WHERE p.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
                ORDER BY p.fecha_creacion DESC
                """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            while (rs.next()) {
                ClienteContrato clienteContrato = new ClienteContrato();
                
                clienteContrato.setClienteId(rs.getInt("cliente_id"));
                clienteContrato.setNombre(rs.getString("nombre") != null ? rs.getString("nombre") : "Sin nombre");
                clienteContrato.setApellidoPaterno(rs.getString("apellido_paterno") != null ? rs.getString("apellido_paterno") : "Sin apellido");
                
                String nombreCompleto = (rs.getString("nombre") != null ? rs.getString("nombre") : "") + " " +
                                       (rs.getString("apellido_paterno") != null ? rs.getString("apellido_paterno") : "") + " " +
                                       (rs.getString("apellido_materno") != null ? rs.getString("apellido_materno") : "");
                clienteContrato.setNombreCompleto(nombreCompleto.trim());
                
                String telefono = rs.getString("telefono");
                clienteContrato.setTelefono(telefono != null && !telefono.trim().isEmpty() ? telefono : "Sin teléfono");
                
                String email = rs.getString("email");
                clienteContrato.setEmail(email != null && !email.trim().isEmpty() ? email : "Sin email");
                
                try {
                    java.sql.Date fechaSQL = rs.getDate("fecha_presupuesto");
                    if (fechaSQL != null) {
                        clienteContrato.setFechaContrato(fechaSQL.toLocalDate().format(formatter));
                    } else {
                        clienteContrato.setFechaContrato(LocalDate.now().format(formatter));
                    }
                } catch (SQLException e) {
                    clienteContrato.setFechaContrato(LocalDate.now().format(formatter));
                }
                
                String paqueteNombre = rs.getString("paquete_nombre");
                clienteContrato.setPaquete(paqueteNombre != null ? paqueteNombre : "Sin paquete");
                
                double montoPresupuesto = rs.getDouble("monto_presupuesto");
                String montoFormateado = "$" + String.format("%.0f", montoPresupuesto);
                clienteContrato.setMonto(montoFormateado);
                clienteContrato.setMontoTotal(montoFormateado);
                
                String estadoCliente = rs.getString("estado_cliente");
                int diasDesde = rs.getInt("dias_desde_presupuesto");
                int diasRestantes = 30 - diasDesde;
                
                if (estadoCliente.equals("Contratado")) {
                    clienteContrato.setEstado("Contratado");
                } else {
                    clienteContrato.setEstado("Presupuesto (" + diasRestantes + " días)");
                }
                
                listaClientes.add(clienteContrato);
            }
            
            tablaClientes.setItems(listaClientes);
            lblTotal.setText(String.valueOf(listaClientes.size()));
            
            System.out.println("✅ Cargados " + listaClientes.size() + " clientes con presupuestos activos");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar presupuestos: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error", "No se pudieron cargar los presupuestos: " + e.getMessage(), "#e74c3c");
        }
    }

    // ========== MÉTODOS PARA DATOS COMPLETOS DEL PRESUPUESTO ==========

    private DatosPresupuestoCompleto obtenerDatosPresupuestoCompleto(int clienteId) {
        try (Connection conn = Conexion.conectar()) {
            DatosPresupuestoCompleto datos = new DatosPresupuestoCompleto();
            
            // Consulta principal del presupuesto
            String sqlPresupuesto = """
                SELECT 
                    p.id as presupuesto_id,
                    p.fecha_creacion,
                    p.subtotal,
                    p.iva,
                    p.total_general,
                    paq.nombre as paquete_nombre,
                    paq.descripcion as paquete_descripcion,
                    paq.precio as paquete_precio
                FROM presupuestos p
                LEFT JOIN paquetes paq ON p.paquete_id = paq.id
                WHERE p.cliente_id = ?
                """;
            
            PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
            stmtPresupuesto.setInt(1, clienteId);
            ResultSet rsPresupuesto = stmtPresupuesto.executeQuery();
            
            if (rsPresupuesto.next()) {
                // Datos básicos
                datos.presupuestoId = rsPresupuesto.getInt("presupuesto_id");
                datos.fechaCreacion = rsPresupuesto.getDate("fecha_creacion");
                datos.subtotal = rsPresupuesto.getDouble("subtotal");
                datos.iva = rsPresupuesto.getDouble("iva");
                datos.total = rsPresupuesto.getDouble("total_general");
                datos.paqueteNombre = rsPresupuesto.getString("paquete_nombre");
                datos.paqueteDescripcion = rsPresupuesto.getString("paquete_descripcion");
                datos.paquetePrecio = rsPresupuesto.getDouble("paquete_precio");
                
                // Obtener extras usando la clase Extra existente
                String sqlExtras = """
                    SELECT 
                        e.id as extra_id,
                        e.nombre as extra_nombre,
                        e.descripcion as extra_descripcion,
                        pe.cantidad,
                        pe.precio_unitario,
                        (pe.cantidad * pe.precio_unitario) as subtotal_extra
                    FROM presupuesto_extras pe
                    INNER JOIN extras e ON pe.extra_id = e.id
                    WHERE pe.presupuesto_id = ?
                    ORDER BY e.nombre
                    """;
                
                PreparedStatement stmtExtras = conn.prepareStatement(sqlExtras);
                stmtExtras.setInt(1, datos.presupuestoId);
                ResultSet rsExtras = stmtExtras.executeQuery();
                
                List<Extra> extras = new ArrayList<>();
                while (rsExtras.next()) {
                    // Usar el constructor existente de Extra
                    Extra extra = new Extra(
                        rsExtras.getInt("extra_id"),
                        rsExtras.getString("extra_nombre"),
                        rsExtras.getDouble("precio_unitario"),
                        rsExtras.getInt("cantidad")
                    );
                    extras.add(extra);
                }
                
                datos.extras = extras;
                return datos;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener presupuesto completo: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    // ========== MÉTODOS PARA GENERAR PDF COMPLETO ==========

    private void generarPDFPresupuestoCompleto(ClienteContrato cliente) {
        try {
            // Obtener datos completos del presupuesto
            DatosPresupuestoCompleto datos = obtenerDatosPresupuestoCompleto(cliente.getClienteId());
            
            if (datos == null) {
                mostrarMensaje("Error", "No se pudieron obtener los datos del presupuesto", "#e74c3c");
                return;
            }
            
            // Crear documento PDF
            Document documento = new Document(PageSize.A4);
            
            // Crear carpeta si no existe
            File carpetaPDF = new File("Presupuestos");
            if (!carpetaPDF.exists()) {
                carpetaPDF.mkdirs();
            }
            
            // Nombre del archivo
            String nombreArchivo = "presupuesto_" + 
                                  cliente.getNombreCompleto().replace(" ", "_").replace(".", "") + 
                                  "_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                                  ".pdf";
            
            String rutaCompleta = "Presupuestos/" + nombreArchivo;
            
            // Crear writer
            PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));
            documento.open();
            
            // Fuentes
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLUE);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Font pequenaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            
            // === HEADER ===
            Paragraph titulo = new Paragraph("🎨 FORGESTUDIO", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(5);
            documento.add(titulo);
            
            Paragraph subtitulo = new Paragraph("PRESUPUESTO DE EVENTO", subtituloFont);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);
            
            // === INFORMACIÓN DEL CLIENTE ===
            documento.add(new Paragraph("INFORMACIÓN DEL CLIENTE", subtituloFont));
            documento.add(new Paragraph("Nombre: " + cliente.getNombreCompleto(), normalFont));
            documento.add(new Paragraph("Teléfono: " + cliente.getTelefono(), normalFont));
            documento.add(new Paragraph("Email: " + cliente.getEmail(), normalFont));
            documento.add(new Paragraph("Fecha del presupuesto: " + cliente.getFechaContrato(), normalFont));
            documento.add(new Paragraph(" ", normalFont));
            
            // === PAQUETE BASE ===
            documento.add(new Paragraph("PAQUETE SELECCIONADO", subtituloFont));
            documento.add(new Paragraph("Nombre: " + datos.paqueteNombre, boldFont));
            if (datos.paqueteDescripcion != null && !datos.paqueteDescripcion.isEmpty()) {
                documento.add(new Paragraph("Descripción: " + datos.paqueteDescripcion, normalFont));
            }
            documento.add(new Paragraph("Precio: $" + String.format("%.0f", datos.paquetePrecio), normalFont));
            documento.add(new Paragraph(" ", normalFont));
            
            // === EXTRAS ADICIONALES ===
            if (datos.extras != null && !datos.extras.isEmpty()) {
                boolean tieneExtrasSeleccionados = datos.extras.stream().anyMatch(extra -> extra.getCantidad() > 0);
                
                if (tieneExtrasSeleccionados) {
                    documento.add(new Paragraph("SERVICIOS ADICIONALES", subtituloFont));
                    
                    for (Extra extra : datos.extras) {
                        if (extra.getCantidad() > 0) {
                            documento.add(new Paragraph("• " + extra.getNombre(), boldFont));
                            documento.add(new Paragraph("  Cantidad: " + extra.getCantidad() + 
                                                       " | Precio unitario: $" + String.format("%.0f", extra.getPrecio()) + 
                                                       " | Subtotal: $" + String.format("%.0f", extra.getSubtotal()), normalFont));
                            documento.add(new Paragraph(" ", pequenaFont));
                        }
                    }
                    documento.add(new Paragraph(" ", normalFont));
                }
            }
            
            // === RESUMEN FINANCIERO ===
            documento.add(new Paragraph("RESUMEN FINANCIERO", subtituloFont));
            documento.add(new Paragraph("Subtotal: $" + String.format("%.0f", datos.subtotal), normalFont));
            documento.add(new Paragraph("IVA (16%): $" + String.format("%.0f", datos.iva), normalFont));
            
            Paragraph total = new Paragraph("TOTAL: $" + String.format("%.0f", datos.total), 
                                           new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.RED));
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(10);
            documento.add(total);
            
            // === PIE DE PÁGINA ===
            documento.add(new Paragraph(" ", normalFont));
            documento.add(new Paragraph(" ", normalFont));
            Paragraph contactInfo = new Paragraph("CONTACTO: bienvenido@forgestudio.com.mx", normalFont);
            contactInfo.setAlignment(Element.ALIGN_CENTER);
            documento.add(contactInfo);
            
            Paragraph pie = new Paragraph("Presupuesto generado el: " + 
                                         java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                                         pequenaFont);
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.add(pie);
            
            // Cerrar documento
            documento.close();
            
            // Guardar ruta en BD
            guardarRutaPDFEnBD(cliente.getClienteId(), rutaCompleta, nombreArchivo);
            
            // Abrir PDF
            abrirPDF(rutaCompleta);
            
            // Mostrar mensaje de éxito
            mostrarMensaje("PDF Generado", 
                          "PDF completo generado exitosamente:\n" + nombreArchivo + "\n\n" +
                          "Incluye paquete base y todos los extras", "#27ae60");
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar PDF completo: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error", "Error al generar PDF: " + e.getMessage(), "#e74c3c");
        }
    }

    // ========== CONFIGURACIÓN DE EMAIL ==========

    private Properties configurarEmail() {
        Properties props = new Properties();
        
        // Configuración SMTP para Hostinger
        props.put("mail.smtp.host", "smtp.hostinger.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.hostinger.com");
        
        // Debug y timeouts
        props.put("mail.debug", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        
        System.out.println("✅ Configuración SMTP ForgeStudio cargada");
        return props;
    }

    private String crearContenidoEmailCompleto(ClienteContrato cliente) {
        // Obtener datos completos del presupuesto
        DatosPresupuestoCompleto datos = obtenerDatosPresupuestoCompleto(cliente.getClienteId());
        
        if (datos == null) {
            // Si no se pueden obtener los datos completos, usar método básico
            return crearContenidoEmailBasico(cliente);
        }
        
        StringBuilder contenidoHTML = new StringBuilder();
        
        contenidoHTML.append("<html>")
            .append("<body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6;\">")
            .append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;\">")
            
            // Header
            .append("<div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">")
            .append("<h1 style=\"margin: 0; font-size: 28px;\">🎨 FORGESTUDIO</h1>")
            .append("<p style=\"margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;\">Creamos experiencias inolvidables</p>")
            .append("</div>")
            
            // Contenido principal
            .append("<div style=\"background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">")
            .append("<h2 style=\"color: #2c3e50; margin-top: 0;\">🎉 Presupuesto para su Evento</h2>")
            
            .append("<p>Estimado/a <strong>").append(cliente.getNombreCompleto()).append("</strong>,</p>")
            
            .append("<p>Nos complace presentarle el presupuesto personalizado para su evento especial. En <strong>ForgeStudio</strong>, nos especializamos en crear experiencias únicas e inolvidables.</p>")
            
            // Paquete base
            .append("<div style=\"background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 20px; border-radius: 10px; margin: 25px 0;\">")
            .append("<h3 style=\"margin-top: 0; text-align: center;\">📦 Paquete Seleccionado</h3>")
            .append("<div style=\"text-align: center; font-size: 18px;\">")
            .append("<strong>").append(datos.paqueteNombre).append("</strong><br>")
            .append("<span style=\"font-size: 16px; opacity: 0.9;\">$").append(String.format("%.0f", datos.paquetePrecio)).append("</span>")
            .append("</div>");
        
        if (datos.paqueteDescripcion != null && !datos.paqueteDescripcion.isEmpty()) {
            contenidoHTML.append("<p style=\"margin-top: 15px; font-size: 14px; opacity: 0.9; text-align: center;\">")
                        .append(datos.paqueteDescripcion)
                        .append("</p>");
        }
        
        contenidoHTML.append("</div>");
        
        // Servicios adicionales (si existen)
        if (datos.extras != null && !datos.extras.isEmpty()) {
            boolean tieneExtrasSeleccionados = datos.extras.stream().anyMatch(extra -> extra.getCantidad() > 0);
            
            if (tieneExtrasSeleccionados) {
                contenidoHTML.append("<div style=\"background-color: #e8f4fd; padding: 20px; border-radius: 10px; margin: 25px 0; border-left: 4px solid #3498db;\">")
                            .append("<h3 style=\"color: #2c3e50; margin: 0 0 15px 0;\">✨ Servicios Adicionales</h3>");
                
                for (Extra extra : datos.extras) {
                    if (extra.getCantidad() > 0) {
                        contenidoHTML.append("<div style=\"margin-bottom: 10px; padding: 10px; background-color: white; border-radius: 5px;\">")
                                    .append("<strong style=\"color: #667eea;\">• ").append(extra.getNombre()).append("</strong><br>")
                                    .append("<span style=\"font-size: 14px;\">Cantidad: ").append(extra.getCantidad())
                                    .append(" | $").append(String.format("%.0f", extra.getPrecio())).append(" c/u")
                                    .append(" = <strong>$").append(String.format("%.0f", extra.getSubtotal())).append("</strong></span>")
                                    .append("</div>");
                    }
                }
                
                contenidoHTML.append("</div>");
            }
        }
        
        // Resumen financiero
        contenidoHTML.append("<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 25px 0;\">")
                    .append("<h3 style=\"color: #2c3e50; margin: 0 0 15px 0; text-align: center;\">💰 Resumen Financiero</h3>")
                    .append("<table style=\"width: 100%; font-size: 16px;\">")
                    .append("<tr><td style=\"padding: 5px 0;\">Subtotal:</td><td style=\"text-align: right;\">$").append(String.format("%.0f", datos.subtotal)).append("</td></tr>")
                    .append("<tr><td style=\"padding: 5px 0;\">IVA (16%):</td><td style=\"text-align: right;\">$").append(String.format("%.0f", datos.iva)).append("</td></tr>")
                    .append("<tr style=\"border-top: 2px solid #667eea; font-weight: bold; font-size: 20px; color: #667eea;\"><td style=\"padding: 10px 0;\">TOTAL:</td><td style=\"text-align: right;\">$").append(String.format("%.0f", datos.total)).append("</td></tr>")
                    .append("</table>")
                    .append("</div>")
                    
            // Resto del email (contacto, footer, etc.)
            .append("<p>📎 <strong>En el archivo adjunto</strong> encontrará todos los detalles específicos de su presupuesto.</p>")
            
            .append("<p>Nuestro equipo está listo para hacer realidad su evento. Si tiene alguna pregunta o desea realizar ajustes, estaremos encantados de atenderle.</p>")
            
            // Información de contacto
            .append("<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; margin: 25px 0; border-left: 4px solid #667eea;\">")
            .append("<h3 style=\"color: #2c3e50; margin: 0 0 15px 0;\">📞 Contacto Directo</h3>")
            .append("<p style=\"margin: 5px 0; font-size: 16px;\"><strong>Email:</strong> bienvenido@forgestudio.com.mx</p>")
            .append("<p style=\"margin: 5px 0; font-size: 16px;\"><strong>Teléfono:</strong> [Agregar tu número]</p>")
            .append("<p style=\"margin: 15px 0 0 0; color: #667eea; font-weight: bold;\">¡Estamos aquí para ayudarle!</p>")
            .append("</div>")
            
            .append("<p style=\"text-align: center; color: #7f8c8d; margin: 30px 0 20px 0;\">Gracias por confiar en <strong>ForgeStudio</strong> para su evento especial.</p>")
            
            .append("<p style=\"text-align: center;\">Saludos cordiales,<br>")
            .append("<strong style=\"color: #667eea; font-size: 18px;\">Equipo ForgeStudio</strong></p>")
            .append("</div>")
            
            // Footer
            .append("<div style=\"text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;\">")
            .append("<p>Este presupuesto fue generado automáticamente el ")
            .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")))
            .append("</p>")
            .append("<p>© ").append(java.time.Year.now()).append(" ForgeStudio - Todos los derechos reservados</p>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
        
        return contenidoHTML.toString();
    }

    private String crearContenidoEmailBasico(ClienteContrato cliente) {
        String contenidoHTML = "<html>" +
            "<body style=\"font-family: Arial, sans-serif; color: #333;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                    "<h2 style=\"color: #2c3e50; text-align: center;\">🎉 Presupuesto de Evento</h2>" +
                    
                    "<p>Estimado/a <strong>" + cliente.getNombreCompleto() + "</strong>,</p>" +
                    
                    "<p>Esperamos que se encuentre bien. Nos complace enviarle el presupuesto solicitado para su evento.</p>" +
                    
                    "<div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                        "<h3 style=\"color: #2c3e50; margin-top: 0;\">📋 Detalles del Presupuesto:</h3>" +
                        "<ul style=\"list-style: none; padding: 0;\">" +
                            "<li><strong>📦 Paquete:</strong> " + cliente.getPaquete() + "</li>" +
                            "<li><strong>💰 Monto:</strong> " + cliente.getMontoTotal() + "</li>" +
                            "<li><strong>📅 Fecha:</strong> " + cliente.getFechaContrato() + "</li>" +
                            "<li><strong>📊 Estado:</strong> " + cliente.getEstado() + "</li>" +
                        "</ul>" +
                    "</div>" +
                    
                    "<p>En el archivo adjunto encontrará todos los detalles del presupuesto.</p>" +
                    
                    "<p>Si tiene alguna pregunta o desea realizar alguna modificación, no dude en contactarnos.</p>" +
                    
                    "<div style=\"background-color: #3498db; color: white; padding: 15px; border-radius: 8px; text-align: center; margin: 20px 0;\">" +
                        "<p style=\"margin: 0;\"><strong>📞 Contáctanos:</strong></p>" +
                        "<p style=\"margin: 5px 0;\">Email: " + EMAIL_EMPRESA + "</p>" +
                    "</div>" +
                    
                    "<p>Esperamos poder hacer realidad su evento especial.</p>" +
                    
                    "<p>Saludos cordiales,<br>" +
                    "<strong>" + NOMBRE_EMPRESA + "</strong></p>" +
                    
                    "<hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">" +
                    "<p style=\"font-size: 12px; color: #7f8c8d; text-align: center;\">" +
                        "Este email fue generado automáticamente el " + 
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                    "</p>" +
                "</div>" +
            "</body>" +
        "</html>";
        
        return contenidoHTML;
    }

    // ========== MÉTODOS PARA ENVÍO DE EMAIL ==========

    @FXML 
    private void enviarEmail() {
        if (clienteActualmenteSeleccionado != null) {
            if (!clienteActualmenteSeleccionado.getEmail().equals("Sin email")) {
                // Primero verificar si existe un PDF o crear uno
                String rutaPDF = buscarPDFExistente(clienteActualmenteSeleccionado.getClienteId());
                
                if (rutaPDF == null) {
                    // No existe PDF, preguntar si quiere generar uno
                    mostrarConfirmacion("📧 Enviar Email", 
                                      "No se encontró un PDF para este cliente.\n\n" +
                                      "¿Deseas generar el PDF y enviarlo por email?",
                                      () -> {
                                          try {
                                              // Generar PDF primero
                                              generarPDFPresupuestoCompleto(clienteActualmenteSeleccionado);
                                              // Buscar la nueva ruta del PDF
                                              String nuevaRutaPDF = buscarPDFExistente(clienteActualmenteSeleccionado.getClienteId());
                                              if (nuevaRutaPDF != null) {
                                                  enviarEmailConPDF(clienteActualmenteSeleccionado, nuevaRutaPDF);
                                              }
                                          } catch (Exception e) {
                                              mostrarMensaje("Error", "Error al generar PDF: " + e.getMessage(), "#e74c3c");
                                          }
                                      });
                } else {
                    // Ya existe PDF, enviarlo directamente
                    enviarEmailConPDF(clienteActualmenteSeleccionado, rutaPDF);
                }
            } else {
                mostrarMensaje("Sin email", "Este cliente no tiene email registrado", "#f39c12");
            }
        } else {
            mostrarMensaje("Atención", "Selecciona un cliente de la tabla", "#f39c12");
        }
    }

    private void enviarEmailConPDF(ClienteContrato cliente, String rutaPDF) {
        try {
            // Configurar propiedades del servidor
            Properties props = configurarEmail();
            
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
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cliente.getEmail()));
            mensaje.setSubject("Presupuesto de Evento - " + cliente.getNombreCompleto());
            
            // Crear contenido del mensaje
            Multipart multipart = new MimeMultipart();
            
            // Parte 1: Texto del email
            MimeBodyPart textoParte = new MimeBodyPart();
            String contenidoEmail = crearContenidoEmailCompleto(cliente); // ← USAR MÉTODO COMPLETO
            textoParte.setContent(contenidoEmail, "text/html; charset=utf-8");
            multipart.addBodyPart(textoParte);
            
            // Parte 2: Adjuntar PDF
            MimeBodyPart adjuntoParte = new MimeBodyPart();
            File archivoPDF = new File(rutaPDF);
            if (archivoPDF.exists()) {
                adjuntoParte.attachFile(archivoPDF);
                adjuntoParte.setFileName("Presupuesto_" + cliente.getNombreCompleto().replace(" ", "_") + ".pdf");
                multipart.addBodyPart(adjuntoParte);
            }
            
            mensaje.setContent(multipart);
            
            // Mostrar progreso
            mostrarMensaje("Enviando...", "Enviando email a " + cliente.getEmail() + "\nPor favor espera...", "#3498db");
            
            // Enviar email en hilo separado para no bloquear la interfaz
            new Thread(() -> {
                try {
                    Transport.send(mensaje);
                    
                    // Actualizar interfaz en el hilo principal
                    javafx.application.Platform.runLater(() -> {
                        mostrarMensaje("✅ Email Enviado", 
                                      "Email enviado exitosamente a:\n" + 
                                      cliente.getEmail() + "\n\n" +
                                      "Cliente: " + cliente.getNombreCompleto() + "\n" +
                                      "PDF adjunto: ✓", "#27ae60");
                        
                        // Registrar envío en la base de datos
                        registrarEnvioEmail(cliente.getClienteId(), cliente.getEmail());
                    });
                    
                } catch (MessagingException e) {
                    javafx.application.Platform.runLater(() -> {
                        mostrarMensaje("❌ Error al Enviar", 
                                      "No se pudo enviar el email:\n" + e.getMessage() + "\n\n" +
                                      "Verifica:\n• Conexión a internet\n• Configuración del email\n• Password de aplicación", "#e74c3c");
                    });
                    System.err.println("Error al enviar email: " + e.getMessage());
                }
            }).start();
            
        } catch (Exception e) {
            mostrarMensaje("Error", "Error al preparar email: " + e.getMessage(), "#e74c3c");
            e.printStackTrace();
        }
    }

    private void registrarEnvioEmail(int clienteId, String emailDestinatario) {
        // Registro simple en consola por ahora (comentado para evitar errores de BD)
        /*
        try (Connection conn = Conexion.conectar()) {
            String sql = "UPDATE presupuestos SET ultimo_envio_email = NOW(), email_enviado_a = ? WHERE cliente_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailDestinatario);
            stmt.setInt(2, clienteId);
            stmt.executeUpdate();
            System.out.println("✅ Registro de envío guardado en BD");
        } catch (SQLException e) {
            System.err.println("⚠️ Error al registrar envío: " + e.getMessage());
        }
        */
        
        System.out.println("📧 Email enviado a: " + emailDestinatario + " (Cliente ID: " + clienteId + ")");
    }

    // ========== MÉTODOS PARA BOTONES DE ACCIÓN ==========

    @FXML
    private void limpiarPresupuestosManual() {
        mostrarConfirmacion("🧹 Confirmar Limpieza", 
                           "Se eliminarán automáticamente:\n\n" +
                           "• Presupuestos con más de 1 mes\n" +
                           "• Los clientes asociados (sin contratos)\n\n" +
                           "⚠️ Esta acción NO se puede deshacer.",
                           () -> {
                               limpiarPresupuestosVencidos();
                               cargarDatos();
                               mostrarMensaje("Éxito", "Presupuestos vencidos eliminados correctamente", "#27ae60");
                           });
    }

    @FXML
    private void actualizarDatos() {
        System.out.println("🔄 Actualizando presupuestos...");
        limpiarPresupuestosVencidos();
        cargarDatos();
        mostrarMensaje("Éxito", "Presupuestos actualizados correctamente", "#27ae60");
    }

    @FXML
    private void buscarCliente() {
        String textoBusqueda = txtBuscar.getText().trim();
        
        if (textoBusqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        // Implementar lógica de búsqueda aquí si es necesario
        cargarDatos();
    }

    @FXML
    private void accionRegresar() {
        try {
            App.setRoot("PanelPrincipal");
        } catch (IOException e) {
            System.err.println("Error al regresar al panel principal: " + e.getMessage());
            mostrarMensaje("Error", "No se pudo regresar al panel principal", "#e74c3c");
        }
    }

    @FXML 
    private void verDetalles() {
        if (clienteActualmenteSeleccionado != null) {
            String detalles = "=== DETALLES DEL CLIENTE ===\n\n" +
                             "👤 Nombre: " + clienteActualmenteSeleccionado.getNombreCompleto() + "\n" +
                             "📞 Teléfono: " + clienteActualmenteSeleccionado.getTelefono() + "\n" +
                             "📧 Email: " + clienteActualmenteSeleccionado.getEmail() + "\n\n" +
                             "=== PRESUPUESTO ===\n\n" +
                             "📅 Fecha: " + clienteActualmenteSeleccionado.getFechaContrato() + "\n" +
                             "📦 Paquete: " + clienteActualmenteSeleccionado.getPaquete() + "\n" +
                             "💰 Monto: " + clienteActualmenteSeleccionado.getMontoTotal() + "\n" +
                             "📊 Estado: " + clienteActualmenteSeleccionado.getEstado();
            
            mostrarMensaje("📋 Detalles del Presupuesto", detalles, "#3498db");
        } else {
            mostrarMensaje("Atención", "Selecciona un cliente de la tabla", "#f39c12");
        }
    }

    @FXML 
    private void verContratoPDF() {
        if (clienteActualmenteSeleccionado != null) {
            try {
                // Primero buscar si ya existe un PDF para este cliente
                String rutaPDFExistente = buscarPDFExistente(clienteActualmenteSeleccionado.getClienteId());
                
                if (rutaPDFExistente != null) {
                    // Ya existe un PDF, preguntarle al usuario qué hacer
                    mostrarConfirmacion("📄 PDF Existente", 
                                      "Ya existe un PDF para este cliente.\n\n" +
                                      "¿Qué deseas hacer?",
                                      () -> {
                                          // Abrir PDF existente
                                          abrirPDF(rutaPDFExistente);
                                          mostrarMensaje("PDF Abierto", "PDF existente abierto correctamente", "#27ae60");
                                      },
                                      () -> {
                                          // Generar nuevo PDF
                                          generarPDFPresupuestoCompleto(clienteActualmenteSeleccionado);
                                      },
                                      "📂 Abrir Existente", "🆕 Generar Nuevo");
                } else {
                    // No existe PDF, generar uno nuevo
                    generarPDFPresupuestoCompleto(clienteActualmenteSeleccionado);
                }
                
            } catch (Exception e) {
                mostrarMensaje("Error", "Error al procesar PDF: " + e.getMessage(), "#e74c3c");
                e.printStackTrace();
            }
        } else {
            mostrarMensaje("Atención", "Selecciona un cliente de la tabla", "#f39c12");
        }
    }

    @FXML 
    private void eliminarSeleccionado() {
        if (clienteActualmenteSeleccionado != null) {
            mostrarConfirmacion("🗑️ Confirmar Eliminación",
                               "¿Eliminar el presupuesto de:\n\n" +
                               "👤 " + clienteActualmenteSeleccionado.getNombreCompleto() + "\n" +
                               "📦 " + clienteActualmenteSeleccionado.getPaquete() + "\n" +
                               "💰 " + clienteActualmenteSeleccionado.getMontoTotal() + "\n\n" +
                               "Si no tiene contratos, también se eliminará el cliente.\n\n" +
                               "⚠️ Esta acción NO se puede deshacer.",
                               () -> eliminarPresupuestoCliente(clienteActualmenteSeleccionado));
        } else {
            mostrarMensaje("Atención", "Selecciona un cliente de la tabla", "#f39c12");
        }
    }

    private void eliminarPresupuestoCliente(ClienteContrato cliente) {
        try (Connection conn = Conexion.conectar()) {
            // Verificar si tiene contratos
            String sqlVerificar = "SELECT COUNT(*) FROM contratos WHERE cliente_id = ?";
            PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar);
            stmtVerificar.setInt(1, cliente.getClienteId());
            ResultSet rsVerificar = stmtVerificar.executeQuery();
            
            boolean tieneContratos = false;
            if (rsVerificar.next()) {
                tieneContratos = rsVerificar.getInt(1) > 0;
            }
            
            // Eliminar presupuesto
            String sqlPresupuesto = "DELETE FROM presupuestos WHERE cliente_id = ?";
            PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
            stmtPresupuesto.setInt(1, cliente.getClienteId());
            stmtPresupuesto.executeUpdate();
            
            // Si no tiene contratos, eliminar cliente también
            if (!tieneContratos) {
                String sqlCliente = "DELETE FROM clientes WHERE id = ?";
                PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente);
                stmtCliente.setInt(1, cliente.getClienteId());
                stmtCliente.executeUpdate();
            }
            
            mostrarMensaje("Éxito", "Presupuesto eliminado correctamente", "#27ae60");
            cargarDatos();
            cerrarMensajes();
            
        } catch (SQLException e) {
            mostrarMensaje("Error", "Error al eliminar presupuesto: " + e.getMessage(), "#e74c3c");
        }
    }

    // ========== MÉTODOS PARA MANEJO DE PDF ==========

    private void guardarRutaPDFEnBD(int clienteId, String rutaCompleta, String nombreArchivo) {
        try (Connection conn = Conexion.conectar()) {
            // Actualizar la tabla presupuestos con la ruta del PDF
            String sqlActualizar = "UPDATE presupuestos SET ruta_archivo_pdf = ?, nombre_archivo_pdf = ? WHERE cliente_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sqlActualizar);
            stmt.setString(1, rutaCompleta);
            stmt.setString(2, nombreArchivo);
            stmt.setInt(3, clienteId);
            
            int filasActualizadas = stmt.executeUpdate();
            
            if (filasActualizadas > 0) {
                System.out.println("✅ Ruta del PDF guardada en BD: " + rutaCompleta);
            } else {
                System.out.println("⚠️ No se pudo actualizar la ruta del PDF en BD");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar ruta PDF en BD: " + e.getMessage());
        }
    }

    private String buscarPDFExistente(int clienteId) {
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT ruta_archivo_pdf FROM presupuestos WHERE cliente_id = ? AND ruta_archivo_pdf IS NOT NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String rutaPDF = rs.getString("ruta_archivo_pdf");
                
                // Verificar que el archivo físico existe
                File archivo = new File(rutaPDF);
                if (archivo.exists()) {
                    return rutaPDF;
                } else {
                    System.out.println("⚠️ PDF registrado en BD pero archivo no existe: " + rutaPDF);
                    // Limpiar registro inválido
                    limpiarRutaPDFInvalida(clienteId);
                    return null;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar PDF existente: " + e.getMessage());
        }
        
        return null;
    }

    private void limpiarRutaPDFInvalida(int clienteId) {
        try (Connection conn = Conexion.conectar()) {
            String sql = "UPDATE presupuestos SET ruta_archivo_pdf = NULL, nombre_archivo_pdf = NULL WHERE cliente_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clienteId);
            stmt.executeUpdate();
            System.out.println("🧹 Ruta PDF inválida limpiada para cliente ID: " + clienteId);
        } catch (SQLException e) {
            System.err.println("❌ Error al limpiar ruta PDF inválida: " + e.getMessage());
        }
    }

    private void abrirPDF(String rutaPDF) {
        try {
            File archivoPDF = new File(rutaPDF);
            if (archivoPDF.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivoPDF);
                    System.out.println("✅ PDF abierto: " + rutaPDF);
                } else {
                    System.out.println("⚠️ Desktop no soportado. PDF guardado en: " + rutaPDF);
                }
            } else {
                System.out.println("❌ No se encontró el archivo PDF: " + rutaPDF);
            }
        } catch (IOException e) {
            System.err.println("❌ Error al abrir PDF: " + e.getMessage());
            // Mostrar la ruta al usuario si no se puede abrir automáticamente
            mostrarMensaje("PDF Guardado", 
                          "No se pudo abrir automáticamente.\n" +
                          "El PDF se guardó en: " + rutaPDF, "#f39c12");
        }
    }

    // ========== MÉTODOS PARA INTERFAZ DE USUARIO ==========

    private void mostrarMensaje(String titulo, String mensaje, String color) {
        if (panelMensajes != null && lblTituloMensaje != null && contenedorMensajes != null) {
            lblTituloMensaje.setText(titulo);
            lblTituloMensaje.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + color + ";");
            
            contenedorMensajes.getChildren().clear();
            
            VBox tarjetaMensaje = new VBox(15);
            tarjetaMensaje.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                  "-fx-background-radius: 15px; " +
                                  "-fx-padding: 20px; " +
                                  "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                                  "-fx-border-color: " + color + "; -fx-border-width: 2px; -fx-border-radius: 15px;");

            Label lblMensaje = new Label(mensaje);
            lblMensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            lblMensaje.setWrapText(true);

            tarjetaMensaje.getChildren().add(lblMensaje);
            contenedorMensajes.getChildren().add(tarjetaMensaje);
            
            panelMensajes.setVisible(true);
        }
    }

    private void mostrarConfirmacion(String titulo, String mensaje, Runnable accion1, Runnable accion2, String textoBoton1, String textoBoton2) {
        if (panelMensajes != null && lblTituloMensaje != null && contenedorMensajes != null) {
            lblTituloMensaje.setText(titulo);
            lblTituloMensaje.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3498db;");
            
            contenedorMensajes.getChildren().clear();
            
            VBox tarjetaConfirmacion = new VBox(15);
            tarjetaConfirmacion.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                       "-fx-background-radius: 15px; " +
                                       "-fx-padding: 20px; " +
                                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                                       "-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 15px;");

            Label lblMensaje = new Label(mensaje);
            lblMensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            lblMensaje.setWrapText(true);

            HBox botones = new HBox(15);
            botones.setAlignment(Pos.CENTER);
            
            Button btn1 = new Button(textoBoton1);
            btn1.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                         "-fx-background-radius: 20px; -fx-padding: 10 20; " +
                         "-fx-font-weight: bold;");
            btn1.setOnAction(e -> {
                accion1.run();
                cerrarMensajes();
            });
            
            Button btn2 = new Button(textoBoton2);
            btn2.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                         "-fx-background-radius: 20px; -fx-padding: 10 20; " +
                         "-fx-font-weight: bold;");
            btn2.setOnAction(e -> {
                accion2.run();
                cerrarMensajes();
            });
            
            Button btnCancelar = new Button("✕ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                                "-fx-background-radius: 20px; -fx-padding: 10 20; " +
                                "-fx-font-weight: bold;");
            btnCancelar.setOnAction(e -> cerrarMensajes());
            
            botones.getChildren().addAll(btn1, btn2, btnCancelar);

            tarjetaConfirmacion.getChildren().addAll(lblMensaje, new Separator(), botones);
            contenedorMensajes.getChildren().add(tarjetaConfirmacion);
            
            panelMensajes.setVisible(true);
        }
    }

    private void mostrarConfirmacion(String titulo, String mensaje, Runnable accionConfirmar) {
        if (panelMensajes != null && lblTituloMensaje != null && contenedorMensajes != null) {
            lblTituloMensaje.setText(titulo);
            lblTituloMensaje.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f39c12;");
            
            contenedorMensajes.getChildren().clear();
            
            VBox tarjetaConfirmacion = new VBox(15);
            tarjetaConfirmacion.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                       "-fx-background-radius: 15px; " +
                                       "-fx-padding: 20px; " +
                                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                                       "-fx-border-color: #f39c12; -fx-border-width: 2px; -fx-border-radius: 15px;");

            Label lblMensaje = new Label(mensaje);
            lblMensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            lblMensaje.setWrapText(true);

            HBox botones = new HBox(15);
            botones.setAlignment(Pos.CENTER);
            
            Button btnConfirmar = new Button("✓ Confirmar");
            btnConfirmar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                 "-fx-background-radius: 20px; -fx-padding: 10 20; " +
                                 "-fx-font-weight: bold;");
            btnConfirmar.setOnAction(e -> {
                accionConfirmar.run();
                cerrarMensajes();
            });
            
            Button btnCancelar = new Button("✕ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                                "-fx-background-radius: 20px; -fx-padding: 10 20; " +
                                "-fx-font-weight: bold;");
            btnCancelar.setOnAction(e -> cerrarMensajes());
            
            botones.getChildren().addAll(btnConfirmar, btnCancelar);

            tarjetaConfirmacion.getChildren().addAll(lblMensaje, new Separator(), botones);
            contenedorMensajes.getChildren().add(tarjetaConfirmacion);
            
            panelMensajes.setVisible(true);
        }
    }

    @FXML
    private void cerrarMensajes() {
        if (panelMensajes != null) {
            panelMensajes.setVisible(false);
        }
    }

    // ========== CLASE INTERNA PARA DATOS COMPLETOS ==========

    private static class DatosPresupuestoCompleto {
        int presupuestoId;
        java.sql.Date fechaCreacion;
        String paqueteNombre;
        String paqueteDescripcion;
        double paquetePrecio;
        double subtotal;
        double iva;
        double total;
        List<Extra> extras;
    }
}