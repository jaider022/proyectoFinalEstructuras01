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
        Asesor juan = new Asesor("A-002", "Juan Perez", "300456", "Chapinero");
        Asesor laura = new Asesor("A-003", "Laura Gomez", "300789", "Colina");
        sistema.registrarAsesor(maria);
        sistema.registrarAsesor(juan);
        sistema.registrarAsesor(laura);

        Inmueble inm1 = new Inmueble("INM-01", "Calle 100 #15", "Bogotá", "Norte", "Apartamento", "Venta", 450000.0, 85,
                0, 3, 2, "Usado", "Disponible");
        inm1.getFotos().add("/assets/img/APARTAMENTOS/A1.jpg");
        inm1.getFotos().add("/assets/img/APARTAMENTOS/A2.jpg");
        inm1.setAsesorResponsable(maria);

        Inmueble inm2 = new Inmueble("INM-02", "Carrera 7 #72", "Bogotá", "Chapinero", "Apartamento", "Arriendo",
                5500.0, 120, 0, 2, 2, "Remodelado", "Disponible");
        inm2.getFotos().add("/assets/img/APARTAMENTOS/B1.jpg");
        inm2.setAsesorResponsable(juan);

        Inmueble inm3 = new Inmueble("INM-03", "Avenida 19 #100", "Bogotá", "Norte", "Oficina", "Venta", 890000.0, 200,
                0, 0, 4, "Nuevo", "Disponible");
        inm3.getFotos().add("/assets/img/OFICINAS/A1.jpg");
        inm3.setAsesorResponsable(maria);

        Inmueble inm4 = new Inmueble("INM-04", "Calle 127 #45", "Bogotá", "Colina", "Casa", "Venta", 670000.0, 150, 200,
                4, 3, "Usado", "Disponible");
        inm4.getFotos().add("/assets/img/CASAS/A1.jpg");
        inm4.setAsesorResponsable(laura);

        sistema.registrarInmueble(inm1);
        sistema.registrarInmueble(inm2);
        sistema.registrarInmueble(inm3);
        sistema.registrarInmueble(inm4);

        Cliente c1 = new Cliente("C-01", "Jaider Admin", "jaider@proptech.com", "311", "Comprador", 1000000.0, "Norte",
                "Apto", 0, "Activo");
        Cliente c2 = new Cliente("C-02", "Carlos Inversor", "carlos@mail.com", "322", "Comprador", 500000.0, "Norte",
                "Apto", 2, "Activo");
        Cliente c3 = new Cliente("C-03", "Ana Arrendataria", "ana@mail.com", "333", "Arrendatario", 6000.0, "Chapinero",
                "Apto", 1, "Activo");
        Cliente c4 = new Cliente("C-04", "Luis Negocios", "luis@mail.com", "344", "Comprador", 2000000.0, "Colina",
                "Oficina", 0, "Activo");

        sistema.registrarCliente(c1);
        sistema.registrarCliente(c2);
        sistema.registrarCliente(c3);
        sistema.registrarCliente(c4);
        sistema.registrarCliente(new Cliente("C-DEMO", "Cliente Invitado", "guest@proptech.com", "0000", "Invitado", 0,
                "Norte", "Todo", 0, "Activo"));

        // Dummy Operations (Cierres)
        System.out.println("Cargando simulaciones de cierres de negocio para Analitica...");
        for (int i = 1; i <= 10; i++) {
            Asesor as = (i % 3 == 0) ? juan : (i % 2 == 0) ? laura : maria;
            Cliente cl = (i % 2 == 0) ? c1 : c4;
            Inmueble in = (i % 2 == 0) ? inm1 : inm4;
            double val = (i * 100000.0);
            Operacion op = new Operacion("OP-SIM-" + i, in, cl, as, "2026-04-" + (10 + i), "Venta", val, val * 0.05);
            sistema.registrarOperacion(op);
            sistema.agendarVisita(cl.getIdentificacion(), in.getCodigo(), as.getIdentificacion(), "2026-03-" + (10 + i),
                    "10:00");
        }

        // 2. Iniciar el Servidor Web
        try {
            PropTechServer webServer = new PropTechServer(sistema, 8081);
            webServer.start();

            System.out.println("\n✅ SISTEMA LISTO.");
            System.out.println(">>> Abre tu navegador en: http://localhost:8081");
            System.out.println("\nPresiona Ctrl+C en esta consola para apagar el servidor.");

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
