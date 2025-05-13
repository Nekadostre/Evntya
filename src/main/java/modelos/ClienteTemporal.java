package modelos;

public class ClienteTemporal {
    private static ClienteTemporal instancia;
    private int id;
    private String nombre;
    private String apellido;
    private String rfc;

    private ClienteTemporal() {}

    public static ClienteTemporal getInstancia() {
        if (instancia == null) {
            instancia = new ClienteTemporal();
        }
        return instancia;
    }

    public void setDatos(int id, String nombre, String apellido, String rfc) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rfc = rfc;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getRfc() { return rfc; }
}
