package models;

public class Operacion {
    private String idOperacion;
    private Inmueble inmueble;
    private Cliente cliente;
    private Asesor asesor;
    private String fecha;
    private String tipoOperacion; // Venta, Arriendo, Renovacion
    private double valorAcordado;
    private double comision;
    private String estadoProceso;

    public Operacion(String idOperacion, Inmueble inmueble, Cliente cliente, Asesor asesor, 
                     String fecha, String tipoOperacion, double valorAcordado, double comision) {
        this.idOperacion = idOperacion;
        this.inmueble = inmueble;
        this.cliente = cliente;
        this.asesor = asesor;
        this.fecha = fecha;
        this.tipoOperacion = tipoOperacion;
        this.valorAcordado = valorAcordado;
        this.comision = comision;
        this.estadoProceso = "Procesando";
    }

    public String getIdOperacion() { return idOperacion; }
    public Inmueble getInmueble() { return inmueble; }
    public Cliente getCliente() { return cliente; }
    public Asesor getAsesor() { return asesor; }
    public String getFecha() { return fecha; }
    public String getTipoOperacion() { return tipoOperacion; }
    public double getValorAcordado() { return valorAcordado; }
    public double getComision() { return comision; }
    public String getEstadoProceso() { return estadoProceso; }

    public void setEstadoProceso(String estadoProceso) { this.estadoProceso = estadoProceso; }

    @Override
    public String toString() {
        return "Operacion [" + idOperacion + "] - " + tipoOperacion + " | Valor: $" + valorAcordado;
    }
}
