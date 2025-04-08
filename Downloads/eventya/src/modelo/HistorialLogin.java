package modelo;

public class HistorialLogin {
    private String usuarioId;
    private String fecha;

    public HistorialLogin(String usuarioId, String fecha) {
        this.usuarioId = usuarioId;
        this.fecha = fecha;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getFecha() {
        return fecha;
    }
}
