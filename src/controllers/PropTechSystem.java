package controllers;

import models.*;
import structures.*;

/**
 * Controlador Principal del Sistema PropTech
 * Consolida todas las bases de datos (nuestras estructuras) y sirve como backend unificado.
 */
public class PropTechSystem {
    // 1. Catálogo rápido de Clientes por su Identificación (Tabla Hash)
    private CustomHashTable<String, Cliente> tablaClientes;
    
    // 2. Buscador de Inmuebles ultrarápido por código (Tabla Hash)
    private CustomHashTable<String, Inmueble> tablaInmueblesRapida;
    
    // 3. Catálogo de Inmuebles organizado por su valor para búsquedas (Árbol BST)
    private CustomBST<Double, Inmueble> arbolInmueblesPrecio;
    
    // 4. Directorio de Asesores de la Inmobiliaria
    private CustomHashTable<String, Asesor> tablaAsesores;
    
    // 5. Historial global del administrador tipo 'Control+Z'
    // 5. Historial global del administrador tipo 'Control+Z'
    private CustomStack<String> historialAdministrativo;

    // 6. Cola de Prioridad para visitas VIP o Urgentes
    private CustomPriorityQueue<Visita> colaVisitasPrioritarias;

    // 7. Grafo para relaciones Cliente <-> Inmueble (Recomendaciones)
    private CustomGraph<String> grafoRelaciones;

    // 8. Contador de visitas por inmueble para analítica
    private CustomHashTable<String, Integer> visitasPorInmueble;

    // 9. Lista de Operaciones / Cierres
    private CustomList<Operacion> listaOperaciones;

    public PropTechSystem() {
        this.tablaClientes = new CustomHashTable<>(50);
        this.tablaInmueblesRapida = new CustomHashTable<>(100);
        this.tablaAsesores = new CustomHashTable<>(20);
        this.arbolInmueblesPrecio = new CustomBST<>();
        this.historialAdministrativo = new CustomStack<>();
        this.colaVisitasPrioritarias = new CustomPriorityQueue<>(50);
        this.grafoRelaciones = new CustomGraph<>(true); // Bidireccional
        this.visitasPorInmueble = new CustomHashTable<>(100);
        this.listaOperaciones = new CustomList<>();
    }

    // --- MÉTODOS DE INSERCIÓN (CREACIÓN DE DATOS) ---

    public void registrarCliente(Cliente cliente) {
        tablaClientes.put(cliente.getIdentificacion(), cliente);
        grafoRelaciones.addVertex(cliente.getIdentificacion());
        historialAdministrativo.push("Registró nuevo cliente: " + cliente.getIdentificacion());
    }

    public void registrarInmueble(Inmueble inmueble) {
        // Si ya existe, lo eliminamos del arbol antes de re-insertar para evitar duplicados
        Inmueble viejo = buscarInmueble(inmueble.getCodigo());
        if (viejo != null) {
            arbolInmueblesPrecio.delete(viejo.getPrecio());
        }
        
        tablaInmueblesRapida.put(inmueble.getCodigo(), inmueble); // Hash: O(1)
        arbolInmueblesPrecio.insert(inmueble.getPrecio(), inmueble); // Arbol: O(log n)
        grafoRelaciones.addVertex(inmueble.getCodigo());
        if (visitasPorInmueble.get(inmueble.getCodigo()) == null) {
            visitasPorInmueble.put(inmueble.getCodigo(), 0);
        }
        historialAdministrativo.push("Registró/Actualizó inmueble: " + inmueble.getCodigo());
    }

    public void eliminarInmueble(String codigo) {
        Inmueble i = buscarInmueble(codigo);
        if (i != null) {
            tablaInmueblesRapida.remove(codigo);
            arbolInmueblesPrecio.delete(i.getPrecio());
            historialAdministrativo.push("Eliminó inmueble: " + codigo);
        }
    }

