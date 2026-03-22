package umg.biometrico;

import nu.pattern.OpenCV;
import umg.biometrico.configuracion.ConfiguracionBD;

/**
 * Iniciador - Punto de entrada principal del sistema biometrico UMG.
 */
public class Iniciador {

    public static void main(String[] argumentos) {
        // Cargar libreria nativa de OpenCV
        try {
            OpenCV.loadLocally();
            System.out.println("OpenCV cargado correctamente.");
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo cargar OpenCV — " + e.getMessage());
        }

        // Inicializar esquema de base de datos
        try {
            ConfiguracionBD.inicializarEsquema();
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo conectar a la base de datos — " + e.getMessage());
        }

        // Crear directorio de fotos
        new java.io.File(ConfiguracionBD.obtenerPropiedad("fotos.directorio", "fotos_personas")).mkdirs();

        // Lanzar interfaz grafica JavaFX
        AplicacionBiometrica.iniciar(argumentos);
    }
}
