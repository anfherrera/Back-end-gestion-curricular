package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
            log.debug("Obteniendo historial completo - periodoAcademico: {}, tipoSolicitud: {}, estadoActual: {}, idUsuario: {}", 
                    periodoAcademico, tipoSolicitud, estadoActual, idUsuario);
            
            // Obtener todas las solicitudes usando el caso de uso (más confiable)
            // Esto usa el mismo método que otros endpoints que funcionan
            List<Solicitud> todasLasSolicitudesDominio = solicitudCU.listarSolicitudes();
            log.info("Total de solicitudes encontradas (usando CU): {}", todasLasSolicitudesDominio.size());
            
            if (todasLasSolicitudesDominio == null || todasLasSolicitudesDominio.isEmpty()) {
                log.warn("No se encontraron solicitudes en la base de datos usando el caso de uso");
                Map<String, Object> respuestaVacia = new HashMap<>();
                respuestaVacia.put("solicitudes", new ArrayList<>());
                respuestaVacia.put("total", 0);
                respuestaVacia.put("total_solicitudes_sistema", 0);
                respuestaVacia.put("total_solicitudes_procesadas", 0);
                respuestaVacia.put("total_solicitudes_no_procesadas", 0);
                respuestaVacia.put("nota", "No se encontraron solicitudes en la base de datos. Verifica que los datos se hayan cargado correctamente.");
                return ResponseEntity.ok(respuestaVacia);
            }
            
            // Convertir a entidades para aplicar filtros (o trabajar directamente con los modelos de dominio)
            // Por ahora, vamos a convertir los modelos de dominio a DTOs directamente
            List<SolicitudDTORespuesta> todasLasSolicitudesDTO = todasLasSolicitudesDominio.stream()
                    .map(mapper::mappearDeSolicitudARespuesta)
                    .collect(Collectors.toList());
            
            log.info("Total de DTOs creados: {}", todasLasSolicitudesDTO.size());
            
            // Contar solicitudes con estados (para estadísticas)
            long totalConEstados = todasLasSolicitudesDTO.stream()
                    .filter(s -> s.getEstadosSolicitud() != null && !s.getEstadosSolicitud().isEmpty())
                    .count();
            
            log.info("Solicitudes con estados: {} de {}", totalConEstados, todasLasSolicitudesDTO.size());
            
            // Aplicar filtros adicionales a los DTOs
            List<SolicitudDTORespuesta> solicitudesFiltradas = todasLasSolicitudesDTO.stream()
                    .filter(s -> {
                        // Filtro por período académico
                        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                            if (s.getPeriodo_academico() == null || !s.getPeriodo_academico().equals(periodoAcademico.trim())) {
                                return false;
                            }
                        }
                        
                        // Filtro por tipo de solicitud (más flexible)
                        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
                            String nombreSolicitud = s.getNombre_solicitud() != null ? s.getNombre_solicitud().toLowerCase() : "";
                            String tipoFiltro = tipoSolicitud.trim().toLowerCase();
                            
                            // Normalizar nombres comunes
                            String tipoNormalizado = tipoFiltro
                                    .replace("académico", "")
                                    .replace("academico", "")
                                    .trim();
                            
                            // Mapear nombres del frontend a nombres en BD
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
                                // Búsqueda genérica
                                coincide = nombreSolicitud.contains(tipoNormalizado);
                            }
                            
                            if (!coincide) {
                                return false;
                            }
                        }
                        
                        // Filtro por estado actual
                        if (estadoActual != null && !estadoActual.trim().isEmpty()) {
                            if (s.getEstadosSolicitud() == null || s.getEstadosSolicitud().isEmpty()) {
                                return false;
                            }
                            // El último estado es el último elemento del array (ya ordenado por fecha)
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
            
            log.info("Solicitudes después de aplicar filtros: {} (filtros: periodoAcademico={}, tipoSolicitud={}, estadoActual={}, idUsuario={})", 
                    solicitudesFiltradas.size(), periodoAcademico, tipoSolicitud, estadoActual, idUsuario);
            
            // Construir respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("solicitudes", solicitudesFiltradas);
            respuesta.put("total", solicitudesFiltradas.size());
            respuesta.put("total_solicitudes_sistema", todasLasSolicitudesDTO.size());
            respuesta.put("total_solicitudes_procesadas", totalConEstados);
            respuesta.put("total_solicitudes_no_procesadas", todasLasSolicitudesDTO.size() - totalConEstados);
            
            log.info("Respuesta final - Total filtrado: {}, Total sistema: {}, Procesadas: {}", 
                    solicitudesFiltradas.size(), todasLasSolicitudesDTO.size(), totalConEstados);
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error al obtener historial completo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
