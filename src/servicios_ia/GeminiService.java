package servicios_ia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Servicio encargado de la comunicación con la API de Google Gemini (1.5 Flash).
 * Permite delegar preguntas conversacionales y complejas al modelo generativo.
 */
public class GeminiService {
    
    // TODO: Reemplaza esto con tu API Key real de Google AI Studio
    // Puedes obtener una gratis en: https://aistudio.google.com/
    private static final String API_KEY = "AIzaSyAzZ1Vf_rUiGXwpG-3hZssk6QnruheqKsw"; 
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private final HttpClient httpClient;

    public GeminiService() {
        // Inicializamos el HttpClient con un timeout de 10 segundos para no bloquear el hilo principal indefinidamente
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Envía un mensaje a la API de Gemini y retorna su respuesta en formato de texto plano.
     * @param promptUsuario El texto escrito por el usuario.
     * @return La respuesta generada por la IA.
     */
    public String enviarMensaje(String promptUsuario) {
        // Si el usuario no ha configurado su API Key, devolvemos un mensaje informativo útil
        if ("TU_GEMINI_API_KEY_AQUI".equals(API_KEY) || API_KEY.trim().isEmpty()) {
            return "🤖 [Asistente Virtual]: La conexión con Gemini está configurada pero aún no has colocado tu API Key en la clase GeminiService.java (línea 14).";
        }

        try {
            // Instrucción del sistema para definir la personalidad de la IA
            String systemInstruction = "Eres un asistente virtual experto de la plataforma inmobiliaria PropTech. "
                    + "Tu objetivo es ayudar a clientes y asesores a resolver dudas generales, dar consejos sobre inmuebles "
                    + "y mantener una conversación amable. Tus respuestas deben ser cordiales, profesionales, "
                    + "en español, relativamente cortas (máximo 3 párrafos) y sin rodeos técnicos innecesarios.";

            // Escapamos comillas dobles y saltos de línea para que el JSON sea válido
            String mensajeEscapado = escaparJson(promptUsuario);
            String instruccionEscapada = escaparJson(systemInstruction);

            // Payload en formato JSON requerido por la API de Gemini
            String jsonPayload = """
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }],
              "systemInstruction": {
                "parts": [{"text": "%s"}]
              }
            }
            """.formatted(mensajeEscapado, instruccionEscapada);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extraerTextoDeJson(response.body());
            } else {
                return "🤖 [Asistente Virtual]: No pude obtener respuesta de la IA (Código de error HTTP: " + response.statusCode() + "). Verifique su API Key.";
            }

        } catch (java.net.http.HttpConnectTimeoutException e) {
            return "🤖 [Asistente Virtual]: Se agotó el tiempo de espera al conectar con el servidor de la IA. Por favor, intente de nuevo.";
        } catch (Exception e) {
            e.printStackTrace();
            return "🤖 [Asistente Virtual]: Hubo un problema al procesar la respuesta de la IA (" + e.getMessage() + ").";
        }
    }

    /**
     * Limpia los caracteres especiales para evitar romper el formato JSON del cuerpo de la petición.
     */
    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * Analiza de manera simple el JSON de respuesta de la API de Gemini para extraer el texto de la respuesta.
     * Estructura del JSON de Gemini: candidates[0].content.parts[0].text
     */
    private String extraerTextoDeJson(String jsonResponse) {
        try {
            // Buscamos la primera aparición de la clave "text": "
            int indexStart = jsonResponse.indexOf("\"text\": \"");
            if (indexStart != -1) {
                int start = indexStart + 9;
                
                // Buscamos el final de la cadena de texto, teniendo cuidado de no parar en una comilla escapada (\")
                int end = start;
                while (end < jsonResponse.length()) {
                    char c = jsonResponse.charAt(end);
                    if (c == '"' && jsonResponse.charAt(end - 1) != '\\') {
                        break;
                    }
                    end++;
                }
                
                String textoExtraido = jsonResponse.substring(start, end);
                
                // Desescapamos los caracteres del JSON para la presentación final en pantalla
                return textoExtraido
                        .replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
        } catch (Exception e) {
            // Fallback si falla el parseo
        }
        return "🤖 [Asistente Virtual]: Recibí una respuesta inesperada de la IA que no pude interpretar.";
    }
}
