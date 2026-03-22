package umg.biometrico.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.dao.PersonaDAO;
import umg.biometrico.modelo.Persona;

import java.util.List;

/**
 * ControladorRestringidos - Modulo de gestion de personas con acceso restringido.
 */
public class ControladorRestringidos {

    @FXML private TableView<Persona>          tablaPersonas;
    @FXML private TableColumn<Persona,String> columnaCarnet;
    @FXML private TableColumn<Persona,String> columnaNombre;
    @FXML private TableColumn<Persona,String> columnaTipo;
    @FXML private TableColumn<Persona,String> columnaMotivo;
    @FXML private TableColumn<Persona,Boolean> columnaRestringido;

    @FXML private TextField campoFiltro;
    @FXML private TextArea  campoMotivo;
    @FXML private Label     etiquetaSeleccionada;
    @FXML private Button    botonRestringir;
    @FXML private Button    botonDesrestringir;

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();
    private Persona personaSeleccionada;
    private boolean modoSoloRestringidos = false;

    @FXML
    public void initialize() {
        columnaCarnet.setCellValueFactory(new PropertyValueFactory<>("numeroCarnet"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        columnaTipo.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(
                        datos.getValue().getTipoPersona().getEtiqueta()));
        columnaMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoRestriccion"));
        columnaRestringido.setCellValueFactory(new PropertyValueFactory<>("restringido"));

        tablaPersonas.setItems(listaPersonas);
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> actualizarSeleccion(seleccionada));

        cargarPersonas();
    }

    private void cargarPersonas() {
        listaPersonas.clear();
        try {
            List<Persona> personas = modoSoloRestringidos
                    ? personaDAO.listarRestringidos()
                    : personaDAO.listarTodos();

            String filtro = campoFiltro.getText().trim().toLowerCase();
            if (!filtro.isEmpty()) {
                personas.removeIf(p ->
                        !p.getNombreCompleto().toLowerCase().contains(filtro)
                                && !nvl(p.getNumeroCarnet()).toLowerCase().contains(filtro));
            }
            listaPersonas.addAll(personas);
        } catch (Exception e) {
            mostrarAlerta("Error al cargar personas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void accionFiltrar() {
        cargarPersonas();
    }

    @FXML
    public void accionToggleSoloRestringidos() {
        modoSoloRestringidos = !modoSoloRestringidos;
        cargarPersonas();
    }

    private void actualizarSeleccion(Persona persona) {
        this.personaSeleccionada = persona;
        if (persona != null) {
            etiquetaSeleccionada.setText(persona.getNombreCompleto()
                    + " — " + persona.getTipoPersona().getEtiqueta());
            botonRestringir.setDisable(persona.isRestringido());
            botonDesrestringir.setDisable(!persona.isRestringido());
            if (persona.getMotivoRestriccion() != null) {
                campoMotivo.setText(persona.getMotivoRestriccion());
            }
        } else {
            etiquetaSeleccionada.setText("Ninguna persona seleccionada");
            botonRestringir.setDisable(true);
            botonDesrestringir.setDisable(true);
        }
    }

    @FXML
    public void accionRestringir() {
        if (personaSeleccionada == null) return;
        String motivo = campoMotivo.getText().trim();
        if (motivo.isEmpty()) {
            mostrarAlerta("Ingrese el motivo de restriccion.", Alert.AlertType.WARNING);
            return;
        }
        try {
            personaDAO.restringir(personaSeleccionada.getId(), motivo);
            mostrarAlerta(personaSeleccionada.getNombreCompleto()
                    + " ha sido RESTRINGIDO.", Alert.AlertType.INFORMATION);
            campoMotivo.clear();
            cargarPersonas();
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void accionDesrestringir() {
        if (personaSeleccionada == null) return;
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar la restriccion de " + personaSeleccionada.getNombreCompleto() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    personaDAO.desrestringir(personaSeleccionada.getId());
                    mostrarAlerta("Restriccion eliminada.", Alert.AlertType.INFORMATION);
                    campoMotivo.clear();
                    cargarPersonas();
                } catch (Exception e) {
                    mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
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

    private String nvl(String s) { return s != null ? s : ""; }
}
