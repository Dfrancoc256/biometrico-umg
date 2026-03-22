package umg.biometrico.controlador;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.dao.PersonaDAO;
import umg.biometrico.modelo.Persona;
import umg.biometrico.modelo.TipoPersona;
import umg.biometrico.servicio.*;
import umg.biometrico.util.SesionActual;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ControladorRegistro - Proceso 1: Registro biografico y biometrico.
 */
public class ControladorRegistro {

    @FXML private TextField     campoNombre;
    @FXML private TextField     campoApellido;
    @FXML private TextField     campoTelefono;
    @FXML private TextField     campoCorreo;
    @FXML private TextField     campoCarrera;
    @FXML private TextField     campoSeccion;
    @FXML private ComboBox<TipoPersona> combotipo;
    @FXML private ImageView     vistaCamara;
    @FXML private ImageView     vistaFotoCapturada;
    @FXML private Label         etiquetaEstadoEncoding;
    @FXML private Label         etiquetaCarnet;
    @FXML private Button        botonCapturar;
    @FXML private Button        botonGuardar;

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final ServicioReconocimientoFacial servicioFacial = ServicioReconocimientoFacial.obtenerInstancia();
    private final ServicioPDF   servicioPDF    = ServicioPDF.obtenerInstancia();
    private final ServicioCorreo servicioCorreo = ServicioCorreo.obtenerInstancia();
    private final ServicioMensajeria servicioMensajeria = ServicioMensajeria.obtenerInstancia();

    private VideoCapture camara;
    private ScheduledExecutorService hilosCamara;
    private Mat fotogranaActual;
    private String encodingCapturado;
    private String rutaFotoCapturada;

    @FXML
    public void initialize() {
        combotipo.setItems(FXCollections.observableArrayList(TipoPersona.values()));
        combotipo.setValue(TipoPersona.ESTUDIANTE);
        iniciarCamara();
    }

    private void iniciarCamara() {
        camara = servicioFacial.abrirCamara(0);
        if (camara == null) {
            etiquetaEstadoEncoding.setText("Camara no disponible. Use 'Cargar foto'.");
            return;
        }
        hilosCamara = Executors.newSingleThreadScheduledExecutor();
        hilosCamara.scheduleAtFixedRate(() -> {
            Mat fotograma = servicioFacial.leerFotograma(camara);
            if (!fotograma.empty()) {
                this.fotogranaActual = fotograma;
                // Detectar y dibujar
                var rostros = servicioFacial.detectarRostros(fotograma);
                servicioFacial.dibujarRostros(fotograma, rostros, "", rostros.toArray().length > 0);
                Platform.runLater(() -> vistaCamara.setImage(matAImagen(fotograma)));
            }
        }, 0, 66, TimeUnit.MILLISECONDS);
    }

    @FXML
    public void accionCapturarFoto() {
        if (fotogranaActual == null || fotogranaActual.empty()) {
            mostrarAlerta("Camara no disponible.", Alert.AlertType.WARNING);
            return;
        }
        // Guardar fotograma temporal
        String rutaTemporal = "fotos_personas/temp_captura.jpg";
        Imgcodecs.imwrite(rutaTemporal, fotogranaActual);
        rutaFotoCapturada = rutaTemporal;
        vistaFotoCapturada.setImage(matAImagen(fotogranaActual));

        // Extraer encoding
        encodingCapturado = servicioFacial.extraerEncodingComoTexto(fotogranaActual);
        etiquetaEstadoEncoding.setText(encodingCapturado != null
                ? "Encoding facial extraido correctamente."
                : "No se detecto rostro. Intente de nuevo.");
    }

