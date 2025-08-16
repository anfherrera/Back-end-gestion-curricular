package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.Security.JwtUtil;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.LoginDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UsuarioDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.LoginDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.usuarioSalidaDto;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Validated
public class UsuarioRestController {
    private final GestionarUsuarioCUIntPort objUsuarioCUIntPort;
    private final UsuarioMapperDominio objUsuarioMapperDominio;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    @PostMapping("/crearUsuario")
    public ResponseEntity<UsuarioDTORespuesta> crearUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        Usuario usuario = objUsuarioMapperDominio.mappearDeUsuarioDTOPeticionAUsuario(peticion);
        Usuario usuarioCreado = objUsuarioCUIntPort.crearUsuario(usuario);
        ResponseEntity<UsuarioDTORespuesta> objRespuesta = new ResponseEntity<UsuarioDTORespuesta>( 
            objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuarioCreado), HttpStatus.CREATED);
        return objRespuesta;
    }
    
    @PutMapping("/actualizarUsuario")
    public ResponseEntity<UsuarioDTORespuesta> actualizarrUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        Usuario usuario = objUsuarioMapperDominio.mappearDeUsuarioDTOPeticionAUsuario(peticion);
        Usuario usuarioActualizado = objUsuarioCUIntPort.actualizarUsuario(usuario);
        ResponseEntity<UsuarioDTORespuesta> objRespuesta = new ResponseEntity<UsuarioDTORespuesta>( 
            objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuarioActualizado), HttpStatus.ACCEPTED);
        return objRespuesta;
    }

    @GetMapping("/buscarUsuarioPorId/{id}")
    public ResponseEntity<UsuarioDTORespuesta> buscarUsuarioPorId(@Min(value = 1) @PathVariable Integer id) {
        Usuario usuario = objUsuarioCUIntPort.obtenerUsuarioPorId(id);
        ResponseEntity<UsuarioDTORespuesta> objRespuesta = null;
        if (usuario == null) {
            objRespuesta = new ResponseEntity<UsuarioDTORespuesta>(HttpStatus.NOT_FOUND);
        }
        objRespuesta = new ResponseEntity<UsuarioDTORespuesta>(
        objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuario), HttpStatus.OK);
        return objRespuesta;
    }

    @DeleteMapping("/eliminarUsuario/{id}")
    public ResponseEntity<Boolean> eliminarUsuario(@Min(value = 1) @PathVariable Integer id) {
        boolean respuesta = objUsuarioCUIntPort.eliminarUsuario(id);
        ResponseEntity<Boolean> objRespuesta = null;
        if(respuesta){
            objRespuesta = new ResponseEntity<Boolean>(HttpStatus.OK);
        }else{
            objRespuesta = new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        return objRespuesta;
    }

    @GetMapping("/buscarPorPrograma/{id}")
    public ResponseEntity<List<UsuarioDTORespuesta>> buscarUsuariosPorPrograma(@Min(value = 1) @PathVariable Integer id) {
        List<Usuario> usuarios = objUsuarioCUIntPort.buscarUsuariosPorPrograma(id);
        List<UsuarioDTORespuesta> respuesta = usuarios.stream()
                .map(objUsuarioMapperDominio::mappearDeUsuarioAUsuarioDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarUsuarios")
    public ResponseEntity<List<UsuarioDTORespuesta>> obtenerUsuarios() {
        List<Usuario> usuarios = objUsuarioCUIntPort.listarUsuarios();
        List<UsuarioDTORespuesta> respuesta = usuarios.stream()
                .map(objUsuarioMapperDominio::mappearDeUsuarioAUsuarioDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTOPeticion request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
            String token = jwtUtil.generarToken(userDetails.getUsername());

            return ResponseEntity.ok(new LoginDTORespuesta(token));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}
