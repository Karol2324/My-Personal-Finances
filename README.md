💰 MyPersonalFinances (Mis Finanzas)Aplicación móvil desarrollada en Android nativo (Java) diseñada para el control y administración integral de finanzas personales. Implementa sincronización reactiva en la nube, control de acceso basado en roles (RBAC), auditoría global del sistema, geolocalización de operaciones financieras y captura de comprobantes de pago.📌 ¿Qué compone este proyecto?El repositorio y la aplicación están estructurados en los siguientes módulos fundamentales:Gestión de Autenticación y Perfiles: Registro e inicio de sesión seguros conectados con Firebase Authentication.Control de Acceso Basado en Roles (RBAC):Cliente: Acceso restringido únicamente al panel personal y a sus propios registros.Administrador: Panel de bienvenida exclusivo y acceso al botón protegido Módulo de Auditoría (Admin).Módulo de Auditoría Global (Exclusivo Administrador):Métricas consolidadas del sistema: Total de operaciones globales, volumen acumulado de ingresos y total de gastos.Historial completo de movimientos de todos los usuarios registrados.CRUD de Finanzas Personales: Registro, edición, eliminación y listado en tiempo real de ingresos y gastos con Cloud Firestore.Geolocalización y Cartografía:Detección automática de coordenadas mediante el hardware GPS del dispositivo.Integración de Google Maps SDK para georreferenciación de puntos de gasto e ingreso.Módulo de Evidencias: Captura y vinculación de fotos y recibos de pago como soporte de transacciones.🛠️ Stack TecnológicoLenguaje: Java 17Plataforma: Android SDK (Min SDK 24 - Target SDK 34)Arquitectura & UI: Material Design 3 (MaterialToolbar, MaterialCardView, RecyclerView, FloatingActionButton)Backend & Servicios (Firebase):Firebase Authentication: Gestión de cuentas y credenciales.Cloud Firestore: Base de datos NoSQL reactiva para perfiles y transacciones.Firebase Storage: Almacenamiento multimedia para comprobantes de pago.APIs de Google: Google Play Services (Location & Maps API).🗄️ Esquema de Base de Datos NoSQL (Firestore)Colección: usuariosIdentificada por el UID único generado por Firebase Auth:{
  "correo": "karolisam_27@hotmail.com",
  "nombre": "Diana Guerrero",
  "rol": "Administrador"
}
Subcolección / Colección: transaccionesRegistra los movimientos financieros vinculados al usuario:{
  "categoria": "Salario",
  "descripcion": "Pago quincenal",
  "fecha": "04/11/2026",
  "monto": 2100000.0,
  "tipo": "Ingreso",
  "usuarioId": "6pjvHZZhIgPKTEVuwItP5SusSsT2",
  "latitud": 4.60971,
  "longitud": -74.08175,
  "fotoUrl": ""
}
🔑 Cuentas para Demostración y EvaluaciónPara la sustentación y pruebas del sistema se definieron dos perfiles con diferentes niveles de autorización:Rol de UsuarioCorreo ElectrónicoContraseña de PruebaAlcance y PermisosAdministradorkarolisam_27@hotmail.com(DanteH2017*)Acceso completo: Visualiza botón rojo de auditoría, métricas consolidadas y todas las transacciones.Clientekarolhg95@gmail.com(Nala2017*)Modo cliente: Solo visualiza sus propias transacciones y balance personal.🚀 Guía de Ejecución LocalClonar el repositorio:git clone https://github.com/<tu-usuario>/MyPersonalFinances.git
Abrir la carpeta raíz en Android Studio.Asegurar que el archivo de configuración google-services.json esté ubicado en la carpeta app/.Sincronizar dependencias con Gradle (Sync Project with Gradle Files).Compilar y ejecutar en emulador o dispositivo físico con Google Play Services activado.👩‍💻 AutoríaProyecto desarrollado por: Diana GuerreroProyecto Académico / Portafolio Android Java
