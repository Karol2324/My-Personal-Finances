# Mis Finanzas - Gestor de Gastos Personales

Aplicación nativa para Android desarrollada en Java como parte del proyecto del Momento 1. Permite llevar un control diario de ingresos y gastos mediante un sistema de almacenamiento local.

## Estudiante
- **Nombre Completo:** Diana Carolina Hidalgo Guerrero
- **Materia:** Programación de Aplicaciones Móviles II

## Características Técnicas
- **Lenguaje:** Java 100%
- **Persistencia:** SQLite nativo (operaciones CRUD completas).
- **Interfaz de Usuario:** XML Layouts con ViewBinding y listados dinámicos con `RecyclerView` optimizado mediante `ViewHolder`.

## Capturas de Pantalla
Aquí se muestran las vistas principales de la aplicación en funcionamiento:

### Lista de Transacciones Principal
![Lista de Transacciones](capturas/lista_principal.png)

### Formulario de Registro / Edición
![Formulario](capturas/formulario_registro.png)

## Novedades y Tecnologías (Momento 2)

### 1. Base de Datos NoSQL y Sincronización en Tiempo Real
* **Cloud Firestore:** Persistencia NoSQL para almacenar transacciones de forma remota.
* **Real-time Listener:** Uso de `addSnapshotListener` para actualizar la interfaz del usuario al instante ante cualquier cambio (sin refrescar manualmente).

### 2. Operaciones CRUD Completas
* **Create:** Registro de nuevos ingresos y gastos.
* **Read:** Mapeo automático de documentos a objetos `Transaccion`.
* **Update:** Edición de montos, conceptos y tipos mediante referencias por *Document ID*.
* **Delete:** Eliminación directa desde el formulario o accesos rápidos de la lista.

### 3. Diseño e Interfaz UI/UX (Material Design 3)
* **Balance Dinámico:** Encabezado con logo y sumatoria en tiempo real de ingresos y gastos totales.
* **Componentes Material 3:** `MaterialCardView`, `TextInputLayout`, `FloatingActionButton` y badges dinámicos con indicadores de color (verde para Ingresos, rojo para Gastos).

### 4. Calidad del Código y Rendimiento
* **Validación en tiempo real:** Control de errores mediante `TextWatcher` en los campos del formulario.
* **Prevención de doble envío:** Deshabilitación de botones y barra de progreso (`ProgressBar`) durante transacciones asíncronas.
* **Gestión de Memoria:** Liberación de listeners en el ciclo de vida `onDestroy()` para evitar fugas de memoria (*Memory Leaks*).

---

## Tecnologías Utilizadas
* **Lenguaje:** Java
* **Framework:** Android SDK (API 24+)
* **Backend:** Firebase Cloud Firestore
* **Arquitectura:** ViewBinding, RecyclerView, CardView & Material Components
