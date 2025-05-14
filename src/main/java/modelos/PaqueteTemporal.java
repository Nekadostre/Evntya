
package modelos;

public class PaqueteTemporal {
    private static PaqueteTemporal instancia;

    private int id;
    private String nombre;       // Nombre del paquete
    private double precio;       // Precio del paquete
    private String extras;       // Texto de extras
    private double total;        // Total general (paquete + extras)

    private PaqueteTemporal() {
        this.nombre = "";
        this.extras = "";
        this.precio = 0.0;
        this.total = 0.0;
    }

    public static PaqueteTemporal getInstancia() {
        if (instancia == null) {
            instancia = new PaqueteTemporal();
        }
        return instancia;
    }

    public void setDatos(int id, String nombre, double precio) {
        this.id = id;
                this.nombre = nombre;
        this.precio = precio;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public String getExtras() {
        return extras;
    }

    public double getTotal() {
        return total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void reset() {
        this.nombre = "";
        this.precio = 0.0;
        this.extras = "";
        this.total = 0.0;
    }
}
