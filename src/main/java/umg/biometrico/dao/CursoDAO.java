package umg.biometrico.dao;

import umg.biometrico.configuracion.ConfiguracionBD;
import umg.biometrico.modelo.Curso;
import umg.biometrico.modelo.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CursoDAO - Operaciones de base de datos para la entidad Curso.
 */
public class CursoDAO {

    public Curso insertar(Curso curso) throws SQLException {
        String sql = """
            INSERT INTO cursos (codigo,nombre,catedratico_id,salon,horario,activo)
            VALUES (?,?,?,?,?,TRUE)
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, curso.getCodigo());
            ps.setString(2, curso.getNombre());
            ps.setInt   (3, curso.getCatedraticoId());
            ps.setString(4, curso.getSalon());
            ps.setString(5, curso.getHorario());
            ps.executeUpdate();
            ResultSet llaves = ps.getGeneratedKeys();
            if (llaves.next()) curso.setId(llaves.getInt(1));
        }
        return curso;
    }

    public List<Curso> listarPorCatedratico(int idCatedratico) throws SQLException {
        String sql = """
            SELECT c.*, p.nombre||' '||p.apellido AS catedratico_nombre
            FROM cursos c
            LEFT JOIN personas p ON c.catedratico_id = p.id
            WHERE c.catedratico_id=? AND c.activo=TRUE
            ORDER BY c.nombre
        """;
        List<Curso> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCatedratico);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    public List<Curso> listarTodos() throws SQLException {
        String sql = """
            SELECT c.*, p.nombre||' '||p.apellido AS catedratico_nombre
            FROM cursos c
            LEFT JOIN personas p ON c.catedratico_id = p.id
            WHERE c.activo=TRUE ORDER BY c.nombre
        """;
        List<Curso> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    public Curso buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT c.*, p.nombre||' '||p.apellido AS catedratico_nombre
            FROM cursos c LEFT JOIN personas p ON c.catedratico_id=p.id WHERE c.id=?
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearFila(rs) : null;
        }
    }

    public void inscribirEstudiante(int idCurso, int idEstudiante) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO curso_estudiantes (curso_id,estudiante_id) VALUES (?,?) ON CONFLICT DO NOTHING")) {
            ps.setInt(1, idCurso); ps.setInt(2, idEstudiante);
            ps.executeUpdate();
        }
    }

    public void desinscribirEstudiante(int idCurso, int idEstudiante) throws SQLException {
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM curso_estudiantes WHERE curso_id=? AND estudiante_id=?")) {
            ps.setInt(1, idCurso); ps.setInt(2, idEstudiante);
            ps.executeUpdate();
        }
    }

    public List<Persona> listarEstudiantesDeCurso(int idCurso) throws SQLException {
        String sql = """
            SELECT p.* FROM personas p
            INNER JOIN curso_estudiantes ce ON p.id = ce.estudiante_id
            WHERE ce.curso_id=? ORDER BY p.apellido, p.nombre
        """;
        List<Persona> lista = new ArrayList<>();
        PersonaDAO personaDAO = new PersonaDAO();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Persona p = new Persona();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setCorreo(rs.getString("correo"));
                p.setFotoRuta(rs.getString("foto_ruta"));
                p.setNumeroCarnet(rs.getString("numero_carnet"));
                lista.add(p);
            }
        }
        return lista;
    }

    private Curso mapearFila(ResultSet rs) throws SQLException {
        Curso c = new Curso();
        c.setId(rs.getInt("id"));
        c.setCodigo(rs.getString("codigo"));
        c.setNombre(rs.getString("nombre"));
        c.setCatedraticoId(rs.getInt("catedratico_id"));
        try { c.setCatedraticoNombre(rs.getString("catedratico_nombre")); } catch (Exception ignored) {}
        c.setSalon(rs.getString("salon"));
        c.setHorario(rs.getString("horario"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
