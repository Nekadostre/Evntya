package controladores;

import database.Conexion;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.util.Duration;
import modelos.Cliente;
import modelos.SesionTemporal;
import modelos.EventoInfo;
import modelos.ReservaDetalle;
import modelos.Extra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarioContratoController {
    private static final boolean DEBUG_MODE = false; // Cambia a true solo cuando necesites debug
    
    private void debug(String mensaje) {
        if (DEBUG_MODE) {
            System.out.println(mensaje);
        }
    }

    @FXML private TextField txtBuscar;
    @FXML private ListView<String> listaResultados;
    @FXML private GridPane gridCalendario;
    @FXML private Label labelMes;
    
    // NUEVOS ELEMENTOS DEL PANEL LATERAL
    @FXML private VBox panelDetalles;
    @FXML private Label lblTituloDetalle;
    @FXML private ScrollPane scrollDetalles;
    @FXML private VBox contenedorDetalles;

    private LocalDate mesActual = LocalDate.now();
    private final List<Cliente> clientesEncontrados = new ArrayList<>();
    private final Map<LocalDate, EventoInfo> eventos = new HashMap<>();
    private final Map<String, Map<String, Object>> reservasCompletas = new HashMap<>();
    private final PauseTransition pausaBusqueda = new PauseTransition(Duration.millis(300));
    private final SesionTemporal sesion = SesionTemporal.getInstancia();

    @FXML
    public void initialize() {
        // Limpiar sesión al inicializar
        sesion.reset();
        
        cargarEventosPagados();
        construirCalendario();

        pausaBusqueda.setOnFinished(e -> ejecutarBusqueda());
        txtBuscar.setOnKeyReleased(this::buscarCliente);
        listaResultados.setOnMouseClicked(e -> seleccionarCliente());
    }

    @FXML
    public void buscarCliente(KeyEvent event) {
         pausaBusqueda.playFromStart();
     }

    private void ejecutarBusqueda() {
        String texto = txtBuscar.getText().trim();
        listaResultados.getItems().clear();
        clientesEncontrados.clear();

        if (texto.isEmpty()) return;

        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT * FROM clientes WHERE nombre LIKE ? OR apellido_paterno LIKE ? OR apellido_materno LIKE ? OR rfc LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, "%" + texto + "%");
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cliente c = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getString("rfc"),
                        "" // CURP vacío ya que no existe la columna
                );
                clientesEncontrados.add(c);
                listaResultados.getItems().add(c.getId() + " - " + c.getNombreCompleto() + " | RFC: " + c.getRfc());
            }
            debug("Clientes encontrados: " + clientesEncontrados.size());
        } catch (SQLException e) {
            mostrarError("Error de búsqueda", "Error al buscar clientes: " + e.getMessage());
        }
    }

    private void seleccionarCliente() {
        int index = listaResultados.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Cliente seleccionado = clientesEncontrados.get(index);

            if (tienePresupuesto(seleccionado.getId())) {
                // Establecer cliente en la sesión
                sesion.setCliente(seleccionado);
                
                // Cargar datos del presupuesto
                if (cargarDatosPresupuesto(seleccionado.getId())) {
                    // CAMBIO: Mostrar en panel lateral en lugar de popup
                    mostrarClienteSeleccionado(seleccionado);
                } else {
                    mostrarError("Error", "Error al cargar el presupuesto del cliente.");
                }
            } else {
                mostrarError("Sin presupuesto", "Este cliente no tiene un presupuesto registrado.");
            }
        }
    }

    // NUEVO MÉTODO: Mostrar cliente seleccionado en panel lateral
    private void mostrarClienteSeleccionado(Cliente cliente) {
        if (panelDetalles != null && contenedorDetalles != null && lblTituloDetalle != null) {
            lblTituloDetalle.setText("✅ Cliente Seleccionado");
            contenedorDetalles.getChildren().clear();
            
            VBox infoCliente = crearTarjetaCliente(cliente);
            contenedorDetalles.getChildren().add(infoCliente);
            
            panelDetalles.setVisible(true);
        }
    }

    // NUEVO MÉTODO: Crear tarjeta de información del cliente CON EXTRAS
    private VBox crearTarjetaCliente(Cliente cliente) {
        VBox tarjeta = new VBox(12);
        tarjeta.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                        "-fx-background-radius: 15px; " +
                        "-fx-padding: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                        "-fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 15px;");

        // Header
        Label headerCliente = new Label("👤 CLIENTE SELECCIONADO");
        headerCliente.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        // Información del cliente
        Label lblNombre = new Label("Nombre: " + cliente.getNombreCompleto());
        lblNombre.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label lblRfc = new Label("RFC: " + cliente.getRfc());
        lblRfc.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        // Información del paquete si existe
        VBox seccionPaquete = new VBox(8);
        seccionPaquete.setStyle("-fx-background-color: #e8f5e8; -fx-background-radius: 12px; -fx-padding: 15px;");

        Label tituloPaquete = new Label("📦 PRESUPUESTO CARGADO");
        tituloPaquete.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #27ae60;");

        Label lblPaquete = new Label("Paquete: " + sesion.getPaqueteNombre());
        lblPaquete.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label lblPrecio = new Label("Precio: $" + String.format("%.2f", sesion.getPaquetePrecio()));
        lblPrecio.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        seccionPaquete.getChildren().addAll(tituloPaquete, lblPaquete, lblPrecio);

        // *** NUEVA SECCIÓN: EXTRAS ***
        VBox seccionExtras = new VBox(8);
        seccionExtras.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 12px; -fx-padding: 15px;");

        Label tituloExtras = new Label("✨ EXTRAS INCLUIDOS");
        tituloExtras.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #e67e22;");

        List<Extra> extras = sesion.getExtrasSeleccionados();
        if (extras != null && !extras.isEmpty()) {
            for (Extra extra : extras) {
                VBox extraItem = new VBox(3);
                extraItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); " +
                                 "-fx-background-radius: 8px; -fx-padding: 10px; " +
                                 "-fx-border-color: #f0f0f0; -fx-border-width: 1px; -fx-border-radius: 8px;");

                Label lblExtraNombre = new Label("• " + extra.getNombre());
                lblExtraNombre.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

                HBox extrasInfo = new HBox(15);
                extrasInfo.setAlignment(Pos.CENTER_LEFT);

                Label lblCantidad = new Label("Cantidad: " + extra.getCantidad());
                lblCantidad.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

                Label lblPrecioExtra = new Label("$" + String.format("%.2f", extra.getPrecio()));
                lblPrecioExtra.setStyle("-fx-font-size: 12px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

                Label lblSubtotal = new Label("Subtotal: $" + String.format("%.2f", extra.getSubtotal()));
                lblSubtotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

                extrasInfo.getChildren().addAll(lblCantidad, lblPrecioExtra, lblSubtotal);
                extraItem.getChildren().addAll(lblExtraNombre, extrasInfo);
                seccionExtras.getChildren().add(extraItem);
            }

            // Total de extras
            Label lblTotalExtras = new Label("Total Extras: $" + String.format("%.2f", sesion.getTotalExtras()));
            lblTotalExtras.setStyle("-fx-font-size: 14px; -fx-text-fill: #e67e22; -fx-font-weight: bold; " +
                                  "-fx-background-color: rgba(230, 126, 34, 0.1); -fx-background-radius: 8px; " +
                                  "-fx-padding: 8px; -fx-alignment: center;");
            seccionExtras.getChildren().add(lblTotalExtras);

        } else {
            Label sinExtras = new Label("Sin extras seleccionados");
            sinExtras.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
            seccionExtras.getChildren().add(sinExtras);
        }

        // Agregar título de extras al inicio
        seccionExtras.getChildren().add(0, tituloExtras);

        // Total general
        Label lblTotal = new Label("💰 TOTAL GENERAL: $" + String.format("%.2f", sesion.getTotalGeneral()));
        lblTotal.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold; " +
                         "-fx-background-color: rgba(39, 174, 96, 0.1); -fx-background-radius: 10px; " +
                         "-fx-padding: 12px; -fx-alignment: center;");

        // Instrucciones
        Label instrucciones = new Label("💡 Ahora selecciona una fecha y horario en el calendario");
        instrucciones.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px; -fx-text-alignment: center;");
        instrucciones.setWrapText(true);

        tarjeta.getChildren().addAll(headerCliente, new Separator(), lblNombre, lblRfc, 
                                    new Separator(), seccionPaquete, new Separator(), 
                                    seccionExtras, new Separator(), lblTotal, 
                                    new Separator(), instrucciones);

        return tarjeta;
    }

    private boolean tienePresupuesto(int clienteId) {
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT COUNT(*) FROM presupuestos WHERE cliente_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
        }
        return false;
    }

    private boolean cargarDatosPresupuesto(int clienteId) {
        try (Connection conn = Conexion.conectar()) {
            // Cargar presupuesto principal
            String sqlPresupuesto = """
                SELECT p.*, paq.nombre as paquete_nombre, paq.precio as paquete_precio
                FROM presupuestos p
                LEFT JOIN paquetes paq ON p.paquete_id = paq.id
                WHERE p.cliente_id = ?
                ORDER BY p.id DESC LIMIT 1
                """;
            
            debug("🔍 Ejecutando consulta SQL para cliente ID: " + clienteId);
            
            PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
            stmtPresupuesto.setInt(1, clienteId);
            ResultSet rsPresupuesto = stmtPresupuesto.executeQuery();
            
            if (rsPresupuesto.next()) {
                // Establecer datos del paquete
                int paqueteId = rsPresupuesto.getInt("paquete_id");
                String paqueteNombre = rsPresupuesto.getString("paquete_nombre");
                double paquetePrecio = rsPresupuesto.getDouble("paquete_precio");
                
                if (paqueteId > 0 && paqueteNombre != null) {
                    sesion.setPaquete(paqueteId, paqueteNombre, paquetePrecio);
                }
                
                // Cargar otros datos del presupuesto
                String horario = null;
                String plazos = null;
                String formaPago = null;
                
                try {
                    horario = rsPresupuesto.getString("horario");
                    debug("Horario cargado: " + horario);
                } catch (SQLException e) {
                    debug("Columna 'horario' no encontrada");
                    horario = "No especificado";
                }
                
                try {
                    plazos = rsPresupuesto.getString("plazos_pago");
                    debug("Plazos cargados: " + plazos);
                } catch (SQLException e) {
                    debug("Columna 'plazos_pago' no encontrada");
                    plazos = "No especificado";
                }
                
                try {
                    formaPago = rsPresupuesto.getString("forma_pago");
                    if (formaPago == null || formaPago.trim().isEmpty()) {
                        formaPago = rsPresupuesto.getString("metodo_pago");
                    }
                    debug("Forma de pago cargada: " + formaPago);
                } catch (SQLException e) {
                    debug("Columnas 'forma_pago' y 'metodo_pago' no encontradas");
                    formaPago = "No especificado";
                }
                
                sesion.setHorarioPresupuesto(horario);
                sesion.setPlazosPresupuesto(plazos);
                sesion.setFormaPagoPresupuesto(formaPago);
                
                // *** CARGAR EXTRAS MEJORADO ***
                List<Extra> extras = new ArrayList<>();
                try {
                    String sqlExtras = """
                        SELECT e.nombre, e.precio, pe.cantidad
                        FROM presupuesto_extras pe
                        JOIN extras e ON pe.extra_id = e.id
                        WHERE pe.presupuesto_id = ?
                        """;
                    
                    PreparedStatement stmtExtras = conn.prepareStatement(sqlExtras);
                    stmtExtras.setInt(1, rsPresupuesto.getInt("id"));
                    ResultSet rsExtras = stmtExtras.executeQuery();
                    
                    while (rsExtras.next()) {
                        Extra extra = new Extra(
                            rsExtras.getString("nombre"),
                            rsExtras.getDouble("precio"),
                            rsExtras.getInt("cantidad")
                        );
                        extras.add(extra);
                        debug("Extra cargado: " + extra.getNombre() + " x" + extra.getCantidad() + " = $" + extra.getSubtotal());
                    }
                } catch (SQLException extraError) {
                    debug("No se pudieron cargar los extras desde presupuesto_extras: " + extraError.getMessage());
                    
                    // Fallback: intentar parsear desde extras_detalle
                    try {
                        String extrasDetalle = rsPresupuesto.getString("extras_detalle");
                        if (extrasDetalle != null && !extrasDetalle.trim().isEmpty() && 
                            !extrasDetalle.equals("Sin extras")) {
                            // Aquí podrías parsear el texto de extras_detalle si tiene un formato específico
                            debug("Extras detalle disponible: " + extrasDetalle);
                        }
                    } catch (SQLException e2) {
                        debug("Tampoco se pudo obtener extras_detalle");
                    }
                }
                
                sesion.setExtrasSeleccionados(extras);
                
                debug("Presupuesto cargado correctamente:");
                debug("- Paquete: " + paqueteNombre + " ($" + paquetePrecio + ")");
                debug("- Extras: " + extras.size());
                debug("- Total: $" + sesion.getTotalGeneral());
                
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar presupuesto", "No se pudo cargar el presupuesto: " + e.getMessage());
        }
        return false;
    }

    private void cargarEventosPagados() {
        eventos.clear();
        reservasCompletas.clear();
        
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT r.id, r.cliente_id, r.paquete_id, r.fecha, r.horario, r.estado, r.metodo_pago, " +
                        "c.nombre AS cliente_nombre, c.rfc AS cliente_rfc, " +
                        "p.nombre AS paquete_nombre, p.precio AS paquete_precio " +
                        "FROM reservas r " +
                        "JOIN clientes c ON r.cliente_id = c.id " +
                        "LEFT JOIN paquetes p ON r.paquete_id = p.id " +
                        "WHERE r.estado = 'pagado'";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String horario = rs.getString("horario");
                String clienteNombre = rs.getString("cliente_nombre");
                
                String claveReserva = fecha.toString() + "_" + horario;
                
                Map<String, Object> infoReserva = new HashMap<>();
                infoReserva.put("id", rs.getInt("id"));
                infoReserva.put("cliente_id", rs.getInt("cliente_id"));
                infoReserva.put("cliente_nombre", clienteNombre);
                infoReserva.put("cliente_rfc", rs.getString("cliente_rfc"));
                infoReserva.put("paquete_id", rs.getInt("paquete_id"));
                infoReserva.put("paquete_nombre", rs.getString("paquete_nombre"));
                infoReserva.put("paquete_precio", rs.getDouble("paquete_precio"));
                infoReserva.put("fecha", fecha);
                infoReserva.put("horario", horario);
                infoReserva.put("estado", rs.getString("estado"));
                infoReserva.put("metodo_pago", rs.getString("metodo_pago"));
                
                reservasCompletas.put(claveReserva, infoReserva);

                EventoInfo info = eventos.getOrDefault(fecha, new EventoInfo());
                ReservaDetalle detalle = new ReservaDetalle(clienteNombre);

                if ("matutino".equalsIgnoreCase(horario)) {
                    info.manana = detalle;
                } else if ("vespertino".equalsIgnoreCase(horario)) {
                    info.tarde = detalle;
                }
                eventos.put(fecha, info);
            }
            debug("Eventos cargados: " + eventos.size());
        } catch (SQLException e) {
            mostrarError("Error al cargar eventos", "No se pudieron cargar las reservas: " + e.getMessage());
        }
    }

    private void construirCalendario() {
        labelMes.setText(obtenerNombreMes(mesActual.getMonthValue()) + " " + mesActual.getYear());
        gridCalendario.getChildren().clear();

        String[] dias = {"DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB"};
        for (int i = 0; i < dias.length; i++) {
            Label diaLabel = new Label(dias[i]);
            diaLabel.setStyle("-fx-font-weight: bold;");
            gridCalendario.add(diaLabel, i, 0);
        }

        LocalDate primerDiaMes = mesActual.withDayOfMonth(1);
        int diaSemana = primerDiaMes.getDayOfWeek().getValue() % 7;
        int fila = 1;
        int columna = diaSemana;
        int diasMes = mesActual.lengthOfMonth();

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mesActual.withDayOfMonth(dia);
            StackPane celda = crearCelda(fecha);
            gridCalendario.add(celda, columna, fila);
            columna++;
            if (columna > 6) {
                columna = 0;
                fila++;
            }
        }
    }

    private StackPane crearCelda(LocalDate fecha) {
        StackPane celda = new StackPane();
        celda.setMinSize(70, 70);
        Rectangle fondo = new Rectangle(70, 70, Color.WHITE);
        fondo.setStroke(Color.GRAY);

        EventoInfo info = eventos.getOrDefault(fecha, new EventoInfo());

        Polygon mananaTriangulo = new Polygon(0, 0, 70, 0, 0, 70);
        Polygon tardeTriangulo = new Polygon(70, 0, 70, 70, 0, 70);

        mananaTriangulo.setFill(info.manana != null ? Color.LIGHTGREEN : Color.WHITE);
        tardeTriangulo.setFill(info.tarde != null ? Color.LIGHTBLUE : Color.WHITE);

        mananaTriangulo.setStroke(Color.GRAY);
        tardeTriangulo.setStroke(Color.GRAY);

        // Eventos de clic para cada triángulo
        mananaTriangulo.setOnMouseClicked(e -> {
            e.consume();
            manejarClicTriangulo(fecha, "matutino");
        });

        tardeTriangulo.setOnMouseClicked(e -> {
            e.consume();
            manejarClicTriangulo(fecha, "vespertino");
        });

        Label diaLabel = new Label(String.valueOf(fecha.getDayOfMonth()));
        diaLabel.setStyle("-fx-font-weight: bold;");

        celda.getChildren().addAll(fondo, mananaTriangulo, tardeTriangulo, diaLabel);

        // Evento de clic para la celda completa (solo para fechas libres)
        celda.setOnMouseClicked(e -> {
            if (sesion.hayClienteSeleccionado()) {
                sesion.setFechaEvento(fecha);
                mostrarFechaSeleccionada(fecha, "Completo");
            } else {
                mostrarError("Cliente requerido", "Selecciona un cliente con presupuesto antes de elegir una fecha.");
            }
        });

        return celda;
    }

    private void manejarClicTriangulo(LocalDate fecha, String horario) {
        String claveReserva = fecha.toString() + "_" + horario;
        Map<String, Object> infoReserva = reservasCompletas.get(claveReserva);
        
        if (infoReserva != null) {
            // Hay una reserva, mostrar información en panel lateral
            mostrarInformacionReservaPanel(fecha, horario, infoReserva);
        } else {
            // No hay reserva, permitir seleccionar si hay cliente
            if (sesion.hayClienteSeleccionado()) {
                sesion.setFechaEvento(fecha);
                sesion.setHorarioEvento(horario);
                mostrarFechaSeleccionada(fecha, horario);
            } else {
                mostrarError("Cliente requerido", "Selecciona un cliente con presupuesto antes de elegir una fecha.");
            }
        }
    }

    // NUEVO MÉTODO: Mostrar fecha seleccionada en panel lateral
    private void mostrarFechaSeleccionada(LocalDate fecha, String horario) {
        if (panelDetalles != null && contenedorDetalles != null && lblTituloDetalle != null) {
            lblTituloDetalle.setText("📅 Fecha Seleccionada");
            contenedorDetalles.getChildren().clear();
            
            VBox tarjetaFecha = crearTarjetaFechaSeleccionada(fecha, horario);
            contenedorDetalles.getChildren().add(tarjetaFecha);
            
            panelDetalles.setVisible(true);
        }
    }

    // NUEVO MÉTODO: Crear tarjeta de fecha seleccionada
    private VBox crearTarjetaFechaSeleccionada(LocalDate fecha, String horario) {
        VBox tarjeta = new VBox(12);
        tarjeta.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                        "-fx-background-radius: 15px; " +
                        "-fx-padding: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                        "-fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 15px;");

        // Header
        Label headerFecha = new Label("✅ FECHA Y HORARIO SELECCIONADOS");
        headerFecha.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #27ae60;");

        // Información de la fecha
        Label lblFecha = new Label("📅 Fecha: " + fecha.toString());
        lblFecha.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label lblHorario = new Label("🕐 Horario: " + horario.toUpperCase());
        lblHorario.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        // Información del cliente
        VBox seccionCliente = new VBox(8);
        seccionCliente.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12px; -fx-padding: 15px;");

        Label tituloCliente = new Label("👤 CLIENTE");
        tituloCliente.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        Label lblCliente = new Label("Nombre: " + sesion.getClienteNombreCompleto());
        lblCliente.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        Label lblTotal = new Label("💰 Total: $" + String.format("%.2f", sesion.getTotalGeneral()));
        lblTotal.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        seccionCliente.getChildren().addAll(tituloCliente, lblCliente, lblTotal);

        // Instrucciones
        Label instrucciones = new Label("✨ ¡Perfecto! Ahora puedes hacer clic en 'Siguiente' para continuar con el contrato.");
        instrucciones.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-text-alignment: center;");
        instrucciones.setWrapText(true);

        tarjeta.getChildren().addAll(headerFecha, new Separator(), lblFecha, lblHorario, 
                                    new Separator(), seccionCliente, new Separator(), instrucciones);

        return tarjeta;
    }

    // NUEVO MÉTODO: Mostrar información de reserva en panel lateral
    private void mostrarInformacionReservaPanel(LocalDate fecha, String horario, Map<String, Object> infoReserva) {
        if (panelDetalles != null && contenedorDetalles != null && lblTituloDetalle != null) {
            lblTituloDetalle.setText("📋 Información de Reserva");
            contenedorDetalles.getChildren().clear();
            
            // Cargar extras de esta reserva específica
            List<Extra> extrasReserva = cargarExtrasDeReserva((Integer) infoReserva.get("id"));
            
            VBox tarjetaReserva = crearTarjetaReservaExistente(fecha, horario, infoReserva, extrasReserva);
            contenedorDetalles.getChildren().add(tarjetaReserva);
            
            panelDetalles.setVisible(true);
        }
    }

    // NUEVO MÉTODO: Cargar extras de una reserva específica
    private List<Extra> cargarExtrasDeReserva(int reservaId) {
        List<Extra> extras = new ArrayList<>();
        try (Connection conn = Conexion.conectar()) {
            String sql = """
                SELECT e.nombre, e.precio, re.cantidad
                FROM reserva_extras re
                JOIN extras e ON re.extra_id = e.id
                WHERE re.reserva_id = ?
                """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reservaId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Extra extra = new Extra(
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getInt("cantidad")
                );
                extras.add(extra);
                debug("Extra de reserva cargado: " + extra.getNombre() + " x" + extra.getCantidad());
            }
        } catch (SQLException e) {
            debug("No se pudieron cargar extras de la reserva " + reservaId + ": " + e.getMessage());
        }
        return extras;
    }

    // NUEVO MÉTODO: Crear tarjeta de reserva existente CON EXTRAS
    private VBox crearTarjetaReservaExistente(LocalDate fecha, String horario, Map<String, Object> infoReserva, List<Extra> extrasReserva) {
        VBox tarjeta = new VBox(12);
        tarjeta.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                        "-fx-background-radius: 15px; " +
                        "-fx-padding: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                        "-fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 15px;");

        // Header con estado
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label headerReserva = new Label("🔒 RESERVA OCUPADA");
        headerReserva.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
        
        Label estadoReserva = new Label(getStringValue(infoReserva, "estado").toUpperCase());
        estadoReserva.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                              "-fx-background-radius: 20px; -fx-padding: 8 15; " +
                              "-fx-font-weight: bold; -fx-font-size: 14px;");
        
        header.getChildren().addAll(headerReserva, estadoReserva);

        // Información del evento
        VBox seccionEvento = new VBox(8);
        seccionEvento.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloEvento = new Label("📅 INFORMACIÓN DEL EVENTO");
        tituloEvento.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #e67e22;");
        
        Label lblFecha = new Label("Fecha: " + fecha.toString());
        lblFecha.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        Label lblHorario = new Label("Horario: " + horario.toUpperCase());
        lblHorario.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        seccionEvento.getChildren().addAll(tituloEvento, lblFecha, lblHorario);

        // Información del cliente
        VBox seccionCliente = new VBox(8);
        seccionCliente.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloCliente = new Label("👤 CLIENTE");
        tituloCliente.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        
        Label lblCliente = new Label("Nombre: " + getStringValue(infoReserva, "cliente_nombre"));
        lblCliente.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        Label lblRfc = new Label("RFC: " + getStringValue(infoReserva, "cliente_rfc"));
        lblRfc.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        seccionCliente.getChildren().addAll(tituloCliente, lblCliente, lblRfc);

        // Información del paquete
        VBox seccionPaquete = new VBox(8);
        seccionPaquete.setStyle("-fx-background-color: #e8f5e8; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloPaquete = new Label("📦 PAQUETE");
        tituloPaquete.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #27ae60;");
        
        Label lblPaquete = new Label("Nombre: " + getStringValue(infoReserva, "paquete_nombre"));
        lblPaquete.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        Label lblPrecio = new Label("Precio: $" + String.format("%.2f", getDoubleValue(infoReserva, "paquete_precio")));
        lblPrecio.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        seccionPaquete.getChildren().addAll(tituloPaquete, lblPaquete, lblPrecio);

        // *** NUEVA SECCIÓN: EXTRAS DE LA RESERVA ***
        VBox seccionExtras = new VBox(8);
        seccionExtras.setStyle("-fx-background-color: #fdf2e9; -fx-background-radius: 12px; -fx-padding: 15px;");

        Label tituloExtras = new Label("✨ EXTRAS DE LA RESERVA");
        tituloExtras.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #d35400;");

        if (extrasReserva != null && !extrasReserva.isEmpty()) {
            double totalExtras = 0.0;
            for (Extra extra : extrasReserva) {
                VBox extraItem = new VBox(3);
                extraItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); " +
                                 "-fx-background-radius: 8px; -fx-padding: 10px; " +
                                 "-fx-border-color: #f0f0f0; -fx-border-width: 1px; -fx-border-radius: 8px;");

                Label lblExtraNombre = new Label("• " + extra.getNombre());
                lblExtraNombre.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

                HBox extrasInfo = new HBox(10);
                extrasInfo.setAlignment(Pos.CENTER_LEFT);

                Label lblCantidad = new Label("Cant: " + extra.getCantidad());
                lblCantidad.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

                Label lblPrecioExtra = new Label("$" + String.format("%.2f", extra.getPrecio()));
                lblPrecioExtra.setStyle("-fx-font-size: 12px; -fx-text-fill: #d35400; -fx-font-weight: bold;");

                Label lblSubtotal = new Label("= $" + String.format("%.2f", extra.getSubtotal()));
                lblSubtotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

                extrasInfo.getChildren().addAll(lblCantidad, lblPrecioExtra, lblSubtotal);
                extraItem.getChildren().addAll(lblExtraNombre, extrasInfo);
                seccionExtras.getChildren().add(extraItem);
                
                totalExtras += extra.getSubtotal();
            }

            // Total de extras
            Label lblTotalExtras = new Label("Total Extras: $" + String.format("%.2f", totalExtras));
            lblTotalExtras.setStyle("-fx-font-size: 14px; -fx-text-fill: #d35400; -fx-font-weight: bold; " +
                                  "-fx-background-color: rgba(211, 84, 0, 0.1); -fx-background-radius: 8px; " +
                                  "-fx-padding: 8px; -fx-alignment: center;");
            seccionExtras.getChildren().add(lblTotalExtras);

        } else {
            Label sinExtras = new Label("Sin extras en esta reserva");
            sinExtras.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
            seccionExtras.getChildren().add(sinExtras);
        }

        // Agregar título de extras al inicio
        seccionExtras.getChildren().add(0, tituloExtras);

        // Información de pago
        VBox seccionPago = new VBox(8);
        seccionPago.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloPago = new Label("💳 PAGO");
        tituloPago.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1976d2;");
        
        Label lblMetodoPago = new Label("Método: " + getStringValue(infoReserva, "metodo_pago"));
        lblMetodoPago.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label lblEstadoPago = new Label("Estado: " + getStringValue(infoReserva, "estado").toUpperCase());
        lblEstadoPago.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        seccionPago.getChildren().addAll(tituloPago, lblMetodoPago, lblEstadoPago);

        // Mensaje informativo
        Label mensaje = new Label("⚠️ Esta fecha y horario ya están ocupados. Selecciona otro horario o fecha.");
        mensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-text-alignment: center;");
        mensaje.setWrapText(true);

        tarjeta.getChildren().addAll(header, new Separator(), seccionEvento, seccionCliente, 
                                    seccionPaquete, new Separator(), seccionExtras, seccionPago, 
                                    new Separator(), mensaje);

        return tarjeta;
    }

    // NUEVO MÉTODO: Cerrar panel de detalles
    @FXML
    private void cerrarDetalles() {
        if (panelDetalles != null) {
            panelDetalles.setVisible(false);
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "N/A";
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private void mostrarError(String titulo, String mensaje) {
        // CAMBIO: Mostrar errores en panel lateral en lugar de popup
        if (panelDetalles != null && contenedorDetalles != null && lblTituloDetalle != null) {
            lblTituloDetalle.setText("⚠️ " + titulo);
            contenedorDetalles.getChildren().clear();
            
            VBox tarjetaError = crearTarjetaError(titulo, mensaje);
            contenedorDetalles.getChildren().add(tarjetaError);
            
            panelDetalles.setVisible(true);
        }
    }

    // NUEVO MÉTODO: Crear tarjeta de error
    private VBox crearTarjetaError(String titulo, String mensaje) {
        VBox tarjeta = new VBox(12);
        tarjeta.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                        "-fx-background-radius: 15px; " +
                        "-fx-padding: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                        "-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 15px;");

        Label headerError = new Label("⚠️ " + titulo.toUpperCase());
        headerError.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");

        Label lblMensaje = new Label(mensaje);
        lblMensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        lblMensaje.setWrapText(true);

        Label instruccion = new Label("💡 Revisa los datos e intenta nuevamente.");
        instruccion.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-text-alignment: center;");

        tarjeta.getChildren().addAll(headerError, new Separator(), lblMensaje, new Separator(), instruccion);

        return tarjeta;
    }

    private String obtenerNombreMes(int mes) {
        switch (mes) {
            case 1: return "ENERO";
            case 2: return "FEBRERO";
            case 3: return "MARZO";
            case 4: return "ABRIL";
            case 5: return "MAYO";
            case 6: return "JUNIO";
            case 7: return "JULIO";
            case 8: return "AGOSTO";
            case 9: return "SEPTIEMBRE";
            case 10: return "OCTUBRE";
            case 11: return "NOVIEMBRE";
            case 12: return "DICIEMBRE";
            default: return "";
        }
    }

    @FXML
    private void mesAnterior() {
        mesActual = mesActual.minusMonths(1);
        cargarEventosPagados();
        construirCalendario();
    }

    @FXML
    private void mesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        cargarEventosPagados();
        construirCalendario();
    }

    @FXML
    private void accionRegresar() throws IOException {
        // Limpiar sesión al regresar
        sesion.reset();
        App.setRoot("PanelPrincipal");
    }

    @FXML
    private void accionSiguiente() throws IOException {
        if (sesion.getFechaEvento() != null && sesion.hayClienteSeleccionado() && sesion.hayPaqueteSeleccionado()) {
            // Mostrar resumen antes de continuar
            debug("=== DATOS CARGADOS EN SESIÓN ===");
            debug("Cliente: " + sesion.getClienteNombreCompleto());
            debug("RFC: " + sesion.getClienteRfc());
            debug("Fecha evento: " + sesion.getFechaEvento());
            debug("Horario evento: " + sesion.getHorarioEvento());
            debug("Paquete: " + sesion.getPaqueteNombre());
            debug("Precio paquete: $" + sesion.getPaquetePrecio());
            debug("Total extras: $" + sesion.getTotalExtras());
            debug("Total general: $" + sesion.getTotalGeneral());
            debug("Horario presupuesto: " + sesion.getHorarioPresupuesto());
            debug("Plazos: " + sesion.getPlazosPresupuesto());
            debug("Forma de pago: " + sesion.getFormaPagoPresupuesto());
            debug("===============================");
            
            App.setRoot("VistaPreviaContrato");
        } else {
            StringBuilder mensaje = new StringBuilder();
            if (!sesion.hayClienteSeleccionado()) {
                mensaje.append("• Selecciona un cliente\n");
            }
            if (sesion.getFechaEvento() == null) {
                mensaje.append("• Selecciona una fecha\n");
            }
            if (!sesion.hayPaqueteSeleccionado()) {
                mensaje.append("• El cliente debe tener un paquete en su presupuesto\n");
            }
            
            mostrarError("Faltan datos requeridos", mensaje.toString());
        }
    }
}