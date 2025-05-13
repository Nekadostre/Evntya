package modelos;

import javafx.beans.property.*;

public class Cliente {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty apellido = new SimpleStringProperty();
    private final StringProperty rfc = new SimpleStringProperty();

    public Cliente(int id, String nombre, String apellido, String rfc) {
        this.id.set(id);
        this.nombre.set(nombre);
        this.apellido.set(apellido);
        this.rfc.set(rfc);
    }

    public int getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getApellido() { return apellido.get(); }
    public String getRfc() { return rfc.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty apellidoProperty() { return apellido; }
    public StringProperty rfcProperty() { return rfc; }
}
