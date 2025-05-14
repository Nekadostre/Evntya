package modelos;

public class PresupuestoTemporal {
    private static PresupuestoTemporal instancia;
    private String horario;
    private String plazos;
    private String formaPago;

    private PresupuestoTemporal() {}

    public static PresupuestoTemporal getInstancia() {
        if (instancia == null) {
            instancia = new PresupuestoTemporal();
        }
        return instancia;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getPlazos() {
        return plazos;
    }

    public void setPlazos(String plazos) {
        this.plazos = plazos;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }
}
