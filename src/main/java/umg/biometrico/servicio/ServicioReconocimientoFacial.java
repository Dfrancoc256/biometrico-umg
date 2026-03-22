package umg.biometrico.servicio;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import umg.biometrico.modelo.Persona;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServicioReconocimientoFacial - Deteccion e identificacion facial con OpenCV.
 * Utiliza similitud coseno sobre parches faciales normalizados de 64x64 px.
 */
public class ServicioReconocimientoFacial {

    private static ServicioReconocimientoFacial instancia;

    private CascadeClassifier detectorRostros;
    private final Map<Integer, float[]> encodings  = new HashMap<>();
    private boolean modeloEntrenado = false;

    private static final String DIRECTORIO_FOTOS = "fotos_personas";
    private static final double UMBRAL_SIMILITUD  = 0.83;

    private ServicioReconocimientoFacial() {
        inicializarDetector();
        new File(DIRECTORIO_FOTOS).mkdirs();
    }

    public static ServicioReconocimientoFacial obtenerInstancia() {
        if (instancia == null) instancia = new ServicioReconocimientoFacial();
        return instancia;
    }

    // ------------------------------------------------------------------
    // Inicializacion del detector Haar
    // ------------------------------------------------------------------

    private void inicializarDetector() {
        try {
            Path rutaCascada = Paths.get("haarcascade_frontalface_default.xml");
            if (!rutaCascada.toFile().exists()) {
                try (InputStream flujo = getClass()
                        .getResourceAsStream("/haarcascade_frontalface_default.xml")) {
                    if (flujo != null) Files.copy(flujo, rutaCascada);
                }
            }
            detectorRostros = new CascadeClassifier(rutaCascada.toString());
            if (detectorRostros.empty()) {
                System.err.println("Advertencia: Cascada de deteccion facial vacia.");
            }
        } catch (Exception e) {
            System.err.println("No se pudo inicializar detector facial: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Deteccion
    // ------------------------------------------------------------------

    public MatOfRect detectarRostros(Mat fotograma) {
        MatOfRect rostros = new MatOfRect();
        if (detectorRostros == null || detectorRostros.empty()) return rostros;
        Mat gris = new Mat();
        Imgproc.cvtColor(fotograma, gris, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gris, gris);
        detectorRostros.detectMultiScale(gris, rostros, 1.1, 3, 0,
                new Size(80, 80), new Size());
        return rostros;
    }

    // ------------------------------------------------------------------
    // Encoding facial
    // ------------------------------------------------------------------

    public String extraerEncodingComoTexto(Mat fotograma) {
        float[] enc = extraerEncoding(fotograma);
        if (enc == null) return null;
        StringBuilder sb = new StringBuilder();
        for (float v : enc) { sb.append((int) v).append(","); }
        return sb.toString();
    }

    private float[] extraerEncoding(Mat fotograma) {
        MatOfRect rostros = detectarRostros(fotograma);
        if (rostros.toArray().length == 0) return null;
        Mat gris = new Mat();
        Imgproc.cvtColor(fotograma, gris, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gris, gris);
        Mat parche = new Mat(gris, rostros.toArray()[0]);
        Imgproc.resize(parche, parche, new Size(64, 64));
        float[] datos = new float[(int)(parche.total())];
        parche.get(0, 0, datos);
        return normalizar(datos);
    }

    private float[] normalizar(float[] arr) {
        float maximo = 1f, minimo = 0f;
        for (float v : arr) { if (v > maximo) maximo = v; if (v < minimo) minimo = v; }
        float rango = maximo - minimo;
        if (rango == 0) return arr;
        for (int i = 0; i < arr.length; i++) arr[i] = (arr[i] - minimo) / rango;
        return arr;
    }

    // ------------------------------------------------------------------
    // Entrenamiento del modelo
    // ------------------------------------------------------------------

    public void entrenarModelo(List<Persona> personas) {
        encodings.clear();
        for (Persona persona : personas) {
            cargarEncodingDePersona(persona);
        }
        modeloEntrenado = !encodings.isEmpty();
        System.out.println("Modelo entrenado con " + encodings.size() + " persona(s).");
    }

    private void cargarEncodingDePersona(Persona persona) {
        // Si tiene encoding guardado en BD, usarlo directamente
        if (persona.getEncodingFacial() != null && !persona.getEncodingFacial().isEmpty()) {
            String[] partes = persona.getEncodingFacial().split(",");
            float[] enc = new float[partes.length];
            int validos = 0;
            for (int i = 0; i < partes.length; i++) {
                try { enc[i] = Float.parseFloat(partes[i].trim()); validos++; }
                catch (NumberFormatException ignorado) {}
            }
            if (validos > 0) { encodings.put(persona.getId(), enc); return; }
        }
        // Si no, extraerlo de la foto
        if (persona.getFotoRuta() != null && new File(persona.getFotoRuta()).exists()) {
            Mat imagen = Imgcodecs.imread(persona.getFotoRuta());
            if (!imagen.empty()) {
                float[] enc = extraerEncoding(imagen);
                if (enc != null) encodings.put(persona.getId(), enc);
            }
        }
    }

    // ------------------------------------------------------------------
    // Reconocimiento
    // ------------------------------------------------------------------

    public ResultadoReconocimiento reconocer(Mat fotograma) {
        if (!modeloEntrenado || encodings.isEmpty()) {
            return new ResultadoReconocimiento(-1, 0.0, false);
        }
        float[] encodingActual = extraerEncoding(fotograma);
        if (encodingActual == null) return new ResultadoReconocimiento(-1, 0.0, false);

        int mejorId = -1;
        double mejorSimilitud = -1.0;
        for (Map.Entry<Integer, float[]> entrada : encodings.entrySet()) {
            double similitud = similitudCoseno(encodingActual, entrada.getValue());
            if (similitud > mejorSimilitud) {
                mejorSimilitud = similitud;
                mejorId = entrada.getKey();
            }
        }
        boolean reconocido = mejorSimilitud >= UMBRAL_SIMILITUD && mejorId > 0;
        return new ResultadoReconocimiento(reconocido ? mejorId : -1, mejorSimilitud, reconocido);
    }

    private double similitudCoseno(float[] vectorA, float[] vectorB) {
        int longitud = Math.min(vectorA.length, vectorB.length);
        double productoPunto = 0, normaA = 0, normaB = 0;
        for (int i = 0; i < longitud; i++) {
            productoPunto += vectorA[i] * vectorB[i];
            normaA        += vectorA[i] * vectorA[i];
            normaB        += vectorB[i] * vectorB[i];
        }
        if (normaA == 0 || normaB == 0) return 0;
        return productoPunto / (Math.sqrt(normaA) * Math.sqrt(normaB));
    }

    // ------------------------------------------------------------------
    // Utilidades de camara y dibujo
    // ------------------------------------------------------------------

    public String guardarFoto(Mat fotograma, int idPersona) {
        String rutaFoto = DIRECTORIO_FOTOS + "/persona_" + idPersona + ".jpg";
        Rect[] rostros  = detectarRostros(fotograma).toArray();
        Mat imagenGuardar = (rostros.length > 0) ? new Mat(fotograma, rostros[0]) : fotograma;
        Imgcodecs.imwrite(rutaFoto, imagenGuardar);
        return rutaFoto;
    }

    public void dibujarRostros(Mat fotograma, MatOfRect rostros, String nombre, boolean reconocido) {
        Scalar color = reconocido ? new Scalar(0, 255, 0) : new Scalar(0, 0, 255);
        for (Rect rect : rostros.toArray()) {
            Imgproc.rectangle(fotograma, rect.tl(), rect.br(), color, 2);
            if (nombre != null && !nombre.isEmpty()) {
                Imgproc.putText(fotograma, nombre,
                        new Point(rect.x, rect.y - 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, color, 2);
            }
        }
    }

    public VideoCapture abrirCamara(int indice) {
        VideoCapture camara = new VideoCapture(indice);
        return camara.isOpened() ? camara : null;
    }

    public Mat leerFotograma(VideoCapture camara) {
        Mat fotograma = new Mat();
        camara.read(fotograma);
        return fotograma;
    }

    public boolean isModeloEntrenado() { return modeloEntrenado; }

    // ------------------------------------------------------------------
    // Clase resultado del reconocimiento
    // ------------------------------------------------------------------

    public record ResultadoReconocimiento(int idPersona, double confianza, boolean reconocido) {}
}
