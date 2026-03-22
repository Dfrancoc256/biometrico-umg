package umg.biometrico.servicio;

import umg.biometrico.configuracion.ConfiguracionBD;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * ServicioCorreo - Envio de correos electronicos via SMTP.
 * Configurado en configuracion.properties (correo.host, correo.puerto, correo.usuario, correo.contrasena).
 * Si las credenciales estan vacias, el servicio queda deshabilitado.
 */
public class ServicioCorreo {

    private static ServicioCorreo instancia;

    private final String servidorSmtp;
    private final String puerto;
    private final String usuarioCorreo;
    private final String contrasenaCorreo;
    private final boolean habilitado;

    private ServicioCorreo() {
        servidorSmtp    = ConfiguracionBD.obtenerPropiedad("correo.host",     "smtp.gmail.com");
        puerto          = ConfiguracionBD.obtenerPropiedad("correo.puerto",   "587");
        usuarioCorreo   = ConfiguracionBD.obtenerPropiedad("correo.usuario",  "");
        contrasenaCorreo= ConfiguracionBD.obtenerPropiedad("correo.contrasena","");
        habilitado      = !usuarioCorreo.isEmpty() && !contrasenaCorreo.isEmpty();

        if (!habilitado) {
            System.out.println("Correo deshabilitado. Configure correo.usuario y correo.contrasena en configuracion.properties");
        }
    }

    public static ServicioCorreo obtenerInstancia() {
        if (instancia == null) instancia = new ServicioCorreo();
        return instancia;
    }

    public boolean isHabilitado() { return habilitado; }

    /**
     * Envia el carnet PDF al correo del usuario registrado.
     */
    public void enviarCarnet(String destinatario, String nombrePersona, byte[] pdfCarnet) throws Exception {
        if (!habilitado) { System.out.println("Correo no configurado. Saltando envio."); return; }

        Session sesion = crearSesion();
        Message mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(usuarioCorreo));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        mensaje.setSubject("Carnet Digital UMG — " + nombrePersona);

        MimeBodyPart parteCuerpo = new MimeBodyPart();
        parteCuerpo.setText("Estimado/a " + nombrePersona + ",\n\n"
                + "Adjunto encontrara su carnet digital de identificacion de la\n"
                + "Universidad Mariano Galvez — Sede La Florida, Zona 19.\n\n"
                + "Atentamente,\nSistema Biometrico UMG 2026");

        MimeBodyPart partePDF = new MimeBodyPart();
        partePDF.setDataHandler(new DataHandler(nuevaFuenteDatos(pdfCarnet, "application/pdf")));
        partePDF.setFileName("carnet_" + limpiarNombre(nombrePersona) + ".pdf");

        Multipart multiParte = new MimeMultipart();
        multiParte.addBodyPart(parteCuerpo);
        multiParte.addBodyPart(partePDF);
        mensaje.setContent(multiParte);
        Transport.send(mensaje);
        System.out.println("Carnet enviado a " + destinatario);
    }

    /**
     * Envia el listado de asistencia PDF al correo del catedratico.
     */
    public void enviarListadoAsistencia(String destinatario, String nombreCatedratico,
                                        String nombreCurso, byte[] pdfListado) throws Exception {
        if (!habilitado) { System.out.println("Correo no configurado. Saltando envio."); return; }

        Session sesion = crearSesion();
        Message mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(usuarioCorreo));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        mensaje.setSubject("Listado de Asistencia — " + nombreCurso);

        MimeBodyPart parteCuerpo = new MimeBodyPart();
        parteCuerpo.setText("Estimado/a " + nombreCatedratico + ",\n\n"
                + "Adjunto el listado de asistencia del curso: " + nombreCurso + "\n\n"
                + "Atentamente,\nSistema Biometrico UMG 2026");

        MimeBodyPart partePDF = new MimeBodyPart();
        partePDF.setDataHandler(new DataHandler(nuevaFuenteDatos(pdfListado, "application/pdf")));
        partePDF.setFileName("asistencia_" + limpiarNombre(nombreCurso) + ".pdf");

        Multipart multiParte = new MimeMultipart();
        multiParte.addBodyPart(parteCuerpo);
        multiParte.addBodyPart(partePDF);
        mensaje.setContent(multiParte);
        Transport.send(mensaje);
    }

    private Session crearSesion() {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth",            "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host",             servidorSmtp);
        propiedades.put("mail.smtp.port",             puerto);

        return Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuarioCorreo, contrasenaCorreo);
            }
        });
    }

    private DataSource nuevaFuenteDatos(byte[] datos, String tipoContenido) {
        return new DataSource() {
            @Override public InputStream  getInputStream()  { return new ByteArrayInputStream(datos); }
            @Override public OutputStream getOutputStream() throws IOException { throw new IOException("Solo lectura"); }
            @Override public String       getContentType()  { return tipoContenido; }
            @Override public String       getName()         { return "adjunto"; }
        };
    }

    private String limpiarNombre(String nombre) {
        return nombre != null ? nombre.replaceAll("[^a-zA-Z0-9]", "_") : "archivo";
    }
}