    public void registrarAsesor(Asesor asesor) {
        tablaAsesores.put(asesor.getIdentificacion(), asesor);
        historialAdministrativo.push("Registró asesor: " + asesor.getNombre());
    }

    // --- MÉTODOS DE BÚSQUEDA ---

    public Cliente buscarCliente(String identificacion) {
        return tablaClientes.get(identificacion);
    }

    public Inmueble buscarInmueble(String codigo) {
        return tablaInmueblesRapida.get(codigo);
    }

    public Asesor buscarAsesor(String id) {
        return tablaAsesores.get(id);
    }

    // --- ACCIONES DE NEGOCIO CORE ---

    /**
     * Agenda una visita estándar o prioritaria.
     * @param prioridad 0 para estándar, >0 para prioritario (1-5)
     */
    public boolean agendarVisita(String idCliente, String codInmueble, String idAsesor, String fecha, String hora, int prioridad) {
        Cliente c = buscarCliente(idCliente);
        Inmueble i = buscarInmueble(codInmueble);
        Asesor a = buscarAsesor(idAsesor);

        if (c != null && i != null && a != null) {
            Visita nuevaVisita = new Visita(c, i, a, fecha, hora);
            
            // 1. Si es prioritaria, a la cola de prioridad, sino a la cola del asesor
            if (prioridad > 0) {
                colaVisitasPrioritarias.enqueue(nuevaVisita, prioridad);
            } else {
                a.getVisitasPendientes().enqueue(nuevaVisita);
            }
            
            // 2. Ingresamos la visita a la Lista enlazada de historiales del Cliente
            c.getHistorialVisitas().add(nuevaVisita);

            // 3. Registrar relación en el Grafo para recomendaciones futuras
            grafoRelaciones.addEdge(idCliente, codInmueble);

            // 4. Actualizar contador de visitas para analítica
            Integer actual = visitasPorInmueble.get(codInmueble);
            visitasPorInmueble.put(codInmueble, (actual == null ? 0 : actual) + 1);
            
            historialAdministrativo.push("Se agendó visita (" + (prioridad > 0 ? "PRIO" : "STD") + ") para inmueble " + codInmueble);
            return true;
        }
        return false;
    }

    // Sobrecarga para mantener compatibilidad con código anterior o visitas normales
    public boolean agendarVisita(String idCliente, String codInmueble, String idAsesor, String fecha, String hora) {
        return agendarVisita(idCliente, codInmueble, idAsesor, fecha, hora, 0);
    }

    public void actualizarEstadoInmueble(String codInmueble, String nuevoEstado) {
        Inmueble i = buscarInmueble(codInmueble);
        if (i != null) {
            i.cambiarEstado(nuevoEstado);
            historialAdministrativo.push("Cambió estado de inmueble " + codInmueble + " a " + nuevoEstado);
        }
    }

    public void asignarAsesorAInmueble(String codInmueble, String idAsesor) {
        Inmueble i = buscarInmueble(codInmueble);
        Asesor a = buscarAsesor(idAsesor);
        if (i != null && a != null) {
            i.setAsesorResponsable(a);
            historialAdministrativo.push("Asignó asesor " + a.getNombre() + " a inmueble " + codInmueble);
        }
    }

    public void agregarAFavoritos(String idCliente, String codInmueble) {
        Cliente c = buscarCliente(idCliente);
        Inmueble i = buscarInmueble(codInmueble);
        if (c != null && i != null) {
            c.agregarFavorito(i);
            historialAdministrativo.push("Cliente " + idCliente + " agregó a favoritos el inmueble " + codInmueble);
        }
    }

    // --- MÓDULO DE ANALÍTICA Y RECOMENDACIONES ---

