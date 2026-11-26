package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarDocumentosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatosDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatosGeneracionDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PlantillaDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

@Service("documentGeneratorService")
@RequiredArgsConstructor
public class ServicioGeneracionDocumentos implements GenerarDocumentosIntPort {

    public byte[] generarPazYSalvo(String numero, String fecha, String observaciones,
                                                  DatosDocumento datos) throws IOException {
        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("nombreEstudiante", datos.nombreEstudiante());
        solicitud.put("codigoEstudiante", datos.codigoEstudiante());
        solicitud.put("programa", datos.programa());
        solicitud.put("fechaSolicitud", datos.fechaSolicitud());
        solicitud.put("cedulaEstudiante", datos.cedula());
        solicitud.put("tituloTrabajoGrado", datos.titulo());
        solicitud.put("directorTrabajoGrado", datos.director());
        return generarDocumento(new DatosGeneracionDocumento(0, "PAZ_SALVO",
                Map.of("numeroDocumento", numero, "fechaDocumento", fecha, "observaciones", observaciones),
                solicitud, null, null));
    }

    /**
     * Generar documento Word usando plantilla
     */
    public byte[] generarDocumento(DatosGeneracionDocumento request) throws IOException {
        String templatePath = obtenerRutaPlantilla(request.getTipoDocumento());
        
        // Cargar plantilla
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            throw new IOException("Plantilla no encontrada: " + templatePath);
        }
        
