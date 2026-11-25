package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Date;
import java.util.Map;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DecisionSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.ErrorInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionAceptada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionRechazada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.InscripcionesVeranoDTORespuesta.InscripcionRechazadaPorFuncionario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.ErroresCursosVerano;

/**
 * Inscripciones a cursos de verano: creación, aceptación (con control de cupo) y rechazo.
 * Subrecurso {@code /inscripciones} del prefijo de cursos de verano (EST-7, paso 8b). El comprobante se descarga
 * por {@code GET /api/archivos/inscripciones/{id}}.
 */
@RestController
@RequestMapping("/api/cursos-verano/inscripciones")
@RequiredArgsConstructor
@Slf4j
public class InscripcionesVeranoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;

    /**
     * Crear inscripcion a curso de verano
     * POST /api/cursos-verano/inscripciones
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping
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
     * Aceptar (valida el pago) o rechazar la inscripcion de un estudiante (funcionario/coordinador)
     * PUT /api/cursos-verano/inscripciones/{idInscripcion}/estado, con {@code nuevoEstado} aceptada (y
     * {@code observaciones}) o rechazada (y {@code motivo}).
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/{idInscripcion:\\d+}/estado")
    public ResponseEntity<?> cambiarEstadoInscripcion(
            @PathVariable Long idInscripcion,
            @RequestBody @Valid DecisionSolicitudDTOPeticion request) {
        String nuevoEstado = request.getNuevoEstado();
        if ("aceptada".equals(nuevoEstado)) {
            // Sin cupo, o bien otro funcionario ocupó el último cupo mientras tanto
            return ErroresCursosVerano.conCupo(() -> aceptarInscripcion(idInscripcion, request));
        }
        if ("rechazada".equals(nuevoEstado)) {
            return rechazarInscripcion(idInscripcion, request);
        }
        throw new IllegalArgumentException("Estado no válido: use aceptada o rechazada");
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

    private ResponseEntity<?> rechazarInscripcion(Long idInscripcion, DecisionSolicitudDTOPeticion request) {
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
}
