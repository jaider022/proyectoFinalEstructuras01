import controllers.PropTechSystem;
import models.Inmueble;
import models.Cliente;
import models.Asesor;
import models.Operacion;
import structures.CustomList;
import servicios_ia.AsistenteVirtual;

public class TestFilter {
    public static void main(String[] args) {
        PropTechSystem sistema = new PropTechSystem();

        Inmueble inm10 = new Inmueble("INM-10", "Calle 134 #50", "Bogotá", "Colina", "Apartamento",
                                        "Venta", 520000.0, 110,
                                        0, 3, 2, "Remodelado", "Disponible");
        sistema.registrarInmueble(inm10);

        CustomList<Inmueble> filtrados = sistema.filtrarInmueblesAvanzado("Todos", "Todos", 3, 0, 0, 0);
        System.out.println("Filtrados directos: " + filtrados.getSize());

        AsistenteVirtual asistente = new AsistenteVirtual(sistema);
        AsistenteVirtual.RespuestaChat res = asistente.procesarMensaje("hola me ayudas a buscar un partamento de 3 habitacioenes", "cliente");
        System.out.println("Respuesta Asistente: " + res.texto);
        if (res.propiedades != null) {
            System.out.println("Propiedades Asistente: " + res.propiedades.getSize());
        }
    }
}
