package modelos;

public class ReservaDetalle {

    private String nombreCliente;

    public ReservaDetalle(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String descripcion() {
        return "Cliente: " + nombreCliente;
    }
}
