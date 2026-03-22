package umg.biometrico.modelo;

/**
 * Instalacion - Representa un edificio o sede de la UMG.
 */
public class Instalacion {

    private int    id;
    private String nombre;
    private String direccion;

    public Instalacion() {}

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getNombre()                { return nombre; }
    public void setNombre(String nombre)     { this.nombre = nombre; }

    public String getDireccion()             { return direccion; }
    public void setDireccion(String dir)     { this.direccion = dir; }

    @Override
    public String toString() { return nombre; }
}
