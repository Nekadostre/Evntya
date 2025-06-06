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
import java.io.FileNotFoundException;

// Imports para JavaMail
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

// Imports para modelos existentes
import modelos.Cliente;
import modelos.Extra;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

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
    private static final String NOMBRE_EMPRESA = "Segundo Castillo - ¡Hacemos tu evento realidad!";

    private ObservableList<ClienteContrato> listaClientes = FXCollections.observableArrayList();
    private ClienteContrato clienteActualmenteSeleccionado = null;

    @FXML
    public void initialize() {
        configurarTabla();
        limpiarPresupuestosVencidos();
        cargarDatos();
        configurarSeleccionTabla();
        
        // DEBUG TEMPORAL - Ver qué hay en la BD
        debugPresupuestosSimple();
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
        System.out.println("🧹 Limpiando presupuestos vencidos (> 30 días)..."); // ⭐ CORREGIDO: 30 días
        
        String sqlLimpiar = """
            DELETE p, c FROM presupuestos p
            INNER JOIN clientes c ON p.cliente_id = c.id
            WHERE p.fecha_creacion < DATE_SUB(NOW(), INTERVAL 30 DAY)  -- ⭐ CORREGIDO: 30 días
            AND NOT EXISTS (
                SELECT 1 FROM contratos cont WHERE cont.cliente_id = c.id
            )
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sqlLimpiar);
        int eliminados = stmt.executeUpdate();
        
        if (eliminados > 0) {
            System.out.println("🗑️ Se eliminaron " + eliminados + " presupuestos/clientes vencidos (>30 días)");
        } else {
            System.out.println("✅ No hay presupuestos vencidos para eliminar");
        }
        
        // ⭐ MOSTRAR INFORMACIÓN SOBRE LA POLÍTICA DE LIMPIEZA
        System.out.println("📋 POLÍTICA DE LIMPIEZA:");
        System.out.println("   • Presupuestos válidos: 30 días");
        System.out.println("   • Eliminación automática: después de 30 días");
        System.out.println("   • Los contratos firmados NO se eliminan nunca");
        
    } catch (SQLException e) {
        System.err.println("❌ Error al limpiar presupuestos vencidos: " + e.getMessage());
    }
}

    private void cargarDatos() {
    listaClientes.clear();
    
    try (Connection conn = Conexion.conectar()) {
        System.out.println("🔄 CARGANDO CLIENTES CON PRESUPUESTOS ACTIVOS - DÍAS CORREGIDOS");
        
        // === CONSULTA CORREGIDA - VALIDEZ DE 30 DÍAS ===
        String sql = """
            SELECT DISTINCT
                c.id as cliente_id,
                c.nombre,
                c.apellido_paterno,
                c.apellido_materno,
                c.telefono,
                c.correo as email,
                p.fecha_creacion as fecha_presupuesto,
                paq.nombre as paquete_nombre,
                p.total_general as monto_presupuesto,
                p.numero_presupuesto,
                CASE 
                    WHEN EXISTS(SELECT 1 FROM contratos cont WHERE cont.cliente_id = c.id) THEN 'Contratado'
                    ELSE 'Presupuesto'
                END as estado_cliente,
                DATEDIFF(NOW(), p.fecha_creacion) as dias_desde_presupuesto
            FROM clientes c
            INNER JOIN (
                SELECT cliente_id, MAX(fecha_creacion) as max_fecha
                FROM presupuestos 
                WHERE fecha_creacion >= DATE_SUB(NOW(), INTERVAL 1 MONTH)  -- ⭐ VOLVER A 1 MES
                GROUP BY cliente_id
            ) latest_p ON c.id = latest_p.cliente_id
            INNER JOIN presupuestos p ON c.id = p.cliente_id AND p.fecha_creacion = latest_p.max_fecha
            LEFT JOIN paquetes paq ON p.paquete_id = paq.id
            ORDER BY p.fecha_creacion DESC
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int contador = 0;
        
        while (rs.next()) {
            contador++;
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
            int diasRestantes = Math.max(0, 30 - diasDesde); // ⭐ CORREGIDO: 30 días en lugar de 90
            
            if (estadoCliente.equals("Contratado")) {
                clienteContrato.setEstado("Contratado");
            } else {
                if (diasRestantes > 0) {
                    clienteContrato.setEstado("Presupuesto (" + diasRestantes + " días)");
                } else {
                    clienteContrato.setEstado("Presupuesto (Vencido)");
                }
            }
            
            listaClientes.add(clienteContrato);
            
            // ⭐ DEBUG DETALLADO POR CADA CLIENTE CARGADO
            System.out.println("✅ Cliente #" + contador + " cargado:");
            System.out.println("   ID: " + clienteContrato.getClienteId());
            System.out.println("   Nombre: " + clienteContrato.getNombreCompleto());
            System.out.println("   Fecha: " + clienteContrato.getFechaContrato());
            System.out.println("   Días desde creación: " + diasDesde);
            System.out.println("   Días restantes: " + diasRestantes + "/30"); // ⭐ MOSTRAR DE 30
            System.out.println("   Estado: " + clienteContrato.getEstado());
            System.out.println("   ─────────────────────");
        }
        
        tablaClientes.setItems(listaClientes);
        lblTotal.setText(String.valueOf(listaClientes.size()));
        
        System.out.println("✅ TOTAL CARGADOS: " + listaClientes.size() + " clientes con presupuestos activos");
        System.out.println("📅 VALIDEZ: Presupuestos válidos por 30 días");
        
        // ⭐ VERIFICACIÓN ADICIONAL
        verificarPresupuestosRecientes();
        
    } catch (SQLException e) {
        System.err.println("❌ Error al cargar presupuestos: " + e.getMessage());
        e.printStackTrace();
        mostrarMensaje("Error", "No se pudieron cargar los presupuestos: " + e.getMessage(), "#e74c3c");
    }
}
    
    // ⭐ MÉTODO ADICIONAL: VERIFICAR PRESUPUESTOS RECIENTES EN BD
