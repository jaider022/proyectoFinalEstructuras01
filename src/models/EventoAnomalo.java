package models;

public class EventoAnomalo implements Comparable<EventoAnomalo> {
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

    @Override
    public int compareTo(EventoAnomalo otro) {
        return Integer.compare(getPrioridadValor(this.nivelAtencion), getPrioridadValor(otro.nivelAtencion));
    }

    private int getPrioridadValor(String nivel) {
        if (nivel == null) return 0;
        String n = nivel.toUpperCase();
        if (n.equals("HIGH") || n.equals("ALTO")) return 4;
        if (n.equals("MEDIUM-HIGH") || n.equals("MEDIO-ALTO")) return 3;
        if (n.equals("MEDIUM") || n.equals("MEDIO")) return 2;
        if (n.equals("LOW") || n.equals("BAJO")) return 1;
        return 0;
    }
}
