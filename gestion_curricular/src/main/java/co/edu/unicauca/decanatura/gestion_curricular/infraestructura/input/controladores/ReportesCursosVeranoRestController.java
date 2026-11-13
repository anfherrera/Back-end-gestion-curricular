package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import lombok.RequiredArgsConstructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesCursosVeranoCUIntPort;

/**
 * Reportes de cursos de verano: listado y exportación de solicitudes, dashboard y seguimiento del estudiante.
 * Ruta base compartida con los demás controladores de cursos de verano.
 */
@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
public class ReportesCursosVeranoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GenerarReportesCursosVeranoCUIntPort generadorExcel;

    /**
     * Obtener todas las solicitudes de cursos intersemestrales para funcionarios y coordinadores
     * GET /api/cursos-intersemestrales/solicitudes
     * 
     * @param idMateria Filtro por ID de materia (opcional)
     * @param periodoAcademico Filtro por período académico (opcional, formato: "YYYY-P", ej: "2025-2")
     * @param idCurso Filtro por ID de curso (opcional)
     * @param estado Filtro por estado de solicitud (opcional)
     * @param fechaInicio Filtro por fecha de inicio (opcional)
     * @param fechaFin Filtro por fecha de fin (opcional)
     */
    @GetMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasLasSolicitudes(
            @RequestParam(value = "idMateria", required = false) Integer idMateria,
            @RequestParam(value = "periodoAcademico", required = false) String periodoAcademico,
            @RequestParam(value = "idCurso", required = false) Integer idCurso,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        List<Map<String, Object>> solicitudesFormateadas = formatearSolicitudesCursos(idMateria, idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
        
        return ResponseEntity.ok(solicitudesFormateadas);
    }

    /**
     * Exportar solicitudes de cursos intersemestrales a Excel con filtros opcionales
     * GET /api/cursos-intersemestrales/solicitudes/export/excel
     * 
     * @param idCurso Filtro por ID de curso (opcional)
     * @param estado Filtro por estado de solicitud (opcional)
     * @param periodoAcademico Filtro por período académico (opcional, formato: "YYYY-P", ej: "2025-2")
     * @param fechaInicio Filtro por fecha de inicio (opcional)
     * @param fechaFin Filtro por fecha de fin (opcional)
     * @return Archivo Excel con las solicitudes filtradas
     */
    @GetMapping("/solicitudes/export/excel")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<byte[]> exportarSolicitudesExcel(
            @RequestParam(value = "idCurso", required = false) Integer idCurso,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "periodoAcademico", required = false) String periodoAcademico,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        byte[] excelBytes = generadorExcel.generarExcelSolicitudes(idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String nombreArchivo = "solicitudes_cursos_intersemestrales_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.formulario(nombreArchivo));
        
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * Preinscripciones e inscripciones de cursos de verano con los filtros dados, en el formato
     * que usan el listado y la exportación a Excel.
     *
     * @param idMateria filtro por materia (null o 0 = sin filtro)
     */
    private List<Map<String, Object>> formatearSolicitudesCursos(Integer idMateria, Integer idCurso, String estado,
            String periodoAcademico, Date fechaInicio, Date fechaFin) {
        String periodo = periodoAcademico == null ? "" : periodoAcademico.trim();
        String periodoFiltro = periodo.isEmpty() || "todos".equalsIgnoreCase(periodo)
                ? null : SoporteCursosVerano.normalizarPeriodoAcademico(periodo);
        List<Solicitud> todasLasSolicitudes = solicitudCU.listarSolicitudesParaReporte(idCurso, estado,
                periodoFiltro, fechaInicio, fechaFin);

        return co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ReporteSolicitudesVeranoMapper.formatear(todasLasSolicitudes, idMateria);
    }

    /**
     * Obtener estadisticas del dashboard de cursos intersemestrales
     * GET /api/cursos-intersemestrales/dashboard/estadisticas
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/dashboard/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboard() {
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // 1. Contar cursos activos (estados: Publicado, Preinscripcion, Inscripcion)
        List<CursoOfertadoVerano> todosLosCursos = cursoCU.listarTodos();
        int cursosActivos = 0;
        int cursosGestionados = 0;
        
        for (CursoOfertadoVerano curso : todosLosCursos) {
            if (curso.getEstadosCursoOfertados() != null && !curso.getEstadosCursoOfertados().isEmpty()) {
                String estadoActual = curso.getEstadosCursoOfertados()
                    .get(curso.getEstadosCursoOfertados().size() - 1)
                    .getEstado_actual();
                
                // Cursos activos: Publicado, Preinscripcion, Inscripcion
                if ("Publicado".equals(estadoActual) || 
                    "Preinscripcion".equals(estadoActual) || 
                    "Inscripcion".equals(estadoActual)) {
                    cursosActivos++;
                }
                
                // Cursos gestionados: cualquier curso que tenga al menos un estado
                cursosGestionados++;
            }
        }
        
        // 2 y 3. Contar preinscripciones e inscripciones con una sola consulta
        int totalPreinscripciones = 0;
        int totalInscripciones = 0;
        for (Solicitud solicitud : solicitudCU.listarSolicitudesParaReporte(null, null, null, null, null)) {
            if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                totalPreinscripciones++;
            } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                totalInscripciones++;
            }
        }
        
        // 4. Calcular progreso de gestion
        int totalCursos = todosLosCursos.size();
        
        int cursosGestionadosValido = Math.max(0, cursosGestionados); // No permitir negativos
        int totalCursosValido = Math.max(0, totalCursos); // No permitir negativos
        
        // Calcular porcentaje asegurando que esté entre 0 y 100
        double porcentajeProgreso = 0;
        if (totalCursosValido > 0) {
            porcentajeProgreso = (cursosGestionadosValido * 100.0 / totalCursosValido);
            porcentajeProgreso = Math.max(0, Math.min(100, porcentajeProgreso)); // Asegurar entre 0 y 100
        }
        
        // Construir respuesta con valores validados
        estadisticas.put("cursosActivos", Math.max(0, cursosActivos));
        estadisticas.put("totalPreinscripciones", Math.max(0, totalPreinscripciones));
        estadisticas.put("totalInscripciones", Math.max(0, totalInscripciones));
        estadisticas.put("cursosGestionados", cursosGestionadosValido);
        estadisticas.put("totalCursos", totalCursosValido);
        estadisticas.put("porcentajeProgreso", Math.round(porcentajeProgreso));
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtener seguimiento completo de actividades del usuario (preinscripciones + inscripciones)
     * GET /api/cursos-intersemestrales/seguimiento/{idUsuario}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#idUsuario)")
    @GetMapping("/seguimiento/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerSeguimientoActividades(@PathVariable Integer idUsuario) {
        
        // Obtener preinscripciones reales de la base de datos
        List<Map<String, Object>> preinscripciones = new ArrayList<>();
        
        try {
            // Obtener preinscripciones reales de la base de datos
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                Map<String, Object> preinscripcionMap = new HashMap<>();
                preinscripcionMap.put("id", preinscripcion.getId_solicitud());
                preinscripcionMap.put("fecha", preinscripcion.getFecha_registro_solicitud());
                preinscripcionMap.put("estado", preinscripcion.estadoActual("Enviado"));
                
                // Determinar el tipo correcto de solicitud
                String tipoSolicitud = "Preinscripcion";
                
                // Deteccion para solicitudes de curso nuevo
                boolean esSolicitudCursoNuevo = false;
                
                if (preinscripcion.getObservacion() != null) {
                    String obs = preinscripcion.getObservacion().toLowerCase();
                    if (obs.contains("solicitud de apertura") || obs.contains("curso:")) {
                        esSolicitudCursoNuevo = true;
                    }
                }
                
                if (!esSolicitudCursoNuevo && preinscripcion.getNombre_solicitud() != null) {
                    String nombre = preinscripcion.getNombre_solicitud().toLowerCase();
                    if (nombre.contains("solicitud de apertura") || nombre.contains("curso:")) {
                        esSolicitudCursoNuevo = true;
                    }
                }
                
                if (esSolicitudCursoNuevo) {
                    tipoSolicitud = "Solicitud de Curso Nuevo";
                }
                preinscripcionMap.put("tipo", tipoSolicitud);
                
                // Obtener el nombre del curso correctamente
                String nombreCurso = "Curso no disponible";
                if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                    // Para cursos ya existentes
                    nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                } else {
                    // Para solicitudes de curso nuevo, obtener el nombre del campo observacion
                    if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().isEmpty()) {
                        // Extraer solo el nombre del curso de la observacion
                        String observacion = preinscripcion.getObservacion();
                        if (observacion.contains(": ")) {
                            nombreCurso = observacion.split(": ")[1].trim();
                        } else {
                            nombreCurso = observacion;
                        }
                    } else if (preinscripcion.getNombre_solicitud() != null && 
                              preinscripcion.getNombre_solicitud().contains(":")) {
                        // Fallback: extraer del nombre de la solicitud
                        nombreCurso = preinscripcion.getNombre_solicitud().split(":")[1].trim();
                    }
                }
                preinscripcionMap.put("curso", nombreCurso);
                preinscripcionMap.put("estudianteId", idUsuario);
                preinscripcionMap.put("cursoId", preinscripcion.getObjCursoOfertadoVerano() != null 
                    ? preinscripcion.getObjCursoOfertadoVerano().getId_curso() : null);
                
                // Agregar informacion del estado del curso y acciones disponibles
                String estadoCurso = "No disponible";
                List<String> accionesDisponibles = new ArrayList<>();
                
                if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                    // Para cursos existentes, obtener el estado del curso
                    CursoOfertadoVerano curso = preinscripcion.getObjCursoOfertadoVerano();
                    if (curso.getEstadosCursoOfertados() != null && !curso.getEstadosCursoOfertados().isEmpty()) {
                        estadoCurso = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                    }
                    
                    // Determinar acciones disponibles basado en el estado de la preinscripcion y del curso
                    String estadoPreinscripcion = preinscripcion.estadoActual("Enviado");
                    
                    if (EstadoSolicitudCursoVerano.APROBADO.texto().equals(estadoPreinscripcion)) {
                        if ("Inscripcion".equals(estadoCurso)) {
                            accionesDisponibles.add("proceder_inscripcion");
                        } else {
                            accionesDisponibles.add("esperando_inscripcion");
                        }
                    } else if (EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoPreinscripcion)) {
                        accionesDisponibles.add("esperando_aprobacion");
                    } else if ("Rechazada".equals(estadoPreinscripcion)) { // BUG-8: se guarda "Rechazado", nunca coincide
                        accionesDisponibles.add("revisar_motivo_rechazo");
                    }
                } else {
                    // Para solicitudes de curso nuevo
                    String estadoPreinscripcion = preinscripcion.estadoActual("Enviado");
                    
                    if (EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoPreinscripcion)) {
                        accionesDisponibles.add("esperando_aprobacion_curso");
                    } else if (EstadoSolicitudCursoVerano.APROBADO.texto().equals(estadoPreinscripcion)) {
                        accionesDisponibles.add("curso_aprobado_esperando_apertura");
                    }
                }
                
                preinscripcionMap.put("estadoCurso", estadoCurso);
                preinscripcionMap.put("accionesDisponibles", accionesDisponibles);
                
                preinscripciones.add(preinscripcionMap);
            }
            
            
        } catch (Exception e) {
            // En caso de error, devolver lista vacia en lugar de datos simulados
            preinscripciones = new ArrayList<>();
        }
        
        // Obtener inscripciones reales del usuario
        List<Map<String, Object>> inscripcionesUsuario = new ArrayList<>();
        
        try {
            // Obtener inscripciones reales de la base de datos
            List<SolicitudCursoVeranoIncripcion> inscripcionesReales = solicitudCU.buscarInscripcionesPorUsuario(idUsuario);
            
            for (SolicitudCursoVeranoIncripcion inscripcion : inscripcionesReales) {
                Map<String, Object> inscripcionMap = new HashMap<>();
                inscripcionMap.put("id", inscripcion.getId_solicitud());
                inscripcionMap.put("fecha", inscripcion.getFecha_registro_solicitud());
                
                // Obtener el estado actual de la inscripcion
                String estadoInscripcion = inscripcion.estadoActual(EstadoSolicitudCursoVerano.ENVIADA.texto());
                inscripcionMap.put("estado", estadoInscripcion);
                
                inscripcionMap.put("tipoSolicitud", "Inscripcion");
                inscripcionMap.put("nombreSolicitud", inscripcion.getNombre_solicitud());
                
                // Informacion del curso - solo el nombre para evitar [object Object]
                String nombreCurso = "Curso no disponible";
                Integer cursoId = null;
                if (inscripcion.getObjCursoOfertadoVerano() != null && 
                    inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                    nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                    cursoId = inscripcion.getObjCursoOfertadoVerano().getId_curso();
                }
                inscripcionMap.put("curso", nombreCurso);
                inscripcionMap.put("cursoId", cursoId);
                inscripcionMap.put("estudianteId", idUsuario);
                
                // Mapear estadoCurso basado en el estado de la inscripcion
                String estadoCurso = "PENDIENTE";
                if (EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoInscripcion)) {
                    estadoCurso = "INSCRITO";
                } else if (EstadoSolicitudCursoVerano.INSCRIPCION_COMPLETADA.texto().equals(estadoInscripcion)) {
                    estadoCurso = "ACTIVO";
                } else if ("Curso_Aprobado".equals(estadoInscripcion)) {
                    estadoCurso = "APROBADO";
                } else if ("Curso_Finalizado".equals(estadoInscripcion)) {
                    estadoCurso = "FINALIZADO";
                } else if (EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoInscripcion)) {
                    estadoCurso = "PENDIENTE";
                } else if (EstadoSolicitudCursoVerano.PAGO_RECHAZADO.texto().equals(estadoInscripcion)) {
                    estadoCurso = "RECHAZADO";
                }
                inscripcionMap.put("estadoCurso", estadoCurso);
                
                // Determinar acciones disponibles basadas en el estado
                List<String> accionesDisponibles = new ArrayList<>();
                if (EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoInscripcion)) {
                    accionesDisponibles.add("esperando_validacion_pago");
                } else if (EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoInscripcion)) {
                    accionesDisponibles.add("inscripcion_aceptada");
                } else if (EstadoSolicitudCursoVerano.PAGO_RECHAZADO.texto().equals(estadoInscripcion)) {
                    accionesDisponibles.add("pago_rechazado");
                }
                
                inscripcionMap.put("accionesDisponibles", accionesDisponibles);
                
                inscripcionesUsuario.add(inscripcionMap);
            }
            
            
        } catch (Exception e) {
            // En caso de error, devolver lista vacia
            inscripcionesUsuario = new ArrayList<>();
        }
        
        // Crear respuesta con estadisticas
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("preinscripciones", preinscripciones);
        respuesta.put("inscripciones", inscripcionesUsuario);
        respuesta.put("totalPreinscripciones", preinscripciones.size());
        respuesta.put("totalInscripciones", inscripcionesUsuario.size());
        respuesta.put("totalActividades", preinscripciones.size() + inscripcionesUsuario.size());
        return ResponseEntity.ok(respuesta);
    }

}
