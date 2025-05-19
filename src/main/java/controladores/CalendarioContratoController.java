package controladores;

import database.Conexion;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import modelos.Cliente;
import modelos.DatosContratoTemporal;
import modelos.EventoInfo;
import modelos.ReservaDetalle;

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

    @FXML private TextField txtBuscar;
    @FXML private ListView<String> listaResultados;
    @FXML private GridPane gridCalendario;
    @FXML private Label labelMes;

    private LocalDate mesActual = LocalDate.now();
    private List<Cliente> clientesEncontrados = new ArrayList<>();
    private Map<LocalDate, EventoInfo> eventos = new HashMap<>();
    private final PauseTransition pausaBusqueda = new PauseTransition(Duration.millis(300));

    @FXML
    public void initialize() {
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
            String sql = "SELECT * FROM clientes WHERE nombre LIKE ? OR apellido_paterno LIKE ? OR apellido_materno LIKE ? OR rfc LIKE ? OR curp LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int i = 1; i <= 5; i++) {
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
                        rs.getString("curp")
                );
                clientesEncontrados.add(c);
                listaResultados.getItems().add(c.getId() + " - " + c.getNombreCompleto() + " | RFC: " + c.getRfc());
            }
        } catch (SQLException e) {
        }
    }

    private void seleccionarCliente() {
        int index = listaResultados.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Cliente seleccionado = clientesEncontrados.get(index);

            if (tienePresupuesto(seleccionado.getId())) {
                DatosContratoTemporal.cliente = seleccionado;
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Se ha encontrado el cliente y presupuesto correctamente!");
                ok.show();
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setHeaderText("Este cliente no tiene un presupuesto registrado.");
                alerta.show();
            }
        }
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

    private void cargarEventosPagados() {
        eventos.clear();
        try (Connection conn = Conexion.conectar()) {
            String sql = 
    "SELECT r.fecha, r.horario, c.nombre AS cliente_nombre " +
    "FROM reservas r " +
    "JOIN clientes c ON r.cliente_id = c.id " +
    "WHERE r.estado = 'pagado'";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String horario = rs.getString("horario");
                String nombre = rs.getString("cliente_nombre");

                EventoInfo info = eventos.getOrDefault(fecha, new EventoInfo());
                ReservaDetalle detalle = new ReservaDetalle(nombre);

                if ("matutino".equalsIgnoreCase(horario)) {
                    info.manana = detalle;
                } else if ("vespertino".equalsIgnoreCase(horario)) {
                    info.tarde = detalle;
                }
                eventos.put(fecha, info);
            }
            System.out.println("Eventos cargados: " + eventos.size());
        } catch (SQLException e) {
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

        Label diaLabel = new Label(String.valueOf(fecha.getDayOfMonth()));
        diaLabel.setStyle("-fx-font-weight: bold;");

        celda.getChildren().addAll(fondo, mananaTriangulo, tardeTriangulo, diaLabel);

        celda.setOnMouseClicked(e -> {
            if (DatosContratoTemporal.cliente != null) {
                DatosContratoTemporal.fechaEvento = fecha;
                Alert infoFecha = new Alert(Alert.AlertType.INFORMATION);
                infoFecha.setHeaderText("✓ Fecha seleccionada: " + fecha);
                infoFecha.show();
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setHeaderText("Selecciona un cliente con presupuesto antes de elegir una fecha.");
                alerta.show();
            }
        });

        return celda;
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
        App.setRoot("PanelPrincipal");
    }

    @FXML
    private void accionSiguiente() throws IOException {
        if (DatosContratoTemporal.fechaEvento != null) {
            App.setRoot("VistaPreviaContrato");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Selecciona una fecha para continuar.");
            alert.showAndWait();
        }
    }
}
