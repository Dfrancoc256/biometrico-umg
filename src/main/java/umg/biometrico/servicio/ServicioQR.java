package umg.biometrico.servicio;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import umg.biometrico.modelo.Persona;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * ServicioQR - Generacion de codigos QR con ZXing.
 */
public class ServicioQR {

    private static ServicioQR instancia;

    private ServicioQR() {}

    public static ServicioQR obtenerInstancia() {
        if (instancia == null) instancia = new ServicioQR();
        return instancia;
    }

    public byte[] generarQR(String contenido, int ancho, int alto) {
        try {
            Map<EncodeHintType, Object> sugerencias = new EnumMap<>(EncodeHintType.class);
            sugerencias.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            sugerencias.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter escritor = new QRCodeWriter();
            BitMatrix matriz = escritor.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto, sugerencias);

            ByteArrayOutputStream flujoSalida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", flujoSalida);
            return flujoSalida.toByteArray();
        } catch (Exception e) {
            System.err.println("Error al generar QR: " + e.getMessage());
            return new byte[0];
        }
    }

    public String generarContenidoQR(Persona persona) {
        return "UMG-BIOMETRICO\n"
                + "Carnet: "   + nvl(persona.getNumeroCarnet()) + "\n"
                + "Nombre: "   + persona.getNombreCompleto() + "\n"
                + "Tipo: "     + persona.getTipoPersona().getEtiqueta() + "\n"
                + "Correo: "   + nvl(persona.getCorreo()) + "\n"
                + "Carrera: "  + nvl(persona.getCarrera());
    }

    private String nvl(String valor) { return valor != null ? valor : ""; }
}
