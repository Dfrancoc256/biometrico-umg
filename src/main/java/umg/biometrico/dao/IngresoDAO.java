package umg.biometrico.dao;

import umg.biometrico.configuracion.ConfiguracionBD;
import umg.biometrico.modelo.RegistroIngreso;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * IngresoDAO - Operaciones de base de datos para RegistroIngreso.
 */
public class IngresoDAO {

    public void insertar(RegistroIngreso registro) throws SQLException {
        String sql = """
            INSERT INTO registro_ingreso (persona_id,puerta_id,fecha_hora,metodo)
            VALUES (?,?,CURRENT_TIMESTAMP,?)
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, registro.getPersonaId());
            ps.setInt(2, registro.getPuertaId());
            ps.setString(3, registro.getMetodo());
            ps.executeUpdate();
            ResultSet llaves = ps.getGeneratedKeys();
            if (llaves.next()) registro.setId(llaves.getInt(1));
        }
    }

    public List<RegistroIngreso> listarPorPuertaYFecha(int idPuerta, LocalDate fecha) throws SQLException {
        String sql = """
            SELECT ri.*, p.nombre||' '||p.apellido AS persona_nombre,
                   p.correo AS persona_correo, p.foto_ruta AS persona_foto
            FROM registro_ingreso ri
            INNER JOIN personas p ON ri.persona_id = p.id
            WHERE ri.puerta_id=? AND DATE(ri.fecha_hora)=?
            ORDER BY ri.fecha_hora
        """;
        return ejecutarListado(sql, idPuerta, Date.valueOf(fecha));
    }

    public List<RegistroIngreso> listarPorPuerta(int idPuerta) throws SQLException {
        String sql = """
            SELECT ri.*, p.nombre||' '||p.apellido AS persona_nombre,
                   p.correo AS persona_correo, p.foto_ruta AS persona_foto
            FROM registro_ingreso ri
            INNER JOIN personas p ON ri.persona_id = p.id
            WHERE ri.puerta_id=? ORDER BY ri.fecha_hora DESC
        """;
        return ejecutarListado(sql, idPuerta, null);
    }

    public boolean tieneIngresoHoyPorPuerta(int idPersona, int idPuerta) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM registro_ingreso
            WHERE persona_id=? AND puerta_id=? AND DATE(fecha_hora)=CURRENT_DATE
        """;
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPersona); ps.setInt(2, idPuerta);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Obtiene fechas distintas que tienen al menos un registro en una puerta.
     * Usado para construir el arbol historico de ingresos.
     */
    public List<LocalDate> listarFechasConRegistro(int idPuerta) throws SQLException {
        String sql = """
            SELECT DISTINCT DATE(fecha_hora) AS fecha
            FROM registro_ingreso WHERE puerta_id=?
            ORDER BY fecha DESC
        """;
        List<LocalDate> fechas = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPuerta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) fechas.add(rs.getDate("fecha").toLocalDate());
        }
        return fechas;
    }

    private List<RegistroIngreso> ejecutarListado(String sql, Integer param1, Object param2) throws SQLException {
        List<RegistroIngreso> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param1 != null) ps.setInt(1, param1);
            if (param2 != null) ps.setObject(2, param2);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    private RegistroIngreso mapearFila(ResultSet rs) throws SQLException {
        RegistroIngreso r = new RegistroIngreso();
        r.setId(rs.getInt("id"));
        r.setPersonaId(rs.getInt("persona_id"));
        r.setPuertaId(rs.getInt("puerta_id"));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        if (ts != null) r.setFechaHora(ts.toLocalDateTime());
        r.setMetodo(rs.getString("metodo"));
        try {
            r.setPersonaNombre(rs.getString("persona_nombre"));
            r.setPersonaCorreo(rs.getString("persona_correo"));
            r.setPersonaFotoRuta(rs.getString("persona_foto"));
        } catch (Exception ignored) {}
        return r;
    }
}
