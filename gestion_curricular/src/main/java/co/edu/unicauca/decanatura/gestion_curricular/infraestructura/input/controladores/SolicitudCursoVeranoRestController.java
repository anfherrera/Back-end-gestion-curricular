package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCurosoVeranoPreinscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoVeranoInscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoInscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoInscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solicitudes-curso-verano")
@RequiredArgsConstructor
public class SolicitudCursoVeranoRestController {

   
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudCursoVeranoPreinscripcionMapper;
    private final SolicitudCursoDeVeranoInscripcionMapperDominio solicitudCursoDeVeranoInscripcionMapper;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;


    
    @PostMapping("/crearCursoVeranoPreinscripcion")
    public ResponseEntity<SolicitudCursoVeranoPreinscripcionDTORespuesta> crearSolicitudCursoVeranoPreinscripcion(
            @RequestBody SolicitudCurosoVeranoPreinscripcionDTOPeticion solicitud) {

        SolicitudCursoVeranoPreinscripcion solicitudDominio = solicitudCursoVeranoPreinscripcionMapper
                .mappearDePeticionASolicitudCursoVeranoPreinscripcion(solicitud);

        SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU
                .crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

        SolicitudCursoVeranoPreinscripcionDTORespuesta respuesta = solicitudCursoVeranoPreinscripcionMapper
                .mappearDeSolicitudCursoVeranoPreinscripcionARespuesta(solicitudGuardada);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @PostMapping("/crearCursoVeranoInscripcion")
    public ResponseEntity<SolicitudCursoVeranoInscripcionDTORespuesta> crearSolicitudCursoVeranoInscripcion(
            @RequestBody SolicitudCursoVeranoInscripcionDTOPeticion solicitud) {

        SolicitudCursoVeranoIncripcion solicitudDominio = solicitudCursoDeVeranoInscripcionMapper
                .mappearDePeticionASolicitudCursoVeranoIncripcion(solicitud);

        SolicitudCursoVeranoIncripcion solicitudGuardada = solicitudCU
                .crearSolicitudCursoVeranoInscripcion(solicitudDominio);

        SolicitudCursoVeranoInscripcionDTORespuesta respuesta = solicitudCursoDeVeranoInscripcionMapper
                .mappearDeSolicitudCursoVeranoIncripcionARespuesta(solicitudGuardada);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }
}
