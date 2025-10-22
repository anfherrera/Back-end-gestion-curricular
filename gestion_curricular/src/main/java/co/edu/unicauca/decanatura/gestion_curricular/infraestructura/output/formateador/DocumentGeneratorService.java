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
        System.out.println("🔍 [DOCUMENT_GENERATOR] Iniciando generación de documento...");
        System.out.println("🔍 [DOCUMENT_GENERATOR] Tipo de documento: " + request.getTipoDocumento());
        System.out.println("🔍 [DOCUMENT_GENERATOR] Datos del documento: " + request.getDatosDocumento());
        System.out.println("🔍 [DOCUMENT_GENERATOR] Datos de la solicitud: " + request.getDatosSolicitud());
        
        String templatePath = obtenerRutaPlantilla(request.getTipoDocumento());
        System.out.println("🔍 [DOCUMENT_GENERATOR] Ruta de plantilla: " + templatePath);
        
        // Cargar plantilla
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            System.err.println("❌ [DOCUMENT_GENERATOR] La plantilla no existe: " + templatePath);
            throw new IOException("Plantilla no encontrada: " + templatePath);
        }
        
        System.out.println("✅ [DOCUMENT_GENERATOR] Plantilla encontrada, cargando...");
        InputStream templateStream = resource.getInputStream();
        
        // Crear documento desde plantilla
        XWPFDocument document = new XWPFDocument(templateStream);
        System.out.println("✅ [DOCUMENT_GENERATOR] Documento creado desde plantilla");
        
        // Reemplazar placeholders
        System.out.println("🔍 [DOCUMENT_GENERATOR] Reemplazando placeholders...");
        reemplazarPlaceholders(document, request);
        System.out.println("✅ [DOCUMENT_GENERATOR] Placeholders reemplazados");
        
        // Convertir a bytes con codificación UTF-8
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        templateStream.close();
        
        System.out.println("✅ [DOCUMENT_GENERATOR] Documento generado exitosamente");
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
        rutas.put("PAZ_SALVO", "templates/paz-salvo-real.docx");
        rutas.put("RESOLUCION_REINGRESO", "templates/resolucion-reingreso.docx");
        
        return rutas.getOrDefault(tipoDocumento, "templates/documento-generico.docx");
    }

    private void reemplazarPlaceholders(XWPFDocument document, DocumentRequest request) {
        System.out.println("🔍 [DOCUMENT_GENERATOR] Creando mapa de reemplazos...");
        Map<String, String> replacements = crearMapaReemplazos(request);
        System.out.println("✅ [DOCUMENT_GENERATOR] Mapa de reemplazos creado: " + replacements.size() + " elementos");
        
        // Reemplazar en párrafos
        System.out.println("🔍 [DOCUMENT_GENERATOR] Reemplazando en párrafos...");
        int paragraphCount = 0;
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            paragraphCount++;
            reemplazarEnParrafo(paragraph, replacements);
        }
        System.out.println("✅ [DOCUMENT_GENERATOR] Reemplazos en párrafos completados: " + paragraphCount + " párrafos");
        
        // Reemplazar en tablas
        System.out.println("🔍 [DOCUMENT_GENERATOR] Reemplazando en tablas...");
        int tableCount = 0;
        for (XWPFTable table : document.getTables()) {
            tableCount++;
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        reemplazarEnParrafo(paragraph, replacements);
                    }
                }
            }
        }
        System.out.println("✅ [DOCUMENT_GENERATOR] Reemplazos en tablas completados: " + tableCount + " tablas");
    }

    private void reemplazarEnParrafo(XWPFParagraph paragraph, Map<String, String> replacements) {
        String texto = paragraph.getText();
        if (texto == null) {
            texto = "";
        }
        
        // Verificar si el párrafo contiene placeholders
        boolean tienePlaceholders = false;
        for (String key : replacements.keySet()) {
            if (texto.contains("[" + key + "]")) {
                tienePlaceholders = true;
                break;
            }
        }
        
        if (!tienePlaceholders) {
            return; // No hay placeholders que reemplazar
        }
        
        // Reemplazar placeholders en el texto
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "[" + entry.getKey() + "]";
            if (texto.contains(placeholder)) {
                texto = texto.replace(placeholder, entry.getValue());
            }
        }
        
        // Preservar el formato original del párrafo
        ParagraphAlignment alignment = paragraph.getAlignment();
    boolean isBold = false;
    boolean isItalic = false;
    String fontFamily = "Arial";
    int fontSize = 11;
        
        // Obtener formato del primer run si existe
        if (!paragraph.getRuns().isEmpty()) {
            XWPFRun firstRun = paragraph.getRuns().get(0);
            isBold = firstRun.isBold();
            isItalic = firstRun.isItalic();
            if (firstRun.getFontFamily() != null) {
                fontFamily = firstRun.getFontFamily();
            }
            // Manejar tamaño de fuente de forma segura (puede ser null o -1)
            try {
                Double fs = firstRun.getFontSizeAsDouble();
                if (fs != null && fs.intValue() > 0) {
                    fontSize = fs.intValue();
                } else {
                    int fsInt = firstRun.getFontSize();
                    if (fsInt > 0) {
                        fontSize = fsInt;
                    }
                }
            } catch (Exception ignore) {
                // Mantener fontSize por defecto si hay cualquier problema
            }
        }
        
        // Limpiar todos los runs existentes
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }
        
        // Crear nuevo run con el texto reemplazado y formato preservado
        XWPFRun newRun = paragraph.createRun();
        
        // Usar el texto directamente sin conversión adicional
        // Apache POI maneja UTF-8 internamente
        newRun.setText(texto);
        newRun.setBold(isBold);
        newRun.setItalic(isItalic);
        newRun.setFontFamily(fontFamily);
        newRun.setFontSize(fontSize);
        
        // Restaurar alineación del párrafo
        if (alignment != null) {
            paragraph.setAlignment(alignment);
        }
    }

    private Map<String, String> crearMapaReemplazos(DocumentRequest request) {
        System.out.println("🔍 [DOCUMENT_GENERATOR] Creando mapa de reemplazos...");
        Map<String, String> replacements = new HashMap<>();
        
        // Datos del documento
        Map<String, Object> datosDocumento = request.getDatosDocumento();
        if (datosDocumento == null) {
            System.out.println("⚠️ [DOCUMENT_GENERATOR] datosDocumento es null, usando HashMap vacío");
            datosDocumento = new HashMap<>();
        } else {
            System.out.println("✅ [DOCUMENT_GENERATOR] datosDocumento recibido: " + datosDocumento);
        }
        replacements.put("NUMERO_DOCUMENTO", datosDocumento.getOrDefault("numeroDocumento", "001-2025").toString());
        replacements.put("FECHA_DOCUMENTO", formatearFecha(datosDocumento.get("fechaDocumento")));
        replacements.put("OBSERVACIONES", datosDocumento.getOrDefault("observaciones", "").toString());
        
        // Datos de la solicitud
        Map<String, Object> datosSolicitud = request.getDatosSolicitud();
        if (datosSolicitud == null) {
            System.out.println("⚠️ [DOCUMENT_GENERATOR] datosSolicitud es null, usando HashMap vacío");
            datosSolicitud = new HashMap<>();
        } else {
            System.out.println("✅ [DOCUMENT_GENERATOR] datosSolicitud recibido: " + datosSolicitud);
        }
        replacements.put("NOMBRE_ESTUDIANTE", datosSolicitud.getOrDefault("nombreEstudiante", "Estudiante").toString());
        replacements.put("CODIGO_ESTUDIANTE", datosSolicitud.getOrDefault("codigoEstudiante", "000000000").toString());
        replacements.put("PROGRAMA", datosSolicitud.getOrDefault("programa", "Programa").toString());
        replacements.put("FECHA_SOLICITUD", formatearFecha(datosSolicitud.get("fechaSolicitud")));
        
        // Datos de la universidad (predefinidos)
        replacements.put("NOMBRE_UNIVERSIDAD", "UNIVERSIDAD DEL CAUCA");
        replacements.put("FACULTAD", "FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES");
        replacements.put("CIUDAD", "Popayán");
        replacements.put("FECHA_ACTUAL", formatearFecha(LocalDate.now()));
        
        // Campos específicos según el tipo de documento
        if ("OFICIO_HOMOLOGACION".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "homologación de asignaturas");
            replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE HOMOLOGACIÓN");
            
            // Placeholders específicos para homologación
            replacements.put("CEDULA_ESTUDIANTE", datosSolicitud.getOrDefault("cedulaEstudiante", "No especificada").toString());
            replacements.put("FECHA_CONCEPTO", formatearFecha(LocalDate.now().minusDays(7))); // 7 días antes de la fecha actual
            replacements.put("FECHA_SESION_CONSEJO", formatearFecha(LocalDate.now().minusDays(3))); // 3 días antes
            replacements.put("NUMERO_ACTA_CONSEJO", "001-2025"); // Número de acta fijo por ahora
            replacements.put("PERIODO_ACADEMICO", "Segundo Período Académico de 2025"); // Período fijo por ahora
            
            // Fecha de firma (formateada)
            LocalDate fechaFirma = LocalDate.now();
            replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaFirma));
            replacements.put("DIA_NUMERO", String.valueOf(fechaFirma.getDayOfMonth()));
            replacements.put("MES_FIRMA", formatearMes(fechaFirma.getMonthValue()));
            replacements.put("AÑO_FIRMA", formatearAño(fechaFirma.getYear()));
            
            // Fecha del documento (extraída de FECHA_DOCUMENTO)
            Object fechaDocumento = datosDocumento.get("fechaDocumento");
            if (fechaDocumento != null) {
                LocalDate fechaDoc = parsearFecha(fechaDocumento);
                if (fechaDoc != null) {
                    replacements.put("AÑO_DOCUMENTO", formatearAño(fechaDoc.getYear()));
                    replacements.put("MES_DOCUMENTO", formatearMes(fechaDoc.getMonthValue()));
                    replacements.put("DIA_DOCUMENTO", String.valueOf(fechaDoc.getDayOfMonth()));
                } else {
                    // Si no se puede parsear, usar valores por defecto
                    replacements.put("AÑO_DOCUMENTO", "----");
                    replacements.put("MES_DOCUMENTO", "----");
                    replacements.put("DIA_DOCUMENTO", "----");
                }
            } else {
                // Si no hay fecha del documento, usar valores por defecto
                replacements.put("AÑO_DOCUMENTO", "----");
                replacements.put("MES_DOCUMENTO", "----");
                replacements.put("DIA_DOCUMENTO", "----");
            }
            
            // Programas (no disponibles en los datos, usar placeholders)
            replacements.put("PROGRAMA_DESTINO", "------");
            replacements.put("PROGRAMA_ORIGEN", "------");
            
            // NOTA: Los siguientes campos se dejan como "---- (DESCRIPCIÓN A DEFINIR)" en la plantilla:
            // - ASIGNATURA 1, 2, 3 (tabla de asignaturas)
            // - CÓDIGO 1, 2, 3 (códigos de asignaturas)
            // - ASIGNATURA HOMOLOGAR 1, 2, 3 (asignaturas a homologar)
            // - NOTA 1, 2, 3 (notas de las asignaturas)
        } else if ("PAZ_SALVO".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "paz y salvo académico");
            replacements.put("TITULO_DOCUMENTO", "PAZ Y SALVO ACADÉMICO");
            
            // Placeholders específicos para paz y salvo
            replacements.put("FACULTAD", "FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES");
            replacements.put("NOMBRE_UNIVERSIDAD", "UNIVERSIDAD DEL CAUCA");
            replacements.put("CIUDAD", "Popayán");
            
            // Fecha actual para paz y salvo
            LocalDate fechaActual = LocalDate.now();
            replacements.put("FECHA_ACTUAL", formatearFechaCompleta(fechaActual));
            
            // Fecha de firma (formateada)
            replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaActual));
            replacements.put("DIA_NUMERO", String.valueOf(fechaActual.getDayOfMonth()));
            replacements.put("MES_FIRMA", formatearMes(fechaActual.getMonthValue()));
            replacements.put("AÑO_FIRMA", formatearAño(fechaActual.getYear()));
        } else if ("RESOLUCION_REINGRESO".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "reingreso al programa");
            replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE REINGRESO");
        }
        
        System.out.println("✅ [DOCUMENT_GENERATOR] Mapa de reemplazos creado exitosamente con " + replacements.size() + " elementos");
        System.out.println("🔍 [DOCUMENT_GENERATOR] Elementos del mapa: " + replacements);
        
        return replacements;
    }

    private String formatearFecha(Object fecha) {
        if (fecha == null) return "";
        
        try {
            if (fecha instanceof LocalDate) {
                return ((LocalDate) fecha).format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
            } else if (fecha instanceof String) {
                String fechaStr = fecha.toString();
                // Si viene como string ISO (ej: "2025-09-30T04:57:37.000+00:00")
                if (fechaStr.contains("T")) {
                    LocalDate localDate = LocalDate.parse(fechaStr.substring(0, 10));
                    return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
                } else {
                    // Si viene como string simple (ej: "2025-09-30")
                    LocalDate localDate = LocalDate.parse(fechaStr);
                    return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
                }
            }
        } catch (Exception e) {
            // Si hay error, retornar la fecha como string
            return fecha.toString();
        }
        
        return fecha.toString();
    }
    
    private String formatearFechaCompleta(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
    }
    
    private String formatearMes(int mes) {
        String[] meses = {
            "", "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        };
        return meses[mes];
    }
    
    private String formatearAño(int año) {
        return String.valueOf(año);
    }
    
    private LocalDate parsearFecha(Object fecha) {
        if (fecha == null) return null;
        
        try {
            if (fecha instanceof LocalDate) {
                return (LocalDate) fecha;
            } else if (fecha instanceof String) {
                String fechaStr = fecha.toString();
                // Si viene como string ISO (ej: "2025-09-30T04:57:37.000+00:00")
                if (fechaStr.contains("T")) {
                    return LocalDate.parse(fechaStr.substring(0, 10));
                } else {
                    // Si viene como string simple (ej: "2025-09-30")
                    return LocalDate.parse(fechaStr);
                }
            }
        } catch (Exception e) {
            // Si hay error, retornar null
            return null;
        }
        
        return null;
    }
    
}

