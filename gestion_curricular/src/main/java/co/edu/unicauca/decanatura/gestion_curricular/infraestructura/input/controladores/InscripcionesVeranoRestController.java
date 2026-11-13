package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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
    private final GestionarArchivosCUIntPort objGestionarArchivos;
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
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se encontro una preinscripcion aprobada para este usuario y curso");
            error.put("codigo", "PREINSCRIPCION_NO_APROBADA");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (solicitudCU.tieneInscripcionActiva(peticion.getIdUsuario(), peticion.getIdCurso())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya tienes una inscripcion activa para este curso",
                    "codigo", "INSCRIPCION_DUPLICADA"));
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
        SolicitudCursoVeranoIncripcion inscripcionGuardada = solicitudCU.crearSolicitudCursoVeranoInscripcion(nuevaInscripcion);
        
        if (inscripcionGuardada == null || inscripcionGuardada.getId_solicitud() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al guardar la inscripcion en la base de datos");
            return ResponseEntity.internalServerError().body(error);
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
                inscripcionGuardada.getId_solicitud(), peticion.getIdUsuario(), peticion.getIdCurso(),
                peticion.getNombreSolicitud(), inscripcionGuardada.getFecha_registro_solicitud(), "Inscrito"));
    }

    record InscripcionCreada(boolean success, String message, Integer id_solicitud, Integer id_usuario,
                             Integer id_curso, String nombre_solicitud, java.util.Date fecha_inscripcion, String estado) {
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
            if (!(solicitud instanceof SolicitudCursoVeranoIncripcion inscripcion)) {
                continue;
            }

            List<EstadoSolicitud> estados = inscripcion.getEstadosSolicitud();
            String estado = estados != null && !estados.isEmpty()
                    ? estados.get(estados.size() - 1).getEstado_actual() : null;
            var curso = inscripcion.getObjCursoOfertadoVerano();
            Usuario usuario = inscripcion.getObjUsuario();
            Integer idUsuario = usuario == null ? null : usuario.getId_usuario();
            var estudiante = new EstudianteInscripcion(idUsuario,
                    usuario == null ? inscripcion.getNombre_estudiante() : usuario.getNombre_completo(),
                    usuario == null ? null : usuario.getCorreo(), usuario == null ? null : usuario.getCodigo());

            var documentos = inscripcion.getDocumentos();
            Documento comprobante = documentos == null || documentos.isEmpty() ? null : documentos.get(0);
            var archivoPago = new ArchivoPago(comprobante == null ? null : comprobante.getId_documento(),
                    comprobante == null ? "Sin comprobante" : comprobante.getNombre(), null,
                    comprobante == null ? null : comprobante.getFecha_documento());
            inscripciones.add(new InscripcionListado(inscripcion.getId_solicitud(), inscripcion.getFecha_registro_solicitud(),
                    estado, curso == null ? null : curso.getId_curso(), idUsuario, estudiante, archivoPago));
        }
        return ResponseEntity.ok(inscripciones);
    }

    record InscripcionListado(Integer id, java.util.Date fecha, String estado, Integer cursoId,
                               Integer estudianteId, EstudianteInscripcion estudiante, ArchivoPago archivoPago) {
    }

    record EstudianteInscripcion(Integer id_usuario, String nombre, String email, String codigo_estudiante) {
    }

    record ArchivoPago(Integer id_documento, String nombre, String url, java.util.Date fecha) {
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
        if (EstadoSolicitudCursoVerano.APROBADO.texto().equalsIgnoreCase(estadoActual)) {
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
                if (EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoActual)) {
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
            SolicitudCursoVeranoIncripcion inscripcionAceptada = solicitudCU.validarPago(
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

        } catch (co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException e) {
            // Otro funcionario ocupó el último cupo mientras tanto
            return SoporteCursosVerano.respuestaCupoAgotado(e);
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
        if (!EstadoSolicitudCursoVerano.ENVIADA.texto().equals(estadoActual) && !EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoActual)) {
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
        Integer pendientes = solicitudCU.contarInscripcionesPorEstado(idCurso.intValue(), EstadoSolicitudCursoVerano.ENVIADA.texto());
        Integer aceptadas = solicitudCU.contarInscripcionesPorEstado(idCurso.intValue(), EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto());
        Integer rechazadas = solicitudCU.contarInscripcionesPorEstado(idCurso.intValue(), EstadoSolicitudCursoVerano.PAGO_RECHAZADO.texto());
        
        
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
        Integer ocupados = solicitudCU.contarInscripcionesAceptadasPorCurso(curso.getId_curso());
        int ocupadosSeguro = ocupados != null ? ocupados : 0;
        if (ocupadosSeguro >= curso.getCupo_estimado()) {
            return SoporteCursosVerano.respuestaCupoAgotado(new co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones
                    .CupoAgotadoException(curso.getCupo_estimado(), ocupadosSeguro));
        }
        return null;
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
