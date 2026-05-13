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

## 🔑 Credenciales de Prueba
Para evaluar las diferentes funcionalidades del sistema, puede utilizar las siguientes credenciales predefinidas:

| Rol | Usuario (ID) | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` |
| **Asesor** | `A-001` | *No requiere* |
| **Cliente** | `C-001` | *No requiere* |

*Nota: Al entrar como Administrador, se habilitan todas las opciones de gestión, incluyendo Auditoría y Simulación de Demanda.*

## 💻 Instrucciones de Ejecución
1. Compilar los archivos `.java` ubicados en la carpeta `src/`.
2. Ejecutar la clase principal `MainWeb.java` (Levanta el servidor en `localhost:8080`).
3. Abrir `web/index.html` en el navegador.
