package models;

public class Visita {
    private Cliente cliente;
    private Inmueble inmueble;
    private Asesor asesorAsignado;
    private String fecha;
    private String hora;
    private String estado; // Pendiente, Confirmada, Realizada, Cancelada
    private String observaciones;

    public Visita(Cliente cliente, Inmueble inmueble, Asesor asesor, String fecha, String hora) {
        this.cliente = cliente;
        this.inmueble = inmueble;
        this.asesorAsignado = asesor;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = "Pendiente";
        this.observaciones = "";
    }

    public Cliente getCliente() { return cliente; }
    public Inmueble getInmueble() { return inmueble; }
    public Asesor getAsesorAsignado() { return asesorAsignado; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }

    public void setEstado(String estado) {
        if (validarEstado(estado)) {
            this.estado = estado;
        }
    }

    public void cambiarEstado(String nuevoEstado) {
        setEstado(nuevoEstado);
    }

    private boolean validarEstado(String e) {
        if (e == null) return false;
        String s = e.toLowerCase();
        return s.equals("pendiente") || s.equals("confirmada") || s.equals("realizada") || 
               s.equals("cancelada") || s.equals("reprogramada");
    }

    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "Visita: " + fecha + " a las " + hora + " | Inmueble " + inmueble.getCodigo() + " | Estado: " + estado;
    }
}
