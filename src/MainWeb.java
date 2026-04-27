import controllers.*;
import models.*;
import java.io.IOException;

public class MainWeb {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PROPTECH WEB EDITION ===\n");

        PropTechSystem sistema = new PropTechSystem();

        // 1. Precarga de datos para que la web no se vea vacía
        System.out.println("Precargando datos en las estructuras personalizadas...");
        
        Asesor maria = new Asesor("A-001", "Maria Lopez", "300123", "Norte");
        sistema.registrarAsesor(maria);

        sistema.registrarInmueble(new Inmueble("INM-01", "Calle 100 #15", "Bogotá", "Chicó", "Apartamento", "Venta", 450000.0, 85, 0, 3, 2, "Disponible"));
        sistema.registrarInmueble(new Inmueble("INM-02", "Carrera 7 #72", "Bogotá", "Chapinero", "Apartamento", "Arriendo", 5500.0, 120, 0, 2, 2, "Disponible"));
        sistema.registrarInmueble(new Inmueble("INM-03", "Avenida 19 #100", "Bogotá", "Norte", "Oficina", "Venta", 890000.0, 200, 0, 0, 4, "Disponible"));
        sistema.registrarInmueble(new Inmueble("INM-04", "Calle 127 #45", "Bogotá", "Colina", "Casa", "Venta", 670000.0, 150, 200, 4, 3, "Disponible"));

        sistema.registrarCliente(new Cliente("C-01", "Jaider Admin", "jaider@proptech.com", "311", "Comprador", 1000000.0, "Norte", "Apto"));
        sistema.registrarCliente(new Cliente("C-02", "Carlos Inversor", "carlos@mail.com", "322", "Comprador", 500000.0, "Norte", "Apto"));
        sistema.registrarCliente(new Cliente("C-DEMO", "Cliente Invitado", "guest@proptech.com", "0000", "Invitado", 0, "Norte", "Todo"));

        // 2. Iniciar el Servidor Web
        try {
            PropTechServer webServer = new PropTechServer(sistema, 8080);
            webServer.start();
            
            System.out.println("\n✅ SISTEMA LISTO.");
            System.out.println(">>> Abre tu navegador en: http://localhost:8080");
            System.out.println("\nPresiona Ctrl+C en esta consola para apagar el servidor.");
            
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
