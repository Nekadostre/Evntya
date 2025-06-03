package modelos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SesionTemporal {
    private static SesionTemporal instancia = null;
    
    // ========== DATOS DEL USUARIO LOGUEADO ========== 
    private int usuarioId;
    private String usuarioNombre;
    private String usuarioApellidos;
    private String usuarioLogin;
    private String usuarioRol;
    
    // ========== DATOS DEL CLIENTE ========== 
    private Cliente clienteSeleccionado;
    
    // ========== DATOS DEL PAQUETE ========== 
    private int paqueteId;
    private String paqueteNombre;
    private double paquetePrecio;
    
    // ========== DATOS DEL EVENTO ========== 
    private LocalDate fechaEvento;
    private String horarioEvento;
    
    // ========== DATOS DEL PRESUPUESTO ========== 
    private String horarioPresupuesto;
    private String plazosPresupuesto;
    private String formaPagoPresupuesto;
    private List<Extra> extrasSeleccionados = new ArrayList<>();
    
    // ========== NUEVOS CAMPOS PARA INFORMACIÓN COMPLETA DEL PRESUPUESTO ==========
    private String numeroPresupuesto;
    private LocalDate fechaPresupuesto;
    private LocalDate validoHasta;
    private String estadoPresupuesto;
    
    private SesionTemporal() {
        // Constructor privado para Singleton
    }
    
    public static SesionTemporal getInstancia() {
        if (instancia == null) {
            instancia = new SesionTemporal();
        }
        return instancia;
    }
    
    public boolean validarDatosCompletos() {
        boolean clienteOK = hayClienteSeleccionado();
        boolean paqueteOK = hayPaqueteSeleccionado();
        boolean horarioOK = horarioPresupuesto != null && !horarioPresupuesto.trim().isEmpty();
        
        System.out.println("=== VALIDACIÓN DE SESIÓN ===");
        System.out.println("Cliente OK: " + clienteOK + " (" + getClienteNombreCompleto() + ")");
        System.out.println("Paquete OK: " + paqueteOK + " (" + getPaqueteNombre() + ")");
        System.out.println("Horario OK: " + horarioOK + " (" + horarioPresupuesto + ")");
        System.out.println("============================");
        
        return clienteOK && paqueteOK && horarioOK;
    }
    
    // ========== MÉTODOS PARA USUARIO LOGUEADO ========== 
    
    public void setUsuarioLogueado(int id, String nombre, String apellidos, String login, String rol) {
        this.usuarioId = id;
        this.usuarioNombre = nombre;
        this.usuarioApellidos = apellidos;
        this.usuarioLogin = login;
        this.usuarioRol = rol;
        
        System.out.println("✅ Usuario guardado en sesión: " + getNombreCompletoUsuario());
    }
    
    public String getNombreCompletoUsuario() {
        if (usuarioNombre != null && usuarioApellidos != null) {
            return usuarioNombre + " " + usuarioApellidos;
        }
        return "Usuario no identificado";
    }
    
    public String getUsuarioNombre() {
        return usuarioNombre;
    }
    
    public String getUsuarioApellidos() {
        return usuarioApellidos;
    }
    
    public String getUsuarioLogin() {
        return usuarioLogin;
    }
    
    public String getUsuarioRol() {
        return usuarioRol;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public boolean hayUsuarioLogueado() {
        return usuarioId > 0 && usuarioNombre != null;
    }
    
    // ========== MÉTODOS PARA CLIENTE ========== 
    
    public void setCliente(Cliente cliente) {
        this.clienteSeleccionado = cliente;
        System.out.println("✅ Cliente guardado en sesión: " + cliente.getNombreCompleto());
    }
    
    public Cliente getCliente() {
        return clienteSeleccionado;
    }
    
    public boolean hayClienteSeleccionado() {
        return clienteSeleccionado != null;
    }
    
    public String getClienteNombreCompleto() {
        return hayClienteSeleccionado() ? clienteSeleccionado.getNombreCompleto() : "Sin cliente";
    }
    
    public String getClienteRfc() {
        return hayClienteSeleccionado() ? clienteSeleccionado.getRfc() : "Sin RFC";
    }
    
    public String getClienteTelefono() {
        return hayClienteSeleccionado() ? clienteSeleccionado.getTelefono() : "Sin teléfono";
    }
    
    public String getClienteEmail() {
        try {
            return hayClienteSeleccionado() ? clienteSeleccionado.getEmail() : "Sin email";
        } catch (Exception e) {
            return "Sin email";
        }
    }
    
    public int getClienteId() {
        return hayClienteSeleccionado() ? clienteSeleccionado.getId() : 0;
    }
    
    // ========== MÉTODOS PARA PAQUETE ========== 
    
    public void setPaquete(int id, String nombre, double precio) {
        this.paqueteId = id;
        this.paqueteNombre = nombre;
        this.paquetePrecio = precio;
        System.out.println("✅ Paquete guardado en sesión: " + nombre + " ($" + precio + ")");
    }
    
    public boolean hayPaqueteSeleccionado() {
        return paqueteId > 0 && paqueteNombre != null;
    }
    
    public String getPaqueteNombre() {
        return paqueteNombre;
    }
    
    public double getPaquetePrecio() {
        return paquetePrecio;
    }
    
    public int getPaqueteId() {
        return paqueteId;
    }
    
    // ========== MÉTODOS PARA EVENTO ========== 
    
    public void setFechaEvento(LocalDate fecha) {
        this.fechaEvento = fecha;
        System.out.println("✅ Fecha de evento guardada: " + fecha);
    }
    
    public void setHorarioEvento(String horario) {
        this.horarioEvento = horario;
        System.out.println("✅ Horario de evento guardado: " + horario);
    }
    
    public LocalDate getFechaEvento() {
        return fechaEvento;
    }
    
    public String getHorarioEvento() {
        return horarioEvento;
    }
    
    // ========== MÉTODOS PARA PRESUPUESTO ========== 
    
    public void setHorarioPresupuesto(String horario) {
        this.horarioPresupuesto = horario;
    }
    
    public void setPlazosPresupuesto(String plazos) {
        this.plazosPresupuesto = plazos;
    }
    
    public void setFormaPagoPresupuesto(String formaPago) {
        this.formaPagoPresupuesto = formaPago;
    }
    
    public String getHorarioPresupuesto() {
        return horarioPresupuesto != null ? horarioPresupuesto : "No especificado";
    }
    
    public String getPlazosPresupuesto() {
        return plazosPresupuesto != null ? plazosPresupuesto : "No especificado";
    }
    
    public String getFormaPagoPresupuesto() {
        return formaPagoPresupuesto != null ? formaPagoPresupuesto : "No especificado";
    }
    
    // ========== NUEVOS MÉTODOS PARA INFORMACIÓN COMPLETA DEL PRESUPUESTO ==========
    
    public void setNumeroPresupuesto(String numeroPresupuesto) {
        this.numeroPresupuesto = numeroPresupuesto;
    }
    
    public String getNumeroPresupuesto() {
        return numeroPresupuesto != null ? numeroPresupuesto : "No asignado";
    }
    
    public void setFechaPresupuesto(LocalDate fechaPresupuesto) {
        this.fechaPresupuesto = fechaPresupuesto;
    }
    
    public LocalDate getFechaPresupuesto() {
        return fechaPresupuesto;
    }
    
    public void setValidoHasta(LocalDate validoHasta) {
        this.validoHasta = validoHasta;
    }
    
    public LocalDate getValidoHasta() {
        return validoHasta;
    }
    
    public void setEstadoPresupuesto(String estadoPresupuesto) {
        this.estadoPresupuesto = estadoPresupuesto;
    }
    
    public String getEstadoPresupuesto() {
        return estadoPresupuesto != null ? estadoPresupuesto : "Pendiente";
    }
    
    public boolean isPresupuestoVencido() {
        if (validoHasta == null) return false;
        return LocalDate.now().isAfter(validoHasta);
    }
    
    public long getDiasRestantesValidez() {
        if (validoHasta == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), validoHasta);
    }
    
    // ========== MÉTODOS PARA EXTRAS ========== 
    
    public void setExtrasSeleccionados(List<Extra> extras) {
        this.extrasSeleccionados = extras != null ? extras : new ArrayList<>();
        System.out.println("✅ Extras guardados en sesión: " + this.extrasSeleccionados.size() + " items");
    }
    
    public List<Extra> getExtrasSeleccionados() {
        return extrasSeleccionados;
    }
    
    public boolean tieneExtras() {
        return extrasSeleccionados != null && !extrasSeleccionados.isEmpty() && 
               extrasSeleccionados.stream().anyMatch(extra -> extra.getCantidad() > 0);
    }
    
    public String getResumenExtras() {
        if (!tieneExtras()) {
            return "Sin extras seleccionados";
        }
        
        StringBuilder resumen = new StringBuilder();
        boolean hayExtras = false;
        
        for (Extra extra : extrasSeleccionados) {
            if (extra.getCantidad() > 0) {
                if (hayExtras) resumen.append("; ");
                resumen.append(extra.getNombre())
                       .append(" x").append(extra.getCantidad())
                       .append(" ($").append(String.format("%.2f", extra.getPrecio() * extra.getCantidad()))
                       .append(")");
                hayExtras = true;
            }
        }
        
        return hayExtras ? resumen.toString() : "Sin extras seleccionados";
    }
    
    public double getTotalExtras() {
        if (extrasSeleccionados == null) return 0.0;
        
        return extrasSeleccionados.stream()
                .filter(extra -> extra != null && extra.getCantidad() > 0)
                .mapToDouble(extra -> extra.getPrecio() * extra.getCantidad())
                .sum();
    }
    
    public double getTotalGeneral() {
        return paquetePrecio + getTotalExtras();
    }
    
    // ========== MÉTODOS DE UTILIDAD ========== 
    
    public void reset() {
        // NO limpiar datos del usuario logueado
        // Solo limpiar datos del proceso de contrato/presupuesto
        
        this.clienteSeleccionado = null;
        this.paqueteId = 0;
        this.paqueteNombre = null;
        this.paquetePrecio = 0;
        this.fechaEvento = null;
        this.horarioEvento = null;
        this.horarioPresupuesto = null;
        this.plazosPresupuesto = null;
        this.formaPagoPresupuesto = null;
        this.extrasSeleccionados.clear();
        
        // Limpiar nuevos campos
        this.numeroPresupuesto = null;
        this.fechaPresupuesto = null;
        this.validoHasta = null;
        this.estadoPresupuesto = null;
        
        System.out.println("🔄 Sesión reiniciada (manteniendo usuario logueado)");
    }
    
    public void logout() {
        // Limpiar TODA la información, incluyendo el usuario
        this.usuarioId = 0;
        this.usuarioNombre = null;
        this.usuarioApellidos = null;
        this.usuarioLogin = null;
        this.usuarioRol = null;
        
        reset(); // También limpiar el resto
        
        System.out.println("🚪 Sesión cerrada completamente");
    }
    
    public void mostrarResumen() {
        System.out.println("========== RESUMEN DE SESIÓN ==========");
        System.out.println("Usuario: " + getNombreCompletoUsuario() + " (" + usuarioRol + ")");
        System.out.println("Cliente: " + getClienteNombreCompleto());
        System.out.println("Paquete: " + getPaqueteNombre() + " ($" + getPaquetePrecio() + ")");
        System.out.println("Fecha evento: " + getFechaEvento());
        System.out.println("Horario evento: " + getHorarioEvento());
        System.out.println("Número presupuesto: " + getNumeroPresupuesto());
        System.out.println("Estado presupuesto: " + getEstadoPresupuesto());
        System.out.println("Total extras: $" + getTotalExtras());
        System.out.println("Total general: $" + getTotalGeneral());
        System.out.println("=======================================");
    }
}