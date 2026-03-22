package umg.biometrico.controlador;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.dao.InstalacionDAO;
import umg.biometrico.dao.IngresoDAO;
import umg.biometrico.dao.PersonaDAO;
import umg.biometrico.modelo.*;
import umg.biometrico.servicio.ServicioReconocimientoFacial;
import umg.biometrico.servicio.ServicioReconocimientoFacial.ResultadoReconocimiento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

/**
 * ControladorIngreso - Proceso 2 (puerta principal) y Proceso 3 (salon de clases).
 * Modo puerta: es_salon=false. Modo salon: es_salon=true.
 */
public class ControladorIngreso {

    // Modo compartido entre instancias (se establece desde ControladorMenu)
    private static boolean modoSalon = false;

    public static void establecerModoPuerta(boolean esSalon) {
        modoSalon = esSalon;
    }

    @FXML private ImageView  vistaCamara;
    @FXML private Label      etiquetaEstado;
    @FXML private Label      etiquetaPersona;
    @FXML private Label      etiquetaCarnet;
    @FXML private Label      etiquetaTitulo;
    @FXML private ComboBox<Instalacion> comboInstalacion;
    @FXML private ComboBox<Puerta>      comboPuerta;
    @FXML private TextField  campoCarnetManual;
    @FXML private ListView<String> listaRegistros;

    private final ServicioReconocimientoFacial servicioFacial = ServicioReconocimientoFacial.obtenerInstancia();
    private final PersonaDAO    personaDAO     = new PersonaDAO();
    private final IngresoDAO    ingresoDAO     = new IngresoDAO();
    private final InstalacionDAO instalacionDAO = new InstalacionDAO();

    private VideoCapture camara;
    private ScheduledExecutorService hilosCamara;
    private final ObservableList<String> registrosRecientes = FXCollections.observableArrayList();
    private long ultimoRegistroMs = 0;
    private static final long PAUSA_ENTRE_REGISTROS_MS = 3000;

    @FXML
    public void initialize() {
        etiquetaTitulo.setText(modoSalon ? "Proceso 3 — Ingreso por Salon" : "Proceso 2 — Ingreso Puerta Principal");
        listaRegistros.setItems(registrosRecientes);
        cargarInstalaciones();
        entrenarModeloFacial();
        iniciarCamara();
    }

