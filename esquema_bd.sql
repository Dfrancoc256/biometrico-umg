-- ============================================================
--  SISTEMA BIOMETRICO UMG - Sede La Florida Zona 19
--  Script de inicializacion de la base de datos PostgreSQL
--  2026
-- ============================================================
-- Ejecutar con:
--   psql -U postgres -d biometrico_umg -f esquema_bd.sql
-- O crear la BD primero:
--   psql -U postgres -c "CREATE DATABASE biometrico_umg;"
--   psql -U postgres -d biometrico_umg -f esquema_bd.sql
-- ============================================================

-- Extensiones utiles
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================
-- TABLA: personas
-- Almacena estudiantes, catedraticos, administrativos, etc.
-- ============================================================
CREATE TABLE IF NOT EXISTS personas (
    id                 SERIAL PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    apellido           VARCHAR(100) NOT NULL,
    telefono           VARCHAR(25),
    correo             VARCHAR(200) NOT NULL,
    foto_ruta          VARCHAR(400),
    encoding_facial    TEXT,                                  -- Vector flotante separado por comas
    tipo_persona       VARCHAR(30) NOT NULL DEFAULT 'ESTUDIANTE',
                       -- Valores: ESTUDIANTE, CATEDRATICO, MANTENIMIENTO, ADMINISTRATIVO, VISITANTE
    carrera            VARCHAR(200),
    seccion            VARCHAR(20),
    numero_carnet      VARCHAR(40) UNIQUE,                    -- Ej: UMG-2026-0001
    contrasena         VARCHAR(200),                          -- Hash BCrypt
    activo             BOOLEAN NOT NULL DEFAULT TRUE,
    restringido        BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_restriccion TEXT,
    fecha_registro     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices para busqueda rapida
CREATE INDEX IF NOT EXISTS idx_personas_carnet       ON personas(numero_carnet);
CREATE INDEX IF NOT EXISTS idx_personas_correo       ON personas(correo);
CREATE INDEX IF NOT EXISTS idx_personas_tipo         ON personas(tipo_persona);
CREATE INDEX IF NOT EXISTS idx_personas_restringido  ON personas(restringido) WHERE restringido = TRUE;

-- ============================================================
-- TABLA: instalaciones
-- Edificios o sedes de la UMG
-- ============================================================
CREATE TABLE IF NOT EXISTS instalaciones (
    id         SERIAL PRIMARY KEY,
    nombre     VARCHAR(200) NOT NULL,
    direccion  VARCHAR(300)
);

-- ============================================================
-- TABLA: puertas
-- Puertas de acceso y salones dentro de una instalacion
-- ============================================================
CREATE TABLE IF NOT EXISTS puertas (
    id             SERIAL PRIMARY KEY,
    instalacion_id INTEGER NOT NULL REFERENCES instalaciones(id) ON DELETE CASCADE,
    nombre         VARCHAR(150) NOT NULL,
    nivel          VARCHAR(50),
    es_salon       BOOLEAN NOT NULL DEFAULT FALSE,
    descripcion    VARCHAR(300)
);

CREATE INDEX IF NOT EXISTS idx_puertas_instalacion ON puertas(instalacion_id);
CREATE INDEX IF NOT EXISTS idx_puertas_es_salon    ON puertas(es_salon);

-- ============================================================
-- TABLA: cursos
-- Materias o cursos asignados a un catedratico
-- ============================================================
CREATE TABLE IF NOT EXISTS cursos (
    id               SERIAL PRIMARY KEY,
    codigo           VARCHAR(20) UNIQUE,
    nombre           VARCHAR(200) NOT NULL,
    catedratico_id   INTEGER REFERENCES personas(id) ON DELETE SET NULL,
    salon            VARCHAR(80),
    horario          VARCHAR(150),                            -- Ej: "Lunes y Miercoles 07:00-09:00"
    activo           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_cursos_catedratico ON cursos(catedratico_id);

-- ============================================================
-- TABLA: curso_estudiantes
-- Relacion N:M entre cursos y estudiantes inscritos
-- ============================================================
CREATE TABLE IF NOT EXISTS curso_estudiantes (
    curso_id      INTEGER NOT NULL REFERENCES cursos(id)   ON DELETE CASCADE,
    estudiante_id INTEGER NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
    fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (curso_id, estudiante_id)
);

CREATE INDEX IF NOT EXISTS idx_ce_estudiante ON curso_estudiantes(estudiante_id);

-- ============================================================
-- TABLA: registro_ingreso
-- Historial de cada ingreso de una persona por una puerta
-- ============================================================
CREATE TABLE IF NOT EXISTS registro_ingreso (
    id           SERIAL PRIMARY KEY,
    persona_id   INTEGER NOT NULL REFERENCES personas(id)   ON DELETE CASCADE,
    puerta_id    INTEGER NOT NULL REFERENCES puertas(id)    ON DELETE CASCADE,
    fecha_hora   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo       VARCHAR(20) NOT NULL DEFAULT 'FACIAL'
                 -- Valores: FACIAL, MANUAL
);

CREATE INDEX IF NOT EXISTS idx_ingreso_persona ON registro_ingreso(persona_id);
CREATE INDEX IF NOT EXISTS idx_ingreso_puerta  ON registro_ingreso(puerta_id);
CREATE INDEX IF NOT EXISTS idx_ingreso_fecha   ON registro_ingreso(fecha_hora);

-- ============================================================
-- TABLA: asistencia
-- Registro oficial de asistencia confirmado por el catedratico
-- ============================================================
CREATE TABLE IF NOT EXISTS asistencia (
    id            SERIAL PRIMARY KEY,
    estudiante_id INTEGER NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
    curso_id      INTEGER NOT NULL REFERENCES cursos(id)   ON DELETE CASCADE,
    fecha         DATE NOT NULL,
    presente      BOOLEAN NOT NULL DEFAULT FALSE,
    hora_registro TIMESTAMP,
    UNIQUE (estudiante_id, curso_id, fecha)
);

CREATE INDEX IF NOT EXISTS idx_asistencia_curso        ON asistencia(curso_id, fecha);
CREATE INDEX IF NOT EXISTS idx_asistencia_estudiante   ON asistencia(estudiante_id);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Instalacion predeterminada
INSERT INTO instalaciones (nombre, direccion)
VALUES ('UMG Sede La Florida', 'Zona 19, La Florida, Ciudad de Guatemala')
ON CONFLICT DO NOTHING;

-- Puertas y salones
DO $$
DECLARE v_inst_id INTEGER;
BEGIN
    SELECT id INTO v_inst_id FROM instalaciones WHERE nombre = 'UMG Sede La Florida' LIMIT 1;

    -- Puerta principal
    INSERT INTO puertas (instalacion_id, nombre, nivel, es_salon, descripcion) VALUES
        (v_inst_id, 'Puerta Principal',  'Planta baja', FALSE, 'Acceso principal al edificio'),
        (v_inst_id, 'Puerta Lateral',    'Planta baja', FALSE, 'Acceso lateral / emergencia')
    ON CONFLICT DO NOTHING;

    -- Salones Nivel 1
    INSERT INTO puertas (instalacion_id, nombre, nivel, es_salon, descripcion) VALUES
        (v_inst_id, 'Salon A-101', 'Nivel 1', TRUE, 'Salon de clases A-101'),
        (v_inst_id, 'Salon A-102', 'Nivel 1', TRUE, 'Salon de clases A-102'),
        (v_inst_id, 'Salon A-103', 'Nivel 1', TRUE, 'Salon de clases A-103')
    ON CONFLICT DO NOTHING;

    -- Salones Nivel 2
    INSERT INTO puertas (instalacion_id, nombre, nivel, es_salon, descripcion) VALUES
        (v_inst_id, 'Salon A-201', 'Nivel 2', TRUE, 'Salon de clases A-201'),
        (v_inst_id, 'Salon A-202', 'Nivel 2', TRUE, 'Salon de clases A-202'),
        (v_inst_id, 'Salon A-203', 'Nivel 2', TRUE, 'Salon de clases A-203')
    ON CONFLICT DO NOTHING;

    -- Laboratorios Nivel 3
    INSERT INTO puertas (instalacion_id, nombre, nivel, es_salon, descripcion) VALUES
        (v_inst_id, 'Lab. Computacion 1', 'Nivel 3', TRUE, 'Laboratorio de computacion 1'),
        (v_inst_id, 'Lab. Computacion 2', 'Nivel 3', TRUE, 'Laboratorio de computacion 2')
    ON CONFLICT DO NOTHING;
END $$;

-- Usuario administrador predeterminado
-- Contrasena: admin123 (Hash BCrypt incluido)
INSERT INTO personas (nombre, apellido, correo, tipo_persona, numero_carnet, contrasena, activo)
VALUES (
    'Administrador', 'Sistema', 'admin@umg.edu.gt',
    'ADMINISTRATIVO', 'ADMIN-001',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVcALjlCnW',
    TRUE
)
ON CONFLICT (numero_carnet) DO NOTHING;

-- Catedratico de prueba
-- Contrasena: catedratico123
INSERT INTO personas (nombre, apellido, correo, tipo_persona, numero_carnet, contrasena, activo)
VALUES (
    'Juan Carlos', 'Gomez Reyes', 'jcgomez@umg.edu.gt',
    'CATEDRATICO', 'UMG-2026-0001',
    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW',
    TRUE
)
ON CONFLICT (numero_carnet) DO NOTHING;

-- Curso de prueba asignado al catedratico anterior
INSERT INTO cursos (codigo, nombre, catedratico_id, salon, horario)
SELECT 'ING-101', 'Introduccion a la Ingenieria en Sistemas',
       p.id, 'Salon A-101', 'Lunes y Miercoles 07:00-09:00'
FROM personas p WHERE p.numero_carnet = 'UMG-2026-0001'
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- VISTAS UTILES
-- ============================================================

-- Vista: ingresos con nombre de persona y puerta
CREATE OR REPLACE VIEW v_ingresos AS
SELECT
    ri.id,
    ri.fecha_hora,
    ri.metodo,
    p.nombre || ' ' || p.apellido     AS persona_nombre,
    p.numero_carnet,
    p.tipo_persona,
    pu.nombre                         AS puerta_nombre,
    pu.es_salon,
    pu.nivel,
    i.nombre                          AS instalacion_nombre
FROM registro_ingreso ri
JOIN personas      p  ON ri.persona_id   = p.id
JOIN puertas       pu ON ri.puerta_id    = pu.id
JOIN instalaciones i  ON pu.instalacion_id = i.id
ORDER BY ri.fecha_hora DESC;

-- Vista: asistencia con nombre de estudiante y curso
CREATE OR REPLACE VIEW v_asistencia AS
SELECT
    a.id,
    a.fecha,
    a.presente,
    a.hora_registro,
    p.nombre || ' ' || p.apellido     AS estudiante_nombre,
    p.numero_carnet,
    p.correo                          AS correo_estudiante,
    c.nombre                          AS curso_nombre,
    c.codigo                          AS curso_codigo,
    cat.nombre || ' ' || cat.apellido AS catedratico_nombre
FROM asistencia a
JOIN personas p   ON a.estudiante_id = p.id
JOIN cursos   c   ON a.curso_id      = c.id
LEFT JOIN personas cat ON c.catedratico_id = cat.id
ORDER BY a.fecha DESC, p.apellido;

-- Vista: resumen de asistencia por curso y fecha
CREATE OR REPLACE VIEW v_resumen_asistencia AS
SELECT
    c.codigo,
    c.nombre AS curso,
    a.fecha,
    COUNT(*)                                      AS total_estudiantes,
    COUNT(*) FILTER (WHERE a.presente = TRUE)     AS presentes,
    COUNT(*) FILTER (WHERE a.presente = FALSE)    AS ausentes,
    ROUND(
        COUNT(*) FILTER (WHERE a.presente = TRUE) * 100.0 / COUNT(*), 1
    )                                             AS porcentaje_asistencia
FROM asistencia a
JOIN cursos c ON a.curso_id = c.id
GROUP BY c.codigo, c.nombre, a.fecha
ORDER BY a.fecha DESC;

-- ============================================================
-- CONSULTAS DE REFERENCIA (para reportes manuales)
-- ============================================================

/*
-- Historico de ingresos por instalacion > puerta > fecha:
SELECT
    i.nombre AS instalacion,
    pu.nombre AS puerta,
    DATE(ri.fecha_hora) AS fecha,
    p.nombre || ' ' || p.apellido AS persona,
    p.numero_carnet,
    ri.metodo,
    ri.fecha_hora::TIME AS hora
FROM registro_ingreso ri
JOIN personas p ON ri.persona_id = p.id
JOIN puertas pu ON ri.puerta_id = pu.id
JOIN instalaciones i ON pu.instalacion_id = i.id
ORDER BY i.nombre, pu.nombre, ri.fecha_hora;

-- Estudiantes presentes en un curso hoy:
SELECT
    p.nombre || ' ' || p.apellido AS estudiante,
    p.correo,
    a.presente,
    a.hora_registro
FROM asistencia a
JOIN personas p ON a.estudiante_id = p.id
WHERE a.curso_id = 1 AND a.fecha = CURRENT_DATE
ORDER BY p.apellido;

-- Personas restringidas:
SELECT
    numero_carnet, nombre, apellido, tipo_persona, motivo_restriccion
FROM personas
WHERE restringido = TRUE;

-- Buscar persona por carnet para control de ingreso:
SELECT id, nombre, apellido, foto_ruta, encoding_facial, restringido, motivo_restriccion
FROM personas
WHERE numero_carnet = 'UMG-2026-0001' AND activo = TRUE;
*/