private void verificarPresupuestosRecientes() {
    try (Connection conn = Conexion.conectar()) {
        System.out.println("\n🔍 === VERIFICACIÓN DE PRESUPUESTOS RECIENTES ===");
        
        String sqlRecientes = """
            SELECT 
                p.id,
                p.cliente_nombre,
                p.numero_presupuesto,
                p.fecha_creacion,
                p.total_general,
                c.nombre as cliente_bd_nombre,
                c.apellido_paterno,
                CASE WHEN p.archivo_pdf_contenido IS NOT NULL THEN 'SÍ' ELSE 'NO' END as tiene_pdf
            FROM presupuestos p
            LEFT JOIN clientes c ON p.cliente_id = c.id
            WHERE p.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            ORDER BY p.fecha_creacion DESC
            LIMIT 10
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sqlRecientes);
        ResultSet rs = stmt.executeQuery();
        
        int contador = 0;
        while (rs.next()) {
            contador++;
            System.out.println("📋 Presupuesto #" + contador + ":");
            System.out.println("   ID Presupuesto: " + rs.getInt("id"));
            System.out.println("   Cliente en presupuesto: " + rs.getString("cliente_nombre"));
            System.out.println("   Cliente en BD: " + rs.getString("cliente_bd_nombre") + " " + rs.getString("apellido_paterno"));
            System.out.println("   Número: " + rs.getString("numero_presupuesto"));
            System.out.println("   Fecha: " + rs.getTimestamp("fecha_creacion"));
            System.out.println("   Total: $" + rs.getDouble("total_general"));
            System.out.println("   Tiene PDF: " + rs.getString("tiene_pdf"));
            System.out.println("   ─────────────────────");
        }
        
        if (contador == 0) {
            System.out.println("❌ NO HAY PRESUPUESTOS EN LOS ÚLTIMOS 7 DÍAS");
            
            // Verificar si hay presupuestos en general
            String sqlTotal = "SELECT COUNT(*) as total FROM presupuestos";
            PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal);
            ResultSet rsTotal = stmtTotal.executeQuery();
            
            if (rsTotal.next()) {
                int total = rsTotal.getInt("total");
                System.out.println("📊 Total de presupuestos en BD: " + total);
                
                if (total > 0) {
                    System.out.println("⚠️ HAY PRESUPUESTOS PERO SON MÁS ANTIGUOS DE 7 DÍAS");
                }
            }
        } else {
            System.out.println("✅ ENCONTRADOS " + contador + " PRESUPUESTOS RECIENTES");
        }
        
        System.out.println("🔍 === FIN VERIFICACIÓN ===\n");
        
    } catch (SQLException e) {
        System.err.println("❌ Error en verificación: " + e.getMessage());
    }
}
    
    // ========== MÉTODO SIMPLIFICADO DE DEBUG ==========
    private void debugPresupuestosSimple() {
        System.out.println("🔍 === DEBUG SIMPLE: PRESUPUESTOS ===");
        
        try (Connection conn = Conexion.conectar()) {
            // Contar presupuestos totales
            String sqlTotal = "SELECT COUNT(*) as total FROM presupuestos";
            PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal);
            ResultSet rsTotal = stmtTotal.executeQuery();
            
            if (rsTotal.next()) {
                System.out.println("📊 Total presupuestos en BD: " + rsTotal.getInt("total"));
            }
            
            // Mostrar los 3 más recientes
            String sqlRecientes = """
                SELECT 
                    cliente_nombre, 
                    fecha_creacion, 
                    total_general,
                    CASE WHEN archivo_pdf_contenido IS NOT NULL THEN 'SÍ' ELSE 'NO' END as tiene_pdf
                FROM presupuestos 
                ORDER BY fecha_creacion DESC 
                LIMIT 5
                """;
            
            PreparedStatement stmtRecientes = conn.prepareStatement(sqlRecientes);
            ResultSet rsRecientes = stmtRecientes.executeQuery();
            
            System.out.println("📋 Presupuestos más recientes:");
            int contador = 0;
            while (rsRecientes.next()) {
                contador++;
                System.out.println("   " + contador + ". " + 
                                 rsRecientes.getString("cliente_nombre") + 
                                 " - $" + rsRecientes.getDouble("total_general") + 
                                 " - PDF: " + rsRecientes.getString("tiene_pdf") +
                                 " - " + rsRecientes.getTimestamp("fecha_creacion"));
            }
            
            if (contador == 0) {
                System.out.println("   ❌ No hay presupuestos en la base de datos");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error en debug: " + e.getMessage());
        }
        
        System.out.println("🔍 === FIN DEBUG ===");
    }

    // MÉTODOS PARA DATOS COMPLETOS DEL PRESUPUESTO 

   // ========== MÉTODO AUXILIAR MEJORADO: OBTENER DATOS COMPLETOS ==========
private DatosPresupuestoCompleto obtenerDatosPresupuestoCompleto(int clienteId) {
    try (Connection conn = Conexion.conectar()) {
        DatosPresupuestoCompleto datos = new DatosPresupuestoCompleto();
        
        // Consulta principal del presupuesto
        String sqlPresupuesto = """
            SELECT 
                p.id as presupuesto_id,
                p.fecha_creacion,
                p.total_extras,
                p.total_general,
                p.paquete_precio,
                paq.nombre as paquete_nombre,
                paq.descripcion as paquete_descripcion
            FROM presupuestos p
            LEFT JOIN paquetes paq ON p.paquete_id = paq.id
            WHERE p.cliente_id = ?
            ORDER BY p.fecha_creacion DESC LIMIT 1
            """;
        
        PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
        stmtPresupuesto.setInt(1, clienteId);
        ResultSet rsPresupuesto = stmtPresupuesto.executeQuery();
        
        if (rsPresupuesto.next()) {
            // Datos básicos
            datos.presupuestoId = rsPresupuesto.getInt("presupuesto_id");
            datos.fechaCreacion = rsPresupuesto.getDate("fecha_creacion");
            datos.total = rsPresupuesto.getDouble("total_general");
            datos.paqueteNombre = rsPresupuesto.getString("paquete_nombre");
            datos.paqueteDescripcion = rsPresupuesto.getString("paquete_descripcion");
            datos.paquetePrecio = rsPresupuesto.getDouble("paquete_precio");
            
            // Calcular subtotal e IVA
            double totalExtras = rsPresupuesto.getDouble("total_extras");
            datos.subtotal = datos.paquetePrecio + totalExtras;
            datos.iva = datos.subtotal * 0.16; // 16% de IVA
            
            // Verificar si el total incluye IVA
            if (Math.abs(datos.total - (datos.subtotal + datos.iva)) < 0.01) {
                // El total incluye IVA
            } else {
                // El total no incluye IVA, ajustar
                datos.subtotal = datos.total;
                datos.iva = 0;
            }
            
            // Obtener extras usando la tabla presupuesto_extras
            String sqlExtras = """
                SELECT 
                    e.id as extra_id,
                    e.nombre as extra_nombre,
                    e.precio as extra_precio,
                    pe.cantidad
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
                Extra extra = new Extra(
                    rsExtras.getInt("extra_id"),
                    rsExtras.getString("extra_nombre"),
                    rsExtras.getDouble("extra_precio"),
                    rsExtras.getInt("cantidad")
                );
                extras.add(extra);
            }
            
            datos.extras = extras;
            return datos;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error al obtener presupuesto completo: " + e.getMessage());
    }
    
    return null;
}

    // MÉTODOS PARA GENERAR PDF COMPLETO

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
            
            // HEADER
            Paragraph titulo = new Paragraph("🎨 FORGESTUDIO", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(5);
            documento.add(titulo);
            
            Paragraph subtitulo = new Paragraph("PRESUPUESTO DE EVENTO", subtituloFont);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);
            
            // INFORMACIÓN DEL CLIENTE
            documento.add(new Paragraph("INFORMACIÓN DEL CLIENTE", subtituloFont));
            documento.add(new Paragraph("Nombre: " + cliente.getNombreCompleto(), normalFont));
            documento.add(new Paragraph("Teléfono: " + cliente.getTelefono(), normalFont));
            documento.add(new Paragraph("Email: " + cliente.getEmail(), normalFont));
            documento.add(new Paragraph("Fecha del presupuesto: " + cliente.getFechaContrato(), normalFont));
            documento.add(new Paragraph(" ", normalFont));
            
            // PAQUETE BASE
            documento.add(new Paragraph("PAQUETE SELECCIONADO", subtituloFont));
            documento.add(new Paragraph("Nombre: " + datos.paqueteNombre, boldFont));
            if (datos.paqueteDescripcion != null && !datos.paqueteDescripcion.isEmpty()) {
                documento.add(new Paragraph("Descripción: " + datos.paqueteDescripcion, normalFont));
            }
            documento.add(new Paragraph("Precio: $" + String.format("%.0f", datos.paquetePrecio), normalFont));
            documento.add(new Paragraph(" ", normalFont));
            
            // EXTRAS ADICIONALES
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
            
            // RESUMEN FINANCIERO
            documento.add(new Paragraph("RESUMEN FINANCIERO", subtituloFont));
            documento.add(new Paragraph("Subtotal: $" + String.format("%.0f", datos.subtotal), normalFont));
            documento.add(new Paragraph("IVA (16%): $" + String.format("%.0f", datos.iva), normalFont));
            
            Paragraph total = new Paragraph("TOTAL: $" + String.format("%.0f", datos.total), 
                                           new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.RED));
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(10);
            documento.add(total);
            
            // PIE DE PÁGINA
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
            guardarPDFComoBLOBEnBD(cliente.getClienteId(), rutaCompleta, nombreArchivo);
            
            // Abrir PDF
            abrirPDF(rutaCompleta);
            
            // Mostrar mensaje de éxito
             mostrarMensaje("PDF Generado", 
                      "PDF completo generado exitosamente:\n" + nombreArchivo + "\n\n" +
                      "✅ Guardado localmente\n✅ Guardado en base de datos\n" +
                      "Incluye paquete base y todos los extras", "#27ae60");
            
        } catch (DocumentException | FileNotFoundException e) {
        System.err.println("❌ Error al generar PDF completo: " + e.getMessage());
        mostrarMensaje("Error", "Error al generar PDF: " + e.getMessage(), "#e74c3c");
    }
}
   
   // ========== MÉTODO NUEVO: GUARDAR PDF COMO BLOB ==========
