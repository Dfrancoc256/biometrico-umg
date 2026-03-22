package umg.biometrico.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.dao.PersonaDAO;
import umg.biometrico.modelo.Persona;
import umg.biometrico.util.SesionActual;

/**
 * ControladorLogin - Maneja la autenticacion de usuarios.
 */
public class ControladorLogin {

    @FXML private TextField     campoCarne;
    @FXML private PasswordField campoContrasena;
    @FXML private Label         etiquetaError;
    @FXML private Button        botonIngresar;

    private final PersonaDAO personaDAO = new PersonaDAO();

    @FXML
    public void initialize() {
        etiquetaError.setVisible(false);
        // Permitir login con Enter
        campoContrasena.setOnAction(this::accionIngresar);
    }

    @FXML
    public void accionIngresar(ActionEvent evento) {
        String carnet     = campoCarne.getText().trim();
        String contrasena = campoContrasena.getText();

        if (carnet.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Ingrese su numero de carnet y contrasena.");
            return;
        }

        botonIngresar.setDisable(true);
        try {
            Persona persona = personaDAO.autenticar(carnet, contrasena);
            if (persona != null) {
                SesionActual.obtenerInstancia().iniciarSesion(persona);
                AplicacionBiometrica.cargarVista("menu-principal.fxml");
            } else {
                mostrarError("Carnet o contrasena incorrectos.");
                campoContrasena.clear();
            }
        } catch (Exception e) {
            mostrarError("Error de conexion: " + e.getMessage());
        } finally {
            botonIngresar.setDisable(false);
        }
    }

    private void mostrarError(String mensaje) {
        etiquetaError.setText(mensaje);
        etiquetaError.setVisible(true);
    }
}