        try (InputStream templateStream = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(templateStream)) {
            reemplazarPlaceholders(document, request);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Obtener plantillas disponibles por proceso
     */
    public List<PlantillaDocumento> getTemplates(String proceso) {
        Map<String, List<PlantillaDocumento>> templates = new HashMap<>();
        
        // Plantilla de homologación
        templates.put("homologacion", Arrays.asList(
            new PlantillaDocumento(
                "OFICIO_HOMOLOGACION",
                "Oficio de Homologación",
                "Documento oficial que aprueba la homologación de asignaturas",
                Arrays.asList("numeroDocumento", "fechaDocumento"),
                Arrays.asList("observaciones")
            )
        ));
        
        // Plantilla de paz y salvo
        templates.put("paz-salvo", Arrays.asList(
            new PlantillaDocumento(
                "PAZ_SALVO",
                "Paz y Salvo",
                "Documento que certifica que el estudiante no tiene pendientes académicos",
                Arrays.asList("numeroDocumento", "fechaDocumento"),
                Arrays.asList("observaciones", "semestre")
            )
        ));
        
        // Plantilla de reingreso
        templates.put("reingreso", Arrays.asList(
            new PlantillaDocumento(
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

    private void reemplazarPlaceholders(XWPFDocument document, DatosGeneracionDocumento request) {
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

    /** Campos que siempre van en negrilla, aunque en la plantilla no lo estén. */
    private static final Set<String> CAMPOS_EN_NEGRILLA = Set.of("CODIGO_ESTUDIANTE", "CEDULA_ESTUDIANTE",
            "NOMBRE_ESTUDIANTE", "TITULO_PROFESIONAL", "TITULO_TRABAJO_GRADO", "DIRECTOR_TRABAJO_GRADO");

    /** Un trozo de texto que va en su propio run, con o sin negrilla forzada. */
    private record Trozo(String texto, boolean negrilla) {}

    /**
     * Reemplaza los campos [CLAVE] del párrafo sin tocar el resto: cada valor queda en el run donde estaba el
     * campo y hereda su formato (letra, tamaño, negrilla, color). Un campo partido en varios runs se junta en el
     * primero. Los campos de {@link #CAMPOS_EN_NEGRILLA} y las partes destacadas de TEXTO_TRABAJO_GRADO van en
     * runs propios con negrilla; el texto que los rodea conserva el formato de la plantilla.
     */
    private void reemplazarEnParrafo(XWPFParagraph paragraph, Map<String, String> replacements) {
        int desde = 0;
        while (true) {
            String texto = textoDeRuns(paragraph);
            String clave = null;
            int inicio = -1;
            for (String candidata : replacements.keySet()) {
                int posicion = texto.indexOf("[" + candidata + "]", desde);
                if (posicion >= 0 && (inicio < 0 || posicion < inicio)) {
                    inicio = posicion;
                    clave = candidata;
                }
            }
            if (clave == null) {
                return;
            }
            String valor = replacements.get(clave) != null ? replacements.get(clave) : "";
            reemplazarCampo(paragraph, inicio, ("[" + clave + "]").length(), trozos(clave, valor, replacements));
            desde = inicio + valor.length();
        }
    }

    /** Cómo se escribe el valor de un campo: un solo trozo, o varios con partes en negrilla. */
    private static List<Trozo> trozos(String clave, String valor, Map<String, String> replacements) {
        if ("TEXTO_TRABAJO_GRADO".equals(clave) && !valor.isEmpty()) {
            // Solo el título (entre comillas) y el director van en negrilla
            String titulo = "\"" + replacements.getOrDefault("TITULO_TRABAJO_GRADO", "") + "\"";
            String director = replacements.getOrDefault("DIRECTOR_TRABAJO_GRADO", "");
            int posTitulo = valor.indexOf(titulo);
            int posDirector = posTitulo < 0 || director.isEmpty() ? -1
                    : valor.indexOf(director, posTitulo + titulo.length());
            if (posTitulo >= 0 && posDirector >= 0) {
                return List.of(new Trozo(valor.substring(0, posTitulo), false),
                        new Trozo(titulo, true),
                        new Trozo(valor.substring(posTitulo + titulo.length(), posDirector), false),
                        new Trozo(director, true),
                        new Trozo(valor.substring(posDirector + director.length()), false));
            }
            return List.of(new Trozo(valor, false));
        }
        return List.of(new Trozo(valor, CAMPOS_EN_NEGRILLA.contains(clave)));
    }

    /** Pone los trozos en lugar de los {@code largo} caracteres que empiezan en {@code inicio}. */
    private static void reemplazarCampo(XWPFParagraph paragraph, int inicio, int largo, List<Trozo> trozos) {
        List<XWPFRun> runs = paragraph.getRuns();
        int fin = inicio + largo;
        int acumulado = 0;
        int primero = -1;
        int offsetPrimero = 0;
        for (int i = 0; i < runs.size(); i++) {
            String textoRun = textoDeRun(runs.get(i));
            int finRun = acumulado + textoRun.length();
            if (primero < 0 && inicio < finRun) {
                primero = i;
                offsetPrimero = inicio - acumulado;
            }
            if (primero >= 0) {
                // Lo que el campo ocupa de este run se quita; el primero se queda con el campo entero
                int desdeAqui = Math.max(inicio, acumulado) - acumulado;
                int hastaAqui = Math.min(fin, finRun) - acumulado;
                if (i == primero) {
                    String campo = textoRun.substring(desdeAqui, hastaAqui);
                    // Las partes del campo que siguen en otros runs se agregan aquí
                    int resto = fin - finRun;
                    String despues = textoRun.substring(hastaAqui);
                    if (resto > 0) {
                        StringBuilder completo = new StringBuilder(campo);
                        int a = finRun;
                        for (int j = i + 1; j < runs.size() && a < fin; j++) {
                            String t = textoDeRun(runs.get(j));
                            int toma = Math.min(t.length(), fin - a);
                            completo.append(t, 0, toma);
                            ponerTexto(runs.get(j), t.substring(toma));
                            a += t.length();
                        }
                        campo = completo.toString();
                    }
                    ponerTexto(runs.get(i), textoRun.substring(0, desdeAqui) + campo + despues);
                    break;
                }
            }
            acumulado = finRun;
        }
        if (primero < 0) {
            return;
        }

        // Ahora el campo entero está en el run "primero", desde offsetPrimero
        XWPFRun run = paragraph.getRuns().get(primero);
        String textoRun = textoDeRun(run);
        String antes = textoRun.substring(0, offsetPrimero);
        String despues = textoRun.substring(offsetPrimero + largo);
        boolean hayNegrillaForzada = trozos.stream().anyMatch(Trozo::negrilla);
        if (!hayNegrillaForzada) {
            StringBuilder valor = new StringBuilder();
            trozos.forEach(t -> valor.append(t.texto()));
            ponerTexto(run, antes + valor + despues);
            return;
        }
        // El valor va en runs propios con el formato del run original
        ponerTexto(run, antes);
        int posicion = primero + 1;
        for (Trozo trozo : trozos) {
            if (trozo.texto().isEmpty()) {
                continue;
            }
            XWPFRun nuevo = copiarFormato(paragraph, run, posicion++);
            ponerTexto(nuevo, trozo.texto());
            if (trozo.negrilla()) {
                nuevo.setBold(true);
            }
        }
        if (!despues.isEmpty()) {
            ponerTexto(copiarFormato(paragraph, run, posicion), despues);
        }
    }

    private static XWPFRun copiarFormato(XWPFParagraph paragraph, XWPFRun modelo, int posicion) {
        XWPFRun nuevo = paragraph.insertNewRun(posicion);
        if (modelo.getCTR().isSetRPr()) {
            nuevo.getCTR().setRPr((org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr)
                    modelo.getCTR().getRPr().copy());
        }
        return nuevo;
    }

    /** El texto de los runs del párrafo (solo los w:t, que es lo que se reemplaza). */
    private static String textoDeRuns(XWPFParagraph paragraph) {
        StringBuilder texto = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            texto.append(textoDeRun(run));
        }
        return texto.toString();
    }

    private static String textoDeRun(XWPFRun run) {
        StringBuilder texto = new StringBuilder();
        for (var t : run.getCTR().getTList()) {
            texto.append(t.getStringValue());
        }
        return texto.toString();
    }

    /** Deja el texto del run en un solo w:t, conservando los espacios de los extremos. */
    private static void ponerTexto(XWPFRun run, String texto) {
        while (run.getCTR().sizeOfTArray() > 1) {
            run.getCTR().removeT(run.getCTR().sizeOfTArray() - 1);
        }
        run.setText(texto, 0);
    }

    private Map<String, String> crearMapaReemplazos(DatosGeneracionDocumento request) {
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
            
            SolicitudHomologacion solicitudHomologacion = request.homologacion();
            
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
            replacements.put("DIA_FIRMA", diaEnLetras(fechaFirma.getDayOfMonth()));
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
                if (solicitudHomologacion.getProgramaOrigen() != null && !solicitudHomologacion.getProgramaOrigen().trim().isEmpty()) {
                    programaOrigen = solicitudHomologacion.getProgramaOrigen();
                }
                if (solicitudHomologacion.getProgramaDestino() != null && !solicitudHomologacion.getProgramaDestino().trim().isEmpty()) {
                    programaDestino = solicitudHomologacion.getProgramaDestino();
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
            replacements.put("DIA_FIRMA", diaEnLetras(fechaActual.getDayOfMonth()));
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
            
            SolicitudReingreso solicitudReingreso = request.reingreso();
            
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
            if (solicitudReingreso != null && solicitudReingreso.getPeriodoAcademico() != null && !solicitudReingreso.getPeriodoAcademico().trim().isEmpty()) {
                periodoAcademico = solicitudReingreso.getPeriodoAcademico().trim();
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
            
            if (solicitudReingreso != null && solicitudReingreso.getFechaRegistroSolicitud() != null) {
                // Convertir java.util.Date a LocalDate
                java.util.Date fechaRegistro = solicitudReingreso.getFechaRegistroSolicitud();
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
                // Día de firma en letras: la plantilla dice "a los [DIA_FIRMA] ([DIA_NUMERO]) días"
                replacements.put("DIA_FIRMA", diaEnLetras(fechaRegistroLocal.getDayOfMonth()));
                
                // Día numérico (solo el número del día)
                replacements.put("DIA_NUMERO", String.valueOf(fechaRegistroLocal.getDayOfMonth()));
                
                // Mes de firma (nombre del mes en español)
                replacements.put("MES_FIRMA", formatearMes(fechaRegistroLocal.getMonthValue()));
                
                // Año de firma (año en formato texto)
                replacements.put("AÑO_FIRMA", formatearAño(fechaRegistroLocal.getYear()));
            } else {
                // Fallback: usar fecha actual si no se puede obtener de la solicitud
                LocalDate fechaActual = LocalDate.now();
                replacements.put("DIA_FIRMA", diaEnLetras(fechaActual.getDayOfMonth()));
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
    
    private static final String[] DIAS_EN_LETRAS = {
        "", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte",
        "veintiún", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete",
        "veintiocho", "veintinueve", "treinta", "treinta y un"
    };

    /** El día del mes en letras, como en "a los veintiséis (26) días del mes de...". */
    private static String diaEnLetras(int dia) {
        return DIAS_EN_LETRAS[dia];
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

