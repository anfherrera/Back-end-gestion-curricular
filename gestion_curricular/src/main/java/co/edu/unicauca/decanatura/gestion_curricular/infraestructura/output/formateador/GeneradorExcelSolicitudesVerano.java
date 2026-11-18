package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarExcelSolicitudesVeranoIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.SolicitudReporte;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ReporteSolicitudesVeranoMapper;

/** Genera el Excel del listado de solicitudes de cursos de verano. */
@Component
@Slf4j
public class GeneradorExcelSolicitudesVerano implements GenerarExcelSolicitudesVeranoIntPort {

    /**
     * Genera un archivo Excel con las solicitudes de cursos intersemestrales
     * 
     * @param solicitudes Lista de solicitudes a incluir en el Excel
     * @param idCurso Filtro de curso aplicado (opcional)
     * @param estado Filtro de estado aplicado (opcional)
     * @param periodoAcademico Filtro de período académico aplicado (opcional)
     * @param fechaInicio Filtro de fecha inicio aplicado (opcional)
     * @param fechaFin Filtro de fecha fin aplicado (opcional)
     * @return Array de bytes del archivo Excel
     */
    public byte[] generarExcelSolicitudes(List<Solicitud> datos,
                                          Integer idCurso, 
                                          String estado, 
                                          String periodoAcademico,
                                          Date fechaInicio, 
                                          Date fechaFin) {
        List<SolicitudReporte> solicitudes = ReporteSolicitudesVeranoMapper.formatear(datos, null);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Crear hoja de solicitudes
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Solicitudes Cursos Intersemestrales");
            
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
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            int rowNum = 0;
            
            // Titulo
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SOLICITUDES DE CURSOS INTERSEMESTRALES DE VERANO");
            titleCell.setCellStyle(titleStyle);
            
            rowNum++; // Espacio
            
            // Fecha de generacion
            org.apache.poi.ss.usermodel.Row fechaRow = sheet.createRow(rowNum++);
            fechaRow.createCell(0).setCellValue("Fecha de generacion: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            
            // Información de filtros aplicados
            StringBuilder filtrosInfo = new StringBuilder();
            boolean hayFiltros = false;
            
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
                filtrosInfo.append("Período Académico: ").append(periodoAcademico);
                hayFiltros = true;
            } else {
                try {
                    PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
                    if (periodoActual != null) {
                        filtrosInfo.append("Período Académico: Todos (Período actual: ").append(periodoActual.getValor()).append(")");
                    } else {
                        filtrosInfo.append("Período Académico: Todos los períodos");
                    }
                } catch (Exception e) {
                    filtrosInfo.append("Período Académico: Todos los períodos");
                }
                hayFiltros = true;
            }
            
            if (idCurso != null) {
                if (hayFiltros) filtrosInfo.append(" | ");
                filtrosInfo.append("Curso ID: ").append(idCurso);
                hayFiltros = true;
            }
            
            if (estado != null && !estado.trim().isEmpty()) {
                if (hayFiltros) filtrosInfo.append(" | ");
                filtrosInfo.append("Estado: ").append(estado);
                hayFiltros = true;
            }
            
            if (fechaInicio != null || fechaFin != null) {
                if (hayFiltros) filtrosInfo.append(" | ");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                if (fechaInicio != null && fechaFin != null) {
                    filtrosInfo.append("Fechas: ").append(dateFormat.format(fechaInicio)).append(" - ").append(dateFormat.format(fechaFin));
                } else if (fechaInicio != null) {
                    filtrosInfo.append("Fecha desde: ").append(dateFormat.format(fechaInicio));
                } else if (fechaFin != null) {
                    filtrosInfo.append("Fecha hasta: ").append(dateFormat.format(fechaFin));
                }
                hayFiltros = true;
            }
            
            if (hayFiltros) {
                org.apache.poi.ss.usermodel.Row filtrosRow = sheet.createRow(rowNum++);
                filtrosRow.createCell(0).setCellValue("Filtros aplicados: " + filtrosInfo.toString());
            }
            
            // Total de solicitudes
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(0).setCellValue("Total de solicitudes: " + solicitudes.size());
            
            rowNum++; // Espacio
            
            // Encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Nombre Completo", "Codigo", "ID Curso", "ID Materia", "Codigo Materia", "Grupo", "Curso", "Condicion", "Motivo de la Solicitud", "Estado", "Fecha", "Tipo"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (SolicitudReporte solicitud : solicitudes) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                int colNum = 0;
                row.createCell(colNum++).setCellValue(solicitud.nombreCompleto() != null ? solicitud.nombreCompleto() : "");
                row.createCell(colNum++).setCellValue(solicitud.codigo() != null ? solicitud.codigo() : "");
                
                // ID Curso
                if (solicitud.idCurso() != null) {
                    row.createCell(colNum++).setCellValue(solicitud.idCurso().toString());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // ID Materia
                if (solicitud.idMateria() != null) {
                    row.createCell(colNum++).setCellValue(solicitud.idMateria().toString());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // Codigo Materia
                if (solicitud.codigoMateria() != null) {
                    row.createCell(colNum++).setCellValue(solicitud.codigoMateria());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // Grupo
                if (solicitud.grupoCurso() != null) {
                    row.createCell(colNum++).setCellValue(solicitud.grupoCurso());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                row.createCell(colNum++).setCellValue(solicitud.curso() != null ? solicitud.curso() : "");
                
                // Condicion - formatear para que sea mas legible
                String condicion = solicitud.condicion() != null ? solicitud.condicion() : "";
                if (condicion.equals("PRIMERA_VEZ")) {
                    condicion = "PRIMERA VEZ";
                }
                row.createCell(colNum++).setCellValue(condicion);
                
                row.createCell(colNum++).setCellValue(solicitud.motivoSolicitud() != null ? solicitud.motivoSolicitud() : "");
                row.createCell(colNum++).setCellValue(solicitud.estado() != null ? solicitud.estado() : "");
                
                // Fecha formateada
                if (solicitud.fecha() != null) {
                    Date fecha = solicitud.fecha();
                    row.createCell(colNum++).setCellValue(dateFormat.format(fecha));
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                row.createCell(colNum++).setCellValue(solicitud.tipo() != null ? solicitud.tipo() : "");
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Aumentar un poco mas el ancho para mejor legibilidad
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generando Excel: {}", e.getMessage(), e);
            log.error("Error: {}", e.getMessage(), e);
            
            // Generar Excel de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                org.apache.poi.xssf.usermodel.XSSFWorkbook errorWorkbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet errorSheet = errorWorkbook.createSheet("Error");
                
                org.apache.poi.ss.usermodel.Row errorRow = errorSheet.createRow(0);
                errorRow.createCell(0).setCellValue("Error al generar el reporte: " + e.getMessage());
                
                errorWorkbook.write(errorBaos);
                errorWorkbook.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                log.error("Error generando Excel de error: {}", ex.getMessage(), ex);
                return new byte[0];
            }
        }
    }
}
