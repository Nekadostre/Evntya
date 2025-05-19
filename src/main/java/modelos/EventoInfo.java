package modelos;

public class EventoInfo {
    public ReservaDetalle manana;
    public ReservaDetalle tarde;

    public EventoInfo() {
        // Constructor vacío
    }

    public EventoInfo(ReservaDetalle manana, ReservaDetalle tarde) {
        this.manana = manana;
        this.tarde = tarde;
    }
}
