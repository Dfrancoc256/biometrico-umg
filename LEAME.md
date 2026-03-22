# Sistema de Control Biométrico UMG
## Universidad Mariano Gálvez — Sede La Florida, Zona 19

---

## Descripción General

Sistema de escritorio en **Java puro (JavaFX + PostgreSQL)** para el control de acceso biométrico, registro de estudiantes y control de asistencia de la Universidad Mariano Gálvez, Sede La Florida, Zona 19.

**No requiere internet para funcionar.** Toda la configuración se realiza en el archivo `configuracion.properties`.

---

## Funcionalidades

| Módulo | Descripción |
|--------|-------------|
| **Proceso 1 — Registro Biográfico/Biométrico** | Registro de personas con foto (webcam o archivo), extracción de encoding facial, generación de carnet PDF con QR, notificación por correo y WhatsApp |
| **Proceso 2 — Control de Ingreso (Puerta Principal)** | Reconocimiento facial en tiempo real, registro de ingreso, alerta de personas restringidas |
| **Proceso 3 — Control de Ingreso (Salón de Clases)** | Igual al proceso 2, pero orientado a salones |
| **Proceso 4 — Dashboard Catedrático** | Árbol visual con foto, nombre y correo de cada estudiante (verde=presente, rojo=ausente), confirmación de asistencia, PDF por correo |
| **Proceso 5 — Reportes y Estadísticas** | 4 árboles de reporte: histórico por puerta, por fecha/puerta, histórico por salón, por fecha/salón |
| **Módulo 6 — Personas Restringidas** | Registro de restricciones con motivo, deshabilitar acceso, búsqueda y filtrado |

---

## Requisitos del Sistema

### Mínimos
| Componente | Requisito |
|-----------|-----------|
| Sistema Operativo | Windows 10/11, Linux (Ubuntu 20.04+), macOS 11+ |
| Java | JDK 19 o superior (recomendado: JDK 21 LTS) |
| RAM | 4 GB mínimo (8 GB recomendado) |
| Espacio en disco | 500 MB libres |
| Cámara web | Cualquier cámara compatible con OpenCV (UVC) |
| Base de datos | PostgreSQL 13 o superior |

### Descarga de Java JDK
- **Windows/Mac/Linux:** https://adoptium.net/es/ (Eclipse Temurin — gratuito)
- Seleccionar: Versión 21 (LTS) → paquete JDK → su sistema operativo

---

## Instalación Paso a Paso

### Paso 1 — Instalar PostgreSQL

**Windows:**
1. Descargar desde: https://www.postgresql.org/download/windows/
2. Instalar con las opciones predeterminadas
3. Anotar la contraseña que establezca para el usuario `postgres`
4. Puerto predeterminado: `5432`

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib -y
sudo systemctl start postgresql
sudo systemctl enable postgresql
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'admin';"
```

**macOS:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

---

### Paso 2 — Crear la Base de Datos

Abrir la terminal/símbolo del sistema y ejecutar:

```bash
# Conectarse a PostgreSQL
psql -U postgres

# Dentro de psql, crear la base de datos:
CREATE DATABASE biometrico_umg;
\q
```

**O usando pgAdmin** (interfaz gráfica de PostgreSQL):
1. Abrir pgAdmin → Servers → PostgreSQL → Databases
2. Clic derecho → Create → Database
3. Nombre: `biometrico_umg`
4. Guardar

---

### Paso 3 — Ejecutar el Script de Base de Datos

```bash
psql -U postgres -d biometrico_umg -f esquema_bd.sql
```

Este script crea automáticamente:
- Todas las tablas con índices optimizados
- Vistas de consulta útiles
- La instalación UMG Sede La Florida con 10 puertas/salones
- Un usuario administrador predeterminado

---

### Paso 4 — Configurar el Archivo de Propiedades

Editar el archivo `configuracion.properties` en la misma carpeta que el JAR:

```properties
# Base de datos
bd.host=localhost
bd.puerto=5432
bd.nombre=biometrico_umg
bd.usuario=postgres
bd.contrasena=SU_CONTRASENA_AQUI

# Correo (opcional — dejar vacío para deshabilitar)
correo.host=smtp.gmail.com
correo.puerto=587
correo.usuario=su_correo@gmail.com
correo.contrasena=contrasena_de_aplicacion_gmail

