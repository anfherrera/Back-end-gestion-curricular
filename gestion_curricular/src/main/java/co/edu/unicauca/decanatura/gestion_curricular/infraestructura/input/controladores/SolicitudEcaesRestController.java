package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.FechasEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.FechaEcaesDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudEcaesDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudEcaesMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/solicitudes-ecaes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudEcaesRestController {

    private final GestionarSolicitudEcaesCUIntPort solicitudEcaesCU;
    private final SolicitudEcaesMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final Autorizacion autorizacion;
    private final ValidadorCambioEstado validadorCambioEstado;

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping("/crearSolicitud-Ecaes")
    public ResponseEntity<SolicitudEcaesDTORespuesta> crearSolicitud(@Valid @RequestBody SolicitudEcaesDTOPeticion peticion) {
        // El solicitante sale del token, no de lo que envía el cliente
        peticion.getObjUsuario().setId_usuario(autorizacion.idSolicitante(peticion.getObjUsuario().getId_usuario()));
        SolicitudEcaes solicitud = solicitudMapperDominio.mappearDeSolicitudEcaesDTOPeticionASolicitudEcaes(peticion);
        SolicitudEcaes solicitudCreada = solicitudEcaesCU.guardar(solicitud);
        ResponseEntity<SolicitudEcaesDTORespuesta> respuesta = new ResponseEntity<SolicitudEcaesDTORespuesta>
        (solicitudMapperDominio.mappearDeSolicitudEcaesARespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    @GetMapping("/listarSolicitudes-Ecaes")
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudes() {
        List<SolicitudEcaes> solicitudes = solicitudEcaesCU.listarSolicitudes();
        List<SolicitudEcaesDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudEcaesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitudes-Ecaes/Funcionario")
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudesToFuncionario(
            @RequestParam(required = false) String periodoAcademico) {
        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
                log.debug("Usando período académico actual automático: {}", periodoAcademico);
            }
        }
        
        List<SolicitudEcaes> solicitudes;
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            solicitudes = solicitudEcaesCU.listarSolicitudesToFuncionarioPorPeriodo(periodoAcademico.trim());
        } else {
            solicitudes = solicitudEcaesCU.listarSolicitudesToFuncionario();
        }
        
        List<SolicitudEcaesDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudEcaesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @GetMapping("/buscarSolicitud-Ecaes/{id}")
    public ResponseEntity<SolicitudEcaesDTORespuesta> obtenerPorId(@PathVariable Integer id) {
        SolicitudEcaes solicitud = solicitudEcaesCU.buscarPorId(id);
        SolicitudEcaesDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudEcaesARespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listarSolicitud-ecaes/porRol")
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

        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
                log.debug("Usando período académico actual automático: {}", periodoAcademico);
            }
        }

        List<SolicitudEcaes> solicitudes;
        
        // Si es coordinador, filtrar por programa y período
        if ("COORDINADOR".equalsIgnoreCase(rol)) {
            Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
            if (idPrograma != null && periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                solicitudes = solicitudEcaesCU.listarSolicitudesToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico.trim());
            } else if (idPrograma != null) {
                // Filtrar solo por programa si no hay período
                solicitudes = solicitudEcaesCU.listarSolicitudesPorRol(rol, idUsuario);
                solicitudes = solicitudes.stream()
                        .filter(s -> s.getObjUsuario() != null 
                                && s.getObjUsuario().getObjPrograma() != null
                                && s.getObjUsuario().getObjPrograma().getId_programa() != null
                                && s.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma))
                        .toList();
            } else {
                log.warn("No se pudo obtener el programa del coordinador para ECAES, retornando todas las solicitudes");
                solicitudes = solicitudEcaesCU.listarSolicitudesPorRol(rol, idUsuario);
            }
        } else if ("ESTUDIANTE".equalsIgnoreCase(rol) && idUsuario != null && periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            solicitudes = solicitudEcaesCU.listarSolicitudesPorUsuarioYPeriodo(idUsuario, periodoAcademico.trim());
        } else {
            solicitudes = solicitudEcaesCU.listarSolicitudesPorRol(rol, idUsuario);
        }

        List<SolicitudEcaesDTORespuesta> respuesta =
                solicitudMapperDominio.mappearListaDeSolicitudEcaesARespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/publicarFechasEcaes")
    public ResponseEntity<FechaEcaesDTORespuesta> publicarFechasEcaes(@RequestBody FechasEcaesDTOPeticion fechasEcaes) {
        FechaEcaes fechaEcaes = solicitudMapperDominio.mappearDeFechasEcaesDTOPeticionAFechaEcaes(fechasEcaes);
        FechaEcaes fechaPublicada = solicitudEcaesCU.publicarFechasEcaes(fechaEcaes);
        FechaEcaesDTORespuesta respuesta = solicitudMapperDominio.mappearDeFechaEcaesAFechaEcaesDTORespuesta(fechaPublicada);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listarFechasEcaes")
    public ResponseEntity<List<FechaEcaesDTORespuesta>> listarFechasEcaes() {
        List<FechaEcaes> fechas = solicitudEcaesCU.listarFechasEcaes();
        List<FechaEcaesDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeFechaEcaesAFechaEcaesDTORespuesta(fechas);
        return ResponseEntity.ok(respuesta);
    }

    
    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Map<String, Object>> actualizarEstadoSolicitud(@RequestBody CambioEstadoSolicitudDTOPeticion solicitudPeticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(solicitudPeticion);
        // Solo el Funcionario (o el Administrador) pre-registra o rechaza
        String estadoActual = FlujoEstadosSolicitud.ultimoEstado(
                solicitudEcaesCU.buscarPorId(solicitud.getIdSolicitud()).getEstadosSolicitud());
        ResponseEntity<Map<String, Object>> rechazo = validadorCambioEstado.validar(
                FlujoEstadosSolicitud.ECAES, estadoActual, solicitud.getNuevoEstado());
        if (rechazo != null) {
            return rechazo;
        }
        solicitudEcaesCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/buscarFechasPorPeriodo/{periodoAcademico}")
    public ResponseEntity<FechaEcaesDTORespuesta> buscarFechasPorPeriodo(@PathVariable String periodoAcademico) {
        Optional<FechaEcaes> fechas = solicitudEcaesCU.buscarFechasPorPeriodo(periodoAcademico);
        if (fechas.isPresent()) {
            FechaEcaesDTORespuesta respuesta = solicitudMapperDominio.mappearDeFechaEcaesAFechaEcaesDTORespuesta(fechas.get());
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/actualizarFechasEcaes")
    public ResponseEntity<FechaEcaesDTORespuesta> actualizarFechasEcaes(@RequestBody FechasEcaesDTOPeticion fechasEcaes) {
        FechaEcaes fechaEcaes = solicitudMapperDominio.mappearDeFechasEcaesDTOPeticionAFechaEcaes(fechasEcaes);
        FechaEcaes fechaActualizada = solicitudEcaesCU.actualizarFechasEcaes(fechaEcaes);
        FechaEcaesDTORespuesta respuesta = solicitudMapperDominio.mappearDeFechaEcaesAFechaEcaesDTORespuesta(fechaActualizada);
        return ResponseEntity.ok(respuesta);
    }
    
}
