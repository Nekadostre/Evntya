package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mysql://srv1711.hstgr.io:3306/u984674772_dbsegunda?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "u984674772_root";
    private static final String PASSWORD = "Forgestudio4321$";

    public static Connection conectar() throws SQLException{
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexion exitosa a la base de datos.");
            return conn;
        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
            return null;
        }
    }
}
