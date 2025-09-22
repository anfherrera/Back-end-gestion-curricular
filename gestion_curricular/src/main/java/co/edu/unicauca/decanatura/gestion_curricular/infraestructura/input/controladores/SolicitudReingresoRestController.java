package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudReingresoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudHomologacionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudReingresoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudReingresoMapperDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/solicitudes-reingreso")
@RequiredArgsConstructor
public class SolicitudReingresoRestController {

    private final SolicitudReingresoMapperDominio solicitudReingresoMapper;
    private final GestionarSolicitudReingresoCUIntPort solicitudService;
    private final SolicitudMapperDominio solicitudMapper;

    @PostMapping("/crearSolicitud-Reingreso")
    public ResponseEntity<SolicitudReingresoDTORespuesta> crearSolicitudReingreso(@RequestBody SolicitudReingresoDTOPeticion solicitudDTO) {
        SolicitudReingreso solicitud = solicitudReingresoMapper.mappearDeSolicitudReingresoDTOPeticionASolicitudReingreso(solicitudDTO);
        SolicitudReingreso solicitudCreada = solicitudService.crearSolicitudReingreso(solicitud);
        ResponseEntity<SolicitudReingresoDTORespuesta> respuesta = new ResponseEntity<>
        (solicitudReingresoMapper.mappearDeSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    @GetMapping("/listarSolicitudes-Reingreso")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudesReingreso() {
        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingreso();
        List<SolicitudReingresoDTORespuesta> solicitudesDTO = solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);
        return ResponseEntity.ok(solicitudesDTO);
    }
    
    //====================================

    @GetMapping("/listarSolicitud-Reingreso/porUser")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudPorUser(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario) {

        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingresoPorRol(rol, idUsuario);

        List<SolicitudReingresoDTORespuesta> respuesta =
                solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }
//====================================


    @GetMapping("/listarSolicitud-Reingreo/{id}")
    public ResponseEntity<SolicitudReingresoDTORespuesta> listarReingresoById(@PathVariable Integer id) {
        SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(id);
        SolicitudReingresoDTORespuesta respuesta = solicitudReingresoMapper.mappearDeSolicitudReingresoASolicitudReingresoDTORespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud-Reingreso")
    public ResponseEntity<Void> actualizarEstadoSolicitudReingreso(@RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        solicitudService.cambiarEstadoSolicitudReingreso(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

}
