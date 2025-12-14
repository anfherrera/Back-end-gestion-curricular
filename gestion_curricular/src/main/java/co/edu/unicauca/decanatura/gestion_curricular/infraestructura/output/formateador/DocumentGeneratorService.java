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
        
        // Preservar alineación del párrafo
        ParagraphAlignment alignment = paragraph.getAlignment();
        
        // Obtener formato base del primer run
        boolean isBoldBase = false;
        boolean isItalicBase = false;
        String fontFamily = "Arial";
        int fontSize = 12;
        
        if (!paragraph.getRuns().isEmpty()) {
            XWPFRun firstRun = paragraph.getRuns().get(0);
            isBoldBase = firstRun.isBold();
            isItalicBase = firstRun.isItalic();
            if (firstRun.getFontFamily() != null) {
                fontFamily = firstRun.getFontFamily();
            }
            try {
                Double fs = firstRun.getFontSizeAsDouble();
                if (fs != null && fs.intValue() > 0) {
                    fontSize = fs.intValue();
                }
            } catch (Exception ignore) {
            }
        }
        
        // Recopilar información de formato de cada run y detectar qué runs tienen placeholders en negrilla
        List<RunInfo> runInfos = new ArrayList<>();
        Map<String, Boolean> placeholderEnNegrilla = new HashMap<>();
        
        for (XWPFRun run : paragraph.getRuns()) {
            String runText = run.getText(0);
            if (runText != null) {
                boolean isBold = run.isBold();
                boolean isItalic = run.isItalic();
                String font = run.getFontFamily();
                int size = 12;
                try {
                    Double fs = run.getFontSizeAsDouble();
                    if (fs != null && fs.intValue() > 0) {
                        size = fs.intValue();
                    }
                } catch (Exception ignore) {
                }
                if (font == null) {
                    font = "Arial";
                }
                runInfos.add(new RunInfo(runText, isBold, isItalic, font, size));
                
                // Detectar si algún placeholder en este run está en negrilla
                for (String key : replacements.keySet()) {
                    String placeholder = "[" + key + "]";
                    if (runText.contains(placeholder) && isBold) {
                        placeholderEnNegrilla.put(key, true);
                    }
                }
            }
        }
        
        // Limpiar todos los runs existentes
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }
        
        // Procesar el texto completo del párrafo
        String textoCompleto = texto;
        boolean tieneTextoGrado = textoCompleto.contains("[TEXTO_TRABAJO_GRADO]");
        
        // Reemplazar TODOS los placeholders excepto TEXTO_TRABAJO_GRADO en el texto completo
        // Usar replace para reemplazar todas las ocurrencias (replace reemplaza todas las ocurrencias por defecto)
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (!"TEXTO_TRABAJO_GRADO".equals(entry.getKey())) {
                String placeholder = "[" + entry.getKey() + "]";
                String valor = entry.getValue() != null ? entry.getValue() : "";
                if (textoCompleto.contains(placeholder)) {
                    textoCompleto = textoCompleto.replace(placeholder, valor);
                }
            }
        }
        
        // Si tiene TEXTO_TRABAJO_GRADO, dividir y procesar
        if (tieneTextoGrado && replacements.containsKey("TEXTO_TRABAJO_GRADO")) {
            String textoTrabajoGrado = replacements.get("TEXTO_TRABAJO_GRADO");
            String[] partes = textoCompleto.split("\\[TEXTO_TRABAJO_GRADO\\]", 2);
            
            // Texto antes del placeholder
            if (partes.length > 0 && !partes[0].isEmpty()) {
                insertarTextoConNegrillaSelectiva(paragraph, texto, partes[0], placeholderEnNegrilla, replacements, 
                    isBoldBase, isItalicBase, fontFamily, fontSize);
            }
            
            // Insertar TEXTO_TRABAJO_GRADO formateado con negrilla
            // Crear RunInfo solo con el formato necesario (el campo text no se usa en este método)
            RunInfo formatoBase = new RunInfo("dummy", isBoldBase, isItalicBase, fontFamily, fontSize);
            insertarTextoTrabajoGradoFormateado(paragraph, textoTrabajoGrado, formatoBase);
            
            // Texto después del placeholder
            if (partes.length > 1 && !partes[1].isEmpty()) {
                insertarTextoConNegrillaSelectiva(paragraph, texto, partes[1], placeholderEnNegrilla, replacements, 
                    isBoldBase, isItalicBase, fontFamily, fontSize);
            }
        } else {
            // No tiene TEXTO_TRABAJO_GRADO, procesar texto preservando negrilla en placeholders
            if (!textoCompleto.isEmpty()) {
                // Crear múltiples runs: uno para cada placeholder que estaba en negrilla
                insertarTextoConNegrillaSelectiva(paragraph, texto, textoCompleto, placeholderEnNegrilla, replacements, 
                    isBoldBase, isItalicBase, fontFamily, fontSize);
            }
        }
        
        // Restaurar alineación del párrafo
        if (alignment != null) {
            paragraph.setAlignment(alignment);
        }
    }
    
    /**
     * Inserta texto preservando negrilla solo en los valores que reemplazaron placeholders que estaban en negrilla
     */
    private void insertarTextoConNegrillaSelectiva(XWPFParagraph paragraph, String textoOriginal, String textoReemplazado,
            Map<String, Boolean> placeholderEnNegrilla, Map<String, String> replacements,
            boolean isBoldBase, boolean isItalicBase, String fontFamily, int fontSize) {
        
        // Forzar negrilla para NOMBRE_ESTUDIANTE si existe (siempre debe estar en negrilla)
        if (replacements.containsKey("NOMBRE_ESTUDIANTE") && !placeholderEnNegrilla.containsKey("NOMBRE_ESTUDIANTE")) {
            placeholderEnNegrilla.put("NOMBRE_ESTUDIANTE", true);
        }
        
        // Si no hay placeholders en negrilla, crear un solo run
        if (placeholderEnNegrilla.isEmpty() || !placeholderEnNegrilla.containsValue(true)) {
            XWPFRun run = paragraph.createRun();
            run.setText(textoReemplazado);
            run.setBold(isBoldBase);
            run.setItalic(isItalicBase);
            run.setFontFamily(fontFamily);
            run.setFontSize(fontSize);
            return;
        }
        
        // Encontrar posiciones de los valores reemplazados que deben ir en negrilla
        // Buscar todas las ocurrencias, no solo la primera
        List<int[]> rangosNegrilla = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : placeholderEnNegrilla.entrySet()) {
            if (entry.getValue()) {
                String valor = replacements.get(entry.getKey());
                if (valor != null && !valor.isEmpty()) {
                    // Buscar todas las ocurrencias del valor
                    // Primero intentar búsqueda exacta
                    int inicio = 0;
                    while ((inicio = textoReemplazado.indexOf(valor, inicio)) != -1) {
                        int fin = inicio + valor.length();
                        rangosNegrilla.add(new int[]{inicio, fin});
                        inicio = fin; // Continuar buscando desde el final de esta ocurrencia
                    }
                    
                    // Si no se encontró, intentar búsqueda sin espacios al inicio/final
                    if (rangosNegrilla.isEmpty()) {
                        String valorTrim = valor.trim();
                        if (!valorTrim.equals(valor) && !valorTrim.isEmpty()) {
                            inicio = 0;
                            while ((inicio = textoReemplazado.indexOf(valorTrim, inicio)) != -1) {
                                int fin = inicio + valorTrim.length();
                                rangosNegrilla.add(new int[]{inicio, fin});
                                inicio = fin;
                            }
                        }
                    }
                    
                    // Si aún no se encontró, usar búsqueda case-insensitive para nombres
                    if (rangosNegrilla.isEmpty() && (entry.getKey().contains("NOMBRE") || entry.getKey().contains("NOMBRE_ESTUDIANTE"))) {
                        String textoLower = textoReemplazado.toLowerCase();
                        String valorLower = valor.toLowerCase();
                        inicio = 0;
                        while ((inicio = textoLower.indexOf(valorLower, inicio)) != -1) {
                            // Usar la posición encontrada en el texto original (no en lowercase)
                            int fin = inicio + valor.length();
                            rangosNegrilla.add(new int[]{inicio, fin});
                            inicio = fin;
                        }
                    }
                    
                    // Si aún no se encontró para NOMBRE_ESTUDIANTE, buscar en el texto original antes del reemplazo
                    if (rangosNegrilla.isEmpty() && entry.getKey().equals("NOMBRE_ESTUDIANTE")) {
                        // Buscar el patrón "el (la) estudiante [NOMBRE]" y aplicar negrilla al nombre
                        String patron = "el \\(la\\) estudiante ";
                        int posPatron = textoReemplazado.toLowerCase().indexOf(patron);
                        if (posPatron != -1) {
                            int inicioNombre = posPatron + patron.length();
                            // Buscar hasta la siguiente coma o "identificado"
                            int finNombre = textoReemplazado.indexOf(",", inicioNombre);
                            if (finNombre == -1) {
                                finNombre = textoReemplazado.indexOf(" identificado", inicioNombre);
                            }
                            if (finNombre == -1) {
                                finNombre = textoReemplazado.length();
                            }
                            if (finNombre > inicioNombre) {
                                rangosNegrilla.add(new int[]{inicioNombre, finNombre});
                            }
                        }
                    }
                }
            }
        }
        
        // Ordenar rangos
        rangosNegrilla.sort((a, b) -> Integer.compare(a[0], b[0]));
        
        // Construir runs
        int posicionActual = 0;
        for (int[] rango : rangosNegrilla) {
            // Texto antes del rango (normal)
            if (rango[0] > posicionActual) {
                String textoNormal = textoReemplazado.substring(posicionActual, rango[0]);
                if (!textoNormal.isEmpty()) {
                    XWPFRun runNormal = paragraph.createRun();
                    runNormal.setText(textoNormal);
                    runNormal.setBold(false);
                    runNormal.setItalic(isItalicBase);
                    runNormal.setFontFamily(fontFamily);
                    runNormal.setFontSize(fontSize);
                }
            }
            
            // Texto del rango (en negrilla)
            String textoNegrilla = textoReemplazado.substring(rango[0], rango[1]);
            if (!textoNegrilla.isEmpty()) {
                XWPFRun runNegrilla = paragraph.createRun();
                runNegrilla.setText(textoNegrilla);
                runNegrilla.setBold(true);
                runNegrilla.setItalic(isItalicBase);
                runNegrilla.setFontFamily(fontFamily);
                runNegrilla.setFontSize(fontSize);
            }
            
            posicionActual = rango[1];
        }
        
        // Texto final (normal)
        if (posicionActual < textoReemplazado.length()) {
            String textoFinal = textoReemplazado.substring(posicionActual);
            if (!textoFinal.isEmpty()) {
                XWPFRun runFinal = paragraph.createRun();
                runFinal.setText(textoFinal);
                runFinal.setBold(false);
                runFinal.setItalic(isItalicBase);
                runFinal.setFontFamily(fontFamily);
                runFinal.setFontSize(fontSize);
            }
        }
    }
    
    /**
     * Inserta el texto de trabajo de grado formateado con negrilla en partes específicas
     * Formatea en negrilla: fechas, títulos de trabajo de grado (entre comillas), y nombres de directores
     */
    private void insertarTextoTrabajoGradoFormateado(XWPFParagraph paragraph, String textoTrabajoGrado, RunInfo formatoBase) {
        if (textoTrabajoGrado == null || textoTrabajoGrado.trim().isEmpty()) {
            return;
        }
        
        // Patrones para identificar partes que deben ir en negrilla:
        // 1. Fechas (formato: "día DD de MES de YYYY" o variaciones)
        // 2. Títulos entre comillas dobles
        // 3. Nombres después de "dirigido por el/la"
        
        // Dividir el texto en partes: texto normal y texto en negrilla
        // Usar regex para encontrar fechas, títulos entre comillas, y nombres de directores
        
        String texto = textoTrabajoGrado;
        
        // Buscar títulos entre comillas dobles (van en negrilla)
        java.util.regex.Pattern patronComillas = java.util.regex.Pattern.compile("\"([^\"]+)\"");
        java.util.regex.Matcher matcherComillas = patronComillas.matcher(texto);
        
        // Buscar fechas (formato: "día DD de MES de YYYY")
        java.util.regex.Pattern patronFecha = java.util.regex.Pattern.compile("\\d{1,2}\\s+de\\s+\\w+\\s+de\\s+\\d{4}");
        java.util.regex.Matcher matcherFecha = patronFecha.matcher(texto);
        
        // Buscar nombres después de "dirigido por el/la" o "Ingeniero(a)"
        // Patrón más flexible para capturar nombres con títulos (Esp., Inge., PhD., etc.)
        // Captura desde "Ingeniero(a)" o "dirigido por" hasta el final de la oración o punto
        // Primero buscar el patrón completo "dirigido por el Ingeniero(a) [nombre]"
        java.util.regex.Pattern patronDirectorCompleto = java.util.regex.Pattern.compile("dirigido por el Ingeniero\\(a\\)\\s+([A-ZÁÉÍÓÚÑ][^.]*?)(?:\\.|$)");
        java.util.regex.Matcher matcherDirectorCompleto = patronDirectorCompleto.matcher(texto);
        
        // También buscar solo "Ingeniero(a) [nombre]" (sin "dirigido por")
        java.util.regex.Pattern patronDirectorSimple = java.util.regex.Pattern.compile("Ingeniero\\(a\\)\\s+([A-ZÁÉÍÓÚÑ][^.]*?)(?:\\.|$)");
        java.util.regex.Matcher matcherDirectorSimple = patronDirectorSimple.matcher(texto);
        
        // También buscar "dirigido por el/la [nombre]" (sin "Ingeniero(a)")
        java.util.regex.Pattern patronDirectorSinIngeniero = java.util.regex.Pattern.compile("dirigido por (?:el|la)\\s+([A-ZÁÉÍÓÚÑ][^.]*?)(?:\\.|$)");
        java.util.regex.Matcher matcherDirectorSinIngeniero = patronDirectorSinIngeniero.matcher(texto);
        
        // Crear lista de rangos que deben ir en negrilla
        List<int[]> rangosNegrilla = new ArrayList<>();
        
        // Agregar rangos de títulos entre comillas
        while (matcherComillas.find()) {
            rangosNegrilla.add(new int[]{matcherComillas.start(), matcherComillas.end()});
        }
        
        // Agregar rangos de fechas
        while (matcherFecha.find()) {
            rangosNegrilla.add(new int[]{matcherFecha.start(), matcherFecha.end()});
        }
        
        // Agregar rangos de nombres de directores
        // Primero buscar el patrón completo "dirigido por el Ingeniero(a) [nombre]"
        while (matcherDirectorCompleto.find()) {
            // El grupo 1 contiene el nombre del director
            int inicioNombre = matcherDirectorCompleto.start(1);
            int finNombre = matcherDirectorCompleto.end(1);
            rangosNegrilla.add(new int[]{inicioNombre, finNombre});
        }
        
        // Luego buscar solo "Ingeniero(a) [nombre]" (si no fue capturado por el patrón anterior)
        while (matcherDirectorSimple.find()) {
            int inicioNombre = matcherDirectorSimple.start(1);
            int finNombre = matcherDirectorSimple.end(1);
            // Verificar que este rango no esté ya incluido
            boolean yaIncluido = false;
            for (int[] rango : rangosNegrilla) {
                if (rango[0] <= inicioNombre && rango[1] >= finNombre) {
                    yaIncluido = true;
                    break;
                }
            }
            if (!yaIncluido) {
                rangosNegrilla.add(new int[]{inicioNombre, finNombre});
            }
        }
        
        // Finalmente buscar "dirigido por el/la [nombre]" (si no fue capturado por los patrones anteriores)
        while (matcherDirectorSinIngeniero.find()) {
            int inicioNombre = matcherDirectorSinIngeniero.start(1);
            int finNombre = matcherDirectorSinIngeniero.end(1);
            // Verificar que este rango no esté ya incluido
            boolean yaIncluido = false;
            for (int[] rango : rangosNegrilla) {
                if (rango[0] <= inicioNombre && rango[1] >= finNombre) {
                    yaIncluido = true;
                    break;
                }
            }
            if (!yaIncluido) {
                rangosNegrilla.add(new int[]{inicioNombre, finNombre});
            }
        }
        
        // Ordenar rangos por posición inicial
        rangosNegrilla.sort((a, b) -> Integer.compare(a[0], b[0]));
        
        // Eliminar rangos solapados (mantener el más largo)
        List<int[]> rangosFinales = new ArrayList<>();
        for (int[] rango : rangosNegrilla) {
            boolean solapado = false;
            for (int i = 0; i < rangosFinales.size(); i++) {
                int[] existente = rangosFinales.get(i);
                // Si se solapan, mantener el más largo
                if (rango[0] < existente[1] && rango[1] > existente[0]) {
                    if ((rango[1] - rango[0]) > (existente[1] - existente[0])) {
                        rangosFinales.set(i, rango);
                    }
                    solapado = true;
                    break;
                }
            }
            if (!solapado) {
                rangosFinales.add(rango);
            }
        }
        
        // Si no hay rangos en negrilla, insertar todo el texto normal
        if (rangosFinales.isEmpty()) {
            XWPFRun runCompleto = paragraph.createRun();
            runCompleto.setText(texto);
            runCompleto.setBold(formatoBase.isBold);
            runCompleto.setItalic(formatoBase.isItalic);
            runCompleto.setFontFamily(formatoBase.fontFamily);
            runCompleto.setFontSize(formatoBase.fontSize);
            return;
        }
        
        // Construir el texto con múltiples runs
        int posicionActual = 0;
        for (int[] rango : rangosFinales) {
            // Texto antes del rango (normal)
            if (rango[0] > posicionActual) {
                String textoNormal = texto.substring(posicionActual, rango[0]);
                if (!textoNormal.isEmpty()) {
                    XWPFRun runNormal = paragraph.createRun();
                    runNormal.setText(textoNormal);
                    runNormal.setBold(false);
                    runNormal.setItalic(formatoBase.isItalic);
                    runNormal.setFontFamily(formatoBase.fontFamily);
                    runNormal.setFontSize(formatoBase.fontSize);
                }
            }
            
            // Texto del rango (en negrilla)
            String textoNegrilla = texto.substring(rango[0], rango[1]);
            if (!textoNegrilla.isEmpty()) {
                XWPFRun runNegrilla = paragraph.createRun();
                runNegrilla.setText(textoNegrilla);
                runNegrilla.setBold(true); // En negrilla
                runNegrilla.setItalic(formatoBase.isItalic);
                runNegrilla.setFontFamily(formatoBase.fontFamily);
                runNegrilla.setFontSize(formatoBase.fontSize);
            }
            
            posicionActual = rango[1];
        }
        
        // Texto final después del último rango (normal)
        if (posicionActual < texto.length()) {
            String textoFinal = texto.substring(posicionActual);
            if (!textoFinal.isEmpty()) {
                XWPFRun runFinal = paragraph.createRun();
                runFinal.setText(textoFinal);
                runFinal.setBold(false);
                runFinal.setItalic(formatoBase.isItalic);
                runFinal.setFontFamily(formatoBase.fontFamily);
                runFinal.setFontSize(formatoBase.fontSize);
            }
        }
    }
    
    // Clase auxiliar para almacenar información de formato de runs
    private static class RunInfo {
        @SuppressWarnings("unused")
        String text;
        boolean isBold;
        boolean isItalic;
        String fontFamily;
        int fontSize;
        
        RunInfo(String text, boolean isBold, boolean isItalic, String fontFamily, int fontSize) {
            this.text = text;
            this.isBold = isBold;
            this.isItalic = isItalic;
            this.fontFamily = fontFamily;
            this.fontSize = fontSize;
        }
    }

    private Map<String, String> crearMapaReemplazos(DocumentRequest request) {
        Map<String, String> replacements = new HashMap<>();
        
        // Datos del documento
        Map<String, Object> datosDocumento = request.getDatosDocumento();
        if (datosDocumento == null) {
            datosDocumento = new HashMap<>();
        }
        replacements.put("NUMERO_DOCUMENTO", datosDocumento.getOrDefault("numeroDocumento", "001-2025").toString());
        replacements.put("FECHA_DOCUMENTO", formatearFecha(datosDocumento.get("fechaDocumento")));
        replacements.put("OBSERVACIONES", datosDocumento.getOrDefault("observaciones", "").toString());
        
        // Datos de la solicitud
        Map<String, Object> datosSolicitud = request.getDatosSolicitud();
        if (datosSolicitud == null) {
            datosSolicitud = new HashMap<>();
        }
        replacements.put("NOMBRE_ESTUDIANTE", datosSolicitud.getOrDefault("nombreEstudiante", "Estudiante").toString());
        replacements.put("CODIGO_ESTUDIANTE", datosSolicitud.getOrDefault("codigoEstudiante", "000000000").toString());
        
        // Obtener el nombre del programa y mapearlo al título profesional
        String nombrePrograma = datosSolicitud.getOrDefault("programa", "Programa").toString();
        String tituloProfesional = obtenerTituloProfesional(nombrePrograma);
        replacements.put("PROGRAMA", nombrePrograma);
        replacements.put("TITULO_PROFESIONAL", tituloProfesional);
        
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
            
            // Cédula del estudiante
            replacements.put("CEDULA_ESTUDIANTE", datosSolicitud.getOrDefault("cedulaEstudiante", "No especificada").toString());
            
            // Título de trabajo de grado
            replacements.put("TITULO_TRABAJO_GRADO", datosSolicitud.getOrDefault("tituloTrabajoGrado", "Trabajo de grado").toString());
            
            // Director de trabajo de grado
            replacements.put("DIRECTOR_TRABAJO_GRADO", datosSolicitud.getOrDefault("directorTrabajoGrado", "Director asignado").toString());
            
            // Actualizar el título profesional para Paz y Salvo (ya está calculado arriba, pero lo actualizamos si el programa cambió)
            // El título profesional ya fue calculado en la línea 220 y agregado a replacements en la línea 221
            // Solo actualizamos si el programa específico de Paz y Salvo es diferente
            String programaPazSalvo = datosSolicitud.getOrDefault("programa", nombrePrograma).toString();
            if (!programaPazSalvo.equals(nombrePrograma)) {
                String tituloPazSalvo = obtenerTituloProfesional(programaPazSalvo);
                replacements.put("PROGRAMA", programaPazSalvo);
                replacements.put("TITULO_PROFESIONAL", tituloPazSalvo);
            }
            
            // Texto del trabajo de grado (solo para programas que requieren trabajo de grado)
            // Para Telemática, este texto estará vacío
            String textoTrabajoGrado = "";
            boolean esTelematica = programaPazSalvo.toLowerCase().contains("telematica") || 
                                   programaPazSalvo.toLowerCase().contains("telemática");
            
            if (!esTelematica) {
                // Solo incluir la línea del trabajo de grado si NO es Telemática
                String tituloTG = datosSolicitud.getOrDefault("tituloTrabajoGrado", "").toString();
                String directorTG = datosSolicitud.getOrDefault("directorTrabajoGrado", "").toString();
                
                if (!tituloTG.trim().isEmpty() && !directorTG.trim().isEmpty()) {
                    textoTrabajoGrado = String.format(
                        "El estudiante terminó el plan de estudios el día %s, sustentó el trabajo de grado denominado \"%s\", dirigido por el Ingeniero(a) %s.",
                        formatearFecha(datosSolicitud.get("fechaSolicitud")),
                        tituloTG,
                        directorTG
                    );
                } else if (!tituloTG.trim().isEmpty()) {
                    // Si solo tiene título pero no director
                    textoTrabajoGrado = String.format(
                        "El estudiante terminó el plan de estudios el día %s, sustentó el trabajo de grado denominado \"%s\".",
                        formatearFecha(datosSolicitud.get("fechaSolicitud")),
                        tituloTG
                    );
                } else {
                    // Si no tiene información del trabajo de grado
                    textoTrabajoGrado = String.format(
                        "El estudiante terminó el plan de estudios el día %s.",
                        formatearFecha(datosSolicitud.get("fechaSolicitud"))
                    );
                }
            } else {
                // Para Telemática, solo mencionar que terminó el plan de estudios
                textoTrabajoGrado = String.format(
                    "El estudiante terminó el plan de estudios el día %s.",
                    formatearFecha(datosSolicitud.get("fechaSolicitud"))
                );
            }
            
            replacements.put("TEXTO_TRABAJO_GRADO", textoTrabajoGrado);
            
            // Fecha actual para paz y salvo
            LocalDate fechaActual = LocalDate.now();
            replacements.put("FECHA_ACTUAL", formatearFechaCompleta(fechaActual));
            
            // Fecha de firma (formateada)
            replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaActual));
            replacements.put("DIA_NUMERO", String.valueOf(fechaActual.getDayOfMonth()));
            replacements.put("MES_FIRMA", formatearMes(fechaActual.getMonthValue()));
            replacements.put("AÑO_FIRMA", formatearAño(fechaActual.getYear()));
            
            // Fecha del documento (extraída de FECHA_DOCUMENTO)
            Object fechaDocumento = datosDocumento.get("fechaDocumento");
            LocalDate fechaDoc = null;
            
            if (fechaDocumento != null) {
                fechaDoc = parsearFecha(fechaDocumento);
            }
            
            // Si no se pudo parsear, intentar usar la fecha actual como fallback
            if (fechaDoc == null) {
                fechaDoc = LocalDate.now();
            }
            
            // Siempre establecer DIA_DOCUMENTO y MES_DOCUMENTO (nunca vacíos)
            replacements.put("DIA_DOCUMENTO", String.valueOf(fechaDoc.getDayOfMonth()));
            replacements.put("MES_DOCUMENTO", formatearMes(fechaDoc.getMonthValue()));
            replacements.put("AÑO_DOCUMENTO", formatearAño(fechaDoc.getYear()));
        } else if ("RESOLUCION_REINGRESO".equals(request.getTipoDocumento())) {
            replacements.put("TIPO_PROCESO", "reingreso al programa");
            replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE REINGRESO");
        }
        
        return replacements;
    }

    private String formatearFecha(Object fecha) {
        if (fecha == null) return "";
        
        try {
            LocalDate localDate = null;
            
            if (fecha instanceof LocalDate) {
                localDate = (LocalDate) fecha;
            } else if (fecha instanceof java.util.Date) {
                // Convertir Date a LocalDate
                java.util.Date date = (java.util.Date) fecha;
                localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else if (fecha instanceof java.sql.Date) {
                // Convertir java.sql.Date a LocalDate
                localDate = ((java.sql.Date) fecha).toLocalDate();
            } else if (fecha instanceof java.sql.Timestamp) {
                // Convertir Timestamp a LocalDate
                java.sql.Timestamp timestamp = (java.sql.Timestamp) fecha;
                localDate = timestamp.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else if (fecha instanceof String) {
                String fechaStr = fecha.toString().trim();
                
                // Verificar si es formato con timezone como "Sun Dec 14 2025 00:00:00 GMT-0500 (hora estándar de Colombia)"
                if (fechaStr.contains("GMT") || fechaStr.contains("hora estándar") || fechaStr.matches(".*\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}.*")) {
                    try {
                        // Intentar parsear formato completo con timezone
                        // Formato: "Sun Dec 14 2025 00:00:00 GMT-0500 (hora estándar de Colombia)"
                        // Extraer la parte relevante: "Sun Dec 14 2025"
                        java.util.regex.Pattern patron = java.util.regex.Pattern.compile("([A-Za-z]{3}\\s+[A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{4})");
                        java.util.regex.Matcher matcher = patron.matcher(fechaStr);
                        if (matcher.find()) {
                            String fechaParte = matcher.group(1);
                            java.text.SimpleDateFormat formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH);
                            java.util.Date date = formatoIngles.parse(fechaParte);
                            localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        }
                    } catch (Exception ex) {
                        // Si falla, intentar otros formatos
                    }
                }
                
                // Si aún no se parseó, verificar si es formato en inglés simple como "Sun Dec 14" o "Sun Dec 14 2025"
                if (localDate == null && fechaStr.matches("^[A-Za-z]{3}\\s+[A-Za-z]{3}\\s+\\d{1,2}(\\s+\\d{4})?$")) {
                    try {
                        // Intentar parsear formato en inglés
                        java.text.SimpleDateFormat formatoIngles;
                        if (fechaStr.matches("^[A-Za-z]{3}\\s+[A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{4}$")) {
                            formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH);
                        } else {
                            formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd", java.util.Locale.ENGLISH);
                        }
                        java.util.Date date = formatoIngles.parse(fechaStr);
                        localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    } catch (Exception ex) {
                        // Si falla, intentar parsear como ISO
                        if (fechaStr.contains("T") || fechaStr.contains(" ")) {
                            String fechaSolo = fechaStr.substring(0, Math.min(10, fechaStr.length()));
                            if (fechaSolo.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                localDate = LocalDate.parse(fechaSolo);
                            }
                        }
                    }
                }
                
                // Si aún no se parseó, intentar formato ISO
                if (localDate == null && (fechaStr.contains("T") || fechaStr.contains(" "))) {
                    // Si viene como string ISO con hora (ej: "2025-12-14 11:06:13.0" o "2025-12-14T11:06:13.000+00:00")
                    // Extraer solo la parte de la fecha (primeros 10 caracteres)
                    String fechaSolo = fechaStr.substring(0, Math.min(10, fechaStr.length()));
                    if (fechaSolo.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        localDate = LocalDate.parse(fechaSolo);
                    }
                }
                
                // Si aún no se parseó, intentar formato simple
                if (localDate == null && fechaStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    // Si viene como string simple (ej: "2025-09-30")
                    localDate = LocalDate.parse(fechaStr);
                }
            }
            
            if (localDate != null) {
                return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", 
                    new java.util.Locale("es", "CO")));
            }
        } catch (Exception e) {
            // Si hay error, intentar parsear el string de diferentes formas
            String fechaStr = fecha.toString().trim();
            
            // Intentar parsear formato con timezone como "Sun Dec 14 2025 00:00:00 GMT-0500 (hora estándar de Colombia)"
            if (fechaStr.contains("GMT") || fechaStr.contains("hora estándar") || fechaStr.matches(".*\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}.*")) {
                try {
                    java.util.regex.Pattern patron = java.util.regex.Pattern.compile("([A-Za-z]{3}\\s+[A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{4})");
                    java.util.regex.Matcher matcher = patron.matcher(fechaStr);
                    if (matcher.find()) {
                        String fechaParte = matcher.group(1);
                        java.text.SimpleDateFormat formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH);
                        java.util.Date date = formatoIngles.parse(fechaParte);
                        LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", 
                            new java.util.Locale("es", "CO")));
                    }
                } catch (Exception ex) {
                    // Si falla, continuar con otros intentos
                }
            }
            
            // Intentar parsear formato en inglés simple como "Sun Dec 14" o "Sun Dec 14 2025"
            if (fechaStr.matches("^[A-Za-z]{3}\\s+[A-Za-z]{3}\\s+\\d{1,2}(\\s+\\d{4})?$")) {
                try {
                    java.text.SimpleDateFormat formatoIngles;
                    if (fechaStr.matches("^[A-Za-z]{3}\\s+[A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{4}$")) {
                        formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH);
                    } else {
                        formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd", java.util.Locale.ENGLISH);
                    }
                    java.util.Date date = formatoIngles.parse(fechaStr);
                    LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", 
                        new java.util.Locale("es", "CO")));
                } catch (Exception ex) {
                    // Si falla, continuar con otros intentos
                }
            }
            
            // Intentar extraer solo la fecha del string ISO
            if (fechaStr.length() >= 10) {
                try {
                    String fechaSolo = fechaStr.substring(0, 10);
                    if (fechaSolo.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        LocalDate localDate = LocalDate.parse(fechaSolo);
                        return localDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", 
                            new java.util.Locale("es", "CO")));
                    }
                } catch (Exception ex) {
                    // Si falla, retornar solo la parte de la fecha sin hora
                    if (fechaStr.length() >= 10) {
                        return fechaStr.substring(0, 10);
                    }
                }
            }
        }
        
        return fecha.toString();
    }
    
    private String formatearFechaCompleta(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", 
            new java.util.Locale("es", "CO")));
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
                    return LocalDate.parse(fechaStr.substring(0, 10));
                }
                
                // Si viene con hora (ej: "2025-09-30 04:57:37")
                if (fechaStr.contains(" ")) {
                    String[] partes = fechaStr.split(" ");
                    // Verificar si es formato ISO con hora
                    if (partes[0].matches("\\d{4}-\\d{2}-\\d{2}")) {
                        return LocalDate.parse(partes[0]);
                    }
                    // Verificar si es formato en inglés como "Sun Dec 14" o "Sun Dec 14 2025"
                    if (partes.length >= 3 && partes[0].matches("[A-Za-z]{3}") && partes[1].matches("[A-Za-z]{3}")) {
                        try {
                            // Intentar parsear formato en inglés: "Sun Dec 14 2025"
                            if (partes.length >= 4 && partes[3].matches("\\d{4}")) {
                                java.text.SimpleDateFormat formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH);
                                java.util.Date date = formatoIngles.parse(fechaStr);
                                return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            } else {
                                // Intentar sin año: "Sun Dec 14" (usar año actual)
                                java.text.SimpleDateFormat formatoInglesSinAño = new java.text.SimpleDateFormat("EEE MMM dd", java.util.Locale.ENGLISH);
                                java.util.Date date = formatoInglesSinAño.parse(fechaStr);
                                return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            }
                        } catch (Exception ex) {
                            // Si falla, intentar extraer solo la parte de la fecha ISO si existe
                            if (fechaStr.length() >= 10 && fechaStr.substring(0, 10).matches("\\d{4}-\\d{2}-\\d{2}")) {
                                return LocalDate.parse(fechaStr.substring(0, 10));
                            }
                        }
                    }
                }
                
                // Si viene como string simple (ej: "2025-09-30")
                if (fechaStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    return LocalDate.parse(fechaStr);
                }
                
                // Intentar parsear formato en inglés sin espacios de hora (ej: "SunDec14")
                // Esto es menos común, pero lo intentamos
                try {
                    java.text.SimpleDateFormat formatoIngles = new java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH);
                    java.util.Date date = formatoIngles.parse(fechaStr);
                    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                } catch (Exception ex) {
                    try {
                        java.text.SimpleDateFormat formatoInglesSinAño = new java.text.SimpleDateFormat("EEE MMM dd", java.util.Locale.ENGLISH);
                        java.util.Date date = formatoInglesSinAño.parse(fechaStr);
                        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    } catch (Exception ex2) {
                        // Si falla, retornar null
                    }
                }
            } else if (fecha instanceof java.util.Date) {
                return ((java.util.Date) fecha).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            } else if (fecha instanceof java.sql.Date) {
                return ((java.sql.Date) fecha).toLocalDate();
            } else if (fecha instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) fecha).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            }
        } catch (Exception e) {
            // Si falla, retornar null
        }
        
        return null;
    }
    
    /**
     * Obtiene el título profesional correspondiente al nombre del programa académico
     * Mapea los nombres de programas a sus títulos profesionales oficiales
     * 
     * @param nombrePrograma Nombre del programa académico
     * @return Título profesional correspondiente
     */
    private String obtenerTituloProfesional(String nombrePrograma) {
        if (nombrePrograma == null || nombrePrograma.trim().isEmpty()) {
            return "Ingeniero en Electrónica y Telecomunicaciones"; // Valor por defecto
        }
        
        String programaNormalizado = nombrePrograma.trim();
        
        // Mapeo de programas a títulos profesionales
        if (programaNormalizado.equalsIgnoreCase("Ingenieria de Sistemas") || 
            programaNormalizado.equalsIgnoreCase("Ingeniería de Sistemas")) {
            return "Ingeniero de Sistemas";
        } else if (programaNormalizado.equalsIgnoreCase("Ingenieria Electronica y Telecomunicaciones") ||
                   programaNormalizado.equalsIgnoreCase("Ingeniería Electrónica y Telecomunicaciones") ||
                   programaNormalizado.equalsIgnoreCase("Ingenieria en Electronica y Telecomunicaciones") ||
                   programaNormalizado.equalsIgnoreCase("Ingeniería en Electrónica y Telecomunicaciones")) {
            return "Ingeniero en Electrónica y Telecomunicaciones";
        } else if (programaNormalizado.equalsIgnoreCase("Ingenieria Automatica Industrial") ||
                   programaNormalizado.equalsIgnoreCase("Ingeniería Automática Industrial") ||
                   programaNormalizado.equalsIgnoreCase("Ingenieria en Automatica Industrial") ||
                   programaNormalizado.equalsIgnoreCase("Ingeniería en Automática Industrial")) {
            return "Ingeniero en Automática Industrial";
        } else if (programaNormalizado.equalsIgnoreCase("Tecnologia en Telematica") ||
                   programaNormalizado.equalsIgnoreCase("Tecnología en Telemática")) {
            return "Tecnólogo en Telemática";
        }
        
        // Si no coincide con ningún programa conocido, retornar el nombre del programa con "Ingeniero" por defecto
        // o el nombre original si ya contiene "Tecnólogo"
        if (programaNormalizado.toLowerCase().contains("tecnolog")) {
            return programaNormalizado; // Mantener el nombre original si es tecnología
        }
        
        // Por defecto, agregar "Ingeniero" al inicio si no está presente
        if (!programaNormalizado.toLowerCase().startsWith("ingeniero") && 
            !programaNormalizado.toLowerCase().startsWith("ingeniería")) {
            return "Ingeniero " + programaNormalizado;
        }
        
        return programaNormalizado;
    }
    
}

