package models;

import structures.CustomList;
import structures.CustomStack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Cliente {
    private String identificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private String tipoCliente; // Comprador, Arrendatario
    private double presupuesto;
    private String zonasDeInteres;
    private String tipoInmuebleDeseado;
    private int minHabitaciones;
    private String estadoBusqueda; // Activo, Inactivo, Exitoso
    private String fechaUltimoSeguimiento;
    
    // Aquí conectamos la estructura que programamos a mano
    private CustomList<Inmueble> favoritos; 
    private CustomList<Inmueble> intenciones;
    private CustomList<Inmueble> consultados;
    private CustomList<Inmueble> descartados;
    private CustomList<Inmueble> negociados;
    private CustomList<Visita> historialVisitas;
    private CustomStack<String> historialInteracciones;

    public Cliente(String identificacion, String nombre, String correo, String telefono, 
                   String tipoCliente, double presupuesto, String zonasDeInteres, String tipoInmuebleDeseado,
                   int minHabitaciones, String estadoBusqueda) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.tipoCliente = tipoCliente;
        this.presupuesto = presupuesto;
        this.zonasDeInteres = zonasDeInteres;
        this.tipoInmuebleDeseado = tipoInmuebleDeseado;
        this.minHabitaciones = minHabitaciones;
        setEstadoBusqueda(estadoBusqueda);
        this.favoritos = new CustomList<>();
        this.intenciones = new CustomList<>();
        this.consultados = new CustomList<>();
        this.descartados = new CustomList<>();
        this.negociados = new CustomList<>();
        this.historialVisitas = new CustomList<>();
        this.historialInteracciones = new CustomStack<>();
    }

    public CustomList<Inmueble> getFavoritos() { return favoritos; }
    public CustomList<Inmueble> getIntenciones() { return intenciones; }
    public CustomList<Inmueble> getConsultados() { return consultados; }
    public CustomList<Inmueble> getDescartados() { return descartados; }
    public CustomList<Inmueble> getNegociados() { return negociados; }
    public CustomList<Visita> getHistorialVisitas() { return historialVisitas; }
    public CustomStack<String> getHistorialInteracciones() { return historialInteracciones; }

    public void registrarInteraccion(String accion) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.historialInteracciones.push(timestamp + " - " + accion);
    }

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

    public int getMinHabitaciones() { return minHabitaciones; }
    public void setMinHabitaciones(int minHabitaciones) { this.minHabitaciones = minHabitaciones; }

    public String getEstadoBusqueda() { return estadoBusqueda; }
    public void setEstadoBusqueda(String estadoBusqueda) { 
        if (validarEstadoBusqueda(estadoBusqueda)) {
            this.estadoBusqueda = estadoBusqueda;
        } else {
            this.estadoBusqueda = "Activo"; // Fallback
        }
    }

    public String getFechaUltimoSeguimiento() { return fechaUltimoSeguimiento; }
    public void setFechaUltimoSeguimiento(String f) { this.fechaUltimoSeguimiento = f; }

    private boolean validarEstadoBusqueda(String e) {
        if (e == null) return false;
        String s = e.toLowerCase();
        return s.equals("activo") || s.equals("inactivo") || s.equals("exitoso");
    }

    public void agregarFavorito(Inmueble inmueble) {
        if (favoritos.indexOf(inmueble) == -1) {
            favoritos.add(inmueble);
        }
    }

    public void removerFavorito(Inmueble inmueble) {
        favoritos.remove(inmueble);
    }

    public void registrarIntencion(Inmueble inmueble) {
        if (intenciones.indexOf(inmueble) == -1) {
            intenciones.add(inmueble);
        }
    }

    public void agregarConsultado(Inmueble i) {
        if (consultados.indexOf(i) == -1) consultados.add(i);
    }

    public void agregarDescartado(Inmueble i) {
        if (descartados.indexOf(i) == -1) descartados.add(i);
    }

    public void agregarNegociado(Inmueble i) {
        if (negociados.indexOf(i) == -1) negociados.add(i);
    }

    public boolean prefiere(Inmueble inm) {
        // 1. Presupuesto (precio <= presupuesto del cliente, o presupuesto <= 0 para ignorar el límite)
        boolean cumplePresupuesto = this.presupuesto <= 0 || inm.getPrecio() <= this.presupuesto;
        
        // 2. Zona (ignorar si es nulo, vacío, "cualquiera" o "todos")
        boolean cumpleZona = this.zonasDeInteres == null || 
                             this.zonasDeInteres.trim().isEmpty() || 
                             this.zonasDeInteres.equalsIgnoreCase("cualquiera") || 
                             this.zonasDeInteres.equalsIgnoreCase("todos") ||
                             this.zonasDeInteres.toLowerCase().contains(inm.getZona().toLowerCase());
        
        // 3. Tipo (ignorar si es nulo, vacío, "cualquiera" o "todos")
        boolean cumpleTipo = this.tipoInmuebleDeseado == null || 
                             this.tipoInmuebleDeseado.trim().isEmpty() || 
                             this.tipoInmuebleDeseado.equalsIgnoreCase("cualquiera") || 
                             this.tipoInmuebleDeseado.equalsIgnoreCase("todos") ||
                             this.tipoInmuebleDeseado.equalsIgnoreCase(inm.getTipo());
                             
        // 4. Disponibilidad (solo inmuebles que estén 'Disponible')
        boolean estaDisponible = "Disponible".equalsIgnoreCase(inm.getDisponibilidad());
        
        // 5. Habitaciones (mayor o igual al mínimo del cliente, o <= 0 para ignorar)
        boolean cumpleHabitaciones = this.minHabitaciones <= 0 || inm.getHabitaciones() >= this.minHabitaciones;

        return cumplePresupuesto && cumpleZona && cumpleTipo && estaDisponible && cumpleHabitaciones;
    }

    @Override
    public String toString() {
        return "Cliente: " + nombre + " (" + identificacion + ") - Presupuesto: $" + presupuesto + " - Búsqueda: " + estadoBusqueda;
    }
}
