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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoNuevoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CreateCursoDTO;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UpdateCursoDTO;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class CursosIntersemestralesRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarSolicitudCursoVeranoGatewayIntPort solicitudGateway;
    private final GestionarMateriasCUIntPort materiaCU;
    private final GestionarDocentesCUIntPort docenteCU;
    private final CursosOfertadosMapperDominio cursoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final GestionarSolicitudCUIntPort solicitudGeneralCU;
    private final co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort objGestionarUsuarioCU;
    private final GestionarSalonesCUIntPort salonCU;

    /**
     * Obtener cursos de verano (endpoint principal que llama el frontend)
     * GET /api/cursos-intersemestrales/cursos-verano
     */
    /**
     * Obtener cursos de verano (endpoint principal que llama el frontend)
     * GET /api/cursos-intersemestrales/cursos-verano
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar cursos
     */
    @GetMapping("/cursos-verano")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        
        // Filtrar por período académico si se proporciona
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            String periodoFiltro = periodoAcademico.trim();
            cursos = cursos.stream()
                    .filter(curso -> curso.getPeriodo_academico() != null 
                            && curso.getPeriodo_academico().equals(periodoFiltro))
                    .collect(Collectors.toList());
        } else {
            // Si no se proporciona período, usar el período actual automáticamente
            PeriodoAcademicoEnum periodoActualEnum = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActualEnum != null) {
                final String periodoFiltro = periodoActualEnum.getValor();
                cursos = cursos.stream()
                        .filter(curso -> {
                            boolean coincide = curso.getPeriodo_academico() != null 
                                    && curso.getPeriodo_academico().equals(periodoFiltro);
                            if (!coincide && curso.getPeriodo_academico() != null) {
                            }
                            return coincide;
                        })
                        .collect(Collectors.toList());
            } else {
            }
        }
        
        // Si se proporciona idPrograma, filtrar cursos que tengan estudiantes inscritos o solicitudes de ese programa
        // También incluir cursos sin solicitudes/inscripciones (disponibles para todos)
        if (idPrograma != null) {
            cursos = cursos.stream()
                    .filter(curso -> {
                        // Verificar si el curso tiene estudiantes inscritos del programa especificado
                        boolean tieneEstudiantesDelPrograma = false;
                        if (curso.getEstudiantesInscritos() != null && !curso.getEstudiantesInscritos().isEmpty()) {
                            tieneEstudiantesDelPrograma = curso.getEstudiantesInscritos().stream()
                                    .anyMatch(usuario -> usuario.getObjPrograma() != null
                                            && usuario.getObjPrograma().getId_programa().equals(idPrograma));
                        }
                        
                        // Verificar si el curso tiene solicitudes de estudiantes del programa especificado
                        boolean tieneSolicitudesDelPrograma = false;
                        if (curso.getSolicitudes() != null && !curso.getSolicitudes().isEmpty()) {
                            tieneSolicitudesDelPrograma = curso.getSolicitudes().stream()
                                    .anyMatch(solicitud -> solicitud.getObjUsuario() != null 
                                            && solicitud.getObjUsuario().getObjPrograma() != null
                                            && solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma));
                        }
                        
                        // Incluir el curso si:
                        // 1. Tiene estudiantes inscritos del programa, O
                        // 2. Tiene solicitudes del programa, O
                        // 3. No tiene ni estudiantes ni solicitudes (disponible para todos)
                        boolean sinInscripcionesNiSolicitudes = 
                                (curso.getEstudiantesInscritos() == null || curso.getEstudiantesInscritos().isEmpty())
                                && (curso.getSolicitudes() == null || curso.getSolicitudes().isEmpty());
                        
                        return tieneEstudiantesDelPrograma || tieneSolicitudesDelPrograma || sinInscripcionesNiSolicitudes;
                    })
                    .collect(Collectors.toList());
        }
        
        
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(curso -> {
                    // Log para verificar docente antes del mapeo
                    if (curso.getObjDocente() == null) {
                    } else {
                    }
                    return cursoMapper.mappearDeCursoOfertadoARespuestaDisponible(curso);
                })
                .map(dto -> {
                    // Log para verificar docente despues del mapeo
                    if (dto.getObjDocente() == null) {
                    } else {
                    }
                    return cursoMapper.postMapCurso(dto);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener cursos de verano disponibles para estudiantes (solo estados visibles)
     * GET /api/cursos-intersemestrales/cursos-verano/disponibles
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2"). 
     *                         Si se envía "todos" o no se proporciona y todosLosPeriodos=true, se muestran todos los cursos sin filtrar.
     * @param idPrograma ID del programa académico opcional para filtrar cursos
     * @param todosLosPeriodos Si es true, muestra todos los cursos sin filtrar por período (ignora periodoAcademico)
     */
    @GetMapping("/cursos-verano/disponibles")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosVeranoDisponibles(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false, defaultValue = "false") Boolean todosLosPeriodos) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        String periodoUsado = null;
        
        // Si se solicita ver todos los períodos, no filtrar por período
        if (Boolean.TRUE.equals(todosLosPeriodos) || 
            (periodoAcademico != null && periodoAcademico.trim().equalsIgnoreCase("todos"))) {
            periodoUsado = "todos";
        } 
        // Filtrar por período académico si se proporciona
        else if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            String periodoFiltro = periodoAcademico.trim();
            periodoUsado = periodoFiltro;
            cursos = cursos.stream()
                    .filter(curso -> curso.getPeriodo_academico() != null 
                            && curso.getPeriodo_academico().equals(periodoFiltro))
                    .collect(Collectors.toList());
        } else {
            // Si no se proporciona período, usar el período actual automáticamente
            PeriodoAcademicoEnum periodoActualEnum = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActualEnum != null) {
                final String periodoFiltro = periodoActualEnum.getValor();
                periodoUsado = periodoFiltro;
                cursos = cursos.stream()
                        .filter(curso -> {
                            boolean coincide = curso.getPeriodo_academico() != null 
                                    && curso.getPeriodo_academico().equals(periodoFiltro);
                            if (!coincide && curso.getPeriodo_academico() != null) {
                            }
                            return coincide;
                        })
                        .collect(Collectors.toList());
            } else {
                periodoUsado = "indeterminado";
            }
        }
        
        // Si se proporciona idPrograma, filtrar cursos que tengan estudiantes inscritos o solicitudes de ese programa
        // También incluir cursos sin solicitudes/inscripciones (disponibles para todos)
        if (idPrograma != null) {
            cursos = cursos.stream()
                    .filter(curso -> {
                        // Verificar si el curso tiene estudiantes inscritos del programa especificado
                        boolean tieneEstudiantesDelPrograma = false;
                        if (curso.getEstudiantesInscritos() != null && !curso.getEstudiantesInscritos().isEmpty()) {
                            tieneEstudiantesDelPrograma = curso.getEstudiantesInscritos().stream()
                                    .anyMatch(usuario -> usuario.getObjPrograma() != null
                                            && usuario.getObjPrograma().getId_programa().equals(idPrograma));
                        }
                        
                        // Verificar si el curso tiene solicitudes de estudiantes del programa especificado
                        boolean tieneSolicitudesDelPrograma = false;
                        if (curso.getSolicitudes() != null && !curso.getSolicitudes().isEmpty()) {
                            tieneSolicitudesDelPrograma = curso.getSolicitudes().stream()
                                    .anyMatch(solicitud -> solicitud.getObjUsuario() != null 
                                            && solicitud.getObjUsuario().getObjPrograma() != null
                                            && solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma));
                        }
                        
                        // Incluir el curso si:
                        // 1. Tiene estudiantes inscritos del programa, O
                        // 2. Tiene solicitudes del programa, O
                        // 3. No tiene ni estudiantes ni solicitudes (disponible para todos)
                        boolean sinInscripcionesNiSolicitudes = 
                                (curso.getEstudiantesInscritos() == null || curso.getEstudiantesInscritos().isEmpty())
                                && (curso.getSolicitudes() == null || curso.getSolicitudes().isEmpty());
                        
                        return tieneEstudiantesDelPrograma || tieneSolicitudesDelPrograma || sinInscripcionesNiSolicitudes;
                    })
                    .collect(Collectors.toList());
        }
        
        // Filtrar solo cursos visibles para estudiantes
        List<CursoOfertadoVerano> cursosDisponibles = cursos.stream()
                .filter(curso -> {
                    if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                        return false;
                    }
                    String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                    // Estados visibles para estudiantes: Publicado, Preinscripcion, Inscripcion
                    return "Publicado".equals(estadoActual) || 
                           "Preinscripcion".equals(estadoActual) ||
                           "Inscripcion".equals(estadoActual);
                })
                .collect(Collectors.toList());
        
        List<CursosOfertadosDTORespuesta> respuesta = cursosDisponibles.stream()
                .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                .collect(Collectors.toList());
        
        // Log informativo cuando no hay cursos
        if (respuesta.isEmpty()) {
            if (periodoUsado != null && !periodoUsado.equals("todos")) {
            } else {
            }
        } else {
        }
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Obtener todos los cursos de verano para funcionarios y coordinadores (incluye estados no visibles)
     * GET /api/cursos-intersemestrales/cursos-verano/todos
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2"). 
     *                         Si no se proporciona, muestra TODOS los cursos sin filtrar por período.
     * @param idPrograma ID del programa académico opcional para filtrar cursos
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/cursos-verano/todos")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerTodosLosCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        
        
        // Filtrar por período académico SOLO si se proporciona explícitamente
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
            String periodoFiltro = periodoAcademico.trim();
            
            // Intentar convertir formatos alternativos a formato estándar YYYY-P
            String periodoNormalizado = normalizarPeriodoAcademico(periodoFiltro);
            if (!periodoNormalizado.equals(periodoFiltro)) {
                periodoFiltro = periodoNormalizado;
            }
            
            final String periodoFinal = periodoFiltro; // Para usar en lambda
            List<CursoOfertadoVerano> cursosFiltrados = cursos.stream()
                    .filter(curso -> {
                        boolean coincide = curso.getPeriodo_academico() != null 
                                && curso.getPeriodo_academico().equals(periodoFinal);
                        if (!coincide && curso.getPeriodo_academico() != null) {
                        }
                        return coincide;
                    })
                    .collect(Collectors.toList());
            
            
            cursos = cursosFiltrados;
        } else {
        }
        
        // Si se proporciona idPrograma, filtrar cursos que tengan estudiantes inscritos o solicitudes de ese programa
        // También incluir cursos sin solicitudes/inscripciones (disponibles para todos)
        if (idPrograma != null) {
            cursos = cursos.stream()
                    .filter(curso -> {
                        // Verificar si el curso tiene estudiantes inscritos del programa especificado
                        boolean tieneEstudiantesDelPrograma = false;
                        if (curso.getEstudiantesInscritos() != null && !curso.getEstudiantesInscritos().isEmpty()) {
                            tieneEstudiantesDelPrograma = curso.getEstudiantesInscritos().stream()
                                    .anyMatch(usuario -> usuario.getObjPrograma() != null
                                            && usuario.getObjPrograma().getId_programa().equals(idPrograma));
                        }
                        
                        // Verificar si el curso tiene solicitudes de estudiantes del programa especificado
                        boolean tieneSolicitudesDelPrograma = false;
                        if (curso.getSolicitudes() != null && !curso.getSolicitudes().isEmpty()) {
                            tieneSolicitudesDelPrograma = curso.getSolicitudes().stream()
                                    .anyMatch(solicitud -> solicitud.getObjUsuario() != null 
                                            && solicitud.getObjUsuario().getObjPrograma() != null
                                            && solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma));
                        }
                        
                        // Incluir el curso si:
                        // 1. Tiene estudiantes inscritos del programa, O
                        // 2. Tiene solicitudes del programa, O
                        // 3. No tiene ni estudiantes ni solicitudes (disponible para todos)
                        boolean sinInscripcionesNiSolicitudes = 
                                (curso.getEstudiantesInscritos() == null || curso.getEstudiantesInscritos().isEmpty())
                                && (curso.getSolicitudes() == null || curso.getSolicitudes().isEmpty());
                        
                        return tieneEstudiantesDelPrograma || tieneSolicitudesDelPrograma || sinInscripcionesNiSolicitudes;
                    })
                    .collect(Collectors.toList());
        }
        
        // Para funcionarios y coordinadores, mostrar todos los cursos sin filtro de estado
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                .map(dto -> {
                    // Agregar información completa del salón si existe
                    String numeroSalon = dto.getSalon() != null ? dto.getSalon().trim() : null;
                    
                    if (numeroSalon != null && !numeroSalon.isEmpty()) {
                        try {
                            // Intentar obtener el salón por número
                            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon = null;
                            try {
                                salon = salonCU.obtenerSalonPorNumero(numeroSalon);
                            } catch (Exception e) {
                                // Si el método lanza excepción cuando no encuentra, intentar buscar directamente
                                // Buscar en la lista de salones activos
                                List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon> salonesActivos = 
                                    salonCU.listarSalonesActivos();
                                salon = salonesActivos.stream()
                                    .filter(s -> s.getNumero_salon() != null && s.getNumero_salon().equals(numeroSalon))
                                    .findFirst()
                                    .orElse(null);
                                if (salon != null) {
                                }
                            }
                            
                            if (salon != null) {
                                Map<String, Object> salonInfo = new HashMap<>();
                                salonInfo.put("id_salon", salon.getId_salon());
                                salonInfo.put("numero_salon", salon.getNumero_salon());
                                salonInfo.put("edificio", salon.getEdificio());
                                salonInfo.put("activo", salon.getActivo());
                                salonInfo.put("descripcion", salon.getNumero_salon() + 
                                    (salon.getEdificio() != null ? " - " + salon.getEdificio() : ""));
                                dto.setSalonInfo(salonInfo);
                                dto.setId_salon(salon.getId_salon());
                            } else {
                                // Mantener el número del salón aunque no se encuentre en la tabla Salones
                                dto.setId_salon(null);
                                dto.setSalonInfo(null);
                            }
                        } catch (Exception e) {
                            // Mantener el número del salón aunque haya error
                            dto.setId_salon(null);
                            dto.setSalonInfo(null);
                        }
                    } else {
                        dto.setId_salon(null);
                        dto.setSalonInfo(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener cursos disponibles para preinscripcion (solo cursos en estado Preinscripcion)
     * GET /api/cursos-intersemestrales/cursos/preinscripcion
     * 
     * @param periodoAcademico Período académico opcional. Si no se proporciona, muestra cursos de todos los períodos.
     */
    @GetMapping("/cursos/preinscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosPreinscripcion(
            @RequestParam(required = false) String periodoAcademico) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        
        // Filtrar solo cursos en estado de preinscripcion
        List<CursoOfertadoVerano> cursosPreinscripcion = cursos.stream()
                .filter(curso -> {
                    if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                        return false;
                    }
                    String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                    return "Preinscripcion".equals(estadoActual);
                })
                .collect(Collectors.toList());
        
        
        // Filtrar por período académico si se proporciona
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
            String periodoFiltro = periodoAcademico.trim();
            cursosPreinscripcion = cursosPreinscripcion.stream()
                    .filter(curso -> curso.getPeriodo_academico() != null 
                            && curso.getPeriodo_academico().equals(periodoFiltro))
                    .collect(Collectors.toList());
        } else {
        }
        
        List<CursosOfertadosDTORespuesta> respuesta = cursosPreinscripcion.stream()
                .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Obtener cursos disponibles para inscripcion (solo cursos en estado Inscripcion)
     * GET /api/cursos-intersemestrales/cursos/inscripcion
     * 
     * @param periodoAcademico Período académico opcional. Si no se proporciona, muestra cursos de todos los períodos.
     */
    @GetMapping("/cursos/inscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosInscripcion(
            @RequestParam(required = false) String periodoAcademico) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        
        // Filtrar solo cursos en estado de inscripcion
        List<CursoOfertadoVerano> cursosInscripcion = cursos.stream()
                .filter(curso -> {
                    if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                        return false;
                    }
                    String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                    return "Inscripcion".equals(estadoActual);
                })
                .collect(Collectors.toList());
        
        
        // Filtrar por período académico si se proporciona
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
            String periodoFiltro = periodoAcademico.trim();
            cursosInscripcion = cursosInscripcion.stream()
                    .filter(curso -> curso.getPeriodo_academico() != null 
                            && curso.getPeriodo_academico().equals(periodoFiltro))
                    .collect(Collectors.toList());
        } else {
        }
        
        List<CursosOfertadosDTORespuesta> respuesta = cursosInscripcion.stream()
                .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener solicitudes de un estudiante especifico
     * GET /api/cursos-intersemestrales/estudiante/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping("/estudiante/{id}")
    public ResponseEntity<List<SolicitudCursoVeranoPreinscripcionDTORespuesta>> obtenerSolicitudesEstudiante(
            @Min(value = 1) @PathVariable Integer id) {
        List<SolicitudCursoVeranoPreinscripcion> solicitudes = solicitudCU.buscarSolicitudesPorUsuario(id);
        List<SolicitudCursoVeranoPreinscripcionDTORespuesta> respuesta = solicitudes.stream()
                .map(solicitudMapper::mappearDeSolicitudCursoVeranoPreinscripcionARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener preinscripciones de un usuario especifico (endpoint que espera el frontend)
     * GET /api/cursos-intersemestrales/preinscripciones/usuario/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping("/preinscripciones/usuario/{id}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPreinscripcionesPorUsuario(
            @Min(value = 1) @PathVariable Integer id) {
        
        List<Map<String, Object>> preinscripciones = new ArrayList<>();
        
        // Obtener preinscripciones reales de la base de datos
        List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(id);
        
        
        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
            Map<String, Object> preinscripcionMap = new HashMap<>();
            preinscripcionMap.put("id", preinscripcion.getId_solicitud());
            preinscripcionMap.put("fecha", preinscripcion.getFecha_registro_solicitud());
            String estadoActual = preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual()
                : "Enviado";
            preinscripcionMap.put("estado", estadoActual);
            String comentarioEstado = null;
            if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoEstado =
                    preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1);
                comentarioEstado = ultimoEstado.getComentario();
            }
            if (comentarioEstado != null && !comentarioEstado.trim().isEmpty()) {
                preinscripcionMap.put("comentarioEstado", comentarioEstado);
            }
            preinscripcionMap.put("tipo", "Preinscripcion");
            preinscripcionMap.put("estudianteId", preinscripcion.getObjUsuario().getId_usuario());
            
            // Informacion del curso
            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                preinscripcionMap.put("cursoId", preinscripcion.getObjCursoOfertadoVerano().getId_curso());
                if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                    preinscripcionMap.put("cursoNombre", preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
                }
            }
            
            // Acciones disponibles basadas en el estado
            List<String> accionesDisponibles = new ArrayList<>();
            
            if ("Aprobado".equals(estadoActual)) {
                accionesDisponibles.add("inscribirse");
            } else if ("Rechazado".equals(estadoActual)) {
                accionesDisponibles.add("ver_motivo");
            } else {
                accionesDisponibles.add("esperando_respuesta");
            }
            
            preinscripcionMap.put("accionesDisponibles", accionesDisponibles);
            
            preinscripciones.add(preinscripcionMap);
        }
        
        return ResponseEntity.ok(preinscripciones);
    }

    /**
     * Obtener preinscripciones por estudiante (alias para compatibilidad con pruebas)
     * GET /api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping("/solicitudes/preinscripcion/estudiante/{id}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPreinscripcionesPorEstudianteAlias(@PathVariable Integer id) {
        return obtenerPreinscripcionesPorUsuario(id);
    }

    /**
     * Obtener cursos ofertados (metodo legacy para compatibilidad)
     * GET /api/cursos-intersemestrales/cursos/ofertados
     */
    @GetMapping("/cursos/ofertados")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosOfertados() {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener cursos disponibles para solicitud de curso nuevo
     * GET /api/cursos-intersemestrales/cursos-disponibles
     */
    @GetMapping("/cursos-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerCursosDisponibles() {
        // Usar datos reales de la base de datos
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        List<Map<String, Object>> cursosDisponibles = new ArrayList<>();
        
        for (CursoOfertadoVerano curso : cursos) {
            Map<String, Object> cursoMap = new HashMap<>();
            cursoMap.put("id_curso", curso.getId_curso());
            cursoMap.put("nombre_curso", curso.getObjMateria() != null ? curso.getObjMateria().getNombre() : "Sin nombre");
            cursoMap.put("codigo_curso", curso.getObjMateria() != null ? curso.getObjMateria().getCodigo() : "Sin codigo");
            cursoMap.put("creditos", curso.getObjMateria() != null ? curso.getObjMateria().getCreditos() : 0);
            cursoMap.put("descripcion", "Curso de " + (curso.getObjMateria() != null ? curso.getObjMateria().getNombre() : "curso"));
            cursosDisponibles.add(cursoMap);
        }
        
        return ResponseEntity.ok(cursosDisponibles);
    }

    /**
     * Obtener todas las materias disponibles para solicitud de cursos intersemestrales
     * GET /api/cursos-intersemestrales/materias-disponibles
     */
    @GetMapping("/materias-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerMateriasDisponibles() {
        // Obtener todas las materias de la base de datos
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materias = 
            materiaCU.listarMaterias();
        
        List<Map<String, Object>> materiasDisponibles = new ArrayList<>();
        
        for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materias) {
            Map<String, Object> materiaMap = new HashMap<>();
            materiaMap.put("id_materia", materia.getId_materia());
            materiaMap.put("codigo", materia.getCodigo());
            materiaMap.put("nombre", materia.getNombre());
            materiaMap.put("creditos", materia.getCreditos());
            materiaMap.put("descripcion", materia.getNombre() + " (" + materia.getCodigo() + ") - " + materia.getCreditos() + " creditos");
            materiasDisponibles.add(materiaMap);
        }
        
        return ResponseEntity.ok(materiasDisponibles);
    }

    /**
     * Obtener condiciones de solicitud disponibles
     * GET /api/cursos-intersemestrales/condiciones-solicitud
     */
    @GetMapping("/condiciones-solicitud")
    public ResponseEntity<String[]> obtenerCondicionesSolicitud() {
        CondicionSolicitudVerano[] condiciones = CondicionSolicitudVerano.values();
        String[] condicionesString = new String[condiciones.length];
        for (int i = 0; i < condiciones.length; i++) {
            condicionesString[i] = condiciones[i].name();
        }
        return ResponseEntity.ok(condicionesString);
    }

    /**
     * Crear solicitud de curso nuevo
     * POST /api/cursos-intersemestrales/solicitudes-curso-nuevo
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/solicitudes-curso-nuevo")
    public ResponseEntity<Map<String, Object>> crearSolicitudCursoNuevo(@RequestBody SolicitudCursoNuevoDTOPeticion peticion) {
        try {

            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombre_estudiante(peticion.getNombreCompleto());
            solicitudDominio.setCodigo_estudiante(peticion.getCodigo());
            
            // Mapear la condicion con validacion
            try {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Condicion invalida: " + peticion.getCondicion() + ". Valores validos: " + java.util.Arrays.toString(CondicionSolicitudVerano.values()));
            }
            
            solicitudDominio.setObservacion("Solicitud de apertura de curso: " + peticion.getCurso());
            
            // Inicializar campos obligatorios de la clase padre
            // Generar nombre descriptivo con el nombre del estudiante
            String nombreSolicitud = "Solicitud Curso Verano";
            if (peticion.getNombreCompleto() != null && !peticion.getNombreCompleto().trim().isEmpty()) {
                nombreSolicitud = "Solicitud Curso Verano - " + peticion.getNombreCompleto();
            }
            solicitudDominio.setNombre_solicitud(nombreSolicitud);
            solicitudDominio.setFecha_registro_solicitud(new java.util.Date());
            
            // Si no se proporcionó período académico, establecer el período actual automáticamente
            // El gateway también lo manejará, pero es mejor establecerlo aquí para consistencia
            if (solicitudDominio.getPeriodo_academico() == null || solicitudDominio.getPeriodo_academico().trim().isEmpty()) {
                PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
                if (periodoActual != null) {
                    solicitudDominio.setPeriodo_academico(periodoActual.getValor());
                }
            }
            
            // Crear un curso con ID = 0 para indicar que es un curso nuevo
            CursoOfertadoVerano cursoTemporal = new CursoOfertadoVerano();
            cursoTemporal.setId_curso(0); // ID = 0 indica curso nuevo
            solicitudDominio.setObjCursoOfertadoVerano(cursoTemporal);
            
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuarioTemporal = 
                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
            usuarioTemporal.setId_usuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuarioTemporal);


            // Llamar al caso de uso
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);
            

            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_solicitud", solicitudGuardada.getId_solicitud());
            respuesta.put("nombreCompleto", peticion.getNombreCompleto());
            respuesta.put("codigo", peticion.getCodigo());
            respuesta.put("curso", peticion.getCurso());
            respuesta.put("condicion", peticion.getCondicion());
            respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud().toString());
            respuesta.put("estado", "Pendiente");
            
            // Crear objeto usuario
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", peticion.getIdUsuario());
            usuario.put("nombre_completo", peticion.getNombreCompleto());
            respuesta.put("objUsuario", usuario);
            
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error de validacion: " + e.getMessage());
            error.put("tipo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Crear preinscripcion a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/preinscripciones
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/cursos-verano/preinscripciones")
    public ResponseEntity<Map<String, Object>> crearPreinscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        
        // Validar datos obligatorios
        if (peticion.getIdUsuario() == null || peticion.getIdCurso() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Los campos idUsuario e idCurso son obligatorios");
            error.put("codigo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Mapear el DTO a nuestro modelo de dominio
        SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
        solicitudDominio.setNombre_estudiante("Estudiante"); // Valor por defecto
        solicitudDominio.setCodigo_estudiante("EST001"); // Valor por defecto
        solicitudDominio.setObservacion(peticion.getNombreSolicitud() != null ? peticion.getNombreSolicitud() : "Preinscripcion curso de verano");
        
        // Usar la condicion del frontend o valor por defecto
        if (peticion.getCondicion() != null && !peticion.getCondicion().trim().isEmpty()) {
            try {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
            }
        } else {
            solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        }
        
        
        // Crear curso con el ID real
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(peticion.getIdCurso());
        solicitudDominio.setObjCursoOfertadoVerano(curso);
        
        
        // Crear usuario
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
        usuario.setId_usuario(peticion.getIdUsuario());
        solicitudDominio.setObjUsuario(usuario);

        
        // Verificar si ya existe una preinscripcion para este usuario y curso
        Solicitud preinscripcionExistente = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(peticion.getIdUsuario(), peticion.getIdCurso());
        
        if (preinscripcionExistente != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ya tienes una preinscripcion activa para este curso");
            error.put("codigo", "DUPLICATE_PREINSCRIPTION");
            return ResponseEntity.badRequest().body(error);
        }
        

        // Llamar al gateway directamente para guardar en la base de datos
        SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);


        // Crear respuesta JSON con la estructura esperada
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id_preinscripcion", solicitudGuardada.getId_solicitud());
        respuesta.put("idUsuario", peticion.getIdUsuario());
        respuesta.put("idCurso", peticion.getIdCurso());
        respuesta.put("nombreSolicitud", peticion.getNombreSolicitud());
        respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud());
        respuesta.put("estado", "Pendiente");
        respuesta.put("mensaje", "Preinscripcion creada exitosamente");
        
        return ResponseEntity.status(201).body(respuesta);
    }

    /**
     * Crear inscripcion a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/inscripciones
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/cursos-verano/inscripciones")
    public ResponseEntity<Map<String, Object>> crearInscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        
        // 1. Verificar que existe una preinscripcion valida para este usuario y curso
        
        // Buscar preinscripcion aprobada usando consulta especifica
        
        // Primero intentar buscar directamente por usuario y curso
        Solicitud preinscripcionExistente = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(peticion.getIdUsuario(), peticion.getIdCurso());
        
        SolicitudCursoVeranoPreinscripcion preinscripcionAprobada = null;
        
        if (preinscripcionExistente != null && preinscripcionExistente instanceof SolicitudCursoVeranoPreinscripcion) {
            preinscripcionAprobada = (SolicitudCursoVeranoPreinscripcion) preinscripcionExistente;
            
            // Verificar si esta aprobada
            String estadoActual = "Sin estado";
            boolean estaAprobada = false;
            
            if (preinscripcionAprobada.getEstadosSolicitud() != null && !preinscripcionAprobada.getEstadosSolicitud().isEmpty()) {
                estadoActual = preinscripcionAprobada.getEstadosSolicitud()
                    .get(preinscripcionAprobada.getEstadosSolicitud().size() - 1).getEstado_actual();
                estaAprobada = "Aprobada".equals(estadoActual) || "Aprobado".equals(estadoActual);
            }
            
            
            if (!estaAprobada) {
                preinscripcionAprobada = null;
            }
        } else {
            
            // Fallback: buscar todas las preinscripciones del usuario
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarSolicitudesPorUsuario(peticion.getIdUsuario());
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripciones) {
                // Verificar si coincide con el usuario y curso
                boolean coincideUsuario = preinscripcion.getObjUsuario() != null && 
                    preinscripcion.getObjUsuario().getId_usuario().equals(peticion.getIdUsuario());
                boolean coincideCurso = preinscripcion.getObjCursoOfertadoVerano() != null &&
                    preinscripcion.getObjCursoOfertadoVerano().getId_curso().equals(peticion.getIdCurso());
                
                
                if (coincideUsuario && coincideCurso) {
                    // Verificar si esta aprobada
                    String estadoActual = "Sin estado";
                    boolean estaAprobada = false;
                    
                    if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoActual = preinscripcion.getEstadosSolicitud()
                            .get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                        estaAprobada = "Aprobada".equals(estadoActual) || "Aprobado".equals(estadoActual);
                    }
                    
                    
                    if (estaAprobada) {
                        preinscripcionAprobada = preinscripcion;
                        break;
                    }
                }
            }
        }
        
        if (preinscripcionAprobada == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se encontro una preinscripcion aprobada para este usuario y curso");
            error.put("codigo", "PREINSCRIPCION_NO_APROBADA");
            return ResponseEntity.badRequest().body(error);
        }
        
        // 1.5. VALIDACIONES DE SEGURIDAD
        
        // Validacion 1: Verificar que no tenga una inscripcion activa para este curso
        List<SolicitudCursoVeranoIncripcion> inscripcionesExistentes = solicitudGateway.buscarInscripcionesPorUsuarioYCurso(
            peticion.getIdUsuario(), peticion.getIdCurso()
        );
        
        if (!inscripcionesExistentes.isEmpty()) {
            // Verificar si alguna no esta rechazada
            boolean tieneInscripcionActiva = inscripcionesExistentes.stream()
                .anyMatch(insc -> {
                    if (insc.getEstadosSolicitud() == null || insc.getEstadosSolicitud().isEmpty()) {
                        return true; // Sin estados = activa
                    }
                    String ultimoEstado = insc.getEstadosSolicitud().get(insc.getEstadosSolicitud().size() - 1).getEstado_actual();
                    return !"Pago_Rechazado".equals(ultimoEstado);
                });
            
            if (tieneInscripcionActiva) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Ya tienes una inscripcion activa para este curso");
                error.put("codigo", "INSCRIPCION_DUPLICADA");
                return ResponseEntity.badRequest().body(error);
            }
        }
        
        // Validacion 2: el curso debe tener cupo disponible
        ResponseEntity<Map<String, Object>> sinCupo =
                validarCupoDisponible(preinscripcionAprobada.getObjCursoOfertadoVerano());
        if (sinCupo != null) {
            return sinCupo;
        }
        
        // 2. Crear la inscripcion usando el modelo de dominio
        
        SolicitudCursoVeranoIncripcion nuevaInscripcion = new SolicitudCursoVeranoIncripcion();
        nuevaInscripcion.setNombre_solicitud(peticion.getNombreSolicitud());
        nuevaInscripcion.setFecha_registro_solicitud(new java.util.Date());
        nuevaInscripcion.setObservacion("Inscripcion creada desde preinscripcion aprobada");
        nuevaInscripcion.setObjUsuario(preinscripcionAprobada.getObjUsuario());
        nuevaInscripcion.setObjCursoOfertadoVerano(preinscripcionAprobada.getObjCursoOfertadoVerano());
        
        // Campos obligatorios que faltaban
        nuevaInscripcion.setNombre_estudiante("Estudiante"); // Valor por defecto
        nuevaInscripcion.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez); // Valor por defecto
        
        // 3. Guardar la inscripcion en la base de datos
        SolicitudCursoVeranoIncripcion inscripcionGuardada = solicitudGateway.crearSolicitudCursoVeranoInscripcion(nuevaInscripcion);
        
        if (inscripcionGuardada == null || inscripcionGuardada.getId_solicitud() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al guardar la inscripcion en la base de datos");
            return ResponseEntity.internalServerError().body(error);
        }
        
        // 4. Asociar documentos sin solicitud a la inscripcion recien creada y moverlos a carpeta organizada
        try {
            Integer idDueno = inscripcionGuardada.getObjUsuario() != null
                ? inscripcionGuardada.getObjUsuario().getId_usuario() : null;
            documentosCU.asociarDocumentosPendientes(inscripcionGuardada, idDueno, "curso-verano");
        } catch (Exception e) {
            log.error("Error asociando documentos: {}", e.getMessage(), e);
            // No fallar la operacion por esto
        }
        
        // 5. Asociar estudiante al curso en la tabla de relacion
        try {
            if (!cursoCU.asociarEstudiante(peticion.getIdCurso(), peticion.getIdUsuario())) {
                log.warn("No se pudo asociar el estudiante {} al curso {}", peticion.getIdUsuario(), peticion.getIdCurso());
            }
        } catch (Exception e) {
            log.error("Error asociando estudiante al curso: {}", e.getMessage(), e);
            // No fallar la operacion por esto
        }
        
        // 5. Preparar respuesta exitosa
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Inscripcion creada exitosamente");
        respuesta.put("id_solicitud", inscripcionGuardada.getId_solicitud());
        respuesta.put("id_usuario", peticion.getIdUsuario());
        respuesta.put("id_curso", peticion.getIdCurso());
        respuesta.put("nombre_solicitud", peticion.getNombreSolicitud());
        respuesta.put("fecha_inscripcion", inscripcionGuardada.getFecha_registro_solicitud());
        respuesta.put("estado", "Inscrito");
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Crear preinscripcion a curso de verano (alias para compatibilidad con pruebas)
     * POST /api/cursos-intersemestrales/solicitudes/preinscripcion
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/solicitudes/preinscripcion")
    public ResponseEntity<Map<String, Object>> crearPreinscripcionAlias(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        return crearPreinscripcion(peticion);
    }

    /**
     * Obtener inscripciones con informacion completa
     * GET /api/cursos-intersemestrales/inscripciones
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/inscripciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripciones() {
        // Antes se leían claves que nunca existían y se rellenaba el estudiante con datos inventados
        List<Map<String, Object>> inscripciones = new ArrayList<>();
        for (Solicitud solicitud : solicitudGeneralCU.listarSolicitudes()) {
            if (!(solicitud instanceof SolicitudCursoVeranoIncripcion inscripcion)) {
                continue;
            }
            Map<String, Object> fila = new HashMap<>();
            fila.put("id", inscripcion.getId_solicitud());
            fila.put("fecha", inscripcion.getFecha_registro_solicitud());
            List<EstadoSolicitud> estados = inscripcion.getEstadosSolicitud();
            fila.put("estado", estados != null && !estados.isEmpty()
                    ? estados.get(estados.size() - 1).getEstado_actual() : null);
            fila.put("cursoId", inscripcion.getObjCursoOfertadoVerano() != null
                    ? inscripcion.getObjCursoOfertadoVerano().getId_curso() : null);

            Usuario usuario = inscripcion.getObjUsuario();
            fila.put("estudianteId", usuario != null ? usuario.getId_usuario() : null);
            Map<String, Object> estudiante = new HashMap<>();
            estudiante.put("id_usuario", usuario != null ? usuario.getId_usuario() : null);
            estudiante.put("nombre", usuario != null ? usuario.getNombre_completo() : inscripcion.getNombre_estudiante());
            estudiante.put("email", usuario != null ? usuario.getCorreo() : null);
            estudiante.put("codigo_estudiante", usuario != null ? usuario.getCodigo() : null);
            fila.put("estudiante", estudiante);

            // Comprobante de pago: el primer documento adjunto a la inscripcion, si hay
            Map<String, Object> archivoPago = new HashMap<>();
            Documento comprobante = inscripcion.getDocumentos() != null && !inscripcion.getDocumentos().isEmpty()
                    ? inscripcion.getDocumentos().get(0) : null;
            archivoPago.put("id_documento", comprobante != null ? comprobante.getId_documento() : null);
            archivoPago.put("nombre", comprobante != null ? comprobante.getNombre() : "Sin comprobante");
            archivoPago.put("url", null);
            archivoPago.put("fecha", comprobante != null ? comprobante.getFecha_documento() : null);
            fila.put("archivoPago", archivoPago);

            inscripciones.add(fila);
        }
        return ResponseEntity.ok(inscripciones);
    }

    /**
     * Obtener inscripciones reales de la base de datos filtradas por usuario
     * GET /api/cursos-intersemestrales/inscripciones-reales/{id_usuario}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id_usuario)")
    @GetMapping("/inscripciones-reales/{id_usuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripcionesReales(@PathVariable Integer id_usuario) {
        
        // Consultar las inscripciones guardadas en la base de datos
        List<SolicitudCursoVeranoIncripcion> inscripcionesUsuario = solicitudGateway.buscarInscripcionesPorUsuario(id_usuario);

        List<Map<String, Object>> inscripciones = inscripcionesUsuario.stream().map(solicitud -> {
            Map<String, Object> inscripcion = new HashMap<>();
            inscripcion.put("id", solicitud.getId_solicitud());
            inscripcion.put("nombre_solicitud", solicitud.getNombre_solicitud());
            inscripcion.put("fecha_solicitud", solicitud.getFecha_registro_solicitud());

            // Estado mas reciente
            String estadoActual = "Sin Estado";
            if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                estadoActual = solicitud.getEstadosSolicitud()
                    .get(solicitud.getEstadosSolicitud().size() - 1)
                    .getEstado_actual();
            }
            inscripcion.put("estado", estadoActual);
            inscripcion.put("observaciones", solicitud.getObservacion());

            // Informacion del curso si existe
            if (solicitud.getObjCursoOfertadoVerano() != null) {
                CursoOfertadoVerano curso = solicitud.getObjCursoOfertadoVerano();
                inscripcion.put("curso_id", curso.getId_curso());
                inscripcion.put("salon", curso.getSalon());
                inscripcion.put("grupo", curso.getGrupo() != null ? curso.getGrupo().toString() : "");
                
                // Obtener informacion de la materia
                if (curso.getObjMateria() != null) {
                    inscripcion.put("materia", curso.getObjMateria().getNombre());
                }
                
                // Obtener informacion del docente
                if (curso.getObjDocente() != null) {
                    inscripcion.put("docente", curso.getObjDocente().getNombre_docente());
                }
            } else {
                inscripcion.put("curso_id", null);
                inscripcion.put("salon", "");
                inscripcion.put("grupo", "");
                inscripcion.put("materia", "");
                inscripcion.put("docente", "");
            }
            
            return inscripcion;
        }).collect(Collectors.toList());
        
        
        return ResponseEntity.ok(inscripciones);
    }

    
    
    /**
     * Validar permisos de operacion segun el estado del curso y el rol del usuario
     */
    private Map<String, Object> validarPermisosPorEstado(String estadoCurso, String rolUsuario, String operacion) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("permitido", false);
        
        // Definir matriz de permisos por estado y rol
        Map<String, List<String>> permisosPorEstado = new HashMap<>();
        
        // Estado: Borrador
        permisosPorEstado.put("Borrador", List.of(
            "FUNCIONARIO:ver,editar_completo,eliminar,cambiar_estado",
            "COORDINADOR:ver,editar_completo,eliminar,cambiar_estado",
            "ESTUDIANTE:ninguno"
        ));
        
        // Estado: Abierto
        permisosPorEstado.put("Abierto", List.of(
            "FUNCIONARIO:ver,editar_parcial,cambiar_estado",
            "COORDINADOR:ver,editar_parcial,cambiar_estado",
            "ESTUDIANTE:ninguno"
        ));
        
        // Estado: Publicado
        permisosPorEstado.put("Publicado", List.of(
            "FUNCIONARIO:ver,gestionar_solicitudes,cambiar_estado",
            "COORDINADOR:ver,gestionar_solicitudes,cambiar_estado",
            "ESTUDIANTE:ver,solicitar_curso_nuevo"
        ));
        
        // Estado: Preinscripcion
        permisosPorEstado.put("Preinscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_preinscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_preinscripciones,cambiar_estado",
            "ESTUDIANTE:ver,preinscribirse"
        ));
        
        // Estado: Inscripcion
        permisosPorEstado.put("Inscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_inscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_inscripciones,cambiar_estado",
            "ESTUDIANTE:ver,inscribirse"
        ));
        
        // Estado: Cerrado
        permisosPorEstado.put("Cerrado", List.of(
            "FUNCIONARIO:ver,consultar",
            "COORDINADOR:ver,consultar",
            "ESTUDIANTE:ver,consultar"
        ));
        
        // Obtener permisos para el estado actual
        List<String> permisosEstado = permisosPorEstado.get(estadoCurso);
        if (permisosEstado == null) {
            resultado.put("error", "Estado desconocido");
            resultado.put("message", "No se pueden determinar permisos para el estado: " + estadoCurso);
            return resultado;
        }
        
        // Buscar permisos para el rol especifico
        String permisosRol = permisosEstado.stream()
            .filter(permiso -> permiso.startsWith(rolUsuario + ":"))
            .findFirst()
            .orElse(null);
        
        if (permisosRol == null) {
            resultado.put("error", "Rol no valido");
            resultado.put("message", "No se encontraron permisos para el rol: " + rolUsuario);
            return resultado;
        }
        
        // Extraer las operaciones permitidas
        String operacionesPermitidas = permisosRol.split(":")[1];
        
        if ("ninguno".equals(operacionesPermitidas)) {
            resultado.put("error", "Sin permisos");
            resultado.put("message", "Este rol no tiene permisos para realizar operaciones en el estado: " + estadoCurso);
            return resultado;
        }
        
        // Verificar si la operacion especifica esta permitida
        if (operacionesPermitidas.contains(operacion) || operacionesPermitidas.contains("todas")) {
            resultado.put("permitido", true);
            resultado.put("message", "Operacion permitida");
        } else {
            resultado.put("error", "Operacion no permitida");
            resultado.put("message", "El rol " + rolUsuario + " no puede realizar la operacion '" + operacion + "' en el estado " + estadoCurso);
        }
        
        return resultado;
    }
    
    /**
     * Endpoint para obtener informacion de permisos por estado
     * GET /api/cursos-intersemestrales/permisos-estado/{estado}/{rol}
     */
    @GetMapping("/permisos-estado/{estado}/{rol}")
    public ResponseEntity<Map<String, Object>> obtenerPermisosPorEstado(
            @PathVariable String estado,
            @PathVariable String rol) {
        Map<String, Object> permisos = validarPermisosPorEstado(estado, rol, "consultar_permisos");
        return ResponseEntity.ok(permisos);
    }

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
        byte[] excelBytes = generarExcelSolicitudes(solicitudesFormateadas, idCurso, estado, periodoAcademico, fechaInicio, fechaFin);
        
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
                    String periodoFiltro = normalizarPeriodoAcademico(periodoAcademico.trim());
                    
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
    private byte[] generarExcelSolicitudes(List<Map<String, Object>> solicitudes, 
                                          Integer idCurso, 
                                          String estado, 
                                          String periodoAcademico,
                                          Date fechaInicio, 
                                          Date fechaFin) {
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
            for (Map<String, Object> solicitud : solicitudes) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                int colNum = 0;
                row.createCell(colNum++).setCellValue(solicitud.get("nombreCompleto") != null ? solicitud.get("nombreCompleto").toString() : "");
                row.createCell(colNum++).setCellValue(solicitud.get("codigo") != null ? solicitud.get("codigo").toString() : "");
                
                // ID Curso
                if (solicitud.get("idCurso") != null) {
                    row.createCell(colNum++).setCellValue(solicitud.get("idCurso").toString());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // ID Materia
                if (solicitud.get("idMateria") != null) {
                    row.createCell(colNum++).setCellValue(solicitud.get("idMateria").toString());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // Codigo Materia
                if (solicitud.get("codigoMateria") != null) {
                    row.createCell(colNum++).setCellValue(solicitud.get("codigoMateria").toString());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // Grupo
                if (solicitud.get("grupoCurso") != null) {
                    row.createCell(colNum++).setCellValue(solicitud.get("grupoCurso").toString());
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                row.createCell(colNum++).setCellValue(solicitud.get("curso") != null ? solicitud.get("curso").toString() : "");
                
                // Condicion - formatear para que sea mas legible
                String condicion = solicitud.get("condicion") != null ? solicitud.get("condicion").toString() : "";
                if (condicion.equals("PRIMERA_VEZ")) {
                    condicion = "PRIMERA VEZ";
                }
                row.createCell(colNum++).setCellValue(condicion);
                
                row.createCell(colNum++).setCellValue(solicitud.get("motivoSolicitud") != null ? solicitud.get("motivoSolicitud").toString() : "");
                row.createCell(colNum++).setCellValue(solicitud.get("estado") != null ? solicitud.get("estado").toString() : "");
                
                // Fecha formateada
                if (solicitud.get("fecha") != null) {
                    Date fecha = (Date) solicitud.get("fecha");
                    row.createCell(colNum++).setCellValue(dateFormat.format(fecha));
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                row.createCell(colNum++).setCellValue(solicitud.get("tipo") != null ? solicitud.get("tipo").toString() : "");
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

    /**
     * Cancelar inscripcion
     * DELETE /api/cursos-intersemestrales/inscripciones/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @DeleteMapping("/inscripciones/{id}")
    public ResponseEntity<Map<String, Object>> cancelarInscripcion(@PathVariable Integer id) {
        
        Solicitud solicitud = solicitudGeneralCU.obtenerSolicitudPorId(id);
        if (solicitud == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Inscripcion no encontrada");
            error.put("message", "No existe una inscripcion con el ID: " + id);
            error.put("status", 404);
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(404).body(error);
        }

        // No se puede cancelar una inscripcion ya aprobada
        List<EstadoSolicitud> estados = solicitud.getEstadosSolicitud();
        String estadoActual = estados != null && !estados.isEmpty()
            ? estados.get(estados.size() - 1).getEstado_actual() : null;
        if ("Aprobado".equalsIgnoreCase(estadoActual)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se puede cancelar");
            error.put("message", "No se puede cancelar una inscripcion que ya ha sido aprobada");
            error.put("status", 400);
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(400).body(error);
        }

        solicitudGeneralCU.eliminarSolicitud(id);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Inscripcion cancelada exitosamente");
        respuesta.put("id_inscripcion", id);
        respuesta.put("status", 200);
        respuesta.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(respuesta);
    }

    // ==================== ENDPOINTS FALTANTES PARA EL FRONTEND ====================

    /**
     * Crear nuevo curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PostMapping("/cursos-verano")
    public ResponseEntity<Map<String, Object>> crearCurso(@RequestBody CreateCursoDTO dto) {
        
        // Validar que el ID del docente sea válido
        if (dto.getId_docente() != null && dto.getId_docente() <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "ID de docente inválido");
            error.put("message", "El ID del docente debe ser mayor a 0. ID recibido: " + dto.getId_docente());
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar que el ID de la materia sea válido
        if (dto.getId_materia() != null && dto.getId_materia() <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "ID de materia inválido");
            error.put("message", "El ID de la materia debe ser mayor a 0. ID recibido: " + dto.getId_materia());
            return ResponseEntity.badRequest().body(error);
        }
        
        if (dto.getId_materia() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Materia requerida");
            error.put("message", "Debe seleccionar una materia");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (dto.getId_docente() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Docente requerido");
            error.put("message", "Debe seleccionar un docente");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (dto.getCupo_estimado() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Cupo estimado requerido");
            error.put("message", "Debe proporcionar un cupo estimado");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar fechas
        if (dto.getFecha_inicio() == null || dto.getFecha_inicio().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Fecha de inicio requerida");
            error.put("message", "Debe proporcionar una fecha de inicio");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (dto.getFecha_fin() == null || dto.getFecha_fin().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Fecha de fin requerida");
            error.put("message", "Debe proporcionar una fecha de fin");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar período académico
        if (dto.getPeriodoAcademico() == null || dto.getPeriodoAcademico().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Período académico requerido");
            error.put("message", "Debe seleccionar un período académico");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar que el período académico sea válido usando el enum (consistencia con ECAES)
        String periodoAcademicoTrimmed = dto.getPeriodoAcademico().trim();
        if (!PeriodoAcademicoEnum.esValido(periodoAcademicoTrimmed)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Período académico inválido");
            error.put("message", "El período académico '" + periodoAcademicoTrimmed + "' no es válido. Debe ser un período válido como '2025-1' o '2025-2'");
            error.put("periodosValidos", PeriodoAcademicoEnum.getPeriodosRecientes());
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar que la fecha de fin sea posterior a la fecha de inicio
        try {
            java.util.Date fechaInicio;
            java.util.Date fechaFin;
            
            try {
                fechaInicio = parsearFecha(dto.getFecha_inicio());
            } catch (java.text.ParseException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Formato de fecha de inicio inválido");
                error.put("message", "La fecha de inicio debe estar en formato ISO 8601 (ej: '2025-01-15T08:00:00Z' o '2025-01-15')");
                return ResponseEntity.badRequest().body(error);
            }
            
            try {
                fechaFin = parsearFecha(dto.getFecha_fin());
            } catch (java.text.ParseException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Formato de fecha de fin inválido");
                error.put("message", "La fecha de fin debe estar en formato ISO 8601 (ej: '2025-01-15T08:00:00Z' o '2025-01-15')");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (fechaFin.before(fechaInicio) || fechaFin.equals(fechaInicio)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Fechas inválidas");
                error.put("message", "La fecha de fin debe ser posterior a la fecha de inicio");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error validando fechas");
            error.put("message", "No se pudieron validar las fechas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
        
        // Crear el curso usando el caso de uso existente
        
        // Crear objeto de dominio del curso
        CursoOfertadoVerano cursoDominio = new CursoOfertadoVerano();
        cursoDominio.setCupo_estimado(dto.getCupo_estimado());
        
        // Obtener salón de la base de datos
        String numeroSalon = null;
        if (dto.getId_salon() != null) {
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon = 
                salonCU.obtenerSalonPorId(dto.getId_salon());
            if (salon == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Salón no encontrado");
                error.put("message", "El salón con ID " + dto.getId_salon() + " no existe");
                return ResponseEntity.badRequest().body(error);
            }
            numeroSalon = salon.getNumero_salon();
        } else if (dto.getEspacio_asignado() != null && !dto.getEspacio_asignado().trim().isEmpty()) {
            // Compatibilidad: si no hay id_salon pero hay espacio_asignado, usarlo
            numeroSalon = dto.getEspacio_asignado().trim();
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Salón requerido");
            error.put("message", "Debe seleccionar un salón de la lista disponible");
            return ResponseEntity.badRequest().body(error);
        }
        cursoDominio.setSalon(numeroSalon);
        
        // Grupo: usar el proporcionado o "A" por defecto
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano grupo;
        if (dto.getGrupo() != null && !dto.getGrupo().trim().isEmpty()) {
            try {
                grupo = co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano.valueOf(dto.getGrupo().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                grupo = co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano.A;
            }
        } else {
            grupo = co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano.A;
        }
        cursoDominio.setGrupo(grupo);
        
        // Parsear fecha de inicio (ANTES de usarla)
        java.util.Date fechaInicioDate;
        try {
            fechaInicioDate = parsearFecha(dto.getFecha_inicio());
        } catch (java.text.ParseException e) {
            // Si falla el parsing, usar la fecha actual
            fechaInicioDate = new java.util.Date();
        }
        
        // Parsear fecha de fin (ANTES de usarla)
        java.util.Date fechaFinDate;
        try {
            fechaFinDate = parsearFecha(dto.getFecha_fin());
        } catch (java.text.ParseException e) {
            // Si falla el parsing, calcular como fecha_inicio + 6 semanas
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            cal.setTime(fechaInicioDate);
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 6);
            fechaFinDate = cal.getTime();
        }
        
        // Guardar fechas y período directamente en el curso (no en estados)
        cursoDominio.setFecha_inicio(fechaInicioDate);
        cursoDominio.setFecha_fin(fechaFinDate);
        cursoDominio.setPeriodo_academico(periodoAcademicoTrimmed);
        
        // Obtener materia real de la base de datos
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materiaDominio = 
            materiaCU.obtenerMateriaPorId(dto.getId_materia().intValue());
        if (materiaDominio == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Materia no encontrada");
            error.put("message", "La materia con ID " + dto.getId_materia() + " no existe");
            return ResponseEntity.badRequest().body(error);
        }
        cursoDominio.setObjMateria(materiaDominio);
        
        // Obtener docente real de la base de datos
        Integer idDocenteParaCurso = dto.getId_docente().intValue();
        
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente docenteDominio = 
            docenteCU.obtenerDocentePorId(idDocenteParaCurso);
        if (docenteDominio == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Docente no encontrado");
            error.put("message", "El docente con ID " + dto.getId_docente() + " no existe");
            error.put("id_docente_recibido", dto.getId_docente());
            return ResponseEntity.badRequest().body(error);
        }
        
        cursoDominio.setObjDocente(docenteDominio);
        
        // Crear estado inicial del curso
        // Validar estado: debe ser uno de los estados válidos
        String estadoInicial = (dto.getEstado() != null && !dto.getEstado().trim().isEmpty()) 
            ? dto.getEstado().trim() 
            : "Abierto";
        
        // Validar que el estado sea válido
        String[] estadosValidos = {"Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado"};
        boolean estadoValido = false;
        for (String estadoVal : estadosValidos) {
            if (estadoVal.equals(estadoInicial)) {
                estadoValido = true;
                break;
            }
        }
        if (!estadoValido) {
            estadoInicial = "Abierto"; // Por defecto si el estado no es válido
        }
        
        // Crear estado inicial (solo con fecha de registro del estado, las fechas del curso van en el curso)
        EstadoCursoOfertado estadoCurso = new EstadoCursoOfertado();
        estadoCurso.setEstado_actual(estadoInicial);
        estadoCurso.setFecha_registro_estado(fechaInicioDate); // Fecha en que se registra este estado (coincide con fecha_inicio del curso al crearlo)
        // fecha_fin y periodo_academico ahora están solo en el curso, no en estados
        List<EstadoCursoOfertado> estados = new ArrayList<>();
        estados.add(estadoCurso);
        cursoDominio.setEstadosCursoOfertados(estados);
        
        
        // Validar que no exista un curso duplicado (misma materia, docente, período académico Y grupo)
        // Esto permite crear grupos A, B, C, D de la misma materia con el mismo docente en el mismo período
        if (cursoCU.existeCurso(dto.getId_materia().intValue(), dto.getId_docente().intValue(),
                periodoAcademicoTrimmed, grupo)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Curso duplicado");
            error.put("message", "Ya existe un curso con la misma materia, docente, período académico y grupo '" + grupo + "'. Puedes crear grupos diferentes (A, B, C, D) para la misma materia y docente.");
            error.put("codigo", "CURSO_DUPLICADO");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Usar el caso de uso para crear el curso
        CursoOfertadoVerano cursoCreado = cursoCU.crearCurso(cursoDominio);
        
        // Obtener información completa del curso creado desde la BD para la respuesta
        // Usar el mapper para obtener la información estructurada
        CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(cursoCreado);
        
        // Crear respuesta con datos reales del curso creado
        // Usar las fechas y período académico proporcionados por el usuario en lugar de los calculados
        Map<String, Object> nuevoCurso = new HashMap<>();
        nuevoCurso.put("id_curso", cursoCreado.getId_curso());
        nuevoCurso.put("nombre_curso", cursoDTO.getNombre_curso()); // De la materia
        nuevoCurso.put("codigo_curso", cursoDTO.getCodigo_curso()); // De la materia
        nuevoCurso.put("descripcion", cursoDTO.getDescripcion()); // Generada automáticamente
        nuevoCurso.put("fecha_inicio", dto.getFecha_inicio()); // Usar la fecha proporcionada por el usuario
        nuevoCurso.put("fecha_fin", dto.getFecha_fin()); // Usar la fecha proporcionada por el usuario
        nuevoCurso.put("periodo", periodoAcademicoTrimmed); // Usar el período académico validado (enum)
        nuevoCurso.put("cupo_maximo", cursoDTO.getCupo_maximo()); // Igual a cupo_estimado
        nuevoCurso.put("cupo_disponible", cursoDTO.getCupo_disponible()); // Igual a cupo_estimado inicialmente
        nuevoCurso.put("cupo_estimado", cursoDTO.getCupo_estimado());
        nuevoCurso.put("espacio_asignado", cursoDTO.getEspacio_asignado());
        nuevoCurso.put("estado", cursoDTO.getEstado()); // Estado actual
        nuevoCurso.put("objMateria", cursoDTO.getObjMateria());
        nuevoCurso.put("objDocente", cursoDTO.getObjDocente());
        
        nuevoCurso.put("message", "Curso creado exitosamente en la base de datos");
        
        return ResponseEntity.ok(nuevoCurso);
    }

    /**
     * Actualizar curso de verano
     * PUT /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> actualizarCurso(
            @PathVariable Long id, 
            @RequestBody UpdateCursoDTO dto) {
        
        // Validaciones basicas
        if (dto.getCupo_estimado() != null && (dto.getCupo_estimado() < 1 || dto.getCupo_estimado() > 100)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Cupo invalido");
            error.put("message", "El cupo estimado debe estar entre 1 y 100");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (dto.getEspacio_asignado() != null && dto.getEspacio_asignado().trim().length() < 3) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Espacio invalido");
            error.put("message", "El espacio asignado debe tener al menos 3 caracteres");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar estado
        if (dto.getEstado() != null) {
            String[] estadosValidos = {"Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado"};
            boolean estadoValido = false;
            for (String estado : estadosValidos) {
                if (estado.equals(dto.getEstado())) {
                    estadoValido = true;
                    break;
                }
            }
            if (!estadoValido) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Estado invalido");
                error.put("message", "El estado debe ser uno de: Borrador, Abierto, Publicado, Preinscripcion, Inscripcion, Cerrado");
                return ResponseEntity.badRequest().body(error);
            }
        }
        
        // Salón: priorizar id_salon sobre espacio_asignado (compatibilidad)
        String nuevoSalon = null;
        if (dto.getId_salon() != null) {
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon =
                salonCU.obtenerSalonPorId(dto.getId_salon());
            if (salon == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Salón no encontrado");
                error.put("message", "El salón con ID " + dto.getId_salon() + " no existe");
                return ResponseEntity.badRequest().body(error);
            }
            nuevoSalon = salon.getNumero_salon();
        } else if (dto.getEspacio_asignado() != null && !dto.getEspacio_asignado().trim().isEmpty()) {
            nuevoSalon = dto.getEspacio_asignado().trim();
        }

        if (dto.getCupo_estimado() == null && nuevoSalon == null && dto.getEstado() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Sin cambios");
            error.put("message", "No se proporcionaron datos para actualizar");
            return ResponseEntity.badRequest().body(error);
        }

        CursoOfertadoVerano cursoActualizado;
        try {
            cursoActualizado = cursoCU.modificarCurso(id.intValue(), dto.getCupo_estimado(), nuevoSalon, dto.getEstado());
        } catch (TransicionEstadoCurso.TransicionInvalidaException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valido", false);
            error.put("error", e.getError());
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
        if (cursoActualizado == null) {
            return ResponseEntity.notFound().build();
        }


        // Crear respuesta con los datos actualizados
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("id_curso", cursoActualizado.getId_curso());
        resultado.put("nombre_curso", cursoActualizado.getObjMateria() != null ? cursoActualizado.getObjMateria().getNombre() : "Curso");
        resultado.put("codigo_curso", cursoActualizado.getObjMateria() != null ? cursoActualizado.getObjMateria().getCodigo() : "N/A");
        resultado.put("cupo_estimado", cursoActualizado.getCupo_estimado());
        resultado.put("espacio_asignado", cursoActualizado.getSalon());
        
        // Obtener información del salón si existe
        if (cursoActualizado.getSalon() != null && !cursoActualizado.getSalon().trim().isEmpty()) {
            try {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon = 
                    salonCU.obtenerSalonPorNumero(cursoActualizado.getSalon());
                if (salon != null) {
                    Map<String, Object> salonInfo = new HashMap<>();
                    salonInfo.put("id_salon", salon.getId_salon());
                    salonInfo.put("numero_salon", salon.getNumero_salon());
                    salonInfo.put("edificio", salon.getEdificio());
                    salonInfo.put("activo", salon.getActivo());
                    salonInfo.put("descripcion", salon.getNumero_salon() + 
                        (salon.getEdificio() != null ? " - " + salon.getEdificio() : ""));
                    resultado.put("salon", salonInfo);
                    resultado.put("id_salon", salon.getId_salon());
                } else {
                    resultado.put("id_salon", null);
                    resultado.put("salon", null);
                }
            } catch (Exception e) {
                resultado.put("id_salon", null);
                resultado.put("salon", null);
            }
        } else {
            resultado.put("id_salon", null);
            resultado.put("salon", null);
        }
        
        resultado.put("estado", dto.getEstado() != null ? dto.getEstado() : "Actualizado");
        resultado.put("message", "Curso actualizado exitosamente");
        resultado.put("debug_info", "Cambios aplicados y guardados en BD");
        
        return ResponseEntity.ok(resultado);
    }


    /**
     * Eliminar curso de verano
     * DELETE /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @DeleteMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCurso(@PathVariable Long id) {
        
        // Verificar que el curso existe
        CursoOfertadoVerano cursoExistente = cursoCU.obtenerCursoPorId(id.intValue());
        if (cursoExistente == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Curso no encontrado");
            error.put("message", "No se encontro el curso con ID: " + id);
            return ResponseEntity.notFound().build();
        }
        
        // Verificar si hay estudiantes inscritos
        if (cursoExistente.getEstudiantesInscritos() != null && !cursoExistente.getEstudiantesInscritos().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se puede eliminar el curso");
            error.put("message", "El curso tiene estudiantes inscritos. No se puede eliminar.");
            error.put("estudiantes_inscritos", cursoExistente.getEstudiantesInscritos().size());
            return ResponseEntity.badRequest().body(error);
        }
        
        // Llamar al caso de uso para eliminar
        boolean eliminado = cursoCU.eliminarCurso(id.intValue());
        
        if (eliminado) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("message", "Curso eliminado exitosamente");
            resultado.put("id_curso_eliminado", id);
            resultado.put("nombre_curso", cursoExistente.getObjMateria() != null ? cursoExistente.getObjMateria().getNombre() : "Curso");
            
            return ResponseEntity.ok(resultado);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al eliminar el curso");
            error.put("message", "No se pudo eliminar el curso por razones desconocidas");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Obtener curso de verano por ID (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @GetMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> getCursoPorId(@PathVariable Long id) {
        
        // Obtener curso real de la base de datos - lanza EntidadNoExisteException si no existe
        CursoOfertadoVerano cursoReal = cursoCU.obtenerCursoPorId(id.intValue());
        
        // Verificar docente antes del mapeo
        if (cursoReal.getObjDocente() == null) {
        }
        
        // Usar el mapper existente para obtener datos estructurados
        CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(cursoReal);
        
        // Verificar docente después del mapeo
        if (cursoDTO.getObjDocente() == null) {
        }
        
        // Mapear a estructura esperada por el frontend
        Map<String, Object> curso = new HashMap<>();
        curso.put("id_curso", cursoDTO.getId_curso());
        curso.put("idCurso", cursoDTO.getId_curso());
        curso.put("nombre_curso", cursoDTO.getNombre_curso());
        curso.put("codigo_curso", cursoDTO.getCodigo_curso());
        curso.put("descripcion", cursoDTO.getDescripcion());
        curso.put("fecha_inicio", cursoDTO.getFecha_inicio());
        curso.put("fecha_fin", cursoDTO.getFecha_fin());
        curso.put("cupo_maximo", cursoDTO.getCupo_maximo());
        curso.put("cupo_disponible", cursoDTO.getCupo_disponible());
        curso.put("cupo_estimado", cursoDTO.getCupo_estimado());
        curso.put("espacio_asignado", cursoDTO.getEspacio_asignado());
        curso.put("estado", cursoDTO.getEstado());
        
        // Obtener información del salón si existe
        if (cursoReal.getSalon() != null && !cursoReal.getSalon().trim().isEmpty()) {
            try {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon = 
                    salonCU.obtenerSalonPorNumero(cursoReal.getSalon());
                if (salon != null) {
                    Map<String, Object> salonInfo = new HashMap<>();
                    salonInfo.put("id_salon", salon.getId_salon());
                    salonInfo.put("numero_salon", salon.getNumero_salon());
                    salonInfo.put("edificio", salon.getEdificio());
                    salonInfo.put("activo", salon.getActivo());
                    salonInfo.put("descripcion", salon.getNumero_salon() + 
                        (salon.getEdificio() != null ? " - " + salon.getEdificio() : ""));
                    curso.put("salon", salonInfo);
                    curso.put("id_salon", salon.getId_salon()); // Para facilitar la edición
                } else {
                    // Si no se encuentra el salón por número, solo poner el número
                    curso.put("id_salon", null);
                    curso.put("salon", null);
                }
            } catch (Exception e) {
                curso.put("id_salon", null);
                curso.put("salon", null);
            }
        } else {
            curso.put("id_salon", null);
            curso.put("salon", null);
        }
        
        // Obtener conteo real de preinscripciones para este curso
        try {
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(id.intValue());
            curso.put("solicitudes", preinscripciones.size());
        } catch (Exception e) {
            curso.put("solicitudes", 0);
        }
        
        // Usar la informacion del DTO que ya esta mapeada correctamente
        curso.put("objMateria", cursoDTO.getObjMateria());
        curso.put("objDocente", cursoDTO.getObjDocente());
        
        // Log final para verificar que el docente está en la respuesta
        if (curso.get("objDocente") == null) {
        } else {
        }
        
        
        return ResponseEntity.ok(curso);
    }

    /**
     * Filtrar cursos por periodo academico
     * GET /api/cursos-intersemestrales/cursos-verano/periodo/{periodo}
     */
    @GetMapping("/cursos-verano/periodo/{periodo}")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> filtrarCursosPorPeriodo(@PathVariable String periodo) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                .map(cursoMapper::postMapCurso) // Asignar idCurso
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Filtrar cursos por materia
     * GET /api/cursos-intersemestrales/cursos-verano/materia/{idMateria}
     */
    @GetMapping("/cursos-verano/materia/{idMateria}")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> filtrarCursosPorMateria(@PathVariable Integer idMateria) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        // Filtrar cursos por materia
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .filter(curso -> curso.getObjMateria() != null && 
                               curso.getObjMateria().getId_materia().equals(idMateria))
                .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                .map(cursoMapper::postMapCurso) // Asignar idCurso
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    // ==================== ENDPOINTS PARA MATERIAS Y DOCENTES ====================

    /**
     * Obtener todas las materias (endpoint actualizado para usar datos reales)
     * GET /api/cursos-intersemestrales/materias
     */
    @GetMapping("/materias")
    public ResponseEntity<List<Map<String, Object>>> getTodasLasMaterias() {
        // Obtener todas las materias de la base de datos
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materiasReales = 
            materiaCU.listarMaterias();
        
        List<Map<String, Object>> materias = new ArrayList<>();
        
        for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materiasReales) {
            Map<String, Object> materiaMap = new HashMap<>();
            materiaMap.put("id_materia", materia.getId_materia());
            materiaMap.put("nombre_materia", materia.getNombre());
            materiaMap.put("codigo_materia", materia.getCodigo());
            materiaMap.put("creditos", materia.getCreditos());
            materias.add(materiaMap);
        }
        
        return ResponseEntity.ok(materias);
    }

    /**
     * Obtener materias para el filtro de solicitudes (formato simplificado)
     * GET /api/cursos-intersemestrales/materias-filtro
     */
    @GetMapping("/materias-filtro")
    public ResponseEntity<List<Map<String, Object>>> obtenerMateriasParaFiltro() {
        try {
            
            // Obtener todas las materias de la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materiasReales = 
                materiaCU.listarMaterias();
            
            List<Map<String, Object>> materiasFiltro = new ArrayList<>();
            
            // Agregar opcion "Todas las materias"
            Map<String, Object> todasLasMaterias = new HashMap<>();
            todasLasMaterias.put("id", 0);
            todasLasMaterias.put("nombre", "Todas las materias");
            todasLasMaterias.put("codigo", "TODAS");
            materiasFiltro.add(todasLasMaterias);
            
            // Agregar materias reales
            for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materiasReales) {
                Map<String, Object> materiaMap = new HashMap<>();
                materiaMap.put("id", materia.getId_materia());
                materiaMap.put("nombre", materia.getNombre());
                materiaMap.put("codigo", materia.getCodigo());
                materiasFiltro.add(materiaMap);
            }
            
            
            return ResponseEntity.ok(materiasFiltro);
        } catch (Exception e) {
            log.error("Error obteniendo materias para filtro: {}", e.getMessage(), e);
            
            // En caso de error, devolver al menos la opcion "Todas las materias"
            List<Map<String, Object>> fallback = new ArrayList<>();
            Map<String, Object> todasLasMaterias = new HashMap<>();
            todasLasMaterias.put("id", 0);
            todasLasMaterias.put("nombre", "Todas las materias");
            todasLasMaterias.put("codigo", "TODAS");
            fallback.add(todasLasMaterias);
            
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * Obtener todos los salones disponibles
     * GET /api/cursos-intersemestrales/salones
     * 
     * @return Lista de salones activos disponibles para asignar a cursos
     */
    @GetMapping("/salones")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<List<Map<String, Object>>> obtenerSalones() {
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon> salonesReales = 
            salonCU.listarSalonesActivos();
        
        List<Map<String, Object>> salones = new ArrayList<>();
        
        for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon : salonesReales) {
            Map<String, Object> salonMap = new HashMap<>();
            salonMap.put("id_salon", salon.getId_salon());
            salonMap.put("numero_salon", salon.getNumero_salon());
            salonMap.put("edificio", salon.getEdificio() != null ? salon.getEdificio() : "Sin especificar");
            salonMap.put("activo", salon.getActivo() != null ? salon.getActivo() : true);
            // Agregar descripción completa para el frontend
            String descripcion = salon.getNumero_salon();
            if (salon.getEdificio() != null && !salon.getEdificio().trim().isEmpty()) {
                descripcion += " - " + salon.getEdificio();
            }
            salonMap.put("descripcion", descripcion);
            salones.add(salonMap);
        }
        
        // Ordenar por número de salón
        salones.sort((a, b) -> {
            String numA = (String) a.get("numero_salon");
            String numB = (String) b.get("numero_salon");
            if (numA == null) numA = "";
            if (numB == null) numB = "";
            return numA.compareToIgnoreCase(numB);
        });
        
        return ResponseEntity.ok(salones);
    }

    /**
     * Obtener todos los docentes
     * GET /api/cursos-intersemestrales/docentes
     */
    @GetMapping("/docentes")
    public ResponseEntity<List<Map<String, Object>>> getTodosLosDocentes() {
        
        // Obtener docentes reales de la base de datos
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente> docentesDominio = 
            docenteCU.listarDocentes();
        
        if (docentesDominio == null || docentesDominio.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        
        // Mapear docentes de dominio a formato de respuesta para el frontend
        List<Map<String, Object>> docentes = docentesDominio.stream()
            .map(docente -> {
                Map<String, Object> docenteMap = new HashMap<>();
                // Campo crítico: id_docente (NO id_usuario)
                docenteMap.put("id_docente", docente.getId_docente());
                docenteMap.put("codigo_docente", docente.getCodigo_docente());
                docenteMap.put("nombre_docente", docente.getNombre_docente());
                // Mantener compatibilidad con frontend (si lo necesita)
                docenteMap.put("id_usuario", docente.getId_docente()); // Usar id_docente como id_usuario para compatibilidad
                docenteMap.put("codigo_usuario", docente.getCodigo_docente());
                docenteMap.put("nombre_usuario", docente.getNombre_docente());
                
                return docenteMap;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(docentes);
    }

    // ==================== ENDPOINTS PARA GESTION DE PREINSCRIPCIONES ====================

    /**
     * Obtener preinscripciones por curso (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/preinscripciones/curso/{idCurso}
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/preinscripciones/curso/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getPreinscripcionesPorCurso(
            @PathVariable Long idCurso) {
        
        List<Map<String, Object>> preinscripciones = new ArrayList<>();
        
        // Obtener preinscripciones reales de la base de datos
        List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarPreinscripcionesPorCurso(idCurso.intValue());
        
        
        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
            Map<String, Object> preinscripcionMap = new HashMap<>();
            
            // Informacion basica de la preinscripcion
            preinscripcionMap.put("id_preinscripcion", preinscripcion.getId_solicitud());
            preinscripcionMap.put("id_solicitud", preinscripcion.getId_solicitud()); // Para compatibilidad con frontend
            preinscripcionMap.put("fecha_preinscripcion", preinscripcion.getFecha_registro_solicitud());
            
            // Estado de la solicitud
            String estado = "Pendiente"; // Valor por defecto
            if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                estado = preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
            }
            preinscripcionMap.put("estado", estado);
            
            // Observaciones
            preinscripcionMap.put("observaciones", preinscripcion.getObservacion() != null ? preinscripcion.getObservacion() : "");
            
            // Condicion de la solicitud
            preinscripcionMap.put("condicion", preinscripcion.getCodicion_solicitud() != null ? 
                preinscripcion.getCodicion_solicitud().toString() : "Primera_Vez");
            
            // Informacion del usuario/estudiante
            if (preinscripcion.getObjUsuario() != null) {
                Map<String, Object> usuarioMap = new HashMap<>();
                usuarioMap.put("id_usuario", preinscripcion.getObjUsuario().getId_usuario());
                usuarioMap.put("nombre_completo", preinscripcion.getObjUsuario().getNombre_completo());
                usuarioMap.put("correo", preinscripcion.getObjUsuario().getCorreo());
                usuarioMap.put("codigo", preinscripcion.getObjUsuario().getCodigo());
                usuarioMap.put("codigo_estudiante", preinscripcion.getCodigo_estudiante());
                
                // Informacion del rol
                if (preinscripcion.getObjUsuario().getObjRol() != null) {
                    Map<String, Object> rolMap = new HashMap<>();
                    rolMap.put("id_rol", preinscripcion.getObjUsuario().getObjRol().getId_rol());
                    rolMap.put("nombre", preinscripcion.getObjUsuario().getObjRol().getNombre());
                    usuarioMap.put("objRol", rolMap);
                }
                
                preinscripcionMap.put("objUsuario", usuarioMap);
            }
            
            // Informacion del curso usando el mapper existente
            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(preinscripcion.getObjCursoOfertadoVerano());
                
                Map<String, Object> cursoMap = new HashMap<>();
                cursoMap.put("id_curso", cursoDTO.getId_curso());
                cursoMap.put("nombre_curso", cursoDTO.getNombre_curso());
                cursoMap.put("codigo_curso", cursoDTO.getCodigo_curso());
                cursoMap.put("descripcion", cursoDTO.getDescripcion());
                cursoMap.put("fecha_inicio", cursoDTO.getFecha_inicio());
                cursoMap.put("fecha_fin", cursoDTO.getFecha_fin());
                cursoMap.put("cupo_maximo", cursoDTO.getCupo_maximo());
                cursoMap.put("cupo_estimado", cursoDTO.getCupo_estimado());
                cursoMap.put("cupo_disponible", cursoDTO.getCupo_disponible());
                cursoMap.put("espacio_asignado", cursoDTO.getEspacio_asignado());
                cursoMap.put("estado", cursoDTO.getEstado());
                cursoMap.put("objMateria", cursoDTO.getObjMateria());
                cursoMap.put("objDocente", cursoDTO.getObjDocente());
                
                preinscripcionMap.put("objCurso", cursoMap);
            }
            
            preinscripciones.add(preinscripcionMap);
        }
        
        
        return ResponseEntity.ok(preinscripciones);
    }

    /**
     * Obtener estudiantes inscritos en un curso (estudiantes que ya completaron la inscripcion)
     * GET /api/cursos-intersemestrales/inscripciones/estudiantes-elegibles/{idCurso}
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/inscripciones/estudiantes-elegibles/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getEstudiantesElegiblesParaInscripcion(
            @PathVariable Long idCurso) {
        
        List<Map<String, Object>> estudiantesInscritos = new ArrayList<>();
        
        // 1. Obtener todas las inscripciones del curso (no preinscripciones)
        List<SolicitudCursoVeranoIncripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso.intValue());
        
        // 2. Procesar cada inscripcion encontrada
        
        for (SolicitudCursoVeranoIncripcion inscripcion : inscripciones) {
            try {
                // 2.1. Verificar el estado de la inscripcion - SOLO mostrar las que NO estan aceptadas
                String estadoActual = "Inscrito"; // Estado por defecto
                if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                    estadoActual = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                }
                
                // Si la inscripcion ya fue aceptada (Pago_Validado), no la mostrar en la lista
                if ("Pago_Validado".equals(estadoActual)) {
                    continue; // Saltar esta inscripcion
                }
                
                // Crear informacion del estudiante inscrito
                Map<String, Object> estudianteInscrito = new HashMap<>();
                
                // Informacion basica del estudiante
                estudianteInscrito.put("id_usuario", inscripcion.getObjUsuario().getId_usuario());
                estudianteInscrito.put("nombre_completo", inscripcion.getObjUsuario().getNombre_completo());
                estudianteInscrito.put("codigo", inscripcion.getObjUsuario().getCodigo());
                estudianteInscrito.put("correo", inscripcion.getObjUsuario().getCorreo());
                
                // Informacion de la inscripcion
                estudianteInscrito.put("id_solicitud", inscripcion.getId_solicitud());
                estudianteInscrito.put("nombre_solicitud", inscripcion.getNombre_solicitud());
                estudianteInscrito.put("fecha_solicitud", inscripcion.getFecha_registro_solicitud());
                estudianteInscrito.put("condicion_solicitud", inscripcion.getCodicion_solicitud());
                
                // Informacion del curso
                if (inscripcion.getObjCursoOfertadoVerano() != null) {
                    estudianteInscrito.put("id_curso", inscripcion.getObjCursoOfertadoVerano().getId_curso());
                    estudianteInscrito.put("nombre_curso", inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
                    // El codigo del curso no esta disponible en esta entidad
                }
                
                // Estado de la inscripcion (ya calculado arriba)
                estudianteInscrito.put("estado_actual", estadoActual);
                
                // Informacion adicional
                estudianteInscrito.put("tipo_solicitud", "Inscripcion");
                estudianteInscrito.put("motivo_inclusion", "Estudiante inscrito en el curso");
                estudianteInscrito.put("tiene_inscripcion_formal", true);
                
                // Informacion del archivo de pago (asumir que esta validado)
                Map<String, Object> archivoPago = new HashMap<>();
                archivoPago.put("id_documento", "validado");
                archivoPago.put("nombre", "Comprobante de pago validado");
                archivoPago.put("url", "/uploads/comprobantes/");
                archivoPago.put("fecha", inscripcion.getFecha_registro_solicitud());
                estudianteInscrito.put("archivoPago", archivoPago);
                
                estudiantesInscritos.add(estudianteInscrito);
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage(), e);
            }
        }
        
        
        return ResponseEntity.ok(estudiantesInscritos);
    }

    /**
     * Obtener lista de estudiantes preinscritos e inscritos en un curso (para funcionarios)
     * GET /api/cursos-intersemestrales/cursos-verano/{idCurso}/estudiantes
     * 
     * Este endpoint permite a los funcionarios ver todos los estudiantes que están
     * preinscritos o inscritos en un curso específico, incluyendo información sobre
     * su estado y tipo de solicitud.
     */
    @GetMapping("/cursos-verano/{idCurso}/estudiantes")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<Map<String, Object>> obtenerEstudiantesDelCurso(
            @PathVariable Integer idCurso) {
        
        // Verificar que el curso existe
        CursoOfertadoVerano curso = cursoCU.obtenerCursoPorId(idCurso);
        if (curso == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Curso no encontrado con ID: " + idCurso);
            return ResponseEntity.notFound().build();
        }
        
        // Obtener preinscripciones
        List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(idCurso);
        
        // Obtener inscripciones
        List<SolicitudCursoVeranoIncripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso);
        
        // Procesar preinscripciones
        List<Map<String, Object>> estudiantesPreinscritos = new ArrayList<>();
        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripciones) {
            if (preinscripcion.getObjUsuario() == null) {
                continue;
            }
            
            Map<String, Object> estudiante = new HashMap<>();
            estudiante.put("id_usuario", preinscripcion.getObjUsuario().getId_usuario());
            estudiante.put("nombre_completo", preinscripcion.getObjUsuario().getNombre_completo());
            estudiante.put("codigo", preinscripcion.getObjUsuario().getCodigo());
            estudiante.put("correo", preinscripcion.getObjUsuario().getCorreo());
            
            // Información del programa
            if (preinscripcion.getObjUsuario().getObjPrograma() != null) {
                estudiante.put("programa", preinscripcion.getObjUsuario().getObjPrograma().getNombre_programa());
                estudiante.put("id_programa", preinscripcion.getObjUsuario().getObjPrograma().getId_programa());
            }
            
            // Estado de la preinscripción
            String estadoPreinscripcion = "Enviada";
            if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                estadoPreinscripcion = preinscripcion.getEstadosSolicitud()
                    .get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
            }
            estudiante.put("estado_preinscripcion", estadoPreinscripcion);
            estudiante.put("fecha_preinscripcion", preinscripcion.getFecha_registro_solicitud());
            estudiante.put("id_solicitud_preinscripcion", preinscripcion.getId_solicitud());
            
            // Condición de la solicitud
            if (preinscripcion.getCodicion_solicitud() != null) {
                estudiante.put("condicion", preinscripcion.getCodicion_solicitud().toString());
            }
            
            // Verificar si tiene inscripción
            boolean tieneInscripcion = false;
            String estadoInscripcion = null;
            Integer idInscripcion = null;
            for (SolicitudCursoVeranoIncripcion inscripcion : inscripciones) {
                if (inscripcion.getObjUsuario() != null && 
                    inscripcion.getObjUsuario().getId_usuario().equals(preinscripcion.getObjUsuario().getId_usuario())) {
                    tieneInscripcion = true;
                    idInscripcion = inscripcion.getId_solicitud();
                    if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoInscripcion = inscripcion.getEstadosSolicitud()
                            .get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                    }
                    break;
                }
            }
            
            estudiante.put("tiene_inscripcion", tieneInscripcion);
            estudiante.put("estado_inscripcion", estadoInscripcion != null ? estadoInscripcion : "Sin inscripción");
            estudiante.put("id_solicitud_inscripcion", idInscripcion);
            
            // Determinar el tipo basado en si tiene inscripción válida
            // Si tiene inscripción y está validada (Pago_Validado), es "Inscrito", sino "Preinscrito"
            if (tieneInscripcion && estadoInscripcion != null && "Pago_Validado".equals(estadoInscripcion)) {
                estudiante.put("tipo", "Inscrito");
            } else {
                estudiante.put("tipo", "Preinscrito");
            }
            
            estudiantesPreinscritos.add(estudiante);
        }
        
        // Procesar inscripciones que no tienen preinscripción (casos especiales)
        List<Map<String, Object>> estudiantesInscritos = new ArrayList<>();
        for (SolicitudCursoVeranoIncripcion inscripcion : inscripciones) {
            if (inscripcion.getObjUsuario() == null) {
                continue;
            }
            
            // Verificar si ya fue procesado en preinscripciones
            boolean yaProcesado = estudiantesPreinscritos.stream()
                .anyMatch(e -> e.get("id_usuario").equals(inscripcion.getObjUsuario().getId_usuario()));
            
            if (!yaProcesado) {
                Map<String, Object> estudiante = new HashMap<>();
                estudiante.put("id_usuario", inscripcion.getObjUsuario().getId_usuario());
                estudiante.put("nombre_completo", inscripcion.getObjUsuario().getNombre_completo());
                estudiante.put("codigo", inscripcion.getObjUsuario().getCodigo());
                estudiante.put("correo", inscripcion.getObjUsuario().getCorreo());
                
                // Información del programa
                if (inscripcion.getObjUsuario().getObjPrograma() != null) {
                    estudiante.put("programa", inscripcion.getObjUsuario().getObjPrograma().getNombre_programa());
                    estudiante.put("id_programa", inscripcion.getObjUsuario().getObjPrograma().getId_programa());
                }
                
                // Estado de la inscripción
                String estadoInscripcion = "Enviada";
                if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                    estadoInscripcion = inscripcion.getEstadosSolicitud()
                        .get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                }
                estudiante.put("estado_inscripcion", estadoInscripcion);
                estudiante.put("fecha_inscripcion", inscripcion.getFecha_registro_solicitud());
                estudiante.put("id_solicitud_inscripcion", inscripcion.getId_solicitud());
                
                // Condición de la solicitud
                if (inscripcion.getCodicion_solicitud() != null) {
                    estudiante.put("condicion", inscripcion.getCodicion_solicitud().toString());
                }
                
                estudiante.put("tiene_preinscripcion", false);
                estudiante.put("estado_preinscripcion", null);
                
                // Determinar el tipo: solo es "Inscrito" si está validado, sino es "Preinscrito" o "En proceso"
                if ("Pago_Validado".equals(estadoInscripcion)) {
                    estudiante.put("tipo", "Inscrito");
                } else {
                    // Si tiene inscripción pero no está validada, aún es "Preinscrito" o "En proceso de inscripción"
                    estudiante.put("tipo", "Preinscrito");
                }
                
                estudiantesInscritos.add(estudiante);
            }
        }
        
        // Combinar ambas listas
        List<Map<String, Object>> todosLosEstudiantes = new ArrayList<>();
        todosLosEstudiantes.addAll(estudiantesPreinscritos);
        todosLosEstudiantes.addAll(estudiantesInscritos);
        
        // Ordenar por nombre
        todosLosEstudiantes.sort((a, b) -> {
            String nombreA = (String) a.get("nombre_completo");
            String nombreB = (String) b.get("nombre_completo");
            if (nombreA == null) nombreA = "";
            if (nombreB == null) nombreB = "";
            return nombreA.compareToIgnoreCase(nombreB);
        });
        
        // Preparar respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id_curso", idCurso);
        respuesta.put("nombre_curso", curso.getObjMateria() != null ? curso.getObjMateria().getNombre() : "N/A");
        respuesta.put("codigo_curso", curso.getObjMateria() != null ? curso.getObjMateria().getCodigo() : "N/A");
        respuesta.put("cupo_estimado", curso.getCupo_estimado());
        // Contar estudiantes con inscripción
        
        // Contar solo estudiantes realmente inscritos (con Pago_Validado)
        long estudiantesRealmenteInscritos = todosLosEstudiantes.stream()
            .filter(e -> {
                String tipo = (String) e.get("tipo");
                String estadoInscripcion = (String) e.get("estado_inscripcion");
                return "Inscrito".equals(tipo) && estadoInscripcion != null && "Pago_Validado".equals(estadoInscripcion);
            })
            .count();
        
        respuesta.put("total_estudiantes", todosLosEstudiantes.size());
        respuesta.put("total_preinscritos", estudiantesPreinscritos.size());
        respuesta.put("total_inscritos", (int) estudiantesRealmenteInscritos);
        respuesta.put("estudiantes", todosLosEstudiantes);
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Exportar cursos filtrados a PDF
     * GET /api/cursos-intersemestrales/cursos-verano/todos/export/pdf
     * 
     * @param periodoAcademico Período académico opcional para filtrar
     * @param idPrograma ID del programa opcional para filtrar
     * @return Archivo PDF con los cursos filtrados
     */
    @GetMapping("/cursos-verano/todos/export/pdf")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<byte[]> exportarCursosPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        
        ResponseEntity<List<CursosOfertadosDTORespuesta>> cursosResponse = obtenerTodosLosCursosVerano(periodoAcademico, idPrograma);
        
        if (cursosResponse.getStatusCode() != HttpStatus.OK || cursosResponse.getBody() == null) {
            log.error("No se pudieron obtener los cursos para generar el PDF");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        List<CursosOfertadosDTORespuesta> cursos = cursosResponse.getBody();
        
        // Generar PDF
        byte[] pdfBytes = generarPDFCursos(cursos, periodoAcademico, idPrograma);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        // Nombre del archivo con fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        String filename = "cursos_verano_" + fecha + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Exportar estudiantes del curso a PDF
     * GET /api/cursos-intersemestrales/cursos-verano/{idCurso}/estudiantes/export/pdf
     * 
     * @param idCurso ID del curso
     * @return Archivo PDF con la lista de estudiantes
     */
    @GetMapping("/cursos-verano/{idCurso}/estudiantes/export/pdf")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<byte[]> exportarEstudiantesCursoPDF(
            @PathVariable Integer idCurso) {
        
        ResponseEntity<Map<String, Object>> estudiantesResponse = obtenerEstudiantesDelCurso(idCurso);
        
        if (estudiantesResponse.getStatusCode() != HttpStatus.OK || estudiantesResponse.getBody() == null) {
            log.error("No se pudieron obtener los estudiantes para generar el PDF");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        Map<String, Object> datos = estudiantesResponse.getBody();
        
        // Generar PDF
        byte[] pdfBytes = generarPDFEstudiantesCurso(datos);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        // Nombre del archivo
        String nombreCurso = (String) datos.get("codigo_curso");
        if (nombreCurso == null || nombreCurso.isEmpty()) {
            nombreCurso = "curso_" + idCurso;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        String filename = "estudiantes_" + nombreCurso + "_" + fecha + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Aceptar inscripcion de estudiante (funcionario/coordinador acepta inscripcion final)
     * PUT /api/cursos-intersemestrales/inscripciones/{idInscripcion}/aceptar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/inscripciones/{idInscripcion}/aceptar")
    public ResponseEntity<Map<String, Object>> aceptarInscripcionEstudiante(
            @PathVariable Long idInscripcion,
            @RequestBody Map<String, String> request) {
        try {
            
            String observaciones = request.get("observaciones");
            if (observaciones == null || observaciones.trim().isEmpty()) {
                observaciones = "Inscripcion aceptada por funcionario/coordinador";
            }
            
            // 1. Buscar la inscripcion directamente por ID
            SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
            
            if (inscripcion == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se encontro la inscripcion con ID: " + idInscripcion);
                return ResponseEntity.badRequest().body(error);
            }
            
            // 2. El curso debe tener cupo (si la inscripcion ya ocupaba cupo, aceptarla de nuevo no suma)
            if (!ocupaCupo(inscripcion)) {
                ResponseEntity<Map<String, Object>> sinCupo = validarCupoDisponible(inscripcion.getObjCursoOfertadoVerano());
                if (sinCupo != null) {
                    return sinCupo;
                }
            }

            // 3. Validar pago de la inscripcion (marcar como aceptada)
            SolicitudCursoVeranoIncripcion inscripcionAceptada = solicitudGateway.validarPago(
                inscripcion.getId_solicitud(), 
                true, 
                observaciones
            );
            
            if (inscripcionAceptada == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se pudo aceptar la inscripcion");
                return ResponseEntity.internalServerError().body(error);
            }
            
            // 3. Insertar en tabla cursosestudiantes (relacion Many-to-Many)
            try {
                Integer idCurso = inscripcion.getObjCursoOfertadoVerano().getId_curso();
                Integer idEstudiante = inscripcion.getObjUsuario().getId_usuario();
                if (!cursoCU.asociarEstudiante(idCurso, idEstudiante)) {
                    log.warn("No se pudo asociar el estudiante {} al curso {}", idEstudiante, idCurso);
                }
            } catch (Exception e) {
                log.error("Error asociando estudiante al curso: {}", e.getMessage(), e);
                // No fallar la operacion por esto, pero logear el error
            }
            
            // 4. Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Inscripcion aceptada exitosamente");
            respuesta.put("id_inscripcion", inscripcionAceptada.getId_solicitud());
            respuesta.put("estudiante_nombre", inscripcion.getObjUsuario().getNombre_completo());
            respuesta.put("curso_nombre", inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
            respuesta.put("fecha_aceptacion", new java.util.Date());
            respuesta.put("observaciones", observaciones);
            
            return ResponseEntity.ok(respuesta);

        } catch (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CupoAgotadoException e) {
            // Otro funcionario ocupó el último cupo mientras tanto
            return respuestaCupoAgotado(e);
        }
    }

    /**
     * Rechazar inscripcion de estudiante (funcionario/coordinador rechaza inscripcion)
     * PUT /api/cursos-intersemestrales/inscripciones/{idInscripcion}/rechazar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/inscripciones/{idInscripcion}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarInscripcionEstudiante(
            @PathVariable Long idInscripcion,
            @RequestBody Map<String, String> request) {
        
        String motivo = request.get("motivo");
        if (motivo == null || motivo.trim().isEmpty()) {
            motivo = "Inscripcion rechazada por funcionario/coordinador";
        } else {
            motivo = motivo.trim();
        }
        
        // 1. Buscar la inscripcion directamente por ID
        SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
        if (inscripcion == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se encontro la inscripcion con ID: " + idInscripcion);
            return ResponseEntity.badRequest().body(error);
        }
        
        String estadoActual = null;
        if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimo = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1);
            estadoActual = ultimo.getEstado_actual();
        }
        
        // 2. Verificar que la inscripcion este en estado valido para rechazar
        if (!"Enviada".equals(estadoActual) && !"Pago_Validado".equals(estadoActual)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "La inscripcion no puede ser rechazada en su estado actual: " + estadoActual);
            return ResponseEntity.badRequest().body(error);
        }
        
        // 3. Marcar como rechazada usando el caso de uso
        SolicitudCursoVeranoIncripcion inscripcionRechazada;
        try {
            inscripcionRechazada = solicitudCU.validarPago(
                inscripcion.getId_solicitud(), 
                false, 
                "Inscripcion rechazada: " + motivo
            );
        } catch (Exception e) {
            log.error("Error en validarPago: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al rechazar la inscripcion: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
        
        if (inscripcionRechazada == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se pudo actualizar el estado de la inscripcion");
            return ResponseEntity.internalServerError().body(error);
        }
        
        String nuevoEstado = null;
        if (inscripcionRechazada.getEstadosSolicitud() != null && !inscripcionRechazada.getEstadosSolicitud().isEmpty()) {
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoNuevo = inscripcionRechazada.getEstadosSolicitud().get(inscripcionRechazada.getEstadosSolicitud().size() - 1);
            nuevoEstado = ultimoNuevo.getEstado_actual();
        }
        
        // 4. Preparar respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Inscripcion rechazada exitosamente");
        respuesta.put("id_inscripcion", inscripcionRechazada.getId_solicitud());
        respuesta.put("estudiante_nombre", inscripcion.getObjUsuario().getNombre_completo());
        respuesta.put("curso_nombre", inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
        respuesta.put("fecha_rechazo", new java.util.Date());
        respuesta.put("motivo", motivo);
        respuesta.put("nuevo_estado", nuevoEstado);
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Actualizar observaciones de preinscripcion
     * PUT /api/cursos-intersemestrales/preinscripciones/{idPreinscripcion}/observaciones
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idPreinscripcion}/observaciones")
    public ResponseEntity<Map<String, Object>> actualizarObservacionesPreinscripcion(
            @PathVariable Long idPreinscripcion,
            @RequestBody Map<String, String> request) {
        String observaciones = request.get("observaciones");
        
        if (observaciones == null || observaciones.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Las observaciones no pueden estar vacias");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Buscar la solicitud en la base de datos
        SolicitudCursoVeranoPreinscripcion solicitud = solicitudCU.buscarSolicitudPorId(idPreinscripcion.intValue());
        if (solicitud == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se encontro la preinscripcion con ID: " + idPreinscripcion);
            return ResponseEntity.notFound().build();
        }
        
        // Actualizar las observaciones directamente usando el gateway de solicitudes de curso verano
        solicitud.setObservacion(observaciones);
        SolicitudCursoVeranoPreinscripcion solicitudActualizada = solicitudGateway.actualizarSolicitudCursoVerano(solicitud);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("message", "Observaciones actualizadas exitosamente");
        respuesta.put("success", true);
        
        Map<String, Object> preinscripcion = new HashMap<>();
        preinscripcion.put("id_preinscripcion", solicitudActualizada.getId_solicitud());
        preinscripcion.put("observaciones", solicitudActualizada.getObservacion());
        preinscripcion.put("estado", solicitudActualizada.getEstadosSolicitud() != null && !solicitudActualizada.getEstadosSolicitud().isEmpty() 
            ? solicitudActualizada.getEstadosSolicitud().get(solicitudActualizada.getEstadosSolicitud().size() - 1).getEstado_actual() : "Pendiente");
        
        respuesta.put("preinscripcion", preinscripcion);
        
        return ResponseEntity.ok(respuesta);
    }

    

    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR PREINSCRIPCIONES ====================

    /**
     * Aprobar preinscripcion (para funcionarios y coordinadores)
     * PUT /api/cursos-intersemestrales/preinscripciones/{idSolicitud}/aprobar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idSolicitud}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody(required = false) Map<String, String> requestBody) {
        String comentarios = requestBody != null ? requestBody.get("comentarios") : null;
        SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Preinscripcion aprobada exitosamente");
        respuesta.put("id_solicitud", solicitudAprobada.getId_solicitud());
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechazar preinscripcion (para funcionarios y coordinadores)
     * PUT /api/cursos-intersemestrales/preinscripciones/{idSolicitud}/rechazar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idSolicitud}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody Map<String, String> requestBody) {
        String motivo = requestBody.get("motivo");
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar un motivo para el rechazo"));
        }
        motivo = motivo.trim();
        
        SolicitudCursoVeranoPreinscripcion solicitudRechazada = solicitudCU.rechazarPreinscripcion(idSolicitud, motivo);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Preinscripcion rechazada");
        respuesta.put("id_solicitud", solicitudRechazada.getId_solicitud());
        respuesta.put("motivo", motivo);
        
        return ResponseEntity.ok(respuesta);
    }

    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR SOLICITUDES ====================

    // ==================== ENDPOINT PARA CONFIRMAR INSCRIPCION ====================

    /**
     * Confirmar inscripcion de estudiante
     * PUT /api/cursos-intersemestrales/inscripciones/{id}/confirmar
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id.intValue())")
    @PutMapping("/inscripciones/{id}/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarInscripcion(@PathVariable Long id) {
        // Simular confirmacion de inscripcion
        Map<String, Object> inscripcion = new HashMap<>();
        inscripcion.put("id", id);
        inscripcion.put("fecha", "2024-01-15T10:30:00");
        inscripcion.put("estado", "inscrito"); // Cambiado de "pendiente" a "inscrito"
        inscripcion.put("estudianteId", 1);
        inscripcion.put("cursoId", 1);
        
        // Informacion completa del estudiante
        Map<String, Object> estudiante = new HashMap<>();
        estudiante.put("id_usuario", 1);
        estudiante.put("nombre", "Ana");
        estudiante.put("apellido", "Gonzalez");
        estudiante.put("email", "ana.gonzalez@unicauca.edu.co");
        estudiante.put("codigo_estudiante", "104612345660");
        inscripcion.put("estudiante", estudiante);
        
        // Informacion del archivo de pago (ya existia)
        Map<String, Object> archivoPago = new HashMap<>();
        archivoPago.put("id_documento", 1);
        archivoPago.put("nombre", "comprobante_pago_ana.pdf");
        archivoPago.put("url", "/uploads/comprobante_pago_ana.pdf");
        archivoPago.put("fecha", "2024-01-15T10:30:00");
        inscripcion.put("archivoPago", archivoPago);
        
        // Mensaje de confirmacion
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("message", "Inscripcion confirmada exitosamente");
        respuesta.put("inscripcion", inscripcion);
        
        return ResponseEntity.ok(respuesta);
    }

    // ==================== ENDPOINT PARA DESCARGAR COMPROBANTE DE PAGO ====================

    

    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idInscripcion.intValue())")
    @GetMapping("/inscripciones/{idInscripcion}/info")
    public ResponseEntity<Map<String, Object>> infoInscripcion(@PathVariable Long idInscripcion) {
        
        // 1. Buscar la inscripcion directamente por ID
        SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
        Map<String, Object> resultado = new HashMap<>();
        
        if (inscripcion == null) {
            resultado.put("encontrada", false);
            resultado.put("mensaje", "Inscripcion no encontrada");
            return ResponseEntity.ok(resultado);
        }
        
        resultado.put("encontrada", true);
        resultado.put("id_solicitud", inscripcion.getId_solicitud());
        resultado.put("nombre_solicitud", inscripcion.getNombre_solicitud());
        resultado.put("fecha_registro", inscripcion.getFecha_registro_solicitud());
        
        // Verificar documentos
        List<Documento> documentos = inscripcion.getDocumentos();
        resultado.put("documentos_count", documentos != null ? documentos.size() : "NULL");
        resultado.put("documentos_null", documentos == null);
        resultado.put("documentos_empty", documentos != null && documentos.isEmpty());
        
        if (documentos != null && !documentos.isEmpty()) {
            List<Map<String, Object>> documentosInfo = new ArrayList<>();
            for (Documento doc : documentos) {
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.getId_documento());
                docInfo.put("nombre", doc.getNombre());
                docInfo.put("ruta", doc.getRuta_documento());
                docInfo.put("es_valido", doc.isEsValido());
                documentosInfo.add(docInfo);
            }
            resultado.put("documentos", documentosInfo);
            
            // Agregar informacion especifica del archivo para descarga
            Documento primerDocumento = documentos.get(0);
            resultado.put("archivo_pago", Map.of(
                "nombre_archivo", primerDocumento.getNombre(),
                "ruta_archivo", primerDocumento.getRuta_documento(),
                "id_documento", primerDocumento.getId_documento()
            ));
        }
        
        return ResponseEntity.ok(resultado);
    }


    // ==================== ENDPOINT PARA SOLICITUDES DE CURSO NUEVO ====================

    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/inscripciones/curso/{idCurso}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasInscripciones(@PathVariable Long idCurso) {
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Contar inscripciones por estado con logging detallado
        Integer pendientes = solicitudGateway.contarInscripcionesPorEstado(idCurso.intValue(), "Enviada");
        Integer aceptadas = solicitudGateway.contarInscripcionesPorEstado(idCurso.intValue(), "Pago_Validado");
        Integer rechazadas = solicitudGateway.contarInscripcionesPorEstado(idCurso.intValue(), "Pago_Rechazado");
        
        
        Integer totalInscripciones = pendientes + aceptadas + rechazadas;
        
        estadisticas.put("total_inscripciones", totalInscripciones);
        estadisticas.put("pendientes_revision", pendientes);
        estadisticas.put("aceptadas", aceptadas);
        estadisticas.put("rechazadas", rechazadas);
        estadisticas.put("curso_id", idCurso);
        estadisticas.put("fecha_consulta", new java.util.Date());
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Descargar comprobante de pago por ID de inscripcion (generico para cualquier estudiante)
     * GET /api/cursos-intersemestrales/inscripciones/{idInscripcion}/comprobante
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idInscripcion.intValue())")
    @GetMapping("/inscripciones/{idInscripcion}/comprobante")
    public ResponseEntity<?> descargarComprobantePago(@PathVariable Long idInscripcion) {
        
        // Validar ID
        if (idInscripcion == null || idInscripcion <= 0) {
            return ResponseEntity.badRequest().body("ID de inscripcion invalido");
        }
        
        // 1. Buscar la inscripcion
        SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
        if (inscripcion == null) {
            return ResponseEntity.status(404)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Inscripcion no encontrada\",\"idInscripcion\":" + idInscripcion + "}");
        }
        
        // 2. Buscar documentos asociados
        List<Documento> documentos = inscripcion.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.status(404)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"No hay documentos asociados a esta inscripcion\",\"idInscripcion\":" + idInscripcion + "}");
        }
        
        
        // 3. Buscar el primer documento PDF (comprobante de pago)
        for (Documento documento : documentos) {
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                try {
                    // Lógica adaptativa: usar ruta completa si está organizada, sino usar nombre
                    String rutaDocumento = documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre();
                    byte[] archivo;
                    
                    try {
                        if (rutaDocumento != null && rutaDocumento.contains("/")) {
                            // Ruta organizada (nueva estructura)
                            archivo = objGestionarArchivos.getFileByPath(rutaDocumento);
                        } else {
                            // Ruta simple (compatibilidad hacia atrás)
                            archivo = objGestionarArchivos.getFile(documento.getNombre());
                        }
                    } catch (Exception e) {
                        // Si falla con la ruta, intentar con el nombre como fallback
                        if (documento.getRuta_documento() != null && !documento.getRuta_documento().equals(documento.getNombre())) {
                            archivo = objGestionarArchivos.getFile(documento.getNombre());
                        } else {
                            throw e; // Re-lanzar si no hay alternativa
                        }
                    }
                    
                    if (archivo == null || archivo.length == 0) {
                        continue; // Probar el siguiente documento
                    }
                    
                    // Configurar headers para descarga
                    String contentDisposition = "attachment; filename=\"" + documento.getNombre() + "\"";
                    
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(archivo);
                        
                } catch (Exception e) {
                    log.error("Error procesando documento: {} - {}", 
                        documento.getNombre(), e.getMessage(), e);
                    continue; // Probar el siguiente documento
                }
            }
        }
        
        return ResponseEntity.status(404)
            .header("Content-Type", "application/json")
            .body("{\"error\":\"No se encontro ningun comprobante PDF valido\",\"idInscripcion\":" + idInscripcion + "}");
    }

    /**
     * Método auxiliar para parsear fechas correctamente, evitando problemas de zona horaria.
     * Si la fecha viene sin hora (formato yyyy-MM-dd), se establece al final del día (23:59:59) en UTC.
     * 
     * @param fechaStr String con la fecha a parsear
     * @return Date parseado correctamente
     * @throws java.text.ParseException si no se puede parsear la fecha
     */
    private java.util.Date parsearFecha(String fechaStr) throws java.text.ParseException {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            throw new java.text.ParseException("Fecha vacía", 0);
        }
        
        // Intentar primero con formato ISO completo (con hora y zona horaria)
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            sdf.setLenient(false);
            return sdf.parse(fechaStr);
        } catch (java.text.ParseException e) {
            // Si falla, intentar con formato ISO sin la Z
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                sdf.setLenient(false);
                return sdf.parse(fechaStr);
            } catch (java.text.ParseException e2) {
                // Si falla, intentar con formato simple (solo fecha)
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    sdf.setLenient(false);
                    java.util.Date fecha = sdf.parse(fechaStr);
                    
                    // Si es formato simple, establecer la hora al final del día (23:59:59) en UTC
                    // para evitar problemas de zona horaria que muestren un día anterior
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                    cal.setTime(fecha);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    cal.set(java.util.Calendar.MINUTE, 59);
                    cal.set(java.util.Calendar.SECOND, 59);
                    cal.set(java.util.Calendar.MILLISECOND, 999);
                    return cal.getTime();
                } catch (java.text.ParseException e3) {
                    throw new java.text.ParseException("Formato de fecha inválido: " + fechaStr, 0);
                }
            }
        }
    }

    /**
     * Genera un PDF con los cursos filtrados
     */
    private byte[] generarPDFCursos(List<CursosOfertadosDTORespuesta> cursos, String periodoAcademico, Integer idPrograma) {
        
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
                    "GESTIÓN DE CURSOS INTERSEMESTRALES", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información de filtros y fecha
            com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            StringBuilder info = new StringBuilder();
            info.append("Fecha de generación: ").append(formatearFechaEspanol(new Date())).append("\n");
            
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty() && !periodoAcademico.trim().equalsIgnoreCase("todos")) {
                info.append("Período Académico: ").append(periodoAcademico).append("\n");
            } else {
                try {
                    PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
                    if (periodoActual != null) {
                        info.append("Período Académico: Todos (Período actual: ").append(periodoActual.getValor()).append(")\n");
                    } else {
                        info.append("Período Académico: Todos los períodos\n");
                    }
                } catch (Exception e) {
                    info.append("Período Académico: Todos los períodos\n");
                }
            }
            
            if (idPrograma != null) {
                info.append("Programa: ID ").append(idPrograma).append("\n");
            }
            
            info.append("Total de cursos: ").append(cursos.size()).append("\n");
            
            com.itextpdf.text.Paragraph infoParagraph = new com.itextpdf.text.Paragraph(info.toString(), infoFont);
            infoParagraph.setSpacingAfter(15);
            document.add(infoParagraph);
            
            // Tabla de cursos
            if (cursos != null && !cursos.isEmpty()) {
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(10);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1f, 2f, 1f, 1.5f, 1f, 1.5f, 2f, 1f, 1f, 1.5f});
                
                // Encabezados
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Código", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Nombre", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Período", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Grupo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Docente", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Fecha Inicio", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Fecha Fin", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cupo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Salón", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado", headerFont)));
                
                // Datos
                com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                
                for (CursosOfertadosDTORespuesta curso : cursos) {
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getCodigo_curso() != null ? curso.getCodigo_curso() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getNombre_curso() != null ? curso.getNombre_curso() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getPeriodo() != null ? curso.getPeriodo() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getGrupo() != null ? curso.getGrupo() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getObjDocente() != null && curso.getObjDocente().getNombre_docente() != null 
                                    ? curso.getObjDocente().getNombre_docente() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            formatearFechaISO(curso.getFecha_inicio()), dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            formatearFechaISO(curso.getFecha_fin()), dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getCupo_estimado() != null ? curso.getCupo_estimado().toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getSalon() != null ? curso.getSalon() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            curso.getEstado() != null ? curso.getEstado() : "-", dataFont)));
                }
                
                document.add(table);
            } else {
                com.itextpdf.text.Paragraph noData = new com.itextpdf.text.Paragraph(
                        "No hay cursos disponibles con los filtros aplicados.", infoFont);
                noData.setSpacingAfter(15);
                document.add(noData);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar PDF de cursos: {}", e.getMessage(), e);
            if (document != null && document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Error al generar PDF de cursos", e);
        }
    }

    /**
     * Genera un PDF con la lista de estudiantes del curso
     */
    @SuppressWarnings("unchecked")
    private byte[] generarPDFEstudiantesCurso(Map<String, Object> datos) {
        
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
            String nombreCurso = (String) datos.get("nombre_curso");
            String codigoCurso = (String) datos.get("codigo_curso");
            String titulo = "ESTUDIANTES DEL CURSO: " + (nombreCurso != null ? nombreCurso : "N/A");
            if (codigoCurso != null) {
                titulo += " (" + codigoCurso + ")";
            }
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(titulo, titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información del curso
            com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            StringBuilder info = new StringBuilder();
            info.append("Fecha de generación: ").append(formatearFechaEspanol(new Date())).append("\n");
            info.append("Cupo Estimado: ").append(datos.get("cupo_estimado")).append("\n");
            info.append("Total Estudiantes: ").append(datos.get("total_estudiantes")).append("\n");
            info.append("Preinscritos: ").append(datos.get("total_preinscritos")).append("\n");
            info.append("Inscritos: ").append(datos.get("total_inscritos")).append("\n");
            
            com.itextpdf.text.Paragraph infoParagraph = new com.itextpdf.text.Paragraph(info.toString(), infoFont);
            infoParagraph.setSpacingAfter(15);
            document.add(infoParagraph);
            
            // Tabla de estudiantes
            List<Map<String, Object>> estudiantes = (List<Map<String, Object>>) datos.get("estudiantes");
            if (estudiantes != null && !estudiantes.isEmpty()) {
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(8);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{0.5f, 2.5f, 1f, 2f, 1f, 1.5f, 1.5f, 1.5f});
                
                // Encabezados
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("#", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Nombre Completo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Código", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Programa", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Tipo", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado Preinscripción", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Estado Inscripción", headerFont)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Correo", headerFont)));
                
                // Datos
                com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                
                int contador = 1;
                for (Map<String, Object> estudiante : estudiantes) {
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            String.valueOf(contador++), dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("nombre_completo") != null ? estudiante.get("nombre_completo").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("codigo") != null ? estudiante.get("codigo").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("programa") != null ? estudiante.get("programa").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("tipo") != null ? estudiante.get("tipo").toString() : "-", dataFont)));
                    String estadoPreinscripcion = estudiante.get("estado_preinscripcion") != null 
                            ? estudiante.get("estado_preinscripcion").toString() : "N/A";
                    if ("-".equals(estadoPreinscripcion) || estadoPreinscripcion.isEmpty() || estadoPreinscripcion.trim().isEmpty()) {
                        estadoPreinscripcion = "N/A";
                    }
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(estadoPreinscripcion, dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("estado_inscripcion") != null ? estudiante.get("estado_inscripcion").toString() : "-", dataFont)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                            estudiante.get("correo") != null ? estudiante.get("correo").toString() : "-", dataFont)));
                }
                
                document.add(table);
            } else {
                com.itextpdf.text.Paragraph noData = new com.itextpdf.text.Paragraph(
                        "No hay estudiantes registrados en este curso.", infoFont);
                noData.setSpacingAfter(15);
                document.add(noData);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar PDF de estudiantes: {}", e.getMessage(), e);
            if (document != null && document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Error al generar PDF de estudiantes", e);
        }
    }

    /**
     * Formatea una fecha en español sin hora
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "";
        }
        try {
            java.util.Locale spanishLocale = new java.util.Locale("es", "ES");
            SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", spanishLocale);
            return sdf.format(fecha);
        } catch (Exception e) {
            return fecha.toString();
        }
    }
    
    /**
     * Formatea una fecha ISO (String) a formato legible (dd/MM/yyyy)
     * Acepta formatos como "2026-06-16T16:55:43Z" o "2026-06-16"
     */
    private String formatearFechaISO(String fechaISO) {
        if (fechaISO == null || fechaISO.trim().isEmpty()) {
            return "-";
        }
        
        try {
            // Intentar parsear formato ISO completo (con hora)
            if (fechaISO.contains("T")) {
                String fechaPart = fechaISO.split("T")[0]; // Extraer solo la parte de la fecha
                String[] partes = fechaPart.split("-");
                if (partes.length == 3) {
                    return partes[2] + "/" + partes[1] + "/" + partes[0]; // dd/MM/yyyy
                }
            } else if (fechaISO.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // Formato simple yyyy-MM-dd
                String[] partes = fechaISO.split("-");
                return partes[2] + "/" + partes[1] + "/" + partes[0]; // dd/MM/yyyy
            }
            
            // Si no se puede parsear, retornar el original
            return fechaISO;
        } catch (Exception e) {
            return fechaISO;
        }
    }
    
    /**
     * Normaliza el formato del período académico a YYYY-P
     * Acepta formatos como "2025 segundo semestre", "2025-2", "Segundo Período 2025", etc.
     */
    private String normalizarPeriodoAcademico(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) {
            return periodo;
        }
        
        String periodoLimpio = periodo.trim();
        
        // Si ya está en formato YYYY-P, retornarlo
        if (periodoLimpio.matches("\\d{4}-[12]")) {
            return periodoLimpio;
        }
        
        // Intentar extraer año y período de formatos como "2025 segundo semestre" o "Segundo Período 2025"
        try {
            // Buscar año (4 dígitos)
            java.util.regex.Pattern añoPattern = java.util.regex.Pattern.compile("(\\d{4})");
            java.util.regex.Matcher añoMatcher = añoPattern.matcher(periodoLimpio);
            
            if (añoMatcher.find()) {
                String año = añoMatcher.group(1);
                
                // Buscar indicador de período
                String periodoLower = periodoLimpio.toLowerCase();
                int numeroPeriodo = 1; // Por defecto primer período
                
                if (periodoLower.contains("segundo") || periodoLower.contains("2") || periodoLower.contains("ii")) {
                    numeroPeriodo = 2;
                } else if (periodoLower.contains("primer") || periodoLower.contains("primero") || periodoLower.contains("1") || periodoLower.contains("i")) {
                    numeroPeriodo = 1;
                }
                
                String periodoNormalizado = año + "-" + numeroPeriodo;
                return periodoNormalizado;
            }
        } catch (Exception e) {
        }
        
        // Si no se pudo normalizar, retornar el original
        return periodoLimpio;
    }

    /**
     * Verifica que el curso tenga cupo. El cupo del curso es {@code cupo_estimado}
     * (el frontend lo muestra como cupo máximo). Ocupan cupo las inscripciones con pago
     * validado o completadas.
     *
     * @return null si hay cupo; si no, una respuesta 409 con código CUPO_AGOTADO
     */
    private ResponseEntity<Map<String, Object>> validarCupoDisponible(
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano curso) {
        if (curso == null || curso.getId_curso() == null || curso.getCupo_estimado() == null
                || curso.getCupo_estimado() <= 0) {
            return null; // Curso sin cupo definido: no se limita
        }
        Integer ocupados = solicitudGateway.contarInscripcionesAceptadasPorCurso(curso.getId_curso());
        int ocupadosSeguro = ocupados != null ? ocupados : 0;
        if (ocupadosSeguro >= curso.getCupo_estimado()) {
            return respuestaCupoAgotado(new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos
                    .CupoAgotadoException(curso.getCupo_estimado(), ocupadosSeguro));
        }
        return null;
    }

    /** 409 con código CUPO_AGOTADO. */
    static ResponseEntity<Map<String, Object>> respuestaCupoAgotado(
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CupoAgotadoException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("codigo", "CUPO_AGOTADO");
        error.put("cupo", e.getCupo());
        error.put("ocupados", e.getOcupados());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /** true si el último estado de la inscripción ya ocupa un cupo del curso. */
    private boolean ocupaCupo(SolicitudCursoVeranoIncripcion inscripcion) {
        if (inscripcion.getEstadosSolicitud() == null || inscripcion.getEstadosSolicitud().isEmpty()) {
            return false;
        }
        String ultimo = inscripcion.getEstadosSolicitud()
                .get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
        return co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway
                .GestionarSolicitudCursoVeranoGatewayImplAdapter.ESTADOS_INSCRIPCION_QUE_OCUPAN_CUPO.contains(ultimo);
    }

}


