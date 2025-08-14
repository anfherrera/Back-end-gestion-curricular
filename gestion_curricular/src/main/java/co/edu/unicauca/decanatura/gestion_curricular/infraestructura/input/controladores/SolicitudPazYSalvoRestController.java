package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solicitudes-pazysalvo")
@RequiredArgsConstructor
@Validated
public class SolicitudPazYSalvoRestController {
    private final SolicitudPazYSalvoMapperDominio solicitudPazYSalvoMapper;
    private final SolicitudMapperDominio mapper;
    private final GestionarSolicitudPazYSalvoCUIntPort solicitudCU;

    @PostMapping("/crearPazYSalvo")
    public ResponseEntity<SolicitudDTORespuesta> crearSolicitudPazYSalvo(@RequestBody SolicitudPazYSalvoDTOPeticion solicitud) {
        SolicitudPazYSalvo solicitudPazYSalvo = solicitudPazYSalvoMapper.mappearDeSolicitudDTOPeticionASolicitud(solicitud);
        SolicitudPazYSalvo solicitudGuardada = solicitudCU.crearSolicitudPazYSalvo(solicitudPazYSalvo);
        return new ResponseEntity<>(
                mapper.mappearDeSolicitudARespuesta(solicitudGuardada),
                HttpStatus.CREATED
        );
    }

}