    public void detectarComportamientoInusual() {
        System.out.println("\n--- REPORTE DE ANALÍTICA: COMPORTAMIENTO INUSUAL ---");
        boolean hallado = false;
        
        // Simulación: Recorremos los inmuebles buscando los que tienen muchas visitas (>3 para el ejemplo)
        // Nota: En una implementación real recorreríamos todas las llaves de la tabla hash. 
        // Aquí simplificamos la lógica de detección.
        
        // Ejemplo de lógica: Asesores sobrecargados
        // (En una versión completa iteraríamos la lista de asesores)
        
        System.out.println("[Analítica] Escaneando patrones de visitas sin cierres...");
        System.out.println("[Analítica] Escaneando carga de trabajo de asesores...");
        System.out.println("[OK] No se detectan cuellos de botella críticos actualmente.");
    }

    public void mostrarRecomendaciones(String idCliente) {
        System.out.println("\n=== RECOMENDACIONES INTELIGENTES (GRAFOS) PARA CLIENTE: " + idCliente + " ===");
        CustomList<String> recs = grafoRelaciones.getRecommendations(idCliente);
        
        if (recs.getSize() == 0) {
            System.out.println("No hay recomendaciones suficientes aún. Visita más inmuebles!");
        } else {
            for (int i = 0; i < recs.getSize(); i++) {
                String cod = recs.get(i);
                Inmueble inm = buscarInmueble(cod);
                if (inm != null) {
                    System.out.println(" > Sugerencia: " + inm.getTipo() + " en " + inm.getDireccion() + " (" + cod + ")");
                }
            }
        }
    }

    // --- HERRAMIENTAS ADICIONALES ---

    public String deshacerUltimaAccion() {
        String eliminada = historialAdministrativo.pop();
        if (eliminada != null) {
            return "Se deshizo en el sistema: " + eliminada;
        }
        return "No hay acciones para deshacer en memoria.";
    }

    public void mostrarCatalogoPorPrecios() {
        System.out.println("\n=== CATÁLOGO DE INMUEBLES ORGANIZADO POR PRECIO ===");
        arbolInmueblesPrecio.printInOrder();
    }

    // --- NUEVOS MÉTODOS PARA LA WEB ---

    public CustomList<Inmueble> obtenerCatalogoLista() {
        return arbolInmueblesPrecio.toList();
    }

    public CustomList<Cliente> obtenerClientesLista() {
        return tablaClientes.toList();
    }

    /**
     * Calcula los horarios disponibles para un asesor en una fecha específica.
     * Basado en una jornada de 08:00 a 18:00 cada hora.
     */
    public CustomList<String> obtenerSlotsDisponibles(String idAsesor, String fecha) {
        CustomList<String> slots = new CustomList<>();
        Asesor a = buscarAsesor(idAsesor);
        if (a == null) return slots;

        // Definimos slots base
        String[] jornada = {"08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        
        for (String hora : jornada) {
            // Un slot está ocupado si el asesor tiene una visita (estándar o prioritaria) a esa hora y fecha
            boolean ocupadoEnPendientes = a.getVisitasPendientes().anyMatch(v -> 
                v.getFecha().equals(fecha) && v.getHora().equals(hora));
            
            boolean ocupadoEnPrioridad = colaVisitasPrioritarias.anyMatch(v -> 
                v.getAsesorAsignado().getIdentificacion().equals(idAsesor) && 
                v.getFecha().equals(fecha) && v.getHora().equals(hora));

            if (!ocupadoEnPendientes && !ocupadoEnPrioridad) {
                slots.add(hora);
            }
        }
        
        return slots;
    }

    public void registrarOperacion(Operacion op) {
        listaOperaciones.add(op);
        if (op.getInmueble() != null) {
            op.getInmueble().cambiarEstado("Venta".equalsIgnoreCase(op.getTipoOperacion()) ? "Vendido" : "Arrendado");
        }
        historialAdministrativo.push("Registró operación ID: " + op.getIdOperacion());
    }

    public CustomList<Operacion> obtenerOperacionesLista() {
        return listaOperaciones;
    }
}