private void guardarPDFComoBLOBEnBD(int clienteId, String rutaCompleta, String nombreArchivo) {
    try (Connection conn = Conexion.conectar()) {
        
        // ✅ LEER EL ARCHIVO PDF COMO BYTES
        byte[] pdfBytes = null;
        try {
            java.nio.file.Path pdfPath = java.nio.file.Paths.get(rutaCompleta);
            pdfBytes = java.nio.file.Files.readAllBytes(pdfPath);
            System.out.println("✅ PDF leído para base de datos: " + pdfBytes.length + " bytes");
        } catch (Exception e) {
            System.err.println("❌ Error leyendo PDF: " + e.getMessage());
            return;
        }
        
        // ✅ ACTUALIZAR CON BLOB
        String sqlActualizar = "UPDATE presupuestos SET " +
                              "ruta_archivo_pdf = ?, " +
                              "nombre_archivo_pdf = ?, " +
                              "archivo_pdf_contenido = ? " +
                              "WHERE cliente_id = ?";
        
        PreparedStatement stmt = conn.prepareStatement(sqlActualizar);
        stmt.setString(1, rutaCompleta);
        stmt.setString(2, nombreArchivo);
        stmt.setBytes(3, pdfBytes);  // ← AQUÍ SE GUARDA EL PDF COMPLETO
        stmt.setInt(4, clienteId);
        
        int filasActualizadas = stmt.executeUpdate();
        
        if (filasActualizadas > 0) {
            System.out.println("✅ PDF guardado en BD como BLOB: " + pdfBytes.length + " bytes");
            System.out.println("✅ Ruta del PDF: " + rutaCompleta);
            System.out.println("✅ ¡Ahora disponible desde cualquier computadora!");
        } else {
            System.out.println("⚠️ No se pudo actualizar el PDF en BD");
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error al guardar PDF BLOB en BD: " + e.getMessage());
    }
}
    
    // ========== MÉTODO NUEVO: DESCARGAR PDF DESDE BD ==========
public static boolean descargarPDFPresupuestoDesdeBD(int clienteId, String rutaDestino) {
    try (Connection conn = Conexion.conectar()) {
        
        String sql = "SELECT archivo_pdf_contenido, nombre_archivo_pdf, cliente_nombre " +
                    "FROM presupuestos WHERE cliente_id = ? " +
                    "ORDER BY fecha_creacion DESC LIMIT 1";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, clienteId);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            byte[] pdfBytes = rs.getBytes("archivo_pdf_contenido");
            String nombreArchivo = rs.getString("nombre_archivo_pdf");
            String clienteNombre = rs.getString("cliente_nombre");
            
            if (pdfBytes != null && pdfBytes.length > 0) {
                // Si no hay nombre de archivo, generar uno
                if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
                    nombreArchivo = "Presupuesto_" + clienteNombre.replace(" ", "_") + ".pdf";
                }
                
                // Crear archivo
                java.io.File archivo = new java.io.File(rutaDestino, nombreArchivo);
                java.nio.file.Files.write(archivo.toPath(), pdfBytes);
                
                System.out.println("✅ Presupuesto descargado: " + archivo.getAbsolutePath());
                return true;
            } else {
                System.err.println("❌ No hay contenido PDF en la base de datos para cliente ID: " + clienteId);
                return false;
            }
        } else {
            System.err.println("❌ No se encontró presupuesto para cliente ID: " + clienteId);
            return false;
        }
        
    } catch (Exception e) {
        System.err.println("❌ Error descargando presupuesto: " + e.getMessage());
        return false;
    }
}

    // CONFIGURACIÓN DE EMAIL

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

   // ========== MÉTODO SIMPLE: EMAIL COMPLETO SIN DEPENDENCIAS ==========
