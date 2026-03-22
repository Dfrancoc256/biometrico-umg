package umg.biometrico.servicio;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import umg.biometrico.modelo.Asistencia;
import umg.biometrico.modelo.Curso;
import umg.biometrico.modelo.Persona;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ServicioPDF - Generacion de documentos PDF con iText 5.
 * Produce carnets de identificacion y listados de asistencia con diseno UMG.
 */
public class ServicioPDF {

    private static ServicioPDF instancia;

    // Colores UMG
    private static final BaseColor AZUL_UMG   = new BaseColor(0,   51, 102);
    private static final BaseColor AZUL_CLARO = new BaseColor(0,   85, 170);
    private static final BaseColor VERDE       = new BaseColor(39, 174,  96);
    private static final BaseColor ROJO        = new BaseColor(192, 57,  43);
    private static final BaseColor GRIS_FONDO  = new BaseColor(245, 245, 245);

    private final ServicioQR servicioQR = ServicioQR.obtenerInstancia();

    private ServicioPDF() {}

    public static ServicioPDF obtenerInstancia() {
        if (instancia == null) instancia = new ServicioPDF();
        return instancia;
    }

    // ------------------------------------------------------------------
    // Carnet de identificacion (85.6 x 54 mm = 226 x 142 pt)
    // ------------------------------------------------------------------

