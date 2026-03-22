package umg.biometrico.web.dao;

import umg.biometrico.web.modelo.RegistroIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para registros de ingreso.
 */
@Repository
public interface IngresoRepositorio extends JpaRepository<RegistroIngreso, Long> {

    @Query("SELECT r FROM RegistroIngreso r WHERE DATE(r.fechaHora) = :fecha ORDER BY r.fechaHora DESC")
    List<RegistroIngreso> findByFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT r FROM RegistroIngreso r WHERE r.puerta.id = :puertaId ORDER BY r.fechaHora DESC")
    List<RegistroIngreso> findByPuerta(@Param("puertaId") Long puertaId);

    @Query("SELECT r FROM RegistroIngreso r WHERE r.persona.id = :personaId ORDER BY r.fechaHora DESC")
    List<RegistroIngreso> findByPersona(@Param("personaId") Long personaId);
}
