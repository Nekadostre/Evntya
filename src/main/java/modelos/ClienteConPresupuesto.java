package modelos;

import javafx.scene.control.Button;

public class ClienteConPresupuesto {
    private String nombre;
    private String apellidos;
    private String fecha;
    private String paquete;
    private double monto;
    private Button botonEliminar;

    public ClienteConPresupuesto(String nombre, String apellidos, String fecha, String paquete, double monto, Button botonEliminar) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha = fecha;
        this.paquete = paquete;
        this.monto = monto;
        this.botonEliminar = botonEliminar;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getFecha() {
        return fecha;
    }

    public String getPaquete() {
        return paquete;
    }

    public double getMonto() {
        return monto;
    }

    public Button getBotonEliminar() {
        return botonEliminar;
    }
} 
