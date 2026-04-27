package controllers;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import structures.JsonUtil;
import structures.CustomList;
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
        server.createContext("/api/inmuebles/add", new InmuebleAddHandler());
        server.createContext("/api/inmuebles/delete", new InmuebleDeleteHandler());
        server.createContext("/api/clientes", new ClientesHandler());
        server.createContext("/api/clientes/register", new ClienteRegisterHandler());
        server.createContext("/api/visitas/agendar", new VisitaAgendarHandler());
        server.createContext("/api/visitas/slots", new VisitaSlotsHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/operaciones/add", new OperacionAddHandler());
        server.createContext("/api/analitica", new AnaliticaHandler());

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
                
                exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
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
                Inmueble n = new Inmueble(p.get("cod"), p.get("dir"), p.get("ciu"), p.get("zon"), p.get("tip"), p.get("fin"),
                    Double.parseDouble(p.get("pre")), Double.parseDouble(p.get("are")), Double.parseDouble(p.get("areT")),
                    Integer.parseInt(p.get("hab")), Integer.parseInt(p.get("ban")), p.get("est"));
                
                String ftsStr = p.get("fts");
                if (ftsStr != null && !ftsStr.isEmpty()) {
                    // Usamos el nuevo separador |SEP| para no romper el Base64
                    for (String url : ftsStr.split("\\|SEP\\|")) {
                        if (!url.trim().isEmpty()) n.getFotos().add(url.trim());
                    }
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
                Cliente c = new Cliente(p.get("id"), p.get("nom"), p.get("cor"), p.get("tel"), p.get("tip"), 
                    Double.parseDouble(p.get("pre")), p.get("zon"), p.get("itm"));
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

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.util.Map<String, String> p = getQueryParams(exchange.getRequestURI().getQuery());
            String pwd = p.get("pwd");
            if ("admin123".equals(pwd)) {
                sendResponse(exchange, "{\"status\":\"ok\"}", 200);
            } else {
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                sendResponse(exchange, "{\"status\":\"error\", \"message\":\"Clave incorrecta\"}", 401);
            }
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
}
