# Documentación Técnica: PropTech System

## 🎯 Definición del Problema
El sector inmobiliario requiere gestionar dinámicamente inventarios masivos y relaciones complejas entre usuarios. Los sistemas tradicionales suelen fallar en proporcionar recomendaciones personalizadas en tiempo real y en gestionar la prioridad de atención de forma justa. Este proyecto aborda la necesidad de una plataforma que maneje búsquedas eficientes por precio, zona y código, mientras detecta anomalías comerciales (especulación/estancamiento) mediante el análisis estructural de datos.

Este documento detalla el diseño arquitectónico y la justificación técnica de las estructuras de datos utilizadas en el sistema PropTech, cumpliendo con los requisitos no funcionales del proyecto.

## 🏗️ Arquitectura del Sistema
El sistema sigue una arquitectura de **Tres Capas**:
1.  **Modelos (`src/models/`)**: Representan las entidades del negocio (Inmueble, Cliente, Asesor, Operación, Visita).
2.  **Estructuras (`src/structures/`)**: Implementaciones propias de estructuras de datos genéricas, evitando el uso de librerías estándar de Java para demostrar el conocimiento técnico.
3.  **Controladores (`src/controllers/`)**:
    *   `PropTechSystem`: Lógica de negocio y orquestación de estructuras.
    *   `PropTechServer`: Capa de API REST para comunicación con el frontend.

---

## 📊 Justificación Técnica de Estructuras de Datos

| Estructura | Complejidad (Promedio) | Justificación Técnica |
|---|---|---|
| **CustomHashTable** | **O(1)** Búsqueda | Utilizada para el acceso instantáneo a clientes por ID y asesores. Es la opción más eficiente cuando se tiene una clave única y se requiere velocidad extrema. |
| **CustomBST** | **O(log n)** Búsqueda | Utilizado para organizar inmuebles por precio. Permite realizar **consultas por rango** (Range Queries) de forma eficiente, algo que una Tabla Hash no puede hacer. |
| **CustomGraph** | **O(V + E)** Recorrido | Representa las relaciones complejas Cliente-Inmueble. Permite algoritmos de recomendación basados en "clientes similares" y detección de propiedades con alta conectividad (Hotspots). |
| **CustomStack** | **O(1)** Push/Pop | Implementa el sistema **Undo (Deshacer)**. Almacena snapshots de estados previos para permitir la reversibilidad de cambios administrativos y de inventario. |
| **CustomQueue** | **O(1)** Enqueue/Dequeue | Gestiona el flujo de trabajo **FIFO (First-In-First-Out)** para solicitudes de clientes y alertas, asegurando que se atiendan en orden de llegada. |
| **PriorityQueue** | **O(log n)** Inserción | Utilizado para **VIP Management**. Permite que clientes de alto interés o contratos urgentes "salten" la fila y sean atendidos con prioridad máxima. |
| **CustomList** | **O(n)** Acceso | Utilizada para historiales, favoritos y colecciones de fotos donde el orden de inserción es importante y el tamaño es dinámico. |

---

## 🛡️ Validaciones y Consistencia
El sistema implementa validaciones críticas en cada operación:
- **Disponibilidad:** El motor de agenda (`agendarVisita`) rechaza citas si un inmueble no tiene estado "Disponible".
- **Presupuesto:** El motor de recomendaciones filtra automáticamente inmuebles que superan el presupuesto definido del cliente.
- **Consistencia:** Las operaciones de cierre (Venta/Arriendo) actualizan automáticamente el inventario y el historial del asesor en una única transacción lógica.

## 🚀 Eficiencia de Búsqueda
- **Búsqueda por Código:** Realizada mediante Tabla Hash en **tiempo constante O(1)**.
- **Búsqueda por Precio/Rango:** Realizada mediante BST, permitiendo encontrar propiedades en rangos específicos en **tiempo logarítmico O(log n)**.

## 🛠️ Manejo de Errores
La capa de servidor (`PropTechServer`) utiliza bloques `try-catch` para capturar excepciones y retornar códigos de estado HTTP adecuados (200 OK, 400 Bad Request, 401 Unauthorized), asegurando que el frontend pueda informar al usuario de cualquier fallo sin que el sistema colapse.

---
---

## 📈 Requisitos Adicionales (Req 8)
El sistema integra motores de inteligencia y proyección:
1.  **Recomendaciones:** Algoritmo híbrido que combina similitud en Grafos con filtrado por presupuesto en BST.
2.  **Rankings:** Ordenamiento de asesores por cierres y zonas por tráfico mediante Tablas Hash y Heaps.
3.  **Simulación de Demanda:** Modelo matemático que proyecta el crecimiento de visitas en sectores específicos basándose en la tendencia histórica.
4.  **Probabilidad de Cierre:** Uso de Colas de Prioridad para detectar clientes con mayor volumen de interacción.

*Desarrollado como Proyecto Final de Estructuras de Datos 2026-1.*
