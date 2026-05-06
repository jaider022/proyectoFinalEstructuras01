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
    private String fechaVencimiento;
    public Operacion(String idOperacion, Inmueble inmueble, Cliente cliente, Asesor asesor, 
                     String fecha, String tipoOperacion, double valorAcordado, double comision) {
        this.idOperacion = idOperacion;
        this.inmueble = inmueble;
        this.cliente = cliente;
        this.asesor = asesor;
        this.fecha = fecha;
        setTipoOperacion(tipoOperacion);
        this.valorAcordado = valorAcordado;
        this.comision = comision;
        this.estadoProceso = "Iniciado";
    }

    public String getIdOperacion() { return idOperacion; }
    public Inmueble getInmueble() { return inmueble; }
    public Cliente getCliente() { return cliente; }
    public Asesor getAsesor() { return asesor; }
    public String getFecha() { return fecha; }
    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { 
        if (validarTipo(tipoOperacion)) {
            this.tipoOperacion = tipoOperacion;
        } else {
            this.tipoOperacion = "Otros";
        }
    }

    public double getValorAcordado() { return valorAcordado; }
    public void setValorAcordado(double valor) { this.valorAcordado = valor; }

    public double getComision() { return comision; }
    public void setComision(double comision) { this.comision = comision; }

    public String getEstadoProceso() { return estadoProceso; }

    public void setEstadoProceso(String estadoProceso) { 
        if (validarEstado(estadoProceso)) {
            this.estadoProceso = estadoProceso;
        }
    }

    private boolean validarTipo(String t) {
        if (t == null) return false;
        String s = t.toLowerCase();
        return s.equals("arriendo") || s.equals("venta") || 
               s.contains("renovación") || s.contains("renovacion") ||
               s.contains("cancelación") || s.contains("cancelacion");
    }

    private boolean validarEstado(String e) {
        if (e == null) return false;
        String s = e.toLowerCase();
        return s.equals("iniciado") || s.equals("en trámite") || s.equals("finalizada") || 
               s.equals("fallida") || s.equals("firmada") || s.equals("procesando");
    }
    
    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    @Override
    public String toString() {
        return "Operacion [" + idOperacion + "] - " + tipoOperacion + " | Valor: $" + valorAcordado;
    }
}
