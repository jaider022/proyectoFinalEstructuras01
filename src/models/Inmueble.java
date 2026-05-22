package models;

import structures.CustomList;

public class Inmueble {
    private String codigo;
    private String direccion;
    private String ciudad;
    private String zona;
    private String tipo;      // apartamento, casa, local, etc.
    private String finalidad; // venta o arriendo
    private double precio;
    private double area;
    private double areaTerreno;
    private int habitaciones;
    private int banos;
    private String estadoFisico; // Nuevo, Usado, En Remodelación, Sobre Planos
    private String disponibilidad; // Disponible, Reservado, Arrendado, Vendido
    private String fechaUltimaVisita;
    private String fechaReserva;
    private int cambiosPrecioCount;
    private Asesor asesorResponsable;
    private CustomList<String> fotos;

    public Inmueble(String codigo, String direccion, String ciudad, String zona, String tipo, String finalidad,
                    double precio, double area, double areaTerreno, int habitaciones, int banos, String estadoFisico, String disponibilidad) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.zona = zona;
        this.tipo = tipo;
        this.finalidad = finalidad;
        this.precio = precio;
        this.area = area;
        this.areaTerreno = areaTerreno;
        this.habitaciones = habitaciones;
        this.banos = banos;
        this.estadoFisico = estadoFisico;
        this.disponibilidad = disponibilidad;
        this.asesorResponsable = null;
        this.fotos = new CustomList<>();
    }

    // Getters y Setters
    public CustomList<String> getFotos() { return fotos; }
    public void setFotos(CustomList<String> fotos) { this.fotos = fotos; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { 
        if (validarTipo(tipo)) {
            this.tipo = tipo; 
        } else {
            this.tipo = "Otro"; // Fallback seguro
        }
    }

    public String getFinalidad() { return finalidad; }
    public void setFinalidad(String finalidad) { 
        if (validarFinalidad(finalidad)) {
            this.finalidad = finalidad; 
        }
    }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { 
        if (this.precio != 0 && this.precio != precio) {
            this.cambiosPrecioCount++;
        }
        this.precio = precio; 
    }

    public int getCambiosPrecioCount() { return cambiosPrecioCount; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getAreaTerreno() { return areaTerreno; }
    public void setAreaTerreno(double areaTerreno) { this.areaTerreno = areaTerreno; }

    public int getHabitaciones() { return habitaciones; }
    public void setHabitaciones(int habitaciones) { this.habitaciones = habitaciones; }

    public int getBanos() { return banos; }
    public void setBanos(int banos) { this.banos = banos; }

    public String getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(String estadoFisico) { 
        if (validarEstadoFisico(estadoFisico)) {
            this.estadoFisico = estadoFisico; 
        } else {
            this.estadoFisico = "Usado"; // Fallback
        }
    }

    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) {
        if (validarDisponibilidad(disponibilidad)) {
            this.disponibilidad = disponibilidad;
        }
    }

    public void cambiarDisponibilidad(String nuevaDisponibilidad) {
        setDisponibilidad(nuevaDisponibilidad);
    }

    private boolean validarDisponibilidad(String d) {
        if (d == null) return false;
        String s = d.toLowerCase();
        return s.equals("disponible") || s.equals("reservado") || s.equals("arrendado") || s.equals("vendido");
    }

    private boolean validarTipo(String t) {
        if (t == null) return false;
        String s = t.toLowerCase();
        return s.equals("apartamento") || s.equals("casa") || s.equals("local comercial") || 
               s.equals("oficina") || s.equals("lote") || s.equals("bodega") || s.equals("apto");
    }

    private boolean validarFinalidad(String f) {
        if (f == null) return false;
        String s = f.toLowerCase();
        return s.equals("venta") || s.equals("arriendo");
    }

    private boolean validarEstadoFisico(String e) {
        if (e == null) return false;
        String s = e.toLowerCase();
        return s.equals("nuevo") || s.equals("usado") || s.equals("en remodelación") || 
               s.equals("sobre planos") || s.equals("remodelado");
    }

    private boolean parqueadero = true; // Default true so we don't break existing properties

    public boolean tieneParqueadero() {
        return parqueadero;
    }

    public void setParqueadero(boolean parqueadero) {
        this.parqueadero = parqueadero;
    }

    public Asesor getAsesorResponsable() { return asesorResponsable; }
    
    public String getFechaUltimaVisita() { return fechaUltimaVisita; }
    public void setFechaUltimaVisita(String f) { this.fechaUltimaVisita = f; }

    public String getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(String f) { this.fechaReserva = f; }

    public void setAsesorResponsable(Asesor asesor) {
        // Desvincular del anterior
        if (this.asesorResponsable != null) {
            this.asesorResponsable.removeInmueble(this);
        }
        
        this.asesorResponsable = asesor;
        
        // Vincular al nuevo
        if (this.asesorResponsable != null) {
            this.asesorResponsable.addInmueble(this);
        }
    }

    @Override
    public String toString() {
        return "Inmueble [" + codigo + "] " + tipo + " en " + finalidad + " - $" + precio;
    }
}
