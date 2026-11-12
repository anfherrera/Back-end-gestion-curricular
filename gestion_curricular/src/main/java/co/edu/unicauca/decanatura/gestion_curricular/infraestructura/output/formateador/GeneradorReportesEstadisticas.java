package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Genera los reportes PDF y Excel de estadísticas (dashboard general, cursos de verano y reporte completo)
 * a partir de los datos que entrega el caso de uso de estadísticas.
 */
@Component
public class GeneradorReportesEstadisticas {

    /** Agrega el contenido propio de cada reporte a un documento PDF ya abierto. */
    @FunctionalInterface
    private interface ContenidoPdf {
        void agregar(Document document) throws Exception;
    }

    /** PDF del dashboard general. */
    public byte[] pdfGeneral(Map<String, Object> estadisticas) {
        return generarPdf("DASHBOARD GENERAL - ESTADÍSTICAS DEL SISTEMA", document -> {
            agregarFecha(document, 15);
            agregarSeccionEstadisticasGenerales(document, estadisticas);
        });
    }

    /** PDF del dashboard de cursos de verano, con los filtros aplicados si los hay. */
    public byte[] pdfCursosVerano(Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
        return generarPdf("DASHBOARD CURSOS DE VERANO - ANÁLISIS DE DEMANDA", document -> {
            agregarFecha(document, 15);
            if (periodoAcademico != null || idPrograma != null) {
                StringBuilder filtros = new StringBuilder("Filtros aplicados: ");
                if (periodoAcademico != null) {
                    filtros.append("Período Académico: ").append(periodoAcademico);
                }
                if (idPrograma != null) {
                    if (periodoAcademico != null) {
                        filtros.append(" | ");
                    }
                    filtros.append("Programa ID: ").append(idPrograma);
                }
                agregarParrafo(document, filtros.toString(), new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC), 10);
            }
            agregarSeccionCursosVerano(document, datosCursosVerano);
        });
    }

    /** PDF completo: estadísticas generales y, en otra página, cursos de verano (cada parte si viene). */
    public byte[] pdfCompleto(Map<String, Object> estadisticas, Map<String, Object> datosCursosVerano,
            String periodoAcademico, Integer idPrograma) {
        return generarPdf("REPORTE GENERAL DE ESTADÍSTICAS", document -> {
            Font normal = new Font(Font.FontFamily.HELVETICA, 10);
            agregarFecha(document, 10);
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                agregarParrafo(document, "Período académico filtrado: " + formatearPeriodoAcademicoLegible(periodoAcademico), normal, 10);
            }
            if (idPrograma != null) {
                String programa = nombreProgramaFiltrado(estadisticas);
                agregarParrafo(document, "Programa filtrado: " + (programa != null ? programa : "ID " + idPrograma), normal, 15);
            }
            if (periodoAcademico == null && idPrograma == null) {
                agregarParrafo(document, " ", normal, 15);
            }
            if (estadisticas != null) {
                agregarSeccionEstadisticasGenerales(document, estadisticas);
            }
            if (datosCursosVerano != null) {
                document.newPage();
                agregarSeccionCursosVerano(document, datosCursosVerano);
            }
        });
    }

    /**
     * Excel con una hoja de estadísticas generales y otra de cursos de verano; cada hoja se incluye solo
     * si vienen sus datos (el dashboard general manda solo la primera, el de cursos de verano solo la segunda).
     */
    public byte[] excel(Map<String, Object> estadisticas, Map<String, Object> datosCursosVerano,
            String periodoAcademico, Integer idPrograma) {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (estadisticas != null) {
                crearHojaEstadisticasGenerales(workbook, estadisticas, periodoAcademico, idPrograma);
            }
            if (datosCursosVerano != null) {
                crearHojaCursosVerano(workbook, datosCursosVerano, periodoAcademico, idPrograma);
            }
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar el reporte Excel", e);
        }
    }

    /** Abre un PDF A4, le pone el título centrado, agrega el contenido y devuelve los bytes. */
    private byte[] generarPdf(String titulo, ContenidoPdf contenido) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph title = new Paragraph(titulo, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            contenido.agregar(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar el reporte PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private void agregarFecha(Document document, float espacioDespues) throws Exception {
        SimpleDateFormat formato = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm:ss", new Locale("es", "CO"));
        agregarParrafo(document, "Fecha de generación: " + formato.format(new Date()),
                new Font(Font.FontFamily.HELVETICA, 10), espacioDespues);
    }

    private void agregarParrafo(Document document, String texto, Font fuente, float espacioDespues) throws Exception {
        Paragraph parrafo = new Paragraph(texto, fuente);
        parrafo.setSpacingAfter(espacioDespues);
        document.add(parrafo);
    }

    /** Con filtro de programa, si las estadísticas traen un único programa, ese es el filtrado. */
    private String nombreProgramaFiltrado(Map<String, Object> estadisticas) {
        if (estadisticas != null && estadisticas.get("porPrograma") instanceof Map<?, ?> porPrograma && porPrograma.size() == 1) {
            return String.valueOf(porPrograma.keySet().iterator().next());
        }
        return null;
    }

    /**
     * Formatea un período académico en formato legible para mostrar en reportes.
     * Convierte "2025-1" a "2025 - Primer Semestre" o "2025-2" a "2025 - Segundo Semestre"
     */
    private String formatearPeriodoAcademicoLegible(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) {
            return null;
        }
        
        String periodoLimpio = periodo.trim();
        
        // Si ya está en formato legible, retornarlo
        if (periodoLimpio.contains("Primer") || periodoLimpio.contains("Segundo") || 
            periodoLimpio.contains("primer") || periodoLimpio.contains("segundo")) {
            return periodoLimpio;
        }
        
        // Si está en formato YYYY-P, convertirlo
        if (periodoLimpio.matches("\\d{4}-[12]")) {
            String[] partes = periodoLimpio.split("-");
            String año = partes[0];
            String semestre = partes[1].equals("1") ? "Primer Semestre" : "Segundo Semestre";
            return año + " - " + semestre;
        }
        
        // Si no se puede formatear, retornar el original
        return periodoLimpio;
    }

    /**
     * Agrega la sección de estadísticas generales al PDF.
     */
    private void agregarSeccionEstadisticasGenerales(com.itextpdf.text.Document document, Map<String, Object> estadisticas) throws Exception {
        // Título de sección
        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph sectionTitle = new com.itextpdf.text.Paragraph("1. ESTADÍSTICAS GENERALES", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        // Estadísticas principales
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        
        // Resumen general
        Object totalSolicitudes = estadisticas.get("totalSolicitudes");
        Object totalAprobadas = estadisticas.get("totalAprobadas");
        Object totalRechazadas = estadisticas.get("totalRechazadas");
        Object totalEnProceso = estadisticas.get("totalEnProceso");
        Object porcentajeAprobacion = estadisticas.get("porcentajeAprobacion");
        
        if (totalSolicitudes != null) {
            document.add(new com.itextpdf.text.Paragraph("Total de Solicitudes: " + totalSolicitudes, boldFont));
        }
        
        if (totalAprobadas != null || totalRechazadas != null || totalEnProceso != null) {
            document.add(new com.itextpdf.text.Paragraph("\nResumen de Estados:", boldFont));
            if (totalAprobadas != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Total Aprobadas: " + totalAprobadas, normalFont));
            }
            if (totalRechazadas != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Total Rechazadas: " + totalRechazadas, normalFont));
            }
            if (totalEnProceso != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Total en Proceso: " + totalEnProceso, normalFont));
            }
            if (porcentajeAprobacion != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Porcentaje de Aprobación: " + porcentajeAprobacion + "%", normalFont));
            }
        }
        
        // Por estado (detallado)
        @SuppressWarnings("unchecked")
        Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
        if (porEstado != null && !porEstado.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Estado:", boldFont));
            for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
        
        // Por tipo de proceso
        @SuppressWarnings("unchecked")
        Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticas.get("porTipoProceso");
        if (porTipoProceso != null && !porTipoProceso.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Tipo de Proceso:", boldFont));
            for (Map.Entry<String, Object> entry : porTipoProceso.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
        
        // Por programa
        @SuppressWarnings("unchecked")
        Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
        if (porPrograma != null && !porPrograma.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Programa:", boldFont));
            for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
    }

    /**
     * Agrega la sección de cursos de verano al PDF.
     */
    private void agregarSeccionCursosVerano(com.itextpdf.text.Document document, Map<String, Object> datosCursosVerano) throws Exception {
        // Título de sección
        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph sectionTitle = new com.itextpdf.text.Paragraph("2. ESTADÍSTICAS DE CURSOS DE VERANO", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        
        // Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datosCursosVerano.get("resumen");
        if (resumen != null) {
            document.add(new com.itextpdf.text.Paragraph("Resumen General:", normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Total Solicitudes: " + resumen.get("totalSolicitudes"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Materias Únicas: " + resumen.get("materiasUnicas"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Programas Participantes: " + resumen.get("programasParticipantes"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Tasa de Aprobación: " + resumen.get("tasaAprobacion") + "%", normalFont));
        }
        
        // Top materias
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topMaterias = (List<Map<String, Object>>) datosCursosVerano.get("topMaterias");
        if (topMaterias != null && !topMaterias.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nTop Materias por Demanda:", normalFont));
            for (Map<String, Object> materia : topMaterias) {
                document.add(new com.itextpdf.text.Paragraph("  • " + materia.get("nombre") + ": " + 
                    materia.get("solicitudes") + " solicitudes (" + materia.get("porcentaje") + "%)", normalFont));
            }
        }
        
        // Análisis por programa
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> analisisPorPrograma = (List<Map<String, Object>>) datosCursosVerano.get("analisisPorPrograma");
        if (analisisPorPrograma != null && !analisisPorPrograma.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nAnálisis por Programa:", normalFont));
            for (Map<String, Object> programa : analisisPorPrograma) {
                document.add(new com.itextpdf.text.Paragraph("  • " + programa.get("nombre") + ": " + 
                    programa.get("solicitudes") + " solicitudes (" + programa.get("porcentaje") + "%)", normalFont));
            }
        }
        
        // Predicciones
        @SuppressWarnings("unchecked")
        Map<String, Object> predicciones = (Map<String, Object>) datosCursosVerano.get("predicciones");
        if (predicciones != null) {
            document.add(new com.itextpdf.text.Paragraph("\nPredicciones:", normalFont));
            Object demandaEstimada = predicciones.get("demandaEstimadaProximoPeriodo");
            if (demandaEstimada != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Demanda Estimada Próximo Período: " + demandaEstimada, normalFont));
            }
            Object confiabilidad = predicciones.get("confiabilidad");
            if (confiabilidad != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Confiabilidad: " + confiabilidad, normalFont));
            }
            Object metodologia = predicciones.get("metodologia");
            if (metodologia != null && !"null".equals(String.valueOf(metodologia))) {
                document.add(new com.itextpdf.text.Paragraph("  • Metodología: " + metodologia, normalFont));
            }
        }
        
        // Recomendaciones
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recomendaciones = (List<Map<String, Object>>) datosCursosVerano.get("recomendaciones");
        if (recomendaciones != null && !recomendaciones.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nRecomendaciones:", normalFont));
            for (Map<String, Object> rec : recomendaciones) {
                document.add(new com.itextpdf.text.Paragraph("  • " + rec.get("titulo") + ": " + rec.get("descripcion"), normalFont));
            }
        }
    }

    private void crearHojaEstadisticasGenerales(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, Map<String, Object> estadisticas, String periodoAcademico, Integer idPrograma) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Estadísticas Generales");
            
            // Estilos
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            
            org.apache.poi.ss.usermodel.CellStyle infoStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font infoFont = workbook.createFont();
            infoFont.setItalic(true);
            infoFont.setFontHeightInPoints((short) 10);
            infoStyle.setFont(infoFont);
            
            int rowNum = 0;
            
            // Título
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ESTADÍSTICAS GENERALES DEL SISTEMA");
            titleCell.setCellStyle(titleStyle);
            
        rowNum++; // Espacio
        
        // Fecha de generación
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm:ss", new java.util.Locale("es", "CO"));
        org.apache.poi.ss.usermodel.Row fechaRow = sheet.createRow(rowNum++);
        fechaRow.createCell(0).setCellValue("Fecha de generación: " + dateFormat.format(new Date()));
        fechaRow.getCell(0).setCellStyle(infoStyle);
        
        // Información de filtros aplicados
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            String periodoFormateado = formatearPeriodoAcademicoLegible(periodoAcademico);
            org.apache.poi.ss.usermodel.Row periodoRow = sheet.createRow(rowNum++);
            periodoRow.createCell(0).setCellValue("Período académico filtrado: " + periodoFormateado);
            periodoRow.getCell(0).setCellStyle(infoStyle);
        }
        
        if (idPrograma != null) {
            // Intentar obtener el nombre del programa desde las estadísticas
            String nombrePrograma = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
                if (porPrograma != null && porPrograma.size() == 1) {
                    nombrePrograma = porPrograma.keySet().iterator().next();
                }
            } catch (Exception e) {
            }
            
            org.apache.poi.ss.usermodel.Row programaRow = sheet.createRow(rowNum++);
            String textoPrograma = nombrePrograma != null ? 
                "Programa filtrado: " + nombrePrograma : 
                "Programa filtrado: ID " + idPrograma;
            programaRow.createCell(0).setCellValue(textoPrograma);
            programaRow.getCell(0).setCellStyle(infoStyle);
        }
        
        rowNum++; // Espacio
        
        // Total de solicitudes
        Object totalSolicitudes = estadisticas.get("totalSolicitudes");
        if (totalSolicitudes != null) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Total de Solicitudes");
            row.createCell(1).setCellValue(totalSolicitudes.toString());
        }
        
        // Resumen de estados
        Object totalAprobadas = estadisticas.get("totalAprobadas");
        Object totalRechazadas = estadisticas.get("totalRechazadas");
        Object totalEnProceso = estadisticas.get("totalEnProceso");
        Object totalEnviadas = estadisticas.get("totalEnviadas");
        Object porcentajeAprobacion = estadisticas.get("porcentajeAprobacion");
        
        if (totalAprobadas != null || totalRechazadas != null || totalEnProceso != null || totalEnviadas != null) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("RESUMEN DE ESTADOS");
            headerCell.setCellStyle(headerStyle);
            
            if (totalAprobadas != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total Aprobadas");
                row.createCell(1).setCellValue(totalAprobadas.toString());
            }
            if (totalRechazadas != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total Rechazadas");
                row.createCell(1).setCellValue(totalRechazadas.toString());
            }
            if (totalEnProceso != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total en Proceso");
                row.createCell(1).setCellValue(totalEnProceso.toString());
            }
            if (totalEnviadas != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total Enviadas");
                row.createCell(1).setCellValue(totalEnviadas.toString());
            }
            if (porcentajeAprobacion != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Porcentaje de Aprobación");
                row.createCell(1).setCellValue(porcentajeAprobacion.toString() + "%");
            }
        }
        
        // Por tipo de proceso
        @SuppressWarnings("unchecked")
        Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticas.get("porTipoProceso");
        if (porTipoProceso != null && !porTipoProceso.isEmpty()) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ESTADÍSTICAS POR TIPO DE PROCESO");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : porTipoProceso.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().toString());
            }
            }
            
        // Por estado
            @SuppressWarnings("unchecked")
        Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
        if (porEstado != null && !porEstado.isEmpty()) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ESTADÍSTICAS POR ESTADO");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue().toString());
            }
        }
        
        // Por programa
        @SuppressWarnings("unchecked")
        Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
        if (porPrograma != null && !porPrograma.isEmpty()) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ESTADÍSTICAS POR PROGRAMA");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().toString());
            }
        }
        
        // Ajustar ancho de columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Crea la hoja de cursos de verano en el Excel.
     */
    private void crearHojaCursosVerano(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Cursos de Verano");
        
        // Estilos
        org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        org.apache.poi.ss.usermodel.CellStyle filterStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font filterFont = workbook.createFont();
        filterFont.setItalic(true);
        filterFont.setFontHeightInPoints((short) 10);
        filterStyle.setFont(filterFont);
        
        int rowNum = 0;
        
        // Título
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ESTADÍSTICAS DE CURSOS DE VERANO");
        titleCell.setCellStyle(titleStyle);
        
        // Fecha de generación
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        org.apache.poi.ss.usermodel.Row fechaRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha de generación: " + dateFormat.format(new Date()));
        
        // Información de filtros aplicados (si existen)
        if (periodoAcademico != null || idPrograma != null) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row filterRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell filterCell = filterRow.createCell(0);
            StringBuilder filtrosTexto = new StringBuilder("Filtros aplicados: ");
            if (periodoAcademico != null) {
                filtrosTexto.append("Período Académico: ").append(periodoAcademico);
            }
            if (idPrograma != null) {
                if (periodoAcademico != null) {
                    filtrosTexto.append(" | ");
                }
                filtrosTexto.append("Programa ID: ").append(idPrograma);
            }
            filterCell.setCellValue(filtrosTexto.toString());
            filterCell.setCellStyle(filterStyle);
        }
        
        rowNum++; // Espacio
        
        // Resumen
            @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datosCursosVerano.get("resumen");
        if (resumen != null) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("RESUMEN GENERAL");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : resumen.entrySet()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
            rowNum++; // Espacio
        }
        
        // Top materias
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topMaterias = (List<Map<String, Object>>) datosCursosVerano.get("topMaterias");
        if (topMaterias != null && !topMaterias.isEmpty()) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("TOP MATERIAS POR DEMANDA");
            headerCell.setCellStyle(headerStyle);
            
            for (Map<String, Object> materia : topMaterias) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(materia.get("nombre").toString());
                row.createCell(1).setCellValue(materia.get("solicitudes").toString());
                row.createCell(2).setCellValue(materia.get("porcentaje").toString() + "%");
            }
            rowNum++; // Espacio
        }
        
        // Análisis por programa
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> analisisPorPrograma = (List<Map<String, Object>>) datosCursosVerano.get("analisisPorPrograma");
        if (analisisPorPrograma != null && !analisisPorPrograma.isEmpty()) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ANÁLISIS POR PROGRAMA");
            headerCell.setCellStyle(headerStyle);
            
            for (Map<String, Object> programa : analisisPorPrograma) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(programa.get("nombre").toString());
                row.createCell(1).setCellValue(programa.get("solicitudes").toString());
                row.createCell(2).setCellValue(programa.get("porcentaje").toString() + "%");
            }
            }
            
            // Ajustar ancho de columnas
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

}
