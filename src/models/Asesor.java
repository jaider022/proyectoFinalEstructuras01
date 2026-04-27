package models;

import structures.CustomList;
import structures.CustomQueue;

public class Asesor {
    private String identificacion;
    private String nombre;
    private String contacto;
    private String especialidadZona;
    private int cierresRealizados;

    // Conectamos nuestros Árboles y Colas propias:
    private CustomList<Inmueble> inmueblesAsignados;
    private CustomQueue<Visita> visitasPendientes;

    public Asesor(String identificacion, String nombre, String contacto, String especialidadZona) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.contacto = contacto;
        this.especialidadZona = especialidadZona;
        this.cierresRealizados = 0;
        this.inmueblesAsignados = new CustomList<>();
        this.visitasPendientes = new CustomQueue<>();
    }

    public CustomList<Inmueble> getInmueblesAsignados() { return inmueblesAsignados; }
    public CustomQueue<Visita> getVisitasPendientes() { return visitasPendientes; }


    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getEspecialidadZona() { return especialidadZona; }
    public void setEspecialidadZona(String especialidadZona) { this.especialidadZona = especialidadZona; }

    public int getCierresRealizados() { return cierresRealizados; }
    public void registrarCierre() { this.cierresRealizados++; }

    public void addInmueble(Inmueble inmueble) {
        if (inmueblesAsignados.indexOf(inmueble) == -1) {
            inmueblesAsignados.add(inmueble);
        }
    }

    public void removeInmueble(Inmueble inmueble) {
        inmueblesAsignados.remove(inmueble);
    }

    @Override
    public String toString() {
        return "Asesor: " + nombre + " [Zona: " + especialidadZona + "]";
    }
}
