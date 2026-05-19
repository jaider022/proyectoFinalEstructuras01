package servicios_ia;

import models.Inmueble;
import structures.CustomList;
import controllers.PropTechSystem;

public class AsistenteVirtual {

    private PropTechSystem sistema;

    public AsistenteVirtual(PropTechSystem sistema) {
        this.sistema = sistema;
    }

    public RespuestaChat procesarMensaje(String mensaje, String rol) {
        String textoLimpio = mensaje.toLowerCase();
        
        // 1. Intención: Buscar propiedades
        if (textoLimpio.contains("buscar") || textoLimpio.contains("quiero") || textoLimpio.contains("necesito") || textoLimpio.contains("muéstrame") || textoLimpio.contains("busco")) {
            
            String tipoBuscado = "Todos";
            if (textoLimpio.contains("apartamento") || textoLimpio.contains("apto")) tipoBuscado = "Apartamento";
            else if (textoLimpio.contains("casa")) tipoBuscado = "Casa";
            else if (textoLimpio.contains("oficina")) tipoBuscado = "Oficina";
            else if (textoLimpio.contains("local")) tipoBuscado = "Local Comercial";

            int habBuscadas = 0;
            // Detección simple de números
            if (textoLimpio.contains("1 hab") || textoLimpio.contains("una hab") || textoLimpio.contains("1 alcoba")) habBuscadas = 1;
            if (textoLimpio.contains("2 hab") || textoLimpio.contains("dos hab") || textoLimpio.contains("2 alcoba") || textoLimpio.contains("dos alcoba")) habBuscadas = 2;
            if (textoLimpio.contains("3 hab") || textoLimpio.contains("tres hab") || textoLimpio.contains("3 alcoba") || textoLimpio.contains("tres alcoba") || textoLimpio.contains("3 habitacione")) habBuscadas = 3;
            if (textoLimpio.contains("4 hab") || textoLimpio.contains("cuatro hab") || textoLimpio.contains("4 alcoba")) habBuscadas = 4;

            String finalidad = "Todos";
            if (textoLimpio.contains("arriendo") || textoLimpio.contains("alquilar") || textoLimpio.contains("rentar")) finalidad = "Arriendo";
            else if (textoLimpio.contains("comprar") || textoLimpio.contains("venta")) finalidad = "Venta";

            CustomList<Inmueble> filtrados = sistema.filtrarInmueblesAvanzado(tipoBuscado, finalidad, habBuscadas, 0, 0, 0);

            // Filtrar también por "Bogotá" o zona si menciona
            CustomList<Inmueble> resultadosFinales = new CustomList<>();
            for (int i = 0; i < filtrados.getSize(); i++) {
                Inmueble in = filtrados.get(i);
                boolean pasaZona = true;
                if (textoLimpio.contains("norte") && !in.getZona().toLowerCase().contains("norte")) pasaZona = false;
                if (textoLimpio.contains("chapinero") && !in.getZona().toLowerCase().contains("chapinero")) pasaZona = false;
                if (textoLimpio.contains("colina") && !in.getZona().toLowerCase().contains("colina")) pasaZona = false;
                
                if (pasaZona) resultadosFinales.add(in);
            }

            String descFiltro = "";
            if (!tipoBuscado.equals("Todos")) descFiltro += " " + tipoBuscado + "(s)";
            if (habBuscadas > 0) descFiltro += " de " + habBuscadas + " habitaciones";
            if (!finalidad.equals("Todos")) descFiltro += " en " + finalidad;

            String textoRespuesta = "¡Claro! Busqué en nuestro inventario" + descFiltro + " y encontré " + resultadosFinales.getSize() + " opciones excelentes para ti:";
            if (resultadosFinales.getSize() == 0) {
                textoRespuesta = "Lo siento, actualmente no encontré propiedades exactas para lo que buscas. Pero te invito a explorar nuestro catálogo completo.";
            }

            return new RespuestaChat(textoRespuesta, resultadosFinales);
        }

        // 2. Preguntar por el rol ("¿quién soy?")
        if (textoLimpio.contains("quién soy") || textoLimpio.contains("quien soy") || textoLimpio.contains("mi rol") || textoLimpio.contains("mi perfil")) {
            String nombreRol = rol.substring(0, 1).toUpperCase() + rol.substring(1);
            if (rol.equals("guest")) nombreRol = "Invitado";
            return new RespuestaChat("Actualmente he identificado que estás conectado con el rol de: **" + nombreRol + "**.", null);
        }

        // 3. Respuestas Generales y Saludos Personalizados
        if (textoLimpio.contains("hola") || textoLimpio.contains("saludos") || textoLimpio.contains("buenas")) {
            String saludoPers = "¡Hola!";
            if (rol.equals("admin")) saludoPers = "¡Hola Jefe/Administrador!";
            else if (rol.equals("asesor")) saludoPers = "¡Hola Colega Asesor!";
            else if (rol.equals("cliente")) saludoPers = "¡Hola! Qué gusto tenerte por aquí como nuestro Cliente.";
            else if (rol.equals("guest")) saludoPers = "¡Hola Invitado!";

            return new RespuestaChat(saludoPers + " Soy tu Asistente Inteligente. Puedo ayudarte a buscar propiedades. Prueba escribiendo: 'Ayúdame a buscar un apartamento de 3 habitaciones'.", null);
        }

        // 4. Respuestas exclusivas por Rol
        if (rol.equals("admin") && (textoLimpio.contains("resumen") || textoLimpio.contains("estado"))) {
            return new RespuestaChat("Como Administrador tienes control total. Actualmente el sistema tiene " + sistema.obtenerCatalogoLista().getSize() + " inmuebles y " + sistema.obtenerClientesLista().getSize() + " clientes registrados.", null);
        }

        if (rol.equals("asesor") && textoLimpio.contains("mis clientes")) {
            return new RespuestaChat("Colega asesor, te recomiendo revisar siempre tu panel principal para ver las visitas pendientes. ¡Mucho éxito en tus cierres de hoy!", null);
        }

        return new RespuestaChat("No estoy seguro de entenderte por completo. Recuerda que soy un asistente inmobiliario, intenta decirme: 'Quiero comprar una casa de 2 habitaciones en el norte'.", null);
    }

    public static class RespuestaChat {
        public String texto;
        public CustomList<Inmueble> propiedades;

        public RespuestaChat(String texto, CustomList<Inmueble> propiedades) {
            this.texto = texto;
            this.propiedades = propiedades;
        }
    }
}
