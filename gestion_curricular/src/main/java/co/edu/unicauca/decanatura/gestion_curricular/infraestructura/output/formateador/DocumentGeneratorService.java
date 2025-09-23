package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentTemplate;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DocumentGeneratorService {

    /**
     * Generar documento Word usando plantilla
     */
    public ByteArrayOutputStream generarDocumento(DocumentRequest request) throws IOException {
        String templatePath = obtenerRutaPlantilla(request.getTipoDocumento());
        
        // Cargar plantilla
        ClassPathResource resource = new ClassPathResource(templatePath);
        InputStream templateStream = resource.getInputStream();
        
        // Crear documento desde plantilla
        XWPFDocument document = new XWPFDocument(templateStream);
        
        // Reemplazar placeholders
        reemplazarPlaceholders(document, request);
        
        // Convertir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        
        return outputStream;
    }

    /**
     * Obtener plantillas disponibles por proceso
     */
    public List<DocumentTemplate> getTemplates(String proceso) {
        Map<String, List<DocumentTemplate>> templates = new HashMap<>();
        
        // Plantilla de homologación
        templates.put("homologacion", Arrays.asList(
            new DocumentTemplate(
                "OFICIO_HOMOLOGACION",
                "Oficio de Homologación",
                "Documento oficial que aprueba la homologación de asignaturas",
                Arrays.asList("numeroDocumento", "fechaDocumento"),
                Arrays.asList("observaciones")
            )
        ));
        
        // Plantilla de paz y salvo
        templates.put("paz-salvo", Arrays.asList(
            new DocumentTemplate(
                "PAZ_SALVO",
                "Paz y Salvo",
                "Documento que certifica que el estudiante no tiene pendientes académicos",
                Arrays.asList("numeroDocumento", "fechaDocumento"),
                Arrays.asList("observaciones", "semestre")
            )
        ));
        
        // Plantilla de reingreso
        templates.put("reingreso", Arrays.asList(
            new DocumentTemplate(
                "RESOLUCION_REINGRESO",
                "Resolución de Reingreso",
                "Documento que autoriza el reingreso del estudiante al programa",
                Arrays.asList("numeroDocumento", "fechaDocumento"),
                Arrays.asList("observaciones", "motivoReingreso")
            )
        ));
        
        return templates.getOrDefault(proceso, new ArrayList<>());
    }

    private String obtenerRutaPlantilla(String tipoDocumento) {
        Map<String, String> rutas = new HashMap<>();
        rutas.put("OFICIO_HOMOLOGACION", "templates/oficio-homologacion.docx");
        rutas.put("PAZ_SALVO", "templates/paz-salvo.docx");
        rutas.put("RESOLUCION_REINGRESO", "templates/resolucion-reingreso.docx");
        
        return rutas.getOrDefault(tipoDocumento, "templates/documento-generico.docx");
    }

    private void reemplazarPlaceholders(XWPFDocument document, DocumentRequest request) {
        Map<String, String> replacements = crearMapaReemplazos(request);
        
        // Reemplazar en párrafos
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            reemplazarEnParrafo(paragraph, replacements);
        }
        
        // Reemplazar en tablas
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        reemplazarEnParrafo(paragraph, replacements);
                    }
                }
            }
        }
    }

    private void reemplazarEnParrafo(XWPFParagraph paragraph, Map<String, String> replacements) {
        String texto = paragraph.getText();
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "[" + entry.getKey() + "]";
            if (texto.contains(placeholder)) {
                texto = texto.replace(placeholder, entry.getValue());
            }
        }
        
        // Limpiar párrafo y agregar nuevo texto
        if (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }
        XWPFRun run = paragraph.createRun();
        run.setText(texto);
    }

    private Map<String, String> crearMapaReemplazos(DocumentRequest request) {
        Map<String, String> replacements = new HashMap<>();
        
        // Datos del documento
        Map<String, Object> datosDocumento = request.getDatosDocumento();
        replacements.put("NUMERO_DOCUMENTO", datosDocumento.get("numeroDocumento").toString());
        replacements.put("FECHA_DOCUMENTO", formatearFecha(datosDocumento.get("fechaDocumento")));
        replacements.put("OBSERVACIONES", datosDocumento.getOrDefault("observaciones", "").toString());
        
        // Datos de la solicitud
        Map<String, Object> datosSolicitud = request.getDatosSolicitud();
        replacements.put("NOMBRE_ESTUDIANTE", datosSolicitud.get("nombreEstudiante").toString());
        replacements.put("CODIGO_ESTUDIANTE", datosSolicitud.get("codigoEstudiante").toString());
        replacements.put("PROGRAMA", datosSolicitud.get("programa").toString());
        replacements.put("FECHA_SOLICITUD", formatearFecha(datosSolicitud.get("fechaSolicitud")));
        
        // Datos de la universidad (predefinidos)
        replacements.put("NOMBRE_UNIVERSIDAD", "UNIVERSIDAD DEL CAUCA");
        replacements.put("FACULTAD", "FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES");
        replacements.put("CIUDAD", "Popayán");
        replacements.put("FECHA_ACTUAL", formatearFecha(LocalDate.now()));
        
        // Campos específicos según el tipo de documento
        if ("OFICIO_HOMOLOGACION".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "homologación de asignaturas");
            replacements.put("TITULO_DOCUMENTO", "OFICIO DE HOMOLOGACIÓN");
        } else if ("PAZ_SALVO".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "paz y salvo académico");
            replacements.put("TITULO_DOCUMENTO", "PAZ Y SALVO ACADÉMICO");
        } else if ("RESOLUCION_REINGRESO".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "reingreso al programa");
            replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE REINGRESO");
        }
        
        return replacements;
    }

    private String formatearFecha(Object fecha) {
        if (fecha == null) return "";
        
        try {
            if (fecha instanceof LocalDate) {
                return ((LocalDate) fecha).format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
            } else if (fecha instanceof String) {
                // Si viene como string, intentar parsearlo
                LocalDate localDate = LocalDate.parse(fecha.toString());
                return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
            }
        } catch (Exception e) {
            // Si hay error, retornar la fecha como string
            return fecha.toString();
        }
        
        return fecha.toString();
    }
}
