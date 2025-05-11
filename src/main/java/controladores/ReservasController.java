package controladores;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import database.Conexion;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.net.URL;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

public class ReservasController implements Initializable 
{
    @FXML private GridPane calendarioGrid;
    @FXML private Label lblMesAnio;

    private YearMonth mesActual;
    private Map<LocalDate, EventoInfo> eventos = new HashMap<>();
    private LocalDate fechaSeleccionada = null;
    private StackPane celdaSeleccionada = null;

    private final Color COLOR_PAGADO = Color.GREEN;
    private final Color COLOR_PRESUPUESTADO = Color.YELLOW;
    private final Color COLOR_SIN_EVENTO = Color.WHITE;
    private final Color COLOR_SELECCIONADO = Color.LIGHTBLUE;

    private class EventoInfo {
        ReservaDetalle manana;
        ReservaDetalle tarde;
    }

    private class ReservaDetalle {
        String cliente;
        String paquete;
        String metodoPago;
        String estado;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mesActual = YearMonth.now();
        actualizarLabelMesAnio();
        cargarReservas();
        mostrarCalendario();
    }

    private void cargarReservas() {
        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT r.fecha, r.horario, r.estado, c.nombre, p.nombre AS paquete, r.metodo_pago " +
                "FROM reservas r " +
                "JOIN clientes c ON r.cliente_id = c.id " +
                "JOIN paquetes p ON r.paquete_id = p.id " +
                "WHERE r.estado = 'pagado'")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String horario = rs.getString("horario").toLowerCase();

                ReservaDetalle detalle = new ReservaDetalle();
                detalle.cliente = rs.getString("nombre");
                detalle.paquete = rs.getString("paquete");
                detalle.metodoPago = rs.getString("metodo_pago");
                detalle.estado = rs.getString("estado");

                EventoInfo info = eventos.getOrDefault(fecha, new EventoInfo());
                if (horario.equals("matutino")) {
                    info.manana = detalle;
                } else if (horario.equals("vespertino")) {
                    info.tarde = detalle;
                }
                eventos.put(fecha, info);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al cargar las reservas desde la base de datos.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarCalendario() {
        calendarioGrid.getChildren().clear();
        String[] diasSemana = {"DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label diaSemana = new Label(diasSemana[i]);
            diaSemana.setFont(Font.font("System", FontWeight.BOLD, 12));
            calendarioGrid.add(diaSemana, i, 0);
        }

        LocalDate primerDia = mesActual.atDay(1);
        int diaSemana = primerDia.getDayOfWeek().getValue() % 7;
        int diasEnMes = mesActual.lengthOfMonth();

        int dia = 1;
        for (int i = 1; i <= 6; i++) {
            for (int j = 0; j < 7; j++) {
                if ((i == 1 && j < diaSemana) || dia > diasEnMes) continue;
                LocalDate fecha = mesActual.atDay(dia++);
                StackPane celda = crearCeldaCalendario(fecha);
                calendarioGrid.add(celda, j, i);
            }
        }
    }

    private StackPane crearCeldaCalendario(LocalDate fecha) {
        StackPane celda = new StackPane();
        celda.setMinSize(70, 70);
        Rectangle fondo = new Rectangle(70, 70, COLOR_SIN_EVENTO);
        fondo.setStroke(Color.BLACK);

        EventoInfo info = eventos.getOrDefault(fecha, new EventoInfo());

        Polygon mananaTriangulo = new Polygon(0, 0, 70, 0, 0, 70);
        Polygon tardeTriangulo = new Polygon(70, 0, 70, 70, 0, 70);

        mananaTriangulo.setFill(info.manana != null ? (info.manana.estado.equalsIgnoreCase("pagado") ? COLOR_PAGADO : COLOR_PRESUPUESTADO) : COLOR_SIN_EVENTO);
        tardeTriangulo.setFill(info.tarde != null ? (info.tarde.estado.equalsIgnoreCase("pagado") ? COLOR_PAGADO : COLOR_PRESUPUESTADO) : COLOR_SIN_EVENTO);

        mananaTriangulo.setOnMouseClicked(e -> mostrarInformacionReserva(fecha, true));
        tardeTriangulo.setOnMouseClicked(e -> mostrarInformacionReserva(fecha, false));

        Label diaLabel = new Label(String.valueOf(fecha.getDayOfMonth()));
        diaLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        celda.getChildren().addAll(fondo, mananaTriangulo, tardeTriangulo, diaLabel);
        return celda;
    }

    private void mostrarInformacionReserva(LocalDate fecha, boolean esManana) {
        EventoInfo info = eventos.get(fecha);
        if (info == null) return;

        ReservaDetalle detalle = esManana ? info.manana : info.tarde;
        if (detalle == null) {
            mostrarAlerta("No hay reservas para este horario.", Alert.AlertType.INFORMATION);
            return;
        }

        String mensaje = String.format(
            "Nombre del cliente: %s\n" +
            "Paquete: %s\n" +
            "Método de pago: %s\n" +
            "Estado: %s",
            detalle.cliente,
            detalle.paquete,
            detalle.metodoPago,
            detalle.estado
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Detalle de Reserva");
        alert.setHeaderText("Reserva para el día " + formatearFecha(fecha) + (esManana ? " (mañana)" : " (tarde)"));
        alert.setContentText(mensaje);

        ButtonType eliminar = new ButtonType("Eliminar esta reserva");
        ButtonType cerrar = new ButtonType("Cerrar");
        alert.getButtonTypes().setAll(eliminar, cerrar);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == eliminar) {
            try (Connection conn = Conexion.conectar();
                 PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM reservas WHERE fecha = ? AND horario = ?")) {
                stmt.setDate(1, java.sql.Date.valueOf(fecha));
                stmt.setString(2, esManana ? "matutino" : "vespertino");
                stmt.executeUpdate();
            } catch (SQLException ex) {
                mostrarAlerta("Error al eliminar reserva de la base de datos.", Alert.AlertType.ERROR);
            }

            if (esManana) {
                info.manana = null;
            } else {
                info.tarde = null;
            }
            eventos.put(fecha, info);
            mostrarCalendario();
            mostrarAlerta("Reserva eliminada.", Alert.AlertType.INFORMATION);
        }
    }

    private String formatearFecha(LocalDate fecha) {
        String[] diasSemana = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int diaSemanaIndex = fecha.getDayOfWeek().getValue() % 7;
        return diasSemana[diaSemanaIndex] + ", " + fecha.getDayOfMonth() + " de " + meses[fecha.getMonthValue() - 1] + " de " + fecha.getYear();
    }

    @FXML private void mesAnterior() {
        mesActual = mesActual.minusMonths(1);
        actualizarLabelMesAnio();
        mostrarCalendario();
    }

    @FXML private void mesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        actualizarLabelMesAnio();
        mostrarCalendario();
    }

    private void actualizarLabelMesAnio() {
        String[] meses = {"ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"};
        lblMesAnio.setText(meses[mesActual.getMonthValue() - 1] + " " + mesActual.getYear());
        lblMesAnio.setFont(Font.font("System", FontWeight.BOLD, 16));
    }

    @FXML private void handleRegresarButtonAction() throws IOException {
        App.setRoot("PanelPrincipal");
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}