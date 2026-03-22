package umg.biometrico.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.util.SesionActual;

/**
 * ControladorMenu - Menu principal del sistema con acceso a todos los modulos.
 */
public class ControladorMenu {

    @FXML private Label etiquetaUsuario;
    @FXML private Label etiquetaTipo;

    @FXML
    public void initialize() {
        SesionActual sesion = SesionActual.obtenerInstancia();
        if (sesion.estaAutenticado()) {
            etiquetaUsuario.setText(sesion.getUsuarioActual().getNombreCompleto());
            etiquetaTipo.setText(sesion.getUsuarioActual().getTipoPersona().getEtiqueta());
        }
    }

    // --- Navegacion a modulos ---

    @FXML
    public void abrirRegistroPersona() {
        AplicacionBiometrica.cargarVista("registro-persona.fxml");
    }

    @FXML
    public void abrirIngresoPuertaPrincipal() {
        ControladorIngreso.establecerModoPuerta(false);
        AplicacionBiometrica.cargarVista("ingreso-webcam.fxml");
    }

    @FXML
    public void abrirIngresoSalon() {
        ControladorIngreso.establecerModoPuerta(true);
        AplicacionBiometrica.cargarVista("ingreso-webcam.fxml");
    }

    @FXML
    public void abrirDashboardCatedratico() {
        AplicacionBiometrica.cargarVista("dashboard-catedratico.fxml");
    }

    @FXML
    public void abrirReportes() {
        AplicacionBiometrica.cargarVista("reportes.fxml");
    }

    @FXML
    public void abrirPersonasRestringidas() {
        AplicacionBiometrica.cargarVista("personas-restringidas.fxml");
    }

    @FXML
    public void cerrarSesion() {
        SesionActual.obtenerInstancia().cerrarSesion();
        AplicacionBiometrica.cargarVista("login.fxml");
    }
}
