package ejercicio_consola;

public class Inmueble {
    // 1. Atributos solicitados
    private String id;
    private String tipo;
    private String ciudad;
    private double precio;
    private int numeroHabitaciones;
    private boolean parqueadero;
    private boolean estadoDisponible;

    public Inmueble(String id, String tipo, String ciudad, double precio, int numeroHabitaciones, boolean parqueadero, boolean estadoDisponible) {
        this.id = id;
        this.tipo = tipo;
        this.ciudad = ciudad;
        this.precio = precio;
        this.numeroHabitaciones = numeroHabitaciones;
        this.parqueadero = parqueadero;
        this.estadoDisponible = estadoDisponible;
    }

    // Getters para acceder a las propiedades
    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public String getCiudad() { return ciudad; }
    public double getPrecio() { return precio; }
    public int getNumeroHabitaciones() { return numeroHabitaciones; }
    public boolean tieneParqueadero() { return parqueadero; }
    public boolean isEstadoDisponible() { return estadoDisponible; }

    @Override
    public String toString() {
        return String.format("Inmueble [ID: %s] %s en %s | Habitaciones: %d | Parqueadero: %s | Precio: $%.2f",
                id, tipo, ciudad, numeroHabitaciones, (parqueadero ? "Sí" : "No"), precio);
    }
}
