package umg.biometrico.servicio;

import umg.biometrico.configuracion.ConfiguracionBD;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ServicioMensajeria - Envio de mensajes de WhatsApp via API CallMeBot (gratuita).
 *
 * Configuracion en configuracion.properties:
 *   whatsapp.apikey = TU_API_KEY_DE_CALLMEBOT
 *
 * Pasos para activar CallMeBot:
 *   1. Enviar "I allow callmebot to send me messages" al numero +34 644 82 93 46 por WhatsApp
 *   2. Recibira su apikey en respuesta. Ingrésela en configuracion.properties
 */
public class ServicioMensajeria {

    private static ServicioMensajeria instancia;

    private final String claveApi;
    private final boolean habilitado;

    private ServicioMensajeria() {
        claveApi  = ConfiguracionBD.obtenerPropiedad("whatsapp.apikey", "");
        habilitado = !claveApi.isEmpty();
        if (!habilitado) {
            System.out.println("WhatsApp deshabilitado. Configure whatsapp.apikey en configuracion.properties");
        }
    }

    public static ServicioMensajeria obtenerInstancia() {
        if (instancia == null) instancia = new ServicioMensajeria();
        return instancia;
    }

    public boolean isHabilitado() { return habilitado; }

    /**
     * Envia un mensaje de WhatsApp al numero de telefono indicado.
     * @param numeroTelefono numero con codigo de pais, sin '+' ni espacios (ej: "50255550123")
     * @param mensaje texto del mensaje a enviar
     */
    public void enviarMensaje(String numeroTelefono, String mensaje) {
        if (!habilitado) { System.out.println("WhatsApp no configurado. Saltando envio."); return; }
        if (numeroTelefono == null || numeroTelefono.isBlank()) return;

        // Limpiar el numero de telefono
        String numero = numeroTelefono.replaceAll("[^0-9]", "");

        try {
            String mensajeCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            String urlTexto = "https://api.callmebot.com/whatsapp.php"
                    + "?phone="  + numero
                    + "&text="   + mensajeCodificado
                    + "&apikey=" + claveApi;

            URL url = new URL(urlTexto);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(10_000);
            conexion.setReadTimeout(10_000);

            int codigoRespuesta = conexion.getResponseCode();
            if (codigoRespuesta == 200) {
                System.out.println("WhatsApp enviado a " + numero);
            } else {
                System.err.println("Error al enviar WhatsApp. Codigo HTTP: " + codigoRespuesta);
            }
            conexion.disconnect();
        } catch (Exception e) {
            System.err.println("Error de conexion al enviar WhatsApp: " + e.getMessage());
        }
    }

    /**
     * Envia notificacion del carnet generado al numero de telefono del estudiante.
     */
    public void enviarNotificacionCarnet(String numeroTelefono, String nombrePersona, String numeroCarnet) {
        String mensaje = "Bienvenido/a a la UMG Sede La Florida!\n"
                + "Su carnet ha sido generado exitosamente.\n"
                + "Nombre: " + nombrePersona + "\n"
                + "Carnet: " + numeroCarnet + "\n"
                + "Universidad Mariano Galvez — Zona 19";
        enviarMensaje(numeroTelefono, mensaje);
    }
}
