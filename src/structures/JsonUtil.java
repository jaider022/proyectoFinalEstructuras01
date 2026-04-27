package structures;

import models.*;
import java.util.Iterator;

/**
 * Utilidad minimalista para convertir nuestras estructuras custom a JSON 
 * sin depender de librerías externas como Jackson o Gson.
 */
public class JsonUtil {

    public static String inmuebleToJson(Inmueble i) {
        if (i == null) return "null";
        String listJson = listToJson(i.getFotos(), s -> "\"" + s + "\"");
        String asesorJson = i.getAsesorResponsable() != null ? 
            String.format("{\"nombre\":\"%s\", \"contacto\":\"%s\"}", i.getAsesorResponsable().getNombre(), i.getAsesorResponsable().getContacto()) : "null";
        
        return String.format(java.util.Locale.US,
            "{\"codigo\":\"%s\", \"direccion\":\"%s\", \"ciudad\":\"%s\", \"tipo\":\"%s\", \"precio\":%.2f, \"zona\":\"%s\", \"finalidad\":\"%s\", \"area\":%.2f, \"areaT\":%.2f, \"hab\":%d, \"banos\":%d, \"estado\":\"%s\", \"asesor\":%s, \"fotos\":%s}",
            i.getCodigo(), i.getDireccion(), i.getCiudad(), i.getTipo(), i.getPrecio(), i.getZona(), i.getFinalidad(), i.getArea(), i.getAreaTerreno(), i.getHabitaciones(), i.getBanos(), i.getEstado(), asesorJson, listJson
        );
    }

    public static String clienteToJson(Cliente c) {
        if (c == null) return "null";
        return String.format(java.util.Locale.US,
            "{\"id\":\"%s\", \"nombre\":\"%s\", \"tipo\":\"%s\", \"presupuesto\":%.2f}",
            c.getIdentificacion(), c.getNombre(), c.getTipoCliente(), c.getPresupuesto()
        );
    }

    public static <T> String listToJson(CustomList<T> list, java.util.function.Function<T, String> mapper) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.getSize(); i++) {
            sb.append(mapper.apply(list.get(i)));
            if (i < list.getSize() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
