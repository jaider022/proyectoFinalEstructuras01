# 🛠️ Solución y Arquitectura del Sistema
**Proyecto Final: Plataforma PropTech**

## 1. Diseño de Entidades y Clases Principales

Para hacer frente a las funcionalidades del sistema PropTech y cumplir con la arquitectura exigida, se deben definir mediante Programación Orientada a Objetos (POO) las siguientes entidades base:

### 🏠 `Inmueble`
- **Atributos:** Código, Dirección, Ciudad, Barrio/Zona, Tipo (apartamento, lote, etc.), Finalidad (venta o arriendo), Precio, Área, Habitaciones, Baños, Estado, Asesor Responsable.
- **Comportamientos Previstos:** Cambiar estado (Disponible, Reservado, Arrendado), asignar asesor.

### 🧑‍💼 `Cliente`
- **Atributos:** Identificación, Nombre, Correo, Teléfono, Tipo (comprador/arrendador), Presupuesto, Zonas de Interés, Tipo de inmueble deseado.
- **Estructuras Internas Asociadas:**
  - Lista de Favoritos (`Lista ENLAZADA`).
  - Historial de Navegación/Visitas (`Lista`).

### 🤵 `Asesor`
- **Atributos:** Identificación, Nombre, Contacto, Especialidad (zona asignada).
- **Estructuras Internas Asociadas:** 
  - Inmuebles asignados y visitas agendadas.
  - Cierres realizados.

### 📅 `Visita`
- **Atributos:** Cliente Asociado, Inmueble, Fecha, Hora, Asesor Asignado, Estado de Visita, Observaciones.
- **Estados posibles:** Pendiente, Confirmada, Realizada, Cancelada.

### 💼 `Operacion / Contrato`
- **Atributos:** ID Operativo, Inmueble Implicado, Cliente, Asesor, Fecha, Tipo de Operación, Valor Acordado, Comisión, Estado del proceso.

---

## 2. Requerimientos Funcionales - Ruta de Implementación Lógica

Para construir el motor base se deberá seguir este flujo arquitectónico:

* **Módulo de Mantenimiento / CRUD:** Crear rutinas en el ciclo de ejecución para Registrar, modificar, eliminar y consultar Inmuebles, Clientes y Asesores haciendo uso de **Tablas Hash** para optimizar memoria en sus consultas.
* **Módulo de Atención y CRM (Gestión de agendas):** Administrar los turnos y agendar las visitas usando conjuntos y colas, apartando espacios especiales en las estructuras de **Colas de Prioridad** para negocios urgentes.
* **Módulo Transaccional:** Grabar el historial de interacción del usuario (apoyado en **Listas**) y sentar un sistema de registro para firmar contratos.
* **Módulo Analítico y Búsquedas Avanzadas:** 
    - Crear algoritmos para clasificar los inmuebles rápidamente bajo filtros de menor o mayor precio utilizando un **Árbol Binario de Búsqueda (BST-/AVL)**.
    - Implementar el generador de modelos de recomendaciones conectando la similitud entre perfilamientos a partir de la teoría de **Grafos**.

---

## 3. Módulo de Analítica Avanzada (Premium Bonus Req)

El sistema incorporará un motor inteligente de **detección de comportamiento inusual** para identificar:
1. Inmuebles con miles de visitas pero sin cierres.
2. Clientes espías o volátiles visitando rápido sin intención confirmada.
3. Asesores sobrecargados y cuellos de botella mediante monitoreo iterativo interno.

---

## 4. Entregables Esperados del Proyecto

Para la entrega exitosa definitiva se requerirá compilar:
- [ ] Código fuente completo documentado.
- [ ] Diagrama de clases (Con base al numeral 1).
- [ ] Documentación: Este análisis descriptivo que relaciona problema y estructuras usadas.
- [ ] Script o interfaz que simule datos y corra ejemplos de ejecución de las operaciones analíticas mencionadas.
