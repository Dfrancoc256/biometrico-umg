package umg.biometrico.web.controlador;

import umg.biometrico.web.dao.PersonaRepositorio;
import umg.biometrico.web.modelo.Persona;
import umg.biometrico.web.servicio.ServicioPersona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para peticiones AJAX del frontend.
 * Devuelve JSON para la busqueda de personas y carga de fotos.
 */
@RestController
@RequestMapping("/api")
public class ControladorApiREST {

    @Autowired
    private ServicioPersona servicioPersona;

    @Autowired
    private PersonaRepositorio personaRepositorio;

    /** Buscar personas por nombre o carnet (para autocomplete) */
    @GetMapping("/personas/buscar")
    public List<Map<String, Object>> buscarPersonas(@RequestParam("q") String termino) {
        return servicioPersona.buscar(termino).stream()
                .limit(10)
                .map(p -> Map.of(
                        "id", p.getId(),
                        "carnet", p.getCarnet(),
                        "nombreCompleto", p.getNombreCompleto(),
                        "tipoPersona", p.getTipoPersona().getEtiqueta(),
                        "correo", p.getCorreo() != null ? p.getCorreo() : ""
                ))
                .collect(Collectors.toList());
    }

    /** Servir la foto de una persona por su ID */
    @GetMapping(value = "/foto/{id}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) {
        return personaRepositorio.findById(id)
                .filter(p -> p.getFotoPath() != null)
                .map(p -> {
                    try {
                        Path ruta = Paths.get(p.getFotoPath());
                        if (Files.exists(ruta)) {
                            byte[] bytes = Files.readAllBytes(ruta);
                            String contentType = ruta.toString().endsWith(".png")
                                    ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
                            return ResponseEntity.ok()
                                    .contentType(MediaType.parseMediaType(contentType))
                                    .body(bytes);
                        }
                    } catch (IOException ignored) {}
                    return ResponseEntity.notFound().<byte[]>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Estado del sistema */
    @GetMapping("/estado")
    public Map<String, Object> estado() {
        long totalPersonas = personaRepositorio.count();
        return Map.of(
                "estado", "activo",
                "totalPersonas", totalPersonas,
                "sistema", "UMG Sistema Biometrico Web"
        );
    }
}
