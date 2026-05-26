package servicios_ia;

import models.Inmueble;
import structures.CustomList;
import controllers.PropTechSystem;

public class AsistenteVirtual {

    private PropTechSystem sistema;

    public AsistenteVirtual(PropTechSystem sistema) {
        this.sistema = sistema;
    }

    private String normalizar(String str) {
        if (str == null) return "";
        return str.toLowerCase()
                  .replace("á", "a")
                  .replace("é", "e")
                  .replace("í", "i")
                  .replace("ó", "o")
                  .replace("ú", "u")
                  .replace("ü", "u")
                  .replace("ñ", "n");
    }

    public RespuestaChat procesarMensaje(String mensaje, String rol) {
        String textoOriginal = mensaje != null ? mensaje : "";
        String textoLimpio = normalizar(textoOriginal);
        
        // 1. Intención: Buscar propiedades
        boolean tieneIntencionBuscar = 
            textoLimpio.contains("buscar") || textoLimpio.contains("bucar") || textoLimpio.contains("busacar") ||
            textoLimpio.contains("busco") || textoLimpio.contains("busq") ||
            textoLimpio.contains("quiero") || textoLimpio.contains("kiero") || textoLimpio.contains("qiero") ||
            textoLimpio.contains("necesito") || textoLimpio.contains("nesesito") || textoLimpio.contains("nececito") || textoLimpio.contains("nesecito") ||
            textoLimpio.contains("muestrame") || textoLimpio.contains("mostrar") || 
            textoLimpio.contains("ver") || textoLimpio.contains("ensename") || 
            textoLimpio.contains("encuentra") || textoLimpio.contains("lista") || 
            textoLimpio.contains("apartamento") || textoLimpio.contains("apto") || textoLimpio.contains("apartameto") || textoLimpio.contains("aprtamento") ||
            textoLimpio.contains("casa") || textoLimpio.contains("caza") ||
            textoLimpio.contains("oficina") || 
            textoLimpio.contains("local") || 
            textoLimpio.contains("lote");

        if (tieneIntencionBuscar) {
            String tipoBuscado = "Todos";
            if (textoLimpio.contains("apto") || textoLimpio.contains("apartamento") || textoLimpio.contains("apartameto") || textoLimpio.contains("aprtamento") || textoLimpio.contains("depa")) {
                tipoBuscado = "Apartamento";
            } else if (textoLimpio.contains("casa") || textoLimpio.contains("casita") || textoLimpio.contains("caza")) {
                tipoBuscado = "Casa";
            } else if (textoLimpio.contains("oficina") || textoLimpio.contains("ofic")) {
                tipoBuscado = "Oficina";
            } else if (textoLimpio.contains("local")) {
                tipoBuscado = "Local Comercial";
            } else if (textoLimpio.contains("lote")) {
                tipoBuscado = "Lote";
            }

            int habBuscadas = 0;
            // Detección mejorada de números con expresiones regulares
            if (textoLimpio.matches(".*\\b(1|un|una)\\s*(hab|alc|cuar|piez|dorm|habitac).*")) habBuscadas = 1;
            else if (textoLimpio.matches(".*\\b(2|dos)\\s*(hab|alc|cuar|piez|dorm|habitac).*")) habBuscadas = 2;
            else if (textoLimpio.matches(".*\\b(3|tres)\\s*(hab|alc|cuar|piez|dorm|habitac).*")) habBuscadas = 3;
            else if (textoLimpio.matches(".*\\b(4|cuatro)\\s*(hab|alc|cuar|piez|dorm|habitac).*")) habBuscadas = 4;
            else if (textoLimpio.matches(".*\\b(5|cinco)\\s*(hab|alc|cuar|piez|dorm|habitac).*")) habBuscadas = 5;

            String finalidad = "Todos";
            if (textoLimpio.contains("arriendo") || textoLimpio.contains("alquilar") || textoLimpio.contains("rentar") || textoLimpio.contains("alquiler") || textoLimpio.contains("renta")) {
                finalidad = "Arriendo";
            } else if (textoLimpio.contains("comprar") || textoLimpio.contains("venta") || textoLimpio.contains("vender") || textoLimpio.contains("compra")) {
                finalidad = "Venta";
            }

            // Detección de presupuesto / precio máximo aproximado
            double precioMax = 0;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:hasta|presupuesto|maximo|precio de|menor a|de)\\s*\\$?(\\d+([.,]\\d+)*)\\s*(millon|millones|k)?\\b");
            java.util.regex.Matcher m = p.matcher(textoLimpio);
            if (m.find()) {
                try {
                    String numStr = m.group(1).replaceAll("[.,]", "");
                    double valor = Double.parseDouble(numStr);
                    String sufijo = m.group(3);
                    if (sufijo != null) {
                        if (sufijo.startsWith("millon")) {
                            valor *= 1000000;
                        } else if (sufijo.equals("k")) {
                            valor *= 1000;
                        }
                    }
                    precioMax = valor;
                } catch (Exception e) {
                    // Ignorar error de parsing
                }
            }

            CustomList<Inmueble> filtrados = sistema.filtrarInmueblesAvanzado(tipoBuscado, finalidad, habBuscadas, 0, 0, precioMax);

            // Filtrado adicional de zonas (normalizadas)
            CustomList<Inmueble> resultadosFinales = new CustomList<>();
            for (int i = 0; i < filtrados.getSize(); i++) {
                Inmueble in = filtrados.get(i);
                boolean pasaZona = true;
                String zonaInmueble = normalizar(in.getZona());
                if (textoLimpio.contains("norte") && !zonaInmueble.contains("norte")) pasaZona = false;
                if (textoLimpio.contains("chapinero") && !zonaInmueble.contains("chapinero")) pasaZona = false;
                if (textoLimpio.contains("colina") && !zonaInmueble.contains("colina")) pasaZona = false;
                
                if (pasaZona) resultadosFinales.add(in);
            }

            StringBuilder descFiltro = new StringBuilder();
            if (!tipoBuscado.equals("Todos")) descFiltro.append(" ").append(tipoBuscado).append("(s)");
            if (habBuscadas > 0) descFiltro.append(" de ").append(habBuscadas).append(" habitaciones");
            if (!finalidad.equals("Todos")) descFiltro.append(" en ").append(finalidad);
            if (precioMax > 0) descFiltro.append(" con precio maximo de $").append(String.format("%,.0f", precioMax));

            String desc = descFiltro.toString().trim();
            String textoRespuesta = "Claro! Busque en nuestro inventario" + (desc.isEmpty() ? "" : " " + desc) + " y encontre " + resultadosFinales.getSize() + " opciones excelentes para ti:";
            if (resultadosFinales.getSize() == 0) {
                textoRespuesta = "Lo siento, actualmente no encontre propiedades exactas para" + (desc.isEmpty() ? " lo que buscas" : " " + desc) + ". Pero te invito a explorar nuestro catalogo completo.";
            }

            return new RespuestaChat(textoRespuesta, resultadosFinales);
        }

