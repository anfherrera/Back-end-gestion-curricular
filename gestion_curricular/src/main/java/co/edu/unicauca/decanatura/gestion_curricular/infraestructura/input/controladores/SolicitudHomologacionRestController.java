// package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
// import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudHomologacionDTOPeticion;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudHomologacionDTORespuesta;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudHomologacioneMapperDominio;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;


// @RestController
// @RequestMapping("/api/solicitudes-homologacion")
// @RequiredArgsConstructor
// public class SolicitudHomologacionRestController {
//     private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
//     private final SolicitudHomologacioneMapperDominio solicitudMapperDominio;

//     @PostMapping("/crearSolicitud-Homologacion")
//     public ResponseEntity<SolicitudHomologacionDTORespuesta> crearSolicitudHomologacion(@Valid @RequestBody SolicitudHomologacionDTOPeticion peticion) {
//         SolicitudHomologacion solicitud = solicitudMapperDominio.mappearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(peticion);
//         SolicitudHomologacion solicitudCreada = solicitudHomologacionCU.guardar(solicitud);
//         ResponseEntity<SolicitudHomologacionDTORespuesta> respuesta = new ResponseEntity<>
//         (solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitudCreada), HttpStatus.CREATED);
//         return respuesta;
//     }
    
// }