    private void cargarInstalaciones() {
        try {
            List<Instalacion> instalaciones = instalacionDAO.listarInstalaciones();
            comboInstalacion.setItems(FXCollections.observableArrayList(instalaciones));
            if (!instalaciones.isEmpty()) {
                comboInstalacion.setValue(instalaciones.get(0));
                cargarPuertas();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar instalaciones: " + e.getMessage());
        }
    }

    @FXML
    public void cargarPuertas() {
        try {
            Instalacion instalacion = comboInstalacion.getValue();
            if (instalacion == null) return;
            List<Puerta> puertas = modoSalon
                    ? instalacionDAO.listarSalones(instalacion.getId())
                    : instalacionDAO.listarPuertas(instalacion.getId());
            comboPuerta.setItems(FXCollections.observableArrayList(puertas));
            if (!puertas.isEmpty()) comboPuerta.setValue(puertas.get(0));
        } catch (Exception e) {
            System.err.println("Error al cargar puertas: " + e.getMessage());
        }
    }

    private void entrenarModeloFacial() {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<Persona> personas = personaDAO.listarActivos();
                servicioFacial.entrenarModelo(personas);
            } catch (Exception e) {
                System.err.println("Error entrenando modelo: " + e.getMessage());
            }
        });
    }

    private void iniciarCamara() {
        camara = servicioFacial.abrirCamara(0);
        if (camara == null) {
            etiquetaEstado.setText("Camara no disponible.");
            return;
        }
        hilosCamara = Executors.newSingleThreadScheduledExecutor();
        hilosCamara.scheduleAtFixedRate(this::procesarFotograma, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void procesarFotograma() {
        try {
            Mat fotograma = servicioFacial.leerFotograma(camara);
            if (fotograma.empty()) return;

            ResultadoReconocimiento resultado = servicioFacial.reconocer(fotograma);

            String nombreMostrar = "";
            if (resultado.reconocido()) {
                Persona persona = personaDAO.buscarPorId(resultado.idPersona());
                if (persona != null) {
                    nombreMostrar = persona.getNombreCompleto();
                    // Registrar ingreso (max 1 vez cada PAUSA_ENTRE_REGISTROS_MS)
                    long ahora = System.currentTimeMillis();
                    if (ahora - ultimoRegistroMs >= PAUSA_ENTRE_REGISTROS_MS) {
                        ultimoRegistroMs = ahora;
                        registrarIngreso(persona);
                    }
                }
            }

            var rostros = servicioFacial.detectarRostros(fotograma);
            servicioFacial.dibujarRostros(fotograma, rostros, nombreMostrar, resultado.reconocido());

            final String nombreFinal = nombreMostrar;
            final boolean reconocido = resultado.reconocido();
            Platform.runLater(() -> {
                vistaCamara.setImage(matAImagen(fotograma));
                if (reconocido) {
                    etiquetaEstado.setText("ACCESO PERMITIDO");
                    etiquetaEstado.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    etiquetaPersona.setText(nombreFinal);
                } else {
                    etiquetaEstado.setText(rostros.toArray().length > 0 ? "Identificando..." : "Esperando...");
                    etiquetaEstado.setStyle("-fx-text-fill: #f59e0b;");
                }
            });
        } catch (Exception e) {
            System.err.println("Error en fotograma: " + e.getMessage());
        }
    }

    private void registrarIngreso(Persona persona) {
        try {
            Puerta puerta = comboPuerta.getValue();
            if (puerta == null) return;
            if (persona.isRestringido()) {
                Platform.runLater(() -> {
                    etiquetaEstado.setText("ACCESO DENEGADO — PERSONA RESTRINGIDA");
                    etiquetaEstado.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                });
                return;
            }
            RegistroIngreso registro = new RegistroIngreso();
            registro.setPersonaId(persona.getId());
            registro.setPuertaId(puerta.getId());
            registro.setMetodo("FACIAL");
            ingresoDAO.insertar(registro);

            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String entrada = hora + " | " + persona.getNombreCompleto() + " | " + puerta.getNombre();
            Platform.runLater(() -> registrosRecientes.add(0, entrada));
        } catch (Exception e) {
            System.err.println("Error al registrar ingreso: " + e.getMessage());
        }
    }

    @FXML
    public void accionBuscarPorCarnet() {
        String carnet = campoCarnetManual.getText().trim();
        if (carnet.isEmpty()) return;
        try {
            Persona persona = personaDAO.buscarPorCarnet(carnet);
            if (persona != null) {
                registrarIngreso(persona);
                etiquetaPersona.setText(persona.getNombreCompleto());
                etiquetaCarnet.setText(persona.getNumeroCarnet());
                etiquetaEstado.setText(persona.isRestringido() ? "ACCESO DENEGADO" : "ACCESO MANUAL REGISTRADO");
            } else {
                etiquetaEstado.setText("Carnet no encontrado.");
            }
        } catch (Exception e) {
            etiquetaEstado.setText("Error: " + e.getMessage());
        }
        campoCarnetManual.clear();
    }

    @FXML
    public void accionVolver() {
        detenerCamara();
        AplicacionBiometrica.cargarVista("menu-principal.fxml");
    }

    private Image matAImagen(Mat fotograma) {
        MatOfByte bufferBytes = new MatOfByte();
        Imgcodecs.imencode(".jpg", fotograma, bufferBytes);
        return new Image(new java.io.ByteArrayInputStream(bufferBytes.toArray()));
    }

    private void detenerCamara() {
        if (hilosCamara != null) hilosCamara.shutdownNow();
        if (camara != null)      camara.release();
    }
}
