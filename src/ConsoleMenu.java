import controllers.PropTechSystem;
import controllers.UnusualBehaviorDetectionModule;
import models.*;
import structures.*;
import java.util.Scanner;

public class ConsoleMenu {
    public static void main(String[] args) {
        PropTechSystem sistema = new PropTechSystem();
        Scanner scanner = new Scanner(System.in);
        UnusualBehaviorDetectionModule auditoria = new UnusualBehaviorDetectionModule(sistema);

        System.out.println("==================================================");
        System.out.println("PROPTECH SYSTEM - UNUSUAL BEHAVIOR DETECTION MODULE");
        System.out.println("==================================================");

        boolean salir = false;
        while (!salir) {
            System.out.println("\nMENU PRINCIPAL");
            System.out.println("1. Cargar datos de prueba (Simular todos los Casos Inusuales)");
            System.out.println("2. Ejecutar Detección de Comportamiento Inusual");
            System.out.println("3. Ver Alertas por Prioridad");
            System.out.println("4. Consultar Reportes Gerenciales (Zona, Precio, Visitas, Cierres)");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");
            
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    cargarDatosPrueba(sistema);
                    System.out.println("\n✅ Datos de prueba cargados correctamente para forzar los 7 casos.");
                    break;
                case "2":
                    System.out.println("\n🔍 Ejecutando módulo de detección...");
                    auditoria.runDetection();
                    System.out.println("✅ Análisis completado.");
                    break;
                case "3":
                    System.out.println("\n🚨 ALERTAS GENERADAS (Ordenadas por Prioridad):");
                    CustomPriorityQueue<EventoAnomalo> alertas = auditoria.getAlertasPrioritarias();
                    if (alertas.isEmpty()) {
                        System.out.println("No hay alertas actualmente.");
                    } else {
                        // Dequeue elements to show priority order
                        while (!alertas.isEmpty()) {
                            System.out.println(alertas.dequeue().toString());
                        }
                    }
                    break;
                case "4":
                    System.out.println("\n📊 GENERANDO REPORTES GERENCIALES...");
                    // 1. Visitas y Cierres por Zona
                    sistema.analizarActividadPorZonas();
                    
                    // 2. Reporte por Precio (Árbol BST)
                    System.out.println("\n=== CATÁLOGO ORDENADO POR PRECIO (USANDO ÁRBOL BINARIO) ===");
                    CustomList<Inmueble> ordenados = sistema.obtenerCatalogoLista();
                    if(ordenados.getSize() == 0) System.out.println("No hay inmuebles registrados.");
                    for(int i=0; i<ordenados.getSize(); i++){
                        System.out.println(" > [$" + ordenados.get(i).getPrecio() + "] " + ordenados.get(i).getCodigo() + " - " + ordenados.get(i).getTipo() + " en " + ordenados.get(i).getZona());
                    }

                    // 3. Reporte de Visitas Totales
                    System.out.println("\n=== REPORTE DE VISITAS (TABLA HASH) ===");
                    CustomHashTable<String, Integer> visitas = sistema.getVisitasPorInmueble();
                    CustomList<String> llavesInmuebles = visitas.keys();
                    if(llavesInmuebles.getSize() == 0) System.out.println("No hay visitas registradas.");
                    for(int i=0; i<llavesInmuebles.getSize(); i++){
                        String cod = llavesInmuebles.get(i);
                        System.out.println(" > Inmueble " + cod + " tiene " + visitas.get(cod) + " visitas registradas.");
                    }
                    System.out.println("==================================================");
                    break;
                case "5":
                    salir = true;
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
        scanner.close();
    }

    private static void cargarDatosPrueba(PropTechSystem sistema) {
        // Asesores
        Asesor a1 = new Asesor("A-1", "Carlos López", "111", "Centro");
        Asesor a2 = new Asesor("A-2", "Ana Martínez", "222", "Norte");
        sistema.registrarAsesor(a1);
        sistema.registrarAsesor(a2);

        // Clientes
        Cliente c1 = new Cliente("C-1", "Juan Pérez", "juan@correo", "123", "Comprador", 500000.0, "Centro", "Casa", 3, "Activo");
        Cliente c2 = new Cliente("C-2", "María Gómez", "maria@correo", "321", "Comprador", 300000.0, "Norte", "Apto", 2, "Activo");
        sistema.registrarCliente(c1);
        sistema.registrarCliente(c2);

        // Inmuebles
        Inmueble inm1 = new Inmueble("APT-101", "Calle 10", "Bogotá", "Centro", "Apartamento", "Venta", 250000.0, 60, 0, 2, 1, "Usado", "Disponible");
        Inmueble inm2 = new Inmueble("CAS-505", "Cra 5", "Bogotá", "Centro", "Casa", "Venta", 400000.0, 100, 0, 3, 2, "Usado", "Disponible");
        Inmueble inm3 = new Inmueble("COM-089", "Av 19", "Bogotá", "Norte", "Local", "Venta", 800000.0, 150, 0, 0, 2, "Nuevo", "Reservado"); // Caso 7
        sistema.registrarInmueble(inm1);
        sistema.registrarInmueble(inm2);
        sistema.registrarInmueble(inm3);

        // Simular Caso 1: APT-101 con >20 visitas sin cierre
        for (int i = 0; i < 25; i++) {
            sistema.getVisitasPorInmueble().put("APT-101", 25);
        }

        // Simular Caso 2 & 6: Clientes con muchas visitas agendadas
        for (int i = 0; i < 12; i++) {
            c1.getHistorialVisitas().add(new Visita(c1, inm1, a1, "2026-05-25", "10:00"));
        }
        for (int i = 0; i < 8; i++) {
            c2.getHistorialVisitas().add(new Visita(c2, inm2, a2, "2026-05-25", "11:00"));
        }

        // Simular Caso 3: Asesor sobrecargado
        Asesor a3 = new Asesor("A-3", "Pedro", "333", "Sur");
        Asesor a4 = new Asesor("A-4", "Lucas", "444", "Occidente");
        Asesor a5 = new Asesor("A-5", "Sara", "555", "Oriente");
        sistema.registrarAsesor(a3);
        sistema.registrarAsesor(a4);
        sistema.registrarAsesor(a5);
        for (int i = 0; i < 45; i++) {
            a1.getVisitasPendientes().enqueue(new Visita(c1, inm1, a1, "2026-05-25", "10:00"));
        }
        for (int i = 0; i < 5; i++) {
            a2.getVisitasPendientes().enqueue(new Visita(c2, inm2, a2, "2026-05-25", "11:00"));
        }

        // Simular Caso 4: Cambios de precio frecuentes en CAS-505
        for (int i = 0; i < 6; i++) {
            inm2.setPrecio(inm2.getPrecio() + 100);
        }

        // Simular Caso 5: Concentración anormal en zona Chapinero
        sistema.getVisitasPorZona().put("Chapinero", 34);
        sistema.getVisitasPorZona().put("Norte", 2);
        sistema.getVisitasPorZona().put("Sur", 3);
        sistema.getVisitasPorZona().put("Occidente", 1);
        sistema.getVisitasPorZona().put("Oriente", 0);
        sistema.getVisitasPorZona().put("Suba", 1);
        sistema.getVisitasPorZona().put("Bosa", 0);
    }
}
