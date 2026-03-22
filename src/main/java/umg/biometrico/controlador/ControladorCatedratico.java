package umg.biometrico.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.dao.AsistenciaDAO;
import umg.biometrico.dao.CursoDAO;
import umg.biometrico.modelo.Asistencia;
import umg.biometrico.modelo.Curso;
import umg.biometrico.modelo.Persona;
import umg.biometrico.servicio.ServicioCorreo;
import umg.biometrico.servicio.ServicioPDF;
import umg.biometrico.util.SesionActual;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * ControladorCatedratico - Proceso 4: Dashboard de asistencia para catedraticos.
 * Muestra arbol con foto, nombre y correo de cada estudiante (verde=presente, rojo=ausente).
 */
public class ControladorCatedratico {

    @FXML private ComboBox<Curso>     comboCursos;
    @FXML private Label               etiquetaFecha;
    @FXML private Label               etiquetaResumen;
    @FXML private FlowPane            panelAsistencia;
    @FXML private ScrollPane          scrollAsistencia;
    @FXML private Button              botonConfirmar;

    private final CursoDAO      cursoDAO      = new CursoDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private final ServicioPDF   servicioPDF   = ServicioPDF.obtenerInstancia();
    private final ServicioCorreo servicioCorreo = ServicioCorreo.obtenerInstancia();

    private List<Asistencia> asistenciasActuales = new ArrayList<>();
    private List<Integer> idsPresentes = new ArrayList<>();

