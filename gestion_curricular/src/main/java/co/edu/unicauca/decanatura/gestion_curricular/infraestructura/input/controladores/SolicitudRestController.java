package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;


import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SolicitudRestController {

    private final GestionarSolicitudCUIntPort solicitudCU;
    private final SolicitudMapperDominio mapper;
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

    /**
     * Obtener historial completo de todas las solicitudes procesadas con filtros opcionales
     * GET /api/solicitudes/historial
     * 
     * Este endpoint permite ver el historial verdadero de todas las solicitudes que han sido procesadas,
     * independientemente de su estado final actual. Permite filtrar por período académico, tipo de solicitud,
     * estado actual, y usuario.
     * 
     * Acceso permitido para: Decano, Funcionario, Coordinador, Secretario, Administrador
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param tipoSolicitud Tipo de solicitud opcional (PAZ_SALVO, ECAES, REINGRESO, HOMOLOGACION, CURSO_VERANO_PREINSCRIPCION, CURSO_VERANO_INSCRIPCION)
     * @param estadoActual Estado actual opcional para filtrar
     * @param idUsuario ID del usuario opcional para filtrar por estudiante
     * @return Lista de todas las solicitudes procesadas con información adicional
     */
    @GetMapping("/historial")
    @PreAuthorize("hasRole('Decano') or hasRole('Funcionario') or hasRole('Coordinador') or hasRole('Secretario') or hasRole('Administrador')")
    public ResponseEntity<?> obtenerHistorialCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        try {
            log.info("Obteniendo historial completo - periodoAcademico: {}, tipoSolicitud: {}, estadoActual: {}, idUsuario: {}", 
                    periodoAcademico, tipoSolicitud, estadoActual, idUsuario);
            
            // Convertir formato del período académico si viene en formato "Primer Período 2025" o "Segundo Período 2025"
            final String periodoAcademicoFiltro = convertirFormatoPeriodoAcademico(periodoAcademico) != null 
                    ? convertirFormatoPeriodoAcademico(periodoAcademico) 
                    : periodoAcademico;
            
            if (periodoAcademicoFiltro != null && !periodoAcademicoFiltro.equals(periodoAcademico)) {
                log.debug("Período académico convertido de '{}' a '{}'", periodoAcademico, periodoAcademicoFiltro);
            }
            
            // Obtener todas las solicitudes
            List<Solicitud> todasLasSolicitudes = solicitudCU.listarSolicitudes();
            
            log.debug("Total de solicitudes obtenidas: {}", todasLasSolicitudes != null ? todasLasSolicitudes.size() : 0);
            
            if (todasLasSolicitudes == null) {
                todasLasSolicitudes = new java.util.ArrayList<>();
            }
            
            // Filtrar solo las procesadas (que tienen más de un estado o estado diferente a "Enviada")
            List<Solicitud> solicitudesProcesadas = todasLasSolicitudes.stream()
                    .filter(solicitud -> {
                        // Una solicitud está procesada si tiene más de un estado o su último estado no es "Enviada"
                        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
                            return false;
                        }
                        
                        // Verificar si está procesada
                        boolean procesada = solicitud.getEstadosSolicitud().size() > 1 || 
                                          !"Enviada".equals(solicitud.getEstadosSolicitud()
                                              .get(solicitud.getEstadosSolicitud().size() - 1)
                                              .getEstado_actual());
                        
                        if (!procesada) {
                            return false;
                        }
                        
                        // Filtrar por período académico
                        if (periodoAcademicoFiltro != null && !periodoAcademicoFiltro.trim().isEmpty()) {
                            if (solicitud.getPeriodo_academico() == null || 
                                !periodoAcademicoFiltro.trim().equals(solicitud.getPeriodo_academico())) {
                                return false;
                            }
                        }
                        
                        // Filtrar por tipo de solicitud
                        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                            String tipoDetectado = detectarTipoSolicitud(solicitud);
                            if (!tipoSolicitud.trim().equalsIgnoreCase(tipoDetectado)) {
                                return false;
                            }
                        }
                        
                        // Filtrar por estado actual
                        if (estadoActual != null && !estadoActual.trim().isEmpty()) {
                            String ultimoEstado = solicitud.getEstadosSolicitud()
                                    .get(solicitud.getEstadosSolicitud().size() - 1)
                                    .getEstado_actual();
                            if (!estadoActual.trim().equalsIgnoreCase(ultimoEstado)) {
                                return false;
                            }
                        }
                        
                        // Filtrar por usuario
                        if (idUsuario != null) {
                            if (solicitud.getObjUsuario() == null || 
                                !idUsuario.equals(solicitud.getObjUsuario().getId_usuario())) {
                                return false;
                            }
                        }
                        
                        return true;
                    })
                    .collect(Collectors.toList());
            
            // Mapear a DTOs y agregar información adicional
            List<java.util.Map<String, Object>> respuesta = solicitudesProcesadas.stream()
                    .map(solicitud -> {
                        SolicitudDTORespuesta dto = mapper.mappearDeSolicitudARespuesta(solicitud);
                        java.util.Map<String, Object> item = new java.util.HashMap<>();
                        
                        // Información básica
                        item.put("id_solicitud", dto.getId_solicitud());
                        item.put("nombre_solicitud", dto.getNombre_solicitud());
                        item.put("periodo_academico", dto.getPeriodo_academico());
                        item.put("fecha_registro_solicitud", dto.getFecha_registro_solicitud());
                        item.put("fecha_ceremonia", solicitud.getFecha_ceremonia());
                        
                        // Tipo de solicitud
                        item.put("tipo_solicitud", detectarTipoSolicitud(solicitud));
                        item.put("tipo_solicitud_display", obtenerNombreTipoSolicitud(detectarTipoSolicitud(solicitud)));
                        
                        // Estado actual
                        String estadoActualSolicitud = "Sin estado";
                        java.util.Date fechaUltimoEstado = null;
                        if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoEstado = 
                                solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1);
                            estadoActualSolicitud = ultimoEstado.getEstado_actual();
                            fechaUltimoEstado = ultimoEstado.getFecha_registro_estado();
                        }
                        item.put("estado_actual", estadoActualSolicitud);
                        item.put("fecha_ultimo_estado", fechaUltimoEstado);
                        
                        // Información del usuario
                        if (dto.getObjUsuario() != null) {
                            java.util.Map<String, Object> usuarioInfo = new java.util.HashMap<>();
                            usuarioInfo.put("id_usuario", dto.getObjUsuario().getId_usuario());
                            usuarioInfo.put("nombre_completo", dto.getObjUsuario().getNombre_completo());
                            usuarioInfo.put("codigo", dto.getObjUsuario().getCodigo());
                            usuarioInfo.put("correo", dto.getObjUsuario().getCorreo());
                            item.put("usuario", usuarioInfo);
                        }
                        
                        // Información de estados (historial completo)
                        item.put("estadosSolicitud", dto.getEstadosSolicitud());
                        item.put("total_estados", solicitud.getEstadosSolicitud() != null ? solicitud.getEstadosSolicitud().size() : 0);
                        
                        // Información de documentos
                        item.put("total_documentos", dto.getDocumentos() != null ? dto.getDocumentos().size() : 0);
                        
                        return item;
                    })
                    .collect(Collectors.toList());
            
            // Contar solicitudes no procesadas (solo con estado "Enviada")
            long solicitudesNoProcesadas = todasLasSolicitudes.stream()
                    .filter(solicitud -> {
                        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
                            return false;
                        }
                        // Una solicitud no está procesada si solo tiene un estado y es "Enviada"
                        return solicitud.getEstadosSolicitud().size() == 1 && 
                               "Enviada".equals(solicitud.getEstadosSolicitud().get(0).getEstado_actual());
                    })
                    .count();
            
            int totalSolicitudesSistema = todasLasSolicitudes.size();
            int totalSolicitudesProcesadas = solicitudesProcesadas.size();
            
            log.info("Total de solicitudes en el sistema: {}", totalSolicitudesSistema);
            log.info("Solicitudes procesadas encontradas: {}", totalSolicitudesProcesadas);
            log.info("Solicitudes no procesadas (solo Enviada): {}", solicitudesNoProcesadas);
            log.info("Solicitudes después de aplicar filtros: {}", respuesta.size());
            
            // Crear respuesta con metadatos
            java.util.Map<String, Object> respuestaCompleta = new java.util.HashMap<>();
            respuestaCompleta.put("total", respuesta.size());
            respuestaCompleta.put("total_solicitudes_sistema", totalSolicitudesSistema);
            respuestaCompleta.put("total_solicitudes_procesadas", totalSolicitudesProcesadas);
            respuestaCompleta.put("total_solicitudes_no_procesadas", (int) solicitudesNoProcesadas);
            respuestaCompleta.put("nota", "Este historial muestra solo solicitudes procesadas (que han sido revisadas o tienen más de un estado). Las solicitudes que solo tienen estado 'Enviada' no aparecen aquí.");
            respuestaCompleta.put("filtros_aplicados", java.util.Map.of(
                "periodo_academico", periodoAcademicoFiltro != null ? periodoAcademicoFiltro : "Todos",
                "tipo_solicitud", tipoSolicitud != null ? tipoSolicitud : "Todos",
                "estado_actual", estadoActual != null ? estadoActual : "Todos",
                "id_usuario", idUsuario != null ? idUsuario : "Todos"
            ));
            respuestaCompleta.put("solicitudes", respuesta);
            
            return ResponseEntity.ok(respuestaCompleta);
            
        } catch (Exception e) {
            log.error("Error al obtener historial completo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al obtener historial: " + e.getMessage()));
        }
    }
    
    /**
     * Exporta el historial completo de solicitudes a PDF con los mismos filtros aplicados
     * GET /api/solicitudes/historial/export/pdf
     * 
     * Este endpoint genera un PDF con el historial de solicitudes procesadas,
     * respetando los mismos filtros que el endpoint de historial.
     * 
     * Acceso permitido para: Decano, Funcionario, Coordinador, Secretario, Administrador
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param tipoSolicitud Tipo de solicitud opcional (PAZ_SALVO, ECAES, REINGRESO, HOMOLOGACION, CURSO_VERANO_PREINSCRIPCION, CURSO_VERANO_INSCRIPCION)
     * @param estadoActual Estado actual opcional para filtrar
     * @param idUsuario ID del usuario opcional para filtrar por estudiante
     * @return Archivo PDF con el historial de solicitudes
     */
    @GetMapping("/historial/export/pdf")
    @PreAuthorize("hasRole('Decano') or hasRole('Funcionario') or hasRole('Coordinador') or hasRole('Secretario') or hasRole('Administrador')")
    public ResponseEntity<byte[]> exportarHistorialPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        try {
            log.info("Exportando historial completo a PDF - periodoAcademico: {}, tipoSolicitud: {}, estadoActual: {}, idUsuario: {}", 
                    periodoAcademico, tipoSolicitud, estadoActual, idUsuario);
            
            // Convertir formato del período académico si viene en formato "Primer Período 2025" o "Segundo Período 2025"
            final String periodoAcademicoFiltro = convertirFormatoPeriodoAcademico(periodoAcademico) != null 
                    ? convertirFormatoPeriodoAcademico(periodoAcademico) 
                    : periodoAcademico;
            
            // Obtener todas las solicitudes
            List<Solicitud> todasLasSolicitudes = solicitudCU.listarSolicitudes();
            
            if (todasLasSolicitudes == null) {
                todasLasSolicitudes = new java.util.ArrayList<>();
            }
            
            // Filtrar solo las procesadas (misma lógica que el endpoint de historial)
            List<Solicitud> solicitudesProcesadas = todasLasSolicitudes.stream()
                    .filter(solicitud -> {
                        // Una solicitud está procesada si tiene más de un estado o su último estado no es "Enviada"
                        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
                            return false;
                        }
                        
                        // Verificar si está procesada
                        boolean procesada = solicitud.getEstadosSolicitud().size() > 1 || 
                                          !"Enviada".equals(solicitud.getEstadosSolicitud()
                                              .get(solicitud.getEstadosSolicitud().size() - 1)
                                              .getEstado_actual());
                        
                        if (!procesada) {
                            return false;
                        }
                        
                        // Filtrar por período académico
                        if (periodoAcademicoFiltro != null && !periodoAcademicoFiltro.trim().isEmpty()) {
                            if (solicitud.getPeriodo_academico() == null || 
                                !periodoAcademicoFiltro.trim().equals(solicitud.getPeriodo_academico())) {
                                return false;
                            }
                        }
                        
                        // Filtrar por tipo de solicitud
                        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                            String tipoDetectado = detectarTipoSolicitud(solicitud);
                            if (!tipoSolicitud.trim().equalsIgnoreCase(tipoDetectado)) {
                                return false;
                            }
                        }
                        
                        // Filtrar por estado actual
                        if (estadoActual != null && !estadoActual.trim().isEmpty()) {
                            String ultimoEstado = solicitud.getEstadosSolicitud()
                                    .get(solicitud.getEstadosSolicitud().size() - 1)
                                    .getEstado_actual();
                            if (!estadoActual.trim().equalsIgnoreCase(ultimoEstado)) {
                                return false;
                            }
                        }
                        
                        // Filtrar por usuario
                        if (idUsuario != null) {
                            if (solicitud.getObjUsuario() == null || 
                                !idUsuario.equals(solicitud.getObjUsuario().getId_usuario())) {
                                return false;
                            }
                        }
                        
                        return true;
                    })
                    .collect(Collectors.toList());
            
            // Generar PDF
            byte[] pdfBytes = generarPDFHistorial(solicitudesProcesadas, periodoAcademicoFiltro, tipoSolicitud, estadoActual, idUsuario);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            // Nombre del archivo con fecha
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String nombreArchivo = String.format("historial_solicitudes_%s.pdf", sdf.format(new Date()));
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            log.info("PDF del historial generado exitosamente: {} solicitudes", solicitudesProcesadas.size());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error al exportar historial a PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener historial completo de una solicitud específica
     * GET /api/solicitudes/{id}/historial
     * 
     * Este endpoint devuelve toda la información de una solicitud, incluyendo su historial completo de estados,
     * documentos, y toda la información relacionada.
     * 
     * Acceso permitido para: Decano, Funcionario, Coordinador, Secretario, Administrador
     * 
     * @param id ID de la solicitud
     * @return Información completa de la solicitud con historial de estados
     */
    @GetMapping("/{id}/historial")
    @PreAuthorize("hasRole('Decano') or hasRole('Funcionario') or hasRole('Coordinador') or hasRole('Secretario') or hasRole('Administrador')")
    public ResponseEntity<?> obtenerHistorialSolicitud(@PathVariable Integer id) {
        try {
            log.info("Obteniendo historial completo de solicitud con ID: {}", id);
            
            Solicitud solicitud = solicitudCU.obtenerSolicitudPorId(id);
            
            if (solicitud == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Solicitud no encontrada con ID: " + id));
            }
            
            SolicitudDTORespuesta dto = mapper.mappearDeSolicitudARespuesta(solicitud);
            java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
            
            // Información básica de la solicitud
            respuesta.put("id_solicitud", dto.getId_solicitud());
            respuesta.put("nombre_solicitud", dto.getNombre_solicitud());
            respuesta.put("periodo_academico", dto.getPeriodo_academico());
            respuesta.put("fecha_registro_solicitud", dto.getFecha_registro_solicitud());
            respuesta.put("fecha_ceremonia", solicitud.getFecha_ceremonia());
            
            // Tipo de solicitud
            respuesta.put("tipo_solicitud", detectarTipoSolicitud(solicitud));
            respuesta.put("tipo_solicitud_display", obtenerNombreTipoSolicitud(detectarTipoSolicitud(solicitud)));
            
            // Estado actual
            String estadoActualSolicitud = "Sin estado";
            java.util.Date fechaUltimoEstado = null;
            if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoEstado = 
                    solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1);
                estadoActualSolicitud = ultimoEstado.getEstado_actual();
                fechaUltimoEstado = ultimoEstado.getFecha_registro_estado();
            }
            respuesta.put("estado_actual", estadoActualSolicitud);
            respuesta.put("fecha_ultimo_estado", fechaUltimoEstado);
            
            // Información del usuario
            if (dto.getObjUsuario() != null) {
                java.util.Map<String, Object> usuarioInfo = new java.util.HashMap<>();
                usuarioInfo.put("id_usuario", dto.getObjUsuario().getId_usuario());
                usuarioInfo.put("nombre_completo", dto.getObjUsuario().getNombre_completo());
                usuarioInfo.put("codigo", dto.getObjUsuario().getCodigo());
                usuarioInfo.put("correo", dto.getObjUsuario().getCorreo());
                usuarioInfo.put("cedula", dto.getObjUsuario().getCedula());
                respuesta.put("usuario", usuarioInfo);
            }
            
            // Historial completo de estados (ordenados por fecha)
            List<java.util.Map<String, Object>> historialEstados = new java.util.ArrayList<>();
            if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                // Ordenar estados por fecha de registro
                List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud> estadosOrdenados = 
                    new java.util.ArrayList<>(solicitud.getEstadosSolicitud());
                estadosOrdenados.sort((e1, e2) -> {
                    if (e1.getFecha_registro_estado() == null && e2.getFecha_registro_estado() == null) return 0;
                    if (e1.getFecha_registro_estado() == null) return -1;
                    if (e2.getFecha_registro_estado() == null) return 1;
                    return e1.getFecha_registro_estado().compareTo(e2.getFecha_registro_estado());
                });
                
                for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud estado : estadosOrdenados) {
                    java.util.Map<String, Object> estadoInfo = new java.util.HashMap<>();
                    estadoInfo.put("id_estado", estado.getId_estado());
                    estadoInfo.put("estado_actual", estado.getEstado_actual());
                    estadoInfo.put("fecha_registro_estado", estado.getFecha_registro_estado());
                    estadoInfo.put("comentario", estado.getComentario());
                    historialEstados.add(estadoInfo);
                }
            }
            respuesta.put("historial_estados", historialEstados);
            respuesta.put("total_estados", historialEstados.size());
            
            // Información de documentos
            respuesta.put("documentos", dto.getDocumentos());
            respuesta.put("total_documentos", dto.getDocumentos() != null ? dto.getDocumentos().size() : 0);
            
            // Información adicional según el tipo de solicitud
            java.util.Map<String, Object> informacionAdicional = new java.util.HashMap<>();
            if (solicitud instanceof co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo) {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo pazSalvo = 
                    (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo) solicitud;
                informacionAdicional.put("titulo_trabajo_grado", pazSalvo.getTitulo_trabajo_grado());
                informacionAdicional.put("director_trabajo_grado", pazSalvo.getDirector_trabajo_grado());
            }
            respuesta.put("informacion_adicional", informacionAdicional);
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error al obtener historial de solicitud {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al obtener historial de solicitud: " + e.getMessage()));
        }
    }
    
    /**
     * Detecta el tipo de solicitud basándose en la instancia de la clase
     */
    private String detectarTipoSolicitud(Solicitud solicitud) {
        String nombreClase = solicitud.getClass().getSimpleName();
        
        if (nombreClase.contains("PazYSalvo") || nombreClase.contains("PazYSalvo")) {
            return "PAZ_SALVO";
        } else if (nombreClase.contains("Ecaes") || nombreClase.contains("ECAES")) {
            return "ECAES";
        } else if (nombreClase.contains("Reingreso") || nombreClase.contains("REINGRESO")) {
            return "REINGRESO";
        } else if (nombreClase.contains("Homologacion") || nombreClase.contains("HOMOLOGACION")) {
            return "HOMOLOGACION";
        } else if (nombreClase.contains("CursoVeranoPreinscripcion") || nombreClase.contains("Preinscripcion")) {
            return "CURSO_VERANO_PREINSCRIPCION";
        } else if (nombreClase.contains("CursoVeranoInscripcion") || nombreClase.contains("Inscripcion")) {
            return "CURSO_VERANO_INSCRIPCION";
        }
        
        // Fallback: intentar detectar por nombre
        String nombreSolicitud = solicitud.getNombre_solicitud() != null ? solicitud.getNombre_solicitud().toLowerCase() : "";
        if (nombreSolicitud.contains("paz") || nombreSolicitud.contains("salvo")) {
            return "PAZ_SALVO";
        } else if (nombreSolicitud.contains("ecaes")) {
            return "ECAES";
        } else if (nombreSolicitud.contains("reingreso")) {
            return "REINGRESO";
        } else if (nombreSolicitud.contains("homologacion")) {
            return "HOMOLOGACION";
        } else if (nombreSolicitud.contains("preinscripcion")) {
            return "CURSO_VERANO_PREINSCRIPCION";
        } else if (nombreSolicitud.contains("inscripcion") && nombreSolicitud.contains("curso")) {
            return "CURSO_VERANO_INSCRIPCION";
        }
        
        return "DESCONOCIDO";
    }
    
    /**
     * Obtiene el nombre legible del tipo de solicitud
     */
    private String obtenerNombreTipoSolicitud(String tipo) {
        switch (tipo.toUpperCase()) {
            case "PAZ_SALVO":
                return "Paz y Salvo Académico";
            case "ECAES":
                return "ECAES";
            case "REINGRESO":
                return "Reingreso";
            case "HOMOLOGACION":
                return "Homologación";
            case "CURSO_VERANO_PREINSCRIPCION":
                return "Curso Intersemestral - Preinscripción";
            case "CURSO_VERANO_INSCRIPCION":
                return "Curso Intersemestral - Inscripción";
            default:
                return "Desconocido";
        }
    }
    
    /**
     * Convierte el formato del período académico de "Primer Período 2025" o "Segundo Período 2025" 
     * al formato "2025-1" o "2025-2"
     * 
     * @param periodoAcademico Período académico en formato legible o formato YYYY-P
     * @return Período académico en formato YYYY-P, o null si no se puede convertir
     */
    private String convertirFormatoPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            return null;
        }
        
        String periodo = periodoAcademico.trim();
        
        // Si ya está en formato YYYY-P, retornarlo tal cual
        if (periodo.matches("\\d{4}-[12]")) {
            return periodo;
        }
        
        // Intentar convertir desde formato "Primer Período 2025" o "Segundo Período 2025"
        // Patrón: "Primer Período 2025" o "Segundo Período 2025"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(Primer|Segundo)\\s+Per[ií]odo\\s+(\\d{4})", 
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(periodo);
        
        if (matcher.find()) {
            String numeroPeriodo = matcher.group(1).equalsIgnoreCase("Primer") ? "1" : "2";
            String año = matcher.group(2);
            String resultado = año + "-" + numeroPeriodo;
            
            // Validar que el período existe en el enum
            try {
                PeriodoAcademicoEnum.fromValor(resultado);
                return resultado;
            } catch (IllegalArgumentException e) {
                log.warn("Período académico '{}' no es válido", resultado);
                return null;
            }
        }
        
        // Si no coincide con ningún patrón, intentar buscar en el enum por descripción
        for (PeriodoAcademicoEnum periodoEnum : PeriodoAcademicoEnum.values()) {
            if (periodoEnum.getDescripcion().equalsIgnoreCase(periodo)) {
                return periodoEnum.getValor();
            }
        }
        
        log.warn("No se pudo convertir el período académico '{}' al formato YYYY-P", periodo);
        return null;
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
     * Genera un PDF con el historial de solicitudes procesadas
     * 
     * @param solicitudes Lista de solicitudes procesadas
     * @param periodoAcademico Período académico filtrado (puede ser null)
     * @param tipoSolicitud Tipo de solicitud filtrado (puede ser null)
     * @param estadoActual Estado actual filtrado (puede ser null)
     * @param idUsuario ID de usuario filtrado (puede ser null)
     * @return Array de bytes del PDF generado
     */
    private byte[] generarPDFHistorial(List<Solicitud> solicitudes, String periodoAcademico, 
                                      String tipoSolicitud, String estadoActual, Integer idUsuario) {
        log.debug("Iniciando la generación del PDF del historial de solicitudes...");
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate()); // Horizontal para tabla
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
            
            // Fecha de generación
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph(
                "Fecha de generación: " + sdf.format(new Date()), dateFont);
            fecha.setSpacingAfter(10);
            document.add(fecha);
            
            // Información de filtros aplicados
            if (periodoAcademico != null || tipoSolicitud != null || estadoActual != null || idUsuario != null) {
                com.itextpdf.text.Font filterFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.ITALIC);
                StringBuilder filtros = new StringBuilder("Filtros aplicados: ");
                boolean tieneFiltros = false;
                
                if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                    filtros.append("Período: ").append(periodoAcademico);
                    tieneFiltros = true;
                }
                if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                    if (tieneFiltros) filtros.append(" | ");
                    filtros.append("Tipo: ").append(obtenerNombreTipoSolicitud(tipoSolicitud));
                    tieneFiltros = true;
                }
                if (estadoActual != null && !estadoActual.trim().isEmpty()) {
                    if (tieneFiltros) filtros.append(" | ");
                    filtros.append("Estado: ").append(estadoActual);
                    tieneFiltros = true;
                }
                if (idUsuario != null) {
                    if (tieneFiltros) filtros.append(" | ");
                    filtros.append("Usuario ID: ").append(idUsuario);
                }
                
                com.itextpdf.text.Paragraph filtrosPara = new com.itextpdf.text.Paragraph(
                    filtros.toString(), filterFont);
                filtrosPara.setSpacingAfter(15);
                document.add(filtrosPara);
            }
            
            // Resumen
            com.itextpdf.text.Font summaryFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph resumen = new com.itextpdf.text.Paragraph(
                String.format("Total de solicitudes procesadas: %d", solicitudes.size()), summaryFont);
            resumen.setSpacingAfter(15);
            document.add(resumen);
            
            // Crear tabla
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.5f, 1f, 1.5f, 1.5f});
            
            // Encabezados de la tabla
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
            
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("ID", headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("Solicitud", headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("Tipo", headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("Estudiante", headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("Período", headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("Estado", headerFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase("Fecha Registro", headerFont)));
            
            // Datos de la tabla
            com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            for (Solicitud solicitud : solicitudes) {
                SolicitudDTORespuesta dto = mapper.mappearDeSolicitudARespuesta(solicitud);
                
                // ID
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(
                        String.valueOf(dto.getId_solicitud()), cellFont)));
                
                // Nombre de la solicitud (truncar si es muy largo)
                String nombreSolicitud = dto.getNombre_solicitud() != null 
                    ? dto.getNombre_solicitud() : "Sin nombre";
                if (nombreSolicitud.length() > 50) {
                    nombreSolicitud = nombreSolicitud.substring(0, 47) + "...";
                }
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(nombreSolicitud, cellFont)));
                
                // Tipo
                String tipo = obtenerNombreTipoSolicitud(detectarTipoSolicitud(solicitud));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(tipo, cellFont)));
                
                // Estudiante
                String estudiante = "N/A";
                if (dto.getObjUsuario() != null) {
                    estudiante = dto.getObjUsuario().getNombre_completo() != null 
                        ? dto.getObjUsuario().getNombre_completo() 
                        : dto.getObjUsuario().getCodigo() != null 
                            ? dto.getObjUsuario().getCodigo() 
                            : "N/A";
                }
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(estudiante, cellFont)));
                
                // Período académico
                String periodo = dto.getPeriodo_academico() != null 
                    ? dto.getPeriodo_academico() : "N/A";
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(periodo, cellFont)));
                
                // Estado actual
                String estado = "Sin estado";
                if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                    estado = solicitud.getEstadosSolicitud()
                        .get(solicitud.getEstadosSolicitud().size() - 1)
                        .getEstado_actual();
                }
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(estado, cellFont)));
                
                // Fecha de registro
                String fechaRegistro = "N/A";
                if (dto.getFecha_registro_solicitud() != null) {
                    fechaRegistro = dateFormat.format(dto.getFecha_registro_solicitud());
                }
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(fechaRegistro, cellFont)));
            }
            
            document.add(table);
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar el PDF del historial: {}", e.getMessage(), e);
            
            // Generar PDF de error
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                
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
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    log.error("Error al cerrar ByteArrayOutputStream: {}", e.getMessage());
                }
            }
        }
    }

}
