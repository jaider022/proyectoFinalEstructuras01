package models;

import structures.CustomList;

public class Cliente {
    private String identificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private String tipoCliente; // Comprador, Arrendatario
    private double presupuesto;
    private String zonasDeInteres;
    private String tipoInmuebleDeseado;
    
    // Aquí conectamos la estructura que programamos a mano
    private CustomList<Inmueble> favoritos; 
    private CustomList<Visita> historialVisitas;

    public Cliente(String identificacion, String nombre, String correo, String telefono, 
                   String tipoCliente, double presupuesto, String zonasDeInteres, String tipoInmuebleDeseado) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.tipoCliente = tipoCliente;
        this.presupuesto = presupuesto;
        this.zonasDeInteres = zonasDeInteres;
        this.tipoInmuebleDeseado = tipoInmuebleDeseado;
        this.favoritos = new CustomList<>();
        this.historialVisitas = new CustomList<>();
    }

    public CustomList<Inmueble> getFavoritos() { return favoritos; }
    public CustomList<Visita> getHistorialVisitas() { return historialVisitas; }

    // Getters y Setters
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    
    public double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(double presupuesto) { this.presupuesto = presupuesto; }
    
    public String getZonasDeInteres() { return zonasDeInteres; }
    public void setZonasDeInteres(String zonasDeInteres) { this.zonasDeInteres = zonasDeInteres; }
    
    public String getTipoInmuebleDeseado() { return tipoInmuebleDeseado; }
    public void setTipoInmuebleDeseado(String tipoInmuebleDeseado) { this.tipoInmuebleDeseado = tipoInmuebleDeseado; }

    public void agregarFavorito(Inmueble inmueble) {
        if (favoritos.indexOf(inmueble) == -1) {
            favoritos.add(inmueble);
        }
    }

    public void removerFavorito(Inmueble inmueble) {
        favoritos.remove(inmueble);
    }

    @Override
    public String toString() {
        return "Cliente: " + nombre + " (" + identificacion + ") - Presupuesto: $" + presupuesto;
    }
}
