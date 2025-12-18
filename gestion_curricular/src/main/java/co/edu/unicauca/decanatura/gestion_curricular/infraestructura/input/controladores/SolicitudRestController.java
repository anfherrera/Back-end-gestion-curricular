package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import org.modelmapper.ModelMapper;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SolicitudRestController {

    private final GestionarSolicitudCUIntPort solicitudCU;
    private final SolicitudMapperDominio mapper;
    private final SolicitudRepositoryInt solicitudRepository;
    private final ModelMapper modelMapper;
//    private final SolicitudPazYSalvoMapperDominio solicitudPazYSalvoMapper;
//    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudCursoVeranoPreinscripcionMapper;
//    private final SolicitudCursoDeVeranoInscripcionMapperDominio solicitudCursoDeVeranoInscripcionMapper;

    

    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<SolicitudDTORespuesta> buscarSolicitudPorId(@Min(value = 1) @PathVariable Integer id) {
        Solicitud solicitud = solicitudCU.obtenerSolicitudPorId(id);
        if (solicitud == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                mapper.mappearDeSolicitudARespuesta(solicitud),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Boolean> eliminarSolicitud(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminada = solicitudCU.eliminarSolicitud(id);
        return new ResponseEntity<>(eliminada ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<SolicitudDTORespuesta>> listarSolicitudes() {
        List<Solicitud> solicitudes = solicitudCU.listarSolicitudes();
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorUsuario/{idUsuario}")
    public ResponseEntity<List<SolicitudDTORespuesta>> obtenerPorUsuario(@PathVariable Integer idUsuario) {
        List<Solicitud> solicitudes = solicitudCU.obtenerSolicitudesPorUsuario(idUsuario);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorNombre")
    public ResponseEntity<List<SolicitudDTORespuesta>> obtenerPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {
        List<Solicitud> solicitudes = solicitudCU.obtenerSolicitudesPorNombre(nombre);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorFechas")
    public ResponseEntity<List<SolicitudDTORespuesta>> buscarPorFechas(
            @RequestParam(name = "inicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam(name = "fin", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin) {

        List<Solicitud> solicitudes = solicitudCU.buscarSolicitudesPorFecha(inicio, fin);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

//    @PostMapping("/crearPazYSalvo")
//    public ResponseEntity<SolicitudDTORespuesta> crearSolicitudPazYSalvo(@RequestBody SolicitudPazYSalvoDTOPeticion solicitud) {
//        SolicitudPazYSalvo solicitudPazYSalvo = solicitudPazYSalvoMapper.mappearDeSolicitudDTOPeticionASolicitud(solicitud);
//        SolicitudPazYSalvo solicitudGuardada = solicitudCU.crearSolicitudPazYSalvo(solicitudPazYSalvo);
//        return new ResponseEntity<>(
//                mapper.mappearDeSolicitudARespuesta(solicitudGuardada),
//                HttpStatus.CREATED
//        );
//    }

//    @PostMapping("/crearCursoVeranoPreinscripcion")
//    public ResponseEntity<SolicitudCursoVeranoPreinscripcionDTORespuesta> crearSolicitudCursoVeranoPreinscripcion(
//            @RequestBody SolicitudCurosoVeranoPreinscripcionDTOPeticion solicitud) {

//        SolicitudCursoVeranoPreinscripcion solicitudDominio = solicitudCursoVeranoPreinscripcionMapper
//                .mappearDePeticionASolicitudCursoVeranoPreinscripcion(solicitud);

//        SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU
//                .crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

//        SolicitudCursoVeranoPreinscripcionDTORespuesta respuesta = solicitudCursoVeranoPreinscripcionMapper
//                .mappearDeSolicitudCursoVeranoPreinscripcionARespuesta(solicitudGuardada);

//        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
//    }

//    @PostMapping("/crearCursoVeranoInscripcion")
//    public ResponseEntity<SolicitudCursoVeranoInscripcionDTORespuesta> crearSolicitudCursoVeranoInscripcion(
//            @RequestBody SolicitudCursoVeranoInscripcionDTOPeticion solicitud) {

//        SolicitudCursoVeranoIncripcion solicitudDominio = solicitudCursoDeVeranoInscripcionMapper
//                .mappearDePeticionASolicitudCursoVeranoIncripcion(solicitud);

//        SolicitudCursoVeranoIncripcion solicitudGuardada = solicitudCU
//                .crearSolicitudCursoVeranoInscripcion(solicitudDominio);

//        SolicitudCursoVeranoInscripcionDTORespuesta respuesta = solicitudCursoDeVeranoInscripcionMapper
//                .mappearDeSolicitudCursoVeranoIncripcionARespuesta(solicitudGuardada);

//        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
//    }

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
    public ResponseEntity<Map<String, Object>> obtenerHistorialCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        try {
            List<SolicitudEntity> solicitudesEntityFiltradas = solicitudRepository.buscarHistorialConFiltros(
                periodoAcademico != null && !periodoAcademico.trim().isEmpty() ? periodoAcademico.trim() : null,
                idUsuario,
                estadoActual != null && !estadoActual.trim().isEmpty() ? estadoActual.trim() : null
            );
            
            if (solicitudesEntityFiltradas == null || solicitudesEntityFiltradas.isEmpty()) {
                // Obtener totales del sistema para estadísticas
                Long totalSistema = solicitudRepository.contarHistorialConFiltros(null, null, null);
                Long totalProcesadas = solicitudRepository.contarSolicitudesProcesadas(null, null);
                
                Map<String, Object> respuestaVacia = new HashMap<>();
                respuestaVacia.put("solicitudes", new ArrayList<>());
                respuestaVacia.put("total", 0);
                respuestaVacia.put("total_solicitudes_sistema", totalSistema != null ? totalSistema.intValue() : 0);
                respuestaVacia.put("total_solicitudes_procesadas", totalProcesadas != null ? totalProcesadas.intValue() : 0);
                respuestaVacia.put("total_solicitudes_no_procesadas", 
                    (totalSistema != null && totalProcesadas != null) ? (totalSistema.intValue() - totalProcesadas.intValue()) : 0);
                return ResponseEntity.ok(respuestaVacia);
            }
            
            // Convertir entidades a modelos de dominio
            List<Solicitud> todasLasSolicitudesDominio = solicitudesEntityFiltradas.stream()
                .map(this::mapearEntityADominio)
                .filter(s -> s != null)
                .collect(Collectors.toList());
            
            // Convertir a DTOs y aplicar filtro de tipoSolicitud (que no se puede hacer fácilmente en SQL)
            List<SolicitudDTORespuesta> todasLasSolicitudesDTO = todasLasSolicitudesDominio.stream()
                    .map(solicitud -> {
                        SolicitudDTORespuesta dto = mapper.mappearDeSolicitudARespuesta(solicitud);
                        determinarCategoriaYTipo(solicitud, dto);
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            // Aplicar filtro por tipoSolicitud (este filtro se hace en memoria porque requiere análisis del nombre)
            List<SolicitudDTORespuesta> solicitudesFiltradas = todasLasSolicitudesDTO.stream()
                    .filter(s -> {
                        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                            String nombreSolicitud = s.getNombre_solicitud() != null ? s.getNombre_solicitud().toLowerCase() : "";
                            String tipoFiltro = tipoSolicitud.trim().toLowerCase();
                            String tipoNormalizado = tipoFiltro
                                    .replace("académico", "")
                                    .replace("academico", "")
                                    .trim();
                            
                            boolean coincide = false;
                            if (tipoNormalizado.contains("paz") && tipoNormalizado.contains("salvo")) {
                                coincide = nombreSolicitud.contains("paz") && nombreSolicitud.contains("salvo");
                            } else if (tipoNormalizado.contains("reingreso")) {
                                coincide = nombreSolicitud.contains("reingreso");
                            } else if (tipoNormalizado.contains("homologacion") || tipoNormalizado.contains("homologación")) {
                                coincide = nombreSolicitud.contains("homologacion") || nombreSolicitud.contains("homologación");
                            } else if (tipoNormalizado.contains("ecaes")) {
                                coincide = nombreSolicitud.contains("ecaes");
                            } else if (tipoNormalizado.contains("curso") && (tipoNormalizado.contains("verano") || tipoNormalizado.contains("intersemestral"))) {
                                coincide = nombreSolicitud.contains("curso") && (nombreSolicitud.contains("verano") || nombreSolicitud.contains("intersemestral"));
                            } else {
                                coincide = nombreSolicitud.contains(tipoNormalizado);
                            }
                            return coincide;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            
            // Obtener totales usando queries optimizadas
            Long totalSistema = solicitudRepository.contarHistorialConFiltros(null, null, null);
            Long totalProcesadas = solicitudRepository.contarSolicitudesProcesadas(null, null);
            
            // Construir respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("solicitudes", solicitudesFiltradas);
            respuesta.put("total", solicitudesFiltradas.size());
            respuesta.put("total_solicitudes_sistema", totalSistema != null ? totalSistema.intValue() : 0);
            respuesta.put("total_solicitudes_procesadas", totalProcesadas != null ? totalProcesadas.intValue() : 0);
            respuesta.put("total_solicitudes_no_procesadas", 
                (totalSistema != null && totalProcesadas != null) ? (totalSistema.intValue() - totalProcesadas.intValue()) : 0);
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error al obtener historial completo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta el historial completo de solicitudes a PDF con filtros opcionales
     * GET /api/solicitudes/historial/export/pdf
     * 
     * @param periodoAcademico Período académico (opcional, formato: "YYYY-P")
     * @param tipoSolicitud Tipo de solicitud (opcional)
     * @param estadoActual Estado actual de la solicitud (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @return Archivo PDF con el historial
     */
    @GetMapping("/historial/export/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarHistorialPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        try {
            ResponseEntity<Map<String, Object>> historialResponse = obtenerHistorialCompleto(
                    periodoAcademico, tipoSolicitud, estadoActual, idUsuario);
            
            if (historialResponse.getStatusCode() != HttpStatus.OK || historialResponse.getBody() == null) {
                log.error("No se pudo obtener el historial para generar el PDF");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            Map<String, Object> historialData = historialResponse.getBody();
            @SuppressWarnings("unchecked")
            List<SolicitudDTORespuesta> solicitudes = (List<SolicitudDTORespuesta>) historialData.get("solicitudes");
            
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
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error al generar PDF del historial: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
                                       Map<String, Object> historialData,
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
                filtrosInfo.append("Total Filtrado: ").append(historialData.get("total")).append(" solicitudes\n");
                filtrosInfo.append("Total Sistema: ").append(historialData.get("total_solicitudes_sistema")).append(" solicitudes\n");
                filtrosInfo.append("Procesadas: ").append(historialData.get("total_solicitudes_procesadas")).append(" solicitudes\n");
                filtrosInfo.append("No Procesadas: ").append(historialData.get("total_solicitudes_no_procesadas")).append(" solicitudes\n");
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
            log.error("Error al generar el PDF del historial: {}", e.getMessage(), e);
            
            // Generar PDF de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                com.itextpdf.text.Document errorDoc = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(errorDoc, errorBaos);
                
                errorDoc.open();
                com.itextpdf.text.Font errorFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
                com.itextpdf.text.Paragraph errorMsg = new com.itextpdf.text.Paragraph(
                        "Error al generar el reporte: " + e.getMessage(), errorFont);
                errorDoc.add(errorMsg);
                errorDoc.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                log.error("No se pudo generar el PDF alterno con información de error: {}", ex.getMessage(), ex);
                return new byte[0];
            }
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

    private Solicitud mapearEntityADominio(SolicitudEntity entity) {
        if (entity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
            return modelMapper.map(entity, SolicitudCursoVeranoPreinscripcion.class);
        } else if (entity instanceof SolicitudEcaesEntity) {
            return modelMapper.map(entity, SolicitudEcaes.class);
        } else if (entity instanceof SolicitudReingresoEntity) {
            return modelMapper.map(entity, SolicitudReingreso.class);
        } else if (entity instanceof SolicitudHomologacionEntity) {
            return modelMapper.map(entity, SolicitudHomologacion.class);
        } else if (entity instanceof SolicitudPazYSalvoEntity) {
            return modelMapper.map(entity, SolicitudPazYSalvo.class);
        } else if (entity instanceof SolicitudCursoVeranoInscripcionEntity) {
            return modelMapper.map(entity, SolicitudCursoVeranoIncripcion.class);
        }
        return null;
    }

    private void determinarCategoriaYTipo(Solicitud solicitud, SolicitudDTORespuesta dto) {
        if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
            dto.setCategoria("Cursos de Verano");
            dto.setTipo_solicitud("Preinscripcion");
        } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
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
