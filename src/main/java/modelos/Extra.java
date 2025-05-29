package modelos;

public class Extra {
    private int id;
    private String nombre;
    private double precio;
    private int cantidad;

    // Constructor completo
    public Extra(int id, String nombre, double precio, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    // Constructor sin ID (para cuando se crea desde presupuesto)
    public Extra(String nombre, double precio, int cantidad) {
        this.id = 0;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    // Constructor básico
    public Extra(String nombre, double precio) {
        this.id = 0;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = 0;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // Métodos útiles
    public double getSubtotal() {
        return precio * cantidad;
    }

    public boolean isSeleccionado() {
        return cantidad > 0;
    }

    @Override
    public String toString() {
        return nombre + " - $" + String.format("%.2f", precio) + 
               (cantidad > 0 ? " (x" + cantidad + ")" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Extra extra = (Extra) obj;
        return id == extra.id && nombre.equals(extra.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode() + (id * 31);
    }
}