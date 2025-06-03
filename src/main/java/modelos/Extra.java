// ========== VERIFICA QUE TU CLASE Extra.java TENGA ESTOS CONSTRUCTORES ==========

package modelos;

public class Extra {
    private int id;
    private String nombre;
    private double precio;
    private int cantidad;
    
    // Constructor CON ID (necesario para cargar desde BD)
    public Extra(int id, String nombre, double precio, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }
    
    // Constructor SIN ID (para crear nuevos extras)
    public Extra(String nombre, double precio, int cantidad) {
        this.id = 0; // Sin ID
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }
    
    // Getters y setters
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
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public double getSubtotal() {
        return precio * cantidad;
    }
    
    @Override
    public String toString() {
        return String.format("%s x%d - $%.2f", nombre, cantidad, getSubtotal());
    }
}