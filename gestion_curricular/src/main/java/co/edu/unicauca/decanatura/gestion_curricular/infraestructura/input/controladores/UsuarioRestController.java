package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import co.edu.unicauca.decanatura.gestion_curricular.Security.LoginRateLimiter;
import co.edu.unicauca.decanatura.gestion_curricular.Security.SecurityAuditService;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.LoginDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UsuarioDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.LoginDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Usuarios", description = "API para la gestión de usuarios del sistema")
public class UsuarioRestController {
    private final GestionarUsuarioCUIntPort objUsuarioCUIntPort;
    private final UsuarioMapperDominio objUsuarioMapperDominio;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;
    private final LoginRateLimiter loginRateLimiter;


    @PostMapping("/crearUsuario")
    @PreAuthorize("hasRole('Administrador') or hasRole('Coordinador')")
    public ResponseEntity<UsuarioDTORespuesta> crearUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        Usuario usuario = new Usuario();
        usuario.setNombre_completo(peticion.getNombre_completo());
        usuario.setCodigo(peticion.getCodigo());
        usuario.setCedula(peticion.getCedula());
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
    @PreAuthorize("hasRole('Administrador') or hasRole('Coordinador')")
    public ResponseEntity<UsuarioDTORespuesta> actualizarrUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(peticion.getId_usuario());
        usuario.setNombre_completo(peticion.getNombre_completo());
        usuario.setCodigo(peticion.getCodigo());
        usuario.setCedula(peticion.getCedula());
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
    @PreAuthorize("hasRole('Administrador')")
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
    
    @Operation(
        summary = "Cambiar estado de un usuario",
        description = "Activa o desactiva un usuario. Si está activo lo desactiva y viceversa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente",
            content = @Content(schema = @Schema(implementation = UsuarioDTORespuesta.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content)
    })
    @PutMapping("/cambiarEstado/{id}")
    public ResponseEntity<UsuarioDTORespuesta> cambiarEstadoUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @Min(value = 1) @PathVariable Integer id) {
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

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario y devuelve un token JWT para acceder a los endpoints protegidos",
        security = {}  // Este endpoint NO requiere autenticación
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = LoginDTORespuesta.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
            content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTOPeticion request, HttpServletRequest httpRequest) {
        try {
            // Rate limiting por correo e IP
            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = httpRequest.getRemoteAddr();
            }
            var lockedUntil = loginRateLimiter.checkAllowed(request.getCorreo(), ip);
            if (lockedUntil != null) {
                // 429 Too Many Requests con Retry-After aproximado
                long retryAfterSeconds = Math.max(1, java.time.Duration.between(java.time.Instant.now(), lockedUntil).getSeconds());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfterSeconds))
                    .body("Demasiados intentos fallidos. Intenta nuevamente en " + retryAfterSeconds + " segundos.");
            }

            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
            String token = jwtUtil.generarToken(userDetails.getUsername());

            Usuario usuario = objUsuarioCUIntPort.buscarUsuarioPorCorreo(request.getCorreo());
            UsuarioDTORespuesta usuarioDTO = objUsuarioMapperDominio.mappearDeUsuarioAUsuarioDTORespuesta(usuario);

            // Limpiar contadores por éxito
            loginRateLimiter.onSuccess(request.getCorreo(), ip);

            // Registrar login exitoso en auditoría de seguridad
            securityAuditService.logSecurityEvent(
                SecurityAuditService.SecurityEventType.LOGIN_SUCCESS,
                "Login exitoso",
                request.getCorreo(),
                httpRequest
            );

            return ResponseEntity.ok(new LoginDTORespuesta(token, usuarioDTO));

        } catch (BadCredentialsException ex) {
            // Registrar intento fallido en limitador
            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = httpRequest.getRemoteAddr();
            }
            loginRateLimiter.onFailure(request.getCorreo(), ip);

            // Registrar intento de login fallido en auditoría de seguridad
            securityAuditService.logSecurityEvent(
                SecurityAuditService.SecurityEventType.LOGIN_FAILED,
                "Credenciales incorrectas para: " + request.getCorreo(),
                null,
                httpRequest
            );
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}
