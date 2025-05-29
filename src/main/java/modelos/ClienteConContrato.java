package modelos;

import javafx.scene.control.Button;

public class ClienteConContrato {
    private int clienteId;
    private int contratoId; // Puede ser presupuesto_id o contrato_id
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String fechaContrato;
    private String paquete;
    private double montoTotal;
    private String estado;
    private Button botonAcciones;
    
    // Constructor completo
    public ClienteConContrato(int clienteId, int contratoId, String nombreCompleto, 
                             String telefono, String email, String fechaContrato, 
                             String paquete, double montoTotal, String estado) {
        this.clienteId = clienteId;
        this.contratoId = contratoId;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono != null ? telefono : "No registrado";
        this.email = email != null ? email : "No registrado";
        this.fechaContrato = fechaContrato;
        this.paquete = paquete;
        this.montoTotal = montoTotal;
        this.estado = estado;
        
        // Crear botón de acciones
        this.botonAcciones = new Button("⚙️ Opciones");
        this.botonAcciones.getStyleClass().add("boton-opciones");
    }
    
    // Constructor simplificado para compatibilidad
    public ClienteConContrato(String nombreCompleto, String telefono, String fechaContrato, 
                             String paquete, double montoTotal, Button botonAcciones) {
        this(0, 0, nombreCompleto, telefono, "No registrado", fechaContrato, 
             paquete, montoTotal, "Activo");
        this.botonAcciones = botonAcciones;
    }
    
    // Getters y Setters
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    
    public int getContratoId() { return contratoId; }
    public void setContratoId(int contratoId) { this.contratoId = contratoId; }
    
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    
    // Para compatibilidad con el código anterior
    public String getNombre() { 
        return nombreCompleto != null ? nombreCompleto.split(" ")[0] : ""; 
    }
    
    public String getApellidos() { 
        if (nombreCompleto == null) return "";
        String[] partes = nombreCompleto.split(" ");
        return partes.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(partes, 1, partes.length)) : "";
    }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFechaContrato() { return fechaContrato; }
    public void setFechaContrato(String fechaContrato) { this.fechaContrato = fechaContrato; }
    
    // Para compatibilidad
    public String getFecha() { return fechaContrato; }
    
    public String getPaquete() { return paquete; }
    public void setPaquete(String paquete) { this.paquete = paquete; }
    
    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    
    // Para compatibilidad
    public double getMonto() { return montoTotal; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Button getBotonAcciones() { return botonAcciones; }
    public void setBotonAcciones(Button botonAcciones) { this.botonAcciones = botonAcciones; }
    
    // Para compatibilidad
    public Button getBotonEliminar() { return botonAcciones; }
    
    @Override
    public String toString() {
        return "ClienteConContrato{" +
                "clienteId=" + clienteId +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", telefono='" + telefono + '\'' +
                ", paquete='" + paquete + '\'' +
                ", montoTotal=" + montoTotal +
                ", estado='" + estado + '\'' +
                '}';
    }
}