
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import database.Conexion;
import modelos.ClienteTemporal;
import modelos.PaqueteTemporal;
import modelos.PresupuestoTemporal;

public class PresupuestoDAO {

    public static void guardarPresupuesto() throws SQLException {
        Connection conn;
        conn = Conexion.conectar();

        ClienteTemporal cliente = ClienteTemporal.getInstancia();
        PaqueteTemporal paquete = PaqueteTemporal.getInstancia();
        PresupuestoTemporal presupuesto = PresupuestoTemporal.getInstancia();

        String sql = "INSERT INTO presupuestos (cliente_id, paquete_id, extras, total, horario, plazos, forma_pago) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cliente.getId());
            stmt.setInt(2, paquete.getId());
            stmt.setString(3, paquete.getExtras());
            stmt.setDouble(4, paquete.getTotal());
            stmt.setString(5, presupuesto.getHorario());
            stmt.setString(6, presupuesto.getPlazos());
            stmt.setString(7, presupuesto.getFormaPago());

            stmt.executeUpdate();
        }
    }
}
