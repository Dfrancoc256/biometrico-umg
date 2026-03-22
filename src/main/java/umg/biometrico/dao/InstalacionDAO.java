package umg.biometrico.dao;

import umg.biometrico.configuracion.ConfiguracionBD;
import umg.biometrico.modelo.Instalacion;
import umg.biometrico.modelo.Puerta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InstalacionDAO - Operaciones de base de datos para Instalacion y Puerta.
 */
public class InstalacionDAO {

    public List<Instalacion> listarInstalaciones() throws SQLException {
        List<Instalacion> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             ResultSet rs = con.createStatement().executeQuery(
                     "SELECT * FROM instalaciones ORDER BY nombre")) {
            while (rs.next()) {
                Instalacion i = new Instalacion();
                i.setId(rs.getInt("id"));
                i.setNombre(rs.getString("nombre"));
                i.setDireccion(rs.getString("direccion"));
                lista.add(i);
            }
        }
        return lista;
    }

    public List<Puerta> listarPuertas(int idInstalacion) throws SQLException {
        return listarPuertasFiltradas(idInstalacion, false);
    }

    public List<Puerta> listarSalones(int idInstalacion) throws SQLException {
        return listarPuertasFiltradas(idInstalacion, true);
    }

    public List<Puerta> listarTodasPuertas(int idInstalacion) throws SQLException {
        String sql = "SELECT * FROM puertas WHERE instalacion_id=? ORDER BY nombre";
        List<Puerta> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstalacion);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearPuerta(rs));
        }
        return lista;
    }

    private List<Puerta> listarPuertasFiltradas(int idInstalacion, boolean soloSalones) throws SQLException {
        String sql = "SELECT * FROM puertas WHERE instalacion_id=? AND es_salon=? ORDER BY nombre";
        List<Puerta> lista = new ArrayList<>();
        try (Connection con = ConfiguracionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstalacion);
            ps.setBoolean(2, soloSalones);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearPuerta(rs));
        }
        return lista;
    }

    private Puerta mapearPuerta(ResultSet rs) throws SQLException {
        Puerta p = new Puerta();
        p.setId(rs.getInt("id"));
        p.setInstalacionId(rs.getInt("instalacion_id"));
        p.setNombre(rs.getString("nombre"));
        p.setNivel(rs.getString("nivel"));
        p.setEsSalon(rs.getBoolean("es_salon"));
        p.setDescripcion(rs.getString("descripcion"));
        return p;
    }
}
