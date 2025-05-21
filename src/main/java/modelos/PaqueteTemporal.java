package modelos;

import java.util.ArrayList;
import java.util.List;

public class PaqueteTemporal {
    private static PaqueteTemporal instancia;

    private int id;
    private String nombre;       // Nombre del paquete
    private double precio;       // Precio del paquete
    private String extras;       // Texto de extras
    private double total;        // Total general (paquete + extras)
    private String nombrePaquete;

    private List<Extra> extrasSeleccionados = new ArrayList<>();

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

    public String getNombrePaquete() {
        return nombrePaquete;
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

    // NUEVO: Getter y Setter de lista de extras
    public List<Extra> getExtrasSeleccionados() {
        return extrasSeleccionados;
    }

    public void setExtrasSeleccionados(List<Extra> extrasSeleccionados) {
        this.extrasSeleccionados = extrasSeleccionados;
    }

    public String getResumenExtras() {
        StringBuilder resumen = new StringBuilder();
        for (Extra extra : extrasSeleccionados) {
            resumen.append("- ")
                   .append(extra.getNombre())
                   .append(" x")
                   .append(extra.getCantidad())
                   .append(" ($")
                   .append(extra.getPrecio() * extra.getCantidad())
                   .append(")\n");
        }
        return resumen.toString().trim();
    }

    public void reset() {
        this.nombre = "";
        this.precio = 0.0;
        this.extras = "";
        this.total = 0.0;
        this.extrasSeleccionados.clear();
    }
}
