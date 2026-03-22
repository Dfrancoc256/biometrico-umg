package umg.biometrico.dao;

import umg.biometrico.configuracion.ConfiguracionBD;
import umg.biometrico.modelo.Asistencia;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AsistenciaDAO - Operaciones de base de datos para la entidad Asistencia.
 */
public class AsistenciaDAO {

    public List<Asistencia> listarPorCursoYFecha(int idCurso, LocalDate fecha) throws SQLException {
        String sql = """
            SELECT a.*, p.nombre||' '||p.apellido AS nombre_estudiante,
                   p.correo AS correo_estudiante, p.foto_ruta AS foto_estudiante
            FROM asistencia a
            INNER JOIN personas p ON a.estudiante_id = p.id
            WHERE a.curso_id=? AND a.fecha=?
            ORDER BY p.apellido, p.nombre
        """;
        List<Asistencia> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.setDate(2, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    /**
     * Confirma la asistencia de un curso para una fecha dada.
     * Inserta registros para todos los estudiantes marcando presentes/ausentes.
     */
    public void confirmarAsistencia(int idCurso, LocalDate fecha, List<Integer> idsPresentes) throws SQLException {
        String sqlBorrar = "DELETE FROM asistencia WHERE curso_id=? AND fecha=?";
        String sqlInsertar = """
            INSERT INTO asistencia (estudiante_id,curso_id,fecha,presente,hora_registro)
            VALUES (?,?,?,?,CURRENT_TIMESTAMP)
            ON CONFLICT (estudiante_id,curso_id,fecha) DO UPDATE
            SET presente=EXCLUDED.presente, hora_registro=EXCLUDED.hora_registro
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                // Obtener todos los estudiantes del curso
                List<Integer> todosEstudiantes = obtenerIdsEstudiantesDeCurso(con, idCurso);

                try (PreparedStatement psIns = con.prepareStatement(sqlInsertar)) {
                    for (int idEst : todosEstudiantes) {
                        boolean presente = idsPresentes.contains(idEst);
                        psIns.setInt(1, idEst);
                        psIns.setInt(2, idCurso);
                        psIns.setDate(3, Date.valueOf(fecha));
                        psIns.setBoolean(4, presente);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Obtiene los IDs de estudiantes con registro de ingreso en una puerta especifica hoy,
     * para marcar automaticamente la asistencia.
     */
    public List<Integer> obtenerPresentesPorIngreso(int idCurso, LocalDate fecha) throws SQLException {
        String sql = """
            SELECT DISTINCT ce.estudiante_id
            FROM curso_estudiantes ce
            INNER JOIN cursos c ON ce.curso_id = c.id
            INNER JOIN registro_ingreso ri ON ri.persona_id = ce.estudiante_id
            WHERE ce.curso_id=?
              AND DATE(ri.fecha_hora) = ?
        """;
        List<Integer> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.setDate(2, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(rs.getInt(1));
        }
        return lista;
    }

    private List<Integer> obtenerIdsEstudiantesDeCurso(Connection con, int idCurso) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT estudiante_id FROM curso_estudiantes WHERE curso_id=?")) {
            ps.setInt(1, idCurso);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        }
        return ids;
    }

    private Asistencia mapearFila(ResultSet rs) throws SQLException {
        Asistencia a = new Asistencia();
        a.setId(rs.getInt("id"));
        a.setEstudianteId(rs.getInt("estudiante_id"));
        a.setCursoId(rs.getInt("curso_id"));
        a.setFecha(rs.getDate("fecha").toLocalDate());
        a.setPresente(rs.getBoolean("presente"));
        Timestamp ts = rs.getTimestamp("hora_registro");
        if (ts != null) a.setHoraRegistro(ts.toLocalDateTime());
        try {
            a.setEstudianteNombre(rs.getString("nombre_estudiante"));
            a.setEstudianteCorreo(rs.getString("correo_estudiante"));
            a.setEstudianteFotoRuta(rs.getString("foto_estudiante"));
        } catch (Exception ignored) {}
        return a;
    }
}
