package umg.biometrico;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * AplicacionBiometrica - Clase principal de JavaFX.
 * Gestiona el ciclo de vida de la aplicacion y el cambio de vistas.
 */
public class AplicacionBiometrica extends Application {

    private static Stage escenarioPrincipal;
    private static final String RUTA_FXML  = "/umg/biometrico/";
    private static final String RUTA_CSS   = "/umg/biometrico/estilos.css";

    public static void iniciar(String[] argumentos) {
        launch(argumentos);
    }

    @Override
    public void start(Stage escenario) throws Exception {
        escenarioPrincipal = escenario;
        escenario.setTitle("Sistema Biometrico UMG — Sede La Florida Zona 19");
        escenario.setMinWidth(900);
        escenario.setMinHeight(600);
        cargarVista("login.fxml");
        escenario.show();
    }

    /**
     * Carga una vista FXML y la establece como escena actual.
     * @param nombreFxml nombre del archivo FXML sin ruta
     */
    public static void cargarVista(String nombreFxml) {
        try {
            URL urlFxml = AplicacionBiometrica.class.getResource(RUTA_FXML + nombreFxml);
            if (urlFxml == null) throw new RuntimeException("No se encontro: " + RUTA_FXML + nombreFxml);

            Parent raiz = FXMLLoader.load(urlFxml);
            Scene escena = new Scene(raiz);

            URL urlCss = AplicacionBiometrica.class.getResource(RUTA_CSS);
            if (urlCss != null) escena.getStylesheets().add(urlCss.toExternalForm());

            escenarioPrincipal.setScene(escena);
        } catch (Exception e) {
            System.err.println("Error al cargar la vista '" + nombreFxml + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga una vista FXML en una ventana nueva (dialog).
     */
    public static void cargarVistaEnVentana(String nombreFxml, String titulo) {
        try {
            URL urlFxml = AplicacionBiometrica.class.getResource(RUTA_FXML + nombreFxml);
            if (urlFxml == null) return;

            Parent raiz = FXMLLoader.load(urlFxml);
            Scene escena = new Scene(raiz);

            URL urlCss = AplicacionBiometrica.class.getResource(RUTA_CSS);
            if (urlCss != null) escena.getStylesheets().add(urlCss.toExternalForm());

            Stage ventana = new Stage();
            ventana.setTitle(titulo);
            ventana.setScene(escena);
            ventana.initOwner(escenarioPrincipal);
            ventana.show();
        } catch (Exception e) {
            System.err.println("Error al cargar ventana '" + nombreFxml + "': " + e.getMessage());
        }
    }

    public static Stage obtenerEscenarioPrincipal() {
        return escenarioPrincipal;
    }
}
