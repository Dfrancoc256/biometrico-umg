package umg.biometrico.web.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una persona registrada en el sistema.
 * Puede ser estudiante, catedratico o administrativo.
 */
@Entity
@Table(name = "personas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carnet", unique = true, nullable = false, length = 20)
    @NotBlank(message = "El carnet es obligatorio")
    @Size(max = 20)
    private String carnet;

    @Column(name = "nombres", nullable = false, length = 100)
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Column(name = "correo", unique = true)
    @Email(message = "Correo electronico invalido")
    private String correo;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false)
    private TipoPersona tipoPersona = TipoPersona.ESTUDIANTE;

    @Column(name = "contrasena_hash")
    private String contrasenaHash;

    @Column(name = "foto_path")
    private String fotoPath;

    @Column(name = "encoding_facial", columnDefinition = "TEXT")
    private String encodingFacial;

    @Column(name = "activo")
    private boolean activo = true;

    @Column(name = "restringido")
    private boolean restringido = false;

    @Column(name = "motivo_restriccion", columnDefinition = "TEXT")
    private String motivoRestriccion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "carnet_generado")
    private boolean carnetGenerado = false;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean tieneEncodingFacial() {
        return encodingFacial != null && !encodingFacial.isBlank();
    }
}
