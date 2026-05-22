package ejercicio_consola;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 2. Crear varios inmuebles de prueba almacenados en un ArrayList.
        ArrayList<Inmueble> inventario = new ArrayList<>();

        inventario.add(new Inmueble("INM-001", "Apartamento", "Bogotá", 300000.0, 3, true, true));
        inventario.add(new Inmueble("INM-002", "Casa", "Medellín", 450000.0, 4, true, true));
        inventario.add(new Inmueble("INM-003", "Apartamento", "Bogotá", 250000.0, 2, false, true));
        // Inmueble no disponible
        inventario.add(new Inmueble("INM-004", "Apartamento", "Cali", 280000.0, 3, true, false));
        inventario.add(new Inmueble("INM-005", "Apartamento", "Bogotá", 350000.0, 3, true, true));
        inventario.add(new Inmueble("INM-006", "Casa", "Bogotá", 500000.0, 5, true, true));

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== SISTEMA DE RECOMENDACIÓN DE INMUEBLES ===");

        // 3. Permitir que el cliente ingrese preferencias
        System.out.print("Ingrese el tipo de inmueble deseado (ej. Apartamento, Casa): ");
        String tipoDeseado = scanner.nextLine();

        System.out.print("Ingrese la ciudad: ");
        String ciudadDeseada = scanner.nextLine();

        System.out.print("Ingrese su precio máximo: ");
        double precioMax = scanner.nextDouble();

        System.out.print("Ingrese el número mínimo de habitaciones: ");
        int minHabitaciones = scanner.nextInt();

        System.out.print("¿Requiere parqueadero? (true/false): ");
        boolean requiereParqueadero = scanner.nextBoolean();

        System.out.println("\nBuscando inmuebles que coincidan con sus preferencias...\n");

        // Llamada al método encargado de filtrar y mostrar (Requisito 8)
        recomendarInmuebles(inventario, tipoDeseado, ciudadDeseada, precioMax, minHabitaciones, requiereParqueadero);

        scanner.close();
    }

    /**
     * 8. Método encargado de realizar el filtrado y mostrar las coincidencias.
     * Separa la responsabilidad de búsqueda y presentación (Requisito 7).
     */
    public static void recomendarInmuebles(ArrayList<Inmueble> inventario, String tipo, String ciudad,
            double precioMax, int minHabitaciones, boolean requiereParqueadero) {

        ArrayList<Inmueble> recomendaciones = new ArrayList<>();

        // 4. Recorrer la lista y comparar cada inmueble con las preferencias (Ciclo for
        // y condicionales if)
        for (Inmueble inm : inventario) {
            // Solo considerar inmuebles que estén disponibles
            if (!inm.isEstadoDisponible()) {
                continue;
            }

            boolean cumpleTipo = inm.getTipo().equalsIgnoreCase(tipo);
            boolean cumpleCiudad = inm.getCiudad().equalsIgnoreCase(ciudad);
            boolean cumplePrecio = inm.getPrecio() <= precioMax;
            boolean cumpleHabitaciones = inm.getNumeroHabitaciones() >= minHabitaciones;

            // Si requiere parqueadero, el inmueble debe tenerlo. Si no, es indiferente.
            boolean cumpleParqueadero = !requiereParqueadero || inm.tieneParqueadero();

            // Si cumple todas las condiciones, se añade a recomendaciones
            if (cumpleTipo && cumpleCiudad && cumplePrecio && cumpleHabitaciones && cumpleParqueadero) {
                recomendaciones.add(inm);
            }
        }

        // 6. Si no existen coincidencias, mostrar un mensaje indicando esto.
        if (recomendaciones.isEmpty()) {
            System.out.println("Lo sentimos, no se encontraron inmuebles compatibles con sus preferencias.");
        } else {
            // Extra: Agregar una opción para ordenar las recomendaciones por precio de
            // menor a mayor.
            Collections.sort(recomendaciones, new Comparator<Inmueble>() {
                @Override
                public int compare(Inmueble i1, Inmueble i2) {
                    return Double.compare(i1.getPrecio(), i2.getPrecio());
                }
            });

            // 5. Mostrar únicamente los inmuebles que cumplan las condiciones.
            System.out.println("=== INMUEBLES RECOMENDADOS ===");
            for (Inmueble recomendado : recomendaciones) {
                System.out.println(recomendado.toString());
            }
        }
    }
}
