package umg.biometrico.web.controlador;

import umg.biometrico.web.modelo.Persona;
import umg.biometrico.web.modelo.TipoPersona;
import umg.biometrico.web.servicio.ServicioPersona;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para el modulo de registro biografico y biometrico.
 */
@Controller
@RequestMapping("/registro")
public class ControladorRegistro {

    @Autowired
    private ServicioPersona servicioPersona;

    @GetMapping
    public String mostrarFormulario(Model modelo) {
        modelo.addAttribute("persona", new Persona());
        modelo.addAttribute("tiposPersona", TipoPersona.values());
        return "modulos/registro";
    }

    @PostMapping
    public String procesarRegistro(
            @Valid @ModelAttribute Persona persona,
            BindingResult resultado,
            @RequestParam("contrasena") String contrasena,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "encodingFacialBase64", required = false) String encodingFacial,
            RedirectAttributes atributos,
            Model modelo) {

        if (resultado.hasErrors()) {
            modelo.addAttribute("tiposPersona", TipoPersona.values());
            return "modulos/registro";
        }

        try {
            if (foto != null && !foto.isEmpty()) {
                String rutaFoto = servicioPersona.guardarFoto(foto, persona.getCarnet());
                persona.setFotoPath(rutaFoto);
            }

            if (encodingFacial != null && !encodingFacial.isBlank()) {
                persona.setEncodingFacial(encodingFacial);
            }

            Persona guardada = servicioPersona.registrar(persona, contrasena);
            atributos.addFlashAttribute("mensajeExito",
                    "Persona registrada exitosamente. Carnet: " + guardada.getCarnet());
            return "redirect:/registro/" + guardada.getId();

        } catch (IllegalArgumentException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("tiposPersona", TipoPersona.values());
            return "modulos/registro";
        } catch (Exception e) {
            modelo.addAttribute("mensajeError", "Error al registrar: " + e.getMessage());
            modelo.addAttribute("tiposPersona", TipoPersona.values());
            return "modulos/registro";
        }
    }

    @GetMapping("/{id}")
    public String verPersona(@PathVariable Long id, Model modelo) {
        return servicioPersona.buscarPorId(id).map(persona -> {
            modelo.addAttribute("persona", persona);
            return "modulos/detalle-persona";
        }).orElse("redirect:/personas");
    }

    @GetMapping("/lista")
    public String listarPersonas(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "tipo", required = false) TipoPersona tipo,
            Model modelo) {

        if (busqueda != null && !busqueda.isBlank()) {
            modelo.addAttribute("personas", servicioPersona.buscar(busqueda));
            modelo.addAttribute("busqueda", busqueda);
        } else if (tipo != null) {
            modelo.addAttribute("personas", servicioPersona.listarPorTipo(tipo));
        } else {
            modelo.addAttribute("personas", servicioPersona.listarTodos());
        }
        modelo.addAttribute("tiposPersona", TipoPersona.values());
        return "modulos/lista-personas";
    }
}
