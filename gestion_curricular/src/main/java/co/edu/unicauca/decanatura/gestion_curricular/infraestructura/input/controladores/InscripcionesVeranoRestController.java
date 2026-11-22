package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DecisionSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.ArchivoPagoInfo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.CancelacionRechazada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.ComprobantePago;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.DocumentoInfo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.ErrorInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.EstadisticasInscripciones;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.EstudianteElegible;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.IdCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InfoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InfoNoEncontrada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionAceptada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionCancelada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionRechazada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionRechazadaPorFuncionario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.NombreCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.ErroresCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

/**
 * Inscripciones a cursos de verano: creación, aceptación (con control de cupo), rechazo y comprobantes.
 * Ruta base compartida con los demás controladores de cursos de verano.
 */
@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class InscripcionesVeranoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final GestionarSolicitudCUIntPort solicitudGeneralCU;

    /**
     * Crear inscripcion a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/inscripciones
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/cursos-verano/inscripciones")
    public ResponseEntity<?> crearInscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        
        SolicitudCursoVeranoPreinscripcion preinscripcionAprobada =
                solicitudCU.buscarPreinscripcionAprobada(peticion.getIdUsuario(), peticion.getIdCurso());
        if (preinscripcionAprobada == null) {
            return ResponseEntity.badRequest().body(new InscripcionRechazada(
                    "No se encontro una preinscripcion aprobada para este usuario y curso", "PREINSCRIPCION_NO_APROBADA"));
        }
        
        if (solicitudCU.tieneInscripcionActiva(peticion.getIdUsuario(), peticion.getIdCurso())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya tienes una inscripcion activa para este curso",
                    "codigo", "INSCRIPCION_DUPLICADA"));
        }

        // Validacion 2: el curso debe tener cupo disponible
        ErroresCursosVerano.conCupo(
                () -> solicitudCU.verificarCupoDisponible(preinscripcionAprobada.getObjCursoOfertadoVerano()));
        
        // 2. Crear la inscripcion usando el modelo de dominio
        
        SolicitudCursoVeranoInscripcion nuevaInscripcion = new SolicitudCursoVeranoInscripcion();
        nuevaInscripcion.setNombreSolicitud(peticion.getNombreSolicitud());
        nuevaInscripcion.setFechaRegistroSolicitud(new Date());
        nuevaInscripcion.setObservacion("Inscripcion creada desde preinscripcion aprobada");
        nuevaInscripcion.setObjUsuario(preinscripcionAprobada.getObjUsuario());
        nuevaInscripcion.setObjCursoOfertadoVerano(preinscripcionAprobada.getObjCursoOfertadoVerano());
        
        // Campos obligatorios que faltaban
        nuevaInscripcion.setNombreEstudiante("Estudiante"); // Valor por defecto
        nuevaInscripcion.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez); // Valor por defecto
        
        // 3. Guardar la inscripcion en la base de datos
        SolicitudCursoVeranoInscripcion inscripcionGuardada = solicitudCU.crearSolicitudCursoVeranoInscripcion(nuevaInscripcion);
        
        if (inscripcionGuardada == null || inscripcionGuardada.getIdSolicitud() == null) {
            return ResponseEntity.internalServerError().body(
                    new ErrorInscripcion("Error al guardar la inscripcion en la base de datos"));
        }
        
        // Los documentos pendientes (comprobante) los asocia el caso de uso al crear la inscripción
        
        // 5. Asociar estudiante al curso en la tabla de relacion
        try {
            if (!cursoCU.asociarEstudiante(peticion.getIdCurso(), peticion.getIdUsuario())) {
                log.warn("No se pudo asociar el estudiante {} al curso {}", peticion.getIdUsuario(), peticion.getIdCurso());
            }
        } catch (Exception e) {
            log.error("Error asociando estudiante al curso: {}", e.getMessage(), e);
            // No fallar la operacion por esto
        }
        
        return ResponseEntity.ok(new InscripcionCreada(true, "Inscripcion creada exitosamente",
                inscripcionGuardada.getIdSolicitud(), peticion.getIdUsuario(), peticion.getIdCurso(),
                peticion.getNombreSolicitud(), inscripcionGuardada.getFechaRegistroSolicitud(), "Inscrito"));
    }

    record InscripcionCreada(boolean success, String message, Integer id_solicitud, Integer id_usuario,
                             Integer id_curso, String nombre_solicitud, Date fecha_inscripcion, String estado) {
    }

    /**
     * Obtener inscripciones con informacion completa
     * GET /api/cursos-intersemestrales/inscripciones
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/inscripciones")
    public ResponseEntity<List<InscripcionListado>> obtenerInscripciones() {
        List<InscripcionListado> inscripciones = new ArrayList<>();
        for (Solicitud solicitud : solicitudGeneralCU.listarSolicitudes()) {
            if (!(solicitud instanceof SolicitudCursoVeranoInscripcion inscripcion)) {
                continue;
            }

            String estado = inscripcion.estadoActual();
            var curso = inscripcion.getObjCursoOfertadoVerano();
            Usuario usuario = inscripcion.getObjUsuario();
            Integer idUsuario = usuario == null ? null : usuario.getIdUsuario();
            var estudiante = new EstudianteInscripcion(idUsuario,
                    usuario == null ? inscripcion.getNombreEstudiante() : usuario.getNombreCompleto(),
                    usuario == null ? null : usuario.getCorreo(), usuario == null ? null : usuario.getCodigo());

            var documentos = inscripcion.getDocumentos();
            Documento comprobante = documentos == null || documentos.isEmpty() ? null : documentos.get(0);
            var archivoPago = new ArchivoPago(comprobante == null ? null : comprobante.getIdDocumento(),
                    comprobante == null ? "Sin comprobante" : comprobante.getNombre(), null,
                    comprobante == null ? null : comprobante.getFechaDocumento());
            inscripciones.add(new InscripcionListado(inscripcion.getIdSolicitud(), inscripcion.getFechaRegistroSolicitud(),
                    estado, curso == null ? null : curso.getIdCurso(), idUsuario, estudiante, archivoPago));
        }
        return ResponseEntity.ok(inscripciones);
    }

    record InscripcionListado(Integer id, Date fecha, String estado, Integer cursoId,
                               Integer estudianteId, EstudianteInscripcion estudiante, ArchivoPago archivoPago) {
    }

    record EstudianteInscripcion(Integer id_usuario, String nombre, String email, String codigo_estudiante) {
    }

    record ArchivoPago(Integer id_documento, String nombre, String url, Date fecha) {
    }

    /**
     * Cancelar inscripcion
     * DELETE /api/cursos-intersemestrales/inscripciones/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @DeleteMapping("/inscripciones/{id}")
    public ResponseEntity<?> cancelarInscripcion(@PathVariable Integer id) {

        Solicitud solicitud = solicitudGeneralCU.obtenerSolicitudPorId(id);
        if (solicitud == null) {
            return ResponseEntity.status(404).body(new CancelacionRechazada("Inscripcion no encontrada",
                    "No existe una inscripcion con el ID: " + id, 404, LocalDateTime.now().toString()));
        }

        // No se puede cancelar una inscripcion ya aprobada
        String estadoActual = solicitud.estadoActual();
        if (EstadoSolicitudCursoVerano.APROBADO.texto().equalsIgnoreCase(estadoActual)) {
            return ResponseEntity.status(400).body(new CancelacionRechazada("No se puede cancelar",
                    "No se puede cancelar una inscripcion que ya ha sido aprobada", 400, LocalDateTime.now().toString()));
        }

        solicitudGeneralCU.eliminarSolicitud(id);

        return ResponseEntity.ok(new InscripcionCancelada("Inscripcion cancelada exitosamente", id, 200,
                LocalDateTime.now().toString()));
    }

    /**
     * Obtener estudiantes inscritos en un curso (estudiantes que ya completaron la inscripcion)
     * GET /api/cursos-intersemestrales/inscripciones/estudiantes-elegibles/{idCurso}
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/inscripciones/estudiantes-elegibles/{idCurso}")
    public ResponseEntity<List<EstudianteElegible>> getEstudiantesElegiblesParaInscripcion(
            @PathVariable Long idCurso) {

        List<EstudianteElegible> estudiantesInscritos = new ArrayList<>();
        
        // 1. Obtener todas las inscripciones del curso (no preinscripciones)
        List<SolicitudCursoVeranoInscripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso.intValue());
        
        // 2. Procesar cada inscripcion encontrada
        
        for (SolicitudCursoVeranoInscripcion inscripcion : inscripciones) {
            // 2.1. Verificar el estado de la inscripcion - SOLO mostrar las que NO estan aceptadas
            String estadoActual = inscripcion.estadoActual("Inscrito"); // Estado por defecto

            // Si la inscripcion ya fue aceptada (Pago_Validado), no la mostrar en la lista
            if (EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoActual)) {
                continue; // Saltar esta inscripcion
            }

            // Sin usuario, los datos del estudiante salen en null (como en GET /inscripciones)
            Usuario usuario = inscripcion.getObjUsuario();

            // Informacion del curso (sin materia, nombre_curso sale en null)
            IdCurso idCursoInscrito = null;
            NombreCurso nombreCurso = null;
            CursoOfertadoVerano curso = inscripcion.getObjCursoOfertadoVerano();
            if (curso != null) {
                idCursoInscrito = new IdCurso(curso.getIdCurso());
                nombreCurso = new NombreCurso(curso.getObjMateria() == null ? null : curso.getObjMateria().getNombre());
                // El codigo del curso no esta disponible en esta entidad
            }

            // El comprobante es el primer documento de la inscripcion, como en GET /inscripciones; sin documentos, null
            List<Documento> documentos = inscripcion.getDocumentos();
            Documento comprobante = documentos == null || documentos.isEmpty() ? null : documentos.get(0);
            estudiantesInscritos.add(new EstudianteElegible(usuario == null ? null : usuario.getIdUsuario(),
                    usuario == null ? inscripcion.getNombreEstudiante() : usuario.getNombreCompleto(),
                    usuario == null ? null : usuario.getCodigo(), usuario == null ? null : usuario.getCorreo(),
                    inscripcion.getIdSolicitud(), inscripcion.getNombreSolicitud(),
                    inscripcion.getFechaRegistroSolicitud(), inscripcion.getCondicionSolicitud(), idCursoInscrito,
                    nombreCurso, estadoActual, "Inscripcion", "Estudiante inscrito en el curso", true,
                    comprobante == null ? null : new ComprobantePago(comprobante.getIdDocumento(),
                            comprobante.getNombre(), comprobante.getRutaDocumento(), comprobante.getFechaDocumento())));
        }
        
        
        return ResponseEntity.ok(estudiantesInscritos);
    }

    /**
     * Aceptar inscripcion de estudiante (funcionario/coordinador acepta inscripcion final)
     * PUT /api/cursos-intersemestrales/inscripciones/{idInscripcion}/aceptar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/inscripciones/{idInscripcion}/aceptar")
    public ResponseEntity<?> aceptarInscripcionEstudiante(
            @PathVariable Long idInscripcion,
            @RequestBody @Valid DecisionSolicitudDTOPeticion request) {
        // Sin cupo, o bien otro funcionario ocupó el último cupo mientras tanto
        return ErroresCursosVerano.conCupo(() -> aceptarInscripcion(idInscripcion, request));
    }

    private ResponseEntity<?> aceptarInscripcion(Long idInscripcion, DecisionSolicitudDTOPeticion request) {
        String observaciones = request.getObservaciones();
        if (observaciones == null || observaciones.trim().isEmpty()) {
            observaciones = "Inscripcion aceptada por funcionario/coordinador";
        }

        // 1. Buscar la inscripcion directamente por ID
        SolicitudCursoVeranoInscripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());

        if (inscripcion == null) {
            return ResponseEntity.badRequest().body(
                    new ErrorInscripcion("No se encontro la inscripcion con ID: " + idInscripcion));
        }

        // 2. El curso debe tener cupo (si la inscripcion ya ocupaba cupo, aceptarla de nuevo no suma)
        solicitudCU.verificarCupoParaAceptar(inscripcion);

        // 3. Validar pago de la inscripcion (marcar como aceptada)
        SolicitudCursoVeranoInscripcion inscripcionAceptada = solicitudCU.validarPago(
            inscripcion.getIdSolicitud(),
            true,
            observaciones
        );

        if (inscripcionAceptada == null) {
            return ResponseEntity.internalServerError().body(new ErrorInscripcion("No se pudo aceptar la inscripcion"));
        }

        // 3. Insertar en tabla cursosestudiantes (relacion Many-to-Many)
        try {
            Integer idCurso = inscripcion.getObjCursoOfertadoVerano().getIdCurso();
            Integer idEstudiante = inscripcion.getObjUsuario().getIdUsuario();
            if (!cursoCU.asociarEstudiante(idCurso, idEstudiante)) {
                log.warn("No se pudo asociar el estudiante {} al curso {}", idEstudiante, idCurso);
            }
        } catch (Exception e) {
            log.error("Error asociando estudiante al curso: {}", e.getMessage(), e);
            // No fallar la operacion por esto, pero logear el error
        }

        // 4. Preparar respuesta
        return ResponseEntity.ok(new InscripcionAceptada(true, "Inscripcion aceptada exitosamente",
                inscripcionAceptada.getIdSolicitud(), inscripcion.getObjUsuario().getNombreCompleto(),
                inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre(), new Date(), observaciones));
    }

    /**
     * Rechazar inscripcion de estudiante (funcionario/coordinador rechaza inscripcion)
     * PUT /api/cursos-intersemestrales/inscripciones/{idInscripcion}/rechazar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/inscripciones/{idInscripcion}/rechazar")
    public ResponseEntity<?> rechazarInscripcionEstudiante(
            @PathVariable Long idInscripcion,
            @RequestBody @Valid DecisionSolicitudDTOPeticion request) {
        
        String motivo = request.getMotivo();
        if (motivo == null || motivo.trim().isEmpty()) {
            motivo = "Inscripcion rechazada por funcionario/coordinador";
        } else {
            motivo = motivo.trim();
        }
        
        // 1. Buscar la inscripcion directamente por ID
        SolicitudCursoVeranoInscripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
        if (inscripcion == null) {
            return ResponseEntity.badRequest().body(
                    new ErrorInscripcion("No se encontro la inscripcion con ID: " + idInscripcion));
        }

        String estadoActual = inscripcion.estadoActual();

        // 2. Verificar que la inscripcion este en estado valido para rechazar
        if (!EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoActual) && !EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoActual)) {
            return ResponseEntity.badRequest().body(
                    new ErrorInscripcion("La inscripcion no puede ser rechazada en su estado actual: " + estadoActual));
        }
        
        // 3. Marcar como rechazada usando el caso de uso
        String observacionRechazo = "Inscripcion rechazada: " + motivo;
        SolicitudCursoVeranoInscripcion inscripcionRechazada = ErroresCursosVerano.alRechazarInscripcion(
                () -> solicitudCU.validarPago(inscripcion.getIdSolicitud(), false, observacionRechazo));
        
        if (inscripcionRechazada == null) {
            return ResponseEntity.internalServerError().body(
                    new ErrorInscripcion("No se pudo actualizar el estado de la inscripcion"));
        }

        String nuevoEstado = inscripcionRechazada.estadoActual();

        // 4. Preparar respuesta
        return ResponseEntity.ok(new InscripcionRechazadaPorFuncionario(true, "Inscripcion rechazada exitosamente",
                inscripcionRechazada.getIdSolicitud(), inscripcion.getObjUsuario().getNombreCompleto(),
                inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre(), new Date(), motivo, nuevoEstado));
    }

    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR SOLICITUDES ====================

    // ==================== ENDPOINT PARA CONFIRMAR INSCRIPCION ====================

    /**
     * Confirmar inscripcion de estudiante
     * PUT /api/cursos-intersemestrales/inscripciones/{id}/confirmar
     *
     * <p>No implementado: responde 501 sin cuerpo. Antes respondía 200 con una inscripción inventada
     * (BUG-33) sin consultar ni modificar nada.
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id.intValue())")
    @PutMapping("/inscripciones/{id}/confirmar")
    public ResponseEntity<Void> confirmarInscripcion(@PathVariable Long id) {
        return ResponseEntity.status(501).build();
    }

    // ==================== ENDPOINT PARA DESCARGAR COMPROBANTE DE PAGO ====================

    

    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idInscripcion.intValue())")
    @GetMapping("/inscripciones/{idInscripcion}/info")
    public ResponseEntity<?> infoInscripcion(@PathVariable Long idInscripcion) {

        // 1. Buscar la inscripcion directamente por ID
        SolicitudCursoVeranoInscripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());

        if (inscripcion == null) {
            return ResponseEntity.ok(new InfoNoEncontrada(false, "Inscripcion no encontrada"));
        }

        // Verificar documentos (0 si la lista es null; documentos_null lo distingue)
        List<Documento> documentos = inscripcion.getDocumentos();
        int documentosCount = documentos != null ? documentos.size() : 0;

        List<DocumentoInfo> documentosInfo = null;
        ArchivoPagoInfo archivoPago = null;
        if (documentos != null && !documentos.isEmpty()) {
            documentosInfo = new ArrayList<>();
            for (Documento doc : documentos) {
                documentosInfo.add(new DocumentoInfo(doc.getIdDocumento(), doc.getNombre(), doc.getRutaDocumento(),
                        doc.isEsValido()));
            }

            // Agregar informacion especifica del archivo para descarga
            Documento primerDocumento = documentos.get(0);
            archivoPago = new ArchivoPagoInfo(primerDocumento.getNombre(), primerDocumento.getRutaDocumento(),
                    primerDocumento.getIdDocumento());
        }

        return ResponseEntity.ok(new InfoInscripcion(true, inscripcion.getIdSolicitud(),
                inscripcion.getNombreSolicitud(), inscripcion.getFechaRegistroSolicitud(), documentosCount,
                documentos == null, documentos != null && documentos.isEmpty(), documentosInfo, archivoPago));
    }

    // ==================== ENDPOINT PARA SOLICITUDES DE CURSO NUEVO ====================

    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/inscripciones/curso/{idCurso}/estadisticas")
    public ResponseEntity<EstadisticasInscripciones> obtenerEstadisticasInscripciones(@PathVariable Long idCurso) {

        // Contar inscripciones por estado con logging detallado
        Integer pendientes = solicitudCU.contarInscripcionesPorEstado(idCurso.intValue(), EstadoSolicitudCursoVerano.ENVIADA.texto());
        Integer aceptadas = solicitudCU.contarInscripcionesPorEstado(idCurso.intValue(), EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto());
        Integer rechazadas = solicitudCU.contarInscripcionesPorEstado(idCurso.intValue(), EstadoSolicitudCursoVerano.PAGO_RECHAZADO.texto());
        
        
        Integer totalInscripciones = pendientes + aceptadas + rechazadas;
        
        return ResponseEntity.ok(new EstadisticasInscripciones(totalInscripciones, pendientes, aceptadas, rechazadas,
                idCurso, new Date()));
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
        SolicitudCursoVeranoInscripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
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
                    String rutaDocumento = documento.getRutaDocumento() != null ? documento.getRutaDocumento() : documento.getNombre();
                    byte[] archivo;
                    
                    try {
                        if (rutaDocumento != null && rutaDocumento.contains("/")) {
                            // Ruta organizada (nueva estructura)
                            archivo = gestionarArchivos.getFileByPath(rutaDocumento);
                        } else {
                            // Ruta simple (compatibilidad hacia atrás)
                            archivo = gestionarArchivos.getFile(documento.getNombre());
                        }
                    } catch (Exception e) {
                        // Si falla con la ruta, intentar con el nombre como fallback
                        if (documento.getRutaDocumento() != null && !documento.getRutaDocumento().equals(documento.getNombre())) {
                            archivo = gestionarArchivos.getFile(documento.getNombre());
                        } else {
                            throw e; // Re-lanzar si no hay alternativa
                        }
                    }
                    
                    if (archivo == null || archivo.length == 0) {
                        continue; // Probar el siguiente documento
                    }
                    
                    // Configurar headers para descarga
                    String contentDisposition = RespuestasArchivo.adjunto(documento.getNombre());
                    
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
}
