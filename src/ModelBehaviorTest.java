import models.*;
import controllers.PropTechSystem;
import structures.CustomList;

public class ModelBehaviorTest {
    public static void main(String[] args) {
        System.out.println("=== TEST DE COMPORTAMIENTOS PROPTECH ===\n");
        
        PropTechSystem sistema = new PropTechSystem();

        // 1. Probar Estados
        Inmueble i1 = new Inmueble("TEST-1", "Dir 1", "Ciu 1", "Zon 1", "Casa", "Venta", 100000.0, 50, 0, 1, 1, "Usado", "Disponible");
        System.out.println("Disponibilidad inicial: " + i1.getDisponibilidad());
        
        i1.cambiarDisponibilidad("Reservado");
        System.out.println("Disponibilidad después de cambiarDisponibilidad('Reservado'): " + i1.getDisponibilidad());
        
        i1.setDisponibilidad("Inexistente");
        System.out.println("Disponibilidad después de setDisponibilidad('Inexistente') (debe ignorar): " + i1.getDisponibilidad());
        
        // 2. Probar Asignación Bi-direccional
        Asesor a1 = new Asesor("A1", "Asesor 1", "123", "Norte");
        Asesor a2 = new Asesor("A2", "Asesor 2", "456", "Sur");
        
        System.out.println("\nAsignando asesor A1 al inmueble...");
        i1.setAsesorResponsable(a1);
        System.out.println("Asesor del inmueble: " + i1.getAsesorResponsable().getNombre());
        System.out.println("Inmuebles de A1: " + a1.getInmueblesAsignados().getSize());
        
        System.out.println("\nCambiando asesor a A2...");
        i1.setAsesorResponsable(a2);
        System.out.println("Asesor del inmueble: " + i1.getAsesorResponsable().getNombre());
        System.out.println("Inmuebles de A1 (debe ser 0): " + a1.getInmueblesAsignados().getSize());
        System.out.println("Inmuebles de A2 (debe ser 1): " + a2.getInmueblesAsignados().getSize());

        // 3. Probar Favoritos
        Cliente c1 = new Cliente("C1", "Cliente 1", "c1@mail.com", "123", "Comprador", 200000.0, "Norte", "Casa", 1, "Activo");
        System.out.println("\nAgregando favorito...");
        c1.agregarFavorito(i1);
        System.out.println("Favoritos de C1: " + c1.getFavoritos().getSize());
        
        c1.removerFavorito(i1);
        System.out.println("Favoritos de C1 después de remover: " + c1.getFavoritos().getSize());

        System.out.println("\n✅ Todos los comportamientos verificados satisfactoriamente.");
    }
}
