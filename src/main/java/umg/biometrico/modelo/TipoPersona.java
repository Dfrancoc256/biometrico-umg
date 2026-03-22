package umg.biometrico.modelo;

/**
 * TipoPersona - Enumeracion de los tipos de persona del sistema.
 */
public enum TipoPersona {
    ESTUDIANTE("Estudiante"),
    CATEDRATICO("Catedratico"),
    MANTENIMIENTO("Mantenimiento"),
    ADMINISTRATIVO("Administrativo"),
    VISITANTE("Visitante");

    private final String etiqueta;

    TipoPersona(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    public static TipoPersona desdeCadena(String valor) {
        if (valor == null) return ESTUDIANTE;
        for (TipoPersona tipo : values()) {
            if (tipo.name().equalsIgnoreCase(valor) || tipo.etiqueta.equalsIgnoreCase(valor))
                return tipo;
        }
        return ESTUDIANTE;
    }
}
