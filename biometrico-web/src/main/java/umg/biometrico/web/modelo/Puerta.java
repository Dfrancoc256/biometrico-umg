package umg.biometrico.web.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA para puertas de acceso y salones de clase.
 */
@Entity
@Table(name = "puertas")
@Data
@NoArgsConstructor
public class Puerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "nivel", length = 10)
    private String nivel;

    @Column(name = "activa")
    private boolean activa = true;
}
