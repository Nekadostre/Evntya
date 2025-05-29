package modelos;

import javafx.scene.control.Button;

public class ClienteConPresupuesto {
    private int id;
    private String nombre;
    private String apellidos;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String fecha;
    private String fechaContrato;
    private String paquete;
    private Double monto;
    private String estado;
    private Button botonEliminar;
    
    // Constructor vacío
    public ClienteConPresupuesto() {
    }
    
    // Constructor que funciona con tu código actual
    public ClienteConPresupuesto(String nombre, String apellidos, String fecha, 
                               String paquete, Double monto, Button botonEliminar) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha = fecha;
        this.paquete = paquete;
        this.monto = monto;
        this.botonEliminar = botonEliminar;
        
        // Construir nombre completo
        this.nombreCompleto = nombre;
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            this.nombreCompleto += " " + apellidos;
        }
        
        // Valores por defecto
        this.telefono = "Sin teléfono";
        this.email = "Sin email";
        this.fechaContrato = fecha;
        this.estado = "Sin estado";
    }
    
    // Constructor completo nuevo
    public ClienteConPresupuesto(String nombreCompleto, String telefono, String email,
                               String fechaContrato, String paquete, Double monto, 
                               String estado, Button botonEliminar) {
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.email = email;
        this.fechaContrato = fechaContrato;
        this.paquete = paquete;
        this.monto = monto;
        this.estado = estado;
        this.botonEliminar = botonEliminar;
        
        // Separar nombre completo
        if (nombreCompleto != null) {
            String[] partes = nombreCompleto.split(" ");
            this.nombre = partes[0];
            if (partes.length > 1) {
                this.apellidos = String.join(" ", java.util.Arrays.copyOfRange(partes, 1, partes.length));
            } else {
                this.apellidos = "";
            }
        }
        
        this.fecha = fechaContrato;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        actualizarNombreCompleto();
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
        actualizarNombreCompleto();
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
        
        // Separar en nombre y apellidos
        if (nombreCompleto != null) {
            String[] partes = nombreCompleto.split(" ");
            this.nombre = partes[0];
            if (partes.length > 1) {
                this.apellidos = String.join(" ", java.util.Arrays.copyOfRange(partes, 1, partes.length));
            } else {
                this.apellidos = "";
            }
        }
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
        this.fechaContrato = fecha;
    }

    public String getFechaContrato() {
        return fechaContrato;
    }

    public void setFechaContrato(String fechaContrato) {
        this.fechaContrato = fechaContrato;
        this.fecha = fechaContrato;
    }

    public String getPaquete() {
        return paquete;
    }

    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Button getBotonEliminar() {
        return botonEliminar;
    }

    public void setBotonEliminar(Button botonEliminar) {
        this.botonEliminar = botonEliminar;
    }
    
    // Método auxiliar
    private void actualizarNombreCompleto() {
        this.nombreCompleto = nombre;
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            this.nombreCompleto += " " + apellidos;
        }
    }

    @Override
    public String toString() {
        return "ClienteConPresupuesto{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", fechaContrato='" + fechaContrato + '\'' +
                ", paquete='" + paquete + '\'' +
                ", monto=" + monto +
                ", estado='" + estado + '\'' +
                '}';
    }
}