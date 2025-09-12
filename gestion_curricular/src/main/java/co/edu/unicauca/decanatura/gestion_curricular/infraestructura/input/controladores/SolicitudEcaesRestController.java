package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
public class SolicitudEcaesRestController {

    private final GestionarSolicitudEcaesCUIntPort solicitudEcaesCU;
    private final SolicitudEcaesMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    
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
    
    @GetMapping("/buscarSolicitud-Ecaes/{id}")
    public ResponseEntity<SolicitudEcaesDTORespuesta> obtenerPorId(@PathVariable Integer id) {
        SolicitudEcaes solicitud = solicitudEcaesCU.buscarPorId(id);
        SolicitudEcaesDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudEcaesARespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    // @PutMapping("/cambiarEstado-Ecaes{id}")
    // public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoSolicitudEcaes nuevoEstado) {
    //     solicitudEcaesCU.cambiarEstadoSolicitudEcaes(id, nuevoEstado);
    //     return ResponseEntity.noContent().build();
    // }

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

    // //metodo para agregar un estado a la solicitud
    // @PutMapping("/actualizarEstadoSolicitud")
    // public ResponseEntity<Void> actualizarEstadoSolicitud(@RequestBody SolicitudEcaesDTOPeticion solicitudPeticion, EstadoSolicitudEcaes nuevoEstado) {
    //     SolicitudEcaes solicitud = solicitudMapperDominio.mappearDeSolicitudEcaesDTOPeticionASolicitudEcaes(solicitudPeticion);
    //     solicitudEcaesCU.cambiarEstadoSolicitudEcaes(solicitud.getId_solicitud(),nuevoEstado);
    //     return ResponseEntity.noContent().build();
    // }

    // Intento de actualizar el estado de la solicitud
    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Void> actualizarEstadoSolicitud(@RequestBody CambioEstadoSolicitudDTOPeticion solicitudPeticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(solicitudPeticion);
        solicitudEcaesCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }
    
    
    
}
