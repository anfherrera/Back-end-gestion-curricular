package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadoSolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> obtenerHistorialCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String estadoActual,
            @RequestParam(required = false) Integer idUsuario) {
        
        try {
            log.debug("Obteniendo historial completo - periodoAcademico: {}, tipoSolicitud: {}, estadoActual: {}, idUsuario: {}", 
                    periodoAcademico, tipoSolicitud, estadoActual, idUsuario);
            
            // Obtener todas las solicitudes con sus relaciones
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAllWithJoins();
            
            log.debug("Total de solicitudes encontradas: {}", todasLasSolicitudes.size());
            
            // Filtrar solicitudes procesadas (que tienen al menos un estado)
            List<SolicitudEntity> solicitudesProcesadas = todasLasSolicitudes.stream()
                    .filter(s -> s.getEstadosSolicitud() != null && !s.getEstadosSolicitud().isEmpty())
                    .collect(Collectors.toList());
            
            log.debug("Solicitudes procesadas encontradas: {}", solicitudesProcesadas.size());
            
            // Aplicar filtros adicionales
            List<SolicitudEntity> solicitudesFiltradas = solicitudesProcesadas.stream()
                    .filter(s -> {
                        // Filtro por período académico
                        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                            if (s.getPeriodo_academico() == null || !s.getPeriodo_academico().equals(periodoAcademico.trim())) {
                                return false;
                            }
                        }
                        
                        // Filtro por tipo de solicitud
                        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                            String nombreSolicitud = s.getNombre_solicitud() != null ? s.getNombre_solicitud().toLowerCase() : "";
                            String tipoFiltro = tipoSolicitud.trim().toLowerCase();
                            if (!nombreSolicitud.contains(tipoFiltro)) {
                                return false;
                            }
                        }
                        
                        // Filtro por estado actual
                        if (estadoActual != null && !estadoActual.trim().isEmpty()) {
                            if (s.getEstadosSolicitud() == null || s.getEstadosSolicitud().isEmpty()) {
                                return false;
                            }
                            String ultimoEstado = s.getEstadosSolicitud().stream()
                                    .max(Comparator.comparing(e -> e.getFecha_registro_estado()))
                                    .map(e -> e.getEstado_actual())
                                    .orElse("");
                            if (!ultimoEstado.equalsIgnoreCase(estadoActual.trim())) {
                                return false;
                            }
                        }
                        
                        // Filtro por usuario
                        if (idUsuario != null) {
                            if (s.getObjUsuario() == null || !s.getObjUsuario().getId_usuario().equals(idUsuario)) {
                                return false;
                            }
                        }
                        
                        return true;
                    })
                    .collect(Collectors.toList());
            
            log.debug("Solicitudes después de aplicar filtros: {}", solicitudesFiltradas.size());
            
            // Convertir a DTOs directamente desde las entidades (ya tienen los datos cargados)
            List<SolicitudDTORespuesta> solicitudesDTO = solicitudesFiltradas.stream()
                    .map(s -> {
                        try {
                            SolicitudDTORespuesta dto = new SolicitudDTORespuesta();
                            dto.setId_solicitud(s.getId_solicitud());
                            dto.setNombre_solicitud(s.getNombre_solicitud());
                            dto.setPeriodo_academico(s.getPeriodo_academico());
                            dto.setFecha_registro_solicitud(s.getFecha_registro_solicitud());
                            
                            // Mapear estados directamente desde Entity
                            if (s.getEstadosSolicitud() != null && !s.getEstadosSolicitud().isEmpty()) {
                                List<EstadoSolicitudDTORespuesta> estadosDTO = s.getEstadosSolicitud().stream()
                                        .map(estadoEntity -> {
                                            EstadoSolicitudDTORespuesta estadoDTO = new EstadoSolicitudDTORespuesta();
                                            estadoDTO.setId_estado(estadoEntity.getId_estado());
                                            estadoDTO.setEstado_actual(estadoEntity.getEstado_actual());
                                            estadoDTO.setFecha_registro_estado(estadoEntity.getFecha_registro_estado());
                                            estadoDTO.setComentario(estadoEntity.getComentario());
                                            return estadoDTO;
                                        })
                                        .collect(Collectors.toList());
                                dto.setEstadosSolicitud(estadosDTO);
                            } else {
                                dto.setEstadosSolicitud(new ArrayList<>());
                            }
                            
                            // Mapear usuario directamente desde Entity
                            if (s.getObjUsuario() != null) {
                                UsuarioEntity usuarioEntity = s.getObjUsuario();
                                UsuarioDTORespuesta usuarioDTO = new UsuarioDTORespuesta();
                                usuarioDTO.setId_usuario(usuarioEntity.getId_usuario());
                                usuarioDTO.setNombre_completo(usuarioEntity.getNombre_completo());
                                usuarioDTO.setCodigo(usuarioEntity.getCodigo());
                                usuarioDTO.setCedula(usuarioEntity.getCedula());
                                usuarioDTO.setCorreo(usuarioEntity.getCorreo());
                                usuarioDTO.setEstado_usuario(usuarioEntity.isEstado_usuario());
                                // Rol y Programa se pueden mapear si están cargados, pero por ahora los dejamos null
                                dto.setObjUsuario(usuarioDTO);
                            }
                            
                            // Documentos (opcional, puede ser null)
                            dto.setDocumentos(new ArrayList<>());
                            
                            return dto;
                        } catch (Exception e) {
                            log.warn("Error al mapear solicitud {}: {}", s.getId_solicitud(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // Construir respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("solicitudes", solicitudesDTO);
            respuesta.put("total", solicitudesDTO.size());
            respuesta.put("total_solicitudes_sistema", todasLasSolicitudes.size());
            respuesta.put("total_solicitudes_procesadas", solicitudesProcesadas.size());
            respuesta.put("total_solicitudes_no_procesadas", todasLasSolicitudes.size() - solicitudesProcesadas.size());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error al obtener historial completo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
