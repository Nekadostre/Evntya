package controladores;

import database.Conexion;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import modelos.EventoInfo;
import modelos.ReservaDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReservasController {

    @FXML
    private GridPane gridCalendario;
    @FXML
    private Label labelMes;

    private LocalDate mesActual = LocalDate.now();
    private Map<LocalDate, EventoInfo> eventos = new HashMap<>();

    @FXML
    public void initialize() {
        cargarEventosPagados();
        construirCalendario();
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



    private void cargarEventosPagados() {
        eventos.clear();
        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT r.fecha, r.horario, c.nombre AS cliente_nombre " +
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

                if (horario.equalsIgnoreCase("matutino")) {
                    info.manana = detalle;
                } else if (horario.equalsIgnoreCase("vespertino")) {
                    info.tarde = detalle;
                }

                eventos.put(fecha, info);
            }
            System.out.println("Eventos cargados: " + eventos.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void construirCalendario() {
        gridCalendario.getChildren().clear();
        labelMes.setText(obtenerNombreMes(mesActual.getMonthValue()) + " " + mesActual.getYear());

        LocalDate primerDiaMes = mesActual.withDayOfMonth(1);
        int diaSemana = primerDiaMes.getDayOfWeek().getValue() % 7;

        int fila = 1;
        int columna = diaSemana;
        int diasMes = mesActual.lengthOfMonth();

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mesActual.withDayOfMonth(dia);
            StackPane celda = crearCeldaCalendario(fecha);
            gridCalendario.add(celda, columna, fila);

            columna++;
            if (columna > 6) {
                columna = 0;
                fila++;
            }
        }
    }

    private StackPane crearCeldaCalendario(LocalDate fecha) {
        StackPane celda = new StackPane();
        celda.setMinSize(70, 70);
        Rectangle fondo = new Rectangle(70, 70, Color.WHITE);
        fondo.setStroke(Color.GRAY);

        EventoInfo info = eventos.getOrDefault(fecha, new EventoInfo());

        Polygon mananaTriangulo = new Polygon(0, 0, 70, 0, 0, 70);
        Polygon tardeTriangulo = new Polygon(70, 0, 70, 70, 0, 70);

        mananaTriangulo.setFill(info.manana != null ? Color.LIGHTGREEN : Color.WHITE);
        tardeTriangulo.setFill(info.tarde != null ? Color.LIGHTBLUE : Color.WHITE);

        mananaTriangulo.setOnMouseClicked(e -> mostrarInformacionReserva(fecha, "matutino"));
        tardeTriangulo.setOnMouseClicked(e -> mostrarInformacionReserva(fecha, "vespertino"));

        Label diaLabel = new Label(String.valueOf(fecha.getDayOfMonth()));
        diaLabel.setStyle("-fx-font-weight: bold;");

        celda.getChildren().addAll(fondo, mananaTriangulo, tardeTriangulo, diaLabel);
        return celda;
    }

    private void mostrarInformacionReserva(LocalDate fecha, String horario) {
        EventoInfo info = eventos.get(fecha);
        if (info == null) return;

        ReservaDetalle detalle = horario.equalsIgnoreCase("matutino") ? info.manana : info.tarde;
        if (detalle == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Reserva: " + horario + " - " + fecha);
        alert.setContentText("Cliente: " + detalle.getNombreCliente() + "\n\n¿Deseas eliminar esta reserva?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarReserva(fecha, horario);
            cargarEventosPagados();
            construirCalendario();
        }
    }

    private void eliminarReserva(LocalDate fecha, String horario) {
        try (Connection conn = Conexion.conectar()) {
            String sql = "DELETE FROM reservas WHERE fecha = ? AND horario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(fecha));
            stmt.setString(2, horario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {
            "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
            "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
        };
        return meses[mes - 1];
    }
}
