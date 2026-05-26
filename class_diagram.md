# Diagrama de Clases Avanzado: PropTech System

Este diagrama de clases detalla la estructura completa de los componentes del sistema, incluyendo sus relaciones, métodos principales, atributos y la integración con las estructuras de datos personalizadas.

```mermaid
classDiagram
    %% Capa de Servidores y Controladores
    class PropTechServer {
        -PropTechSystem sistema
        -int port
        +start() void
    }

    class PropTechSystem {
        -CustomHashTable tablaClientes
        -CustomHashTable tablaInmueblesRapida
        -CustomHashTable tablaAsesores
        -CustomBST arbolInmueblesPrecio
        -CustomBST arbolClientesPresupuesto
        -CustomBST arbolAsesoresCierres
        -CustomBST arbolAsesoresCarga
        -CustomStack historialAdministrativo
        -CustomStack pilaSnapshotsInmueble
        -CustomQueue colaSolicitudesClientes
        -CustomQueue colaTareasAdministrativas
        -CustomQueue colaAlertasPendientes
        -CustomPriorityQueue colaVisitasPrioritarias
        -CustomPriorityQueue colaContratosPorVencer
        -CustomPriorityQueue colaClientesInteres
        -CustomPriorityQueue colaDemandaInmuebles
        -CustomGraph grafoRelaciones
        -CustomHashTable visitasPorInmueble
        -CustomHashTable visitasPorZona
        -CustomList listaOperaciones
        -CustomList logAnomalias
        +registrarCliente(Cliente) void
        +registrarInmueble(Inmueble) void
        +eliminarInmueble(String) void
        +registrarAsesor(Asesor) void
        +eliminarAsesor(String) void
        +buscarCliente(String) Cliente
        +buscarInmueble(String) Inmueble
        +buscarAsesor(String) Asesor
        +agendarVisita(String, String, String, String, String, int) boolean
        +reprogramarVisita(String, String, String, String) boolean
        +cancelarVisita(String, String) boolean
        +confirmarVisita(String, String) boolean
        +consultarInmueble(String, String) Inmueble
        +descartarInmueble(String, String) void
        +agregarAFavoritos(String, String) void
        +removerDeFavoritos(String, String) void
        +detectarComportamientoInusual() void
        +ejecutarAuditoriaComercial() void
        +obtenerRecomendacionesHibridas(String) CustomList
        +filtrarInmueblesAvanzado(String, String, int, int, double, double) CustomList
        +obtenerSlotsDisponibles(String, String, String) CustomList
        +registrarOperacion(Operacion) void
        +renovarContrato(String, String, double) boolean
        +simularCrecimientoDemanda(String, int) double
        +obtenerRankingAsesoresEfectividad() CustomList
        +obtenerRankingZonasActividad() CustomList
        +obtenerClientesAltaProbabilidad() CustomList
    }

    class UnusualBehaviorDetectionModule {
        -PropTechSystem sistema
        -CustomPriorityQueue alertasPrioritarias
        +runDetection() void
    }

    %% Capa de Modelos
    class Cliente {
        -String identificacion
        -String nombre
        -String correo
        -String telefono
        -String tipoCliente
        -double presupuesto
        -String zonasDeInteres
        -String tipoInmuebleDeseado
        -int minHabitaciones
        -String estadoBusqueda
        -String fechaUltimoSeguimiento
        -CustomList favoritos
        -CustomList intenciones
        -CustomList consultados
        -CustomList descartados
        -CustomList negociados
        -CustomList historialVisitas
        -CustomStack historialInteracciones
        +registrarInteraccion(String) void
        +agregarFavorito(Inmueble) void
        +removerFavorito(Inmueble) void
        +prefiere(Inmueble) boolean
    }

    class Inmueble {
        -String codigo
        -String direccion
        -String ciudad
        -String zona
        -String tipo
        -String finalidad
        -double precio
        -double area
        -double areaTerreno
        -int habitaciones
        -int banos
        -String estadoFisico
        -String disponibilidad
        -Asesor asesorResponsable
        -CustomList fotos
        -String fechaUltimaVisita
        -String fechaReserva
        -int cambiosPrecioCount
        +cambiarDisponibilidad(String) void
    }

    class Asesor {
        -String identificacion
        -String nombre
        -String contacto
        -String especialidadZona
        -int cierresRealizados
        -CustomList inmueblesAsignados
        -CustomQueue visitasPendientes
        +registrarCierre() void
        +addInmueble(Inmueble) void
        +removeInmueble(Inmueble) void
    }

    class Visita {
        -Cliente cliente
        -Inmueble inmueble
        -Asesor asesorAsignado
        -String fecha
        -String hora
        -String estado
        -String observaciones
    }

    class Operacion {
        -String idOperacion
        -Inmueble inmueble
        -Cliente cliente
        -Asesor asesor
        -String fecha
        -String tipoOperacion
        -double valorAcordado
        -double comision
        -String fechaVencimiento
        -String estadoProceso
    }

    class EventoAnomalo {
        -String tipo
        -String descripcion
        -String nivelGravedad
        -String fechaDeteccion
        -String idEntidadAfectada
    }

    %% Capa de Servicios de Inteligencia Artificial (IA)
    class PredictorPrecio {
        +estimar(Inmueble) double
    }

    class AsistenteVirtual {
        +procesarPregunta(String, PropTechSystem) String
    }

    class AnalizadorSentimiento {
        +analizar(String) String
    }

    %% Capa de Estructuras de Datos
    class CustomList~T~ {
        -Node head
        -int size
        +add(T) void
        +remove(T) boolean
        +get(int) T
        +indexOf(T) int
        +getSize() int
    }

    class CustomStack~T~ {
        -Node top
        -int size
        +push(T) void
        +pop() T
        +peek() T
        +getSize() int
    }

    class CustomQueue~T~ {
        -Node front
        -Node rear
        -int size
        +enqueue(T) void
        +dequeue() T
        +isEmpty() boolean
        +getSize() int
    }

    class CustomPriorityQueue~T~ {
        -Node head
        -int size
        +enqueue(T, int) void
        +dequeue() T
        +getSize() int
    }

    class CustomHashTable~K, V~ {
        -HashNode[] table
        -int size
        +put(K, V) void
        +get(K) V
        +remove(K) V
        +keys() CustomList
    }

    class CustomBST~K, V~ {
        -TreeNode root
        +insert(K, V) void
        +search(K) V
        +delete(K) void
        +rangeQuery(K, K) CustomList
        +toList() CustomList
    }

    class CustomGraph~T~ {
        -CustomHashTable adjacencyList
        -boolean bidirectional
        +addVertex(T) void
        +addEdge(T, T) void
        +getRecommendations(T) CustomList
        +bfsTraversal(T) CustomList
    }

    %% Relaciones y Dependencias
    PropTechServer --> PropTechSystem : utiliza
    UnusualBehaviorDetectionModule --> PropTechSystem : analiza
    PropTechSystem "1" *-- "many" Cliente : administra
    PropTechSystem "1" *-- "many" Inmueble : administra
    PropTechSystem "1" *-- "many" Asesor : administra
    PropTechSystem "1" *-- "many" Operacion : registra
    PropTechSystem "1" *-- "many" EventoAnomalo : audita

    Cliente "1" *-- "many" Visita : agenda
    Cliente "1" *-- "many" Inmueble : favoritos / consultados
    Inmueble "many" o-- "1" Asesor : asignado_a
    Asesor "1" *-- "many" Visita : gestiona
    Visita "1" --> "1" Cliente : involucra
    Visita "1" --> "1" Inmueble : sobre
    Visita "1" --> "1" Asesor : asignada_a

    Operacion "1" --> "1" Inmueble : afecta
    Operacion "1" --> "1" Cliente : contratante
    Operacion "1" --> "1" Asesor : intermediario

    %% Dependencia a Estructuras
    PropTechSystem ..> CustomHashTable : usa
    PropTechSystem ..> CustomBST : usa
    PropTechSystem ..> CustomGraph : usa
    PropTechSystem ..> CustomQueue : usa
    PropTechSystem ..> CustomPriorityQueue : usa
    PropTechSystem ..> CustomStack : usa
    PropTechSystem ..> CustomList : usa

    AsistenteVirtual ..> PropTechSystem : interactua
    PredictorPrecio ..> Inmueble : predice
