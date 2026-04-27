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
    private String estado;    // Disponible, Reservado, Arrendado, Vendido
    private Asesor asesorResponsable;
    private CustomList<String> fotos;

    public Inmueble(String codigo, String direccion, String ciudad, String zona, String tipo, String finalidad,
                    double precio, double area, double areaTerreno, int habitaciones, int banos, String estado) {
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
        this.estado = estado;
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
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFinalidad() { return finalidad; }
    public void setFinalidad(String finalidad) { this.finalidad = finalidad; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getAreaTerreno() { return areaTerreno; }
    public void setAreaTerreno(double areaTerreno) { this.areaTerreno = areaTerreno; }

    public int getHabitaciones() { return habitaciones; }
    public void setHabitaciones(int habitaciones) { this.habitaciones = habitaciones; }

    public int getBanos() { return banos; }
    public void setBanos(int banos) { this.banos = banos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        if (validarEstado(estado)) {
            this.estado = estado;
        }
    }

    public void cambiarEstado(String nuevoEstado) {
        setEstado(nuevoEstado);
    }

    private boolean validarEstado(String e) {
        String s = e.toLowerCase();
        return s.equals("disponible") || s.equals("reservado") || s.equals("arrendado") || s.equals("vendido");
    }

    public Asesor getAsesorResponsable() { return asesorResponsable; }
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
