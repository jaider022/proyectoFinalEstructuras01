import models.*;
import controllers.PropTechSystem;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== MOTOR PROPTECH: SIMULACIÓN DE ANALÍTICA Y RECOMENDACIONES ===\n");
        
        PropTechSystem sistema = new PropTechSystem();

        // 1. Registro de base
        Asesor maria = new Asesor("A-001", "Maria Lopez", "300123", "Norte");
        sistema.registrarAsesor(maria);

        Inmueble i1 = new Inmueble("INM-A", "Calle 100", "Bogotá", "Norte", "Apto", "Venta", 300000.0, 80, 2, 2, "D");
        Inmueble i2 = new Inmueble("INM-B", "Cra 7", "Bogotá", "Norte", "Casa", "Arriendo", 5000.0, 150, 4, 3, "D");
        Inmueble i3 = new Inmueble("INM-C", "Calle 80", "Bogotá", "Occidente", "Apto", "Venta", 250000.0, 70, 2, 1, "D");
        
        sistema.registrarInmueble(i1);
        sistema.registrarInmueble(i2);
        sistema.registrarInmueble(i3);

        Cliente c1 = new Cliente("C-1", "Carlos", "c@mail.com", "123", "Comprador", 400000.0, "Norte", "Apto");
        Cliente c2 = new Cliente("C-2", "Ana", "a@mail.com", "456", "Inversionista", 500000.0, "Norte", "Todo");
        
        sistema.registrarCliente(c1);
        sistema.registrarCliente(c2);

        // 2. Simulación de Intereses (Grafos)
        System.out.println("Simulando intereses compartidos para el motor de recomendaciones...");
        // Ana visita el Inmueble A y el B
        sistema.agendarVisita("C-2", "INM-A", "A-001", "2026-06-01", "10:00");
        sistema.agendarVisita("C-2", "INM-B", "A-001", "2026-06-01", "11:00");
        
        // Carlos visita el Inmueble A
        sistema.agendarVisita("C-1", "INM-A", "A-001", "2026-06-02", "09:00");

        // Resultado esperado: Como Carlos y Ana ambos visitaron INM-A, 
        // y Ana también visitó INM-B, el sistema debería recomendar INM-B a Carlos.
        sistema.mostrarRecomendaciones("C-1");

        // 3. Prueba de Prioridad (VIP)
        System.out.println("\nAgendando visita VIP (Prioridad 5) para inversor Ana...");
        sistema.agendarVisita("C-2", "INM-C", "A-001", "2026-06-05", "15:00", 5);

        // 4. Analítica
        sistema.detectarComportamientoInusual();

        System.out.println("\n🚀 ¡Simulación finalizada exitosamente!");
    }
}