private String crearContenidoEmailCompleto(ClienteContrato cliente) {
    System.out.println("📧 Creando contenido de email completo para: " + cliente.getNombreCompleto());
    
    // Variables para almacenar datos del presupuesto
    String paqueteNombre = cliente.getPaquete();
    String paqueteDescripcion = "";
    double paquetePrecio = 0.0;
    double totalGeneral = 0.0;
    String numeroPresupuesto = "";
    String estadoPresupuesto = cliente.getEstado();
    String extrasDetalle = "";
    double totalExtras = 0.0;
    
    // Obtener datos de la base de datos
    try (Connection conn = Conexion.conectar()) {
        String sql = """
            SELECT 
                numero_presupuesto,
                estado,
                paquete_precio,
                total_general,
                extras_detalle,
                total_extras,
                paq.nombre as paquete_nombre,
                paq.descripcion as paquete_descripcion
            FROM presupuestos p
            LEFT JOIN paquetes paq ON p.paquete_id = paq.id
            WHERE p.cliente_id = ? 
            ORDER BY p.fecha_creacion DESC 
            LIMIT 1
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, cliente.getClienteId());
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            numeroPresupuesto = rs.getString("numero_presupuesto") != null ? rs.getString("numero_presupuesto") : "";
            estadoPresupuesto = rs.getString("estado") != null ? rs.getString("estado") : cliente.getEstado();
            paquetePrecio = rs.getDouble("paquete_precio");
            totalGeneral = rs.getDouble("total_general");
            extrasDetalle = rs.getString("extras_detalle") != null ? rs.getString("extras_detalle") : "";
            totalExtras = rs.getDouble("total_extras");
            
            String paqNombre = rs.getString("paquete_nombre");
            if (paqNombre != null) paqueteNombre = paqNombre;
            
            String paqDesc = rs.getString("paquete_descripcion");
            if (paqDesc != null) paqueteDescripcion = paqDesc;
            
            System.out.println("✅ Datos obtenidos - Paquete: " + paqueteNombre + ", Total: $" + totalGeneral);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error al obtener datos del presupuesto: " + e.getMessage());
        // Usar datos básicos del cliente si hay error
        totalGeneral = Double.parseDouble(cliente.getMontoTotal().replace("$", "").replace(",", ""));
    }
    
    // Construir HTML del email
    StringBuilder html = new StringBuilder();
    
    html.append("<!DOCTYPE html>")
        .append("<html><head><meta charset='UTF-8'>")
        .append("<style>")
        .append("body { font-family: 'Segoe UI', Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; background-color: #f5f5f5; }")
        .append(".container { max-width: 700px; margin: 0 auto; background-color: white; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }")
        .append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }")
        .append(".content { padding: 40px 30px; }")
        .append(".seccion { margin-bottom: 30px; padding: 20px; background-color: #f8f9fa; border-radius: 10px; border-left: 4px solid #667eea; }")
        .append(".seccion h3 { margin-top: 0; color: #2c3e50; font-size: 18px; }")
        .append(".tabla-info { width: 100%; border-collapse: collapse; }")
        .append(".tabla-info td { padding: 10px; border-bottom: 1px solid #e9ecef; }")
        .append(".tabla-info .label { font-weight: bold; color: #495057; width: 40%; }")
        .append(".tabla-info .value { color: #2c3e50; }")
        .append(".paquete-destacado { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 15px; margin: 25px 0; }")
        .append(".resumen-financiero { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); padding: 25px; border-radius: 15px; margin: 25px 0; }")
        .append(".info-adicional { background-color: #d1ecf1; padding: 20px; border-radius: 10px; border-left: 4px solid #0c5460; margin: 25px 0; }")
        .append(".contacto { background-color: #d4edda; padding: 20px; border-radius: 10px; border-left: 4px solid #28a745; text-align: center; }")
        .append("</style>")
        .append("</head><body>")
        
        .append("<div class='container'>")
        
        // HEADER
        .append("<div class='header'>")
        .append("<h1 style='margin: 0; font-size: 32px;'>🎨 FORGESTUDIO</h1>")
        .append("<p style='margin: 10px 0 0 0; font-size: 18px; opacity: 0.9;'>Creamos experiencias inolvidables</p>")
        .append("</div>")
        
        // CONTENIDO
        .append("<div class='content'>")
        .append("<h2 style='color: #2c3e50; margin-bottom: 20px;'>🎉 Presupuesto para su Evento</h2>")
        .append("<p>Estimado/a <strong>").append(cliente.getNombreCompleto()).append("</strong>,</p>")
        .append("<p>Nos complace presentarle el presupuesto personalizado para su evento especial. En <strong>ForgeStudio</strong>, nos especializamos en crear experiencias únicas e inolvidables.</p>");
    
    // === INFORMACIÓN DEL PRESUPUESTO ===
    html.append("<div class='seccion'>")
        .append("<h3>📋 Información del Presupuesto</h3>")
        .append("<table class='tabla-info'>")
        .append("<tr><td class='label'>📅 Fecha:</td><td class='value'>").append(cliente.getFechaContrato()).append("</td></tr>")
        .append("<tr><td class='label'>📊 Estado del presupuesto:</td><td class='value'>").append(estadoPresupuesto).append("</td></tr>");
    
    if (!numeroPresupuesto.isEmpty()) {
        html.append("<tr><td class='label'>🔢 Número de presupuesto:</td><td class='value'>").append(numeroPresupuesto).append("</td></tr>");
    }
    
    html.append("<tr><td class='label'>⏰ Válido por:</td><td class='value'>30 días desde la fecha de generación</td></tr>")
        .append("</table></div>");
    
    // === PAQUETE SELECCIONADO ===
    html.append("<div class='paquete-destacado'>")
        .append("<h3 style='margin: 0 0 15px 0;'>📦 Paquete Seleccionado</h3>")
        .append("<div style='font-size: 24px; font-weight: bold; margin-bottom: 10px;'>").append(paqueteNombre).append("</div>");
    
    if (paquetePrecio > 0) {
        html.append("<div style='font-size: 20px; opacity: 0.9;'>$").append(String.format("%.0f", paquetePrecio)).append(" MXN</div>");
    } else {
        html.append("<div style='font-size: 20px; opacity: 0.9;'>").append(cliente.getMontoTotal()).append("</div>");
    }
    
    if (!paqueteDescripcion.isEmpty()) {
        html.append("<div style='margin-top: 15px; padding: 15px; background-color: rgba(255,255,255,0.2); border-radius: 10px; font-size: 14px;'>")
            .append("<strong>Descripción:</strong><br>").append(paqueteDescripcion)
            .append("</div>");
    }
    
    html.append("</div>");
    
    // === SERVICIOS ADICIONALES ===
    html.append("<div class='seccion'>")
        .append("<h3>✨ Servicios Adicionales</h3>");
    
    if (!extrasDetalle.isEmpty() && !extrasDetalle.equals("Sin extras")) {
        html.append("<div style='background-color: white; padding: 15px; border-radius: 8px; border-left: 4px solid #3498db;'>")
            .append("<p>").append(extrasDetalle.replace("\n", "<br>")).append("</p>")
            .append("</div>");
        
        if (totalExtras > 0) {
            html.append("<div style='margin-top: 15px; padding: 15px; background-color: #e8f5e8; border-radius: 8px; text-align: right;'>")
                .append("<strong style='color: #28a745; font-size: 18px;'>Total Servicios Adicionales: $")
                .append(String.format("%.0f", totalExtras)).append(" MXN</strong>")
                .append("</div>");
        }
    } else {
        html.append("<p style='color: #6c757d; font-style: italic; text-align: center; padding: 20px;'>")
            .append("No se han seleccionado servicios adicionales para este presupuesto.")
            .append("</p>");
    }
    
    html.append("</div>");
    
    // === RESUMEN FINANCIERO ===
    double subtotal = (paquetePrecio > 0 ? paquetePrecio : totalGeneral - totalExtras) + totalExtras;
    
    html.append("<div class='resumen-financiero'>")
        .append("<h3 style='color: #2c3e50; margin: 0 0 20px 0; text-align: center;'>💰 Resumen Financiero</h3>")
        .append("<table style='width: 100%; font-size: 16px;'>");
    
    if (paquetePrecio > 0) {
        html.append("<tr><td style='padding: 8px 0; color: #495057;'>Paquete base:</td><td style='text-align: right; font-weight: bold;'>$").append(String.format("%.0f", paquetePrecio)).append("</td></tr>");
    }
    
    if (totalExtras > 0) {
        html.append("<tr><td style='padding: 8px 0; color: #495057;'>Servicios adicionales:</td><td style='text-align: right; font-weight: bold;'>$").append(String.format("%.0f", totalExtras)).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0; color: #495057;'>Subtotal:</td><td style='text-align: right; font-weight: bold;'>$").append(String.format("%.0f", subtotal)).append("</td></tr>");
    }
    
    html.append("<tr style='border-top: 3px solid #667eea; font-size: 20px; color: #667eea;'>")
        .append("<td style='padding: 15px 0; font-weight: bold;'>TOTAL GENERAL:</td>")
        .append("<td style='text-align: right; font-weight: bold;'>$").append(String.format("%.0f", totalGeneral)).append(" MXN</td>")
        .append("</tr></table>")
        .append("</div>");
    
    // === INFORMACIÓN ADICIONAL ===
    html.append("<div class='info-adicional'>")
        .append("<h3 style='color: #0c5460; margin-top: 0;'>ℹ️ Información Adicional</h3>")
        .append("<ul style='color: #155724; line-height: 1.8;'>")
        .append("<li>Este presupuesto es válido por 30 días desde su generación</li>")
        .append("<li>Se requiere el 50% de anticipo para confirmar la reserva del evento</li>")
        .append("<li>El evento incluye todos los servicios especificados en el paquete</li>")
        .append("<li>Los precios están expresados en pesos mexicanos (MXN)</li>")
        .append("<li>Para dudas o modificaciones, contáctanos inmediatamente</li>")
        .append("</ul></div>");
    
    // Nota PDF adjunto
    html.append("<div style='background-color: #fff3cd; padding: 20px; border-radius: 10px; border-left: 4px solid #ffc107; margin: 25px 0; text-align: center;'>")
        .append("<p style='margin: 0; color: #856404;'><strong>📎 Archivo Adjunto</strong></p>")
        .append("<p style='margin: 10px 0 0 0; color: #856404;'>En el archivo PDF adjunto encontrará todos los detalles específicos y términos completos de su presupuesto.</p>")
        .append("</div>");
    
    // === CONTACTO ===
    html.append("<div class='contacto'>")
        .append("<h3 style='color: #155724; margin-top: 0;'>📞 Contacto Directo</h3>")
        .append("<p style='color: #155724; margin: 5px 0; font-size: 16px;'><strong>Email:</strong> ").append(EMAIL_EMPRESA).append("</p>")
        .append("<p style='color: #155724; margin: 5px 0; font-size: 16px;'><strong>Empresa:</strong> ").append(NOMBRE_EMPRESA).append("</p>")
        .append("<p style='color: #155724; margin: 15px 0 0 0; font-weight: bold;'>¡Estamos aquí para hacer de su evento algo especial!</p>")
        .append("</div>");
    
    html.append("<p style='text-align: center; color: #7f8c8d; margin: 30px 0 20px 0;'>Gracias por confiar en <strong>ForgeStudio</strong> para su evento especial.</p>")
        .append("<p style='text-align: center;'>Saludos cordiales,<br><strong style='color: #667eea; font-size: 18px;'>Equipo ForgeStudio</strong></p>")
        .append("</div>");
    
    // FOOTER
    html.append("<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px; background-color: #f8f9fa;'>")
        .append("<p>Este presupuesto fue generado automáticamente el ")
        .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")))
        .append("</p>")
        .append("<p>© ").append(java.time.Year.now()).append(" ForgeStudio - Todos los derechos reservados</p>")
        .append("</div>")
        .append("</div>")
        .append("</body></html>");
    
    System.out.println("✅ Contenido HTML del email generado correctamente");
    return html.toString();
}

    // MÉTODOS PARA ENVÍO DE EMAIL 

   @FXML 
private void enviarEmail() {
    if (clienteActualmenteSeleccionado != null) {
        if (!clienteActualmenteSeleccionado.getEmail().equals("Sin email")) {
            // Primero verificar si existe un PDF o crear uno
            String rutaPDF = buscarPDFExistente(clienteActualmenteSeleccionado.getClienteId());
            
            if (rutaPDF == null) {
                // No existe PDF, preguntar si quiere generar uno
                mostrarConfirmacionSimple("📧 Enviar Email", 
                                          "No se encontró PDF.\n\n¿Deseas generar y enviar?",
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
            
            // Texto del email
            MimeBodyPart textoParte = new MimeBodyPart();
            String contenidoEmail = crearContenidoEmailCompleto(cliente);
            textoParte.setContent(contenidoEmail, "text/html; charset=utf-8");
            multipart.addBodyPart(textoParte);
            
            // Adjuntar PDF
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
            
        } catch (IOException | MessagingException e) {
            mostrarMensaje("Error", "Error al preparar email: " + e.getMessage(), "#e74c3c");
        }
    }

    private void registrarEnvioEmail(int clienteId, String emailDestinatario) {
        System.out.println("📧 Email enviado a: " + emailDestinatario + " (Cliente ID: " + clienteId + ")");
    }

    // ========== MÉTODOS CORREGIDOS PARA VER PDF ==========

    @FXML 
    private void verContratoPDF() {
        if (clienteActualmenteSeleccionado != null) {
            System.out.println("🔍 Buscando PDF para: " + clienteActualmenteSeleccionado.getNombreCompleto());
            
            try {
                // PASO 1: Buscar en Desktop (donde se guardan los PDFs nuevos)
                String rutaPDFDesktop = buscarPDFEnDesktop(clienteActualmenteSeleccionado);
                
                if (rutaPDFDesktop != null) {
                    // PDF encontrado en Desktop
                    System.out.println("✅ PDF encontrado en Desktop: " + rutaPDFDesktop);
                    abrirPDF(rutaPDFDesktop);
                    mostrarMensaje("PDF Abierto", "PDF abierto desde Desktop:\n" + 
                                 new File(rutaPDFDesktop).getName(), "#27ae60");
                    return;
                }
                
                // PASO 2: Buscar en base de datos como BLOB
                boolean pdfEnBD = verificarPDFEnBD(clienteActualmenteSeleccionado.getClienteId());
                
                if (pdfEnBD) {
                    // PDF existe en BD, extraer y abrir
                    System.out.println("✅ PDF encontrado en BD, extrayendo...");
                    boolean extraido = extraerYAbrirPDFDesdeBD(clienteActualmenteSeleccionado);
                    
                    if (extraido) {
                        mostrarMensaje("PDF Abierto", "PDF extraído desde base de datos y abierto correctamente", "#27ae60");
                        return;
                    }
                }
                
                // PASO 3: No existe PDF, preguntar si generar uno nuevo
                mostrarConfirmacionSimple("📄 Generar PDF", 
                                         "No se encontró PDF para este cliente.\n\n" +
                                         "¿Deseas generar un presupuesto completo?",
                                         () -> {
                                             generarPDFPresupuestoCompleto(clienteActualmenteSeleccionado);
                                         });
                
            } catch (Exception e) {
                System.err.println("❌ Error al buscar PDF: " + e.getMessage());
                mostrarMensaje("Error", "Error al buscar PDF: " + e.getMessage(), "#e74c3c");
            }
        } else {
            mostrarMensaje("Atención", "Selecciona un cliente de la tabla", "#f39c12");
        }
    }

    // ========== MÉTODO NUEVO: BUSCAR PDF EN DESKTOP ==========
    private String buscarPDFEnDesktop(ClienteContrato cliente) {
    try {
        // Lista de ubicaciones posibles en Desktop
        String[] rutasDesktop = {
            System.getProperty("user.home") + "/Desktop/Presupuestos",
            System.getProperty("user.home") + "\\Desktop\\Presupuestos",
            "Presupuestos", // Carpeta local
            System.getProperty("user.dir") + "/Presupuestos"
        };
        
        String nombreCliente = cliente.getNombreCompleto()
            .replaceAll("[^a-zA-Z0-9\\s]", "") // Remover caracteres especiales
            .replaceAll("\\s+", "_")           // Reemplazar espacios con _
            .trim();
        
        if (nombreCliente.isEmpty()) {
            nombreCliente = "Cliente_Sin_Nombre";
        }
        
        System.out.println("🔍 Buscando PDF ESPECÍFICO para cliente: " + nombreCliente);
        System.out.println("🔍 Cliente ID: " + cliente.getClienteId());
        
        // Buscar en cada ubicación posible
        for (String rutaBase : rutasDesktop) {
            File carpeta = new File(rutaBase);
            
            if (!carpeta.exists()) {
                System.out.println("📁 Carpeta no existe: " + rutaBase);
                continue;
            }
            
            System.out.println("📁 Buscando en: " + carpeta.getAbsolutePath());
            
            // Listar todos los archivos PDF en la carpeta
            File[] archivos = carpeta.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".pdf"));
            
            if (archivos == null || archivos.length == 0) {
                System.out.println("📄 No hay PDFs en: " + rutaBase);
                continue;
            }
            
            System.out.println("📄 Encontrados " + archivos.length + " PDFs en la carpeta");
            
            // ⭐ BÚSQUEDA MÁS ESPECÍFICA - SOLO COINCIDENCIA EXACTA
            for (File archivo : archivos) {
                String nombreArchivo = archivo.getName().toLowerCase();
                String nombreClienteLower = nombreCliente.toLowerCase();
                
                System.out.println("🔍 Comparando archivo: " + archivo.getName());
                System.out.println("🔍 Con cliente: " + nombreCliente);
                
                // ✅ PATRÓN 1: Coincidencia EXACTA con nombre completo
                if (nombreArchivo.contains(nombreClienteLower)) {
                    System.out.println("✅ COINCIDENCIA EXACTA por nombre completo: " + archivo.getName());
                    
                    if (archivo.exists() && archivo.canRead()) {
                        System.out.println("🎯 PDF ESPECÍFICO encontrado: " + archivo.getAbsolutePath());
                        return archivo.getAbsolutePath();
                    }
                }
                
                // ✅ PATRÓN 2: Coincidencia por TODAS las palabras del nombre
                String[] palabrasCliente = nombreClienteLower.split("_");
                if (palabrasCliente.length >= 2) { // Al menos nombre y apellido
                    boolean todasLasPalabrasCoinciden = true;
                    
                    for (String palabra : palabrasCliente) {
                        if (palabra.length() > 2 && !nombreArchivo.contains(palabra)) {
                            todasLasPalabrasCoinciden = false;
                            break;
                        }
                    }
                    
                    if (todasLasPalabrasCoinciden) {
                        System.out.println("✅ COINCIDENCIA COMPLETA por todas las palabras: " + archivo.getName());
                        
                        if (archivo.exists() && archivo.canRead()) {
                            System.out.println("🎯 PDF ESPECÍFICO encontrado: " + archivo.getAbsolutePath());
                            return archivo.getAbsolutePath();
                        }
                    }
                }
            }
            
            System.out.println("❌ No se encontró PDF específico para: " + nombreCliente + " en " + rutaBase);
        }
        
        System.out.println("❌ NO se encontró PDF específico en Desktop para: " + nombreCliente);
        return null;
        
    } catch (Exception e) {
        System.err.println("❌ Error buscando PDF específico en Desktop: " + e.getMessage());
        return null;
    }
}

private boolean verificarPDFCorrespondeAlCliente(String rutaPDF, ClienteContrato cliente) {
    try {
        String nombreArchivo = new File(rutaPDF).getName().toLowerCase();
        String nombreCliente = cliente.getNombreCompleto()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", "_")
            .toLowerCase();
        
        System.out.println("🔍 Verificando correspondencia:");
        System.out.println("   Archivo: " + nombreArchivo);
        System.out.println("   Cliente: " + nombreCliente);
        
        // Verificar que TODAS las palabras del nombre del cliente están en el archivo
        String[] palabrasCliente = nombreCliente.split("_");
        int coincidencias = 0;
        
        for (String palabra : palabrasCliente) {
            if (palabra.length() > 2 && nombreArchivo.contains(palabra)) {
                coincidencias++;
                System.out.println("   ✅ Palabra encontrada: " + palabra);
            } else if (palabra.length() > 2) {
                System.out.println("   ❌ Palabra NO encontrada: " + palabra);
            }
        }
        
        // Debe coincidir al menos el 80% de las palabras significativas
        int palabrasSignificativas = (int) java.util.Arrays.stream(palabrasCliente)
            .filter(p -> p.length() > 2)
            .count();
            
        double porcentajeCoincidencia = (double) coincidencias / palabrasSignificativas;
        
        System.out.println("   Coincidencias: " + coincidencias + "/" + palabrasSignificativas + 
                          " (" + String.format("%.1f", porcentajeCoincidencia * 100) + "%)");
        
        boolean corresponde = porcentajeCoincidencia >= 0.8; // Al menos 80% de coincidencia
        
        if (corresponde) {
            System.out.println("   ✅ PDF CORRESPONDE al cliente");
        } else {
            System.out.println("   ❌ PDF NO CORRESPONDE al cliente");
        }
        
        return corresponde;
        
    } catch (Exception e) {
        System.err.println("❌ Error verificando correspondencia de PDF: " + e.getMessage());
        return false;
    }
}
    // ========== MÉTODO MEJORADO: EXTRAER Y ABRIR PDF DESDE BD ==========
    private boolean extraerYAbrirPDFDesdeBD(ClienteContrato cliente) {
        try (Connection conn = Conexion.conectar()) {
            
            String sql = "SELECT archivo_pdf_contenido, nombre_archivo_pdf FROM presupuestos " +
                        "WHERE cliente_id = ? AND archivo_pdf_contenido IS NOT NULL " +
                        "ORDER BY fecha_creacion DESC LIMIT 1";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cliente.getClienteId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                byte[] pdfBytes = rs.getBytes("archivo_pdf_contenido");
                String nombreArchivo = rs.getString("nombre_archivo_pdf");
                
                if (pdfBytes != null && pdfBytes.length > 0) {
                    System.out.println("✅ PDF encontrado en BD: " + pdfBytes.length + " bytes");
                    
                    // Crear carpeta temporal
                    String rutaTemp = System.getProperty("java.io.tmpdir") + File.separator + "PresupuestosPDFs";
                    File carpetaTemp = new File(rutaTemp);
                    carpetaTemp.mkdirs();
                    
                    // Generar nombre si no existe
                    if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
                        String nombreCliente = cliente.getNombreCompleto().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
                        nombreArchivo = "Presupuesto_" + nombreCliente + "_" + 
                                       java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
                    }
                    
                    // Crear archivo temporal
                    File archivoTemp = new File(carpetaTemp, nombreArchivo);
                    
                    // Escribir bytes al archivo
                    java.nio.file.Files.write(archivoTemp.toPath(), pdfBytes);
                    
                    System.out.println("✅ PDF extraído a: " + archivoTemp.getAbsolutePath());
                    
                    // Abrir el archivo
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(archivoTemp);
                        
                        // Programar eliminación después de 10 minutos
                        programarEliminacionTemporal(archivoTemp, 10);
                        
                        return true;
                    } else {
                        System.err.println("❌ Desktop no soportado en este sistema");
                        mostrarMensaje("PDF Extraído", 
                                      "PDF extraído pero no se puede abrir automáticamente.\n" +
                                      "Ubicación: " + archivoTemp.getAbsolutePath(), "#f39c12");
                        return false;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Error extrayendo PDF desde BD: " + e.getMessage());
            return false;
        }
    }

    // ========== MÉTODO AUXILIAR: PROGRAMAR ELIMINACIÓN TEMPORAL ==========
    private void programarEliminacionTemporal(File archivo, int minutos) {
        Timer timer = new Timer(true); // Timer daemon
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    if (archivo.exists()) {
                        boolean eliminado = archivo.delete();
                        if (eliminado) {
                            System.out.println("🗑️ Archivo temporal eliminado: " + archivo.getName());
                        } else {
                            System.out.println("⚠️ No se pudo eliminar: " + archivo.getName());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error eliminando temporal: " + e.getMessage());
                }
            }
        }, minutos * 60 * 1000L); // Convertir minutos a milisegundos
    }

    // ========== MÉTODO AUXILIAR: VERIFICAR PDF EN BD ==========
    private boolean verificarPDFEnBD(int clienteId) {
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT archivo_pdf_contenido FROM presupuestos " +
                        "WHERE cliente_id = ? AND archivo_pdf_contenido IS NOT NULL " +
                        "ORDER BY fecha_creacion DESC LIMIT 1";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                byte[] pdfBytes = rs.getBytes("archivo_pdf_contenido");
                return pdfBytes != null && pdfBytes.length > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Error verificando PDF en BD: " + e.getMessage());
            return false;
        }
    }

    // ========== MÉTODOS PARA BOTONES DE ACCIÓN ==========

    @FXML
private void limpiarPresupuestosManual() {
    mostrarConfirmacionSimple("🧹 Confirmar Limpieza", 
                             "Se eliminarán presupuestos con más de 30 días.\n\n" + // ⭐ CORREGIDO
                             "⚠️ Esta acción NO se puede deshacer.\n\n" +
                             "Los contratos firmados NO se eliminarán.",
                             () -> {
                                 limpiarPresupuestosVencidos();
                                 cargarDatos();
                                 mostrarMensaje("Éxito", "Presupuestos vencidos (>30 días) eliminados correctamente", "#27ae60");
                             });
}

    @FXML
    private void actualizarDatos() {
        System.out.println("🔄 Actualizando presupuestos...");
        
        // Debug básico en consola
        try (Connection conn = Conexion.conectar()) {
            // Verificar presupuestos recientes
            String sql = "SELECT COUNT(*) as total FROM presupuestos WHERE fecha_creacion >= DATE_SUB(NOW(), INTERVAL 1 DAY)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("📊 Presupuestos del último día: " + total);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error verificando presupuestos: " + e.getMessage());
        }
        
        // Limpiar y recargar
        limpiarPresupuestosVencidos();
        cargarDatos();
        
        System.out.println("✅ Actualización completada");
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

   // ========== MÉTODO CORREGIDO: VER DETALLES COMPLETOS ==========
@FXML 
private void verDetalles() {
    if (clienteActualmenteSeleccionado != null) {
        
        // Obtener datos completos del presupuesto
        DatosPresupuestoCompleto datosCompletos = obtenerDatosPresupuestoCompleto(clienteActualmenteSeleccionado.getClienteId());
        
        StringBuilder detalles = new StringBuilder();
        
        // === HEADER ===
        detalles.append("🎉 DETALLES COMPLETOS DEL PRESUPUESTO\n");
        detalles.append("=" .repeat(50)).append("\n\n");
        
        // === DATOS DEL CLIENTE ===
        detalles.append("👤 INFORMACIÓN DEL CLIENTE\n");
        detalles.append("-".repeat(30)).append("\n");
        detalles.append("Nombre: ").append(clienteActualmenteSeleccionado.getNombreCompleto()).append("\n");
        detalles.append("Teléfono: ").append(clienteActualmenteSeleccionado.getTelefono()).append("\n");
        detalles.append("Email: ").append(clienteActualmenteSeleccionado.getEmail()).append("\n\n");
        
        // === INFORMACIÓN DEL PRESUPUESTO ===
        detalles.append("📋 INFORMACIÓN DEL PRESUPUESTO\n");
        detalles.append("-".repeat(30)).append("\n");
        detalles.append("Fecha: ").append(clienteActualmenteSeleccionado.getFechaContrato()).append("\n");
        detalles.append("Estado: ").append(clienteActualmenteSeleccionado.getEstado()).append("\n\n");
        
        // === PAQUETE BASE ===
        detalles.append("📦 PAQUETE SELECCIONADO\n");
        detalles.append("-".repeat(30)).append("\n");
        detalles.append("Nombre: ").append(clienteActualmenteSeleccionado.getPaquete()).append("\n");
        
        if (datosCompletos != null) {
            if (datosCompletos.paqueteDescripcion != null && !datosCompletos.paqueteDescripcion.trim().isEmpty()) {
                detalles.append("Descripción: ").append(datosCompletos.paqueteDescripcion).append("\n");
            }
            detalles.append("Precio: $").append(String.format("%.0f", datosCompletos.paquetePrecio)).append(" MXN\n\n");
            
            // === EXTRAS SELECCIONADOS ===
            if (datosCompletos.extras != null && !datosCompletos.extras.isEmpty()) {
                boolean tieneExtrasSeleccionados = datosCompletos.extras.stream().anyMatch(extra -> extra.getCantidad() > 0);
                
                if (tieneExtrasSeleccionados) {
                    detalles.append("✨ SERVICIOS ADICIONALES\n");
                    detalles.append("-".repeat(30)).append("\n");
                    
                    double totalExtras = 0.0;
                    for (Extra extra : datosCompletos.extras) {
                        if (extra.getCantidad() > 0) {
                            double subtotalExtra = extra.getPrecio() * extra.getCantidad();
                            detalles.append("• ").append(extra.getNombre()).append("\n");
                            detalles.append("  Cantidad: ").append(extra.getCantidad());
                            detalles.append(" | Precio unitario: $").append(String.format("%.0f", extra.getPrecio()));
                            detalles.append(" | Subtotal: $").append(String.format("%.0f", subtotalExtra)).append(" MXN\n");
                            totalExtras += subtotalExtra;
                        }
                    }
                    detalles.append("\nTotal Extras: $").append(String.format("%.0f", totalExtras)).append(" MXN\n\n");
                } else {
                    detalles.append("✨ SERVICIOS ADICIONALES\n");
                    detalles.append("-".repeat(30)).append("\n");
                    detalles.append("Sin servicios adicionales seleccionados\n\n");
                }
            } else {
                // Intentar obtener extras desde la tabla presupuestos (campo extras_detalle)
                String extrasDetalle = obtenerExtrasDetalleDesdeBD(clienteActualmenteSeleccionado.getClienteId());
                if (extrasDetalle != null && !extrasDetalle.trim().isEmpty() && !extrasDetalle.equals("Sin extras")) {
                    detalles.append("✨ SERVICIOS ADICIONALES\n");
                    detalles.append("-".repeat(30)).append("\n");
                    detalles.append(extrasDetalle).append("\n\n");
                } else {
                    detalles.append("✨ SERVICIOS ADICIONALES\n");
                    detalles.append("-".repeat(30)).append("\n");
                    detalles.append("Sin servicios adicionales seleccionados\n\n");
                }
            }
            
            // === RESUMEN FINANCIERO ===
            detalles.append("💰 RESUMEN FINANCIERO\n");
            detalles.append("-".repeat(30)).append("\n");
            
            if (datosCompletos.subtotal > 0) {
                detalles.append("Subtotal: $").append(String.format("%.0f", datosCompletos.subtotal)).append(" MXN\n");
            }
            
            if (datosCompletos.iva > 0) {
                detalles.append("IVA (16%): $").append(String.format("%.0f", datosCompletos.iva)).append(" MXN\n");
            }
            
            detalles.append("TOTAL GENERAL: $").append(String.format("%.0f", datosCompletos.total)).append(" MXN\n\n");
        } else {
            // Si no se pueden obtener datos completos, usar información básica
            detalles.append("💰 MONTO TOTAL: ").append(clienteActualmenteSeleccionado.getMontoTotal()).append(" MXN\n\n");
        }
        
        // === INFORMACIÓN ADICIONAL ===
        detalles.append("ℹ️ INFORMACIÓN ADICIONAL\n");
        detalles.append("-".repeat(30)).append("\n");
        detalles.append("• Presupuesto válido por 30 días desde su generación\n");
        detalles.append("• Para confirmar, se requiere anticipo del 50%\n");
        detalles.append("• Precios en pesos mexicanos (MXN)\n");
        
        // Verificar si tiene PDF disponible
        boolean tienePDFEnBD = verificarPDFEnBD(clienteActualmenteSeleccionado.getClienteId());
        String rutaPDFLocal = buscarPDFExistente(clienteActualmenteSeleccionado.getClienteId());
        
        if (tienePDFEnBD) {
            detalles.append("• ✅ PDF disponible en base de datos\n");
        } else if (rutaPDFLocal != null) {
            detalles.append("• ✅ PDF disponible localmente\n");
        } else {
            detalles.append("• ⚠️ PDF no generado aún\n");
        }
        
        detalles.append("\n").append("=" .repeat(50));
        
        // Mostrar detalles completos
        mostrarMensaje("📋 Detalles Completos del Presupuesto", detalles.toString(), "#3498db");
        
    } else {
        mostrarMensaje("Atención", "Selecciona un cliente de la tabla", "#f39c12");
    }
}

// ========== MÉTODO AUXILIAR: OBTENER EXTRAS DETALLE DESDE BD ==========
private String obtenerExtrasDetalleDesdeBD(int clienteId) {
    try (Connection conn = Conexion.conectar()) {
        
        String sql = "SELECT extras_detalle, extras, total_extras FROM presupuestos " +
                    "WHERE cliente_id = ? ORDER BY fecha_creacion DESC LIMIT 1";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            String extrasDetalle = rs.getString("extras_detalle");
            String extras = rs.getString("extras");
            double totalExtras = rs.getDouble("total_extras");
            
            // Priorizar extras_detalle
            if (extrasDetalle != null && !extrasDetalle.trim().isEmpty() && !extrasDetalle.equals("Sin extras")) {
                if (totalExtras > 0) {
                    return extrasDetalle + "\n\nTotal Extras: $" + String.format("%.0f", totalExtras) + " MXN";
                }
                return extrasDetalle;
            }
            
            // Si no hay extras_detalle, usar campo extras
            if (extras != null && !extras.trim().isEmpty() && !extras.equals("Sin extras")) {
                if (totalExtras > 0) {
                    return extras + "\n\nTotal Extras: $" + String.format("%.0f", totalExtras) + " MXN";
                }
                return extras;
            }
            
            // Si hay total_extras pero no descripción
            if (totalExtras > 0) {
                return "Servicios adicionales incluidos\nTotal Extras: $" + String.format("%.0f", totalExtras) + " MXN";
            }
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error al obtener extras detalle: " + e.getMessage());
    }
    
    return null;
}

    @FXML 
    private void eliminarSeleccionado() {
        if (clienteActualmenteSeleccionado != null) {
            mostrarConfirmacionSimple("🗑️ Confirmar Eliminación",
                                     "¿Eliminar el presupuesto de:\n\n" +
                                     "👤 " + clienteActualmenteSeleccionado.getNombreCompleto() + "\n" +
                                     "💰 " + clienteActualmenteSeleccionado.getMontoTotal() + "\n\n" +
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

    // MÉTODOS PARA MANEJO DE PDF 

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

    // MÉTODOS PARA INTERFAZ DE USUARIO

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

    // ========== MÉTODO: CONFIRMACIÓN SIMPLE (SÍ/NO) ==========
    private void mostrarConfirmacionSimple(String titulo, String mensaje, Runnable accionConfirmar) {
        if (panelMensajes != null && lblTituloMensaje != null && contenedorMensajes != null) {
            lblTituloMensaje.setText(titulo);
            lblTituloMensaje.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            
            contenedorMensajes.getChildren().clear();
            
            VBox tarjetaConfirmacion = new VBox(15);
            tarjetaConfirmacion.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                       "-fx-background-radius: 15px; " +
                                       "-fx-padding: 20px; " +
                                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                                       "-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 15px;");

            Label lblMensaje = new Label(mensaje);
            lblMensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            lblMensaje.setWrapText(true);

            HBox botones = new HBox(10);
            botones.setStyle("-fx-alignment: center;");
            
            Button btnConfirmar = new Button("✓ Confirmar");
            btnConfirmar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8px;");
            btnConfirmar.setOnAction(e -> {
                accionConfirmar.run();
                cerrarMensajes();
            });
            
            Button btnCancelar = new Button("✕ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8px;");
            btnCancelar.setOnAction(e -> cerrarMensajes());
            
            botones.getChildren().addAll(btnConfirmar, btnCancelar);

            tarjetaConfirmacion.getChildren().addAll(lblMensaje, botones);
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

    // CLASE INTERNA PARA DATOS COMPLETOS
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