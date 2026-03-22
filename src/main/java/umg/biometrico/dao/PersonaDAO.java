package umg.biometrico.dao;

import org.mindrot.jbcrypt.BCrypt;
import umg.biometrico.configuracion.ConfiguracionBD;
import umg.biometrico.modelo.Persona;
import umg.biometrico.modelo.TipoPersona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PersonaDAO - Operaciones de base de datos para la entidad Persona.
 */
public class PersonaDAO {

    public Persona insertar(Persona persona) throws SQLException {
        String sql = """
            INSERT INTO personas (nombre,apellido,telefono,correo,foto_ruta,encoding_facial,
                                  tipo_persona,carrera,seccion,numero_carnet,contrasena,activo)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,TRUE)
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String carnetNuevo = generarNumeroCarnet(con);
            ps.setString(1,  persona.getNombre());
            ps.setString(2,  persona.getApellido());
            ps.setString(3,  persona.getTelefono());
            ps.setString(4,  persona.getCorreo());
            ps.setString(5,  persona.getFotoRuta());
            ps.setString(6,  persona.getEncodingFacial());
            ps.setString(7,  persona.getTipoPersona().name());
            ps.setString(8,  persona.getCarrera());
            ps.setString(9,  persona.getSeccion());
            ps.setString(10, carnetNuevo);
            String hash = (persona.getContrasena() != null && !persona.getContrasena().isEmpty())
                    ? BCrypt.hashpw(persona.getContrasena(), BCrypt.gensalt()) : null;
            ps.setString(11, hash);
            ps.executeUpdate();
            ResultSet llaves = ps.getGeneratedKeys();
            if (llaves.next()) persona.setId(llaves.getInt(1));
            persona.setNumeroCarnet(carnetNuevo);
        }
        return persona;
    }

    public void actualizar(Persona persona) throws SQLException {
        String sql = """
            UPDATE personas SET nombre=?,apellido=?,telefono=?,correo=?,foto_ruta=?,
                                encoding_facial=?,tipo_persona=?,carrera=?,seccion=?,
                                numero_carnet=?,activo=? WHERE id=?
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,  persona.getNombre());
            ps.setString(2,  persona.getApellido());
            ps.setString(3,  persona.getTelefono());
            ps.setString(4,  persona.getCorreo());
            ps.setString(5,  persona.getFotoRuta());
            ps.setString(6,  persona.getEncodingFacial());
            ps.setString(7,  persona.getTipoPersona().name());
            ps.setString(8,  persona.getCarrera());
            ps.setString(9,  persona.getSeccion());
            ps.setString(10, persona.getNumeroCarnet());
            ps.setBoolean(11,persona.isActivo());
            ps.setInt(12,    persona.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarEncoding(int idPersona, String encoding) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE personas SET encoding_facial=? WHERE id=?")) {
            ps.setString(1, encoding); ps.setInt(2, idPersona);
            ps.executeUpdate();
        }
    }

    public Persona buscarPorId(int id) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM personas WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearFila(rs) : null;
        }
    }

    public Persona buscarPorCarnet(String numeroCarnet) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM personas WHERE numero_carnet=?")) {
            ps.setString(1, numeroCarnet);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearFila(rs) : null;
        }
    }

    public Persona autenticar(String numeroCarnet, String contrasenaPlana) throws SQLException {
        Persona persona = buscarPorCarnet(numeroCarnet);
        if (persona == null || persona.getContrasena() == null) return null;
        return BCrypt.checkpw(contrasenaPlana, persona.getContrasena()) ? persona : null;
    }

    public List<Persona> listarTodos() throws SQLException {
        return ejecutarListado("SELECT * FROM personas ORDER BY apellido,nombre");
    }

    public List<Persona> listarActivos() throws SQLException {
        return ejecutarListado("SELECT * FROM personas WHERE activo=TRUE ORDER BY apellido,nombre");
    }

    public List<Persona> listarEstudiantes() throws SQLException {
        return ejecutarListado(
                "SELECT * FROM personas WHERE tipo_persona='ESTUDIANTE' AND activo=TRUE ORDER BY apellido,nombre");
    }

    public List<Persona> listarRestringidos() throws SQLException {
        return ejecutarListado("SELECT * FROM personas WHERE restringido=TRUE ORDER BY apellido,nombre");
    }

    public void restringir(int idPersona, String motivo) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE personas SET restringido=TRUE,motivo_restriccion=? WHERE id=?")) {
            ps.setString(1, motivo); ps.setInt(2, idPersona); ps.executeUpdate();
        }
    }

    public void desrestringir(int idPersona) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE personas SET restringido=FALSE,motivo_restriccion=NULL WHERE id=?")) {
            ps.setInt(1, idPersona); ps.executeUpdate();
        }
    }

    private String generarNumeroCarnet(Connection con) throws SQLException {
        int anio = java.time.Year.now().getValue();
        try (ResultSet rs = con.createStatement().executeQuery(
                "SELECT COUNT(*)+1 AS siguiente FROM personas")) {
            rs.next();
            return String.format("UMG-%d-%04d", anio, rs.getInt("siguiente"));
        }
    }

    private List<Persona> ejecutarListado(String sql) throws SQLException {
        List<Persona> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    private Persona mapearFila(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setFotoRuta(rs.getString("foto_ruta"));
        p.setEncodingFacial(rs.getString("encoding_facial"));
        p.setTipoPersona(TipoPersona.desdeCadena(rs.getString("tipo_persona")));
        p.setCarrera(rs.getString("carrera"));
        p.setSeccion(rs.getString("seccion"));
        p.setNumeroCarnet(rs.getString("numero_carnet"));
        p.setContrasena(rs.getString("contrasena"));
        p.setActivo(rs.getBoolean("activo"));
        p.setRestringido(rs.getBoolean("restringido"));
        p.setMotivoRestriccion(rs.getString("motivo_restriccion"));
        return p;
    }
}
