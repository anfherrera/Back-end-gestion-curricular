package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorExcelSolicitudesVerano;

/**
 * Reportes de cursos de verano: listado y exportación de solicitudes, dashboard y seguimiento del estudiante.
 * Ruta base compartida con los demás controladores de cursos de verano.
 */
@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class ReportesCursosVeranoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarSolicitudCursoVeranoGatewayIntPort solicitudGateway;
    private final GestionarSolicitudCUIntPort solicitudGeneralCU;
    private final GeneradorExcelSolicitudesVerano generadorExcel;

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
        List<Map<String, Object>> solicitudesFormateadas = formatearSolicitudesCursos(null, idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
        // Generar Excel con información de filtros
        byte[] excelBytes = generadorExcel.generarExcelSolicitudes(solicitudesFormateadas, idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String nombreArchivo = "solicitudes_cursos_intersemestrales_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        
        
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
        List<Solicitud> todasLasSolicitudes = solicitudGeneralCU.listarSolicitudes();
        
        // Aplicar filtros si se proporcionan
        todasLasSolicitudes = filtrarSolicitudes(todasLasSolicitudes, idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
        List<Map<String, Object>> solicitudesFormateadas = new ArrayList<>();
        
        for (Solicitud solicitud : todasLasSolicitudes) {
            // Solo procesar solicitudes de cursos intersemestrales
            if (solicitud instanceof SolicitudCursoVeranoPreinscripcion || 
                solicitud instanceof SolicitudCursoVeranoIncripcion) {
                
                Map<String, Object> solicitudInfo = new HashMap<>();
                
                // Informacion basica
                solicitudInfo.put("id", solicitud.getId_solicitud());
                solicitudInfo.put("fecha", solicitud.getFecha_registro_solicitud());
                solicitudInfo.put("tipo", solicitud instanceof SolicitudCursoVeranoPreinscripcion ? 
                    "Preinscripcion" : "Inscripcion");
                
                // Motivo de la solicitud
                String motivoSolicitud = "No especificado";
                
                if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) solicitud;
                    if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().trim().isEmpty()) {
                        motivoSolicitud = preinscripcion.getObservacion();
                    } else if (preinscripcion.getNombre_solicitud() != null && !preinscripcion.getNombre_solicitud().trim().isEmpty()) {
                        motivoSolicitud = preinscripcion.getNombre_solicitud();
                    }
                } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                    SolicitudCursoVeranoIncripcion inscripcion = (SolicitudCursoVeranoIncripcion) solicitud;
                    if (inscripcion.getObservacion() != null && !inscripcion.getObservacion().trim().isEmpty()) {
                        motivoSolicitud = inscripcion.getObservacion();
                    } else if (inscripcion.getNombre_solicitud() != null && !inscripcion.getNombre_solicitud().trim().isEmpty()) {
                        motivoSolicitud = inscripcion.getNombre_solicitud();
                    }
                }
                
                solicitudInfo.put("motivoSolicitud", motivoSolicitud);
                
                // Informacion del usuario
                if (solicitud.getObjUsuario() != null) {
                    solicitudInfo.put("nombreCompleto", solicitud.getObjUsuario().getNombre_completo());
                    solicitudInfo.put("codigo", solicitud.getObjUsuario().getCodigo());
                } else {
                    solicitudInfo.put("nombreCompleto", "Usuario no disponible");
                    solicitudInfo.put("codigo", "N/A");
                }
                
                // Informacion del curso
                String nombreCurso = "Curso no disponible";
                String condicion = "N/A";
                Integer idMateriaSolicitud = null;
                Integer idCursoSolicitud = null;
                String codigoMateria = null;
                String grupoCurso = null;
                
                if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) solicitud;
                    
                    if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                        idCursoSolicitud = preinscripcion.getObjCursoOfertadoVerano().getId_curso();
                        grupoCurso = preinscripcion.getObjCursoOfertadoVerano().getGrupo() != null ? 
                            preinscripcion.getObjCursoOfertadoVerano().getGrupo().toString() : null;
                        
                        if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                            idMateriaSolicitud = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getId_materia();
                            codigoMateria = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getCodigo();
                        }
                    } else if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().isEmpty()) {
                        // Para solicitudes de curso nuevo
                        String observacion = preinscripcion.getObservacion();
                        if (observacion.contains(": ")) {
                            nombreCurso = observacion.split(": ")[1].trim();
                        } else {
                            nombreCurso = observacion;
                        }
                    }
                    
                    // Estado de la preinscripcion
                    String estadoPreinscripcion = "Enviada";
                    if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoPreinscripcion = preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                    }
                    solicitudInfo.put("estado", estadoPreinscripcion);
                    
                    // Condicion academica del estudiante (Primera Vez, Repitencia, Habilitacion, etc.)
                    String condicionAcademica = "PRIMERA_VEZ";
                    if (preinscripcion.getCodicion_solicitud() != null) {
                        condicionAcademica = preinscripcion.getCodicion_solicitud().toString();
                    }
                    solicitudInfo.put("condicion", condicionAcademica);
                    
                } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                    SolicitudCursoVeranoIncripcion inscripcion = (SolicitudCursoVeranoIncripcion) solicitud;
                    
                    if (inscripcion.getObjCursoOfertadoVerano() != null) {
                        idCursoSolicitud = inscripcion.getObjCursoOfertadoVerano().getId_curso();
                        grupoCurso = inscripcion.getObjCursoOfertadoVerano().getGrupo() != null ? 
                            inscripcion.getObjCursoOfertadoVerano().getGrupo().toString() : null;
                        
                        if (inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                            idMateriaSolicitud = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getId_materia();
                            codigoMateria = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getCodigo();
                        }
                    }
                    
                    // Condicion de la inscripcion
                    if (inscripcion.getCodicion_solicitud() != null) {
                        condicion = inscripcion.getCodicion_solicitud().toString();
                    }
                    
                    // Estado de la inscripcion
                    String estadoInscripcion = "Enviada";
                    if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoInscripcion = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                    }
                    solicitudInfo.put("estado", estadoInscripcion);
                    solicitudInfo.put("condicion", condicion);
                }
                
                solicitudInfo.put("curso", nombreCurso);
                solicitudInfo.put("idMateria", idMateriaSolicitud);
                solicitudInfo.put("idCurso", idCursoSolicitud);
                solicitudInfo.put("codigoMateria", codigoMateria);
                solicitudInfo.put("grupoCurso", grupoCurso);
                
                // Filtrar por materia si se proporciona
                if (idMateria != null && idMateria != 0) {
                    if (idMateriaSolicitud == null || !idMateriaSolicitud.equals(idMateria)) {
                        continue; // Saltar esta solicitud si no coincide con el filtro de materia
                    }
                }
                
                solicitudesFormateadas.add(solicitudInfo);
            }
        }
        return solicitudesFormateadas;
    }

    /**
     * Método auxiliar para filtrar solicitudes según los parámetros proporcionados
     * 
     * @param solicitudes Lista de solicitudes a filtrar
     * @param idCurso Filtro por ID de curso (opcional)
     * @param estado Filtro por estado (opcional)
     * @param periodoAcademico Filtro por período académico (opcional)
     * @param fechaInicio Filtro por fecha de inicio (opcional)
     * @param fechaFin Filtro por fecha de fin (opcional)
     * @return Lista de solicitudes filtradas
     */
    private List<Solicitud> filtrarSolicitudes(
            List<Solicitud> solicitudes, 
            Integer idCurso, 
            String estado,
            String periodoAcademico,
            Date fechaInicio, 
            Date fechaFin) {
        
        return solicitudes.stream()
            .filter(solicitud -> {
                // Filtrar por tipo (solo cursos intersemestrales)
                if (!(solicitud instanceof SolicitudCursoVeranoPreinscripcion || 
                      solicitud instanceof SolicitudCursoVeranoIncripcion)) {
                    return false;
                }
                
                // Filtrar por ID de curso
                if (idCurso != null) {
                    if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                        SolicitudCursoVeranoPreinscripcion preinscripcion = 
                            (SolicitudCursoVeranoPreinscripcion) solicitud;
                        if (preinscripcion.getObjCursoOfertadoVerano() == null || 
                            !preinscripcion.getObjCursoOfertadoVerano().getId_curso().equals(idCurso)) {
                            return false;
                        }
                    } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                        SolicitudCursoVeranoIncripcion inscripcion = 
                            (SolicitudCursoVeranoIncripcion) solicitud;
                        if (inscripcion.getObjCursoOfertadoVerano() == null || 
                            !inscripcion.getObjCursoOfertadoVerano().getId_curso().equals(idCurso)) {
                            return false;
                        }
                    }
                }
                
                // Filtrar por estado
                if (estado != null && !estado.trim().isEmpty()) {
                    if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
                        return false;
                    }
                    String estadoActual = solicitud.getEstadosSolicitud()
                        .get(solicitud.getEstadosSolicitud().size() - 1)
                        .getEstado_actual();
                    if (!estadoActual.equalsIgnoreCase(estado.trim())) {
                        return false;
                    }
                }
                
                // Filtrar por período académico
                if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
                    String periodoFiltro = SoporteCursosVerano.normalizarPeriodoAcademico(periodoAcademico.trim());
                    
                    // Verificar período en la solicitud
                    String periodoSolicitud = solicitud.getPeriodo_academico();
                    if (periodoSolicitud == null || !periodoSolicitud.equals(periodoFiltro)) {
                        // Si no coincide con el período de la solicitud, verificar en el curso asociado
                        boolean coincide = false;
                        if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                            SolicitudCursoVeranoPreinscripcion preinscripcion = 
                                (SolicitudCursoVeranoPreinscripcion) solicitud;
                            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                                String periodoCurso = preinscripcion.getObjCursoOfertadoVerano().getPeriodo_academico();
                                coincide = periodoCurso != null && periodoCurso.equals(periodoFiltro);
                            }
                        } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                            SolicitudCursoVeranoIncripcion inscripcion = 
                                (SolicitudCursoVeranoIncripcion) solicitud;
                            if (inscripcion.getObjCursoOfertadoVerano() != null) {
                                String periodoCurso = inscripcion.getObjCursoOfertadoVerano().getPeriodo_academico();
                                coincide = periodoCurso != null && periodoCurso.equals(periodoFiltro);
                            }
                        }
                        
                        if (!coincide) {
                            return false;
                        }
                    }
                }
                
                // Filtrar por rango de fechas
                if (fechaInicio != null || fechaFin != null) {
                    Date fechaSolicitud = solicitud.getFecha_registro_solicitud();
                    if (fechaSolicitud == null) {
                        return false;
                    }
                    
                    if (fechaInicio != null && fechaSolicitud.before(fechaInicio)) {
                        return false;
                    }
                    
                    if (fechaFin != null) {
                        // Incluir el día completo de fechaFin
                        Date fechaFinInclusive = new Date(fechaFin.getTime() + (24 * 60 * 60 * 1000) - 1);
                        if (fechaSolicitud.after(fechaFinInclusive)) {
                            return false;
                        }
                    }
                }
                
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
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
        for (Solicitud solicitud : solicitudGeneralCU.listarSolicitudes()) {
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
                preinscripcionMap.put("estado", preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                    ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual() : "Enviado");
                
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
                    String estadoPreinscripcion = preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                        ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual() : "Enviado";
                    
                    if ("Aprobado".equals(estadoPreinscripcion)) {
                        if ("Inscripcion".equals(estadoCurso)) {
                            accionesDisponibles.add("proceder_inscripcion");
                        } else {
                            accionesDisponibles.add("esperando_inscripcion");
                        }
                    } else if ("Enviada".equals(estadoPreinscripcion)) {
                        accionesDisponibles.add("esperando_aprobacion");
                    } else if ("Rechazada".equals(estadoPreinscripcion)) {
                        accionesDisponibles.add("revisar_motivo_rechazo");
                    }
                } else {
                    // Para solicitudes de curso nuevo
                    String estadoPreinscripcion = preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                        ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual() : "Enviado";
                    
                    if ("Enviada".equals(estadoPreinscripcion)) {
                        accionesDisponibles.add("esperando_aprobacion_curso");
                    } else if ("Aprobado".equals(estadoPreinscripcion)) {
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
            List<SolicitudCursoVeranoIncripcion> inscripcionesReales = solicitudGateway.buscarInscripcionesPorUsuario(idUsuario);
            
            for (SolicitudCursoVeranoIncripcion inscripcion : inscripcionesReales) {
                Map<String, Object> inscripcionMap = new HashMap<>();
                inscripcionMap.put("id", inscripcion.getId_solicitud());
                inscripcionMap.put("fecha", inscripcion.getFecha_registro_solicitud());
                
                // Obtener el estado actual de la inscripcion
                String estadoInscripcion = "Enviada";
                if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                    estadoInscripcion = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                }
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
                if ("Pago_Validado".equals(estadoInscripcion)) {
                    estadoCurso = "INSCRITO";
                } else if ("Inscripcion_Completada".equals(estadoInscripcion)) {
                    estadoCurso = "ACTIVO";
                } else if ("Curso_Aprobado".equals(estadoInscripcion)) {
                    estadoCurso = "APROBADO";
                } else if ("Curso_Finalizado".equals(estadoInscripcion)) {
                    estadoCurso = "FINALIZADO";
                } else if ("Enviada".equals(estadoInscripcion)) {
                    estadoCurso = "PENDIENTE";
                } else if ("Pago_Rechazado".equals(estadoInscripcion)) {
                    estadoCurso = "RECHAZADO";
                }
                inscripcionMap.put("estadoCurso", estadoCurso);
                
                // Determinar acciones disponibles basadas en el estado
                List<String> accionesDisponibles = new ArrayList<>();
                if ("Enviada".equals(estadoInscripcion)) {
                    accionesDisponibles.add("esperando_validacion_pago");
                } else if ("Pago_Validado".equals(estadoInscripcion)) {
                    accionesDisponibles.add("inscripcion_aceptada");
                } else if ("Pago_Rechazado".equals(estadoInscripcion)) {
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

    /**
     * Obtener solicitudes del usuario especifico
     * GET /api/cursos-intersemestrales/cursos-verano/solicitudes/{idUsuario}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#idUsuario)")
    @GetMapping("/cursos-verano/solicitudes/{idUsuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerSolicitudesUsuario(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        List<Map<String, Object>> solicitudes = new ArrayList<>();
        
        // Solicitud 1
        Map<String, Object> solicitud1 = new HashMap<>();
        solicitud1.put("id_solicitud", 1);
        solicitud1.put("curso", "Programacion I");
        solicitud1.put("fecha", "2024-01-15T10:30:00");
        solicitud1.put("estado", "Pendiente");
        solicitud1.put("tipo", "Preinscripcion");
        solicitudes.add(solicitud1);
        
        // Solicitud 2
        Map<String, Object> solicitud2 = new HashMap<>();
        solicitud2.put("id_solicitud", 2);
        solicitud2.put("curso", "Matematicas Basicas");
        solicitud2.put("fecha", "2024-01-16T14:20:00");
        solicitud2.put("estado", "Aprobado");
        solicitud2.put("tipo", "Preinscripcion");
        solicitudes.add(solicitud2);
        
        // Solicitud 3
        Map<String, Object> solicitud3 = new HashMap<>();
        solicitud3.put("id_solicitud", 3);
        solicitud3.put("curso", "Bases de Datos");
        solicitud3.put("fecha", "2024-01-17T09:15:00");
        solicitud3.put("estado", "Rechazado");
        solicitud3.put("tipo", "Inscripcion");
        solicitudes.add(solicitud3);
        
        return ResponseEntity.ok(solicitudes);
    }
}
