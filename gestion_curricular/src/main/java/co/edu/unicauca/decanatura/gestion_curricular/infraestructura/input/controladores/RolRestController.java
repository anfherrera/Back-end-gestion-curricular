package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarRolesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.RolDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RolDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominio;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
public class RolRestController {

    private final GestionarRolesCUIntPort rolCUIntPort;
    private final RolMapperDominio rolMapperDominio;

    @PostMapping("/crearRol")
    public ResponseEntity<RolDTORespuesta> crearRol(@RequestBody @Valid RolDTOPeticion peticion) {
        Rol rol = rolMapperDominio.mapearDeRolDTOPeticionARol(peticion);
        Rol rolCreado = rolCUIntPort.guardarRol(rol);
        return new ResponseEntity<>(
                rolMapperDominio.mapearDeRolARolDTORespuesta(rolCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarRol")
    public ResponseEntity<RolDTORespuesta> actualizarRol(@RequestBody @Valid RolDTOPeticion peticion) {
        Rol rol = rolMapperDominio.mapearDeRolDTOPeticionARol(peticion);
        Rol rolActualizado = rolCUIntPort.actualizarRol(rol);
        return new ResponseEntity<>(
                rolMapperDominio.mapearDeRolARolDTORespuesta(rolActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/buscarRolPorId/{id}")
    public ResponseEntity<RolDTORespuesta> buscarRolPorId(@Min(value = 1) @PathVariable Integer id) {
        Rol rol = rolCUIntPort.obtenerRolPorId(id);
        if (rol == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                rolMapperDominio.mapearDeRolARolDTORespuesta(rol),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminarRol/{id}")
    public ResponseEntity<Boolean> eliminarRol(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = rolCUIntPort.eliminarRol(id);
        return new ResponseEntity<>(eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarPorNombre")
    public ResponseEntity<RolDTORespuesta> buscarPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {
        Rol rol = rolCUIntPort.obtenerRolPorNombre(nombre);
        if (rol == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                rolMapperDominio.mapearDeRolARolDTORespuesta(rol),
                HttpStatus.OK
        );
    }

    @GetMapping("/listarRoles")
    public ResponseEntity<List<RolDTORespuesta>> listarRoles() {
        List<Rol> roles = rolCUIntPort.listarRoles();
        List<RolDTORespuesta> respuesta = roles.stream()
                .map(rolMapperDominio::mapearDeRolARolDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