        // 2. Preguntar por el rol ("¿quién soy?")
        if (textoLimpio.contains("quien soy") || textoLimpio.contains("mi rol") || textoLimpio.contains("mi perfil")) {
            String nombreRol = rol.substring(0, 1).toUpperCase() + rol.substring(1);
            if (rol.equals("guest")) nombreRol = "Invitado";
            return new RespuestaChat("Actualmente he identificado que estas conectado con el rol de: **" + nombreRol + "**.", null);
        }

        // 3. Respuestas Generales y Saludos Personalizados
        if (textoLimpio.contains("hola") || textoLimpio.contains("saludos") || textoLimpio.contains("buenas")) {
            String saludoPers = "¡Hola!";
            if (rol.equals("admin")) saludoPers = "¡Hola Jefe/Administrador!";
            else if (rol.equals("asesor")) saludoPers = "¡Hola Colega Asesor!";
            else if (rol.equals("cliente")) saludoPers = "¡Hola! Que gusto tenerte por aqui como nuestro Cliente.";
            else if (rol.equals("guest")) saludoPers = "¡Hola Invitado!";

            return new RespuestaChat(saludoPers + " Soy tu Asistente Inteligente. Puedo ayudarte a buscar propiedades. Prueba escribiendo: 'Ayudame a buscar un apartamento de 3 habitaciones'.", null);
        }

        // 4. Respuestas exclusivas por Rol
        if (rol.equals("admin") && (textoLimpio.contains("resumen") || textoLimpio.contains("estado"))) {
            return new RespuestaChat("Como Administrador tienes control total. Actualmente el sistema tiene " + sistema.obtenerCatalogoLista().getSize() + " inmuebles y " + sistema.obtenerClientesLista().getSize() + " clientes registrados.", null);
        }

        if (rol.equals("asesor") && textoLimpio.contains("mis clientes")) {
            return new RespuestaChat("Colega asesor, te recomiendo revisar siempre tu panel principal para ver las visitas pendientes. ¡Mucho exito en tus cierres de hoy!", null);
        }

        // 5. Agradecimientos
        if (textoLimpio.contains("gracias") || textoLimpio.contains("agradezco") || textoLimpio.contains("amable") || textoLimpio.contains("excelente") || textoLimpio.contains("super") || textoLimpio.contains("genial")) {
            return new RespuestaChat("¡Con mucho gusto! Estoy aqui para ayudarte. ¿Hay algo mas en lo que te pueda asesorar?", null);
        }

        // 6. Afirmaciones / OK / Entendido
        if (textoLimpio.equals("ok") || textoLimpio.equals("vale") || textoLimpio.equals("listo") || textoLimpio.contains("esta bien") || textoLimpio.contains("de acuerdo") || textoLimpio.contains("entendido") || textoLimpio.equals("perfecto") || textoLimpio.equals("bien") || textoLimpio.equals("si") || textoLimpio.equals("sii")) {
            return new RespuestaChat("¡Perfecto! Quedo a tu disposicion si necesitas buscar alguna otra propiedad o hacer una consulta extra.", null);
        }

        // 7. Despedidas
        if (textoLimpio.contains("adios") || textoLimpio.contains("chao") || textoLimpio.contains("hasta luego") || textoLimpio.contains("nos vemos") || textoLimpio.contains("bye")) {
            return new RespuestaChat("¡Hasta luego! Vuelve pronto si necesitas ayuda buscando tu inmueble ideal.", null);
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
