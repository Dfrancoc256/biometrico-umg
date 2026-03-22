package umg.biometrico.web.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Registro de cada ingreso de una persona por una puerta.
 */
@Entity
@Table(name = "registro_ingreso")
@Data
@NoArgsConstructor
public class RegistroIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puerta_id")
    private Puerta puerta;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "metodo_ingreso", length = 20)
    private String metodoIngreso = "FACIAL";

    @Column(name = "similitud_facial")
    private Double similitudFacial;

    @Column(name = "acceso_permitido")
    private boolean accesoPermitido = true;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
