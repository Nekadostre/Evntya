package controladores;

import database.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modelos.ContratoReserva;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ContratosController {

    @FXML private TableView<ContratoReserva> tablaContratos;
    @FXML private TableColumn<ContratoReserva, String> colCliente;
    @FXML private TableColumn<ContratoReserva, String> colFestejado;
    @FXML private TableColumn<ContratoReserva, String> colFechaEvento;
    @FXML private TableColumn<ContratoReserva, String> colHorario;
    @FXML private TableColumn<ContratoReserva, String> colPaquete;
    @FXML private TableColumn<ContratoReserva, String> colMonto;
    @FXML private TableColumn<ContratoReserva, String> colEstado;
    @FXML private TableColumn<ContratoReserva, String> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private ObservableList<ContratoReserva> listaContratos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        limpiarContratosVencidos(); // Limpiar automáticamente al cargar
        cargarDatos();
    }

    private void configurarTabla() {
        // Configurar columnas
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFestejado.setCellValueFactory(new PropertyValueFactory<>("nombreFestejado"));
        colFechaEvento.setCellValueFactory(new PropertyValueFactory<>("fechaEvento"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colPaquete.setCellValueFactory(new PropertyValueFactory<>("paquete"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar anchos
        colCliente.setPrefWidth(150);
        colFestejado.setPrefWidth(120);
        colFechaEvento.setPrefWidth(100);
        colHorario.setPrefWidth(100);
        colPaquete.setPrefWidth(130);
        colMonto.setPrefWidth(90);
        colEstado.setPrefWidth(120);
        colAcciones.setPrefWidth(80);
        
        // Columna de acciones
        colAcciones.setCellFactory(col -> {
            TableCell<ContratoReserva, String> cell = new TableCell<ContratoReserva, String>() {
                private final Button btnAcciones = new Button("⚙️");
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        btnAcciones.setOnAction(e -> {
                            ContratoReserva contrato = getTableRow().getItem();
                            mostrarMenuAcciones(contrato);
                        });
                        btnAcciones.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                           "-fx-background-radius: 15; -fx-cursor: hand; -fx-pref-width: 30;");
                        setGraphic(btnAcciones);
                    }
                }
            };
            return cell;
        });
        
        tablaContratos.getStyleClass().add("tabla-clientes-moderna");
        System.out.println("✅ Tabla de contratos configurada");
    }

    // 🧹 LIMPIAR CONTRATOS 1 MES DESPUÉS DE LA FECHA DEL EVENTO
    private void limpiarContratosVencidos() {
        try (Connection conn = Conexion.conectar()) {
            System.out.println("🧹 Limpiando contratos vencidos (1 mes después del evento)...");
            
            // Primero verificar cuántos contratos están vencidos
            String sqlVerificar = """
                SELECT COUNT(*) as total,
                       GROUP_CONCAT(CONCAT(c.nombre, ' ', c.apellido_paterno, ' - ', cont.nombre_festejado) SEPARATOR ', ') as nombres
                FROM contratos cont
                INNER JOIN clientes c ON cont.cliente_id = c.id
                WHERE cont.fecha_evento < DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
                """;
            
            PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar);
            ResultSet rsVerificar = stmtVerificar.executeQuery();
            
            int contratosVencidos = 0;
            String nombresVencidos = "";
            
            if (rsVerificar.next()) {
                contratosVencidos = rsVerificar.getInt("total");
                nombresVencidos = rsVerificar.getString("nombres");
            }
            
            if (contratosVencidos > 0) {
                System.out.println("📋 Contratos a eliminar: " + nombresVencidos);
                
                // Eliminar reservas asociadas primero
                String sqlEliminarReservas = """
                    DELETE r FROM reservas r
                    INNER JOIN contratos cont ON r.cliente_id = cont.cliente_id 
                        AND r.fecha = cont.fecha_evento 
                        AND r.horario = cont.horario
                    WHERE cont.fecha_evento < DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
                    """;
                
                PreparedStatement stmtReservas = conn.prepareStatement(sqlEliminarReservas);
                int reservasEliminadas = stmtReservas.executeUpdate();
                
                // Eliminar contratos vencidos
                String sqlEliminarContratos = """
                    DELETE FROM contratos 
                    WHERE fecha_evento < DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
                    """;
                
                PreparedStatement stmtContratos = conn.prepareStatement(sqlEliminarContratos);
                int contratosEliminados = stmtContratos.executeUpdate();
                
                System.out.println("🗑️ Eliminados automáticamente:");
                System.out.println("   - " + contratosEliminados + " contratos vencidos");
                System.out.println("   - " + reservasEliminadas + " reservas asociadas");
                
            } else {
                System.out.println("✅ No hay contratos vencidos para eliminar");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al limpiar contratos vencidos: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        listaContratos.clear();
        
        try (Connection conn = Conexion.conectar()) {
            System.out.println("=== CARGANDO CONTRATOS Y RESERVAS ACTIVOS ===");
            
            // 🎯 CONSULTA: Solo contratos no vencidos (menos de 1 mes después del evento)
            String sql = """
                SELECT 
                    cont.id as contrato_id,
                    CONCAT(c.nombre, ' ', c.apellido_paterno, 
                           CASE WHEN c.apellido_materno IS NOT NULL 
                                THEN CONCAT(' ', c.apellido_materno) 
                                ELSE '' END) as nombre_cliente,
                    cont.nombre_festejado,
                    cont.fecha_evento,
                    cont.horario,
                    paq.nombre as paquete_nombre,
                    cont.total as monto_contrato,
                    cont.estado as estado_contrato,
                    cont.archivo_ruta,
                    DATEDIFF(DATE_ADD(cont.fecha_evento, INTERVAL 1 MONTH), CURDATE()) as dias_hasta_eliminacion,
                    CASE 
                        WHEN cont.fecha_evento > CURDATE() THEN 'Próximo'
                        WHEN cont.fecha_evento = CURDATE() THEN 'Hoy'
                        WHEN cont.fecha_evento < CURDATE() THEN 'Realizado'
                    END as estado_evento
                FROM contratos cont
                INNER JOIN clientes c ON cont.cliente_id = c.id
                INNER JOIN paquetes paq ON cont.paquete_id = paq.id
                WHERE cont.fecha_evento >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
                ORDER BY cont.fecha_evento ASC
                """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            while (rs.next()) {
                ContratoReserva contratoReserva = new ContratoReserva();
                
                // Datos del contrato
                contratoReserva.setContratoId(rs.getInt("contrato_id"));
                contratoReserva.setNombreCliente(rs.getString("nombre_cliente"));
                contratoReserva.setNombreFestejado(rs.getString("nombre_festejado"));
                
                // Fecha del evento
                try {
                    java.sql.Date fechaEvento = rs.getDate("fecha_evento");
                    if (fechaEvento != null) {
                        contratoReserva.setFechaEvento(fechaEvento.toLocalDate().format(formatter));
                    }
                } catch (SQLException e) {
                    contratoReserva.setFechaEvento("Fecha inválida");
                }
                
                // Otros datos
                contratoReserva.setHorario(rs.getString("horario"));
                contratoReserva.setPaquete(rs.getString("paquete_nombre"));
                
                double monto = rs.getDouble("monto_contrato");
                contratoReserva.setMonto("$" + String.format("%.0f", monto));
                
                // Estado con días restantes hasta eliminación
                String estadoEvento = rs.getString("estado_evento");
                int diasHastaEliminacion = rs.getInt("dias_hasta_eliminacion");
                
                if (estadoEvento.equals("Próximo")) {
                    contratoReserva.setEstado("Próximo");
                } else if (estadoEvento.equals("Hoy")) {
                    contratoReserva.setEstado("HOY");
                } else {
                    contratoReserva.setEstado("Realizado (" + diasHastaEliminacion + " días)");
                }
                
                contratoReserva.setArchivoRuta(rs.getString("archivo_ruta"));
                
                listaContratos.add(contratoReserva);
            }
            
            // Actualizar tabla
            tablaContratos.setItems(listaContratos);
            lblTotal.setText(String.valueOf(listaContratos.size()));
            
            if (listaContratos.isEmpty()) {
                System.out.println("📭 No hay contratos activos");
            } else {
                System.out.println("✅ Cargados " + listaContratos.size() + " contratos activos");
                
                // Mostrar resumen por estado
                long proximosEventos = listaContratos.stream().filter(c -> c.getEstado().equals("Próximo")).count();
                long eventosHoy = listaContratos.stream().filter(c -> c.getEstado().equals("HOY")).count();
                long eventosRealizados = listaContratos.stream().filter(c -> c.getEstado().startsWith("Realizado")).count();
                
                System.out.println("📊 Resumen:");
                System.out.println("   - Próximos: " + proximosEventos);
                System.out.println("   - Hoy: " + eventosHoy);
                System.out.println("   - Realizados: " + eventosRealizados);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar contratos: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los contratos: " + e.getMessage());
        }
    }

    @FXML
    private void limpiarContratosManual() {
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("Limpiar Contratos Vencidos");
        confirmacion.setHeaderText("¿Limpiar contratos vencidos?");
        confirmacion.setContentText("Se eliminarán automáticamente:\n" +
                                   "• Contratos con más de 1 mes después del evento\n" +
                                   "• Las reservas asociadas\n\n" +
                                   "Esta acción NO se puede deshacer.");
        
        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            limpiarContratosVencidos();
            cargarDatos();
            mostrarAlerta("Información", "Contratos vencidos eliminados automáticamente");
        }
    }

    @FXML
    private void actualizarDatos() {
        System.out.println("🔄 Actualizando contratos...");
        limpiarContratosVencidos(); // Limpiar automáticamente
        cargarDatos();
        mostrarAlerta("Información", "Contratos actualizados correctamente");
    }

    @FXML
    private void buscarContrato() {
        String textoBusqueda = txtBuscar.getText().trim();
        
        if (textoBusqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        
        // Implementar búsqueda en contratos
        cargarDatos();
    }

    private void mostrarMenuAcciones(ContratoReserva contrato) {
        ContextMenu menuContexto = new ContextMenu();
        
        MenuItem verDetalles = new MenuItem("📋 Ver Detalles");
        verDetalles.setOnAction(e -> mostrarDetallesContrato(contrato));
        
        MenuItem verPDF = new MenuItem("📄 Ver PDF");
        verPDF.setOnAction(e -> abrirPDF(contrato));
        if (contrato.getArchivoRuta() == null || contrato.getArchivoRuta().trim().isEmpty()) {
            verPDF.setDisable(true);
        }
        
        MenuItem editarContrato = new MenuItem("✏️ Editar");
        editarContrato.setOnAction(e -> editarContrato(contrato));
        
        MenuItem eliminar = new MenuItem("🗑️ Eliminar");
        eliminar.setOnAction(e -> eliminarContrato(contrato));
        
        menuContexto.getItems().addAll(verDetalles, verPDF, editarContrato, new SeparatorMenuItem(), eliminar);
        menuContexto.show(tablaContratos, 
                         tablaContratos.getLayoutX() + 100, 
                         tablaContratos.getLayoutY() + 100);
    }

    private void mostrarDetallesContrato(ContratoReserva contrato) {
        Alert detalles = new Alert(Alert.AlertType.INFORMATION);
        detalles.setTitle("Detalles del Contrato");
        detalles.setHeaderText("Información completa del evento");
        
        String info = "Cliente: " + contrato.getNombreCliente() + "\n" +
                     "Festejado: " + contrato.getNombreFestejado() + "\n" +
                     "Fecha del Evento: " + contrato.getFechaEvento() + "\n" +
                     "Horario: " + contrato.getHorario() + "\n" +
                     "Paquete: " + contrato.getPaquete() + "\n" +
                     "Monto: " + contrato.getMonto() + "\n" +
                     "Estado: " + contrato.getEstado();
        
        detalles.setContentText(info);
        detalles.show();
    }

    private void abrirPDF(ContratoReserva contrato) {
        if (contrato.getArchivoRuta() == null || contrato.getArchivoRuta().trim().isEmpty()) {
            mostrarAlerta("Información", "Este contrato no tiene un archivo PDF asociado");
            return;
        }
        
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(contrato.getArchivoRuta()));
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo abrir el archivo PDF: " + e.getMessage());
        }
    }

    private void editarContrato(ContratoReserva contrato) {
        // Implementar edición de contrato
        mostrarAlerta("Información", "Función de edición en desarrollo");
    }

    private void eliminarContrato(ContratoReserva contrato) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Contrato");
        confirmacion.setHeaderText("¿Eliminar contrato?");
        confirmacion.setContentText("Se eliminará el contrato de: " + contrato.getNombreCliente() + 
                                   "\nEvento: " + contrato.getNombreFestejado() +
                                   "\n\nTambién se eliminará la reserva asociada.");
        
        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = Conexion.conectar()) {
                // Eliminar reserva primero
                String sqlReserva = "DELETE FROM reservas WHERE cliente_id = (SELECT cliente_id FROM contratos WHERE id = ?) AND fecha = (SELECT fecha_evento FROM contratos WHERE id = ?)";
                PreparedStatement stmtReserva = conn.prepareStatement(sqlReserva);
                stmtReserva.setInt(1, contrato.getContratoId());
                stmtReserva.setInt(2, contrato.getContratoId());
                stmtReserva.executeUpdate();
                
                // Eliminar contrato
                String sqlContrato = "DELETE FROM contratos WHERE id = ?";
                PreparedStatement stmtContrato = conn.prepareStatement(sqlContrato);
                stmtContrato.setInt(1, contrato.getContratoId());
                int eliminados = stmtContrato.executeUpdate();
                
                if (eliminados > 0) {
                    mostrarAlerta("Éxito", "Contrato y reserva eliminados correctamente");
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el contrato");
                }
                
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar contrato: " + e.getMessage());
            }
        }
    }

    @FXML
    private void accionRegresar() {
        try {
            App.setRoot("PanelPrincipal");
        } catch (IOException e) {
            System.err.println("Error al regresar al panel principal: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo regresar al panel principal");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Métodos FXML para botones
    @FXML private void verDetalles() {
        ContratoReserva seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            mostrarDetallesContrato(seleccionado);
        } else {
            mostrarAlerta("Atención", "Selecciona un contrato de la tabla");
        }
    }

    @FXML private void verContratoPDF() {
        ContratoReserva seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            abrirPDF(seleccionado);
        } else {
            mostrarAlerta("Atención", "Selecciona un contrato de la tabla");
        }
    }

    @FXML private void editarSeleccionado() {
        ContratoReserva seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            editarContrato(seleccionado);
        } else {
            mostrarAlerta("Atención", "Selecciona un contrato de la tabla");
        }
    }

    @FXML private void eliminarSeleccionado() {
        ContratoReserva seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            eliminarContrato(seleccionado);
        } else {
            mostrarAlerta("Atención", "Selecciona un contrato de la tabla");
        }
    }
}