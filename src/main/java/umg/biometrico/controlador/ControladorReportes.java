package umg.biometrico.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import umg.biometrico.AplicacionBiometrica;
import umg.biometrico.dao.InstalacionDAO;
import umg.biometrico.dao.IngresoDAO;
import umg.biometrico.modelo.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ControladorReportes - Proceso 5: Reportes y estadisticas con arboles visuales.
 *
 * Tipos de reporte:
 *   1. Historico de ingresos por puerta (instalacion > puerta > fechas > personas)
 *   2. Por fecha e instalacion/puerta (personas con foto, nombre, correo)
 *   3. Historico por salon (instalacion > nivel > salon > personas)
 *   4. Por fecha y salon
 */
public class ControladorReportes {

    @FXML private ComboBox<Instalacion> comboInstalacion;
    @FXML private ComboBox<Puerta>      comboPuerta;
    @FXML private DatePicker            selectorFecha;
    @FXML private ComboBox<String>      comboOrden;
    @FXML private TreeView<String>      arbolReporte;
    @FXML private TabPane               panelTabs;

    private final InstalacionDAO instalacionDAO = new InstalacionDAO();
    private final IngresoDAO     ingresoDAO     = new IngresoDAO();

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA  =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        selectorFecha.setValue(LocalDate.now());
        comboOrden.setItems(FXCollections.observableArrayList("Ascendente", "Descendente"));
        comboOrden.setValue("Descendente");
        arbolReporte.setRoot(new TreeItem<>("Seleccione instalacion y genere un reporte"));
        arbolReporte.setShowRoot(true);
        cargarInstalaciones();
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
            Instalacion inst = comboInstalacion.getValue();
            if (inst == null) return;
            List<Puerta> puertas = instalacionDAO.listarTodasPuertas(inst.getId());
            comboPuerta.setItems(FXCollections.observableArrayList(puertas));
            if (!puertas.isEmpty()) comboPuerta.setValue(puertas.get(0));
        } catch (Exception e) {
            System.err.println("Error al cargar puertas: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Reporte 1: Historico de ingresos por puerta
    // instalacion (raiz) > puerta (nivel 1) > fecha (nivel 2) > personas (hojas)
    // ------------------------------------------------------------------

    @FXML
    public void accionReporteHistoricoPuerta() {
        try {
            Instalacion instalacion = comboInstalacion.getValue();
            Puerta puerta = comboPuerta.getValue();
            if (instalacion == null || puerta == null) return;

            TreeItem<String> raiz = new TreeItem<>("🏛 " + instalacion.getNombre());
            raiz.setExpanded(true);

            TreeItem<String> nodoPuerta = new TreeItem<>("🚪 " + puerta.getNombre());
            nodoPuerta.setExpanded(true);
            raiz.getChildren().add(nodoPuerta);

            List<LocalDate> fechas = ingresoDAO.listarFechasConRegistro(puerta.getId());
            for (LocalDate fecha : fechas) {
                TreeItem<String> nodoFecha = new TreeItem<>("📅 " + fecha.format(FORMATO_FECHA));
                List<RegistroIngreso> registros = ingresoDAO.listarPorPuertaYFecha(puerta.getId(), fecha);
                for (RegistroIngreso r : registros) {
                    nodoFecha.getChildren().add(crearNodoPersona(r));
                }
                nodoPuerta.getChildren().add(nodoFecha);
            }

            if (fechas.isEmpty()) {
                nodoPuerta.getChildren().add(new TreeItem<>("(Sin registros)"));
            }

            arbolReporte.setRoot(raiz);
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ------------------------------------------------------------------
    // Reporte 2: Por fecha e instalacion/puerta
    // ------------------------------------------------------------------

    @FXML
    public void accionReportePorFecha() {
        try {
            Instalacion instalacion = comboInstalacion.getValue();
            Puerta puerta = comboPuerta.getValue();
            LocalDate fecha = selectorFecha.getValue();
            if (instalacion == null || puerta == null || fecha == null) return;

            List<RegistroIngreso> registros = ingresoDAO.listarPorPuertaYFecha(puerta.getId(), fecha);

            // Ordenar
            boolean ascendente = "Ascendente".equals(comboOrden.getValue());
            if (!ascendente) {
                java.util.Collections.reverse(registros);
            }

            TreeItem<String> raiz = new TreeItem<>("🏛 " + instalacion.getNombre());
            raiz.setExpanded(true);
            TreeItem<String> nodoPuerta = new TreeItem<>("🚪 " + puerta.getNombre()
                    + " — " + fecha.format(FORMATO_FECHA));
            nodoPuerta.setExpanded(true);
            raiz.getChildren().add(nodoPuerta);

            for (RegistroIngreso r : registros) {
                nodoPuerta.getChildren().add(crearNodoPersona(r));
            }

            if (registros.isEmpty()) {
                nodoPuerta.getChildren().add(new TreeItem<>("(Sin ingresos en esta fecha)"));
            }

            arbolReporte.setRoot(raiz);
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ------------------------------------------------------------------
    // Reporte 3: Historico por salon
    // instalacion (raiz) > nivel (nivel 1) > salon (nivel 2) > personas (hojas)
    // ------------------------------------------------------------------

    @FXML
    public void accionReporteHistoricoSalon() {
        try {
            Instalacion instalacion = comboInstalacion.getValue();
            if (instalacion == null) return;

            TreeItem<String> raiz = new TreeItem<>("🏛 " + instalacion.getNombre());
            raiz.setExpanded(true);

            List<Puerta> salones = instalacionDAO.listarSalones(instalacion.getId());

            // Agrupar por nivel
            java.util.Map<String, TreeItem<String>> nivelesMap = new java.util.LinkedHashMap<>();
            for (Puerta salon : salones) {
                String nivel = salon.getNivel() != null ? salon.getNivel() : "Sin nivel";
                TreeItem<String> nodoNivel = nivelesMap.computeIfAbsent(nivel,
                        k -> new TreeItem<>("🏢 " + k));

                TreeItem<String> nodoSalon = new TreeItem<>("🚪 " + salon.getNombre());
                List<RegistroIngreso> registros = ingresoDAO.listarPorPuerta(salon.getId());
                for (RegistroIngreso r : registros) {
                    nodoSalon.getChildren().add(crearNodoPersona(r));
                }
                if (registros.isEmpty()) {
                    nodoSalon.getChildren().add(new TreeItem<>("(Sin registros)"));
                }
                nodoNivel.getChildren().add(nodoSalon);
            }

            nivelesMap.values().forEach(n -> raiz.getChildren().add(n));
            arbolReporte.setRoot(raiz);
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ------------------------------------------------------------------
    // Reporte 4: Por fecha y salon
    // ------------------------------------------------------------------

    @FXML
    public void accionReporteSalonPorFecha() {
        try {
            Instalacion instalacion = comboInstalacion.getValue();
            Puerta salon = comboPuerta.getValue();
            LocalDate fecha = selectorFecha.getValue();
            if (instalacion == null || salon == null || fecha == null) return;

            List<RegistroIngreso> registros = ingresoDAO.listarPorPuertaYFecha(salon.getId(), fecha);
            boolean ascendente = "Ascendente".equals(comboOrden.getValue());
            if (!ascendente) java.util.Collections.reverse(registros);

            TreeItem<String> raiz = new TreeItem<>("🏛 " + instalacion.getNombre());
            raiz.setExpanded(true);
            TreeItem<String> nodoNivel = new TreeItem<>("🏢 " + (salon.getNivel() != null ? salon.getNivel() : ""));
            nodoNivel.setExpanded(true);
            TreeItem<String> nodoSalon = new TreeItem<>("🚪 " + salon.getNombre()
                    + " — " + fecha.format(FORMATO_FECHA));
            nodoSalon.setExpanded(true);

            for (RegistroIngreso r : registros) {
                nodoSalon.getChildren().add(crearNodoPersona(r));
            }
            if (registros.isEmpty()) {
                nodoSalon.getChildren().add(new TreeItem<>("(Sin ingresos en esta fecha)"));
            }

            nodoNivel.getChildren().add(nodoSalon);
            raiz.getChildren().add(nodoNivel);
            arbolReporte.setRoot(raiz);
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private TreeItem<String> crearNodoPersona(RegistroIngreso registro) {
        String texto = (registro.getPersonaNombre() != null ? registro.getPersonaNombre() : "Desconocido")
                + " | " + (registro.getPersonaCorreo() != null ? registro.getPersonaCorreo() : "")
                + " | " + (registro.getFechaHora() != null ? registro.getFechaHora().format(FORMATO_HORA) : "");
        return new TreeItem<>("👤 " + texto);
    }

    @FXML
    public void accionVolver() {
        AplicacionBiometrica.cargarVista("menu-principal.fxml");
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        new Alert(tipo, mensaje, ButtonType.OK).showAndWait();
    }
}
