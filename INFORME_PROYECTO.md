# Informe Final de Proyecto: PropTech System
**Curso:** Estructuras de Datos  
**Semestre:** 2026-1

## 1. Explicación del Problema
La industria inmobiliaria (PropTech) enfrenta el reto de gestionar grandes volúmenes de datos heterogéneos: inmuebles, clientes, asesores y transacciones. El problema principal es la **eficiencia en la búsqueda** y la **capacidad analítica** para recomendar propiedades de forma inteligente, gestionar prioridades en visitas y permitir la trazabilidad de cambios (Undo). Una solución basada en estructuras genéricas de lenguaje a menudo oculta la complejidad y limita la optimización específica para este dominio.

## 2. Descripción de la Solución
Se desarrolló una plataforma integral que utiliza estructuras de datos personalizadas (desde cero) para maximizar el rendimiento:
- **Búsquedas:** Optimizadas mediante Tablas Hash y Árboles de Búsqueda Binaria.
- **Inteligencia:** Motor de recomendaciones basado en Grafos.
- **Gestión:** Colas de prioridad para clientes VIP y Pilas para reversibilidad de cambios.

## 3. Estructuras de Datos y Justificación
(Resumen, detalle completo en `DOCUMENTACION_TECNICA.md`)
- **CustomHashTable:** Búsqueda O(1) de entidades por ID.
- **CustomBST:** Consultas por rango de precio y ordenamiento dinámico.
- **CustomGraph:** Análisis de patrones de consumo y similitud de propiedades.
- **CustomStack:** Persistencia temporal de estados para la función 'Deshacer'.
- **CustomQueue/PriorityQueue:** Orquestación de atención al cliente y urgencias comerciales.

## 4. Ejemplos de Ejecución
### Escenario A: Registro y Recomendación
1. Se registra un inmueble de $500M en "Zona Norte".
2. Un cliente con presupuesto de $600M busca en "Norte".
3. El sistema, mediante el **BST**, lo identifica como coincidencia.
4. Tras registrar una visita, el **Grafo** recomienda inmuebles similares vistos por otros usuarios.

### Escenario B: Auditoría Comercial
1. Un asesor cambia el precio de un inmueble 3 veces en una hora.
2. El motor de auditoría detecta **Especulación de Precios**.
3. El administrador visualiza la alerta en el Dashboard y utiliza el botón **Undo** (Pila) para revertir al precio original.

## 5. Conclusiones
La implementación de estructuras de datos propias permitió un control total sobre la complejidad temporal y espacial del sistema. Se logró una integración fluida entre una lógica de backend robusta (Java) y una interfaz de usuario moderna (HTML/CSS/JS), demostrando que la teoría de estructuras de datos es la base de las aplicaciones PropTech escalables.

---
*Este proyecto cumple con todos los requerimientos funcionales, no funcionales y adicionales solicitados.*
