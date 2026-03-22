package umg.biometrico.modelo;

import java.time.LocalDateTime;

/**
 * RegistroIngreso - Registro de un ingreso de una persona por una puerta.
 */
public class RegistroIngreso {

    private int           id;
    private int           personaId;
    private String        personaNombre;
    private String        personaCorreo;
    private String        personaFotoRuta;
    private int           puertaId;
    private String        puertaNombre;
    private LocalDateTime fechaHora;
    private String        metodo; // FACIAL, MANUAL

    public RegistroIngreso() {
        this.metodo   = "FACIAL";
        this.fechaHora = LocalDateTime.now();
    }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public int getPersonaId()                    { return personaId; }
    public void setPersonaId(int personaId)      { this.personaId = personaId; }

    public String getPersonaNombre()                     { return personaNombre; }
    public void setPersonaNombre(String personaNombre)   { this.personaNombre = personaNombre; }

    public String getPersonaCorreo()                     { return personaCorreo; }
    public void setPersonaCorreo(String personaCorreo)   { this.personaCorreo = personaCorreo; }

    public String getPersonaFotoRuta()                       { return personaFotoRuta; }
    public void setPersonaFotoRuta(String personaFotoRuta)   { this.personaFotoRuta = personaFotoRuta; }

    public int getPuertaId()                 { return puertaId; }
    public void setPuertaId(int puertaId)    { this.puertaId = puertaId; }

    public String getPuertaNombre()                  { return puertaNombre; }
    public void setPuertaNombre(String puertaNombre) { this.puertaNombre = puertaNombre; }

    public LocalDateTime getFechaHora()                  { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora)    { this.fechaHora = fechaHora; }

    public String getMetodo()                { return metodo; }
    public void setMetodo(String metodo)     { this.metodo = metodo; }
}
