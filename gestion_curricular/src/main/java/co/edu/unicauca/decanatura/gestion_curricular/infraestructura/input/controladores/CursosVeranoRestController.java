package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CreateCursoDTO;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UpdateCursoDTO;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorPdfCursosVerano;

/**
 * Cursos de verano: listados, creación, edición, eliminación, estudiantes del curso y exportación a PDF.
 * Ruta base compartida con los demás controladores de cursos de verano.
 */
@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class CursosVeranoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarMateriasCUIntPort materiaCU;
    private final GestionarDocentesCUIntPort docenteCU;
    private final CursosOfertadosMapperDominio cursoMapper;
    private final GestionarSalonesCUIntPort salonCU;
    private final GeneradorPdfCursosVerano generadorPdf;

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
            String periodoNormalizado = SoporteCursosVerano.normalizarPeriodoAcademico(periodoFiltro);
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
        byte[] pdfBytes = generadorPdf.generarPDFCursos(cursos, periodoAcademico, idPrograma);
        
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
        byte[] pdfBytes = generadorPdf.generarPDFEstudiantesCurso(datos);
        
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
}
