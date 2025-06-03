package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    
    private static final String URL = "jdbc:mysql://srv1711.hstgr.io:3306/u984674772_dbsegunda?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "u984674772_root";
    private static final String PASSWORD = "Forgestudio4321$";
    
    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // Sin logs de conexión exitosa
            return conn;
        } catch (SQLException e) {
            // Solo mostrar errores importantes
            System.err.println("Error de conexión a la base de datos: " + e.getMessage());
            return null;
        }
    }
}