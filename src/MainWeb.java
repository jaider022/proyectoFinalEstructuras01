import controllers.*;
import models.*;
import java.io.IOException;

public class MainWeb {
        public static void main(String[] args) {
                try {
                        System.out.println("=== INICIANDO PROPTECH WEB EDITION ===\n");

                        PropTechSystem sistema = new PropTechSystem();

                        // 1. Precarga de datos Completa
                        System.out.println("Precargando datos en las estructuras personalizadas...");

                        Asesor maria = new Asesor("A-001", "Maria Lopez", "300123", "Norte");
                        Asesor juan = new Asesor("A-002", "Juan Perez", "300456", "Chapinero");
                        Asesor laura = new Asesor("A-003", "Laura Gomez", "300789", "Colina");
                        sistema.registrarAsesor(maria);
                        sistema.registrarAsesor(juan);
                        sistema.registrarAsesor(laura);

                        Inmueble inm1 = new Inmueble("INM-01", "Calle 100 #15", "Bogotá", "Norte", "Apartamento",
                                        "Venta", 450000.0, 85,
                                        0, 3, 2, "Usado", "Disponible");
                        cargarFotos(inm1, "APARTAMENTOS", "A", 6);
                        inm1.setAsesorResponsable(maria);

                        Inmueble inm2 = new Inmueble("INM-02", "Carrera 7 #72", "Bogotá", "Chapinero", "Apartamento",
                                        "Arriendo",
                                        5500.0, 120, 0, 2, 2, "Remodelado", "Disponible");
                        cargarFotos(inm2, "APARTAMENTOS", "B", 6);
                        inm2.setAsesorResponsable(juan);

                        Inmueble inm3 = new Inmueble("INM-03", "Avenida 19 #100", "Bogotá", "Norte", "Oficina", "Venta",
                                        890000.0, 200,
                                        0, 0, 4, "Nuevo", "Disponible");
                        cargarFotos(inm3, "OFICINAS", "A", 4);
                        inm3.setAsesorResponsable(maria);

                        Inmueble inm4 = new Inmueble("INM-04", "Calle 127 #45", "Bogotá", "Colina", "Casa", "Venta",
                                        670000.0, 150, 200,
                                        4, 3, "Usado", "Disponible");
                        cargarFotos(inm4, "CASAS", "A", 5);
                        inm4.setAsesorResponsable(laura);

                        Inmueble inm5 = new Inmueble("INM-05", "Calle 140 #11", "Bogotá", "Norte", "Apartamento",
                                        "Venta", 320000.0, 70,
                                        0, 2, 2, "Usado", "Disponible");
                        cargarFotos(inm5, "APARTAMENTOS", "C", 9);
                        inm5.setAsesorResponsable(maria);

                        Inmueble inm6 = new Inmueble("INM-06", "Calle 80 #68", "Bogotá", "Norte", "Apartamento",
                                        "Arriendo", 2500.0, 65,
                                        0, 2, 1, "Nuevo", "Disponible");
                        cargarFotos(inm6, "APARTAMENTOS", "D", 6);
                        inm6.setAsesorResponsable(juan);

                        sistema.registrarInmueble(inm1);
                        sistema.registrarInmueble(inm2);
                        sistema.registrarInmueble(inm3);
                        sistema.registrarInmueble(inm4);
                        sistema.registrarInmueble(inm5);
                        sistema.registrarInmueble(inm6);

                        Cliente c1 = new Cliente("C-01", "Jaider Admin", "jaider@proptech.com", "311", "Comprador",
                                        1000000.0, "Norte",
                                        "Apto", 0, "Activo");
                        Cliente c2 = new Cliente("C-02", "Ana Torres", "ana@correo.com", "320", "Inversor", 5000000.0,
                                        "Norte",
                                        "Apto", 0, "Activo");
                        sistema.registrarCliente(c1);
                        sistema.registrarCliente(c2);

                        // AGENDAR VISITAS DE PRUEBA PARA QUE SE VEAN LOS CAMBIOS EN EL PANEL
                        sistema.agendarVisita("C-01", "INM-01", "A-001", "2026-06-15", "10:00", 0);
                        sistema.agendarVisita("C-02", "INM-05", "A-001", "2026-06-16", "14:00", 1);
                        sistema.agendarVisita("C-01", "INM-02", "A-002", "2026-06-17", "11:00", 0);

                        Cliente c3 = new Cliente("C-03", "Ana Arrendataria", "ana@mail.com", "333", "Arrendatario",
                                        6000.0, "Chapinero",
                                        "Apto", 1, "Activo");
                        Cliente c4 = new Cliente("C-04", "Luis Negocios", "luis@mail.com", "344", "Comprador",
                                        2000000.0, "Colina",
                                        "Oficina", 0, "Activo");

                        sistema.registrarCliente(c3);
                        sistema.registrarCliente(c4);
                        sistema.registrarCliente(new Cliente("C-DEMO", "Cliente Invitado", "guest@proptech.com", "0000",
                                        "Invitado", 0,
                                        "Norte", "Todo", 0, "Activo"));

                        // Simulación de Operaciones (Cierres)
                        System.out.println("Cargando simulaciones de cierres de negocio...");
                        for (int i = 1; i <= 10; i++) {
                                Asesor as = (i % 3 == 0) ? juan : (i % 2 == 0) ? laura : maria;
                                Cliente cl = (i % 2 == 0) ? c1 : c4;
                                Inmueble in = (i % 3 == 0) ? inm2 : (i % 2 == 0) ? inm1 : inm4;
                                double val = (i * 100000.0);
                                Operacion op = new Operacion("OP-SIM-" + i, in, cl, as, "2026-04-" + (10 + i), "Venta",
                                                val, val * 0.05);
                                sistema.registrarOperacion(op);
                        }

                        // 2. Iniciar el Servidor Web
                        System.out.println("Iniciando servidor en puerto 8082...");
                        PropTechServer webServer = new PropTechServer(sistema, 8082);
                        webServer.start();

                        System.out.println("\n✅ SISTEMA LISTO Y PROTEGIDO.");
                        System.out.println(">>> Abre tu navegador en: http://localhost:8082");

                        // Mantener vivo
                        while (true) {
                                Thread.sleep(10000);
                        }

                } catch (Exception e) {
                        System.err.println("\n❌ ERROR CRÍTICO DETECTADO:");
                        e.printStackTrace();
                        try {
                                Thread.sleep(60000);
                        } catch (Exception ex) {
                        }
                }
        }

        private static void cargarFotos(Inmueble inm, String carpeta, String letra, int cantidad) {
                for (int i = 1; i <= cantidad; i++) {
                        String path = "/assets/img/" + carpeta + "/" + letra + i + ".jpg";
                        inm.getFotos().add(path);
                }
        }
}
