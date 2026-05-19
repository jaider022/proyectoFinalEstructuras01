package servicios_ia;

public class AnalizadorSentimiento {

    /**
     * Analiza el sentimiento de un texto dado (ej. comentario de una visita).
     * @param comentario El texto a analizar.
     * @return POSITIVO, NEGATIVO o NEUTRAL.
     */
    public static String analizar(String comentario) {
        if (comentario == null || comentario.trim().isEmpty()) {
            return "NEUTRAL"; // Si no hay comentario, es neutral
        }

        String texto = comentario.toLowerCase();
        
        // Diccionario simple de palabras positivas
        String[] palabrasPositivas = {
            "excelente", "bueno", "bonito", "hermoso", "amplio", 
            "iluminado", "puntual", "amable", "me gusta", "perfecto", 
            "limpio", "recomendado", "espectacular", "increíble", "bien"
        };
        
        // Diccionario simple de palabras negativas
        String[] palabrasNegativas = {
            "malo", "feo", "pequeño", "oscuro", "tarde", 
            "grosero", "sucio", "caro", "ruido", "pésimo", 
            "horrible", "dañado", "incompleto", "terrible", "mal"
        };

        int puntajePositivo = 0;
        int puntajeNegativo = 0;

        // Contar ocurrencias positivas
        for (String palabra : palabrasPositivas) {
            if (texto.contains(palabra)) {
                puntajePositivo++;
            }
        }

        // Contar ocurrencias negativas
        for (String palabra : palabrasNegativas) {
            if (texto.contains(palabra)) {
                puntajeNegativo++;
            }
        }

        // Determinar el resultado basado en los pesos
        if (puntajePositivo > puntajeNegativo) {
            return "POSITIVO";
        } else if (puntajeNegativo > puntajePositivo) {
            return "NEGATIVO";
        } else {
            return "NEUTRAL";
        }
    }
}
