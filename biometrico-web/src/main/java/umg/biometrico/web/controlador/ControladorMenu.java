package umg.biometrico.web.controlador;

import umg.biometrico.web.modelo.UsuarioDetalles;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para el menu principal del sistema.
 */
@Controller
public class ControladorMenu {

    @GetMapping("/menu")
    public String mostrarMenu(@AuthenticationPrincipal UsuarioDetalles usuario, Model modelo) {
        modelo.addAttribute("persona", usuario.getPersona());
        modelo.addAttribute("nombreCompleto", usuario.getPersona().getNombreCompleto());
        modelo.addAttribute("tipo", usuario.getPersona().getTipoPersona());
        return "menu";
    }
}
