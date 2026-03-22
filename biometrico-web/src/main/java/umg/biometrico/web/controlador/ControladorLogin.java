package umg.biometrico.web.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para la pantalla de inicio de sesion.
 */
@Controller
public class ControladorLogin {

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "salida", required = false) String salida,
            @RequestParam(value = "sesionExpirada", required = false) String sesionExpirada,
            Model modelo) {

        if (error != null) {
            modelo.addAttribute("mensajeError", "Carnet o contraseña incorrectos. Verifique sus datos.");
        }
        if (salida != null) {
            modelo.addAttribute("mensajeSalida", "Ha cerrado sesion correctamente.");
        }
        if (sesionExpirada != null) {
            modelo.addAttribute("mensajeError", "Su sesion ha expirado. Por favor ingrese nuevamente.");
        }
        return "login";
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/menu";
    }
}
