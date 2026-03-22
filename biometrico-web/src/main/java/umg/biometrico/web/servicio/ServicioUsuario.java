package umg.biometrico.web.servicio;

import umg.biometrico.web.dao.PersonaRepositorio;
import umg.biometrico.web.modelo.Persona;
import umg.biometrico.web.modelo.UsuarioDetalles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio para la autenticacion con Spring Security.
 * Carga el usuario desde la BD por su numero de carnet.
 */
@Service
public class ServicioUsuario implements UserDetailsService {

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Override
    public UserDetails loadUserByUsername(String carnet) throws UsernameNotFoundException {
        Persona persona = personaRepositorio.findByCarnet(carnet)
                .orElseThrow(() -> new UsernameNotFoundException("No existe el carnet: " + carnet));

        if (!persona.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + carnet);
        }

        return new UsuarioDetalles(persona);
    }
}
