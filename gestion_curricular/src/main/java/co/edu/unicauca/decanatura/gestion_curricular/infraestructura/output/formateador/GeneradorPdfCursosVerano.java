package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import java.util.List;
import java.util.Map;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorPdfCursosVerano;
import org.springframework.stereotype.Component;

/** Genera los PDF de cursos de verano (listado de cursos y estudiantes de un curso). */
@Component
@Slf4j
public class GeneradorPdfCursosVerano {

    /**
     * Genera un PDF con los cursos filtrados
     */
    public byte[] generarPDFCursos(List<CursosOfertadosDTORespuesta> cursos, String periodoAcademico, Integer idPrograma) {
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate()); // Horizontal para más columnas
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Título principal
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "GESTIÓN DE CURSOS INTERSEMESTRALES", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información de filtros y fecha
            com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            StringBuilder info = new StringBuilder();
            info.append("Fecha de generación: ").append(formatearFechaEspanol(new Date())).append("\n");
            
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
                info.append("Período Académico: ").append(periodoAcademico).append("\n");
            } else {
                try {
                    PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
                    if (periodoActual != null) {
                        info.append("Período Académico: Todos (Período actual: ").append(periodoActual.getValor()).append(")\n");
                    } else {
                        info.append("Período Académico: Todos los períodos\n");
                    }
                } catch (Exception e) {
                    info.append("Período Académico: Todos los períodos\n");
                }
            }
            
            if (idPrograma != null) {
                info.append("Programa: ID ").append(idPrograma).append("\n");
            }
            
            info.append("Total de cursos: ").append(cursos.size()).append("\n");
            
            com.itextpdf.text.Paragraph infoParagraph = new com.itextpdf.text.Paragraph(info.toString(), infoFont);
            infoParagraph.setSpacingAfter(15);
            document.add(infoParagraph);
            
            // Tabla de cursos
            if (cursos != null && !cursos.isEmpty()) {
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(10);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1f, 2f, 1f, 1.5f, 1f, 1.5f, 2f, 1f, 1f, 1.5f});
                
                // Encabezados
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Código", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Nombre", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Período", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Grupo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Docente", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Fecha Inicio", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Fecha Fin", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cupo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Salón", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado", headerFont)));
                
                // Datos
                com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                
                for (CursosOfertadosDTORespuesta curso : cursos) {
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getCodigo_curso() != null ? curso.getCodigo_curso() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getNombre_curso() != null ? curso.getNombre_curso() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getPeriodo() != null ? curso.getPeriodo() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getGrupo() != null ? curso.getGrupo() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getObjDocente() != null && curso.getObjDocente().getNombre_docente() != null 
                                    ? curso.getObjDocente().getNombre_docente() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            formatearFechaISO(curso.getFecha_inicio()), dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            formatearFechaISO(curso.getFecha_fin()), dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getCupo_estimado() != null ? curso.getCupo_estimado().toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getSalon() != null ? curso.getSalon() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getEstado() != null ? curso.getEstado() : "-", dataFont)));
                }
                
                document.add(table);
            } else {
                com.itextpdf.text.Paragraph noData = new com.itextpdf.text.Paragraph(
                        "No hay cursos disponibles con los filtros aplicados.", infoFont);
                noData.setSpacingAfter(15);
                document.add(noData);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar PDF de cursos: {}", e.getMessage(), e);
            if (document != null && document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Error al generar PDF de cursos", e);
        }
    }

    /**
     * Genera un PDF con la lista de estudiantes del curso
     */
    @SuppressWarnings("unchecked")
    public byte[] generarPDFEstudiantesCurso(Map<String, Object> datos) {
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate()); // Horizontal para más columnas
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Título principal
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            String nombreCurso = (String) datos.get("nombre_curso");
            String codigoCurso = (String) datos.get("codigo_curso");
            String titulo = "ESTUDIANTES DEL CURSO: " + (nombreCurso != null ? nombreCurso : "N/A");
            if (codigoCurso != null) {
                titulo += " (" + codigoCurso + ")";
            }
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(titulo, titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información del curso
            com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            StringBuilder info = new StringBuilder();
            info.append("Fecha de generación: ").append(formatearFechaEspanol(new Date())).append("\n");
            info.append("Cupo Estimado: ").append(datos.get("cupo_estimado")).append("\n");
            info.append("Total Estudiantes: ").append(datos.get("total_estudiantes")).append("\n");
            info.append("Preinscritos: ").append(datos.get("total_preinscritos")).append("\n");
            info.append("Inscritos: ").append(datos.get("total_inscritos")).append("\n");
            
            com.itextpdf.text.Paragraph infoParagraph = new com.itextpdf.text.Paragraph(info.toString(), infoFont);
            infoParagraph.setSpacingAfter(15);
            document.add(infoParagraph);
            
            // Tabla de estudiantes
            List<Map<String, Object>> estudiantes = (List<Map<String, Object>>) datos.get("estudiantes");
            if (estudiantes != null && !estudiantes.isEmpty()) {
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(8);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{0.5f, 2.5f, 1f, 2f, 1f, 1.5f, 1.5f, 1.5f});
                
                // Encabezados
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("#", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Nombre Completo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Código", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Programa", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Tipo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado Preinscripción", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado Inscripción", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Correo", headerFont)));
                
                // Datos
                com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                
                int contador = 1;
                for (Map<String, Object> estudiante : estudiantes) {
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            String.valueOf(contador++), dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("nombre_completo") != null ? estudiante.get("nombre_completo").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("codigo") != null ? estudiante.get("codigo").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("programa") != null ? estudiante.get("programa").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("tipo") != null ? estudiante.get("tipo").toString() : "-", dataFont)));
                    String estadoPreinscripcion = estudiante.get("estado_preinscripcion") != null 
                            ? estudiante.get("estado_preinscripcion").toString() : "N/A";
                    if ("-".equals(estadoPreinscripcion) || estadoPreinscripcion.isEmpty() || estadoPreinscripcion.trim().isEmpty()) {
                        estadoPreinscripcion = "N/A";
                    }
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(estadoPreinscripcion, dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("estado_inscripcion") != null ? estudiante.get("estado_inscripcion").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("correo") != null ? estudiante.get("correo").toString() : "-", dataFont)));
                }
                
                document.add(table);
            } else {
                com.itextpdf.text.Paragraph noData = new com.itextpdf.text.Paragraph(
                        "No hay estudiantes registrados en este curso.", infoFont);
                noData.setSpacingAfter(15);
                document.add(noData);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar PDF de estudiantes: {}", e.getMessage(), e);
            if (document != null && document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Error al generar PDF de estudiantes", e);
        }
    }

    /**
     * Formatea una fecha en español sin hora
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "";
        }
        try {
            java.util.Locale spanishLocale = new java.util.Locale("es", "ES");
            SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", spanishLocale);
            return sdf.format(fecha);
        } catch (Exception e) {
            return fecha.toString();
        }
    }

    
    /**
     * Formatea una fecha ISO (String) a formato legible (dd/MM/yyyy)
     * Acepta formatos como "2026-06-16T16:55:43Z" o "2026-06-16"
     */
    private String formatearFechaISO(String fechaISO) {
        if (fechaISO == null || fechaISO.trim().isEmpty()) {
            return "-";
        }
        
        try {
            // Intentar parsear formato ISO completo (con hora)
            if (fechaISO.contains("T")) {
                String fechaPart = fechaISO.split("T")[0]; // Extraer solo la parte de la fecha
                String[] partes = fechaPart.split("-");
                if (partes.length == 3) {
                    return partes[2] + "/" + partes[1] + "/" + partes[0]; // dd/MM/yyyy
                }
            } else if (fechaISO.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // Formato simple yyyy-MM-dd
                String[] partes = fechaISO.split("-");
                return partes[2] + "/" + partes[1] + "/" + partes[0]; // dd/MM/yyyy
            }
            
            // Si no se puede parsear, retornar el original
            return fechaISO;
        } catch (Exception e) {
            return fechaISO;
        }
    }
}
