package umg.biometrico.configuracion;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * ConfiguracionBD - Gestiona la conexion a la base de datos PostgreSQL.
 *
 * Lee parametros desde "configuracion.properties" en el directorio de trabajo.
 * Si no existe en disco, lo busca dentro del JAR (classpath).
 *
 * IMPORTANTE: Cada llamada a obtenerConexion() abre una conexion nueva.
 * Siempre use try-with-resources para cerrarla automaticamente.
 * Ejemplo:
 *   try (Connection con = ConfiguracionBD.obtenerConexion();
 *        PreparedStatement ps = con.prepareStatement("...")) { ... }
 */
public class ConfiguracionBD {

    private static final Properties propiedades = new Properties();
    private static String urlJdbc;
    private static String usuarioBd;
    private static String contrasenaBd;

    static {
        cargarPropiedades();
        construirUrl();
    }

    // ------------------------------------------------------------------
    // Carga de propiedades
    // ------------------------------------------------------------------

    private static void cargarPropiedades() {
        File archivoDisco = new File("configuracion.properties");
        try {
            if (archivoDisco.exists()) {
                propiedades.load(new FileInputStream(archivoDisco));
                System.out.println("Configuracion cargada desde: " + archivoDisco.getAbsolutePath());
            } else {
                InputStream flujoInterno = ConfiguracionBD.class
                        .getResourceAsStream("/configuracion.properties");
                if (flujoInterno != null) {
                    propiedades.load(flujoInterno);
                    System.out.println("Configuracion cargada desde classpath (JAR).");
                } else {
                    System.err.println("Advertencia: No se encontro configuracion.properties");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar configuracion.properties: " + e.getMessage());
        }
    }

    private static void construirUrl() {
        String host   = propiedades.getProperty("bd.host",      "localhost");
        String puerto = propiedades.getProperty("bd.puerto",    "5432");
        String nombre = propiedades.getProperty("bd.nombre",    "biometrico_umg");
        usuarioBd     = propiedades.getProperty("bd.usuario",   "postgres");
        contrasenaBd  = propiedades.getProperty("bd.contrasena","");

        urlJdbc = "jdbc:postgresql://" + host + ":" + puerto + "/" + nombre
                + "?characterEncoding=UTF-8"
                + "&connectTimeout=10"
                + "&socketTimeout=30"
                + "&ApplicationName=UMG-Biometrico";

        System.out.println("URL JDBC configurada: " + urlJdbc.split("\\?")[0]);
    }

    // ------------------------------------------------------------------
    // API publica
    // ------------------------------------------------------------------

    /**
     * Devuelve una propiedad de configuracion.
     */
    public static String obtenerPropiedad(String clave) {
        return propiedades.getProperty(clave, "");
    }

    /**
     * Devuelve una propiedad de configuracion con valor predeterminado.
     */
    public static String obtenerPropiedad(String clave, String valorPredeterminado) {
        return propiedades.getProperty(clave, valorPredeterminado);
    }

    /**
     * Abre y devuelve una conexion JDBC nueva.
     * SIEMPRE se debe usar dentro de un try-with-resources.
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(urlJdbc, usuarioBd, contrasenaBd);
    }

    /**
     * Verifica que la BD sea accesible.
     * @return true si la conexion funciona
     */
    public static boolean probarConexion() {
        try (Connection con = obtenerConexion()) {
            return con.isValid(5);
        } catch (SQLException e) {
            System.err.println("Prueba de conexion fallida: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Inicializacion del esquema
    // ------------------------------------------------------------------

    /**
     * Crea todas las tablas si no existen e inserta datos iniciales.
     * Es seguro ejecutar multiples veces (idempotente).
     */
    public static void inicializarEsquema() {
        try (Connection con = obtenerConexion()) {
            crearTablas(con);
            insertarDatosPredeterminados(con);
            System.out.println("Esquema de base de datos inicializado correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al inicializar esquema de BD: " + e.getMessage());
        }
    }

    private static void crearTablas(Connection con) throws SQLException {
        String[] sentencias = {
            // Tabla personas
            """
            CREATE TABLE IF NOT EXISTS personas (
                id                 SERIAL PRIMARY KEY,
                nombre             VARCHAR(100) NOT NULL,
                apellido           VARCHAR(100) NOT NULL,
                telefono           VARCHAR(25),
                correo             VARCHAR(200) NOT NULL,
                foto_ruta          VARCHAR(400),
                encoding_facial    TEXT,
                tipo_persona       VARCHAR(30) NOT NULL DEFAULT 'ESTUDIANTE',
                carrera            VARCHAR(200),
                seccion            VARCHAR(20),
                numero_carnet      VARCHAR(40) UNIQUE,
                contrasena         VARCHAR(200),
                activo             BOOLEAN NOT NULL DEFAULT TRUE,
                restringido        BOOLEAN NOT NULL DEFAULT FALSE,
                motivo_restriccion TEXT,
                fecha_registro     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // Indices personas
            "CREATE INDEX IF NOT EXISTS idx_personas_carnet      ON personas(numero_carnet)",
            "CREATE INDEX IF NOT EXISTS idx_personas_tipo        ON personas(tipo_persona)",
            "CREATE INDEX IF NOT EXISTS idx_personas_restringido ON personas(restringido) WHERE restringido = TRUE",

            // Tabla instalaciones
            """
            CREATE TABLE IF NOT EXISTS instalaciones (
                id         SERIAL PRIMARY KEY,
                nombre     VARCHAR(200) NOT NULL,
                direccion  VARCHAR(300)
            )
            """,

            // Tabla puertas
            """
            CREATE TABLE IF NOT EXISTS puertas (
                id             SERIAL PRIMARY KEY,
                instalacion_id INTEGER NOT NULL REFERENCES instalaciones(id) ON DELETE CASCADE,
                nombre         VARCHAR(150) NOT NULL,
                nivel          VARCHAR(50),
                es_salon       BOOLEAN NOT NULL DEFAULT FALSE,
                descripcion    VARCHAR(300)
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_puertas_instalacion ON puertas(instalacion_id)",
            "CREATE INDEX IF NOT EXISTS idx_puertas_es_salon    ON puertas(es_salon)",

            // Tabla cursos
            """
            CREATE TABLE IF NOT EXISTS cursos (
                id               SERIAL PRIMARY KEY,
                codigo           VARCHAR(20) UNIQUE,
                nombre           VARCHAR(200) NOT NULL,
                catedratico_id   INTEGER REFERENCES personas(id) ON DELETE SET NULL,
                salon            VARCHAR(80),
                horario          VARCHAR(150),
                activo           BOOLEAN NOT NULL DEFAULT TRUE
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_cursos_catedratico ON cursos(catedratico_id)",

            // Tabla curso_estudiantes (inscripciones)
            """
            CREATE TABLE IF NOT EXISTS curso_estudiantes (
                curso_id          INTEGER NOT NULL REFERENCES cursos(id)   ON DELETE CASCADE,
                estudiante_id     INTEGER NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
                fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (curso_id, estudiante_id)
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_ce_estudiante ON curso_estudiantes(estudiante_id)",

            // Tabla registro_ingreso
            """
            CREATE TABLE IF NOT EXISTS registro_ingreso (
                id           SERIAL PRIMARY KEY,
                persona_id   INTEGER NOT NULL REFERENCES personas(id)  ON DELETE CASCADE,
                puerta_id    INTEGER NOT NULL REFERENCES puertas(id)   ON DELETE CASCADE,
                fecha_hora   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                metodo       VARCHAR(20) NOT NULL DEFAULT 'FACIAL'
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_ingreso_persona ON registro_ingreso(persona_id)",
            "CREATE INDEX IF NOT EXISTS idx_ingreso_puerta  ON registro_ingreso(puerta_id)",
            "CREATE INDEX IF NOT EXISTS idx_ingreso_fecha   ON registro_ingreso(fecha_hora)",

            // Tabla asistencia
            """
            CREATE TABLE IF NOT EXISTS asistencia (
                id            SERIAL PRIMARY KEY,
                estudiante_id INTEGER NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
                curso_id      INTEGER NOT NULL REFERENCES cursos(id)   ON DELETE CASCADE,
                fecha         DATE NOT NULL,
                presente      BOOLEAN NOT NULL DEFAULT FALSE,
                hora_registro TIMESTAMP,
                UNIQUE (estudiante_id, curso_id, fecha)
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_asistencia_curso      ON asistencia(curso_id, fecha)",
            "CREATE INDEX IF NOT EXISTS idx_asistencia_estudiante ON asistencia(estudiante_id)",

            // Vistas
            """
            CREATE OR REPLACE VIEW v_ingresos AS
            SELECT
                ri.id,
                ri.fecha_hora,
                ri.metodo,
                p.nombre || ' ' || p.apellido AS persona_nombre,
                p.numero_carnet,
                p.tipo_persona,
                pu.nombre    AS puerta_nombre,
                pu.es_salon,
                pu.nivel,
                i.nombre     AS instalacion_nombre
            FROM registro_ingreso ri
            JOIN personas      p  ON ri.persona_id   = p.id
            JOIN puertas       pu ON ri.puerta_id    = pu.id
            JOIN instalaciones i  ON pu.instalacion_id = i.id
            """,

            """
            CREATE OR REPLACE VIEW v_asistencia AS
            SELECT
                a.id,
                a.fecha,
                a.presente,
                a.hora_registro,
                p.nombre || ' ' || p.apellido AS estudiante_nombre,
                p.numero_carnet,
                p.correo                       AS correo_estudiante,
                c.nombre                       AS curso_nombre,
                c.codigo                       AS curso_codigo,
                cat.nombre || ' ' || cat.apellido AS catedratico_nombre
            FROM asistencia a
            JOIN personas p   ON a.estudiante_id = p.id
            JOIN cursos   c   ON a.curso_id      = c.id
            LEFT JOIN personas cat ON c.catedratico_id = cat.id
            """
        };

        try (Statement stmt = con.createStatement()) {
            for (String sql : sentencias) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Ignorar errores de "ya existe" en indices/vistas
                    if (!e.getMessage().contains("already exists")) {
                        throw e;
                    }
                }
            }
        }
    }

    private static void insertarDatosPredeterminados(Connection con) throws SQLException {
        // Instalacion predeterminada
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO instalaciones (nombre, direccion) VALUES (?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, "UMG Sede La Florida");
            ps.setString(2, "Zona 19, La Florida, Ciudad de Guatemala");
            ps.executeUpdate();
        }

        // Puertas y salones
        try (ResultSet rs = con.createStatement().executeQuery(
                "SELECT id FROM instalaciones WHERE nombre='UMG Sede La Florida' LIMIT 1")) {
            if (rs.next()) {
                int instId = rs.getInt(1);
                insertarPuertasSiNoExisten(con, instId);
            }
        }

        // Administrador predeterminado
        try (ResultSet rs = con.createStatement().executeQuery(
                "SELECT COUNT(*) FROM personas WHERE numero_carnet='ADMIN-001'")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO personas
                        (nombre, apellido, correo, tipo_persona, numero_carnet, contrasena, activo)
                    VALUES
                        ('Administrador','Sistema','admin@umg.edu.gt',
                         'ADMINISTRATIVO','ADMIN-001',?,TRUE)
                """)) {
                    ps.setString(1, BCrypt.hashpw("admin123", BCrypt.gensalt()));
                    ps.executeUpdate();
                    System.out.println("Usuario admin creado — carnet: ADMIN-001 / pass: admin123");
                }
            }
        }
    }

    private static void insertarPuertasSiNoExisten(Connection con, int instId) throws SQLException {
        String sql = """
            INSERT INTO puertas (instalacion_id, nombre, nivel, es_salon, descripcion)
            VALUES (?,?,?,?,?)
            ON CONFLICT DO NOTHING
        """;
        Object[][] puertas = {
            // { nombre, nivel, es_salon, descripcion }
            {"Puerta Principal",    "Planta baja", false, "Acceso principal al edificio"},
            {"Puerta Lateral",      "Planta baja", false, "Acceso lateral / emergencia"},
            {"Salon A-101",         "Nivel 1",     true,  "Salon de clases A-101"},
            {"Salon A-102",         "Nivel 1",     true,  "Salon de clases A-102"},
            {"Salon A-103",         "Nivel 1",     true,  "Salon de clases A-103"},
            {"Salon A-201",         "Nivel 2",     true,  "Salon de clases A-201"},
            {"Salon A-202",         "Nivel 2",     true,  "Salon de clases A-202"},
            {"Salon A-203",         "Nivel 2",     true,  "Salon de clases A-203"},
            {"Lab. Computacion 1",  "Nivel 3",     true,  "Laboratorio de computacion 1"},
            {"Lab. Computacion 2",  "Nivel 3",     true,  "Laboratorio de computacion 2"},
        };
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] p : puertas) {
                ps.setInt(1,     instId);
                ps.setString(2,  (String)  p[0]);
                ps.setString(3,  (String)  p[1]);
                ps.setBoolean(4, (Boolean) p[2]);
                ps.setString(5,  (String)  p[3]);
                ps.executeUpdate();
            }
        }
    }
}
