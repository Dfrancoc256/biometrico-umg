# Sistema Biometrico UMG — Version Escritorio (JavaFX)

**Universidad Mariano Galvez de Guatemala — Sede La Florida, Zona 19**

Aplicacion de escritorio en **Java puro** para el control de acceso biometrico.

## Tecnologias
- Java 19 + JavaFX 19
- PostgreSQL 13+
- OpenCV 4.7 (reconocimiento facial)
- iText PDF + ZXing QR (carnets)
- JavaMail (notificaciones correo)
- CallMeBot API (notificaciones WhatsApp)

## Modulos
| Proceso | Descripcion |
|---------|-------------|
| 1 | Registro biografico y biometrico con webcam |
| 2 | Control de ingreso facial — Puerta Principal |
| 3 | Control de ingreso facial — Salon de Clases |
| 4 | Dashboard catedratico con arbol visual de estudiantes |
| 5 | Reportes PDF con 4 arboles de datos |
| 6 | Gestion de personas restringidas |

## Compilar
```bash
git clone https://github.com/Dfrancoc256/biometrico-umg.git
cd biometrico-umg
mvn package -DskipTests
java -jar target/registro-biometrico-2.0-2026-all.jar
```

## Requisitos
- Java 21 LTS: https://adoptium.net/es/
- PostgreSQL 13+: https://www.postgresql.org/download/
- Configurar conexion en `configuracion.properties` junto al JAR

## Credenciales predeterminadas
| Carnet | Contrasena |
|--------|------------|
| ADMIN-001 | admin123 |

## Version Web
Existe tambien una version web (Spring Boot): https://github.com/Dfrancoc256/biometrico-umg-web

---
*Universidad Mariano Galvez — Sede La Florida, Zona 19 — 2026*