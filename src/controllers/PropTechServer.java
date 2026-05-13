package controllers;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import structures.JsonUtil;
import structures.CustomList;
import structures.CustomHashTable;
import models.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PropTechServer {
    private PropTechSystem sistema;
    private int port;

    public PropTechServer(PropTechSystem sistema, int port) {
        this.sistema = sistema;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Endpoint para archivos estáticos (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler());

        // API endpoints
        server.createContext("/api/inmuebles", new InmueblesHandler());
        server.createContext("/api/inmuebles/filtrar", new InmueblesFiltrarHandler());
        server.createContext("/api/inmuebles/add", new InmuebleAddHandler());
        server.createContext("/api/inmuebles/delete", new InmuebleDeleteHandler());
        server.createContext("/api/clientes", new ClientesHandler());
        server.createContext("/api/clientes/register", new ClienteRegisterHandler());
        server.createContext("/api/visitas/agendar", new VisitaAgendarHandler());
        server.createContext("/api/visitas/slots", new VisitaSlotsHandler());
        server.createContext("/api/login/unified", new UnifiedLoginHandler());
        server.createContext("/api/operaciones/add", new OperacionAddHandler());
        server.createContext("/api/analitica", new AnaliticaHandler());
        server.createContext("/api/asesores", new AsesoresHandler());
        server.createContext("/api/asesores/add", new AsesorAddHandler());
        server.createContext("/api/alertas", new AlertasHandler());
        server.createContext("/api/auditoria", new AuditoriaHandler());
        server.createContext("/api/recomendaciones", new RecomendacionesHandler());
        server.createContext("/api/undo/snapshot", new UndoSnapshotHandler());
        server.createContext("/api/undo/admin", new UndoAdminHandler());
        server.createContext("/api/visitas/reprogramar", new VisitaReprogramarHandler());
        server.createContext("/api/visitas/cancelar", new VisitaCancelarHandler());
        server.createContext("/api/operaciones/renovar", new OperacionRenovarHandler());
        server.createContext("/api/reportes", new ReportesHandler());
        server.createContext("/api/simulacion/demanda", new SimulacionDemandaHandler());
        server.createContext("/api/rankings", new RankingsHandler());
        server.createContext("/api/clientes/favoritos", new ClienteFavoritosHandler());
        server.createContext("/api/clientes/favoritos/add", new ClienteFavoritosAddHandler());
        server.createContext("/api/clientes/favoritos/remove", new ClienteFavoritosRemoveHandler());
        server.createContext("/api/clientes/visitas", new ClienteVisitasHandler());

        server.setExecutor(null);
        System.out.println("Servidor PropTech iniciado en http://localhost:" + port);
        server.start();
    }

    // --- MANIPULACIÓN DE PARÁMETROS URL ---
    private java.util.Map<String, String> getQueryParams(String query) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    params.put(entry[0], java.net.URLDecoder.decode(entry[1], java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        java.io.InputStream is = exchange.getRequestBody();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toString("UTF-8");
    }

    private String unescapeJson(String input) {
        if (input == null) return null;
        // El navegador escapa '/' como '\/' en JSON.stringify, lo cual rompe el Base64.
        return input.replace("\\/", "/").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private java.util.Map<String, String> parseJson(String json) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        try {
            int i = 0;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\"') {
                    // Encontramos una posible clave
                    int start = i + 1;
                    int end = json.indexOf('\"', start);
                    if (end == -1) break;
                    String key = json.substring(start, end);
                    
                    // Buscar el separador ':'
                    int colon = json.indexOf(':', end);
                    if (colon == -1) break;
                    
                    // Buscar el inicio del valor
                    int valueStart = colon + 1;
                    while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                        valueStart++;
                    }
                    
                    if (valueStart < json.length()) {
                        if (json.charAt(valueStart) == '\"') {
                            // Valor es un String
                            int vStart = valueStart + 1;
                            int vEnd = json.indexOf('\"', vStart);
                            if (vEnd != -1) {
                                String value = json.substring(vStart, vEnd);
                                // APLICAR LIMPIEZA DE CARACTERES DE ESCAPE
                                map.put(key, unescapeJson(value));
                                i = vEnd + 1;
                            } else { i = vStart; }
                        } else {
                            // Valor es un número
                            int vEnd = valueStart;
                            while (vEnd < json.length() && (Character.isDigit(json.charAt(vEnd)) || json.charAt(vEnd) == '.')) {
                                vEnd++;
                            }
                            String value = json.substring(valueStart, vEnd);
                            map.put(key, value);
                            i = vEnd;
                        }
                    }
                } else {
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parseando JSON manual: " + e.getMessage());
        }
        return map;
    }

    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            java.io.File file = new java.io.File("web" + path);
            if (file.exists()) {
                byte[] content = Files.readAllBytes(Paths.get(file.getPath()));
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (path.endsWith(".png")) contentType = "image/png";
                else if (path.endsWith(".webp")) contentType = "image/webp";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                sendResponse(exchange, "404 Not Found", 404);
            }
        }
    }

    class InmueblesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<Inmueble> lista = sistema.obtenerCatalogoLista();
            String response = JsonUtil.listToJson(lista, JsonUtil::inmuebleToJson);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class InmueblesFiltrarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            String tipo = p.get("tip");
            String finalidad = p.get("fin");
            int hab = p.get("hab") != null && !p.get("hab").isEmpty() ? Integer.parseInt(p.get("hab")) : 0;
            int ban = p.get("ban") != null && !p.get("ban").isEmpty() ? Integer.parseInt(p.get("ban")) : 0;
            double preMin = p.get("preMin") != null && !p.get("preMin").isEmpty() ? Double.parseDouble(p.get("preMin")) : 0;
            double preMax = p.get("preMax") != null && !p.get("preMax").isEmpty() ? Double.parseDouble(p.get("preMax")) : 0;

            CustomList<Inmueble> filtrados = sistema.filtrarInmueblesAvanzado(tipo, finalidad, hab, ban, preMin, preMax);
            String response = JsonUtil.listToJson(filtrados, JsonUtil::inmuebleToJson);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class AsesoresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<Asesor> lista = sistema.obtenerAsesoresLista();
            String response = JsonUtil.listToJson(lista, JsonUtil::asesorToJson);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class AsesorAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p;
            
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                p = parseJson(body);
            } else {
                p = getQueryParams(exchange.getRequestURI().getQuery());
            }

            try {
                String id = p.get("id");
                String nombre = p.get("nombre");
                String contacto = p.get("contacto");
                String zona = p.get("zona");

                if (id == null || id.isEmpty() || nombre == null || nombre.isEmpty()) {
                    sendResponse(exchange, "{\"status\":\"error\", \"message\":\"ID y Nombre son obligatorios\"}", 400);
                    return;
                }

                Asesor nuevoAsesor = new Asesor(id, nombre, contacto, zona);
                sistema.registrarAsesor(nuevoAsesor);
                
                sendResponse(exchange, "{\"status\":\"ok\"}", 200);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}", 400);
            }
        }
    }

    class InmuebleAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p;
            
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                p = parseJson(body);
            } else {
                p = getQueryParams(exchange.getRequestURI().getQuery());
            }

            try {
                String estFis = p.get("estFis") != null ? p.get("estFis") : "Usado";
                Inmueble n = new Inmueble(p.get("cod"), p.get("dir"), p.get("ciu"), p.get("zon"), p.get("tip"), p.get("fin"),
                    Double.parseDouble(p.get("pre")), Double.parseDouble(p.get("are")), Double.parseDouble(p.get("areT")),
                    Integer.parseInt(p.get("hab")), Integer.parseInt(p.get("ban")), estFis, p.get("est"));
                
                String ftsStr = p.get("fts");
                if (ftsStr != null && !ftsStr.isEmpty()) {
                    // Usamos el nuevo separador |SEP| para no romper el Base64
                    for (String url : ftsStr.split("\\|SEP\\|")) {
                        if (!url.trim().isEmpty()) n.getFotos().add(url.trim());
                    }
                }
                
                boolean isUpdate = "true".equals(p.get("isUpdate"));
                
                // VALIDACIÓN DE DUPLICADOS: Si no es actualización y el código ya existe, error.
                if (!isUpdate && sistema.buscarInmueble(p.get("cod")) != null) {
                    sendResponse(exchange, "{\"status\":\"error\", \"message\":\"El código '" + p.get("cod") + "' ya está en uso por otro inmueble.\"}", 400);
                    return;
                }

                sistema.registrarInmueble(n);
                sendResponse(exchange, "{\"status\":\"ok\"}", 200);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}", 400);
            }
        }
    }

    class InmuebleDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            sistema.eliminarInmueble(p.get("id"));
            sendResponse(exchange, "{\"status\":\"ok\"}", 200);
        }
    }

    class ClientesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<Cliente> lista = sistema.obtenerClientesLista();
            String response = JsonUtil.listToJson(lista, JsonUtil::clienteToJson);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class ClienteRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            try {
                int minHab = p.get("minHab") != null ? Integer.parseInt(p.get("minHab")) : 0;
                String estBusq = p.get("estBusq") != null ? p.get("estBusq") : "Activo";

                Cliente c = new Cliente(p.get("id"), p.get("nom"), p.get("cor"), p.get("tel"), p.get("tip"), 
                    Double.parseDouble(p.get("pre")), p.get("zon"), p.get("itm"), minHab, estBusq);
                sistema.registrarCliente(c);
                sendResponse(exchange, "{\"status\":\"ok\"}", 200);
            } catch (Exception e) {
                sendResponse(exchange, "{\"status\":\"error\"}", 400);
            }
        }
    }

    class VisitaAgendarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            try {
                // sistema.agendarVisita(idCliente, codInmueble, idAsesor, fecha, hora)
                // Usamos el asesor responsable del inmueble si existe, sino el primer asesor disponible
                Inmueble i = sistema.buscarInmueble(p.get("cod"));
                String idAsesor = (i != null && i.getAsesorResponsable() != null) ? 
                    i.getAsesorResponsable().getIdentificacion() : "A-001"; // Fallback por defecto

                boolean ok = sistema.agendarVisita(p.get("cli"), p.get("cod"), idAsesor, p.get("fec"), p.get("hor"));
                if (ok) {
                    sendResponse(exchange, "{\"status\":\"ok\"}", 200);
                } else {
                    sendResponse(exchange, "{\"status\":\"error\", \"message\":\"No se pudo agendar\"}", 400);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}", 400);
            }
        }
    }

    class VisitaSlotsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            try {
                Inmueble i = sistema.buscarInmueble(p.get("cod"));
                String idAsesor = (i != null && i.getAsesorResponsable() != null) ? 
                    i.getAsesorResponsable().getIdentificacion() : "A-001";

                CustomList<String> slots = sistema.obtenerSlotsDisponibles(idAsesor, p.get("fec"));
                String response = JsonUtil.listToJson(slots, s -> "\"" + s + "\"");
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                sendResponse(exchange, response, 200);
            } catch (Exception e) {
                sendResponse(exchange, "{\"status\":\"error\"}", 400);
            }
        }
    }

    class UnifiedLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            String id = p.get("id");
            String pwd = p.get("pwd");
            
            String response = "";
            int code = 200;
            
            try {
                // 1. Verificar si es Admin
                if ("admin".equalsIgnoreCase(id) && "admin123".equals(pwd)) {
                    response = "{\"status\":\"ok\", \"role\":\"admin\", \"id\":\"admin\"}";
                } 
                // 2. Verificar si es Asesor
                else if (id != null && sistema.buscarAsesor(id) != null && "123".equals(pwd)) {
                    Asesor a = sistema.buscarAsesor(id);
                    response = String.format("{\"status\":\"ok\", \"role\":\"asesor\", \"id\":\"%s\", \"data\":%s}", 
                        id, JsonUtil.asesorToJson(a));
                }
                // 3. Verificar si es Cliente
                else if (id != null && sistema.buscarCliente(id) != null) {
                    Cliente c = sistema.buscarCliente(id);
                    response = String.format("{\"status\":\"ok\", \"role\":\"cliente\", \"id\":\"%s\", \"data\":%s}", 
                        id, JsonUtil.clienteToJson(c));
                }
                else {
                    response = "{\"status\":\"error\", \"message\":\"Usuario no encontrado o credenciales inválidas\"}";
                    code = 401;
                }
            } catch (Exception ex) {
                response = "{\"status\":\"error\", \"message\":\"Error interno en el servidor\"}";
                code = 500;
            }
            
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, code);
        }
    }

    class OperacionAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            try {
                Inmueble i = sistema.buscarInmueble(p.get("cod"));
                Cliente c = sistema.buscarCliente(p.get("cli"));
                Asesor a = i != null ? i.getAsesorResponsable() : null;
                String tip = p.get("tip");
                double val = Double.parseDouble(p.get("val"));
                
                Operacion op = new Operacion("OP-" + System.currentTimeMillis(), i, c, a, "Hoy", tip, val, val * 0.05);
                sistema.registrarOperacion(op);
                
                sendResponse(exchange, "{\"status\":\"ok\"}", 200);
            } catch (Exception e) {
                sendResponse(exchange, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}", 400);
            }
        }
    }

    class AnaliticaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                int totalInm = sistema.obtenerCatalogoLista().getSize();
                int totalCli = sistema.obtenerClientesLista().getSize();
                int totalOps = sistema.obtenerOperacionesLista().getSize();
                
                String json = String.format("{\"inmuebles\":%d, \"clientes\":%d, \"operaciones\":%d}", totalInm, totalCli, totalOps);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                sendResponse(exchange, json, 200);
            } catch (Exception e) {
                sendResponse(exchange, "{\"status\":\"error\"}", 400);
            }
        }
    }

    class AlertasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<String> alertas = sistema.generarAlertasSistema();
            String response = JsonUtil.listToJson(alertas, s -> "\"" + s + "\"");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class AuditoriaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sistema.ejecutarAuditoriaComercial(); // Ejecutar motor antes de consultar
            CustomList<EventoAnomalo> log = sistema.obtenerLogAnomalias();
            String response = JsonUtil.listToJson(log, JsonUtil::eventoAnomaloToJson);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class RecomendacionesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            String idCli = p.get("cli");
            if (idCli == null) {
                sendResponse(exchange, "[]", 200);
                return;
            }
            CustomList<Inmueble> recs = sistema.obtenerRecomendacionesHibridas(idCli);
            String response = JsonUtil.listToJson(recs, JsonUtil::inmuebleToJson);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class UndoSnapshotHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String res = sistema.revertirCambioInmueble();
            sendResponse(exchange, "{\"message\":\"" + res + "\"}", 200);
        }
    }

    class UndoAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String res = sistema.deshacerUltimaAccion();
            sendResponse(exchange, "{\"message\":\"" + res + "\"}", 200);
        }
    }

    class VisitaReprogramarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            boolean ok = sistema.reprogramarVisita(p.get("cli"), p.get("cod"), p.get("fec"), p.get("hor"));
            sendResponse(exchange, "{\"status\":\"" + (ok ? "ok" : "error") + "\"}", 200);
        }
    }

    class VisitaCancelarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            boolean ok = sistema.cancelarVisita(p.get("cli"), p.get("cod"));
            sendResponse(exchange, "{\"status\":\"" + (ok ? "ok" : "error") + "\"}", 200);
        }
    }

    class OperacionRenovarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            boolean ok = sistema.renovarContrato(p.get("id"), p.get("fec"), Double.parseDouble(p.get("val")));
            sendResponse(exchange, "{\"status\":\"" + (ok ? "ok" : "error") + "\"}", 200);
        }
    }

    class ReportesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Generar un JSON con reportes por zona y cierres
            CustomHashTable<String, Integer> visitasZona = sistema.getVisitasPorZona();
            CustomList<Operacion> ops = sistema.obtenerOperacionesLista();
            
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"zonas\":[");
            CustomList<String> keys = visitasZona.keys();
            for (int i = 0; i < keys.getSize(); i++) {
                String k = keys.get(i);
                sb.append(String.format("{\"zona\":\"%s\", \"visitas\":%d}", k, visitasZona.get(k)));
                if (i < keys.getSize() - 1) sb.append(",");
            }
            sb.append("],");
            sb.append("\"cierres\":").append(ops.getSize());
            sb.append("}");
            
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, sb.toString(), 200);
        }
    }

    class SimulacionDemandaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            String zona = p.get("zon");
            int meses = p.get("mes") != null ? Integer.parseInt(p.get("mes")) : 12;
            double crecimiento = sistema.simularCrecimientoDemanda(zona, meses);
            sendResponse(exchange, String.format(java.util.Locale.US, "{\"zona\":\"%s\", \"crecimiento\":%.2f}", zona, crecimiento), 200);
        }
    }

    class RankingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<Asesor> asesores = sistema.obtenerRankingAsesoresEfectividad();
            CustomList<String> zonas = sistema.obtenerRankingZonasActividad();
            CustomList<Cliente> vips = sistema.obtenerClientesAltaProbabilidad();
            
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"asesores\":").append(JsonUtil.listToJson(asesores, JsonUtil::asesorToJson)).append(",");
            sb.append("\"zonas\":").append(JsonUtil.listToJson(zonas, s -> "\"" + s + "\"")).append(",");
            sb.append("\"vips\":").append(JsonUtil.listToJson(vips, JsonUtil::clienteToJson));
            sb.append("}");
            
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, sb.toString(), 200);
        }
    }

    class ClienteFavoritosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            Cliente c = sistema.buscarCliente(p.get("id"));
            if (c != null) {
                String response = JsonUtil.listToJson(c.getFavoritos(), JsonUtil::inmuebleToJson);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                sendResponse(exchange, response, 200);
            } else {
                sendResponse(exchange, "[]", 200);
            }
        }
    }

    class ClienteFavoritosAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            sistema.agregarAFavoritos(p.get("cli"), p.get("cod"));
            sendResponse(exchange, "{\"status\":\"ok\"}", 200);
        }
    }

    class ClienteFavoritosRemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            sistema.removerDeFavoritos(p.get("cli"), p.get("cod"));
            sendResponse(exchange, "{\"status\":\"ok\"}", 200);
        }
    }

    class ClienteVisitasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            Cliente c = sistema.buscarCliente(p.get("id"));
            if (c != null) {
                String response = JsonUtil.listToJson(c.getHistorialVisitas(), JsonUtil::visitaToJson);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                sendResponse(exchange, response, 200);
            } else {
                sendResponse(exchange, "[]", 200);
            }
        }
    }
}
