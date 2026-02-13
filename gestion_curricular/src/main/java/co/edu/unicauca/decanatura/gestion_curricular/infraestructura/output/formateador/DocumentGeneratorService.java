package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentTemplate;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentGeneratorService {
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final GestionarSolicitudReingresoCUIntPort solicitudReingresoCU;

    /**
     * Generar documento Word usando plantilla
     */
    public ByteArrayOutputStream generarDocumento(DocumentRequest request) throws IOException {
        String templatePath = obtenerRutaPlantilla(request.getTipoDocumento());
        
        // Cargar plantilla
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            throw new IOException("Plantilla no encontrada: " + templatePath);
        }
        
        InputStream templateStream = resource.getInputStream();
        
        // Crear documento desde plantilla
        XWPFDocument document = new XWPFDocument(templateStream);
        
        // Reemplazar placeholders
        reemplazarPlaceholders(document, request);
        
        // Convertir a bytes con codificación UTF-8
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        templateStream.close();
        
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
        rutas.put("PAZ_SALVO", "templates/oficio-paz-salvo.docx");
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
        
        // Preservar el formato original del párrafo
        ParagraphAlignment alignment = paragraph.getAlignment();
        
        // Preservar formato de cada run individualmente
        List<RunFormat> runFormats = new ArrayList<>();
        for (XWPFRun run : paragraph.getRuns()) {
            RunFormat format = new RunFormat();
            format.isBold = run.isBold();
            format.isItalic = run.isItalic();
            format.fontFamily = run.getFontFamily() != null ? run.getFontFamily() : "Arial";
            try {
                Double fs = run.getFontSizeAsDouble();
                if (fs != null && fs.intValue() > 0) {
                    format.fontSize = fs.intValue();
                } else {
                    format.fontSize = 11;
                }
            } catch (Exception e) {
                format.fontSize = 11;
            }
            format.text = run.getText(0);
            runFormats.add(format);
        }
        
        // Si no hay runs, crear uno por defecto sin negrilla
        if (runFormats.isEmpty()) {
            RunFormat defaultFormat = new RunFormat();
            defaultFormat.isBold = false;
            defaultFormat.isItalic = false;
            defaultFormat.fontFamily = "Arial";
            defaultFormat.fontSize = 11;
            defaultFormat.text = texto;
            runFormats.add(defaultFormat);
        }
        
        // Reemplazar placeholders en el texto completo (usar replaceAll para todas las ocurrencias)
        String textoCompleto = texto;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "[" + entry.getKey() + "]";
            // Escapar caracteres especiales para regex si es necesario, pero como usamos replace simple, está bien
            textoCompleto = textoCompleto.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        
        // Limpiar todos los runs existentes
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }
        
        // Reconstruir el párrafo preservando el formato original
        // Reemplazar solo los placeholders manteniendo el formato del texto alrededor
        if (textoCompleto.equals(texto)) {
            // Si no hubo cambios, mantener formato original
            for (RunFormat format : runFormats) {
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(format.text != null ? format.text : "");
                newRun.setBold(format.isBold);
                newRun.setItalic(format.isItalic);
                newRun.setFontFamily(format.fontFamily);
                newRun.setFontSize(format.fontSize);
            }
        } else {
            // Si hubo cambios, reconstruir preservando formato original
            // Identificar qué placeholders estaban en negrilla y solo aplicar negrilla a esos valores
            Map<String, Boolean> placeholderEnNegrilla = new HashMap<>();
            for (RunFormat format : runFormats) {
                if (format.isBold && format.text != null) {
                    for (String key : replacements.keySet()) {
                        String placeholder = "[" + key + "]";
                        if (format.text.contains(placeholder)) {
                            placeholderEnNegrilla.put(key, true);
                        }
                    }
                }
            }
            
            // Forzar negrilla en campos específicos que siempre deben estar en negrilla
            // Estos son campos clave que deben destacarse en el documento
            placeholderEnNegrilla.put("CODIGO_ESTUDIANTE", true);
            placeholderEnNegrilla.put("CEDULA_ESTUDIANTE", true);
            placeholderEnNegrilla.put("NOMBRE_ESTUDIANTE", true);
            placeholderEnNegrilla.put("TITULO_PROFESIONAL", true);
            placeholderEnNegrilla.put("TITULO_TRABAJO_GRADO", true);
            placeholderEnNegrilla.put("DIRECTOR_TRABAJO_GRADO", true);
            // TEXTO_TRABAJO_GRADO NO debe estar en negrilla, solo las partes individuales (título y director)
            
            // Obtener formato base (del primer run sin negrilla o del primero disponible)
            RunFormat formatoBase = null;
            for (RunFormat format : runFormats) {
                if (!format.isBold) {
                    formatoBase = format;
                    break;
                }
            }
            if (formatoBase == null && !runFormats.isEmpty()) {
                formatoBase = runFormats.get(0);
            }
            if (formatoBase == null) {
                formatoBase = new RunFormat();
                formatoBase.isBold = false;
                formatoBase.isItalic = false;
                formatoBase.fontFamily = "Arial";
                formatoBase.fontSize = 11;
            }
            
            // Usar expresión regular para encontrar todos los placeholders
            // y reconstruir el texto segmento por segmento
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([A-Z_]+)\\]");
            java.util.regex.Matcher matcher = pattern.matcher(texto);
            
            int ultimaPosicionTextoCompleto = 0;
            
            while (matcher.find()) {
                String placeholderCompleto = matcher.group(0); // Ej: [NOMBRE_ESTUDIANTE]
                String key = matcher.group(1); // Ej: NOMBRE_ESTUDIANTE
                int inicioPlaceholder = matcher.start();
                
                // Calcular offset acumulado hasta este placeholder
                int offset = 0;
                java.util.regex.Matcher matcherOffset = pattern.matcher(texto);
                while (matcherOffset.find() && matcherOffset.start() < inicioPlaceholder) {
                    String keyAnterior = matcherOffset.group(1);
                    String valorAnterior = replacements.getOrDefault(keyAnterior, matcherOffset.group(0));
                    offset += valorAnterior.length() - matcherOffset.group(0).length();
                }
                
                // Posición en el texto completo
                int posicionEnCompleto = inicioPlaceholder + offset;
                
                // Agregar texto antes del placeholder (sin negrilla)
                if (posicionEnCompleto > ultimaPosicionTextoCompleto) {
                    String antes = textoCompleto.substring(ultimaPosicionTextoCompleto, posicionEnCompleto);
                    if (!antes.isEmpty()) {
                        XWPFRun run = paragraph.createRun();
                        run.setText(antes);
                        run.setBold(false);
                        run.setItalic(formatoBase.isItalic);
                        run.setFontFamily(formatoBase.fontFamily);
                        run.setFontSize(formatoBase.fontSize);
                    }
                }
                
                // Agregar el valor reemplazado (con negrilla solo si el placeholder original la tenía)
                String valor = replacements.getOrDefault(key, placeholderCompleto);
                
                // Manejo especial para TEXTO_TRABAJO_GRADO: dividir en partes con y sin negrilla
                if ("TEXTO_TRABAJO_GRADO".equals(key)) {
                    // Extraer título y director del texto completo
                    String tituloTrabajo = replacements.getOrDefault("TITULO_TRABAJO_GRADO", "").toString();
                    String directorTrabajo = replacements.getOrDefault("DIRECTOR_TRABAJO_GRADO", "").toString();
                    
                    // Dividir el texto en partes
                    String[] partes = valor.split(java.util.regex.Pattern.quote("\"" + tituloTrabajo + "\""), 2);
                    if (partes.length == 2) {
                        // Parte antes del título (sin negrilla)
                        if (!partes[0].isEmpty()) {
                            XWPFRun runAntes = paragraph.createRun();
                            runAntes.setText(partes[0]);
                            runAntes.setBold(false);
                            runAntes.setItalic(formatoBase.isItalic);
                            runAntes.setFontFamily(formatoBase.fontFamily);
                            runAntes.setFontSize(formatoBase.fontSize);
                        }
                        
                        // Título entre comillas (en negrilla)
                        XWPFRun runTitulo = paragraph.createRun();
                        runTitulo.setText("\"" + tituloTrabajo + "\"");
                        runTitulo.setBold(true);
                        runTitulo.setItalic(formatoBase.isItalic);
                        runTitulo.setFontFamily(formatoBase.fontFamily);
                        runTitulo.setFontSize(formatoBase.fontSize);
                        
                        // Parte después del título
                        String parteDespues = partes[1];
                        String[] partes2 = parteDespues.split(java.util.regex.Pattern.quote(directorTrabajo), 2);
                        if (partes2.length == 2) {
                            // Parte antes del director (sin negrilla)
                            if (!partes2[0].isEmpty()) {
                                XWPFRun runAntesDirector = paragraph.createRun();
                                runAntesDirector.setText(partes2[0]);
                                runAntesDirector.setBold(false);
                                runAntesDirector.setItalic(formatoBase.isItalic);
                                runAntesDirector.setFontFamily(formatoBase.fontFamily);
                                runAntesDirector.setFontSize(formatoBase.fontSize);
                            }
                            
                            // Director (en negrilla)
                            XWPFRun runDirector = paragraph.createRun();
                            runDirector.setText(directorTrabajo);
                            runDirector.setBold(true);
                            runDirector.setItalic(formatoBase.isItalic);
                            runDirector.setFontFamily(formatoBase.fontFamily);
                            runDirector.setFontSize(formatoBase.fontSize);
                            
                            // Parte después del director (sin negrilla)
                            if (!partes2[1].isEmpty()) {
                                XWPFRun runDespues = paragraph.createRun();
                                runDespues.setText(partes2[1]);
                                runDespues.setBold(false);
                                runDespues.setItalic(formatoBase.isItalic);
                                runDespues.setFontFamily(formatoBase.fontFamily);
                                runDespues.setFontSize(formatoBase.fontSize);
                            }
                        } else {
                            // Si no se encuentra el director, poner todo sin negrilla
                            XWPFRun runResto = paragraph.createRun();
                            runResto.setText(parteDespues);
                            runResto.setBold(false);
                            runResto.setItalic(formatoBase.isItalic);
                            runResto.setFontFamily(formatoBase.fontFamily);
                            runResto.setFontSize(formatoBase.fontSize);
                        }
                    } else {
                        // Si no se encuentra el patrón, usar el valor completo sin negrilla
                        XWPFRun runValor = paragraph.createRun();
                        runValor.setText(valor);
                        runValor.setBold(false);
                        runValor.setItalic(formatoBase.isItalic);
                        runValor.setFontFamily(formatoBase.fontFamily);
                        runValor.setFontSize(formatoBase.fontSize);
                    }
                } else {
                    // Para otros placeholders, usar la lógica normal
                    XWPFRun runValor = paragraph.createRun();
                    runValor.setText(valor);
                    runValor.setBold(placeholderEnNegrilla.getOrDefault(key, false));
                    runValor.setItalic(formatoBase.isItalic);
                    runValor.setFontFamily(formatoBase.fontFamily);
                    runValor.setFontSize(formatoBase.fontSize);
                }
                
                ultimaPosicionTextoCompleto = posicionEnCompleto + valor.length();
            }
            
            // Agregar texto restante (sin negrilla)
            if (ultimaPosicionTextoCompleto < textoCompleto.length()) {
                String restante = textoCompleto.substring(ultimaPosicionTextoCompleto);
                if (!restante.isEmpty()) {
                    XWPFRun run = paragraph.createRun();
                    run.setText(restante);
                    run.setBold(false);
                    run.setItalic(formatoBase.isItalic);
                    run.setFontFamily(formatoBase.fontFamily);
                    run.setFontSize(formatoBase.fontSize);
                }
            }
        }
        
        // Restaurar alineación del párrafo
        if (alignment != null) {
            paragraph.setAlignment(alignment);
        }
    }
    
    // Clase auxiliar para preservar formato de runs
    private static class RunFormat {
        boolean isBold;
        boolean isItalic;
        String fontFamily;
        int fontSize;
        String text;
    }
    private Map<String, String> crearMapaReemplazos(DocumentRequest request) {
        Map<String, String> replacements = new HashMap<>();
        
        // Datos del documento
        Map<String, Object> datosDocumento = request.getDatosDocumento();
        if (datosDocumento == null) {
            datosDocumento = new HashMap<>();
        }
        replacements.put("NUMERO_DOCUMENTO", datosDocumento.getOrDefault("numeroDocumento", "001-2025").toString());
        // Formatear fecha del documento correctamente (solo fecha, sin hora)
        Object fechaDoc = datosDocumento.get("fechaDocumento");
        LocalDate fechaDocumentoLocal = null;
        if (fechaDoc != null) {
            fechaDocumentoLocal = parsearFecha(fechaDoc);
        }
        // Si no se pudo parsear, usar la fecha actual
        if (fechaDocumentoLocal == null) {
            fechaDocumentoLocal = LocalDate.now();
        }
        replacements.put("FECHA_DOCUMENTO", formatearFecha(fechaDocumentoLocal));
        replacements.put("OBSERVACIONES", datosDocumento.getOrDefault("observaciones", "").toString());
        
        // Datos de la solicitud
        Map<String, Object> datosSolicitud = request.getDatosSolicitud();
        if (datosSolicitud == null) {
            datosSolicitud = new HashMap<>();
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
            
            // Obtener la solicitud completa desde la base de datos para acceder a los nuevos campos
            SolicitudHomologacion solicitudHomologacion = null;
            try {
                if (request.getIdSolicitud() > 0) {
                    solicitudHomologacion = solicitudHomologacionCU.buscarPorId(request.getIdSolicitud());
                }
            } catch (Exception e) {
                // Si no se puede obtener la solicitud, continuar con valores del mapa
            }
            
            // Cédula del estudiante: obtener desde el usuario asociado a la solicitud
            String cedulaEstudiante = "No especificada";
            if (solicitudHomologacion != null && solicitudHomologacion.getObjUsuario() != null 
                && solicitudHomologacion.getObjUsuario().getCedula() != null) {
                cedulaEstudiante = solicitudHomologacion.getObjUsuario().getCedula();
            } else {
                // Fallback: intentar obtener del mapa de datos
                Object cedulaObj = datosSolicitud.get("cedulaEstudiante");
                if (cedulaObj != null) {
                    cedulaEstudiante = cedulaObj.toString();
                }
            }
            replacements.put("CEDULA_ESTUDIANTE", cedulaEstudiante);
            
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
            Object fechaDocumentoHomologacion = datosDocumento.get("fechaDocumento");
            if (fechaDocumentoHomologacion != null) {
                LocalDate fechaDocHomologacion = parsearFecha(fechaDocumentoHomologacion);
                if (fechaDocHomologacion != null) {
                    replacements.put("AÑO_DOCUMENTO", formatearAño(fechaDocHomologacion.getYear()));
                    replacements.put("MES_DOCUMENTO", formatearMes(fechaDocHomologacion.getMonthValue()));
                    replacements.put("DIA_DOCUMENTO", String.valueOf(fechaDocHomologacion.getDayOfMonth()));
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
            
            // Programas de origen y destino: obtener desde la solicitud o del mapa de datos
            String programaOrigen = "------";
            String programaDestino = "------";
            
            if (solicitudHomologacion != null) {
                if (solicitudHomologacion.getPrograma_origen() != null && !solicitudHomologacion.getPrograma_origen().trim().isEmpty()) {
                    programaOrigen = solicitudHomologacion.getPrograma_origen();
                }
                if (solicitudHomologacion.getPrograma_destino() != null && !solicitudHomologacion.getPrograma_destino().trim().isEmpty()) {
                    programaDestino = solicitudHomologacion.getPrograma_destino();
                }
            }
            
            // Fallback: intentar obtener del mapa de datos si no están en la solicitud
            if ("------".equals(programaOrigen)) {
                Object programaOrigenObj = datosSolicitud.get("programaOrigen");
                if (programaOrigenObj != null && !programaOrigenObj.toString().trim().isEmpty()) {
                    programaOrigen = programaOrigenObj.toString();
                }
            }
            if ("------".equals(programaDestino)) {
                Object programaDestinoObj = datosSolicitud.get("programaDestino");
                if (programaDestinoObj != null && !programaDestinoObj.toString().trim().isEmpty()) {
                    programaDestino = programaDestinoObj.toString();
                }
            }
            
            replacements.put("PROGRAMA_DESTINO", programaDestino);
            replacements.put("PROGRAMA_ORIGEN", programaOrigen);
            
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
            
            // Cédula del estudiante
            replacements.put("CEDULA_ESTUDIANTE", datosSolicitud.getOrDefault("cedulaEstudiante", "No especificada").toString());
            
            // Título de trabajo de grado
            replacements.put("TITULO_TRABAJO_GRADO", datosSolicitud.getOrDefault("tituloTrabajoGrado", "Trabajo de grado").toString());
            
            // Director de trabajo de grado
            replacements.put("DIRECTOR_TRABAJO_GRADO", datosSolicitud.getOrDefault("directorTrabajoGrado", "Director asignado").toString());
            
            // Título profesional (obtenido del programa del estudiante)
            String programa = datosSolicitud.getOrDefault("programa", "Ingeniería Electrónica y Telecomunicaciones").toString();
            // Corregir tildes en "Ingeniería" si falta
            programa = programa.replace("Ingenieria", "Ingeniería");
            programa = programa.replace("ingenieria", "Ingeniería");
            // Verificar si es Telemática (sin importar mayúsculas/minúsculas)
            boolean esTelematica = programa.toLowerCase().contains("telematica") || programa.toLowerCase().contains("telemática");
            // Convertir a mayúsculas para que coincida con el formato del documento original
            // Ej: "Ingeniería de Sistemas" -> "INGENIERO DE SISTEMAS"
            String tituloProfesional = programa.toUpperCase();
            // Ajustar "INGENIERÍA" a "INGENIERO" si es necesario (para títulos profesionales)
            if (tituloProfesional.contains("INGENIERÍA DE SISTEMAS")) {
                tituloProfesional = "INGENIERO DE SISTEMAS";
            } else if (tituloProfesional.contains("INGENIERÍA ELECTRÓNICA")) {
                tituloProfesional = "INGENIERO ELECTRÓNICO Y TELECOMUNICACIONES";
            } else if (tituloProfesional.contains("INGENIERÍA AUTOMÁTICA")) {
                tituloProfesional = "INGENIERO AUTOMÁTICO INDUSTRIAL";
            } else if (esTelematica) {
                // Para Telemática, usar el título específico
                tituloProfesional = "TECNOLOGIA EN TELEMATICA";
            } else if (tituloProfesional.contains("INGENIERÍA")) {
                // Para otros casos, reemplazar "INGENIERÍA" con "INGENIERO"
                tituloProfesional = tituloProfesional.replace("INGENIERÍA", "INGENIERO");
            }
            replacements.put("TITULO_PROFESIONAL", tituloProfesional);
            
            // Texto del trabajo de grado (formato completo)
            // Para Telemática, NO incluir el texto del trabajo de grado
            String textoCompletoTrabajo = "";
            if (!esTelematica) {
                String tituloTrabajo = datosSolicitud.getOrDefault("tituloTrabajoGrado", "Trabajo de grado").toString();
                String directorTrabajo = datosSolicitud.getOrDefault("directorTrabajoGrado", "Director asignado").toString();
                textoCompletoTrabajo = String.format("El trabajo de grado titulado \"%s\", dirigido por %s, ha sido aprobado.", tituloTrabajo, directorTrabajo);
            }
            replacements.put("TEXTO_TRABAJO_GRADO", textoCompletoTrabajo);
            
            // Fecha actual para paz y salvo
            LocalDate fechaActual = LocalDate.now();
            replacements.put("FECHA_ACTUAL", formatearFechaCompleta(fechaActual));
            
            // Fecha de firma (formateada)
            replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaActual));
            replacements.put("DIA_NUMERO", String.valueOf(fechaActual.getDayOfMonth()));
            replacements.put("MES_FIRMA", formatearMes(fechaActual.getMonthValue()));
            replacements.put("AÑO_FIRMA", formatearAño(fechaActual.getYear()));
            
            // Fecha del documento (extraída de FECHA_DOCUMENTO)
            Object fechaDocumentoPazSalvo = datosDocumento.get("fechaDocumento");
            LocalDate fechaDocPazSalvo = null;
            if (fechaDocumentoPazSalvo != null) {
                fechaDocPazSalvo = parsearFecha(fechaDocumentoPazSalvo);
            }
            // Si no se pudo parsear, usar la fecha actual
            if (fechaDocPazSalvo == null) {
                fechaDocPazSalvo = LocalDate.now();
            }
            replacements.put("DIA_DOCUMENTO", String.valueOf(fechaDocPazSalvo.getDayOfMonth()));
            replacements.put("MES_DOCUMENTO", formatearMes(fechaDocPazSalvo.getMonthValue()));
            replacements.put("AÑO_DOCUMENTO", formatearAño(fechaDocPazSalvo.getYear()));
        } else if ("RESOLUCION_REINGRESO".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "reingreso al programa");
            replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE REINGRESO");
            
            // Obtener la solicitud completa desde la base de datos para acceder a los datos reales
            SolicitudReingreso solicitudReingreso = null;
            try {
                if (request.getIdSolicitud() > 0) {
                    solicitudReingreso = solicitudReingresoCU.obtenerSolicitudReingresoPorId(request.getIdSolicitud());
                }
            } catch (Exception e) {
                // Si no se puede obtener la solicitud, continuar con valores del mapa como fallback
                log.warn("No se pudo obtener la solicitud de reingreso con ID: " + request.getIdSolicitud(), e);
            }
            
            // Cédula del estudiante: obtener desde el usuario asociado a la solicitud
            String cedulaEstudiante = "No especificada";
            if (solicitudReingreso != null && solicitudReingreso.getObjUsuario() != null 
                && solicitudReingreso.getObjUsuario().getCedula() != null) {
                cedulaEstudiante = solicitudReingreso.getObjUsuario().getCedula();
            } else {
                // Fallback: intentar obtener del mapa de datos
                Object cedulaObj = datosSolicitud.get("cedulaEstudiante");
                if (cedulaObj != null) {
                    cedulaEstudiante = cedulaObj.toString();
                }
            }
            replacements.put("CEDULA_ESTUDIANTE", cedulaEstudiante);

            // FECHA_CONCEPTO: mismo valor que FECHA_DOCUMENTO (ya calculado al inicio del método)
            replacements.put("FECHA_CONCEPTO", replacements.getOrDefault("FECHA_DOCUMENTO", ""));

            // PERIODO_ACADEMICO: desde la tabla solicitudes (campo periodo_academico de la solicitud de reingreso)
            String periodoAcademico = "";
            if (solicitudReingreso != null && solicitudReingreso.getPeriodo_academico() != null && !solicitudReingreso.getPeriodo_academico().trim().isEmpty()) {
                periodoAcademico = solicitudReingreso.getPeriodo_academico().trim();
            } else {
                Object periodoObj = datosSolicitud.get("periodoAcademico");
                if (periodoObj != null && !periodoObj.toString().trim().isEmpty()) {
                    periodoAcademico = periodoObj.toString().trim();
                }
            }
            replacements.put("PERIODO_ACADEMICO", periodoAcademico);
            
            // Fecha de solicitud: obtener desde fecha_registro_solicitud de la solicitud
            String fechaSolicitud = "";
            LocalDate fechaRegistroLocal = null;
            
            if (solicitudReingreso != null && solicitudReingreso.getFecha_registro_solicitud() != null) {
                // Convertir java.util.Date a LocalDate
                java.util.Date fechaRegistro = solicitudReingreso.getFecha_registro_solicitud();
                fechaRegistroLocal = fechaRegistro.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
                fechaSolicitud = formatearFecha(fechaRegistroLocal);
            } else {
                // Fallback: intentar obtener del mapa de datos
                Object fechaSolicitudObj = datosSolicitud.get("fechaSolicitud");
                if (fechaSolicitudObj != null) {
                    fechaSolicitud = formatearFecha(fechaSolicitudObj);
                }
            }
            replacements.put("FECHA_SOLICITUD", fechaSolicitud);
            
            // Fechas de firma: extraer día, mes y año de fecha_registro_solicitud
            if (fechaRegistroLocal != null) {
                // Día de firma (formato completo: "23 de enero de 2025")
                replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaRegistroLocal));
                
                // Día numérico (solo el número del día)
                replacements.put("DIA_NUMERO", String.valueOf(fechaRegistroLocal.getDayOfMonth()));
                
                // Mes de firma (nombre del mes en español)
                replacements.put("MES_FIRMA", formatearMes(fechaRegistroLocal.getMonthValue()));
                
                // Año de firma (año en formato texto)
                replacements.put("AÑO_FIRMA", formatearAño(fechaRegistroLocal.getYear()));
            } else {
                // Fallback: usar fecha actual si no se puede obtener de la solicitud
                LocalDate fechaActual = LocalDate.now();
                replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaActual));
                replacements.put("DIA_NUMERO", String.valueOf(fechaActual.getDayOfMonth()));
                replacements.put("MES_FIRMA", formatearMes(fechaActual.getMonthValue()));
                replacements.put("AÑO_FIRMA", formatearAño(fechaActual.getYear()));
            }
        }
        
        return replacements;
    }

    private String formatearFecha(Object fecha) {
        if (fecha == null) return "";
        
        try {
            if (fecha instanceof LocalDate) {
                return ((LocalDate) fecha).format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
            } else if (fecha instanceof java.util.Date) {
                // Convertir java.util.Date a LocalDate
                java.util.Date date = (java.util.Date) fecha;
                LocalDate localDate = date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
                return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
            } else if (fecha instanceof String) {
                String fechaStr = fecha.toString();
                // Si viene como string con formato completo de fecha (ej: "Tue Dec 16 2025 00:00:00 GMT-0500")
                if (fechaStr.contains("GMT") || fechaStr.contains("COT") || fechaStr.contains("UTC")) {
                    // Intentar parsear como fecha completa y extraer solo la fecha
                    try {
                        // Formato común: "Tue Dec 16 2025 00:00:00 GMT-0500"
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", java.util.Locale.ENGLISH);
                        java.util.Date date = sdf.parse(fechaStr.substring(0, fechaStr.indexOf("GMT") - 1));
                        LocalDate localDate = date.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
                    } catch (Exception e2) {
                        // Si falla, intentar otros formatos
                    }
                }
                // Si viene como string ISO (ej: "2025-09-30T04:57:37.000+00:00")
                if (fechaStr.contains("T")) {
                    LocalDate localDate = LocalDate.parse(fechaStr.substring(0, 10));
                    return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
                } else {
                    // Si viene como string simple (ej: "2025-09-30")
                    LocalDate localDate = LocalDate.parse(fechaStr);
                    return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
                }
            }
        } catch (Exception e) {
            // Si hay error, intentar parsear como string y extraer solo la fecha
            try {
                String fechaStr = fecha.toString();
                // Si contiene números de fecha, intentar extraerlos
                if (fechaStr.matches(".*\\d{4}.*")) {
                    // Buscar patrón de fecha
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})");
                    java.util.regex.Matcher matcher = pattern.matcher(fechaStr);
                    if (matcher.find()) {
                        int dia = Integer.parseInt(matcher.group(1));
                        int mes = Integer.parseInt(matcher.group(2));
                        int año = Integer.parseInt(matcher.group(3));
                        LocalDate localDate = LocalDate.of(año, mes, dia);
                        return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
                    }
                }
            } catch (Exception e2) {
                // Si todo falla, retornar string vacío o mensaje de error
                return "";
            }
        }
        
        return "";
    }
    
    private String formatearFechaCompleta(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
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
                String fechaStr = fecha.toString().trim();
                // Si viene como string ISO (ej: "2025-09-30T04:57:37.000+00:00")
                if (fechaStr.contains("T")) {
                    // Extraer solo la parte de la fecha (YYYY-MM-DD) antes de la T
                    // Esto evita problemas de zona horaria
                    String fechaParte = fechaStr.substring(0, 10);
                    return LocalDate.parse(fechaParte);
                } else if (fechaStr.contains(" ")) {
                    // Si tiene espacio, tomar solo la parte de la fecha (YYYY-MM-DD)
                    String fechaParte = fechaStr.split(" ")[0];
                    return LocalDate.parse(fechaParte);
                } else {
                    // Si viene como string simple (ej: "2025-09-30" o "2025-12-16")
                    // Parsear directamente sin considerar zona horaria
                    return LocalDate.parse(fechaStr);
                }
            } else if (fecha instanceof java.util.Date) {
                // Convertir java.util.Date a LocalDate usando la zona horaria del sistema
                // IMPORTANTE: Usar la zona horaria del sistema para evitar desfases
                java.util.Date date = (java.util.Date) fecha;
                return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else if (fecha instanceof java.sql.Date) {
                // Convertir java.sql.Date a LocalDate directamente (sin zona horaria)
                return ((java.sql.Date) fecha).toLocalDate();
            }
        } catch (Exception e) {
            // Si hay error, retornar null
            return null;
        }
        
        return null;
    }
    
}

