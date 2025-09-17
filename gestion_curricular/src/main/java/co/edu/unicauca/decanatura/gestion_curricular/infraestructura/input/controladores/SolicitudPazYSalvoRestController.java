package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
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
@RequestMapping("/api/solicitudes-pazysalvo")
@RequiredArgsConstructor
public class SolicitudPazYSalvoRestController {

    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final SolicitudPazYSalvoMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;

    @PostMapping("/crearSolicitud-PazYSalvo")
    public ResponseEntity<SolicitudPazYSalvoDTORespuesta> crearSolicitudPazYSalvo(
            @Valid @RequestBody SolicitudPazYSalvoDTOPeticion peticion) {

        SolicitudPazYSalvo solicitud = solicitudMapperDominio
                .mappearDeSolicitudDTOPeticionASolicitud(peticion);

        SolicitudPazYSalvo solicitudCreada = solicitudPazYSalvoCU.guardar(solicitud);

        ResponseEntity<SolicitudPazYSalvoDTORespuesta> respuesta = new ResponseEntity<>(
                solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitudCreada),
                HttpStatus.CREATED);

        return respuesta;
    }

    @GetMapping("/listarSolicitud-PazYSalvo")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvo() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/{id}")
    public ResponseEntity<SolicitudPazYSalvoDTORespuesta> listarPazYSalvoById(@PathVariable Integer id) {
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(id);
        SolicitudPazYSalvoDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Void> actualizarEstadoSolicitudPazYSalvo(
            @RequestBody CambioEstadoSolicitudDTOPeticion peticion) {

        CambioEstadoSolicitud solicitud = solicitudMapper
                .mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);

        solicitudPazYSalvoCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());

        return ResponseEntity.noContent().build();
    }
}
