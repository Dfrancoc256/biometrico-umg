package umg.biometrico.web.modelo;

/**
 * Tipos de personas registradas en el sistema UMG.
 */
public enum TipoPersona {
    ESTUDIANTE("Estudiante"),
    CATEDRATICO("Catedrático"),
    ADMINISTRATIVO("Administrativo");

    private final String etiqueta;

    TipoPersona(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
