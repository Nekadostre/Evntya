package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pollix
 */
public class LoginDAO {
    
    public boolean validarUsuario (String nombreUsuario, String passwordUsuario){
        
        String consulta = "SELECT * FROM \"Login_2024\".\"usuarioSistema\" "
                + "WHERE \"usuarioSistema\".\"Login_usuario\" = ? AND \"usuarioSistema\".\"Login_contra\" = ?";
    
        try (Connection conectar = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbsgref", "root", "admin123");
        PreparedStatement stmt = conectar.prepareStatement(consulta)) {
        
        stmt.setString(1, nombreUsuario);
        stmt.setString(2, passwordUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
           
            return rs.next();
            }

        } 
        catch (SQLException e) {
        e.printStackTrace();  
        return false; 
        
        }
    }
}
