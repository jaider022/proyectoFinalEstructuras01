package servicios_ia;

import models.Inmueble;

public class PredictorPrecio {

    /**
     * Estima el precio de un inmueble usando un algoritmo heurístico (Simulación de IA).
     * @param inmueble El inmueble a evaluar.
     * @return El precio estimado.
     */
    public static double estimar(Inmueble inmueble) {
        if (inmueble == null) return 0.0;

        // Precios base por metro cuadrado (valores de ejemplo)
        double precioBasePorMetro = 3000000; // 3 millones por metro cuadrado para Venta
        if (inmueble.getFinalidad() != null && inmueble.getFinalidad().equalsIgnoreCase("arriendo")) {
            precioBasePorMetro = 25000; // 25 mil pesos por metro cuadrado para Arriendo
        }

        // Factor por zona (la ubicación impacta muchísimo el precio)
        double multiplicadorZona = 1.0;
        if (inmueble.getZona() != null) {
            String zona = inmueble.getZona().toLowerCase();
            if (zona.contains("norte") || zona.contains("colina") || zona.contains("rosales") || zona.contains("chico")) {
                multiplicadorZona = 1.4;
            } else if (zona.contains("chapinero") || zona.contains("centro") || zona.contains("teusaquillo")) {
                multiplicadorZona = 1.15;
            } else if (zona.contains("sur")) {
                multiplicadorZona = 0.8;
            }
        }

        // Factor por estado físico
        double multiplicadorEstado = 1.0;
        if (inmueble.getEstadoFisico() != null) {
            String estado = inmueble.getEstadoFisico().toLowerCase();
            if (estado.equals("nuevo") || estado.equals("sobre planos")) {
                multiplicadorEstado = 1.2;
            } else if (estado.equals("remodelado")) {
                multiplicadorEstado = 1.1;
            } else if (estado.equals("usado")) {
                multiplicadorEstado = 0.95;
            } else if (estado.equals("en remodelación")) {
                multiplicadorEstado = 0.85;
            }
        }

        // Cálculo base principal: Area * Precio por Metro
        double valorEstimado = inmueble.getArea() * precioBasePorMetro;

        // Añadir valor extra (plusvalía) por habitaciones y baños
        valorEstimado += (inmueble.getHabitaciones() * (precioBasePorMetro * 1.5));
        valorEstimado += (inmueble.getBanos() * (precioBasePorMetro * 1.2));

        // Si tiene área de terreno (como una casa), eso suma mucho valor
        if (inmueble.getAreaTerreno() > inmueble.getArea()) {
            valorEstimado += ((inmueble.getAreaTerreno() - inmueble.getArea()) * (precioBasePorMetro * 0.5));
        }

        // Aplicar los multiplicadores finales
        valorEstimado = valorEstimado * multiplicadorZona * multiplicadorEstado;

        // Retornar redondeando a los 50.000 más cercanos para que no queden números extraños
        return Math.round(valorEstimado / 50000.0) * 50000.0;
    }
}
