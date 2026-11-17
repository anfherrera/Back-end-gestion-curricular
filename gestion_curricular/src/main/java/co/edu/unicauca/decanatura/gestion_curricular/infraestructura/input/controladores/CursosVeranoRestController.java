package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesCursosVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort.DatosCreacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.ActualizarCursoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CrearCursoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.BorradoBloqueado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.CursoActualizado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.CursoCreado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.CursoEliminado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.Detalle;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.ErrorCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.SalonInfo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.TransicionRechazada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta.Estudiante;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta.InscripcionSinPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta.Preinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstudiantesCursoVeranoMapper;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

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
    private final CursosOfertadosMapperDominio cursoMapper;
    private final GestionarSalonesCUIntPort salonCU;
    private final GenerarReportesCursosVeranoCUIntPort generadorPdf;

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
        List<CursosOfertadosDTORespuesta> respuesta = cursoCU
                .listarPorFiltros(PeriodoAcademicoEnum.oActual(periodoAcademico), idPrograma, false).stream()
                .map(cursoMapper::mapearDeCursoOfertadoARespuestaDisponible)
                .map(cursoMapper::postMapCurso)
                .toList();

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
        boolean sinFiltroDePeriodo = Boolean.TRUE.equals(todosLosPeriodos)
                || "todos".equalsIgnoreCase(periodoAcademico == null ? null : periodoAcademico.trim());
        String periodo = sinFiltroDePeriodo ? null : PeriodoAcademicoEnum.oActual(periodoAcademico);
        List<CursosOfertadosDTORespuesta> respuesta = cursoCU.listarPorFiltros(periodo, idPrograma, true).stream()
                .map(cursoMapper::mapearDeCursoOfertadoARespuestaDisponible)
                .toList();

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
        String periodo = periodoAcademico == null ? "" : periodoAcademico.trim();
        String periodoFiltro = periodo.isEmpty() || "todos".equalsIgnoreCase(periodo)
                ? null : SoporteCursosVerano.normalizarPeriodoAcademico(periodo);
        List<CursoOfertadoVerano> cursos = cursoCU.listarPorFiltros(periodoFiltro, idPrograma, false);
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(cursoMapper::mapearDeCursoOfertadoARespuesta)
                .map(dto -> {
                    Salon salon = salonCU.resolverSalonDeCurso(dto.getSalon(), true);
                    dto.setSalonInfo(salonInfo(salon));
                    dto.setId_salon(salon == null ? null : salon.getId_salon());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    private static SalonInfo salonInfo(Salon salon) {
        if (salon == null) {
            return null;
        }
        return new SalonInfo(salon.getId_salon(), salon.getNumero_salon(), salon.getEdificio(), salon.getActivo(),
                salon.getNumero_salon() + (salon.getEdificio() != null ? " - " + salon.getEdificio() : ""));
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
                .map(cursoMapper::mapearDeCursoOfertadoARespuesta)
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
                .map(cursoMapper::mapearDeCursoOfertadoARespuesta)
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
    public ResponseEntity<?> crearCurso(@RequestBody CrearCursoDTOPeticion dto) {
        if (dto.getId_docente() != null && dto.getId_docente() <= 0) {
            return errorCurso("ID de docente inválido",
                    "El ID del docente debe ser mayor a 0. ID recibido: " + dto.getId_docente());
        }
        if (dto.getId_materia() != null && dto.getId_materia() <= 0) {
            return errorCurso("ID de materia inválido",
                    "El ID de la materia debe ser mayor a 0. ID recibido: " + dto.getId_materia());
        }
        if (dto.getId_materia() == null) {
            return errorCurso("Materia requerida", "Debe seleccionar una materia");
        }
        if (dto.getId_docente() == null) {
            return errorCurso("Docente requerido", "Debe seleccionar un docente");
        }
        if (dto.getCupo_estimado() == null) {
            return errorCurso("Cupo estimado requerido", "Debe proporcionar un cupo estimado");
        }
        if (dto.getFecha_inicio() == null || dto.getFecha_inicio().trim().isEmpty()) {
            return errorCurso("Fecha de inicio requerida", "Debe proporcionar una fecha de inicio");
        }
        if (dto.getFecha_fin() == null || dto.getFecha_fin().trim().isEmpty()) {
            return errorCurso("Fecha de fin requerida", "Debe proporcionar una fecha de fin");
        }
        if (dto.getPeriodoAcademico() == null || dto.getPeriodoAcademico().trim().isEmpty()) {
            return errorCurso("Período académico requerido", "Debe seleccionar un período académico");
        }

        String periodo = dto.getPeriodoAcademico().trim();
        if (!PeriodoAcademicoEnum.esValido(periodo)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Período académico inválido",
                    "message", "El período académico '" + periodo
                            + "' no es válido. Debe ser un período válido como '2025-1' o '2025-2'",
                    "periodosValidos", PeriodoAcademicoEnum.getPeriodosRecientes()));
        }

        Date fechaInicio;
        Date fechaFin;
        String tipoFecha = "inicio";
        try {
            fechaInicio = parsearFecha(dto.getFecha_inicio());
            tipoFecha = "fin";
            fechaFin = parsearFecha(dto.getFecha_fin());
        } catch (ParseException e) {
            return errorCurso("Formato de fecha de " + tipoFecha + " inválido",
                    "La fecha de " + tipoFecha + " debe estar en formato ISO 8601 (ej: '2025-01-15T08:00:00Z' o '2025-01-15')");
        }

        CursoOfertadoVerano cursoCreado;
        try {
            cursoCreado = cursoCU.crearCurso(new DatosCreacion(dto.getId_materia().intValue(),
                    dto.getId_docente().intValue(), dto.getCupo_estimado(), fechaInicio, fechaFin, periodo,
                    dto.getId_salon(), dto.getEspacio_asignado(), dto.getGrupo(), dto.getEstado()));
        } catch (DatosCursoInvalidosException e) {
            return errorDatosCurso(e);
        }

        CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mapearDeCursoOfertadoARespuesta(cursoCreado);
        
        // Crear respuesta con datos reales del curso creado
        // Usar las fechas y período académico proporcionados por el usuario en lugar de los calculados
        return ResponseEntity.ok(new CursoCreado(cursoCreado.getId_curso(), cursoDTO.getNombre_curso(),
                cursoDTO.getCodigo_curso(), cursoDTO.getDescripcion(), dto.getFecha_inicio(), dto.getFecha_fin(),
                periodo, cursoDTO.getCupo_maximo(), cursoDTO.getCupo_disponible(), cursoDTO.getCupo_estimado(),
                cursoDTO.getEspacio_asignado(), cursoDTO.getEstado(), cursoDTO.getObjMateria(),
                cursoDTO.getObjDocente(), "Curso creado exitosamente en la base de datos"));
    }

    private ResponseEntity<Map<String, String>> errorCurso(String error, String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("error", error, "message", mensaje));
    }

    private ResponseEntity<Map<String, String>> errorDatosCurso(DatosCursoInvalidosException e) {
        return switch (e.getMotivo()) {
            case FECHAS_INVALIDAS -> errorCurso("Fechas inválidas", e.getMessage());
            case SALON_REQUERIDO -> errorCurso("Salón requerido", e.getMessage());
            case SALON_NO_ENCONTRADO -> errorCurso("Salón no encontrado", e.getMessage());
            case CURSO_DUPLICADO -> ResponseEntity.badRequest().body(Map.of(
                    "error", "Curso duplicado", "message", e.getMessage(), "codigo", "CURSO_DUPLICADO"));
        };
    }

    private Date parsearFecha(String texto) throws ParseException {
        String soloFecha = "yyyy-MM-dd";
        TimeZone utc = TimeZone.getTimeZone("UTC");
        for (String formato : List.of("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss", soloFecha)) {
            SimpleDateFormat parser = new SimpleDateFormat(formato);
            parser.setTimeZone(utc);
            parser.setLenient(false);
            Date fecha;
            try {
                fecha = parser.parse(texto);
            } catch (ParseException e) {
                continue;
            }
            if (!formato.equals(soloFecha)) {
                return fecha;
            }

            Calendar calendario = Calendar.getInstance(utc);
            calendario.setTime(fecha);
            calendario.set(Calendar.HOUR_OF_DAY, 23);
            calendario.set(Calendar.MINUTE, 59);
            calendario.set(Calendar.SECOND, 59);
            calendario.set(Calendar.MILLISECOND, 999);
            return calendario.getTime();
        }
        throw new ParseException("Formato de fecha inválido: " + texto, 0);
    }

    /**
     * Actualizar curso de verano
     * PUT /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/cursos-verano/{id}")
    public ResponseEntity<?> actualizarCurso(
            @PathVariable Long id,
            @RequestBody ActualizarCursoDTOPeticion dto) {

        // Validaciones basicas
        if (dto.getCupo_estimado() != null && (dto.getCupo_estimado() < 1 || dto.getCupo_estimado() > 100)) {
            return ResponseEntity.badRequest().body(new ErrorCurso("Cupo invalido",
                    "El cupo estimado debe estar entre 1 y 100"));
        }

        if (dto.getEspacio_asignado() != null && dto.getEspacio_asignado().trim().length() < 3) {
            return ResponseEntity.badRequest().body(new ErrorCurso("Espacio invalido",
                    "El espacio asignado debe tener al menos 3 caracteres"));
        }
        
        if (dto.getEstado() != null && !TransicionEstadoCurso.esEstadoValido(dto.getEstado())) {
            return errorCurso("Estado invalido",
                    "El estado debe ser uno de: Borrador, Abierto, Publicado, Preinscripcion, Inscripcion, Cerrado");
        }

        String nuevoSalon;
        try {
            nuevoSalon = cursoCU.resolverSalon(dto.getId_salon(), dto.getEspacio_asignado());
        } catch (DatosCursoInvalidosException e) {
            return errorDatosCurso(e);
        }

        if (dto.getCupo_estimado() == null && nuevoSalon == null && dto.getEstado() == null) {
            return ResponseEntity.badRequest().body(new ErrorCurso("Sin cambios",
                    "No se proporcionaron datos para actualizar"));
        }

        CursoOfertadoVerano cursoActualizado;
        try {
            cursoActualizado = cursoCU.modificarCurso(id.intValue(), dto.getCupo_estimado(), nuevoSalon, dto.getEstado());
        } catch (TransicionEstadoCurso.TransicionInvalidaException e) {
            return ResponseEntity.badRequest().body(new TransicionRechazada(false, e.getError(), e.getMessage()));
        }
        if (cursoActualizado == null) {
            return ResponseEntity.notFound().build();
        }

        // Crear respuesta con los datos actualizados
        Salon salon = salonCU.resolverSalonDeCurso(cursoActualizado.getSalon(), false);
        return ResponseEntity.ok(new CursoActualizado(cursoActualizado.getId_curso(),
                cursoActualizado.getObjMateria() != null ? cursoActualizado.getObjMateria().getNombre() : "Curso",
                cursoActualizado.getObjMateria() != null ? cursoActualizado.getObjMateria().getCodigo() : "N/A",
                cursoActualizado.getCupo_estimado(), cursoActualizado.getSalon(), salonInfo(salon),
                salon == null ? null : salon.getId_salon(),
                dto.getEstado() != null ? dto.getEstado() : "Actualizado", "Curso actualizado exitosamente",
                "Cambios aplicados y guardados en BD"));
    }

    /**
     * Eliminar curso de verano
     * DELETE /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @DeleteMapping("/cursos-verano/{id}")
    public ResponseEntity<?> eliminarCurso(@PathVariable Long id) {

        // Verificar que el curso existe
        CursoOfertadoVerano cursoExistente = cursoCU.obtenerCursoPorId(id.intValue());
        if (cursoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Verificar si hay estudiantes inscritos
        if (cursoExistente.getEstudiantesInscritos() != null && !cursoExistente.getEstudiantesInscritos().isEmpty()) {
            return ResponseEntity.badRequest().body(new BorradoBloqueado(
                    cursoExistente.getEstudiantesInscritos().size(), "No se puede eliminar el curso",
                    "El curso tiene estudiantes inscritos. No se puede eliminar."));
        }

        // Llamar al caso de uso para eliminar
        boolean eliminado = cursoCU.eliminarCurso(id.intValue());

        if (eliminado) {
            return ResponseEntity.ok(new CursoEliminado("Curso eliminado exitosamente", id,
                    cursoExistente.getObjMateria() != null ? cursoExistente.getObjMateria().getNombre() : "Curso"));
        } else {
            return ResponseEntity.status(500).body(new ErrorCurso("Error al eliminar el curso",
                    "No se pudo eliminar el curso por razones desconocidas"));
        }
    }

    /**
     * Obtener curso de verano por ID (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @GetMapping("/cursos-verano/{id}")
    public ResponseEntity<Detalle> getCursoPorId(@PathVariable Long id) {
        
        // Obtener curso real de la base de datos - lanza EntidadNoExisteException si no existe
        CursoOfertadoVerano cursoReal = cursoCU.obtenerCursoPorId(id.intValue());
        
        // Verificar docente antes del mapeo
        if (cursoReal.getObjDocente() == null) {
        }
        
        // Usar el mapper existente para obtener datos estructurados
        CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mapearDeCursoOfertadoARespuesta(cursoReal);
        
        // Verificar docente después del mapeo
        if (cursoDTO.getObjDocente() == null) {
        }
        
        // Mapear a estructura esperada por el frontend
        Salon salon = salonCU.resolverSalonDeCurso(cursoReal.getSalon(), false);

        // Obtener conteo real de preinscripciones para este curso
        int solicitudes;
        try {
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(id.intValue());
            solicitudes = preinscripciones.size();
        } catch (Exception e) {
            solicitudes = 0;
        }

        // Usar la informacion del DTO que ya esta mapeada correctamente
        return ResponseEntity.ok(new Detalle(cursoDTO.getId_curso(), cursoDTO.getId_curso(),
                cursoDTO.getNombre_curso(), cursoDTO.getCodigo_curso(), cursoDTO.getDescripcion(),
                cursoDTO.getFecha_inicio(), cursoDTO.getFecha_fin(), cursoDTO.getCupo_maximo(),
                cursoDTO.getCupo_disponible(), cursoDTO.getCupo_estimado(), cursoDTO.getEspacio_asignado(),
                cursoDTO.getEstado(), salonInfo(salon), salon == null ? null : salon.getId_salon(), solicitudes,
                cursoDTO.getObjMateria(), cursoDTO.getObjDocente()));
    }

    /**
     * Filtrar cursos por periodo academico
     * GET /api/cursos-intersemestrales/cursos-verano/periodo/{periodo}
     */
    @GetMapping("/cursos-verano/periodo/{periodo}")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> filtrarCursosPorPeriodo(@PathVariable String periodo) {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(cursoMapper::mapearDeCursoOfertadoARespuestaDisponible)
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
                .map(cursoMapper::mapearDeCursoOfertadoARespuestaDisponible)
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
    public ResponseEntity<EstudiantesCursoVeranoDTORespuesta> obtenerEstudiantesDelCurso(@PathVariable Integer idCurso) {
        return ResponseEntity.ofNullable(datosEstudiantesDelCurso(idCurso));
    }

    private EstudiantesCursoVeranoDTORespuesta datosEstudiantesDelCurso(Integer idCurso) {
        CursoOfertadoVerano curso = cursoCU.obtenerCursoPorId(idCurso);
        if (curso == null) {
            return null;
        }

        var listado = solicitudCU.listarEstudiantesPorCurso(idCurso);
        return EstudiantesCursoVeranoMapper.mapear(curso, listado);
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
        
        byte[] pdfBytes = generadorPdf.generarPdfCursos(periodoAcademico, idPrograma);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        // Nombre del archivo con fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        String filename = "cursos_verano_" + fecha + ".pdf";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.formulario(filename));
        
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
        
        var datos = generadorPdf.generarPdfEstudiantes(idCurso);
        if (datos == null) {
            log.error("No se pudieron obtener los estudiantes para generar el PDF");
            return ResponseEntity.internalServerError().build();
        }

        // Generar PDF
        byte[] pdfBytes = datos.contenido();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        // Nombre del archivo
        String nombreCurso = datos.codigoCurso();
        if (nombreCurso == null || nombreCurso.isEmpty()) {
            nombreCurso = "curso_" + idCurso;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        String filename = "estudiantes_" + nombreCurso + "_" + fecha + ".pdf";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.formulario(filename));
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
