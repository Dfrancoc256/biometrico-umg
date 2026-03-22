package umg.biometrico.web.controlador;

import umg.biometrico.web.dao.IngresoRepositorio;
import umg.biometrico.web.dao.PuertaRepositorio;
import umg.biometrico.web.dao.CursoRepositorio;
import umg.biometrico.web.servicio.ServicioAsistencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controlador para el modulo de reportes y estadisticas.
 * Genera 4 vistas de arbol: puertas historico, puertas por fecha,
 * salones historico, salones por fecha.
 */
@Controller
@RequestMapping("/reportes")
public class ControladorReportes {

    @Autowired
    private IngresoRepositorio ingresoRepositorio;

    @Autowired
    private PuertaRepositorio puertaRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private ServicioAsistencia servicioAsistencia;

    @GetMapping
    public String mostrarReportes(Model modelo) {
        modelo.addAttribute("puertas", puertaRepositorio.findByTipoAndActivaTrue("PUERTA"));
        modelo.addAttribute("salones", puertaRepositorio.findByTipoAndActivaTrue("SALON"));
        modelo.addAttribute("cursos", cursoRepositorio.findByActivoTrue());
        modelo.addAttribute("hoy", LocalDate.now());
        return "modulos/reportes";
    }

    @GetMapping("/ingresos/puerta/{id}")
    public String reporteIngresoPuerta(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("ingresos", ingresoRepositorio.findByPuerta(id));
        modelo.addAttribute("puerta", puertaRepositorio.findById(id).orElse(null));
        return "modulos/reporte-puerta";
    }

    @GetMapping("/ingresos/fecha")
    public String reporteIngresoFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model modelo) {
        modelo.addAttribute("ingresos", ingresoRepositorio.findByFecha(fecha));
        modelo.addAttribute("fecha", fecha);
        return "modulos/reporte-fecha";
    }

    @GetMapping("/asistencia/curso/{id}")
    public String reporteAsistenciaCurso(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("resumen", servicioAsistencia.obtenerResumenCurso(id));
        modelo.addAttribute("curso", cursoRepositorio.findById(id).orElse(null));
        return "modulos/reporte-asistencia";
    }
}
