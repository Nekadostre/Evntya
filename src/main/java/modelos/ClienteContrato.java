package modelos;

public class ClienteContrato {
    private int clienteId;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String fechaContrato;
    private String paquete;
    private String monto;           // Agregado campo monto
    private String montoTotal;
    private String estado;
    private String archivoRuta;

    // Constructores
    public ClienteContrato() {}

    public ClienteContrato(int clienteId, String nombre, String apellidoPaterno, String apellidoMaterno,
                          String telefono, String email, String fechaContrato, String paquete,
                          String monto, String estado, String archivoRuta) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombreCompleto = (nombre + " " + apellidoPaterno + " " + (apellidoMaterno != null ? apellidoMaterno : "")).trim();
        this.telefono = telefono;
        this.email = email;
        this.fechaContrato = fechaContrato;
        this.paquete = paquete;
        this.monto = monto;
        this.montoTotal = monto;  // Mantener compatibilidad
        this.estado = estado;
        this.archivoRuta = archivoRuta;
    }

    // Getters y Setters
    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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

    public String getFechaContrato() {
        return fechaContrato;
    }

    public void setFechaContrato(String fechaContrato) {
        this.fechaContrato = fechaContrato;
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
        this.montoTotal = monto;  // Mantener sincronizados
    }

    public String getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(String montoTotal) {
        this.montoTotal = montoTotal;
        this.monto = montoTotal;  // Mantener sincronizados
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
        return "ClienteContrato{" +
                "clienteId=" + clienteId +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", fechaContrato='" + fechaContrato + '\'' +
                ", paquete='" + paquete + '\'' +
                ", montoTotal='" + montoTotal + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}