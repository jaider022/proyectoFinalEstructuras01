package models;

public class EventoAnomalo {
    private String tipo;
    private String descripcion;
    private String nivelAtencion; // Bajo, Medio, Alto
    private String fechaDeteccion;
    private String entidadId;

    public EventoAnomalo(String tipo, String descripcion, String nivelAtencion, String fechaDeteccion, String entidadId) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.nivelAtencion = nivelAtencion;
        this.fechaDeteccion = fechaDeteccion;
        this.entidadId = entidadId;
    }

    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public String getNivelAtencion() { return nivelAtencion; }
    public String getFechaDeteccion() { return fechaDeteccion; }
    public String getEntidadId() { return entidadId; }

    @Override
    public String toString() {
        return "[" + nivelAtencion + "] " + tipo + ": " + descripcion + " (Entidad: " + entidadId + ")";
    }
}
