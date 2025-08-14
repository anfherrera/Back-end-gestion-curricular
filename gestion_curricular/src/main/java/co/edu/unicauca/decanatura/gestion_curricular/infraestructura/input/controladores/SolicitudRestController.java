package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoInscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCurosoVeranoPreinscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoVeranoInscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;


import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
public class SolicitudRestController {

    private final GestionarSolicitudCUIntPort solicitudCU;
    private final SolicitudMapperDominio mapper;
    private final SolicitudPazYSalvoMapperDominio solicitudPazYSalvoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudCursoVeranoPreinscripcionMapper;
    private final SolicitudCursoDeVeranoInscripcionMapperDominio solicitudCursoDeVeranoInscripcionMapper;

    

    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<SolicitudDTORespuesta> buscarSolicitudPorId(@Min(value = 1) @PathVariable Integer id) {
        Solicitud solicitud = solicitudCU.obtenerSolicitudPorId(id);
        if (solicitud == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                mapper.mappearDeSolicitudARespuesta(solicitud),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Boolean> eliminarSolicitud(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminada = solicitudCU.eliminarSolicitud(id);
        return new ResponseEntity<>(eliminada ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<SolicitudDTORespuesta>> listarSolicitudes() {
        List<Solicitud> solicitudes = solicitudCU.listarSolicitudes();
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorUsuario/{idUsuario}")
    public ResponseEntity<List<SolicitudDTORespuesta>> obtenerPorUsuario(@PathVariable Integer idUsuario) {
        List<Solicitud> solicitudes = solicitudCU.obtenerSolicitudesPorUsuario(idUsuario);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorNombre")
    public ResponseEntity<List<SolicitudDTORespuesta>> obtenerPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {
        List<Solicitud> solicitudes = solicitudCU.obtenerSolicitudesPorNombre(nombre);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorFechas")
    public ResponseEntity<List<SolicitudDTORespuesta>> buscarPorFechas(
            @RequestParam(name = "inicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam(name = "fin", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin) {

        List<Solicitud> solicitudes = solicitudCU.buscarSolicitudesPorFecha(inicio, fin);
        List<SolicitudDTORespuesta> respuesta = solicitudes.stream()
                .map(mapper::mappearDeSolicitudARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

//     @PostMapping("/crearPazYSalvo")
//     public ResponseEntity<SolicitudDTORespuesta> crearSolicitudPazYSalvo(@RequestBody SolicitudPazYSalvoDTOPeticion solicitud) {
//         SolicitudPazYSalvo solicitudPazYSalvo = solicitudPazYSalvoMapper.mappearDeSolicitudDTOPeticionASolicitud(solicitud);
//         SolicitudPazYSalvo solicitudGuardada = solicitudCU.crearSolicitudPazYSalvo(solicitudPazYSalvo);
//         return new ResponseEntity<>(
//                 mapper.mappearDeSolicitudARespuesta(solicitudGuardada),
//                 HttpStatus.CREATED
//         );
//     }

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