    public byte[] generarCarnet(Persona persona) throws Exception {
        ByteArrayOutputStream flujoSalida = new ByteArrayOutputStream();
        Document documento = new Document(new Rectangle(242, 153), 6, 6, 6, 6);
        PdfWriter escritor = PdfWriter.getInstance(documento, flujoSalida);
        documento.open();

        PdfContentByte lienzo = escritor.getDirectContent();

        // Fondo azul
        lienzo.setColorFill(AZUL_UMG);
        lienzo.rectangle(0, 0, 242, 153);
        lienzo.fill();

        // Franja decorativa lateral
        lienzo.setColorFill(AZUL_CLARO);
        lienzo.rectangle(0, 0, 8, 153);
        lienzo.fill();

        // Tarjeta blanca interior
        lienzo.setColorFill(BaseColor.WHITE);
        lienzo.roundRectangle(12, 10, 218, 133, 5);
        lienzo.fill();

        // Encabezado azul dentro de la tarjeta
        lienzo.setColorFill(AZUL_UMG);
        lienzo.roundRectangle(12, 120, 218, 23, 3);
        lienzo.fill();

        // Texto encabezado
        Font fuenteTitulo  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  7, BaseColor.WHITE);
        Font fuenteSubtit  = FontFactory.getFont(FontFactory.HELVETICA,       5, BaseColor.WHITE);
        Font fuenteLabel   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  6, AZUL_UMG);
        Font fuenteValor   = FontFactory.getFont(FontFactory.HELVETICA,       7, new BaseColor(30,30,30));
        Font fuenteCarnet  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  7, AZUL_UMG);
        Font fuenteFirma   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE,4, BaseColor.GRAY);

        ColumnText.showTextAligned(lienzo, Element.ALIGN_CENTER,
                new Phrase("UNIVERSIDAD MARIANO GALVEZ DE GUATEMALA", fuenteTitulo), 121, 135, 0);
        ColumnText.showTextAligned(lienzo, Element.ALIGN_CENTER,
                new Phrase("Sede La Florida — Zona 19", fuenteSubtit), 121, 127, 0);

        // Foto del estudiante
        if (persona.getFotoRuta() != null && new File(persona.getFotoRuta()).exists()) {
            try {
                Image foto = Image.getInstance(persona.getFotoRuta());
                foto.scaleAbsolute(42, 52);
                foto.setAbsolutePosition(18, 60);
                documento.add(foto);
            } catch (Exception ignorada) {}
        } else {
            // Recuadro placeholder si no hay foto
            lienzo.setColorStroke(new BaseColor(200, 200, 200));
            lienzo.rectangle(18, 60, 42, 52);
            lienzo.stroke();
        }

        // Datos del titular
        float xTexto = 68;
        ColumnText columna = new ColumnText(lienzo);
        columna.setSimpleColumn(xTexto, 55, 200, 118);

        // Tipo de persona como badge
        String etiquetaTipo = persona.getTipoPersona().getEtiqueta().toUpperCase();
        Paragraph tipoPara = new Paragraph(etiquetaTipo,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5,
                        BaseColor.WHITE));
        tipoPara.setSpacingAfter(2);
        columna.addText(tipoPara);

        Paragraph nombrePara = new Paragraph(persona.getNombreCompleto().toUpperCase(), fuenteCarnet);
        nombrePara.setSpacingAfter(3);
        columna.addText(nombrePara);

        if (nvl(persona.getCarrera()).length() > 0) {
            columna.addText(new Paragraph(persona.getCarrera(), fuenteValor));
        }
        columna.addText(new Paragraph("Carnet: " + nvl(persona.getNumeroCarnet()), fuenteLabel));
        columna.addText(new Paragraph(nvl(persona.getCorreo()), fuenteValor));
        columna.go();

        // Codigo QR
        byte[] bytesQR = servicioQR.generarQR(servicioQR.generarContenidoQR(persona), 55, 55);
        if (bytesQR.length > 0) {
            Image imagenQR = Image.getInstance(bytesQR);
            imagenQR.scaleAbsolute(45, 45);
            imagenQR.setAbsolutePosition(190, 12);
            documento.add(imagenQR);
        }

        // Firma y fecha
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String textofirma = "Emitido: " + LocalDateTime.now().format(formato)
                + "  |  Firma Digital: UMG-" + LocalDate.now().getYear();
        ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT,
                new Phrase(textofirma, fuenteFirma), 18, 14, 0);

        documento.close();
        return flujoSalida.toByteArray();
    }

    // ------------------------------------------------------------------
    // Listado de asistencia (carta)
    // ------------------------------------------------------------------

    public byte[] generarListadoAsistencia(Curso curso, LocalDate fecha,
                                            List<Asistencia> asistencias) throws Exception {
        ByteArrayOutputStream flujoSalida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 45, 45, 60, 45);
        PdfWriter.getInstance(documento, flujoSalida);
        documento.open();

        Font fuentePrincipal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, AZUL_UMG);
        Font fuenteSecundaria= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, AZUL_UMG);
        Font fuenteEncabezado= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font fuenteNormal    = FontFactory.getFont(FontFactory.HELVETICA,       9, BaseColor.BLACK);
        Font fuenteLabel     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  9, AZUL_UMG);

        // Encabezado
        Paragraph titulo = new Paragraph("UNIVERSIDAD MARIANO GALVEZ DE GUATEMALA", fuentePrincipal);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph sede = new Paragraph("Sede La Florida — Zona 19", fuenteSecundaria);
        sede.setAlignment(Element.ALIGN_CENTER);
        documento.add(sede);

        documento.add(new Paragraph(new Chunk(new DottedLineSeparator())));
        documento.add(Chunk.NEWLINE);

        Paragraph tituloLista = new Paragraph("LISTADO OFICIAL DE ASISTENCIA", fuenteSecundaria);
        tituloLista.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloLista);
        documento.add(Chunk.NEWLINE);

        // Informacion del curso
        PdfPTable tablaInfo = new PdfPTable(2);
        tablaInfo.setWidthPercentage(100);
        agregarCeldaInfo(tablaInfo, "Curso:",        nvl(curso.getNombre()) + " (" + nvl(curso.getCodigo()) + ")", fuenteLabel, fuenteNormal);
        agregarCeldaInfo(tablaInfo, "Catedratico:",  nvl(curso.getCatedraticoNombre()), fuenteLabel, fuenteNormal);
        agregarCeldaInfo(tablaInfo, "Salon:",        nvl(curso.getSalon()),   fuenteLabel, fuenteNormal);
        agregarCeldaInfo(tablaInfo, "Horario:",      nvl(curso.getHorario()), fuenteLabel, fuenteNormal);
        agregarCeldaInfo(tablaInfo, "Fecha:",
                fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
                        new java.util.Locale("es", "GT"))), fuenteLabel, fuenteNormal);
        documento.add(tablaInfo);
        documento.add(Chunk.NEWLINE);

        // Tabla de asistencia
        PdfPTable tablaAsistencia = new PdfPTable(new float[]{0.4f, 2.2f, 2.0f, 1.0f});
        tablaAsistencia.setWidthPercentage(100);

        for (String encabezado : new String[]{"#", "Nombre completo", "Correo electronico", "Asistencia"}) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezado));
            celda.setBackgroundColor(AZUL_UMG);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(6);
            tablaAsistencia.addCell(celda);
        }

        int numero = 1, presentes = 0, ausentes = 0;
        for (Asistencia a : asistencias) {
            BaseColor colorFila = (numero % 2 == 0) ? GRIS_FONDO : BaseColor.WHITE;
            agregarCeldaDato(tablaAsistencia, String.valueOf(numero++), colorFila, fuenteNormal, Element.ALIGN_CENTER);
            agregarCeldaDato(tablaAsistencia, nvl(a.getEstudianteNombre()), colorFila, fuenteNormal, Element.ALIGN_LEFT);
            agregarCeldaDato(tablaAsistencia, nvl(a.getEstudianteCorreo()), colorFila, fuenteNormal, Element.ALIGN_LEFT);

            boolean estaPresente = a.isPresente();
            if (estaPresente) presentes++; else ausentes++;

            PdfPCell celdaEstado = new PdfPCell(new Phrase(estaPresente ? "PRESENTE" : "AUSENTE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE)));
            celdaEstado.setBackgroundColor(estaPresente ? VERDE : ROJO);
            celdaEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaEstado.setPadding(5);
            tablaAsistencia.addCell(celdaEstado);
        }
        documento.add(tablaAsistencia);
        documento.add(Chunk.NEWLINE);

        // Resumen
        Font fuenteResumen = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, AZUL_UMG);
        documento.add(new Paragraph("Resumen:", fuenteResumen));
        documento.add(new Paragraph("Total de estudiantes: " + asistencias.size(), fuenteNormal));
        documento.add(new Paragraph("Presentes: " + presentes,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, VERDE)));
        documento.add(new Paragraph("Ausentes: " + ausentes,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, ROJO)));
        documento.add(Chunk.NEWLINE);
        documento.add(Chunk.NEWLINE);
        documento.add(Chunk.NEWLINE);

        // Firma del catedratico
        Paragraph firma = new Paragraph(
                "_______________________________\nFirma del Catedratico\n" + nvl(curso.getCatedraticoNombre()),
                fuenteNormal);
        firma.setAlignment(Element.ALIGN_RIGHT);
        documento.add(firma);

        documento.close();
        return flujoSalida.toByteArray();
    }

    public void guardarArchivo(byte[] datos, String rutaDestino) throws IOException {
        new File(rutaDestino).getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            fos.write(datos);
        }
    }

    private void agregarCeldaInfo(PdfPTable tabla, String etiqueta, String valor, Font fEtiqueta, Font fValor) {
        PdfPCell ce = new PdfPCell(new Phrase(etiqueta, fEtiqueta));
        ce.setBorder(Rectangle.NO_BORDER); ce.setPadding(3); tabla.addCell(ce);
        PdfPCell cv = new PdfPCell(new Phrase(valor, fValor));
        cv.setBorder(Rectangle.NO_BORDER); cv.setPadding(3); tabla.addCell(cv);
    }

    private void agregarCeldaDato(PdfPTable tabla, String texto, BaseColor fondo, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4);
        tabla.addCell(celda);
    }

    private String nvl(String valor) { return valor != null ? valor : ""; }
}
