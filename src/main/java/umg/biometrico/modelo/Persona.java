package umg.biometrico.modelo;

/**
 * Persona - Modelo que representa a un estudiante, catedratico u otro miembro de la UMG.
 */
public class Persona {

    private int    id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String fotoRuta;
    private String encodingFacial;
    private TipoPersona tipoPersona;
    private String carrera;
    private String seccion;
    private String numeroCarnet;
    private String contrasena;
    private boolean activo;
    private boolean restringido;
    private String motivoRestriccion;

    public Persona() {
        this.tipoPersona = TipoPersona.ESTUDIANTE;
        this.activo      = true;
        this.restringido = false;
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    // --- Getters y Setters ---

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getNombre()                { return nombre; }
    public void setNombre(String nombre)     { this.nombre = nombre; }

    public String getApellido()              { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono()              { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo()               { return correo; }
    public void setCorreo(String correo)    { this.correo = correo; }

    public String getFotoRuta()                  { return fotoRuta; }
    public void setFotoRuta(String fotoRuta)     { this.fotoRuta = fotoRuta; }

    public String getEncodingFacial()                { return encodingFacial; }
    public void setEncodingFacial(String enc)        { this.encodingFacial = enc; }

    public TipoPersona getTipoPersona()                  { return tipoPersona; }
    public void setTipoPersona(TipoPersona tipoPersona)  { this.tipoPersona = tipoPersona; }

    public String getCarrera()               { return carrera; }
    public void setCarrera(String carrera)   { this.carrera = carrera; }

    public String getSeccion()               { return seccion; }
    public void setSeccion(String seccion)   { this.seccion = seccion; }

    public String getNumeroCarnet()                  { return numeroCarnet; }
    public void setNumeroCarnet(String numeroCarnet) { this.numeroCarnet = numeroCarnet; }

    public String getContrasena()                { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public boolean isActivo()                { return activo; }
    public void setActivo(boolean activo)    { this.activo = activo; }

    public boolean isRestringido()               { return restringido; }
    public void setRestringido(boolean restringido) { this.restringido = restringido; }

    public String getMotivoRestriccion()                       { return motivoRestriccion; }
    public void setMotivoRestriccion(String motivoRestriccion) { this.motivoRestriccion = motivoRestriccion; }

    @Override
    public String toString() {
        return getNombreCompleto() + " [" + numeroCarnet + "]";
    }
}
