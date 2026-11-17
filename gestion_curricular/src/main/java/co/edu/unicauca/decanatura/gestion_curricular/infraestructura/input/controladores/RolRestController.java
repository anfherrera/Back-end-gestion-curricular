package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
public class RolRestController {

    private final GestionarRolesCUIntPort objRolCUIntPort;
    private final RolMapperDominio objRolMapperDominio;

    @PostMapping("/crearRol")
    public ResponseEntity<RolDTORespuesta> crearRol(@RequestBody @Valid RolDTOPeticion peticion) {
        Rol rol = objRolMapperDominio.mapearDeRolDTOPeticionARol(peticion);
        Rol rolCreado = objRolCUIntPort.guardarRol(rol);
        return new ResponseEntity<>(
                objRolMapperDominio.mapearDeRolARolDTORespuesta(rolCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarRol")
    public ResponseEntity<RolDTORespuesta> actualizarRol(@RequestBody @Valid RolDTOPeticion peticion) {
        Rol rol = objRolMapperDominio.mapearDeRolDTOPeticionARol(peticion);
        Rol rolActualizado = objRolCUIntPort.actualizarRol(rol);
        return new ResponseEntity<>(
                objRolMapperDominio.mapearDeRolARolDTORespuesta(rolActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/buscarRolPorId/{id}")
    public ResponseEntity<RolDTORespuesta> buscarRolPorId(@Min(value = 1) @PathVariable Integer id) {
        Rol rol = objRolCUIntPort.obtenerRolPorId(id);
        if (rol == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objRolMapperDominio.mapearDeRolARolDTORespuesta(rol),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminarRol/{id}")
    public ResponseEntity<Boolean> eliminarRol(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = objRolCUIntPort.eliminarRol(id);
        return new ResponseEntity<>(eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarPorNombre")
    public ResponseEntity<RolDTORespuesta> buscarPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {
        Rol rol = objRolCUIntPort.obtenerRolPorNombre(nombre);
        if (rol == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objRolMapperDominio.mapearDeRolARolDTORespuesta(rol),
                HttpStatus.OK
        );
    }

    @GetMapping("/listarRoles")
    public ResponseEntity<List<RolDTORespuesta>> listarRoles() {
        List<Rol> roles = objRolCUIntPort.listarRoles();
        List<RolDTORespuesta> respuesta = roles.stream()
                .map(objRolMapperDominio::mapearDeRolARolDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

