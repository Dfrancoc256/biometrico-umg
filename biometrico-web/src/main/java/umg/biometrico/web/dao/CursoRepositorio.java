package umg.biometrico.web.dao;

import umg.biometrico.web.modelo.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para cursos.
 */
@Repository
public interface CursoRepositorio extends JpaRepository<Curso, Long> {

    List<Curso> findByActivoTrue();

    @Query("SELECT c FROM Curso c WHERE c.catedratico.id = :catedraticoId AND c.activo = true")
    List<Curso> findByCatedratico(@Param("catedraticoId") Long catedraticoId);

    @Query("SELECT c FROM Curso c JOIN c.estudiantes e WHERE e.id = :estudianteId")
    List<Curso> findByEstudiante(@Param("estudianteId") Long estudianteId);
}
