package umg.biometrico.web.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de asistencia de un estudiante a un curso.
 */
@Entity
@Table(name = "asistencia")
@Data
@NoArgsConstructor
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id")
    private Persona estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @Column(name = "fecha_clase")
    private LocalDate fechaClase;

    @Column(name = "presente")
    private boolean presente = false;

    @Column(name = "hora_registro")
    private LocalDateTime horaRegistro = LocalDateTime.now();

    @Column(name = "registrado_por", length = 50)
    private String registradoPor;
}
