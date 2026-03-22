package umg.biometrico.web.servicio;

import umg.biometrico.web.dao.AsistenciaRepositorio;
import umg.biometrico.web.dao.CursoRepositorio;
import umg.biometrico.web.modelo.Asistencia;
import umg.biometrico.web.modelo.Curso;
import umg.biometrico.web.modelo.Persona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para el control de asistencia por curso.
 */
@Service
public class ServicioAsistencia {

    @Autowired
    private AsistenciaRepositorio asistenciaRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Transactional
    public void registrarAsistencia(Long cursoId, List<Long> estudiantesPresentes, String registradoPor) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        LocalDate hoy = LocalDate.now();
        Set<Long> presentesSet = new HashSet<>(estudiantesPresentes);

        for (Persona estudiante : curso.getEstudiantes()) {
            Optional<Asistencia> existente = asistenciaRepositorio
                    .findByEstudianteCursoYFecha(estudiante.getId(), cursoId, hoy);

            Asistencia asistencia = existente.orElseGet(() -> {
                Asistencia nueva = new Asistencia();
                nueva.setEstudiante(estudiante);
                nueva.setCurso(curso);
                nueva.setFechaClase(hoy);
                return nueva;
            });

            asistencia.setPresente(presentesSet.contains(estudiante.getId()));
            asistencia.setHoraRegistro(LocalDateTime.now());
            asistencia.setRegistradoPor(registradoPor);
            asistenciaRepositorio.save(asistencia);
        }
    }

    public List<Asistencia> obtenerAsistenciaCursoFecha(Long cursoId, LocalDate fecha) {
        return asistenciaRepositorio.findByCursoYFecha(cursoId, fecha);
    }

    public Map<Persona, List<Asistencia>> obtenerResumenCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        Map<Persona, List<Asistencia>> resumen = new LinkedHashMap<>();
        for (Persona estudiante : curso.getEstudiantes()) {
            List<Asistencia> asistencias = asistenciaRepositorio
                    .findByEstudianteYCurso(estudiante.getId(), cursoId);
            resumen.put(estudiante, asistencias);
        }
        return resumen;
    }
}
