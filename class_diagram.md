# Diagrama de Clases - PropTech System

```mermaid
classDiagram
    class PropTechSystem {
        -CustomHashTable tablaClientes
        -CustomHashTable tablaInmueblesRapida
        -CustomBST arbolInmueblesPrecio
        -CustomGraph grafoRelaciones
        -CustomStack historialAdministrativo
        -CustomPriorityQueue colaVisitasPrioritarias
        +registrarInmueble(Inmueble)
        +registrarCliente(Cliente)
        +agendarVisita(Cliente, Inmueble)
        +obtenerRecomendacionesHibridas(String)
        +simularCrecimientoDemanda(String, int)
    }

    class PropTechServer {
        -HttpServer server
        -PropTechSystem sistema
        +start()
    }

    class Inmueble {
        -String codigo
        -double precio
        -String zona
        -String disponibilidad
        -Asesor asesorResponsable
        +cambiarDisponibilidad(String)
    }

    class Cliente {
        -String identificacion
        -String nombre
        -double presupuesto
        -CustomList favoritos
        -CustomList historialVisitas
    }

    class Asesor {
        -String identificacion
        -String nombre
        -CustomQueue visitasPendientes
        +registrarCierre()
    }

    class Visita {
        -Cliente cliente
        -Inmueble inmueble
        -Asesor asesor
        -String fecha
        -String hora
    }

    class Operacion {
        -String idOperacion
        -String tipo
        -double monto
    }

    PropTechServer --> PropTechSystem : utiliza
    PropTechSystem "1" *-- "many" Inmueble : gestiona
    PropTechSystem "1" *-- "many" Cliente : gestiona
    PropTechSystem "1" *-- "many" Asesor : gestiona
    Inmueble o-- Asesor : asignado a
    Visita --> Cliente : involucra
    Visita --> Inmueble : sobre
    Visita --> Asesor : realizada por
    Operacion --> Inmueble : afecta
    Operacion --> Cliente : realizada por
```

## Relación con Estructuras de Datos
- **Inmueble/Cliente/Asesor:** Almacenados en `CustomHashTable` para búsquedas O(1).
- **Catálogo de Precios:** Gestionado por `CustomBST` para búsquedas por rango.
- **Flujo de Visitas:** `CustomQueue` y `CustomPriorityQueue` para atención y prioridad VIP.
- **Relaciones de Mercado:** `CustomGraph` para analizar conexiones entre usuarios y propiedades.
