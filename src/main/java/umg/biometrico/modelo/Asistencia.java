package umg.biometrico.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Asistencia - Registro de asistencia de un estudiante a un curso en una fecha.
 */
public class Asistencia {

    private int           id;
    private int           estudianteId;
    private String        estudianteNombre;
    private String        estudianteCorreo;
    private String        estudianteFotoRuta;
    private int           cursoId;
    private LocalDate     fecha;
    private boolean       presente;
    private LocalDateTime horaRegistro;

    public Asistencia() {}

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public int getEstudianteId()                     { return estudianteId; }
    public void setEstudianteId(int estudianteId)    { this.estudianteId = estudianteId; }

    public String getEstudianteNombre()                      { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public String getEstudianteCorreo()                      { return estudianteCorreo; }
    public void setEstudianteCorreo(String estudianteCorreo) { this.estudianteCorreo = estudianteCorreo; }

    public String getEstudianteFotoRuta()                        { return estudianteFotoRuta; }
    public void setEstudianteFotoRuta(String estudianteFotoRuta) { this.estudianteFotoRuta = estudianteFotoRuta; }

    public int getCursoId()                  { return cursoId; }
    public void setCursoId(int cursoId)      { this.cursoId = cursoId; }

    public LocalDate getFecha()              { return fecha; }
    public void setFecha(LocalDate fecha)    { this.fecha = fecha; }

    public boolean isPresente()              { return presente; }
    public void setPresente(boolean presente){ this.presente = presente; }

    public LocalDateTime getHoraRegistro()                   { return horaRegistro; }
    public void setHoraRegistro(LocalDateTime horaRegistro)  { this.horaRegistro = horaRegistro; }
}
