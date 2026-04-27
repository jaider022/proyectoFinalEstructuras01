# Proyecto Final Estructuras de Datos - Plataforma PropTech

## 🏢 Descripción
Este proyecto es un sistema de gestión **PropTech** (Property Technology) desarrollado como proyecto final de la asignatura de Estructuras de Datos (2026-1). La plataforma permite la administración eficiente de inmuebles, clientes, asesores, operaciones inmobiliarias y el agendamiento de visitas, aplicando conceptos avanzados de programación.

## 🚀 Características Principales
* **Gestión de Inmuebles:** Registro, visualización y eliminación de propiedades.
* **Gestión de Usuarios:** Administración de clientes y asesores inmobiliarios.
* **Programación de Visitas:** Sistema de agendamiento de visitas a las propiedades.
* **Gestión de Operaciones:** Control y registro de transacciones inmobiliarias.
* **Dashboard Analítico:** Visualización de estadísticas del sistema.

## 🛠️ Arquitectura y Tecnologías
El sistema está construido bajo una arquitectura cliente-servidor:

### Backend (Java)
* Implementación **desde cero** de estructuras de datos nativas para optimizar el rendimiento.
* Servidor HTTP integrado (`PropTechServer`) para exponer una API REST.
* Patrón arquitectónico basado en Modelos (`Asesor`, `Cliente`, `Inmueble`, etc.) y Controladores.

### Frontend (Web)
* Interfaz gráfica intuitiva y moderna construida con **HTML5, CSS3 y JavaScript (Vanilla)**.
* Consumo de la API REST mediante fetch para actualizar la información en tiempo real.

## 🧠 Estructuras de Datos Implementadas
El núcleo lógico del sistema se apoya en las siguientes estructuras de código propio (ubicadas en `src/structures/`):
* **CustomBST:** Árboles Binarios de Búsqueda.
* **CustomGraph:** Grafos (útiles para mapas o relaciones complejas).
* **CustomHashTable:** Tablas Hash para búsquedas ultra-rápidas.
* **Otras:** `CustomList`, `CustomQueue`, `CustomStack`, y `CustomPriorityQueue`.

## 💻 Instrucciones de Ejecución
1. Compilar los archivos `.java` ubicados en la carpeta `src/`.
2. Ejecutar la clase principal encargada de levantar el servidor web (usualmente `PropTechServer.java` o `MainWeb.java`).
3. Una vez el servidor esté corriendo, abrir el archivo `web/index.html` en tu navegador web de preferencia para usar el sistema.
