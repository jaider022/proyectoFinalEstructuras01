import structures.*;
import models.*;
import controllers.*;
import java.util.Map;

public class TestSearch {
    public static void main(String[] args) {
        PropTechSystem sistema = new PropTechSystem();
        
        Asesor as1 = new Asesor("A-1", "Asesor 1", "300", "Norte");
        sistema.registrarAsesor(as1);

        Inmueble inm6 = new Inmueble("INM-06", "Calle 80 #68", "Bogotá", "Norte", "Apartamento", "Arriendo", 2500.0, 65, 0, 2, 1, "Nuevo", "Disponible");
        sistema.registrarInmueble(inm6);

        CustomList<Inmueble> recs = sistema.obtenerRecomendacionesManuales("Cualquiera", "Cualquiera", 2500, 2, false);
        
        System.out.println("Resultados: " + recs.getSize());
        for(int i=0; i<recs.getSize(); i++){
            System.out.println("Encontrado: " + recs.get(i).getCodigo() + " - Precio: " + recs.get(i).getPrecio());
        }
    }
}
