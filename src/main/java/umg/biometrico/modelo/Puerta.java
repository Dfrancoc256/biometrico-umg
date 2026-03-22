package umg.biometrico.modelo;

/**
 * Puerta - Representa una puerta de ingreso o salon de clases en una instalacion.
 */
public class Puerta {

    private int     id;
    private int     instalacionId;
    private String  nombre;
    private String  nivel;
    private boolean esSalon;
    private String  descripcion;

    public Puerta() {}

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public int getInstalacionId()                        { return instalacionId; }
    public void setInstalacionId(int instalacionId)      { this.instalacionId = instalacionId; }

    public String getNombre()                { return nombre; }
    public void setNombre(String nombre)     { this.nombre = nombre; }

    public String getNivel()                 { return nivel; }
    public void setNivel(String nivel)       { this.nivel = nivel; }

    public boolean isEsSalon()               { return esSalon; }
    public void setEsSalon(boolean esSalon)  { this.esSalon = esSalon; }

    public String getDescripcion()               { return descripcion; }
    public void setDescripcion(String desc)      { this.descripcion = desc; }

    @Override
    public String toString() { return nombre; }
}
