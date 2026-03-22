package umg.biometrico.web.servicio;

import umg.biometrico.web.dao.PersonaRepositorio;
import umg.biometrico.web.modelo.Persona;
import umg.biometrico.web.modelo.TipoPersona;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de negocio para el registro y gestion de personas.
 */
@Service
public class ServicioPersona {

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Value("${almacenamiento.fotos:fotos_personas}")
    private String rutaFotos;

    public Persona registrar(Persona persona, String contrasenaPlana) throws Exception {
        if (personaRepositorio.existsByCarnet(persona.getCarnet())) {
            throw new IllegalArgumentException("El carnet ya existe: " + persona.getCarnet());
        }
        if (persona.getCorreo() != null && personaRepositorio.existsByCorreo(persona.getCorreo())) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }

        String hash = BCrypt.hashpw(contrasenaPlana, BCrypt.gensalt(12));
        persona.setContrasenaHash(hash);
        persona.setFechaRegistro(LocalDateTime.now());
        return personaRepositorio.save(persona);
    }

    public String guardarFoto(MultipartFile foto, String carnet) throws IOException {
        Path directorio = Paths.get(rutaFotos);
        Files.createDirectories(directorio);

        String extension = obtenerExtension(foto.getOriginalFilename());
        String nombreArchivo = carnet + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path destino = directorio.resolve(nombreArchivo);
        Files.copy(foto.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return destino.toString();
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null) return ".jpg";
        int puntoIdx = nombreArchivo.lastIndexOf('.');
        return puntoIdx >= 0 ? nombreArchivo.substring(puntoIdx) : ".jpg";
    }

    public Optional<Persona> buscarPorCarnet(String carnet) {
        return personaRepositorio.findByCarnet(carnet);
    }

    public Optional<Persona> buscarPorId(Long id) {
        return personaRepositorio.findById(id);
    }

    public List<Persona> listarTodos() {
        return personaRepositorio.findByActivoTrue();
    }

    public List<Persona> listarPorTipo(TipoPersona tipo) {
        return personaRepositorio.findByTipoPersonaAndActivoTrue(tipo);
    }

    public List<Persona> listarRestringidos() {
        return personaRepositorio.findByRestringidoTrue();
    }

    public List<Persona> buscar(String termino) {
        return personaRepositorio.buscarPorNombreOCarnet(termino);
    }

    public Persona guardar(Persona persona) {
        return personaRepositorio.save(persona);
    }

    public void restringir(Long id, String motivo) {
        personaRepositorio.findById(id).ifPresent(p -> {
            p.setRestringido(true);
            p.setMotivoRestriccion(motivo);
            personaRepositorio.save(p);
        });
    }

    public void removerRestriccion(Long id) {
        personaRepositorio.findById(id).ifPresent(p -> {
            p.setRestringido(false);
            p.setMotivoRestriccion(null);
            personaRepositorio.save(p);
        });
    }

    public List<Persona> listarConEncodingFacial() {
        return personaRepositorio.findConEncodingFacialActivo();
    }
}
