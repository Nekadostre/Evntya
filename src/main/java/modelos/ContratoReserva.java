package modelos;

public class ContratoReserva {
    private int contratoId;
    private String nombreCliente;
    private String nombreFestejado;
    private String fechaEvento;
    private String horario;
    private String paquete;
    private String monto;
    private String estado;
    private String archivoRuta;
    
    // Constructor vacío
    public ContratoReserva() {}
    
    // Constructor completo
    public ContratoReserva(int contratoId, String nombreCliente, String nombreFestejado, 
                          String fechaEvento, String horario, String paquete, 
                          String monto, String estado, String archivoRuta) {
        this.contratoId = contratoId;
        this.nombreCliente = nombreCliente;
        this.nombreFestejado = nombreFestejado;
        this.fechaEvento = fechaEvento;
        this.horario = horario;
        this.paquete = paquete;
        this.monto = monto;
        this.estado = estado;
        this.archivoRuta = archivoRuta;
    }
    
    // Getters y Setters
    public int getContratoId() {
        return contratoId;
    }
    
    public void setContratoId(int contratoId) {
        this.contratoId = contratoId;
    }
    
    public String getNombreCliente() {
        return nombreCliente;
    }
    
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    
    public String getNombreFestejado() {
        return nombreFestejado;
    }
    
    public void setNombreFestejado(String nombreFestejado) {
        this.nombreFestejado = nombreFestejado;
    }
    
    public String getFechaEvento() {
        return fechaEvento;
    }
    
    public void setFechaEvento(String fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
    
    public String getHorario() {
        return horario;
    }
    
    public void setHorario(String horario) {
        this.horario = horario;
    }
    
    public String getPaquete() {
        return paquete;
    }
    
    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }
    
    public String getMonto() {
        return monto;
    }
    
    public void setMonto(String monto) {
        this.monto = monto;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getArchivoRuta() {
        return archivoRuta;
    }
    
    public void setArchivoRuta(String archivoRuta) {
        this.archivoRuta = archivoRuta;
    }
    
    @Override
    public String toString() {
        return "ContratoReserva{" +
                "contratoId=" + contratoId +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", nombreFestejado='" + nombreFestejado + '\'' +
                ", fechaEvento='" + fechaEvento + '\'' +
                ", horario='" + horario + '\'' +
                ", paquete='" + paquete + '\'' +
                ", monto='" + monto + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}