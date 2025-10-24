package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
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
        Usuario usuario = new Usuario();
        usuario.setNombre_completo(peticion.getNombre_completo());
        usuario.setCodigo(peticion.getCodigo());
        usuario.setCorreo(peticion.getCorreo());
        usuario.setPassword(peticion.getPassword());
        usuario.setEstado_usuario(peticion.isEstado_usuario());
        
        // Crear objetos Rol y Programa con solo el ID
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol rol = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol();
        rol.setId_rol(peticion.getId_rol());
        usuario.setObjRol(rol);
        
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa programa = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa();
        programa.setId_programa(peticion.getId_programa());
        usuario.setObjPrograma(programa);
        
        Usuario usuarioCreado = objUsuarioCUIntPort.crearUsuario(usuario);
        return new ResponseEntity<>(
            objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuarioCreado), 
            HttpStatus.CREATED
        );
    }
    
    @PutMapping("/actualizarUsuario")
    public ResponseEntity<UsuarioDTORespuesta> actualizarrUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(peticion.getId_usuario());
        usuario.setNombre_completo(peticion.getNombre_completo());
        usuario.setCodigo(peticion.getCodigo());
        usuario.setCorreo(peticion.getCorreo());
        usuario.setEstado_usuario(peticion.isEstado_usuario());
        
        // Solo actualizar password si se proporciona
        if (peticion.getPassword() != null && !peticion.getPassword().trim().isEmpty()) {
            usuario.setPassword(peticion.getPassword());
        }
        
        // Crear objetos Rol y Programa con solo el ID
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol rol = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol();
        rol.setId_rol(peticion.getId_rol());
        usuario.setObjRol(rol);
        
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa programa = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa();
        programa.setId_programa(peticion.getId_programa());
        usuario.setObjPrograma(programa);
        
        Usuario usuarioActualizado = objUsuarioCUIntPort.actualizarUsuario(usuario);
        return new ResponseEntity<>(
            objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuarioActualizado), 
            HttpStatus.ACCEPTED
        );
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
    
    @PutMapping("/cambiarEstado/{id}")
    public ResponseEntity<UsuarioDTORespuesta> cambiarEstadoUsuario(@Min(value = 1) @PathVariable Integer id) {
        Usuario usuario = objUsuarioCUIntPort.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Cambiar el estado (toggle)
        usuario.setEstado_usuario(!usuario.isEstado_usuario());
        
        Usuario usuarioActualizado = objUsuarioCUIntPort.actualizarUsuario(usuario);
        return new ResponseEntity<>(
            objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuarioActualizado), 
            HttpStatus.OK
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTOPeticion request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
            String token = jwtUtil.generarToken(userDetails.getUsername());

            Usuario usuario = objUsuarioCUIntPort.buscarUsuarioPorCorreo(request.getCorreo());
            UsuarioDTORespuesta usuarioDTO = objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuario);

            return ResponseEntity.ok(new LoginDTORespuesta(token, usuarioDTO));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}
