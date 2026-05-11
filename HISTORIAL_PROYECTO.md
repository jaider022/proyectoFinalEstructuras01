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
- **Seguridad de Acceso:** Se implementó la validación de contraseña para asesores (ID + clave "123") en el servidor.
- **Flujo de Navegación:** Se restauró el cierre de sesión para redirigir a la Landing Page, mejorando la seguridad del cierre de cuenta.
- **Gestión Comercial:** Los asesores ya pueden cerrar negocios (Venta/Arriendo) con visualización de estado en tiempo real.
- **Seguridad Operativa:** Se bloqueó la modificación de inmuebles ya vendidos/arrendados para el rol de asesor, dejando esta facultad solo al administrador.
- **Identidad Visual:** Nuevo badge de perfil con iconos en el encabezado y marcas de agua (Vendido/Arrendado) en el catálogo de propiedades.
- **Optimización de Búsqueda:** Se ajustó el motor de filtros para que los inmuebles no disponibles sigan siendo visibles con su respectiva marca de agua, demostrando éxito comercial.
