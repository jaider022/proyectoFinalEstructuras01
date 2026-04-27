# 📚 Teoría y Justificación Técnica de Estructuras de Datos
**Proyecto Final: Plataforma PropTech**

## 1. Contexto del Problema
Las inmobiliarias modernas (PropTech) dejaron de ser simples listados e inventarios de propiedades. Actualmente, gestionan miles de inmuebles y los relacionan dinámicamente con las necesidades, preferencias e historial de los clientes. Se requiere organizar la información de manera inteligente para optimizar las búsquedas, coordinar asesores, detectar patrones y realizar recomendaciones oportunas.

---

## 2. Estructuras de Datos y su Justificación Técnica

### 2.1 Listas
* **Uso en el proyecto:** Historial de visitas, historial de inmuebles consultados por un usuario, almacenamiento de favoritos, y manejo de elementos asignados a entidades (por ejemplo, los contratos de un inmueble).
* **Justificación:** Las listas son la herramienta natural para almacenar colecciones de tamaño dinámico donde debemos preservar el orden temporal (como en un historial) o cuando frecuentemente iteramos sobre todos los elementos para listar información de forma sencilla.

### 2.2 Pilas (LIFO - Last In, First Out)
* **Uso en el proyecto:** Deshacer cambios recientes en publicaciones de inmuebles, reversar modificaciones en estados de propiedad e historial de acciones administrativas.
* **Justificación:** Como el último cambio hecho es el primero que debe deshacerse, una estructura LIFO es matemáticamente exacta para este requerimiento. Permite ejecutar operaciones `Deshacer / Revertir` introduciendo y extrayendo las últimas acciones en tiempo `O(1)`.

### 2.3 Colas (FIFO - First In, First Out)
* **Uso en el proyecto:** Manejar las solicitudes de nuevos clientes, procesamiento general de visitas estándar pendientes por agendar.
* **Justificación:** Las colas dictan equidad: el primero en solicitar servicio es el primero en ser atendido. Esto es imprescindible para mantener el orden cronológico justo en la atención al cliente de la plataforma.

### 2.4 Colas de Prioridad
* **Uso en el proyecto:** Asignación de alertas urgentes de vencimientos, atención a clientes en formato "VIP" con alta intención de cierre, y análisis de inmuebles de alta demanda.
* **Justificación:** En el mundo empresarial, no todos los tickets tienen la misma urgencia. Implementar colas de prioridad (usualmente apoyadas en estructuras como los *Heaps*) permite romper el esquema tradicional de la Cola e identificar y servir los procesos críticos o más rentables inmediatamente `O(log n)`.

### 2.5 Tablas Hash (Diccionarios / Hash Maps)
* **Uso en el proyecto:** Ubicación instantánea de clientes por número de identificación (CC/ID), buscar inmuebles mediante su código único, conteo de frecuencias.
* **Justificación:** Son imbatibles a la hora de ubicar información usando llaves únicas de acceso rápido. Brindan un tiempo de búsqueda algorítmica aproximado a `O(1)`, lo cual optimiza espectacularmente el sistema cuando se cuenta con miles de clientes o casas y queremos hallar uno sin recorrer listas completas.

### 2.6 Árboles (ej. Árboles Binarios de Búsqueda)
* **Uso en el proyecto:** Ordenar de forma automatizada las listas de los inmuebles por sus precios, organizar los clientes por presupuesto, hacer agrupaciones demográficas escaladas.
* **Justificación:** Las Tablas Hash pueden ser muy rápidas buscando por ID, pero limitan las búsquedas en "rangos matemáticos". Los Árboles mantienen un balance jerárquico que permite organizar permanentemente los números (precios, tamaños) y filtrar colecciones en rangos como "mostrar entre $1M y $3M" en un tiempo eficiente `O(log n)`.

### 2.7 Grafos
* **Uso en el proyecto:** Representar inteligentemente relaciones cliente-propiedad, mapeo de cuáles inmuebles atraen al mismo subgrupo de clientes, motor de recomendaciones avanzado.
* **Justificación:** Los grafos permiten analizar el tejido de conexiones mútiples M:N. Al dibujar nodos de inmuebles interconectados por los clientes que los visitaron o guardaron en favoritos, es fácil analizar matemáticamente qué recomendar a futuros usuarios con base en los perfiles anteriores de forma estructural.
