package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;


import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
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
            // Obtener todas las solicitudes
            List<Solicitud> todasLasSolicitudes = solicitudCU.listarSolicitudes();
            
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
                        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                            if (solicitud.getPeriodo_academico() == null || 
                                !periodoAcademico.trim().equals(solicitud.getPeriodo_academico())) {
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
            
            // Crear respuesta con metadatos
            java.util.Map<String, Object> respuestaCompleta = new java.util.HashMap<>();
            respuestaCompleta.put("total", respuesta.size());
            respuestaCompleta.put("filtros_aplicados", java.util.Map.of(
                "periodo_academico", periodoAcademico != null ? periodoAcademico : "Todos",
                "tipo_solicitud", tipoSolicitud != null ? tipoSolicitud : "Todos",
                "estado_actual", estadoActual != null ? estadoActual : "Todos",
                "id_usuario", idUsuario != null ? idUsuario : "Todos"
            ));
            respuestaCompleta.put("solicitudes", respuesta);
            
            return ResponseEntity.ok(respuestaCompleta);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al obtener historial: " + e.getMessage()));
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


}
