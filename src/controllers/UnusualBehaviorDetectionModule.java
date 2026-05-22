package controllers;

import models.*;
import structures.*;
import java.time.LocalDate;

public class UnusualBehaviorDetectionModule {
    
    private PropTechSystem sistema;
    private CustomPriorityQueue<EventoAnomalo> alertasPrioritarias;

    public UnusualBehaviorDetectionModule(PropTechSystem sistema) {
        this.sistema = sistema;
        this.alertasPrioritarias = new CustomPriorityQueue<>(100);
    }

    public CustomPriorityQueue<EventoAnomalo> getAlertasPrioritarias() {
        return alertasPrioritarias;
    }

    private int getPrioridadInt(String nivel) {
        if (nivel == null) return 0;
        String n = nivel.toUpperCase();
        if (n.equals("HIGH") || n.equals("ALTO")) return 4;
        if (n.equals("MEDIUM-HIGH") || n.equals("MEDIO-ALTO")) return 3;
        if (n.equals("MEDIUM") || n.equals("MEDIO")) return 2;
        if (n.equals("LOW") || n.equals("BAJO")) return 1;
        return 0;
    }

    public void runDetection() {
        String hoy = LocalDate.now().toString();
        
        CustomList<Inmueble> inmuebles = sistema.obtenerCatalogoLista();
        CustomList<Cliente> clientes = sistema.obtenerClientesLista();
        CustomList<Asesor> asesores = sistema.obtenerAsesoresLista();
        CustomList<Operacion> operaciones = sistema.obtenerOperacionesLista();

        for (int i = 0; i < inmuebles.getSize(); i++) {
            Inmueble inm = inmuebles.get(i);
            Integer visitas = sistema.getVisitasPorInmueble().get(inm.getCodigo());
            if (visitas != null && visitas >= 20 && "Disponible".equals(inm.getDisponibilidad())) {
                EventoAnomalo e = new EventoAnomalo("High Traffic No Closing", "Property " + inm.getCodigo() + " has " + visitas + " visits without closing.", "HIGH", hoy, inm.getCodigo());
                alertasPrioritarias.enqueue(e, getPrioridadInt("HIGH"));
            }
        }

        for (int i = 0; i < clientes.getSize(); i++) {
            Cliente c = clientes.get(i);
            CustomList<Visita> hist = c.getHistorialVisitas();
            if (hist.getSize() > 8) {
                boolean tieneCierre = false;
                for (int j = 0; j < operaciones.getSize(); j++) {
                    if (operaciones.get(j).getCliente().getIdentificacion().equals(c.getIdentificacion())) {
                        tieneCierre = true;
                        break;
                    }
                }
                if (!tieneCierre) {
                    EventoAnomalo e = new EventoAnomalo("Compulsive Scheduling", "Client " + c.getNombre() + " scheduled " + hist.getSize() + " visits without closing any.", "MEDIUM", hoy, c.getIdentificacion());
                    alertasPrioritarias.enqueue(e, getPrioridadInt("MEDIUM"));
                }
            }
        }

        if (asesores.getSize() > 0) {
            int totalPendientes = 0;
            for (int i = 0; i < asesores.getSize(); i++) {
                totalPendientes += asesores.get(i).getVisitasPendientes().getSize();
            }
            double average = (double) totalPendientes / asesores.getSize();
            
            for (int i = 0; i < asesores.getSize(); i++) {
                Asesor a = asesores.get(i);
                int pending = a.getVisitasPendientes().getSize();
                if (average > 0 && pending > (average * 2.5)) {
                    EventoAnomalo e = new EventoAnomalo("Advisor Overload", "Advisor " + a.getNombre() + " has " + pending + " visits (average: " + String.format("%.1f", average) + ").", "HIGH", hoy, a.getIdentificacion());
                    alertasPrioritarias.enqueue(e, getPrioridadInt("HIGH"));
                }
            }
        }

        for (int i = 0; i < inmuebles.getSize(); i++) {
            Inmueble inm = inmuebles.get(i);
            if (inm.getCambiosPrecioCount() >= 5) {
                EventoAnomalo e = new EventoAnomalo("Frequent Price Change", "Property " + inm.getCodigo() + " changed price " + inm.getCambiosPrecioCount() + " times.", "MEDIUM", hoy, inm.getCodigo());
                alertasPrioritarias.enqueue(e, getPrioridadInt("MEDIUM"));
            }
        }

        CustomHashTable<String, Integer> visitasZona = sistema.getVisitasPorZona();
        CustomList<String> zonas = visitasZona.keys();
        int totalConsultas = 0;
        for (int i = 0; i < zonas.getSize(); i++) {
            totalConsultas += visitasZona.get(zonas.get(i));
        }
        double avgZone = zonas.getSize() > 0 ? (double) totalConsultas / zonas.getSize() : 0;
        
        for (int i = 0; i < zonas.getSize(); i++) {
            String zona = zonas.get(i);
            int count = visitasZona.get(zona);
            if (avgZone > 0 && count > (avgZone * 5)) {
                EventoAnomalo e = new EventoAnomalo("Abnormal Zone Interest", "Zone " + zona + " received " + count + " consultations (average: " + String.format("%.1f", avgZone) + ").", "HIGH", hoy, zona);
                alertasPrioritarias.enqueue(e, getPrioridadInt("HIGH"));
            }
        }

        for (int i = 0; i < clientes.getSize(); i++) {
            Cliente c = clientes.get(i);
            if (c.getHistorialVisitas().getSize() >= 5) {
                boolean tieneCierre = false;
                for (int j = 0; j < operaciones.getSize(); j++) {
                    if (operaciones.get(j).getCliente().getIdentificacion().equals(c.getIdentificacion())) {
                        tieneCierre = true;
                        break;
                    }
                }
                if (!tieneCierre) {
                    EventoAnomalo e = new EventoAnomalo("Frustration Pattern", "Client " + c.getNombre() + " visited " + c.getHistorialVisitas().getSize() + " similar properties without closing.", "MEDIUM-HIGH", hoy, c.getIdentificacion());
                    alertasPrioritarias.enqueue(e, getPrioridadInt("MEDIUM-HIGH"));
                }
            }
        }

        for (int i = 0; i < inmuebles.getSize(); i++) {
            Inmueble inm = inmuebles.get(i);
            if ("Reservado".equalsIgnoreCase(inm.getDisponibilidad())) {
                EventoAnomalo e = new EventoAnomalo("Long Time Reserved", "Property " + inm.getCodigo() + " has been reserved for a long time without closing.", "HIGH", hoy, inm.getCodigo());
                alertasPrioritarias.enqueue(e, getPrioridadInt("HIGH"));
            }
        }
    }
}
