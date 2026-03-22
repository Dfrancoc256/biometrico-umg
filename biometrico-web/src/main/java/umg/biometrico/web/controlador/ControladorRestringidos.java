package umg.biometrico.web.controlador;

import umg.biometrico.web.servicio.ServicioPersona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para el modulo de personas restringidas.
 */
@Controller
@RequestMapping("/admin/restringidos")
@PreAuthorize("hasRole('ADMIN')")
public class ControladorRestringidos {

    @Autowired
    private ServicioPersona servicioPersona;

    @GetMapping
    public String listarRestringidos(Model modelo) {
        modelo.addAttribute("personas", servicioPersona.listarRestringidos());
        return "modulos/restringidos";
    }

    @PostMapping("/restringir/{id}")
    public String restringir(@PathVariable Long id,
                             @RequestParam("motivo") String motivo,
                             RedirectAttributes atributos) {
        servicioPersona.restringir(id, motivo);
        atributos.addFlashAttribute("mensajeExito", "Persona restringida correctamente.");
        return "redirect:/admin/restringidos";
    }

    @PostMapping("/remover/{id}")
    public String removerRestriccion(@PathVariable Long id, RedirectAttributes atributos) {
        servicioPersona.removerRestriccion(id);
        atributos.addFlashAttribute("mensajeExito", "Restriccion removida correctamente.");
        return "redirect:/admin/restringidos";
    }
}