    @FXML
    public void accionCargarFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar foto");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagenes", "*.jpg", "*.jpeg", "*.png"));
        File archivo = selector.showOpenDialog(AplicacionBiometrica.obtenerEscenarioPrincipal());
        if (archivo != null) {
            rutaFotoCapturada = archivo.getAbsolutePath();
            vistaFotoCapturada.setImage(new Image(archivo.toURI().toString()));
            Mat imagen = Imgcodecs.imread(rutaFotoCapturada);
            encodingCapturado = imagen.empty() ? null : servicioFacial.extraerEncodingComoTexto(imagen);
            etiquetaEstadoEncoding.setText(encodingCapturado != null
                    ? "Encoding facial extraido." : "No se detecto rostro en la imagen.");
        }
    }

    @FXML
    public void accionGuardar() {
        if (!validarCampos()) return;

        Persona persona = new Persona();
        persona.setNombre(campoNombre.getText().trim());
        persona.setApellido(campoApellido.getText().trim());
        persona.setTelefono(campoTelefono.getText().trim());
        persona.setCorreo(campoCorreo.getText().trim());
        persona.setTipoPersona(combotipo.getValue());
        persona.setCarrera(campoCarrera.getText().trim());
        persona.setSeccion(campoSeccion.getText().trim());
        persona.setEncodingFacial(encodingCapturado);

        try {
            // Guardar en BD (genera numero de carnet automaticamente)
            personaDAO.insertar(persona);

            // Mover foto al directorio definitivo
            if (rutaFotoCapturada != null) {
                String rutaDefinitiva = "fotos_personas/persona_" + persona.getId() + ".jpg";
                Files.copy(Paths.get(rutaFotoCapturada), Paths.get(rutaDefinitiva),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                persona.setFotoRuta(rutaDefinitiva);
                personaDAO.actualizar(persona);
            }

            etiquetaCarnet.setText("Carnet asignado: " + persona.getNumeroCarnet());

            // Generar y enviar carnet PDF
            byte[] carnetPdf = servicioPDF.generarCarnet(persona);
            String rutaPdf = "carnets/carnet_" + persona.getNumeroCarnet() + ".pdf";
            servicioPDF.guardarArchivo(carnetPdf, rutaPdf);

            // Enviar por correo
            servicioCorreo.enviarCarnet(persona.getCorreo(), persona.getNombreCompleto(), carnetPdf);

            // Enviar por WhatsApp
            servicioMensajeria.enviarNotificacionCarnet(
                    persona.getTelefono(), persona.getNombreCompleto(), persona.getNumeroCarnet());

            mostrarAlerta("Persona registrada exitosamente.\nCarnet: " + persona.getNumeroCarnet(),
                    Alert.AlertType.INFORMATION);
            accionLimpiar();

        } catch (Exception e) {
            mostrarAlerta("Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void accionLimpiar() {
        campoNombre.clear(); campoApellido.clear(); campoTelefono.clear();
        campoCorreo.clear(); campoCarrera.clear(); campoSeccion.clear();
        combotipo.setValue(TipoPersona.ESTUDIANTE);
        vistaFotoCapturada.setImage(null);
        etiquetaEstadoEncoding.setText("Sin encoding.");
        etiquetaCarnet.setText("");
        encodingCapturado   = null;
        rutaFotoCapturada   = null;
    }

    @FXML
    public void accionVolver() {
        detenerCamara();
        AplicacionBiometrica.cargarVista("menu-principal.fxml");
    }

    private boolean validarCampos() {
        if (campoNombre.getText().isBlank() || campoApellido.getText().isBlank()) {
            mostrarAlerta("Nombre y apellido son obligatorios.", Alert.AlertType.WARNING); return false;
        }
        if (campoCorreo.getText().isBlank() || !campoCorreo.getText().contains("@")) {
            mostrarAlerta("Correo electronico invalido.", Alert.AlertType.WARNING); return false;
        }
        if (combotipo.getValue() == null) {
            mostrarAlerta("Seleccione el tipo de persona.", Alert.AlertType.WARNING); return false;
        }
        return true;
    }

    private Image matAImagen(Mat fotograma) {
        MatOfByte bufferBytes = new MatOfByte();
        Imgcodecs.imencode(".jpg", fotograma, bufferBytes);
        byte[] datosImagen = bufferBytes.toArray();
        return new Image(new java.io.ByteArrayInputStream(datosImagen));
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Platform.runLater(() -> new Alert(tipo, mensaje, ButtonType.OK).showAndWait());
    }

    private void detenerCamara() {
        if (hilosCamara != null) { hilosCamara.shutdownNow(); }
        if (camara != null)      { camara.release(); }
    }
}
