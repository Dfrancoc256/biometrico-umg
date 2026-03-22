package umg.biometrico.web.dao;

import umg.biometrico.web.modelo.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para registros de asistencia.
 */
@Repository
public interface AsistenciaRepositorio extends JpaRepository<Asistencia, Long> {

    @Query("SELECT a FROM Asistencia a WHERE a.curso.id = :cursoId AND a.fechaClase = :fecha")
    List<Asistencia> findByCursoYFecha(@Param("cursoId") Long cursoId, @Param("fecha") LocalDate fecha);

    @Query("SELECT a FROM Asistencia a WHERE a.estudiante.id = :estudianteId AND a.curso.id = :cursoId ORDER BY a.fechaClase DESC")
    List<Asistencia> findByEstudianteYCurso(@Param("estudianteId") Long estudianteId, @Param("cursoId") Long cursoId);

    @Query("SELECT a FROM Asistencia a WHERE a.estudiante.id = :estudianteId AND a.curso.id = :cursoId AND a.fechaClase = :fecha")
    Optional<Asistencia> findByEstudianteCursoYFecha(
            @Param("estudianteId") Long estudianteId,
            @Param("cursoId") Long cursoId,
            @Param("fecha") LocalDate fecha);
}
