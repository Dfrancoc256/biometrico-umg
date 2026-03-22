package umg.biometrico.web.dao;

import umg.biometrico.web.modelo.Persona;
import umg.biometrico.web.modelo.TipoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Persona.
 */
@Repository
public interface PersonaRepositorio extends JpaRepository<Persona, Long> {

    Optional<Persona> findByCarnet(String carnet);

    Optional<Persona> findByCorreo(String correo);

    List<Persona> findByTipoPersona(TipoPersona tipo);

    List<Persona> findByActivoTrue();

    List<Persona> findByRestringidoTrue();

    List<Persona> findByTipoPersonaAndActivoTrue(TipoPersona tipo);

    @Query("SELECT p FROM Persona p WHERE " +
           "LOWER(p.nombres) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.carnet) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Persona> buscarPorNombreOCarnet(@Param("busqueda") String busqueda);

    @Query("SELECT p FROM Persona p WHERE p.encodingFacial IS NOT NULL AND p.activo = true AND p.restringido = false")
    List<Persona> findConEncodingFacialActivo();

    boolean existsByCarnet(String carnet);

    boolean existsByCorreo(String correo);
}
