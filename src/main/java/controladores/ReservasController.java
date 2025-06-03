package controladores;

import database.Conexion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.geometry.Pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class ReservasController {

    @FXML private Label labelMes;
    @FXML private GridPane gridCalendario;
    @FXML private VBox panelDetalles;
    @FXML private Label lblFechaSeleccionada;
    @FXML private VBox contenedorReservas;
    @FXML private ScrollPane scrollDetalles;
    
    private YearMonth mesActual;
    private List<ReservaCalendario> reservasDelMes = new ArrayList<>();
    
    // Clase interna para manejar las reservas del calendario
    private static class ReservaCalendario {
        LocalDate fecha;
        String cliente;
        String festejado;
        String horario;
        String estado;
        String telefono;
        String email;
        String paquete;
        String metodoPago;
        String plazos;
        String extras;
        double total;
        String direccion;
        int contratoId;
        
        // Constructor completo (14 parámetros)
        ReservaCalendario(LocalDate fecha, String cliente, String festejado, String horario, 
                         String estado, String telefono, String email, String paquete, 
                         String metodoPago, String plazos, String extras, double total, 
                         String direccion, int contratoId) {
            this.fecha = fecha;
            this.cliente = cliente;
            this.festejado = festejado;
            this.horario = horario;
            this.estado = estado;
            this.telefono = telefono;
            this.email = email;
            this.paquete = paquete;
            this.metodoPago = metodoPago;
            this.plazos = plazos;
            this.extras = extras;
            this.total = total;
            this.direccion = direccion;
            this.contratoId = contratoId;
        }
        
        // Constructor básico (5 parámetros)
        ReservaCalendario(LocalDate fecha, String cliente, String festejado, String horario, String estado) {
            this.fecha = fecha;
            this.cliente = cliente;
            this.festejado = festejado;
            this.horario = horario;
            this.estado = estado;
            // Valores por defecto para campos opcionales
            this.telefono = "No registrado";
            this.email = "No registrado";
            this.paquete = "Paquete estándar";
            this.metodoPago = "No especificado";
            this.plazos = "No especificado";
            this.extras = "Sin extras";
            this.total = 0.0;
            this.direccion = "No especificada";
            this.contratoId = 0;
        }
    }

    @FXML
    public void initialize() {
        mesActual = YearMonth.now();
        limpiarReservasVencidas();
        actualizarCalendario();
    }

    private void limpiarReservasVencidas() {
        try (Connection conn = Conexion.conectar()) {
            String sqlVerificar = "SELECT COUNT(*) as total FROM contratos WHERE fecha_evento < DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
            PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar);
            ResultSet rsVerificar = stmtVerificar.executeQuery();
            
            int contratosVencidos = 0;
            if (rsVerificar.next()) {
                contratosVencidos = rsVerificar.getInt("total");
            }
            
            if (contratosVencidos > 0) {
                // Solo cambiar estado en lugar de eliminar
                String sqlActualizar = "UPDATE contratos SET estado = 'finalizado' WHERE fecha_evento < DATE_SUB(CURDATE(), INTERVAL 1 MONTH) AND estado != 'finalizado'";
                PreparedStatement stmtActualizar = conn.prepareStatement(sqlActualizar);
                stmtActualizar.executeUpdate();
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar contratos: " + e.getMessage());
        }
    }

    @FXML
    private void mesAnterior() {
        mesActual = mesActual.minusMonths(1);
        actualizarCalendario();
    }

    @FXML
    private void mesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        actualizarCalendario();
    }

    private void actualizarCalendario() {
        // Actualizar título
        String nombreMes = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        labelMes.setText(nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + mesActual.getYear());
        
        cargarReservasDelMes();
        
        // Limpiar calendario
        gridCalendario.getChildren().clear();
        
        // Encabezados días de la semana
        String[] diasSemana = {"DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label labelDia = new Label(diasSemana[i]);
            labelDia.getStyleClass().add("encabezado-dia");
            labelDia.setAlignment(Pos.CENTER);
            labelDia.setPrefWidth(100);
            labelDia.setPrefHeight(30);
            gridCalendario.add(labelDia, i, 0);
        }
        
        // Configurar días del mes
        LocalDate primerDia = mesActual.atDay(1);
        int diasEnMes = mesActual.lengthOfMonth();
        int diaSemanaInicio = primerDia.getDayOfWeek().getValue() % 7;
        
        int fila = 1;
        int columna = diaSemanaInicio;
        
        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fechaDia = mesActual.atDay(dia);
            
            // Crear contenedor
            StackPane contenedorDia = new StackPane();
            contenedorDia.setPrefWidth(100);
            contenedorDia.setPrefHeight(80);
            contenedorDia.getStyleClass().add("celda-calendario");

            if (fechaDia.equals(LocalDate.now())) {
                contenedorDia.getStyleClass().add("celda-hoy");
            }
            
            // Buscar reservas del día
            List<ReservaCalendario> reservasDelDia = new ArrayList<>();
            for (ReservaCalendario reserva : reservasDelMes) {
                if (reserva.fecha.equals(fechaDia)) {
                    reservasDelDia.add(reserva);
                }
            }
            
            // Determinar ocupación
            boolean hayMatutino = false;
            boolean hayVespertino = false;

            for (ReservaCalendario reserva : reservasDelDia) {
                if (esHorarioMatutino(reserva.horario)) {
                    hayMatutino = true;
                } else {
                    hayVespertino = true;
                }
            }
            
            // CREAR TRIÁNGULOS CON CLICK INDEPENDIENTE 
            
            // TRIÁNGULO INFERIOR IZQUIERDO = MATUTINO (clickeable)
            Polygon trianguloMatutino = new Polygon();
            trianguloMatutino.getPoints().addAll(new Double[]{
                0.0, 0.0,    // Esquina superior izquierda
                0.0, 80.0,   // Esquina inferior izquierda
                100.0, 80.0  // Esquina inferior derecha
            });

            // TRIÁNGULO SUPERIOR DERECHO = VESPERTINO (clickeable)
            Polygon trianguloVespertino = new Polygon();
            trianguloVespertino.getPoints().addAll(new Double[]{
                0.0, 0.0,    // Esquina superior izquierda
                100.0, 0.0,  // Esquina superior derecha
                100.0, 80.0  // Esquina inferior derecha
            });

            // APLICAR COLORES
            if (hayMatutino && hayVespertino) {
                // AMBOS OCUPADOS = AMBOS ROJOS
                trianguloMatutino.setFill(Color.rgb(255, 0, 0, 0.6));
                trianguloVespertino.setFill(Color.rgb(255, 0, 0, 0.6));
            } else {
                if (hayMatutino) {
                    // SOLO MATUTINO = TRIÁNGULO INFERIOR IZQUIERDO VERDE
                    trianguloMatutino.setFill(Color.rgb(0, 255, 0, 0.5));
                    trianguloVespertino.setFill(Color.TRANSPARENT);
                } else if (hayVespertino) {
                    // SOLO VESPERTINO = TRIÁNGULO SUPERIOR DERECHO VERDE
                    trianguloMatutino.setFill(Color.TRANSPARENT);
                    trianguloVespertino.setFill(Color.rgb(0, 255, 0, 0.5));
                } else {
                    // NINGUNO OCUPADO = AMBOS TRANSPARENTES
                    trianguloMatutino.setFill(Color.TRANSPARENT);
                    trianguloVespertino.setFill(Color.TRANSPARENT);
                }
            }

            // SEPARAR RESERVAS POR HORARIO 
            List<ReservaCalendario> matutino = new ArrayList<>();
            List<ReservaCalendario> vespertino = new ArrayList<>();

            for (ReservaCalendario reserva : reservasDelDia) {
                if (esHorarioMatutino(reserva.horario)) {
                    matutino.add(reserva);
                } else {
                    vespertino.add(reserva);
                }
            }

            // CLICK INDEPENDIENTE PARA CADA TRIÁNGULO

            // CLICK EN TRIÁNGULO MATUTINO (inferior izquierdo)
            trianguloMatutino.setOnMouseClicked(e -> {
                if (!matutino.isEmpty()) {
                    mostrarDetallesDia(fechaDia, matutino, "MATUTINO 🌅");
                } else {
                    mostrarMensajeVacio(fechaDia, "MATUTINO");
                }
                e.consume(); // Evitar que se propague el evento
            });

            // CLICK EN TRIÁNGULO VESPERTINO (superior derecho)
            trianguloVespertino.setOnMouseClicked(e -> {
                if (!vespertino.isEmpty()) {
                    mostrarDetallesDia(fechaDia, vespertino, "VESPERTINO 🌇");
                } else {
                    mostrarMensajeVacio(fechaDia, "VESPERTINO");
                }
                e.consume(); // Evitar que se propague el evento
            });

            // HACER LOS TRIÁNGULOS INTERACTIVOS VISUALMENTE
            trianguloMatutino.setOnMouseEntered(e -> {
                trianguloMatutino.setOpacity(0.8);
                trianguloMatutino.setCursor(Cursor.HAND);
            });

            trianguloMatutino.setOnMouseExited(e -> {
                trianguloMatutino.setOpacity(1.0);
            });

            trianguloVespertino.setOnMouseEntered(e -> {
                trianguloVespertino.setOpacity(0.8);
                trianguloVespertino.setCursor(Cursor.HAND);
            });

            trianguloVespertino.setOnMouseExited(e -> {
                trianguloVespertino.setOpacity(1.0);
            });

            // LÍNEA DIAGONAL 
            Line lineaDiagonal = new Line();
            lineaDiagonal.setStartX(0);
            lineaDiagonal.setStartY(0);
            lineaDiagonal.setEndX(100);
            lineaDiagonal.setEndY(80);
            lineaDiagonal.setStroke(Color.GRAY);
            lineaDiagonal.setStrokeWidth(1);
            lineaDiagonal.setOpacity(0.6);
            lineaDiagonal.setMouseTransparent(true); // No interfiere con los clicks

            // CONTENEDORES PARA INDICADORES 
            VBox contenedorMatutino = new VBox();
            contenedorMatutino.setAlignment(Pos.BOTTOM_LEFT);
            contenedorMatutino.setPrefWidth(100);
            contenedorMatutino.setPrefHeight(80);
            contenedorMatutino.setStyle("-fx-background-color: transparent;");
            contenedorMatutino.setMouseTransparent(true); // No interfiere con los clicks
            StackPane.setAlignment(contenedorMatutino, Pos.BOTTOM_LEFT);

            VBox contenedorVespertino = new VBox();
            contenedorVespertino.setAlignment(Pos.TOP_RIGHT);
            contenedorVespertino.setPrefWidth(100);
            contenedorVespertino.setPrefHeight(80);
            contenedorVespertino.setStyle("-fx-background-color: transparent;");
            contenedorVespertino.setMouseTransparent(true); // No interfiere con los clicks
            StackPane.setAlignment(contenedorVespertino, Pos.TOP_RIGHT);

            // Indicadores matutino
            if (!matutino.isEmpty()) {
                HBox indicadoresMatutino = new HBox(2);
                indicadoresMatutino.setAlignment(Pos.BOTTOM_LEFT);
                indicadoresMatutino.setStyle("-fx-padding: 0 0 5 5;");
                indicadoresMatutino.setMouseTransparent(true);
                
                for (ReservaCalendario reserva : matutino) {
                    Label indicador = new Label("●");
                    indicador.setStyle("-fx-font-size: 10px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    indicador.setMouseTransparent(true);
                    indicadoresMatutino.getChildren().add(indicador);
                }
                contenedorMatutino.getChildren().add(indicadoresMatutino);
            }

            // Indicadores vespertino
            if (!vespertino.isEmpty()) {
                HBox indicadoresVespertino = new HBox(2);
                indicadoresVespertino.setAlignment(Pos.TOP_RIGHT);
                indicadoresVespertino.setStyle("-fx-padding: 5 5 0 0;");
                indicadoresVespertino.setMouseTransparent(true);
                
                for (ReservaCalendario reserva : vespertino) {
                    Label indicador = new Label("●");
                    indicador.setStyle("-fx-font-size: 10px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    indicador.setMouseTransparent(true);
                    indicadoresVespertino.getChildren().add(indicador);
                }
                contenedorVespertino.getChildren().add(indicadoresVespertino);
            }

            // NÚMERO DEL DÍA
            Label numeroDia = new Label(String.valueOf(dia));
            numeroDia.getStyleClass().add("numero-dia");
            numeroDia.setMouseTransparent(true); // No interfiere con los clicks

            if (!reservasDelDia.isEmpty()) {
                numeroDia.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                  "-fx-background-radius: 15px; " +
                                  "-fx-padding: 6 10 6 10; " +
                                  "-fx-font-weight: bold; " +
                                  "-fx-text-fill: #2c3e50; " +
                                  "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 3, 0, 0, 1);");
            }

            if (fechaDia.equals(LocalDate.now())) {
                numeroDia.getStyleClass().add("dia-hoy");
            }

            StackPane.setAlignment(numeroDia, Pos.CENTER);

            // ENSAMBLAR CELDA
            contenedorDia.getChildren().addAll(
                trianguloMatutino, 
                trianguloVespertino, 
                lineaDiagonal, 
                contenedorMatutino, 
                contenedorVespertino, 
                numeroDia
            );

            gridCalendario.add(contenedorDia, columna, fila);

            columna++;
            if (columna > 6) {
                columna = 0;
                fila++;
            }
        }
    }

    private void cargarReservasDelMes() {
        reservasDelMes.clear();
        
        try (Connection conn = Conexion.conectar()) {
            String sqlSimple = """
                SELECT 
                    con.id as contrato_id,
                    con.fecha_evento,
                    con.horario,
                    con.nombre_festejado,
                    con.total,
                    con.metodo_pago,
                    con.estado,
                    con.cliente_id,
                    con.paquete_id
                FROM contratos con
                WHERE YEAR(con.fecha_evento) = ? 
                AND MONTH(con.fecha_evento) = ?
                ORDER BY con.fecha_evento ASC
                """;
            
            PreparedStatement stmtSimple = conn.prepareStatement(sqlSimple);
            stmtSimple.setInt(1, mesActual.getYear());
            stmtSimple.setInt(2, mesActual.getMonthValue());
            ResultSet rsSimple = stmtSimple.executeQuery();
            
            while (rsSimple.next()) {
                int contratoId = rsSimple.getInt("contrato_id");
                LocalDate fecha = rsSimple.getDate("fecha_evento").toLocalDate();
                String horario = rsSimple.getString("horario");
                String festejado = rsSimple.getString("nombre_festejado");
                double total = rsSimple.getDouble("total");
                String metodoPago = rsSimple.getString("metodo_pago");
                String estado = rsSimple.getString("estado");
                int clienteId = rsSimple.getInt("cliente_id");
                int paqueteId = rsSimple.getInt("paquete_id");
                
                // BUSCAR DATOS DEL CLIENTE POR SEPARADO
                String nombreCliente = "Cliente desconocido";
                String telefonoCliente = "No registrado";
                String emailCliente = "No registrado";
                
                String sqlCliente = "SELECT nombre, apellido_paterno, apellido_materno, telefono, correo FROM clientes WHERE id = ?";
                PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente);
                stmtCliente.setInt(1, clienteId);
                ResultSet rsCliente = stmtCliente.executeQuery();
                
                if (rsCliente.next()) {
                    String nombre = rsCliente.getString("nombre");
                    String apellidoP = rsCliente.getString("apellido_paterno");
                    String apellidoM = rsCliente.getString("apellido_materno");
                    String telefono = rsCliente.getString("telefono");
                    String email = rsCliente.getString("correo");
                    
                    StringBuilder nombreCompleto = new StringBuilder();
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        nombreCompleto.append(nombre.trim());
                    }
                    if (apellidoP != null && !apellidoP.trim().isEmpty()) {
                        if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                        nombreCompleto.append(apellidoP.trim());
                    }
                    if (apellidoM != null && !apellidoM.trim().isEmpty()) {
                        if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                        nombreCompleto.append(apellidoM.trim());
                    }
                    
                    nombreCliente = nombreCompleto.toString().isEmpty() ? "Cliente sin nombre" : nombreCompleto.toString();
                    telefonoCliente = (telefono != null && !telefono.trim().isEmpty()) ? telefono.trim() : "No registrado";
                    emailCliente = (email != null && !email.trim().isEmpty()) ? email.trim() : "No registrado";
                }
                
                // BUSCAR DATOS DEL PAQUETE POR SEPARADO
                String nombrePaquete = "Paquete estándar";
                String descripcionPaquete = "";
                
                if (paqueteId > 0) {
                    String sqlPaquete = "SELECT nombre, descripcion FROM paquetes WHERE id = ?";
                    PreparedStatement stmtPaquete = conn.prepareStatement(sqlPaquete);
                    stmtPaquete.setInt(1, paqueteId);
                    ResultSet rsPaquete = stmtPaquete.executeQuery();
                    
                    if (rsPaquete.next()) {
                        String nomPaq = rsPaquete.getString("nombre");
                        String descPaq = rsPaquete.getString("descripcion");
                        
                        nombrePaquete = (nomPaq != null && !nomPaq.trim().isEmpty()) ? nomPaq.trim() : "Paquete estándar";
                        descripcionPaquete = (descPaq != null && !descPaq.trim().isEmpty()) ? descPaq.trim() : "";
                    }
                }
                
                // CONSTRUIR PAQUETE COMPLETO
                String paqueteCompleto = nombrePaquete;
                if (!descripcionPaquete.isEmpty()) {
                    paqueteCompleto += " - " + descripcionPaquete;
                }
                
                // BUSCAR EXTRAS Y PLAZOS
                String extras = obtenerExtrasDeContrato(conn, clienteId);
                String plazos = obtenerPlazosDeContrato(conn, clienteId);
                
                // DETERMINAR ESTADO
                String estadoEvento;
                if (fecha.equals(LocalDate.now())) {
                    estadoEvento = "HOY";
                } else if (fecha.isAfter(LocalDate.now())) {
                    estadoEvento = "Próximo";
                } else {
                    estadoEvento = "Realizado";
                }
                
                // LIMPIAR DATOS ANTES DE CREAR LA RESERVA
                String horarioFinal = (horario != null && !horario.trim().isEmpty()) ? horario.trim() : "matutino";
                String festejadoFinal = (festejado != null && !festejado.trim().isEmpty()) ? festejado.trim() : "Evento";
                String metodoPagoFinal = (metodoPago != null && !metodoPago.trim().isEmpty()) ? metodoPago.trim() : "No especificado";
                
                // CREAR RESERVA CON DATOS COMPLETOS
                ReservaCalendario reserva = new ReservaCalendario(
                    fecha,
                    nombreCliente,
                    festejadoFinal,
                    horarioFinal,
                    estadoEvento,
                    telefonoCliente,
                    emailCliente,
                    paqueteCompleto,
                    metodoPagoFinal,
                    plazos,
                    extras,
                    total,
                    "Dirección por definir",
                    contratoId
                );
                
                reservasDelMes.add(reserva);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar eventos: " + e.getMessage());
        }
    }

    // MÉTODO LIMPIO PARA OBTENER EXTRAS
    private String obtenerExtrasDeContrato(Connection conn, int clienteId) {
        try {
            // Buscar en la tabla presupuestos
            String sqlPresupuesto = """
                SELECT extras_detalle 
                FROM presupuestos 
                WHERE cliente_id = ? 
                ORDER BY fecha_creacion DESC 
                LIMIT 1
                """;
            
            PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
            stmtPresupuesto.setInt(1, clienteId);
            ResultSet rsPresupuesto = stmtPresupuesto.executeQuery();
            
            if (rsPresupuesto.next()) {
                String extrasDetalle = rsPresupuesto.getString("extras_detalle");
                
                if (extrasDetalle != null && !extrasDetalle.trim().isEmpty() && 
                    !extrasDetalle.equals("Sin extras") && !extrasDetalle.equals("NULL")) {
                    return procesarExtras(extrasDetalle);
                }
            }
            
        } catch (SQLException e) {
            // Error silencioso
        }
        
        return "Sin extras";
    }

    // MÉTODO LIMPIO PARA OBTENER PLAZOS
    private String obtenerPlazosDeContrato(Connection conn, int clienteId) {
        try {
            String sqlPresupuesto = """
                SELECT plazos_pago 
                FROM presupuestos 
                WHERE cliente_id = ? 
                ORDER BY fecha_creacion DESC 
                LIMIT 1
                """;
            
            PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
            stmtPresupuesto.setInt(1, clienteId);
            ResultSet rsPresupuesto = stmtPresupuesto.executeQuery();
            
            if (rsPresupuesto.next()) {
                String plazos = rsPresupuesto.getString("plazos_pago");
                
                if (plazos != null && !plazos.trim().isEmpty()) {
                    return plazos.trim();
                }
            }
            
        } catch (SQLException e) {
            // Error silencioso - la columna plazos_pago no existe
        }
        
        return "Pago único";
    }

    // MÉTODO PARA PROCESAR Y FORMATEAR EXTRAS
    private String procesarExtras(String extras) {
        if (extras == null || extras.trim().isEmpty() || 
            extras.equals("Sin extras especificados") || 
            extras.equals("Reserva sin extras especificados") ||
            extras.equals("Sin extras")) {
            return "Sin extras seleccionados";
        }
        
        try {
            // Si el string contiene el formato "Nombre xCantidad ($Precio)"
            if (extras.contains(" x") && extras.contains("($")) {
                // Formato: "Arco de globos x3 ($1500.00); Horas extra x3 ($2400.00)"
                String[] partesExtras = extras.split(";");
                StringBuilder extrasFormateados = new StringBuilder();
                
                for (String parte : partesExtras) {
                    parte = parte.trim();
                    if (!parte.isEmpty()) {
                        if (extrasFormateados.length() > 0) {
                            extrasFormateados.append("\n");
                        }
                        extrasFormateados.append("• ").append(parte);
                    }
                }
                
                return extrasFormateados.toString();
            } else {
                // Si es texto simple, solo agregamos viñetas
                return "• " + extras;
            }
        } catch (Exception e) {
            return extras; // Devolver original si hay error
        }
    }

    private boolean esHorarioMatutino(String horario) {
        if (horario == null || horario.trim().isEmpty()) {
            return true;
        }
        
        String horarioLower = horario.toLowerCase().trim();
        
        if (horarioLower.contains("matutino")) {
            return true;
        }
        
        if (horarioLower.contains("vespertino")) {
            return false;
        }
        
        if (horarioLower.contains("am") || horarioLower.contains("a.m")) {
            return true;
        }
        
        if (horarioLower.contains("pm") || horarioLower.contains("p.m")) {
            return false;
        }
        
        try {
            String[] partes = horario.split(":");
            if (partes.length >= 1) {
                int hora = Integer.parseInt(partes[0].trim());
                return hora < 12;
            }
        } catch (NumberFormatException e) {
            // Si no se puede parsear, asumir matutino
        }
        
        return true;
    }

    // Método sobrecargado para mostrar detalles de un horario específico
    private void mostrarDetallesDia(LocalDate fecha, List<ReservaCalendario> reservas, String tipoHorario) {
        // Mostrar el panel lateral
        if (panelDetalles != null) {
            panelDetalles.setVisible(true);
        }
        
        // Actualizar título con el tipo de horario
        if (lblFechaSeleccionada != null) {
            lblFechaSeleccionada.setText("📅 " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + tipoHorario);
        }
        
        // Limpiar contenido anterior
        if (contenedorReservas != null) {
            contenedorReservas.getChildren().clear();
        
            if (reservas.isEmpty()) {
                Label sinReservas = new Label("📭 No hay reservas para " + tipoHorario.toLowerCase());
                sinReservas.getStyleClass().addAll("texto-bienvenida");
                sinReservas.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                contenedorReservas.getChildren().add(sinReservas);
                return;
            }
            
            // Mostrar todas las reservas del horario específico
            for (int i = 0; i < reservas.size(); i++) {
                VBox tarjetaReserva = crearTarjetaReserva(reservas.get(i), i + 1);
                contenedorReservas.getChildren().add(tarjetaReserva);
                
                // Separador entre reservas si hay más de una
                if (i < reservas.size() - 1) {
                    Separator separador = new Separator();
                    separador.setStyle("-fx-padding: 5 0;");
                    contenedorReservas.getChildren().add(separador);
                }
            }
        }
    }

    // Método para mostrar mensaje cuando no hay reservas en un horario
    private void mostrarMensajeVacio(LocalDate fecha, String tipoHorario) {
        if (panelDetalles != null) {
            panelDetalles.setVisible(true);
        }
        
        if (lblFechaSeleccionada != null) {
            lblFechaSeleccionada.setText("📅 " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + tipoHorario);
        }
        
        if (contenedorReservas != null) {
            contenedorReservas.getChildren().clear();
            
            Label mensajeVacio = new Label("📭 No hay reservas para el horario " + tipoHorario.toLowerCase() + " en este día");
            mensajeVacio.getStyleClass().addAll("texto-bienvenida");
            mensajeVacio.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-text-alignment: center;");
            mensajeVacio.setWrapText(true);
            
            // Agregar sugerencia
            Label sugerencia = new Label("💡 Haz clic en el otro triángulo para ver el horario " + 
                                       (tipoHorario.equals("MATUTINO") ? "vespertino" : "matutino"));
            sugerencia.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px; -fx-text-alignment: center;");
            sugerencia.setWrapText(true);
            
            VBox contenedorMensaje = new VBox(10);
            contenedorMensaje.setAlignment(Pos.CENTER);
            contenedorMensaje.getChildren().addAll(mensajeVacio, sugerencia);
            
            contenedorReservas.getChildren().add(contenedorMensaje);
        }
    }

    private VBox crearTarjetaReserva(ReservaCalendario reserva, int numero) {
        VBox tarjetaPrincipal = new VBox(12);
        tarjetaPrincipal.getStyleClass().add("tarjeta-formulario");
        tarjetaPrincipal.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                 "-fx-background-radius: 15px; " +
                                 "-fx-padding: 20px; " +
                                 "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                                 "-fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 15px;");
        
        // HEADER CON NÚMERO Y ESTADO
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label numReserva = new Label("#" + numero);
        numReserva.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-background-radius: 20px; -fx-padding: 8 15; " +
                           "-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label estadoReserva = new Label(reserva.estado);
        String colorEstado = switch (reserva.estado) {
            case "HOY" -> "#e74c3c";
            case "Próximo" -> "#27ae60";
            default -> "#95a5a6";
        };
        estadoReserva.setStyle("-fx-background-color: " + colorEstado + "; -fx-text-fill: white; " +
                              "-fx-background-radius: 20px; -fx-padding: 8 15; " +
                              "-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label totalReserva = new Label(String.format("$%.2f MXN", reserva.total));
        totalReserva.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                             "-fx-background-radius: 20px; -fx-padding: 8 15; " +
                             "-fx-font-weight: bold; -fx-font-size: 14px;");
        
        header.getChildren().addAll(numReserva, estadoReserva, totalReserva);
        
        // === INFORMACIÓN DEL CLIENTE ===
        VBox seccionCliente = new VBox(8);
        seccionCliente.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloCliente = new Label("👤 INFORMACIÓN DEL CLIENTE");
        tituloCliente.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        
        Label lblCliente = new Label("Nombre: " + reserva.cliente);
        lblCliente.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        Label lblTelefono = new Label("📞 Teléfono: " + (reserva.telefono != null ? reserva.telefono : "No registrado"));
        lblTelefono.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label lblEmail = new Label("📧 Email: " + (reserva.email != null ? reserva.email : "No registrado"));
        lblEmail.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        seccionCliente.getChildren().addAll(tituloCliente, lblCliente, lblTelefono, lblEmail);
        
        // === INFORMACIÓN DEL EVENTO ===
        VBox seccionEvento = new VBox(8);
        seccionEvento.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloEvento = new Label("🎉 INFORMACIÓN DEL EVENTO");
        tituloEvento.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #e67e22;");
        
        Label lblFestejado = new Label("🎂 Festejado: " + reserva.festejado);
        lblFestejado.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        Label lblHorario = new Label("🕐 Horario: " + reserva.horario);
        lblHorario.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label lblDireccion = new Label("📍 Dirección: " + reserva.direccion);
        lblDireccion.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        lblDireccion.setWrapText(true);
        
        seccionEvento.getChildren().addAll(tituloEvento, lblFestejado, lblHorario, lblDireccion);
        
        // === INFORMACIÓN DEL PAQUETE ===
        VBox seccionPaquete = new VBox(8);
        seccionPaquete.setStyle("-fx-background-color: #e8f5e8; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloPaquete = new Label("📦 DETALLES DEL PAQUETE");
        tituloPaquete.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #27ae60;");
        
        Label lblPaquete = new Label("Paquete: " + reserva.paquete);
        lblPaquete.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        lblPaquete.setWrapText(true);
        
        Label lblExtras;
        if (reserva.extras != null && !reserva.extras.equals("Sin extras especificados") && 
            !reserva.extras.equals("Sin extras") && !reserva.extras.equals("Sin extras seleccionados")) {
            lblExtras = new Label("✨ Extras:\n" + reserva.extras);
            lblExtras.setStyle("-fx-font-size: 13px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
        } else {
            lblExtras = new Label("✨ Extras: " + reserva.extras);
            lblExtras.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
        }
        lblExtras.setWrapText(true);
        
        seccionPaquete.getChildren().addAll(tituloPaquete, lblPaquete, lblExtras);
        
        // === INFORMACIÓN DE PAGO ===
        VBox seccionPago = new VBox(8);
        seccionPago.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        Label tituloPago = new Label("💳 INFORMACIÓN DE PAGO");
        tituloPago.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1976d2;");
        
        Label lblMetodoPago = new Label("Método: " + (reserva.metodoPago != null ? reserva.metodoPago : "No especificado"));
        lblMetodoPago.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label lblPlazos = new Label("Plazos: " + (reserva.plazos != null ? reserva.plazos : "No especificado"));
        lblPlazos.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        lblPlazos.setWrapText(true);
        
        Label lblTotalFinal = new Label(String.format("💰 TOTAL: $%.2f MXN", reserva.total));
        lblTotalFinal.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        seccionPago.getChildren().addAll(tituloPago, lblMetodoPago, lblPlazos, lblTotalFinal);
        
        // === BOTÓN DE CONTRATO (OPCIONAL) ===
        if (reserva.contratoId > 0) {
            Button btnVerContrato = new Button("📄 Ver Contrato #" + reserva.contratoId);
            btnVerContrato.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                   "-fx-background-radius: 25px; -fx-padding: 10 20; " +
                                   "-fx-font-weight: bold; -fx-font-size: 13px; " +
                                   "-fx-cursor: hand;");
            btnVerContrato.setOnAction(e -> {
                // Aquí puedes agregar la lógica para abrir el contrato
                mostrarAlerta("Información", "Función para ver contrato #" + reserva.contratoId + " próximamente");
            });
            
            HBox contenedorBoton = new HBox();
            contenedorBoton.setAlignment(Pos.CENTER);
            contenedorBoton.getChildren().add(btnVerContrato);
            
            tarjetaPrincipal.getChildren().add(contenedorBoton);
        }
        
        // ENSAMBLAR TARJETA COMPLETA
        tarjetaPrincipal.getChildren().addAll(
            header, 
            new Separator(),
            seccionCliente, 
            seccionEvento, 
            seccionPaquete, 
            seccionPago
        );
        
        return tarjetaPrincipal;
    }

    @FXML
    private void cerrarDetalles() {
        if (panelDetalles != null) {
            panelDetalles.setVisible(false);
        }
    }

    @FXML
    private void accionRegresar() {
        try {
            App.setRoot("PanelPrincipal");
        } catch (Exception e) {
            System.err.println("Error al regresar al panel principal: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo regresar al panel principal: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}