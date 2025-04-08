package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HistorialLoginDAO {

    public static List<HistorialLogin> obtenerHistorial() {
        List<HistorialLogin> lista = new ArrayList<>();

        try (Connection conn = Conexion.conectar()) {
            String sql = "SELECT usuario_id, fecha FROM historial_login ORDER BY fecha DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String usuarioId = rs.getString("usuario_id");
                String fecha = rs.getString("fecha");
                lista.add(new HistorialLogin(usuarioId, fecha));
            }

        } catch (Exception e) {
            System.out.println("Error al obtener historial de login: " + e.getMessage());
        }

        return lista;
    }
}