# WhatsApp (opcional — dejar vacío para deshabilitar)
whatsapp.apikey=
```

---

### Paso 5 — Ejecutar la Aplicación

```bash
java -jar registro-biometrico-2.0-2026-all.jar
```

**En Windows:** Doble clic sobre el archivo `.jar`
*(si Java está instalado correctamente como programa predeterminado para .jar)*

---

## Credenciales Predeterminadas

| Campo | Valor |
|-------|-------|
| Número de carnet | `ADMIN-001` |
| Contraseña | `admin123` |
| Tipo de usuario | Administrativo |

**Importante:** Cambiar la contraseña del administrador después del primer inicio.

---

## Configuración de Gmail para Envío de Correos

1. Iniciar sesión en **myaccount.google.com**
2. Ir a **Seguridad** → **Verificación en dos pasos** → Activar
3. Ir a **Seguridad** → **Contraseñas de aplicaciones**
4. Seleccionar "Correo" y "Windows/Mac/Linux" → Generar
5. Copiar la contraseña de 16 caracteres generada
6. Pegarla en `configuracion.properties` → `correo.contrasena`

---

## Configuración de Notificaciones WhatsApp (CallMeBot)

1. En WhatsApp, enviar el mensaje exacto al número **+34 644 82 93 46**:
   ```
   I allow callmebot to send me messages
   ```
2. Esperar la respuesta (puede tomar 1-2 minutos)
3. La respuesta incluirá su `apikey`. Copiarla en `configuracion.properties` → `whatsapp.apikey`
4. Los números de teléfono de los usuarios deben incluir el código de país sin `+`
   - Guatemala: `502XXXXXXXX`
   - México: `52XXXXXXXXXX`

---

## Estructura de Archivos del Proyecto

```
registro-biometrico-2.0-2026/
├── registro-biometrico-2.0-2026-all.jar   ← Ejecutable principal
├── configuracion.properties                ← Configuración (EDITAR PRIMERO)
├── esquema_bd.sql                          ← Script de base de datos
├── LEAME.md                               ← Este archivo
├── fotos_personas/                         ← Creado automáticamente
├── carnets/                                ← PDFs de carnets generados
└── reportes/                               ← PDFs de reportes de asistencia
```

---

## Estructura de la Base de Datos

```
biometrico_umg
├── personas              — Estudiantes, catedráticos, administrativos
├── instalaciones         — Edificios/sedes
├── puertas               — Puertas de acceso y salones de clase
├── cursos                — Materias académicas
├── curso_estudiantes     — Inscripciones (N:M)
├── registro_ingreso      — Historial de ingresos por facial/manual
├── asistencia            — Registro oficial de asistencia por clase
├── v_ingresos            — Vista: ingresos con datos completos
└── v_asistencia          — Vista: asistencia con datos completos
```

---

## Reconocimiento Facial

El sistema usa **OpenCV 4.7** con el detector Haar Cascade para detectar rostros y un algoritmo de **similitud coseno** sobre parches faciales normalizados de 64×64 píxeles.

- El umbral de similitud es `0.83` (83%)
- Para mejores resultados: iluminación frontal uniforme, sin lentes oscuros
- El modelo se re-entrena automáticamente al abrir la pantalla de ingreso

---

## Solución de Problemas Comunes

| Problema | Solución |
|---------|---------|
| "No se encontró configuracion.properties" | Colocar el archivo en la misma carpeta que el JAR |
| "Connection refused" en BD | Verificar que PostgreSQL esté corriendo en el puerto 5432 |
| "Authentication failed" | Verificar usuario y contraseña en `configuracion.properties` |
| Cámara no disponible | Conectar la cámara antes de abrir la ventana de ingreso |
| JAR no abre al doble clic | Ejecutar desde la terminal: `java -jar nombre.jar` |
| Correo no se envía | Usar contraseña de aplicación de Gmail (no la contraseña normal) |

---

## Compilar desde el Código Fuente (Opcional)

Si desea compilar el proyecto usted mismo:

**Requisitos adicionales:**
- Apache Maven 3.8 o superior: https://maven.apache.org/download.cgi

```bash
# Clonar o descomprimir el proyecto
cd java-biometric

# Compilar y generar el JAR ejecutable
mvn package -DskipTests

# El JAR estará en:
# target/registro-biometrico-2.0-2026-all.jar
```

---

## Tecnologías Utilizadas

| Librería | Versión | Función | Licencia |
|----------|---------|---------|---------|
| OpenJFX (JavaFX) | 19 | Interfaz gráfica | GPL v2 + CE |
| PostgreSQL JDBC | 42.7.3 | Conexión a base de datos | BSD-2 |
| OpenCV (openpnp) | 4.7.0 | Reconocimiento facial | Apache 2.0 |
| iText PDF | 5.5.13 | Generación de carnets PDF | AGPL |
| ZXing | 3.5.3 | Generación de códigos QR | Apache 2.0 |
| JavaMail | 1.6.2 | Envío de correos SMTP | CDDL |
| jBCrypt | 0.4 | Hash de contraseñas | ISC |
| ControlsFX | 11.1.2 | Componentes UI adicionales | BSD-3 |
| Gson | 2.10.1 | Procesamiento JSON | Apache 2.0 |

---

## Arquitectura MVC

```
umg.biometrico/
├── Iniciador.java              ← main() — punto de entrada
├── AplicacionBiometrica.java   ← Ciclo de vida JavaFX
├── configuracion/              ← Conexión BD y propiedades
├── modelo/                     ← Entidades del dominio
├── dao/                        ← Acceso a datos (JDBC)
├── servicio/                   ← Lógica de negocio
├── controlador/                ← Controladores FXML (MVC)
└── util/                       ← Sesión actual y utilidades
```

---

*© 2026 Universidad Mariano Gálvez de Guatemala — Sede La Florida, Zona 19*
*Desarrollado en Java 19 + JavaFX + PostgreSQL*
