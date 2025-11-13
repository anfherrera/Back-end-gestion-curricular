// package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controller;

// import java.util.Date;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.http.HttpStatus;

// import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
// import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudEcaesCUAdapter;
// import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
// import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudEcaes;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.solicitudEcaesDto;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;
// import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;



// @RestController
// @RequestMapping("/solicitudes-ecaes")
// public class SolicitudEcaesEstController {

//    private final GestionarSolicitudEcaesCUIntPort solicitudCU;
//    private final Mapper mapper;

//    public SolicitudEcaesEstController(GestionarSolicitudEcaesCUIntPort solicitudCU, Mapper mapper) {
//        this.solicitudCU = solicitudCU;
//        this.mapper = mapper;
//    }

//    // Crear solicitud ECAES
//    @PostMapping
//    public ResponseEntity<solicitudEcaesDto> crearSolicitud(@RequestBody solicitudEcaesDto dto) {
//        SolicitudEcaes solicitud = mapper.map(dto, SolicitudEcaes.class);
//        solicitud.setFecha_registro_solicitud(new Date(System.currentTimeMillis()));

//        SolicitudEcaes creada = solicitudCU.guardar(solicitud);
//        solicitudEcaesDto respuesta = mapper.map(creada, solicitudEcaesDto.class);
//        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
//    }

//    // Cambiar estado de la solicitud
//    @PutMapping("/{id}/estado")
//    public ResponseEntity<Void> cambiarEstado(
//            @PathVariable Integer id,
//            @RequestParam EstadoSolicitudEcaes nuevoEstado
//    ) {
//        solicitudCU.cambiarEstadoSolicitudEcaes(id, nuevoEstado);
//        return ResponseEntity.ok().build();
//    }

//    // Listar todas las solicitudes
//    @GetMapping
//    public ResponseEntity<List<solicitudEcaesDto>> listarSolicitudes() {
//        List<SolicitudEcaes> solicitudes = solicitudCU.listarSolicitudes();
//        List<solicitudEcaesDto> dtos = solicitudes.stream()
//                .map(s -> mapper.map(s, solicitudEcaesDto.class))
//                .collect(Collectors.toList());

//        return ResponseEntity.ok(dtos);
//    }

//    // Buscar solicitud por ID
//    @GetMapping("/{id}")
//    public ResponseEntity<solicitudEcaesDto> obtenerPorId(@PathVariable Integer id) {
//        Optional<SolicitudEcaes> opt = Optional.ofNullable(solicitudCU.buscarPorId(id));
//        return opt
//                .map(sol -> new ResponseEntity<>(mapper.map(sol, solicitudEcaesDto.class), HttpStatus.OK))
//                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }
// }
    