    @FXML
    public void initialize() {
        etiquetaFecha.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy",
                        new java.util.Locale("es", "GT"))));
        cargarCursos();
    }

    private void cargarCursos() {
        try {
            Persona usuario = SesionActual.obtenerInstancia().getUsuarioActual();
            List<Curso> cursos = cursoDAO.listarPorCatedratico(usuario.getId());
            comboCursos.setItems(FXCollections.observableArrayList(cursos));
            if (!cursos.isEmpty()) {
                comboCursos.setValue(cursos.get(0));
                accionCargarAsistencia();
            }
        } catch (Exception e) {
            mostrarAlerta("Error al cargar cursos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void accionCargarAsistencia() {
        Curso curso = comboCursos.getValue();
        if (curso == null) return;
        panelAsistencia.getChildren().clear();
        asistenciasActuales.clear();
        idsPresentes.clear();

        try {
            // Buscar quienes ingresaron hoy (para marcar presentes automaticamente)
            List<Integer> idsPresentesHoy = asistenciaDAO.obtenerPresentesPorIngreso(
                    curso.getId(), LocalDate.now());

            // Obtener estudiantes del curso
            List<Persona> estudiantes = cursoDAO.listarEstudiantesDeCurso(curso.getId());

            for (Persona estudiante : estudiantes) {
                boolean presente = idsPresentesHoy.contains(estudiante.getId());
                if (presente) idsPresentes.add(estudiante.getId());

                Asistencia asistencia = new Asistencia();
                asistencia.setEstudianteId(estudiante.getId());
                asistencia.setCursoId(curso.getId());
                asistencia.setFecha(LocalDate.now());
                asistencia.setPresente(presente);
                asistencia.setEstudianteNombre(estudiante.getNombreCompleto());
                asistencia.setEstudianteCorreo(estudiante.getCorreo());
                asistencia.setEstudianteFotoRuta(estudiante.getFotoRuta());
                asistenciasActuales.add(asistencia);

                panelAsistencia.getChildren().add(crearTarjetaEstudiante(asistencia));
            }

            actualizarResumen();
        } catch (Exception e) {
            mostrarAlerta("Error al cargar asistencia: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Crea una tarjeta visual para el arbol de asistencia.
     * Muestra foto, nombre completo y correo del estudiante.
     * Fondo verde si presente, rojo si ausente.
     */
    private VBox crearTarjetaEstudiante(Asistencia asistencia) {
        VBox tarjeta = new VBox(5);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(10));
        tarjeta.setPrefWidth(155);
        tarjeta.setMaxWidth(155);
        tarjeta.setStyle(asistencia.isPresente()
                ? "-fx-background-color: #d1fae5; -fx-border-color: #16a34a; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2;"
                : "-fx-background-color: #fee2e2; -fx-border-color: #dc2626; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2;");

        // Foto del estudiante
        ImageView fotoVista = new ImageView();
        fotoVista.setFitWidth(70);
        fotoVista.setFitHeight(70);
        fotoVista.setPreserveRatio(true);
        fotoVista.setStyle("-fx-border-radius: 50; -fx-border-color: " +
                (asistencia.isPresente() ? "#16a34a" : "#dc2626") + "; -fx-border-width: 2;");
        if (asistencia.getEstudianteFotoRuta() != null
                && new File(asistencia.getEstudianteFotoRuta()).exists()) {
            fotoVista.setImage(new Image("file:" + asistencia.getEstudianteFotoRuta()));
        }

        // Estado (icono)
        Label iconoEstado = new Label(asistencia.isPresente() ? "✅" : "❌");
        iconoEstado.setStyle("-fx-font-size: 16px;");

        // Nombre
        Label etiquetaNombre = new Label(asistencia.getEstudianteNombre());
        etiquetaNombre.setWrapText(true);
        etiquetaNombre.setMaxWidth(140);
        etiquetaNombre.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                + (asistencia.isPresente() ? "#15803d" : "#b91c1c") + ";");
        etiquetaNombre.setAlignment(Pos.CENTER);

        // Correo
        Label etiquetaCorreo = new Label(asistencia.getEstudianteCorreo() != null
                ? asistencia.getEstudianteCorreo() : "");
        etiquetaCorreo.setWrapText(true);
        etiquetaCorreo.setMaxWidth(140);
        etiquetaCorreo.setStyle("-fx-font-size: 9px; -fx-text-fill: #6b7280;");
        etiquetaCorreo.setAlignment(Pos.CENTER);

        // Boton para alternar asistencia manualmente
        Button botonAlternar = new Button(asistencia.isPresente() ? "Marcar ausente" : "Marcar presente");
        botonAlternar.setStyle("-fx-font-size: 9px; -fx-padding: 3 6;");
        botonAlternar.setOnAction(ev -> {
            boolean nuevoEstado = !asistencia.isPresente();
            asistencia.setPresente(nuevoEstado);
            if (nuevoEstado) idsPresentes.add(asistencia.getEstudianteId());
            else idsPresentes.remove((Integer) asistencia.getEstudianteId());
            // Refrescar solo esta tarjeta
            int indice = panelAsistencia.getChildren().indexOf(tarjeta);
            panelAsistencia.getChildren().set(indice, crearTarjetaEstudiante(asistencia));
            actualizarResumen();
        });

        tarjeta.getChildren().addAll(fotoVista, iconoEstado, etiquetaNombre, etiquetaCorreo, botonAlternar);
        return tarjeta;
    }

    private void actualizarResumen() {
        long presentes = asistenciasActuales.stream().filter(Asistencia::isPresente).count();
        etiquetaResumen.setText("Total: " + asistenciasActuales.size()
                + "  |  Presentes: " + presentes
                + "  |  Ausentes: " + (asistenciasActuales.size() - presentes));
    }

    @FXML
    public void accionConfirmarAsistencia() {
        Curso curso = comboCursos.getValue();
        if (curso == null || asistenciasActuales.isEmpty()) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Esta seguro de confirmar la asistencia para " + curso.getNombre()
                        + " del dia de hoy?\n\nEsta accion guardara el registro oficial.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar Asistencia");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    asistenciaDAO.confirmarAsistencia(curso.getId(), LocalDate.now(), idsPresentes);

                    // Generar PDF y enviar por correo
                    byte[] pdf = servicioPDF.generarListadoAsistencia(
                            curso, LocalDate.now(), asistenciasActuales);
                    servicioPDF.guardarArchivo(pdf, "reportes/asistencia_"
                            + curso.getCodigo() + "_" + LocalDate.now() + ".pdf");

                    Persona catedratico = SesionActual.obtenerInstancia().getUsuarioActual();
                    servicioCorreo.enviarListadoAsistencia(
                            catedratico.getCorreo(), catedratico.getNombreCompleto(),
                            curso.getNombre(), pdf);

                    mostrarAlerta("Asistencia confirmada y PDF generado exitosamente.",
                            Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    mostrarAlerta("Error al confirmar: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void accionVolver() {
        AplicacionBiometrica.cargarVista("menu-principal.fxml");
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        new Alert(tipo, mensaje, ButtonType.OK).showAndWait();
    }
}
