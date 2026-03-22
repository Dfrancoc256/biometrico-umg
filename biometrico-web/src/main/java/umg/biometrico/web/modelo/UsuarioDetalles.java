package umg.biometrico.web.modelo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * Implementacion de UserDetails para Spring Security.
 * Envuelve la entidad Persona para la autenticacion.
 */
public class UsuarioDetalles implements UserDetails {

    private final Persona persona;

    public UsuarioDetalles(Persona persona) {
        this.persona = persona;
    }

    public Persona getPersona() {
        return persona;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = switch (persona.getTipoPersona()) {
            case ADMINISTRATIVO -> "ROLE_ADMIN";
            case CATEDRATICO -> "ROLE_CATEDRATICO";
            case ESTUDIANTE -> "ROLE_ESTUDIANTE";
        };
        return List.of(new SimpleGrantedAuthority(rol));
    }

    @Override
    public String getPassword() {
        return persona.getContrasenaHash();
    }

    @Override
    public String getUsername() {
        return persona.getCarnet();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !persona.isRestringido(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return persona.isActivo(); }
}
