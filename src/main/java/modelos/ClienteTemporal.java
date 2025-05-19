package modelos;

public class ClienteTemporal {
    private static ClienteTemporal instancia;

    private int id;
    private String nombre;
    private String apellido;
    private String fechaEvento;
    private String horario;
    private String curp;

    private ClienteTemporal() {
        this.nombre = "";
        this.apellido = "";
        this.fechaEvento = "";
        this.horario = "";
        this.id = 0;
    }

    public static ClienteTemporal getInstancia() {
        if (instancia == null) {
            instancia = new ClienteTemporal();
        }
        return instancia;
    }

        public void setDatos(int id, String nombre, String apellido, String curp) {
         this.id = id;
         this.nombre = nombre;
         this.apellido = apellido;
         this.curp = curp;
     }

    public void setFechaEvento(String fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getFechaEvento() {
        return fechaEvento;
    }

    public String getHorario() {
        return horario;
    }
    
    public String getCurp() {
    return curp;
}

    public void reset() {
        this.id = 0;
        this.nombre = "";
        this.apellido = "";
        this.fechaEvento = "";
        this.horario = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setCurp(String curp) {
    this.curp = curp;
}
    
}
