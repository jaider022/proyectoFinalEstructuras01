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

    // 3b. Árboles adicionales (Req 5.6)
    private CustomBST<Double, Cliente> arbolClientesPresupuesto;   // Clientes por presupuesto
    private CustomBST<Integer, Asesor> arbolAsesoresCierres;       // Asesores por cierres
    private CustomBST<Integer, Asesor> arbolAsesoresCarga;         // Asesores por carga (visitas)
    
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
    // 8b. Contador de visitas por zona (Req 5.5)
    private CustomHashTable<String, Integer> visitasPorZona;

    // 9. Lista de Operaciones / Cierres
    private CustomList<Operacion> listaOperaciones;
    private CustomList<EventoAnomalo> logAnomalias;

    // 10. Pila de Instantáneas para DESHACER cambios de inmuebles (5.2)
    private CustomStack<String[]> pilaSnapshotsInmueble;

    // 11. COLAS DEL SISTEMA (Req 5.3)
    private CustomQueue<String> colaSolicitudesClientes;  // Solicitudes de atención FIFO
    private CustomQueue<String> colaTareasAdministrativas; // Tareas pendientes del admin
    private CustomQueue<String> colaAlertasPendientes;     // Alertas sin revisar

    // 12. COLAS DE PRIORIDAD ESPECIALIZADAS (Req 5.4)
    private CustomPriorityQueue<Operacion> colaContratosPorVencer; // Alertas de vencimiento crítico
    private CustomPriorityQueue<Cliente>   colaClientesInteres;    // Clientes con alta intención de cierre
    private CustomPriorityQueue<Inmueble>  colaDemandaInmuebles;   // Inmuebles más demandados

    public PropTechSystem() {
        this.tablaClientes = new CustomHashTable<>(50);
        this.tablaInmueblesRapida = new CustomHashTable<>(100);
        this.tablaAsesores = new CustomHashTable<>(20);
        this.arbolInmueblesPrecio = new CustomBST<>();
        this.arbolClientesPresupuesto = new CustomBST<>();
        this.arbolAsesoresCierres = new CustomBST<>();
        this.arbolAsesoresCarga   = new CustomBST<>();
        this.historialAdministrativo = new CustomStack<>();
        this.colaVisitasPrioritarias = new CustomPriorityQueue<>(50);
        this.grafoRelaciones = new CustomGraph<>(true); // Bidireccional
        this.visitasPorInmueble = new CustomHashTable<>(100);
        this.visitasPorZona     = new CustomHashTable<>(50);
        this.listaOperaciones = new CustomList<>();
        this.logAnomalias = new CustomList<>();
        this.pilaSnapshotsInmueble = new CustomStack<>();
        this.colaSolicitudesClientes = new CustomQueue<>();
        this.colaTareasAdministrativas = new CustomQueue<>();
        this.colaAlertasPendientes = new CustomQueue<>();
        this.colaContratosPorVencer = new CustomPriorityQueue<>(50);
        this.colaClientesInteres    = new CustomPriorityQueue<>(50);
        this.colaDemandaInmuebles   = new CustomPriorityQueue<>(100);
    }

    // --- MÉTODOS DE INSERCIÓN (CREACIÓN DE DATOS) ---

    public void registrarCliente(Cliente cliente) {
        tablaClientes.put(cliente.getIdentificacion(), cliente);
        grafoRelaciones.addVertex(cliente.getIdentificacion());
        arbolClientesPresupuesto.insert(cliente.getPresupuesto(), cliente);
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
        arbolAsesoresCierres.insert(asesor.getCierresRealizados(), asesor);
        arbolAsesoresCarga.insert(asesor.getVisitasPendientes().getSize(), asesor);
        historialAdministrativo.push("Registró/Actualizó asesor: " + asesor.getNombre());
    }

    public void eliminarAsesor(String id) {
        Asesor a = buscarAsesor(id);
        if (a != null) {
            tablaAsesores.remove(id);
            // También deberíamos remover de los árboles si fuera una implementación estricta, 
            // pero para este MVP el Hash es lo principal para el borrado.
            historialAdministrativo.push("Eliminó asesor: " + a.getNombre() + " (" + id + ")");
        }
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

    public CustomList<Asesor> obtenerAsesoresLista() {
        return tablaAsesores.toList();
    }

    public CustomHashTable<String, Integer> getVisitasPorInmueble() {
        return visitasPorInmueble;
    }

    // --- ACCIONES DE NEGOCIO CORE ---

    /**
     * Agenda una visita estándar o prioritaria.
     * @param prioridad 0 para estándar, >0 para prioritario (1-5)
     */
    public boolean agendarVisita(String idCliente, String codInmueble, String idAsesor, String fecha, String hora, int prioridad) {
        // Primero busco los objetos completos usando sus IDs. Si no existen, devuelvo null.
        Cliente c = buscarCliente(idCliente);
        Inmueble i = buscarInmueble(codInmueble);
        Asesor a = buscarAsesor(idAsesor);

        // Verifico que todos los datos sean válidos antes de procesar
        if (c != null && i != null && a != null) {
            // Validar que la fecha y hora no sean del pasado
            try {
                java.time.LocalDate datePart = java.time.LocalDate.parse(fecha);
                java.time.LocalTime timePart = java.time.LocalTime.parse(hora);
                java.time.LocalDateTime appointmentTime = java.time.LocalDateTime.of(datePart, timePart);
                if (appointmentTime.isBefore(java.time.LocalDateTime.now())) {
                    historialAdministrativo.push("RECHAZO: Intento de agendar en fecha/hora pasada: " + fecha + " " + hora);
                    return false;
                }
            } catch (Exception e) {
                return false;
            }

            // REQUERIMIENTO: Solo agendar si el inmueble está Disponible
            if (!"Disponible".equalsIgnoreCase(i.getDisponibilidad())) {
                historialAdministrativo.push("RECHAZO: Intento de agenda en inmueble no disponible: " + codInmueble);
                return false;
            }

            // REQUERIMIENTO: Validar que el horario esté libre (ni el asesor ni el inmueble estén ocupados)
            CustomList<String> disponibles = obtenerSlotsDisponibles(idAsesor, codInmueble, fecha);
            if (disponibles.indexOf(hora) == -1) {
                historialAdministrativo.push("RECHAZO: Cruce de horarios para inmueble " + codInmueble + " o asesor " + idAsesor);
                return false;
            }

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

            // 4b. Actualizar contador de visitas por zona (Req 5.5)
            String zona = i.getZona() != null ? i.getZona().toUpperCase() : "SIN_ZONA";
            Integer actualZona = visitasPorZona.get(zona);
            visitasPorZona.put(zona, (actualZona == null ? 0 : actualZona) + 1);
            i.setFechaUltimaVisita(fecha);
            
            historialAdministrativo.push("Se agendó visita (" + (prioridad > 0 ? "PRIO" : "STD") + ") para inmueble " + codInmueble);
            return true;
        }
        return false;
    }

    // Sobrecarga para mantener compatibilidad con código anterior o visitas normales
    public boolean agendarVisita(String idCliente, String codInmueble, String idAsesor, String fecha, String hora) {
        return agendarVisita(idCliente, codInmueble, idAsesor, fecha, hora, 0);
    }

    public boolean reprogramarVisita(String idCliente, String codInmueble, String nuevaFecha, String nuevaHora) {
        // Validar que la fecha y hora no sean del pasado
        try {
            java.time.LocalDate datePart = java.time.LocalDate.parse(nuevaFecha);
            java.time.LocalTime timePart = java.time.LocalTime.parse(nuevaHora);
            java.time.LocalDateTime appointmentTime = java.time.LocalDateTime.of(datePart, timePart);
            if (appointmentTime.isBefore(java.time.LocalDateTime.now())) {
                historialAdministrativo.push("RECHAZO: Intento de reprogramar en fecha/hora pasada: " + nuevaFecha + " " + nuevaHora);
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        CustomList<Asesor> todosAsesores = tablaAsesores.toList();
        for (int j = 0; j < todosAsesores.getSize(); j++) {
            Asesor a = todosAsesores.get(j);
            CustomList<Visita> visitas = a.getVisitasPendientes().toList();
            for (int i = 0; i < visitas.getSize(); i++) {
                Visita v = visitas.get(i);
                if (v.getCliente().getIdentificacion().equals(idCliente) && v.getInmueble().getCodigo().equals(codInmueble)) {
                    v.setFecha(nuevaFecha);
                    v.setHora(nuevaHora);
                    v.setEstado("reprogramada");
                    historialAdministrativo.push("Visita reprogramada: " + codInmueble + " para " + nuevaFecha);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cancelarVisita(String idCliente, String codInmueble) {
        CustomList<Asesor> todosAsesores = tablaAsesores.toList();
        
        // Recorro a todos los asesores para encontrar quién tiene esta cita
        for (int j = 0; j < todosAsesores.getSize(); j++) {
            Asesor a = todosAsesores.get(j);
            CustomList<Visita> visitas = a.getVisitasPendientes().toList();
            
            // Reviso la agenda específica de cada asesor buscando la visita exacta
            for (int i = 0; i < visitas.getSize(); i++) {
                Visita v = visitas.get(i);
                if (v.getCliente().getIdentificacion().equals(idCliente) && v.getInmueble().getCodigo().equals(codInmueble)) {
                    v.setEstado("cancelada");
                    
                    // Aquí es donde elimino físicamente la cita de la cola (CustomQueue) para limpiar el calendario
                    a.getVisitasPendientes().remove(v); 
                    
                    historialAdministrativo.push("Visita cancelada y removida: " + codInmueble);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean confirmarVisita(String idCliente, String codInmueble) {
        CustomList<Asesor> todosAsesores = tablaAsesores.toList();
        for (int j = 0; j < todosAsesores.getSize(); j++) {
            Asesor a = todosAsesores.get(j);
            CustomList<Visita> visitas = a.getVisitasPendientes().toList();
            for (int i = 0; i < visitas.getSize(); i++) {
                Visita v = visitas.get(i);
                if (v.getCliente().getIdentificacion().equals(idCliente) && v.getInmueble().getCodigo().equals(codInmueble)) {
                    v.setEstado("confirmada");
                    historialAdministrativo.push("Visita confirmada: " + codInmueble + " por cliente " + idCliente);
                    return true;
                }
            }
        }
        return false;
    }

    private Asesor buscarAsesorParaInmueble(String codInmueble) {
        Inmueble i = buscarInmueble(codInmueble);
        return i != null ? i.getAsesorResponsable() : null;
    }

    public Inmueble consultarInmueble(String idCliente, String codInmueble) {
        Cliente c = buscarCliente(idCliente);
        Inmueble i = buscarInmueble(codInmueble);
        
        if (c != null && i != null) {
            c.agregarConsultado(i);
            // Registrar relación en el Grafo (interés leve)
            grafoRelaciones.addEdge(idCliente, codInmueble);
            historialAdministrativo.push("Cliente " + idCliente + " consultó " + codInmueble);
        }
        return i;
    }

    public void descartarInmueble(String idCliente, String codInmueble) {
        Cliente c = buscarCliente(idCliente);
        Inmueble i = buscarInmueble(codInmueble);
        if (c != null && i != null) {
            c.agregarDescartado(i);
            historialAdministrativo.push("Cliente " + idCliente + " descartó " + codInmueble);
        }
    }

    public void reprogramarVisita(Visita v, String nuevaFecha, String nuevaHora) {
        if (v != null) {
            v.setFecha(nuevaFecha);
            v.setHora(nuevaHora);
            v.cambiarEstado("Reprogramada");
            historialAdministrativo.push("Reprogramó visita de inmueble " + v.getInmueble().getCodigo() + " para " + nuevaFecha);
        }
    }

    public void actualizarEstadoInmueble(String codInmueble, String nuevaDisponibilidad) {
        Inmueble i = buscarInmueble(codInmueble);
        if (i != null) {
            i.cambiarDisponibilidad(nuevaDisponibilidad);
            historialAdministrativo.push("Cambió disponibilidad de inmueble " + codInmueble + " a " + nuevaDisponibilidad);
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

    public void removerDeFavoritos(String idCliente, String codInmueble) {
        Cliente c = buscarCliente(idCliente);
        Inmueble i = buscarInmueble(codInmueble);
        if (c != null && i != null) {
            c.removerFavorito(i);
            historialAdministrativo.push("Cliente " + idCliente + " eliminó de favoritos el inmueble " + codInmueble);
        }
    }

    // --- MÓDULO DE ANALÍTICA Y RECOMENDACIONES ---

    public void detectarComportamientoInusual() {
        System.out.println("\n--- REPORTE DE ANALÍTICA: COMPORTAMIENTO INUSUAL ---");
        boolean hallado = false;
        
        System.out.println("[Analítica] Escaneando carga de trabajo de asesores...");
        CustomList<Asesor> asesores = tablaAsesores.toList();
        
        for (int i = 0; i < asesores.getSize(); i++) {
            Asesor asesor = asesores.get(i);
            int carga = asesor.getVisitasPendientes().getSize();
            // Umbral: si tiene más de 3 visitas pendientes, consideramos sobrecarga
            if (carga > 3) {
                System.out.println(" > ALERTA: El asesor " + asesor.getNombre() + " (" + asesor.getIdentificacion() + ") tiene sobrecarga de visitas (" + carga + ").");
                hallado = true;
            }
        }

        if (!hallado) {
            System.out.println("[OK] No se detectan cuellos de botella críticos en los asesores actualmente.");
        }
    }

    public void ejecutarAuditoriaComercial() {
        String hoy = "2026-04-29"; // Fecha simulada del sistema
        
        // 1. Inmuebles con ALTO TRÁFICO pero SIN CIERRE (Estancamiento)
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            Integer v = visitasPorInmueble.get(inm.getCodigo());
            if (v != null && v >= 10 && "Disponible".equals(inm.getDisponibilidad())) {
                logAnomalias.add(new EventoAnomalo("Estancamiento Crítico", 
                    "El inmueble tiene " + v + " visitas sin concretar venta/arriendo.", "ALTO", hoy, inm.getCodigo()));
            }
        }

        // 2. Especulación de Precios
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            if (inm.getCambiosPrecioCount() >= 3) {
                logAnomalias.add(new EventoAnomalo("Especulación de Precios", 
                    "Se han detectado " + inm.getCambiosPrecioCount() + " cambios de precio recientes.", "MEDIO", hoy, inm.getCodigo()));
            }
        }

        // 3. Sobrecarga de Asesores
        CustomList<Asesor> asesores = tablaAsesores.toList();
        for (int i = 0; i < asesores.getSize(); i++) {
            Asesor a = asesores.get(i);
            if (a.getVisitasPendientes().getSize() > 5) {
                logAnomalias.add(new EventoAnomalo("Sobrecarga Operativa", 
                    "El asesor tiene una cola de espera excesiva.", "MEDIO", hoy, a.getIdentificacion()));
            }
        }

        // 4. Clientes con comportamiento errático (Muchas visitas, poca continuidad)
        CustomList<Cliente> clientes = tablaClientes.toList();
        for (int i = 0; i < clientes.getSize(); i++) {
            Cliente c = clientes.get(i);
            if (c.getHistorialVisitas().getSize() >= 5 && "Activo".equals(c.getEstadoBusqueda())) {
                logAnomalias.add(new EventoAnomalo("Cliente Errático", 
                    "Cliente con alto volumen de visitas sin registro de intención.", "BAJO", hoy, c.getIdentificacion()));
            }
        }
    }

    public CustomList<EventoAnomalo> obtenerLogAnomalias() {
        return logAnomalias;
    }

    public CustomList<Operacion> obtenerContratosPorVencer() {
        CustomList<Operacion> contratosPorVencer = new CustomList<>();
        
        for (int i = 0; i < listaOperaciones.getSize(); i++) {
            Operacion op = listaOperaciones.get(i);
            if ("Arriendo".equalsIgnoreCase(op.getTipoOperacion()) && op.getFechaVencimiento() != null) {
                contratosPorVencer.add(op);
            }
        }
        
        return contratosPorVencer;
    }

    public CustomList<String> generarAlertasSistema() {
        CustomList<String> alertas = new CustomList<>();
        
        // 1. Inmuebles sin visitas (Fríos)
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            if (inm.getFechaUltimaVisita() == null && "Disponible".equals(inm.getDisponibilidad())) {
                alertas.add("ALERTA: Inmueble " + inm.getCodigo() + " no ha recibido visitas desde su registro.");
            }
        }

        // 2. Alta Demanda
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            Integer v = visitasPorInmueble.get(inm.getCodigo());
            if (v != null && v >= 5) {
                alertas.add("OPORTUNIDAD: Inmueble " + inm.getCodigo() + " tiene ALTA DEMANDA (" + v + " visitas).");
            }
        }

        // 3. Reservas Estancadas
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            if ("Reservado".equals(inm.getDisponibilidad()) && inm.getFechaReserva() != null) {
                alertas.add("CRÍTICO: Inmueble " + inm.getCodigo() + " está RESERVADO desde " + inm.getFechaReserva() + " sin cierre.");
            }
        }

        // 4. Clientes sin Seguimiento
        CustomList<Cliente> clientes = tablaClientes.toList();
        for (int i = 0; i < clientes.getSize(); i++) {
            Cliente c = clientes.get(i);
            if (c.getFechaUltimoSeguimiento() == null && "Activo".equals(c.getEstadoBusqueda())) {
                alertas.add("SEGUIMIENTO: El cliente " + c.getNombre() + " no tiene registro de contacto reciente.");
            }
        }

        // 5. Visitas Pendientes Críticas
        CustomList<Asesor> asesores = tablaAsesores.toList();
        for (int i = 0; i < asesores.getSize(); i++) {
            Asesor a = asesores.get(i);
            int pendientes = a.getVisitasPendientes().getSize();
            if (pendientes > 0) {
                alertas.add("PENDIENTE: El asesor " + a.getNombre() + " tiene " + pendientes + " visitas por confirmar/realizar.");
            }
        }

        return alertas;
    }

    public void analizarActividadPorZonas() {
        System.out.println("\n=== ANÁLISIS DE ACTIVIDAD COMERCIAL POR ZONAS ===");
        CustomHashTable<String, Integer> conteoZonas = new CustomHashTable<>(50);
        
        for (int i = 0; i < listaOperaciones.getSize(); i++) {
            Operacion op = listaOperaciones.get(i);
            if (op.getInmueble() != null && op.getInmueble().getZona() != null) {
                String zona = op.getInmueble().getZona().toUpperCase();
                Integer conteoActual = conteoZonas.get(zona);
                if (conteoActual == null) conteoActual = 0;
                conteoZonas.put(zona, conteoActual + 1);
            }
        }
        
        CustomList<String> zonasUnicas = new CustomList<>();
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        for (int i = 0; i < todos.getSize(); i++) {
            String z = todos.get(i).getZona().toUpperCase();
            if (zonasUnicas.indexOf(z) == -1) {
                zonasUnicas.add(z);
            }
        }
        
        boolean hayOperaciones = false;
        for (int i = 0; i < zonasUnicas.getSize(); i++) {
            String zona = zonasUnicas.get(i);
            Integer cantidad = conteoZonas.get(zona);
            if (cantidad != null && cantidad > 0) {
                System.out.println(" > Zona " + zona + ": " + cantidad + " operaciones cerradas.");
                hayOperaciones = true;
            }
        }
        
        if (!hayOperaciones) {
            System.out.println("No hay suficiente información de operaciones cerradas para analizar por zonas.");
        }
    }

    public CustomList<Inmueble> obtenerRecomendacionesHibridas(String idCliente) {
        CustomList<Inmueble> sugerencias = new CustomList<>();
        Cliente cliente = buscarCliente(idCliente);
        if (cliente == null) return sugerencias;

        // 1. Capa de Filtros Directos (lo que el cliente busca activamente)
        CustomList<Inmueble> compatibles = filtrarPorPreferencias(idCliente);
        for (int i = 0; i < compatibles.getSize(); i++) {
            sugerencias.add(compatibles.get(i));
        }

        // 2. Capa de Inteligencia de Grafos (lo que otros clientes similares vieron)
        CustomList<String> codigosRecomendados = grafoRelaciones.getRecommendations(idCliente);
        for (int i = 0; i < codigosRecomendados.getSize(); i++) {
            Inmueble inm = buscarInmueble(codigosRecomendados.get(i));
            if (inm != null && sugerencias.indexOf(inm) == -1) {
                // Solo sugerir si está disponible
                if ("Disponible".equalsIgnoreCase(inm.getDisponibilidad())) {
                    sugerencias.add(inm);
                }
            }
        }

        // 3. Capa de Historial (recordar lo que consultó pero no agendó)
        for (int i = 0; i < cliente.getConsultados().getSize(); i++) {
            Inmueble inm = cliente.getConsultados().get(i);
            if (sugerencias.indexOf(inm) == -1 && "Disponible".equalsIgnoreCase(inm.getDisponibilidad())) {
                sugerencias.add(inm);
            }
        }

        // 4. LIMPIEZA FINAL: Eliminar inmuebles que el cliente haya DESCARTADO
        for (int i = 0; i < cliente.getDescartados().getSize(); i++) {
            Inmueble descartado = cliente.getDescartados().get(i);
            sugerencias.remove(descartado);
        }

        return sugerencias;
    }

    public void mostrarRecomendaciones(String idCliente) {
        System.out.println("\n=== RECOMENDACIONES HÍBRIDAS PARA CLIENTE: " + idCliente + " ===");
        CustomList<Inmueble> recs = obtenerRecomendacionesHibridas(idCliente);
        
        if (recs.getSize() == 0) {
            System.out.println("No hay recomendaciones suficientes aún.");
        } else {
            for (int i = 0; i < recs.getSize(); i++) {
                Inmueble inm = recs.get(i);
                System.out.println(" > Sugerencia: " + inm.getTipo() + " en " + inm.getDireccion() + " (" + inm.getCodigo() + ")");
            }
        }
    }

    public CustomList<Inmueble> filtrarPorPreferencias(String idCliente) {
        CustomList<Inmueble> coincidencias = new CustomList<>();
        Cliente cliente = buscarCliente(idCliente);
        
        if (cliente == null) {
            return coincidencias;
        }

        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            
            // La lógica de coincidencia se ha movido a la clase Cliente (prefiere)
            if (cliente.prefiere(inm)) {
                coincidencias.add(inm);
            }
        }
        
        return coincidencias;
    }

    public CustomList<Inmueble> filtrarInmueblesAvanzado(String tipo, String finalidad, int minHabitaciones, int minBanos, double precioMin, double precioMax) {
        CustomList<Inmueble> coincidencias = new CustomList<>();
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            
            // 1. Tipo (ignoramos mayúsculas)
            boolean cumpleTipo = tipo == null || tipo.isEmpty() || tipo.equalsIgnoreCase("todos") || tipo.equalsIgnoreCase(inm.getTipo());
            
            // 1.5 Finalidad (Venta / Arriendo)
            boolean cumpleFinalidad = finalidad == null || finalidad.isEmpty() || finalidad.equalsIgnoreCase("todos") || finalidad.equalsIgnoreCase(inm.getFinalidad());
            
            // 2. Habitaciones y Baños
            boolean cumpleHabitaciones = inm.getHabitaciones() >= minHabitaciones;
            boolean cumpleBanos = inm.getBanos() >= minBanos;
            
            // 3. Rango de Precio
            boolean cumplePrecioMin = precioMin <= 0 || inm.getPrecio() >= precioMin;
            boolean cumplePrecioMax = precioMax <= 0 || inm.getPrecio() <= precioMax;
            
            // 4. Disponibilidad (por defecto solo mostramos disponibles en búsquedas generales de clientes)
            boolean estaDisponible = "Disponible".equalsIgnoreCase(inm.getDisponibilidad());

            if (cumpleTipo && cumpleFinalidad && cumpleHabitaciones && cumpleBanos && cumplePrecioMin && cumplePrecioMax && estaDisponible) {
                coincidencias.add(inm);
            }
        }
        
        return coincidencias;
    }

    public CustomList<Inmueble> obtenerRecomendacionesManuales(String tipo, String ciudad, double precioMax, int minHabitaciones, boolean requiereParqueadero) {
        CustomList<Inmueble> coincidencias = new CustomList<>();
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            
            if (!"Disponible".equalsIgnoreCase(inm.getDisponibilidad())) {
                continue;
            }
            
            boolean cumpleTipo = tipo == null || tipo.isEmpty() || tipo.equalsIgnoreCase("Cualquiera") || tipo.equalsIgnoreCase(inm.getTipo());
            boolean cumpleCiudad = ciudad == null || ciudad.isEmpty() || ciudad.equalsIgnoreCase("Cualquiera") || ciudad.equalsIgnoreCase(inm.getCiudad());
            boolean cumplePrecio = precioMax <= 0 || inm.getPrecio() <= precioMax;
            boolean cumpleHabitaciones = inm.getHabitaciones() >= minHabitaciones;
            boolean cumpleParqueadero = !requiereParqueadero || inm.tieneParqueadero();

            if (cumpleTipo && cumpleCiudad && cumplePrecio && cumpleHabitaciones && cumpleParqueadero) {
                coincidencias.add(inm);
            }
        }
        
        // Ordenar por precio (menor a mayor) usando burbuja (CustomList no tiene .sort)
        for (int i = 0; i < coincidencias.getSize() - 1; i++) {
            for (int j = 0; j < coincidencias.getSize() - i - 1; j++) {
                Inmueble inmA = coincidencias.get(j);
                Inmueble inmB = coincidencias.get(j + 1);
                if (inmA.getPrecio() > inmB.getPrecio()) {
                    coincidencias.set(j, inmB);
                    coincidencias.set(j + 1, inmA);
                }
            }
        }
        
        return coincidencias;
    }

    // --- HERRAMIENTAS ADICIONALES (PILAS - REQ 5.2) ---

    /**
     * Historial de acciones administrativas (texto). Pila 1.
     */
    public String deshacerUltimaAccion() {
        String eliminada = historialAdministrativo.pop();
        if (eliminada != null) {
            return "Se deshizo en el sistema: " + eliminada;
        }
        return "No hay acciones para deshacer en memoria.";
    }

    public CustomStack<String> obtenerHistorialAdministrativo() {
        return historialAdministrativo;
    }

    /**
     * Guarda una instantánea del estado actual de un inmueble antes de modificarlo.
     * Permite revertir cambios de precio o disponibilidad. Pila 2.
     * @param inmueble el inmueble a fotografiar
     */
    public void guardarSnapshotInmueble(String codInmueble) {
        Inmueble i = buscarInmueble(codInmueble);
        if (i != null) {
            // snapshot: [codigo, precio, disponibilidad]
            String[] snap = { i.getCodigo(), String.valueOf(i.getPrecio()), i.getDisponibilidad() };
            pilaSnapshotsInmueble.push(snap);
            historialAdministrativo.push("Snapshot guardado para inmueble " + codInmueble);
        }
    }

    /**
     * Revierte el último cambio de precio o disponibilidad de un inmueble.
     */
    public String revertirCambioInmueble() {
        String[] snap = pilaSnapshotsInmueble.pop();
        if (snap == null) {
            return "No hay cambios de inmueble para revertir.";
        }
        Inmueble i = buscarInmueble(snap[0]);
        if (i != null) {
            i.setPrecio(Double.parseDouble(snap[1]));
            i.cambiarDisponibilidad(snap[2]);
            historialAdministrativo.push("Revirtió cambio en inmueble " + snap[0]);
            return "Inmueble " + snap[0] + " revertido a precio $" + snap[1] + " / estado: " + snap[2];
        }
        return "No se encontró el inmueble para revertir.";
    }

    // --- COLAS DEL SISTEMA (REQ 5.3) ---

    /** Encola una solicitud de atención de un cliente (FIFO). */
    public void encolarSolicitudCliente(String idCliente, String motivo) {
        colaSolicitudesClientes.enqueue("[SOLICITUD] Cliente " + idCliente + ": " + motivo);
        historialAdministrativo.push("Nueva solicitud de " + idCliente);
    }

    /** Atiende y extrae la primera solicitud de cliente en espera. */
    public String atenderSiguienteSolicitud() {
        String s = colaSolicitudesClientes.dequeue();
        return s != null ? s : "No hay solicitudes de clientes pendientes.";
    }

    /** Encola una tarea administrativa (seguimiento de flujo de trabajo). */
    public void encolarTareaAdmin(String tarea) {
        colaTareasAdministrativas.enqueue("[TAREA] " + tarea);
    }

    /** Procesa y extrae la siguiente tarea administrativa. */
    public String procesarSiguienteTareaAdmin() {
        String t = colaTareasAdministrativas.dequeue();
        return t != null ? t : "No hay tareas administrativas pendientes.";
    }

    /** Encola una alerta para revisión posterior. */
    public void encolarAlertaPendiente(String alerta) {
        colaAlertasPendientes.enqueue("[ALERTA] " + alerta);
    }

    /** Revisa y extrae la siguiente alerta de la cola. */
    public String revisarSiguienteAlerta() {
        String a = colaAlertasPendientes.dequeue();
        return a != null ? a : "No hay alertas pendientes por revisar.";
    }

    public int getSolicitudesPendientes() { return colaSolicitudesClientes.getSize(); }
    public int getTareasPendientes() { return colaTareasAdministrativas.getSize(); }
    public int getAlertasPendientesCount() { return colaAlertasPendientes.getSize(); }

    // --- COLAS DE PRIORIDAD (REQ 5.4) ---

    /**
     * Encola un contrato de arriendo con prioridad según su urgencia de vencimiento.
     * @param prioridad 5=muy urgente, 1=con tiempo
     */
    public void priorizarContrato(Operacion op, int prioridad) {
        colaContratosPorVencer.enqueue(op, prioridad);
    }

    public Operacion atenderContratoMasUrgente() {
        return colaContratosPorVencer.dequeue();
    }

    /**
     * Encola un cliente según su nivel de intención de compra/arriendo.
     * @param prioridad 5=listo para firmar, 1=explorando
     */
    public void priorizarCliente(Cliente c, int prioridad) {
        colaClientesInteres.enqueue(c, prioridad);
    }

    public Cliente atenderClientePrioritario() {
        return colaClientesInteres.dequeue();
    }

    /**
     * Encola un inmueble según su nivel de demanda (visitas).
     */
    public void priorizarInmueblePorDemanda(String codInmueble) {
        Inmueble i = buscarInmueble(codInmueble);
        if (i != null) {
            Integer visitas = visitasPorInmueble.get(codInmueble);
            int prioridad = (visitas == null) ? 0 : visitas;
            colaDemandaInmuebles.enqueue(i, prioridad);
        }
    }

    public Inmueble obtenerInmuebleMasDemandado() {
        return colaDemandaInmuebles.dequeue();
    }

    // --- TABLAS HASH: AGRUPACIÓN SEMÁNTICA (REQ 5.5) ---

    /** Agrupa el inventario de inmuebles disponibles por ciudad. */
    public CustomHashTable<String, CustomList<Inmueble>> agruparInmueblesPorCiudad() {
        CustomHashTable<String, CustomList<Inmueble>> mapa = new CustomHashTable<>(50);
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            String clave = inm.getCiudad() != null ? inm.getCiudad().toUpperCase() : "SIN_CIUDAD";
            CustomList<Inmueble> lista = mapa.get(clave);
            if (lista == null) { lista = new CustomList<>(); mapa.put(clave, lista); }
            lista.add(inm);
        }
        return mapa;
    }

    /** Agrupa el inventario de inmuebles disponibles por tipo (Casa, Apto, Local...). */
    public CustomHashTable<String, CustomList<Inmueble>> agruparInmueblesPorTipo() {
        CustomHashTable<String, CustomList<Inmueble>> mapa = new CustomHashTable<>(20);
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            String clave = inm.getTipo() != null ? inm.getTipo().toUpperCase() : "OTRO";
            CustomList<Inmueble> lista = mapa.get(clave);
            if (lista == null) { lista = new CustomList<>(); mapa.put(clave, lista); }
            lista.add(inm);
        }
        return mapa;
    }

    /** Agrupa el inventario de inmuebles por estado (Disponible, Vendido, etc.). */
    public CustomHashTable<String, CustomList<Inmueble>> agruparInmueblesPorEstado() {
        CustomHashTable<String, CustomList<Inmueble>> mapa = new CustomHashTable<>(10);
        CustomList<Inmueble> todos = arbolInmueblesPrecio.toList();
        for (int i = 0; i < todos.getSize(); i++) {
            Inmueble inm = todos.get(i);
            String clave = inm.getDisponibilidad() != null ? inm.getDisponibilidad().toUpperCase() : "DESCONOCIDO";
            CustomList<Inmueble> lista = mapa.get(clave);
            if (lista == null) { lista = new CustomList<>(); mapa.put(clave, lista); }
            lista.add(inm);
        }
        return mapa;
    }

    public CustomHashTable<String, Integer> getVisitasPorZona() { return visitasPorZona; }

    // --- ÁRBOLES BST: CONSULTAS (REQ 5.6) ---

    /** Inmuebles en un rango de precios (BST range query) */
    public CustomList<Inmueble> buscarInmueblesPorRangoPrecio(double min, double max) {
        return arbolInmueblesPrecio.rangeQuery(min, max);
    }

    /** Clientes en un rango de presupuesto */
    public CustomList<Cliente> buscarClientesPorPresupuesto(double min, double max) {
        return arbolClientesPresupuesto.rangeQuery(min, max);
    }

    /** Asesores ordenados por cierres realizados (In-Order del BST) */
    public CustomList<Asesor> obtenerAsesoresPorCierres() {
        return arbolAsesoresCierres.toList();
    }

    /** Asesores ordenados por carga de trabajo (visitas pendientes) */
    public CustomList<Asesor> obtenerAsesoresPorCarga() {
        return arbolAsesoresCarga.toList();
    }

    // --- GRAFOS: ANÁLISIS (REQ 5.7) ---

    public void registrarInteraccionGrafo(String idCliente, String codInmueble) {
        if (buscarCliente(idCliente) != null && buscarInmueble(codInmueble) != null) {
            grafoRelaciones.addEdge(idCliente, codInmueble);
        }
    }

    /** Cuántas conexiones tiene un nodo en el grafo (popularidad). */
    public int obtenerGradoNodo(String nodeId) {
        return grafoRelaciones.getDegree(nodeId);
    }

    /** Detecta propiedades "hotspot" consultadas por muchos clientes. */
    public CustomList<String> detectarHotspots(int minConexiones) {
        CustomList<Inmueble> inmuebles = arbolInmueblesPrecio.toList();
        CustomList<String> codigos = new CustomList<>();
        for (int i = 0; i < inmuebles.getSize(); i++) {
            codigos.add(inmuebles.get(i).getCodigo());
        }
        return grafoRelaciones.getHotspots(minConexiones, codigos);
    }

    /** Qué clientes comparten interés en el mismo inmueble. */
    public CustomList<String> obtenerClientesConInteresComun(String codInmueble) {
        return grafoRelaciones.getCommonClients(codInmueble);
    }

    /** Recorrido BFS desde un cliente — mide su alcance de movilidad comercial. */
    public CustomList<String> analizarMovilidadComercial(String idCliente) {
        return grafoRelaciones.bfsTraversal(idCliente);
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

    public String encontrarAsesorLibreParaSlot(String codInmueble, String fecha, String hora) {
        CustomList<Asesor> todosAsesores = tablaAsesores.toList();
        
        // Primero verifico si la propiedad ya está ocupada por algún otro asesor a esa misma hora
        boolean ocupadoInmueble = false;
        for (int i = 0; i < todosAsesores.getSize(); i++) {
            Asesor otroA = todosAsesores.get(i);
            if (otroA.getVisitasPendientes().anyMatch(v -> 
                v.getInmueble().getCodigo().equals(codInmueble) && 
                v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"))) {
                ocupadoInmueble = true;
                break;
            }
        }
        
        // También reviso la cola de prioridad (los VIPs no se nos pueden cruzar tampoco)
        boolean ocupadoPrioridadInmueble = colaVisitasPrioritarias.anyMatch(v -> 
            v.getInmueble().getCodigo().equals(codInmueble) && 
            v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));

        // Si la propiedad está ocupada, corto de una vez, no le puedo asignar a nadie esta visita
        if (ocupadoInmueble || ocupadoPrioridadInmueble) return null;

        // Si está libre, intento darle prioridad al asesor que es dueño de este inmueble
        Inmueble in = buscarInmueble(codInmueble);
        if (in != null && in.getAsesorResponsable() != null) {
            Asesor resp = in.getAsesorResponsable();
            // Reviso que este asesor principal no esté ocupado ni en citas normales ni VIP
            boolean librePend = !resp.getVisitasPendientes().anyMatch(v -> v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
            boolean librePrio = !colaVisitasPrioritarias.anyMatch(v -> v.getAsesorAsignado().getIdentificacion().equals(resp.getIdentificacion()) && v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
            if (librePend && librePrio) return resp.getIdentificacion();
        }

        // Si el asesor principal estaba ocupado (o no tiene), recorro toda la empresa buscando el primero que esté libre
        for (int i = 0; i < todosAsesores.getSize(); i++) {
            Asesor candidato = todosAsesores.get(i);
            boolean librePend = !candidato.getVisitasPendientes().anyMatch(v -> v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
            boolean librePrio = !colaVisitasPrioritarias.anyMatch(v -> v.getAsesorAsignado().getIdentificacion().equals(candidato.getIdentificacion()) && v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
            if (librePend && librePrio) {
                // Encontré un asesor libre, le tiro la cita a él
                return candidato.getIdentificacion();
            }
        }
        return null;
    }

    /**
     * Calcula los horarios disponibles para un asesor en una fecha específica.
     * Basado en una jornada de 08:00 a 18:00 cada hora.
     */
    public CustomList<String> obtenerSlotsDisponibles(String idAsesor, String codInmueble, String fecha) {
        CustomList<String> slots = new CustomList<>();
        
        // Si no se pasó un asesor específico, buscaremos si ALGUNO está libre.
        boolean esAsignacionAutomatica = (idAsesor == null || idAsesor.trim().isEmpty() || idAsesor.equals("ANY"));
        Asesor aEspecifico = esAsignacionAutomatica ? null : buscarAsesor(idAsesor);
        
        // Si pidieron un asesor específico y no existe, no hay slots
        if (!esAsignacionAutomatica && aEspecifico == null) return slots;

        // Definimos slots base
        String[] jornada = {"08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        CustomList<Asesor> todosAsesores = tablaAsesores.toList();
        
        for (String hora : jornada) {
            // 1. Verificar si EL INMUEBLE ya está ocupado a esa hora por CUALQUIER asesor
            boolean ocupadoInmueble = false;
            for (int i = 0; i < todosAsesores.getSize(); i++) {
                Asesor otroA = todosAsesores.get(i);
                if (otroA.getVisitasPendientes().anyMatch(v -> 
                    v.getInmueble().getCodigo().equals(codInmueble) && 
                    v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"))) {
                    ocupadoInmueble = true;
                    break;
                }
            }
            
            boolean ocupadoPrioridadInmueble = colaVisitasPrioritarias.anyMatch(v -> 
                v.getInmueble().getCodigo().equals(codInmueble) && 
                v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));

            // Si el inmueble está ocupado, descartar la hora inmediatamente
            if (ocupadoInmueble || ocupadoPrioridadInmueble) {
                continue;
            }

            // 2. Verificar disponibilidad de Asesor(es)
            if (!esAsignacionAutomatica) {
                // Verificar solo el asesor seleccionado
                boolean ocupadoAsesor = aEspecifico.getVisitasPendientes().anyMatch(v -> 
                    v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
                
                boolean ocupadoPrioridadAsesor = colaVisitasPrioritarias.anyMatch(v -> 
                    v.getAsesorAsignado().getIdentificacion().equals(idAsesor) && 
                    v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
                
                if (!ocupadoAsesor && !ocupadoPrioridadAsesor) {
                    slots.add(hora);
                }
            } else {
                // Asignación automática: Buscar si al menos UN asesor está libre
                boolean algunAsesorLibre = false;
                for (int i = 0; i < todosAsesores.getSize(); i++) {
                    Asesor candidato = todosAsesores.get(i);
                    boolean ocupadoCandidato = candidato.getVisitasPendientes().anyMatch(v -> 
                        v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
                    boolean ocupadoPrioridadCandidato = colaVisitasPrioritarias.anyMatch(v -> 
                        v.getAsesorAsignado().getIdentificacion().equals(candidato.getIdentificacion()) && 
                        v.getFecha().equals(fecha) && v.getHora().equals(hora) && !v.getEstado().equalsIgnoreCase("cancelada"));
                    
                    if (!ocupadoCandidato && !ocupadoPrioridadCandidato) {
                        algunAsesorLibre = true;
                        break;
                    }
                }
                
                if (algunAsesorLibre) {
                    slots.add(hora);
                }
            }
        }
        
        return slots;
    }

    public void registrarOperacion(Operacion op) {
        listaOperaciones.add(op);
        
        if (op.getInmueble() != null) {
            String tipo = op.getTipoOperacion().toLowerCase();
            if (tipo.equals("venta")) {
                op.getInmueble().cambiarDisponibilidad("Vendido");
            } else if (tipo.equals("arriendo")) {
                op.getInmueble().cambiarDisponibilidad("Arrendado");
            } else if (tipo.contains("cancelación") || tipo.contains("cancelacion")) {
                op.getInmueble().cambiarDisponibilidad("Disponible");
            }
        }
        
        // Registrar cierre en el asesor responsable (solo si no es cancelación)
        if (op.getAsesor() != null && !op.getTipoOperacion().toLowerCase().contains("cancelac")) {
            op.getAsesor().registrarCierre();
        }

        // REQUERIMIENTO 4.5: Marcar como negociado para el cliente
        if (op.getCliente() != null && op.getInmueble() != null) {
            op.getCliente().agregarNegociado(op.getInmueble());
        }
        
        historialAdministrativo.push("Registró operación " + op.getTipoOperacion() + " ID: " + op.getIdOperacion());
    }

    public boolean renovarContrato(String idOperacionOriginal, String nuevaFechaFin, double nuevoValor) {
        for (int i = 0; i < listaOperaciones.getSize(); i++) {
            Operacion op = listaOperaciones.get(i);
            if (op.getIdOperacion().equals(idOperacionOriginal) && op.getTipoOperacion().equalsIgnoreCase("Arriendo")) {
                Operacion renovacion = new Operacion(
                    "REN-" + System.currentTimeMillis(),
                    op.getInmueble(),
                    op.getCliente(),
                    op.getAsesor(),
                    "Hoy",
                    "Renovación",
                    nuevoValor,
                    nuevoValor * 0.02
                );
                listaOperaciones.add(renovacion);
                historialAdministrativo.push("Contrato renovado: " + idOperacionOriginal);
                return true;
            }
        }
        return false;
    }

    // --- REQUISITOS ADICIONALES (REQ 8) ---

    /**
     * Simula el crecimiento de la demanda en un sector basándose en la tendencia actual.
     * @param zona El sector a analizar.
     * @param meses Proyección a futuro.
     * @return Porcentaje de crecimiento proyectado.
     */
    public double simularCrecimientoDemanda(String zona, int meses) {
        Integer visitas = visitasPorZona.get(zona.toUpperCase());
        if (visitas == null || visitas == 0) return 1.05 * meses; // Crecimiento orgánico base
        
        // Simulación: factor de visitas actual * exponencial suave
        double factor = 1.0 + (visitas * 0.02);
        return Math.pow(factor, (double)meses / 12.0) * 10; // Retorna un % de crecimiento
    }

    public CustomList<Asesor> obtenerRankingAsesoresEfectividad() {
        CustomList<Asesor> asesores = tablaAsesores.toList();
        // Ordenamiento burbuja descendente por cierres para incluir a todos sin que se sobreescriban
        for (int i = 0; i < asesores.getSize(); i++) {
            for (int j = 0; j < asesores.getSize() - 1 - i; j++) {
                Asesor a1 = asesores.get(j);
                Asesor a2 = asesores.get(j + 1);
                if (a1.getCierresRealizados() < a2.getCierresRealizados()) {
                    asesores.set(j, a2);
                    asesores.set(j + 1, a1);
                }
            }
        }
        return asesores;
    }

    public CustomList<String> obtenerRankingZonasActividad() {
        // Obtenemos las zonas de la tabla hash y las ordenamos por visitas (simulado con lista)
        CustomList<String> zonas = visitasPorZona.keys();
        // Ordenamiento burbuja simple (para no usar util)
        for (int i = 0; i < zonas.getSize(); i++) {
            for (int j = 0; j < zonas.getSize() - 1 - i; j++) {
                String z1 = zonas.get(j);
                String z2 = zonas.get(j + 1);
                if (visitasPorZona.get(z1) < visitasPorZona.get(z2)) {
                    zonas.set(j, z2);
                    zonas.set(j + 1, z1);
                }
            }
        }
        return zonas;
    }

    private int calcularPrioridadCliente(Cliente c) {
        if ("Exitoso".equalsIgnoreCase(c.getEstadoBusqueda()) && !"Inversor".equalsIgnoreCase(c.getTipoCliente())) {
            return 0;
        }
        if ("Inactivo".equalsIgnoreCase(c.getEstadoBusqueda())) {
            return 0;
        }

        int score = 1; // Puntuación base

        // 1. Visitas agendadas activas (Pendiente, Confirmada, Reprogramada)
        CustomList<Visita> visitas = c.getHistorialVisitas();
        boolean tieneVisitasActivas = false;
        for (int i = 0; i < visitas.getSize(); i++) {
            String est = visitas.get(i).getEstado();
            if ("Pendiente".equalsIgnoreCase(est) || "Confirmada".equalsIgnoreCase(est) || "Reprogramada".equalsIgnoreCase(est)) {
                tieneVisitasActivas = true;
                break;
            }
        }
        if (tieneVisitasActivas) {
            score += 2;
        }

        // 2. Favoritos guardados
        if (c.getFavoritos().getSize() >= 2) {
            score += 1;
        }

        // 3. Nivel de interacciones (Actividad en la plataforma)
        if (c.getHistorialInteracciones().getSize() >= 5) {
            score += 1;
        }

        return Math.min(score, 5);
    }

    public void actualizarClientesAltaProbabilidad() {
        this.colaClientesInteres = new CustomPriorityQueue<>(50);
        CustomList<Cliente> todosClientes = tablaClientes.toList();
        for (int i = 0; i < todosClientes.getSize(); i++) {
            Cliente c = todosClientes.get(i);
            int prioridad = calcularPrioridadCliente(c);
            if (prioridad > 0) {
                colaClientesInteres.enqueue(c, prioridad);
            }
        }
    }

    public CustomList<Cliente> obtenerClientesAltaProbabilidad() {
        actualizarClientesAltaProbabilidad();
        return colaClientesInteres.toList();
    }

    public CustomList<Operacion> obtenerOperacionesLista() {
        return listaOperaciones;
    }
}
