package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UsuarioDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Validated
public class UsuarioRestController {
    private final GestionarUsuarioCUIntPort objUsuarioCUIntPort;
    private final UsuarioMapperDominio objUsuarioMapperDominio;

    @PostMapping("/crearUsuario")
    public ResponseEntity<UsuarioDTORespuesta> crearUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        Usuario usuario = objUsuarioMapperDominio.mappearDeUsuarioDTOPeticionAUsuario(peticion);
        Usuario usuarioCreado = objUsuarioCUIntPort.crearUsuario(usuario);
        ResponseEntity<UsuarioDTORespuesta> objRespuesta = new ResponseEntity<UsuarioDTORespuesta>( 
            objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuarioCreado), HttpStatus.CREATED);
        return objRespuesta;
    }
    
}
