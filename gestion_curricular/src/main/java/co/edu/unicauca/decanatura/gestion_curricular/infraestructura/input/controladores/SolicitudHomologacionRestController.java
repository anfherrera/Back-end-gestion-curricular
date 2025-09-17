package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudHomologacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudHomologacionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudHomologacioneMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/solicitudes-homologacion")
@RequiredArgsConstructor
public class SolicitudHomologacionRestController {
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final SolicitudHomologacioneMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;

    @PostMapping("/crearSolicitud-Homologacion")
    public ResponseEntity<SolicitudHomologacionDTORespuesta> crearSolicitudHomologacion(@Valid @RequestBody SolicitudHomologacionDTOPeticion peticion) {
        SolicitudHomologacion solicitud = solicitudMapperDominio.mappearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(peticion);
        SolicitudHomologacion solicitudCreada = solicitudHomologacionCU.guardar(solicitud);
        ResponseEntity<SolicitudHomologacionDTORespuesta> respuesta = new ResponseEntity<>
        (solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    @GetMapping("/listarSolicitud-Homologacion")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacion() {
        List<SolicitudHomologacion> solicitudes = solicitudHomologacionCU.listarSolicitudes();
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Homologacion/{id}")
    public ResponseEntity<SolicitudHomologacionDTORespuesta> listarHomologacionById(@PathVariable Integer id) {
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(id);
        SolicitudHomologacionDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Void> actualizarEstadoSolicitudHomologacion(@RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        solicitudHomologacionCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    // @GetMapping(" ")
    // public ResponseEntity<SolicitudHomologacionDTORespuesta> obtenerSolicitudHomologacionSeleccionada() {
    //     SolicitudHomologacion solicitud = solicitudHomologacionCU. obtenerSolicitudHomologacion();
    //     SolicitudHomologacionDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitud);
    //     return ResponseEntity.ok(respuesta);
    // }


}
