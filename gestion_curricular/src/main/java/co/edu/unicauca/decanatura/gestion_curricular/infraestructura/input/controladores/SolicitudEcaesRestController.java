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
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

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
public class SolicitudEcaesRestController {

    private final GestionarSolicitudEcaesCUIntPort solicitudEcaesCU;
    private final SolicitudEcaesMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    
    @PostMapping("/crearSolicitud-Ecaes")
    public ResponseEntity<SolicitudEcaesDTORespuesta> crearSolicitud(@Valid @RequestBody SolicitudEcaesDTOPeticion peticion) {
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
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudesToFuncionario() {
        List<SolicitudEcaes> solicitudes = solicitudEcaesCU.listarSolicitudesToFuncionario();
        List<SolicitudEcaesDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudEcaesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }
    @GetMapping("/buscarSolicitud-Ecaes/{id}")
    public ResponseEntity<SolicitudEcaesDTORespuesta> obtenerPorId(@PathVariable Integer id) {
        SolicitudEcaes solicitud = solicitudEcaesCU.buscarPorId(id);
        SolicitudEcaesDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudEcaesARespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-ecaes/porRol")
    public ResponseEntity<List<SolicitudEcaesDTORespuesta>> listarSolicitudPorRol(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario) {

        List<SolicitudEcaes> solicitudes = solicitudEcaesCU.listarSolicitudesPorRol(rol, idUsuario);

        // Si es coordinador, filtrar adicionalmente por programa
        if ("COORDINADOR".equalsIgnoreCase(rol)) {
            Integer idPrograma = obtenerProgramaCoordinadorAutenticado();
            if (idPrograma != null) {
                solicitudes = solicitudes.stream()
                        .filter(s -> s.getObjUsuario() != null 
                                && s.getObjUsuario().getObjPrograma() != null
                                && s.getObjUsuario().getObjPrograma().getId_programa() != null
                                && s.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma))
                        .toList();
            } else {
                log.warn("No se pudo obtener el programa del coordinador para ECAES, retornando todas las solicitudes");
            }
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

    @GetMapping("/listarFechasEcaes")
    public ResponseEntity<List<FechaEcaesDTORespuesta>> listarFechasEcaes() {
        List<FechaEcaes> fechas = solicitudEcaesCU.listarFechasEcaes();
        List<FechaEcaesDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeFechaEcaesAFechaEcaesDTORespuesta(fechas);
        return ResponseEntity.ok(respuesta);
    }

    
    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Void> actualizarEstadoSolicitud(@RequestBody CambioEstadoSolicitudDTOPeticion solicitudPeticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(solicitudPeticion);
        solicitudEcaesCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

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

    /**
     * Obtiene el ID del programa académico del coordinador autenticado.
     * @return ID del programa o null si no se puede obtener
     */
    private Integer obtenerProgramaCoordinadorAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No hay usuario autenticado");
                return null;
            }

            String email = authentication.getName();
            Usuario usuarioAutenticado = usuarioGateway.buscarUsuarioPorCorreo(email);
            
            if (usuarioAutenticado == null) {
                log.warn("Usuario autenticado no encontrado: {}", email);
                return null;
            }

            // Verificar que sea coordinador
            String rolNombre = usuarioAutenticado.getObjRol() != null 
                    ? usuarioAutenticado.getObjRol().getNombre() 
                    : null;
            
            if (!"Coordinador".equals(rolNombre)) {
                log.warn("El usuario autenticado no es coordinador: {}", rolNombre);
                return null;
            }

            // Obtener el programa del coordinador
            if (usuarioAutenticado.getObjPrograma() != null && usuarioAutenticado.getObjPrograma().getId_programa() != null) {
                return usuarioAutenticado.getObjPrograma().getId_programa();
            }

            log.warn("El coordinador no tiene programa asignado");
            return null;
        } catch (Exception e) {
            log.error("Error al obtener programa del coordinador autenticado", e);
            return null;
        }
    }
    
}
