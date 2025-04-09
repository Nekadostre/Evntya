package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginDAO {

   public static String[] validarLoginConDatos(String usuarioId, String contrasena) {
    String[] datos = null;

    try (Connection conn = Conexion.conectar()) {
        String sql = "SELECT nombre, apellidos, rol FROM usuarios WHERE usuario = ? AND contrasena = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, usuarioId);
        stmt.setString(2, contrasena);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            datos = new String[] {
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getString("rol") 
            };
        }

    } catch (Exception e) {
        System.out.println("Error en login: " + e.getMessage());
    }

    return datos;
}

   public static void registrarHistorialLogin(String usuarioId) {
    try (Connection conn = Conexion.conectar()) {
        String sql = "INSERT INTO historial_login (usuario_id) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, usuarioId);
        stmt.executeUpdate();
    } catch (Exception e) {
        System.out.println("Error al registrar historial de login: " + e.getMessage());
    }
}


}
