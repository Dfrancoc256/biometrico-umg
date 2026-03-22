package umg.biometrico.util;

import umg.biometrico.modelo.Persona;
import umg.biometrico.modelo.TipoPersona;

/**
 * SesionActual - Singleton que mantiene la informacion del usuario autenticado.
 */
public class SesionActual {

    private static SesionActual instancia;
    private Persona usuarioActual;

    private SesionActual() {}

    public static SesionActual obtenerInstancia() {
        if (instancia == null) instancia = new SesionActual();
        return instancia;
    }

    public void iniciarSesion(Persona persona) {
        this.usuarioActual = persona;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public Persona getUsuarioActual() {
        return usuarioActual;
    }

    public boolean estaAutenticado() {
        return usuarioActual != null;
    }

    public boolean esCatedratico() {
        return usuarioActual != null
                && usuarioActual.getTipoPersona() == TipoPersona.CATEDRATICO;
    }

    public boolean esAdministrador() {
        return usuarioActual != null
                && usuarioActual.getTipoPersona() == TipoPersona.ADMINISTRATIVO;
    }

    public boolean puedeAdministrar() {
        return esCatedratico() || esAdministrador();
    }
}
