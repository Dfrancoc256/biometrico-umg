package umg.biometrico.web.controlador;

import umg.biometrico.web.dao.CursoRepositorio;
import umg.biometrico.web.modelo.*;
import umg.biometrico.web.servicio.ServicioAsistencia;
import umg.biometrico.web.servicio.ServicioPersona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para el dashboard del catedratico.
 * Muestra el arbol visual de estudiantes y gestiona asistencia.
 */
@Controller
@RequestMapping("/catedratico")
public class ControladorCatedratico {

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private ServicioAsistencia servicioAsistencia;

    @Autowired
    private ServicioPersona servicioPersona;

    @GetMapping
    public String mostrarDashboard(@AuthenticationPrincipal UsuarioDetalles usuario, Model modelo) {
        Persona catedratico = usuario.getPersona();
        List<Curso> cursos = cursoRepositorio.findByCatedratico(catedratico.getId());
        modelo.addAttribute("cursos", cursos);
        modelo.addAttribute("catedratico", catedratico);
        return "modulos/catedratico-dashboard";
    }

    @GetMapping("/curso/{id}")
    public String verCurso(@PathVariable Long id,
                           @RequestParam(value = "fecha", required = false) String fechaStr,
                           Model modelo) {

        Curso curso = cursoRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        LocalDate fecha = (fechaStr != null) ? LocalDate.parse(fechaStr) : LocalDate.now();

        List<Asistencia> asistencias = servicioAsistencia.obtenerAsistenciaCursoFecha(id, fecha);
        modelo.addAttribute("curso", curso);
        modelo.addAttribute("fecha", fecha);
        modelo.addAttribute("asistencias", asistencias);
        modelo.addAttribute("estudiantes", curso.getEstudiantes());
        return "modulos/asistencia-clase";
    }

    @PostMapping("/asistencia/guardar")
    public String guardarAsistencia(
            @RequestParam Long cursoId,
            @RequestParam(value = "estudiantesPresentes", required = false) List<Long> presentes,
            @AuthenticationPrincipal UsuarioDetalles usuario,
            RedirectAttributes atributos) {

        if (presentes == null) presentes = List.of();
        servicioAsistencia.registrarAsistencia(cursoId, presentes, usuario.getPersona().getCarnet());
        atributos.addFlashAttribute("mensajeExito", "Asistencia guardada correctamente.");
        return "redirect:/catedratico/curso/" + cursoId;
    }
}
