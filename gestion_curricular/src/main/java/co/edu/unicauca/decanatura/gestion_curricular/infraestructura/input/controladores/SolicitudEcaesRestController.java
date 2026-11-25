package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.FechasEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.FechaEcaesDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RechazoCambioEstadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudEcaesDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudEcaesMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;



@RestController
@RequestMapping("/api/solicitudes-ecaes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudEcaesRestController {

    private final GestionarSolicitudEcaesCUIntPort solicitudEcaesCU;
    private final SolicitudEcaesMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final Autorizacion autorizacion;
    private final ValidadorCambioEstado validadorCambioEstado;

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping
    public ResponseEntity<SolicitudEcaesDTORespuesta> crearSolicitud(@Valid @RequestBody SolicitudEcaesDTOPeticion peticion) {
        // El solicitante sale del token, no de lo que envía el cliente
        peticion.getObjUsuario().setId_usuario(autorizacion.idSolicitante(peticion.getObjUsuario().getId_usuario()));
        SolicitudEcaes solicitud = solicitudMapperDominio.mapearDeSolicitudEcaesDTOPeticionASolicitudEcaes(peticion);
        SolicitudEcaes solicitudCreada = solicitudEcaesCU.guardar(solicitud);
        ResponseEntity<SolicitudEcaesDTORespuesta> respuesta = new ResponseEntity<SolicitudEcaesDTORespuesta>
        (solicitudMapperDominio.mapearDeSolicitudEcaesARespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    /** Sin filtros: todas las solicitudes (solo staff). */
    @GetMapping(params = {"!rol", "!bandeja"})
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudes() {
        List<SolicitudEcaes> solicitudes = solicitudEcaesCU.listarSolicitudes();
        List<SolicitudEcaesDTORespuesta> respuesta = solicitudMapperDominio.mapearListaDeSolicitudEcaesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Bandeja de pendientes del funcionario, en el período pedido o en el actual. Sigue la forma de Paz y Salvo
     * (BUG-45): por ahora solo admite {@code rol=funcionario&bandeja=pendientes}.
     */
    @GetMapping(params = {"rol", "bandeja"})
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudesToFuncionario(
            @RequestParam String rol,
            @RequestParam String bandeja,
            @RequestParam(required = false) String periodoAcademico) {
        if (!"pendientes".equals(bandeja)) {
            throw new IllegalArgumentException("Bandeja no válida: use pendientes");
        }
        if (!"funcionario".equals(rol)) {
            throw new IllegalArgumentException("Rol no válido para la bandeja de pendientes: use funcionario");
        }
        return responder(solicitudEcaesCU.listar(FiltroSolicitudes.pendientesDe(
                FlujoEstadosSolicitud.FUNCIONARIO, null, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    private ResponseEntity<List<SolicitudEcaesDTORespuesta>> responder(List<SolicitudEcaes> solicitudes) {
        return ResponseEntity.ok(solicitudMapperDominio.mapearListaDeSolicitudEcaesARespuesta(solicitudes));
    }
    /** Lo que no atiende ningún listado (por ejemplo, {@code bandeja} sin {@code rol}). Sin esto Spring daría 500. */
    @GetMapping
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSinFiltroValido() {
        throw new IllegalArgumentException("Filtro no válido: use rol, o rol=funcionario&bandeja=pendientes");
    }

    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SolicitudEcaesDTORespuesta> obtenerPorId(@PathVariable Integer id) {
        SolicitudEcaes solicitud = solicitudEcaesCU.buscarPorId(id);
        SolicitudEcaesDTORespuesta respuesta = solicitudMapperDominio.mapearDeSolicitudEcaesARespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = {"rol", "!bandeja"})
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudPorRol(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String periodoAcademico) {

        // Quien no es staff solo puede ver sus propias solicitudes
        if (!autorizacion.esStaff()) {
            rol = "ESTUDIANTE";
            idUsuario = autorizacion.idUsuarioActual();
            if (idUsuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        String periodo = PeriodoDeConsulta.oActual(periodoAcademico);
        if ("COORDINADOR".equalsIgnoreCase(rol)) {
            // El coordinador no participa en el flujo ECAES: la lista sale vacía (BUG-10, por decisión del usuario)
            return responder(solicitudEcaesCU.listar(FiltroSolicitudes.pendientesDe(
                    FlujoEstadosSolicitud.COORDINADOR, autorizacion.idProgramaCoordinadorActual(), periodo)));
        }
        if ("ESTUDIANTE".equalsIgnoreCase(rol) && idUsuario != null && periodo != null) {
            return responder(solicitudEcaesCU.listar(FiltroSolicitudes.deEstudiante(idUsuario, periodo)));
        }
        return responder(solicitudEcaesCU.listarSolicitudesPorRol(rol, idUsuario));
    }

    @PostMapping("/fechas")
    public ResponseEntity<FechaEcaesDTORespuesta> publicarFechasEcaes(@RequestBody FechasEcaesDTOPeticion fechasEcaes) {
        FechaEcaes fechaEcaes = solicitudMapperDominio.mapearDeFechasEcaesDTOPeticionAFechaEcaes(fechasEcaes);
        FechaEcaes fechaPublicada = solicitudEcaesCU.publicarFechasEcaes(fechaEcaes);
        FechaEcaesDTORespuesta respuesta = solicitudMapperDominio.mapearDeFechaEcaesAFechaEcaesDTORespuesta(fechaPublicada);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fechas")
    public ResponseEntity<List<FechaEcaesDTORespuesta>> listarFechasEcaes() {
        List<FechaEcaes> fechas = solicitudEcaesCU.listarFechasEcaes();
        List<FechaEcaesDTORespuesta> respuesta = solicitudMapperDominio.mapearListaDeFechaEcaesAFechaEcaesDTORespuesta(fechas);
        return ResponseEntity.ok(respuesta);
    }

    /** La solicitud es la de la ruta: un {@code idSolicitud} en el cuerpo se ignora. */
    @PutMapping("/{id:\\d+}/estado")
    public ResponseEntity<RechazoCambioEstadoDTORespuesta> actualizarEstadoSolicitud(
            @PathVariable Integer id, @RequestBody CambioEstadoSolicitudDTOPeticion solicitudPeticion) {
        solicitudPeticion.setIdSolicitud(id);
        CambioEstadoSolicitud solicitud = solicitudMapper.mapearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(solicitudPeticion);
        // Solo el Funcionario (o el Administrador) pre-registra o rechaza
        String estadoActual = FlujoEstadosSolicitud.ultimoEstado(
                solicitudEcaesCU.buscarPorId(solicitud.getIdSolicitud()).getEstadosSolicitud());
        ResponseEntity<RechazoCambioEstadoDTORespuesta> rechazo = validadorCambioEstado.validar(
                FlujoEstadosSolicitud.ECAES, estadoActual, solicitud.getNuevoEstado());
        if (rechazo != null) {
            return rechazo;
        }
        solicitudEcaesCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fechas/{periodoAcademico:\\d{4}-\\d+}")
    public ResponseEntity<FechaEcaesDTORespuesta> buscarFechasPorPeriodo(@PathVariable String periodoAcademico) {
        Optional<FechaEcaes> fechas = solicitudEcaesCU.buscarFechasPorPeriodo(periodoAcademico);
        if (fechas.isPresent()) {
            FechaEcaesDTORespuesta respuesta = solicitudMapperDominio.mapearDeFechaEcaesAFechaEcaesDTORespuesta(fechas.get());
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /** Actualiza (o crea) las fechas del período de la ruta: un {@code periodoAcademico} en el cuerpo se ignora. */
    @PutMapping("/fechas/{periodoAcademico:\\d{4}-\\d+}")
    public ResponseEntity<FechaEcaesDTORespuesta> actualizarFechasEcaes(
            @PathVariable String periodoAcademico, @RequestBody FechasEcaesDTOPeticion fechasEcaes) {
        fechasEcaes.setPeriodoAcademico(periodoAcademico);
        FechaEcaes fechaEcaes = solicitudMapperDominio.mapearDeFechasEcaesDTOPeticionAFechaEcaes(fechasEcaes);
        FechaEcaes fechaActualizada = solicitudEcaesCU.actualizarFechasEcaes(fechaEcaes);
        FechaEcaesDTORespuesta respuesta = solicitudMapperDominio.mapearDeFechaEcaesAFechaEcaesDTORespuesta(fechaActualizada);
        return ResponseEntity.ok(respuesta);
    }
    
}
