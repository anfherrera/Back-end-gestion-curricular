package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesCursosVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.ActividadInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.ActividadPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.Dashboard;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.Seguimiento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.SolicitudReporte;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ReporteSolicitudesVeranoMapper;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

/**
 * Reportes de cursos de verano: listado y exportación de solicitudes, dashboard y seguimiento del estudiante.
 * Rutas /api/cursos-verano/reportes/… (EST-7, paso 8c).
 */
@RestController
@RequestMapping("/api/cursos-verano/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReportesCursosVeranoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GenerarReportesCursosVeranoCUIntPort generadorExcel;

    /**
     * Obtener todas las solicitudes de cursos intersemestrales para funcionarios y coordinadores
     * GET /api/cursos-verano/reportes/solicitudes (cualquier formato distinto de excel da este JSON)
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
    public ResponseEntity<List<SolicitudReporte>> obtenerTodasLasSolicitudes(
            @RequestParam(value = "idMateria", required = false) Integer idMateria,
            @RequestParam(value = "periodoAcademico", required = false) String periodoAcademico,
            @RequestParam(value = "idCurso", required = false) Integer idCurso,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        List<SolicitudReporte> solicitudesFormateadas = formatearSolicitudesCursos(idMateria, idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
        
        return ResponseEntity.ok(solicitudesFormateadas);
    }

    /**
     * Exportar solicitudes de cursos intersemestrales a Excel con filtros opcionales
     * GET /api/cursos-verano/reportes/solicitudes?formato=excel (antes /solicitudes/export/excel)
     *
     * @param idCurso Filtro por ID de curso (opcional)
     * @param estado Filtro por estado de solicitud (opcional)
     * @param periodoAcademico Filtro por período académico (opcional, formato: "YYYY-P", ej: "2025-2")
     * @param fechaInicio Filtro por fecha de inicio (opcional)
     * @param fechaFin Filtro por fecha de fin (opcional)
     * @return Archivo Excel con las solicitudes filtradas
     */
    @GetMapping(value = "/solicitudes", params = "formato=excel")
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
        headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.adjuntoEscapado(nombreArchivo));
        
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * Preinscripciones e inscripciones de cursos de verano con los filtros dados, en el formato
     * que usan el listado y la exportación a Excel.
     *
     * @param idMateria filtro por materia (null o 0 = sin filtro)
     */
    private List<SolicitudReporte> formatearSolicitudesCursos(Integer idMateria, Integer idCurso, String estado,
            String periodoAcademico, Date fechaInicio, Date fechaFin) {
        String periodo = periodoAcademico == null ? "" : periodoAcademico.trim();
        String periodoFiltro = periodo.isEmpty() || "todos".equalsIgnoreCase(periodo)
                ? null : SoporteCursosVerano.normalizarPeriodoAcademico(periodo);
        List<Solicitud> todasLasSolicitudes = solicitudCU.listarSolicitudesParaReporte(idCurso, estado,
                periodoFiltro, fechaInicio, fechaFin);

        return ReporteSolicitudesVeranoMapper.formatear(todasLasSolicitudes, idMateria);
    }

    /**
     * Obtener estadisticas del dashboard de cursos intersemestrales
     * GET /api/cursos-verano/reportes/dashboard (antes /dashboard/estadisticas)
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/dashboard")
    public ResponseEntity<Dashboard> obtenerEstadisticasDashboard() {

        // 1. Contar cursos activos (estados: Publicado, Preinscripcion, Inscripcion)
        List<CursoOfertadoVerano> todosLosCursos = cursoCU.listarTodos();
        int cursosActivos = 0;
        int cursosGestionados = 0;
        
        for (CursoOfertadoVerano curso : todosLosCursos) {
            if (curso.ultimoRegistroEstado() != null) {
                // Cursos activos: los visibles para los estudiantes (Publicado, Preinscripcion, Inscripcion)
                if (curso.esVisibleParaEstudiantes()) {
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
            } else if (solicitud instanceof SolicitudCursoVeranoInscripcion) {
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
        return ResponseEntity.ok(new Dashboard(Math.max(0, cursosActivos), Math.max(0, totalPreinscripciones),
                Math.max(0, totalInscripciones), cursosGestionadosValido, totalCursosValido,
                Math.round(porcentajeProgreso)));
    }

    /**
     * Obtener seguimiento completo de actividades del usuario (preinscripciones + inscripciones)
     * GET /api/cursos-verano/reportes/seguimiento?usuario={idUsuario} (antes /seguimiento/{idUsuario})
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#idUsuario)")
    @GetMapping("/seguimiento")
    public ResponseEntity<Seguimiento> obtenerSeguimientoActividades(@RequestParam("usuario") Integer idUsuario) {

        // Obtener preinscripciones reales de la base de datos
        List<ActividadPreinscripcion> preinscripciones = new ArrayList<>();
        
        try {
            // Obtener preinscripciones reales de la base de datos
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
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
                
                if (!esSolicitudCursoNuevo && preinscripcion.getNombreSolicitud() != null) {
                    String nombre = preinscripcion.getNombreSolicitud().toLowerCase();
                    if (nombre.contains("solicitud de apertura") || nombre.contains("curso:")) {
                        esSolicitudCursoNuevo = true;
                    }
                }
                
                if (esSolicitudCursoNuevo) {
                    tipoSolicitud = "Solicitud de Curso Nuevo";
                }

                // Obtener el nombre del curso correctamente
                String nombreCurso = "Curso no disponible";
                if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                    // Para cursos ya existentes; sin materia se queda "Curso no disponible", como en las inscripciones
                    if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                        nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                    }
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
                    } else if (preinscripcion.getNombreSolicitud() != null && 
                              preinscripcion.getNombreSolicitud().contains(":")) {
                        // Fallback: extraer del nombre de la solicitud
                        nombreCurso = preinscripcion.getNombreSolicitud().split(":")[1].trim();
                    }
                }
                // Agregar informacion del estado del curso y acciones disponibles
                String estadoCurso = "No disponible";
                List<String> accionesDisponibles = new ArrayList<>();
                
                if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                    // Para cursos existentes, obtener el estado del curso
                    CursoOfertadoVerano curso = preinscripcion.getObjCursoOfertadoVerano();
                    if (curso.ultimoRegistroEstado() != null) {
                        estadoCurso = curso.estadoActual();
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
                    } else if (EstadoSolicitudCursoVerano.RECHAZADO.texto().equals(estadoPreinscripcion)) {
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
                
                preinscripciones.add(new ActividadPreinscripcion(preinscripcion.getIdSolicitud(),
                        preinscripcion.getFechaRegistroSolicitud(), preinscripcion.estadoActual("Enviado"),
                        tipoSolicitud, nombreCurso, idUsuario,
                        preinscripcion.getObjCursoOfertadoVerano() != null
                                ? preinscripcion.getObjCursoOfertadoVerano().getIdCurso() : null,
                        estadoCurso, accionesDisponibles));
            }
            
            
        } catch (Exception e) {
            // En caso de error, devolver lista vacia en lugar de datos simulados
            log.error("No se pudieron obtener las preinscripciones del usuario {}", idUsuario, e);
            preinscripciones = new ArrayList<>();
        }
        
        // Obtener inscripciones reales del usuario
        List<ActividadInscripcion> inscripcionesUsuario = new ArrayList<>();
        
        try {
            // Obtener inscripciones reales de la base de datos
            List<SolicitudCursoVeranoInscripcion> inscripcionesReales = solicitudCU.buscarInscripcionesPorUsuario(idUsuario);
            
            for (SolicitudCursoVeranoInscripcion inscripcion : inscripcionesReales) {
                // Obtener el estado actual de la inscripcion
                String estadoInscripcion = inscripcion.estadoActual(EstadoSolicitudCursoVerano.ENVIADA.texto());

                // Informacion del curso - solo el nombre para evitar [object Object]
                String nombreCurso = "Curso no disponible";
                Integer cursoId = null;
                if (inscripcion.getObjCursoOfertadoVerano() != null && 
                    inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                    nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                    cursoId = inscripcion.getObjCursoOfertadoVerano().getIdCurso();
                }
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
                // Determinar acciones disponibles basadas en el estado
                List<String> accionesDisponibles = new ArrayList<>();
                if (EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoInscripcion)) {
                    accionesDisponibles.add("esperando_validacion_pago");
                } else if (EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoInscripcion)) {
                    accionesDisponibles.add("inscripcion_aceptada");
                } else if (EstadoSolicitudCursoVerano.PAGO_RECHAZADO.texto().equals(estadoInscripcion)) {
                    accionesDisponibles.add("pago_rechazado");
                }
                
                inscripcionesUsuario.add(new ActividadInscripcion(inscripcion.getIdSolicitud(),
                        inscripcion.getFechaRegistroSolicitud(), estadoInscripcion, "Inscripcion",
                        inscripcion.getNombreSolicitud(), nombreCurso, cursoId, idUsuario, estadoCurso,
                        accionesDisponibles));
            }
            
            
        } catch (Exception e) {
            // En caso de error, devolver lista vacia
            log.error("No se pudieron obtener las inscripciones del usuario {}", idUsuario, e);
            inscripcionesUsuario = new ArrayList<>();
        }
        
        // Crear respuesta con estadisticas
        return ResponseEntity.ok(new Seguimiento(preinscripciones, inscripcionesUsuario, preinscripciones.size(),
                inscripcionesUsuario.size(), preinscripciones.size() + inscripcionesUsuario.size()));
    }

}
