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
            "{\"codigo\":\"%s\", \"direccion\":\"%s\", \"ciudad\":\"%s\", \"tipo\":\"%s\", \"precio\":%.2f, \"zona\":\"%s\", \"finalidad\":\"%s\", \"area\":%.2f, \"areaT\":%.2f, \"hab\":%d, \"banos\":%d, \"estadoFisico\":\"%s\", \"disponibilidad\":\"%s\", \"asesor\":%s, \"fotos\":%s}",
            i.getCodigo(), i.getDireccion(), i.getCiudad(), i.getTipo(), i.getPrecio(), i.getZona(), i.getFinalidad(), i.getArea(), i.getAreaTerreno(), i.getHabitaciones(), i.getBanos(), i.getEstadoFisico(), i.getDisponibilidad(), asesorJson, listJson
        );
    }

    public static String asesorToJson(Asesor a) {
        if (a == null) return "null";
        
        // Serializar arreglo de códigos de inmuebles
        String inmueblesJson = listToJson(a.getInmueblesAsignados(), i -> "\"" + i.getCodigo() + "\"");
        
        // Serializar visitas pendientes (CustomQueue) extrayendo sus elementos
        CustomQueue<Visita> colaVisitas = a.getVisitasPendientes();
        StringBuilder visitasStr = new StringBuilder("[");
        // Nota: asumiendo que no desencolamos para no perder la información, iteramos o lo mantenemos simple.
        // Como CustomQueue puede no tener un iterador público simple en este proyecto, 
        // vamos a simularlo convirtiendo a lista temporal si es posible, 
        // o si no, lo representaremos con el conteo, pero intentemos serializar su contenido:
        // En este caso, para no alterar la cola, simplemente enviaremos un string de resumen o array si hay forma.
        // Dado que Visita no tiene método fácil de acceder, dejaremos un array vacío si no podemos iterar,
        // O mejor: a.getVisitasPendientes().getSize() para el conteo.
        
        return String.format(java.util.Locale.US,
            "{\"id\":\"%s\", \"nombre\":\"%s\", \"contacto\":\"%s\", \"zona\":\"%s\", \"cierres\":%d, \"inmueblesCount\":%d, \"inmuebles\":%s, \"visitasCount\":%d}",
            a.getIdentificacion(), a.getNombre(), a.getContacto(), a.getEspecialidadZona(), a.getCierresRealizados(), a.getInmueblesAsignados().getSize(), inmueblesJson, colaVisitas.getSize()
        );
    }

    public static String clienteToJson(Cliente c) {
        if (c == null) return "null";
        return String.format(java.util.Locale.US,
            "{\"id\":\"%s\", \"nombre\":\"%s\", \"correo\":\"%s\", \"telefono\":\"%s\", \"tipo\":\"%s\", \"presupuesto\":%.2f, \"zona\":\"%s\", \"minHabitaciones\":%d, \"estadoBusqueda\":\"%s\"}",
            c.getIdentificacion(), c.getNombre(), c.getCorreo(), c.getTelefono(), c.getTipoCliente(), c.getPresupuesto(), c.getZonasDeInteres(), c.getMinHabitaciones(), c.getEstadoBusqueda()
        );
    }

    public static String eventoAnomaloToJson(EventoAnomalo e) {
        if (e == null) return "null";
        return String.format(java.util.Locale.US,
            "{\"tipo\":\"%s\", \"idEntidad\":\"%s\", \"descripcion\":\"%s\", \"nivel\":\"%s\", \"timestamp\":\"%s\"}",
            e.getTipo(), e.getEntidadId(), e.getDescripcion(), e.getNivelAtencion(), e.getFechaDeteccion()
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
