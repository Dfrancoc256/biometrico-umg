package umg.biometrico.modelo;

/**
 * Curso - Modelo que representa un curso o materia de la UMG.
 */
public class Curso {

    private int    id;
    private String codigo;
    private String nombre;
    private int    catedraticoId;
    private String catedraticoNombre;
    private String salon;
    private String horario;
    private boolean activo;

    public Curso() {
        this.activo = true;
    }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getCodigo()                { return codigo; }
    public void setCodigo(String codigo)     { this.codigo = codigo; }

    public String getNombre()                { return nombre; }
    public void setNombre(String nombre)     { this.nombre = nombre; }

    public int getCatedraticoId()                    { return catedraticoId; }
    public void setCatedraticoId(int catedraticoId)  { this.catedraticoId = catedraticoId; }

    public String getCatedraticoNombre()                     { return catedraticoNombre; }
    public void setCatedraticoNombre(String catedraticoNombre) { this.catedraticoNombre = catedraticoNombre; }

    public String getSalon()                 { return salon; }
    public void setSalon(String salon)       { this.salon = salon; }

    public String getHorario()               { return horario; }
    public void setHorario(String horario)   { this.horario = horario; }

    public boolean isActivo()                { return activo; }
    public void setActivo(boolean activo)    { this.activo = activo; }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nombre;
    }
}
