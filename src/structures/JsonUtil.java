package structures;

import models.*;
import java.util.Iterator;

/**
 * Utilidad minimalista para convertir nuestras estructuras custom a JSON 
 * sin depender de librerías externas como Jackson o Gson.
 */
public class JsonUtil {

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    public static String inmuebleToJson(Inmueble i) {
        if (i == null) return "null";
        String listJson = listToJson(i.getFotos(), s -> "\"" + escapeJson(s) + "\"");
        
        String asesorJson = "null";
        if (i.getAsesorResponsable() != null) {
            asesorJson = String.format("{\"nombre\":\"%s\", \"contacto\":\"%s\"}", 
                escapeJson(i.getAsesorResponsable().getNombre()), 
                escapeJson(i.getAsesorResponsable().getContacto()));
        }
        
        return String.format(java.util.Locale.US,
            "{\"codigo\":\"%s\", \"direccion\":\"%s\", \"ciudad\":\"%s\", \"tipo\":\"%s\", \"precio\":%.2f, \"zona\":\"%s\", \"finalidad\":\"%s\", \"area\":%.2f, \"areaT\":%.2f, \"hab\":%d, \"banos\":%d, \"estadoFisico\":\"%s\", \"disponibilidad\":\"%s\", \"asesor\":%s, \"fotos\":%s}",
            escapeJson(i.getCodigo()), escapeJson(i.getDireccion()), escapeJson(i.getCiudad()), escapeJson(i.getTipo()), i.getPrecio(), escapeJson(i.getZona()), escapeJson(i.getFinalidad()), i.getArea(), i.getAreaTerreno(), i.getHabitaciones(), i.getBanos(), escapeJson(i.getEstadoFisico()), escapeJson(i.getDisponibilidad()), asesorJson, listJson
        );
    }

    public static String asesorToJson(Asesor a) {
        if (a == null) return "null";
        
        String inmueblesJson = listToJson(a.getInmueblesAsignados(), i -> "\"" + escapeJson(i.getCodigo()) + "\"");
        CustomQueue<Visita> colaVisitas = a.getVisitasPendientes();
        String visitsJson = listToJson(colaVisitas.toList(), JsonUtil::visitaToJson);
        
        return String.format(java.util.Locale.US,
            "{\"id\":\"%s\", \"nombre\":\"%s\", \"contacto\":\"%s\", \"zona\":\"%s\", \"cierres\":%d, \"inmueblesCount\":%d, \"inmuebles\":%s, \"visitasCount\":%d, \"visitas\":%s}",
            escapeJson(a.getIdentificacion()), escapeJson(a.getNombre()), escapeJson(a.getContacto()), escapeJson(a.getEspecialidadZona()), a.getCierresRealizados(), a.getInmueblesAsignados().getSize(), inmueblesJson, colaVisitas.getSize(), visitsJson
        );
    }

    public static String clienteToJson(Cliente c) {
        if (c == null) return "null";
        return String.format(java.util.Locale.US,
            "{\"id\":\"%s\", \"nombre\":\"%s\", \"correo\":\"%s\", \"telefono\":\"%s\", \"tipo\":\"%s\", \"presupuesto\":%.2f, \"zona\":\"%s\", \"minHabitaciones\":%d, \"estadoBusqueda\":\"%s\"}",
            escapeJson(c.getIdentificacion()), escapeJson(c.getNombre()), escapeJson(c.getCorreo()), escapeJson(c.getTelefono()), escapeJson(c.getTipoCliente()), c.getPresupuesto(), escapeJson(c.getZonasDeInteres()), c.getMinHabitaciones(), escapeJson(c.getEstadoBusqueda())
        );
    }

    public static String eventoAnomaloToJson(EventoAnomalo e) {
        if (e == null) return "null";
        return String.format(java.util.Locale.US,
            "{\"tipo\":\"%s\", \"idEntidad\":\"%s\", \"descripcion\":\"%s\", \"nivel\":\"%s\", \"timestamp\":\"%s\"}",
            escapeJson(e.getTipo()), escapeJson(e.getEntidadId()), escapeJson(e.getDescripcion()), escapeJson(e.getNivelAtencion()), escapeJson(e.getFechaDeteccion())
        );
    }

    public static String visitaToJson(Visita v) {
        if (v == null) return "null";
        String inmuebleJson = inmuebleToJson(v.getInmueble());
        String asesorJson = "null";
        if (v.getAsesorAsignado() != null) {
            asesorJson = String.format("{\"nombre\":\"%s\", \"contacto\":\"%s\"}", 
                escapeJson(v.getAsesorAsignado().getNombre()), 
                escapeJson(v.getAsesorAsignado().getContacto()));
        }
        String clienteJson = "null";
        if (v.getCliente() != null) {
            clienteJson = String.format("{\"id\":\"%s\", \"nombre\":\"%s\"}", 
                escapeJson(v.getCliente().getIdentificacion()), 
                escapeJson(v.getCliente().getNombre()));
        }
        
        return String.format(java.util.Locale.US,
            "{\"fecha\":\"%s\", \"hora\":\"%s\", \"estado\":\"%s\", \"observaciones\":\"%s\", \"inmueble\":%s, \"asesor\":%s, \"cliente\":%s}",
            escapeJson(v.getFecha()), escapeJson(v.getHora()), escapeJson(v.getEstado()), escapeJson(v.getObservaciones()), inmuebleJson, asesorJson, clienteJson
        );
    }

    public static <T> String listToJson(CustomList<T> list, java.util.function.Function<T, String> mapper) {
        StringBuilder sb = new StringBuilder("[");
        if (list != null) {
            for (int i = 0; i < list.getSize(); i++) {
                sb.append(mapper.apply(list.get(i)));
                if (i < list.getSize() - 1) sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
