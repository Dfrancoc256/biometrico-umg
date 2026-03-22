package umg.biometrico.web.dao;

import umg.biometrico.web.modelo.Puerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para puertas y salones.
 */
@Repository
public interface PuertaRepositorio extends JpaRepository<Puerta, Long> {

    List<Puerta> findByActivaTrue();

    List<Puerta> findByTipoAndActivaTrue(String tipo);
}
