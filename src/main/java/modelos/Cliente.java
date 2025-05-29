package modelos;

public class Cliente {

    private int id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String email;
    private String rfc;
    private String curp;

    // Constructor original (manteniendo compatibilidad)
    public Cliente(int id, String nombre, String apellidoPaterno, String apellidoMaterno, String rfc, String curp) {
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.rfc = rfc;
        this.curp = curp;
        this.telefono = "";
        this.email = "";
    }

    // Constructor con teléfono y email
    public Cliente(int id, String nombre, String apellidoPaterno, String apellidoMaterno, 
                   String rfc, String curp, String telefono, String email) {
        this(id, nombre, apellidoPaterno, apellidoMaterno, rfc, curp);
        this.telefono = telefono != null ? telefono : "";
        this.email = email != null ? email : "";
    }

    // Getters existentes
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getRfc() {
        return rfc;
    }

    public String getCurp() {
        return curp;
    }
    
    public String getTelefono() {
        return telefono != null ? telefono : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getNombreCompleto() {
        StringBuilder nombre = new StringBuilder();
        if (this.nombre != null) nombre.append(this.nombre);
        if (apellidoPaterno != null) nombre.append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            nombre.append(" ").append(apellidoMaterno);
        }
        return nombre.toString().trim();
    }

    // Setters
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }
    
    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }
    
    public void setRfc(String rfc) {
        this.rfc = rfc;
    }
    
    public void setCurp(String curp) {
        this.curp = curp;
    }
}