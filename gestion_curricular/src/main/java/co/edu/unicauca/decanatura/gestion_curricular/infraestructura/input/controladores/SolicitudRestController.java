package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.HistorialSolicitudesDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
@Slf4j
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudRestController {

    private final GestionarSolicitudCUIntPort solicitudCU;
    private final SolicitudMapperDominio mapper;

    

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SolicitudDTORespuesta> buscarSolicitudPorId(@Min(value = 1) @PathVariable Integer id) {
        Solicitud solicitud = solicitudCU.obtenerSolicitudPorId(id);
        if (solicitud == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                mapper.mapearDeSolicitudARespuesta(solicitud),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Boolean> eliminarSolicitud(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminada = solicitudCU.eliminarSolicitud(id);
        return new ResponseEntity<>(eliminada ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    /** Sin parámetros, todas las solicitudes; con {@code nombre}, las que lo contienen. Las dos son listas. */
    @GetMapping
    public ResponseEntity<List<SolicitudDTORespuesta>> listarOBuscar(
            @RequestParam(name = "nombre", required = false) String nombre) {
        return nombre != null ? obtenerPorNombre(nombre) : listarSolicitudes();
    }

    private ResponseEntity<List<SolicitudDTORespuesta>> listarSolicitudes() {
        List<Solicitud> solicitudes = solicitudCU.listarSolicitudes();
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mapearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    private ResponseEntity<List<SolicitudDTORespuesta>> obtenerPorNombre(String nombre) {
        List<Solicitud> solicitudes = solicitudCU.obtenerSolicitudesPorNombre(nombre);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mapearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }


    /**
     * Obtiene el historial completo de solicitudes con filtros opcionales
     * GET /api/solicitudes/historial
     * 
     * @param periodoAcademico Período académico (opcional, formato: "YYYY-P")
     * @param tipoSolicitud Tipo de solicitud (opcional)
     * @param estadoActual Estado actual de la solicitud (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @return Lista de solicitudes con sus detalles
     */
    @GetMapping("/historial")
    @Transactional(readOnly = true)
    public ResponseEntity<HistorialSolicitudesDTORespuesta> obtenerHistorialCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        List<Solicitud> todasLasSolicitudesDominio = solicitudCU.buscarHistorial(periodoAcademico, idUsuario, estadoActual);

        // Convertir a DTOs y aplicar filtro de tipoSolicitud (que no se puede hacer fácilmente en SQL)
        List<SolicitudDTORespuesta> todasLasSolicitudesDTO = todasLasSolicitudesDominio.stream()
                .map(solicitud -> {
                    SolicitudDTORespuesta dto = mapper.mapearDeSolicitudARespuesta(solicitud);
                    determinarCategoriaYTipo(solicitud, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        
        // Aplicar filtro por tipoSolicitud en memoria, con la categoría y el tipo calculados arriba
        List<SolicitudDTORespuesta> solicitudesFiltradas = todasLasSolicitudesDTO.stream()
                .filter(s -> tipoSolicitud == null || tipoSolicitud.trim().isEmpty() || esDelTipo(s, tipoSolicitud))
                .collect(Collectors.toList());
        
        // Totales del sistema (sin filtros) para las estadísticas del encabezado
        long totalSistema = solicitudCU.contarHistorial();
        long totalProcesadas = solicitudCU.contarSolicitudesProcesadas();

        return ResponseEntity.ok(new HistorialSolicitudesDTORespuesta(solicitudesFiltradas,
                solicitudesFiltradas.size(), (int) totalSistema, (int) totalProcesadas,
                (int) (totalSistema - totalProcesadas)));
    }

    /**
     * Exporta el historial completo de solicitudes a PDF con filtros opcionales
     * GET /api/solicitudes/historial?formato=pdf
     * 
     * @param periodoAcademico Período académico (opcional, formato: "YYYY-P")
     * @param tipoSolicitud Tipo de solicitud (opcional)
     * @param estadoActual Estado actual de la solicitud (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @return Archivo PDF con el historial
     */
    @GetMapping(value = "/historial", params = "formato=pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarHistorialPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        ResponseEntity<HistorialSolicitudesDTORespuesta> historialResponse = obtenerHistorialCompleto(
                periodoAcademico, tipoSolicitud, estadoActual, idUsuario);

        if (historialResponse.getStatusCode() != HttpStatus.OK || historialResponse.getBody() == null) {
            log.error("No se pudo obtener el historial para generar el PDF");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HistorialSolicitudesDTORespuesta historialData = historialResponse.getBody();
        List<SolicitudDTORespuesta> solicitudes = historialData.solicitudes();

        if (solicitudes == null) {
            solicitudes = new ArrayList<>();
        }
        
        // Generar PDF
        byte[] pdfBytes = generarPDFHistorial(solicitudes, historialData, periodoAcademico, tipoSolicitud, estadoActual);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        // Nombre del archivo con fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        String filename = "historial_solicitudes_" + fecha + ".pdf";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.adjuntoEscapado(filename));
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Genera un PDF con el historial de solicitudes
     * 
     * @param solicitudes Lista de solicitudes a incluir en el PDF
     * @param historialData Datos adicionales del historial (totales, etc.)
     * @param periodoAcademico Filtro de período académico aplicado
     * @param tipoSolicitud Filtro de tipo de solicitud aplicado
     * @param estadoActual Filtro de estado aplicado
     * @return Array de bytes del PDF
     */
    private byte[] generarPDFHistorial(List<SolicitudDTORespuesta> solicitudes, 
                                       HistorialSolicitudesDTORespuesta historialData,
                                       String periodoAcademico, 
                                       String tipoSolicitud, 
                                       String estadoActual) {
        
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
                    "HISTORIAL COMPLETO DE SOLICITUDES", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información de filtros aplicados
            com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            StringBuilder filtrosInfo = new StringBuilder();
            filtrosInfo.append("Fecha de generación: ").append(formatearFechaEspanol(new Date())).append("\n");
            
            // Período Académico - siempre mostrar
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                filtrosInfo.append("Período Académico: ").append(periodoAcademico).append("\n");
            } else {
                // Si no hay filtro, mostrar "Todos los períodos" o el período actual
                try {
                    // Intentar obtener el período actual
                    PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
                    if (periodoActual != null) {
                        filtrosInfo.append("Período Académico: Todos (Período actual: ").append(periodoActual.getValor()).append(")\n");
                    } else {
                        filtrosInfo.append("Período Académico: Todos los períodos\n");
                    }
                } catch (Exception e) {
                    filtrosInfo.append("Período Académico: Todos los períodos\n");
                }
            }
            
            // Tipo de Solicitud
            if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                filtrosInfo.append("Tipo de Solicitud: ").append(tipoSolicitud).append("\n");
            } else {
                filtrosInfo.append("Tipo de Solicitud: Todos los tipos\n");
            }
            
            // Estado
            if (estadoActual != null && !estadoActual.trim().isEmpty()) {
                filtrosInfo.append("Estado: ").append(estadoActual).append("\n");
            } else {
                filtrosInfo.append("Estado: Todos los estados\n");
            }
            
            // Totales
            if (historialData != null) {
                filtrosInfo.append("\n");
                filtrosInfo.append("Total Filtrado: ").append(historialData.total()).append(" solicitudes\n");
                filtrosInfo.append("Total Sistema: ").append(historialData.totalSistema()).append(" solicitudes\n");
                filtrosInfo.append("Procesadas: ").append(historialData.totalProcesadas()).append(" solicitudes\n");
                filtrosInfo.append("No Procesadas: ").append(historialData.totalNoProcesadas()).append(" solicitudes\n");
            }
            
            com.itextpdf.text.Paragraph info = new com.itextpdf.text.Paragraph(filtrosInfo.toString(), infoFont);
            info.setSpacingAfter(15);
            document.add(info);
            
            // Tabla de solicitudes
            if (solicitudes != null && !solicitudes.isEmpty()) {
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 2f, 1f, 1.5f, 1f});
                
                // Encabezados
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("ID", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Solicitud", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estudiante", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Programa", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Período", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Fecha", headerFont)));
                
                // Datos
                com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                
                for (SolicitudDTORespuesta solicitud : solicitudes) {
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(String.valueOf(solicitud.getId_solicitud()), cellFont)));
                    
                    String nombreSolicitud = solicitud.getNombre_solicitud() != null 
                            ? solicitud.getNombre_solicitud() : "N/A";
                    if (nombreSolicitud.length() > 40) {
                        nombreSolicitud = nombreSolicitud.substring(0, 37) + "...";
                    }
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(nombreSolicitud, cellFont)));
                    
                    String estudiante = solicitud.getObjUsuario() != null 
                            ? (solicitud.getObjUsuario().getNombre_completo() != null 
                                    ? solicitud.getObjUsuario().getNombre_completo() : "N/A")
                            : "N/A";
                    if (estudiante.length() > 25) {
                        estudiante = estudiante.substring(0, 22) + "...";
                    }
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(estudiante, cellFont)));
                    
                    String programa = solicitud.getObjUsuario() != null 
                            && solicitud.getObjUsuario().getObjPrograma() != null
                            ? (solicitud.getObjUsuario().getObjPrograma().getNombre_programa() != null 
                                    ? solicitud.getObjUsuario().getObjPrograma().getNombre_programa() : "N/A")
                            : "N/A";
                    if (programa.length() > 30) {
                        programa = programa.substring(0, 27) + "...";
                    }
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(programa, cellFont)));
                    
                    String periodo = solicitud.getPeriodo_academico() != null 
                            ? solicitud.getPeriodo_academico() : "N/A";
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(periodo, cellFont)));
                    
                    String estado = "Sin estado";
                    if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                        estado = solicitud.getEstadosSolicitud().stream()
                                .max(Comparator.comparing(e -> e.getFecha_registro_estado() != null 
                                        ? e.getFecha_registro_estado() : new Date(0)))
                                .map(e -> e.getEstado_actual())
                                .orElse("Sin estado");
                    }
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(estado, cellFont)));
                    
                    String fecha = solicitud.getFecha_registro_solicitud() != null 
                            ? dateFormat.format(solicitud.getFecha_registro_solicitud()) : "N/A";
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(fecha, cellFont)));
                }
                
                document.add(table);
            } else {
                com.itextpdf.text.Paragraph noData = new com.itextpdf.text.Paragraph(
                        "No se encontraron solicitudes con los filtros aplicados.", infoFont);
                noData.setSpacingAfter(15);
                document.add(noData);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            // Propagar: antes se devolvía un PDF con el texto del error y código 200
            throw new IllegalStateException("Error al generar el PDF del historial", e);
        } finally {
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                log.error("Ocurrió un error al cerrar los recursos del PDF: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Formatea una fecha en español sin hora
     * Ejemplo: "15 de diciembre de 2025"
     * 
     * @param fecha Fecha a formatear
     * @return Fecha formateada en español
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "";
        }
        
        try {
            // Convertir Date a LocalDate
            LocalDate localDate = fecha.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            
            // Formatear en español
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", 
                    new java.util.Locale("es", "ES"));
            return localDate.format(formatter);
        } catch (Exception e) {
            // Fallback a formato simple
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(fecha);
        }
    }

    /**
     * Indica si la solicitud es del tipo pedido. Un tipo que nombra un proceso (paz y salvo, reingreso,
     * homologación, ECAES o curso de verano/intersemestral) se compara con la categoría que asigna
     * {@link #determinarCategoriaYTipo}; cualquier otro, con la categoría o el tipo exactos. No distingue
     * mayúsculas ni tildes.
     */
    private static boolean esDelTipo(SolicitudDTORespuesta dto, String tipoSolicitud) {
        String tipo = normalizar(tipoSolicitud).replace("academico", "").trim();
        String categoria;
        if (tipo.contains("paz") && tipo.contains("salvo")) {
            categoria = "Paz y Salvo";
        } else if (tipo.contains("reingreso")) {
            categoria = "Reingreso";
        } else if (tipo.contains("homologacion")) {
            categoria = "Homologación";
        } else if (tipo.contains("ecaes")) {
            categoria = "ECAES";
        } else if (tipo.contains("curso") && (tipo.contains("verano") || tipo.contains("intersemestral"))) {
            categoria = "Cursos de Verano";
        } else {
            return tipo.equals(normalizar(dto.getCategoria())) || tipo.equals(normalizar(dto.getTipo_solicitud()));
        }
        return categoria.equals(dto.getCategoria());
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }

    private void determinarCategoriaYTipo(Solicitud solicitud, SolicitudDTORespuesta dto) {
        if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
            dto.setCategoria("Cursos de Verano");
            dto.setTipo_solicitud("Preinscripcion");
        } else if (solicitud instanceof SolicitudCursoVeranoInscripcion) {
            dto.setCategoria("Cursos de Verano");
            dto.setTipo_solicitud("Inscripcion");
        } else if (solicitud instanceof SolicitudCursoVerano) {
            dto.setCategoria("Cursos de Verano");
            dto.setTipo_solicitud("Curso Nuevo");
        } else if (solicitud instanceof SolicitudPazYSalvo) {
            dto.setCategoria("Paz y Salvo");
            dto.setTipo_solicitud("Paz y Salvo");
        } else if (solicitud instanceof SolicitudReingreso) {
            dto.setCategoria("Reingreso");
            dto.setTipo_solicitud("Reingreso");
        } else if (solicitud instanceof SolicitudHomologacion) {
            dto.setCategoria("Homologación");
            dto.setTipo_solicitud("Homologacion");
        } else if (solicitud instanceof SolicitudEcaes) {
            dto.setCategoria("ECAES");
            dto.setTipo_solicitud("ECAES");
        } else {
            if (solicitud.getObjCursoOfertadoVerano() != null) {
                dto.setCategoria("Cursos de Verano");
                dto.setTipo_solicitud("Curso Verano");
            } else {
                String nombre = dto.getNombre_solicitud() != null ? dto.getNombre_solicitud().toLowerCase() : "";
                if (nombre.contains("curso") && (nombre.contains("verano") || nombre.contains("intersemestral"))) {
                    dto.setCategoria("Cursos de Verano");
                    dto.setTipo_solicitud("Curso Verano");
                } else if (nombre.contains("paz") && nombre.contains("salvo")) {
                    dto.setCategoria("Paz y Salvo");
                    dto.setTipo_solicitud("Paz y Salvo");
                } else if (nombre.contains("reingreso")) {
                    dto.setCategoria("Reingreso");
                    dto.setTipo_solicitud("Reingreso");
                } else if (nombre.contains("homologacion") || nombre.contains("homologación")) {
                    dto.setCategoria("Homologación");
                    dto.setTipo_solicitud("Homologacion");
                } else if (nombre.contains("ecaes")) {
                    dto.setCategoria("ECAES");
                    dto.setTipo_solicitud("ECAES");
                } else {
                    dto.setCategoria("Otro");
                    dto.setTipo_solicitud("Otro");
                }
            }
        }
    }

}
