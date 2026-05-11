# Historial de Cambios - Proyecto PropTech

Este archivo registra las modificaciones, actualizaciones y solicitudes realizadas por el equipo de desarrollo.

**Integrantes:**
- Jaider
- Jeison
- Juanita

---

## [2026-05-11] - Actualización de Gestión de Activos y UI
**Autor:** Jaider (vía Antigravity)
**Cambios realizados:**
- **Automatización de Imágenes:** Se implementó la carga masiva de imágenes para las propiedades para asegurar consistencia visual.
- **Tipos de Propiedad:** Se expandió la clasificación de tipos de propiedad, añadiendo la categoría "Oficina".
- **Mejoras en la Interfaz Admin:**
    - Refinamiento de formularios modales.
    - Corrección de errores en la navegación y botones de cierre.
    - Eliminación del botón flotante "AD" redundante.

---

---

## [2026-05-11] - Gestión de Ventas para Asesores
**Autor:** Jaider
**Solicitud:** Permitir que los asesores registren cuando venden o arriendan una propiedad (especialmente oficinas) mediante una casilla o botón.
**Cambios realizados:**
- **Acceso a Operaciones:** Se habilitó el botón "Cerrar Negocio" para el rol de Asesor en la vista de detalles de cualquier inmueble.
- **Botón Directo en Dashboard:** Se añadió un botón "Cerrar" en la lista de inmuebles asignados del Panel de Asesor para facilitar el registro de ventas/arriendos.
- **Lógica Refactorizada:** Se creó la función global `openOperacionModal` para estandarizar el proceso de cierre de negocios desde diferentes partes de la interfaz.
- **Mejora en Cierre de Sesión:** Se ajustó el logout para que el usuario permanezca en el catálogo como invitado en lugar de ser redirigido a la pantalla de inicio.
