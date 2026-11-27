package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.LoginDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.LoginGoogleDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.UsuarioDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.LoginDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.UsuarioDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.ErroresUsuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.InicioSesionRechazadoException;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.JwtUtil;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.LoginRateLimiter;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.SecurityAuditService;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.SecurityAuditService.SecurityEventType;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.VerificadorCuentaGoogle;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Usuarios", description = "API para la gestión de usuarios del sistema")
public class UsuarioRestController {
    private final GestionarUsuarioCUIntPort usuarioCUIntPort;
    private final UsuarioMapperDominio usuarioMapperDominio;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;
    private final LoginRateLimiter loginRateLimiter;
    private final VerificadorCuentaGoogle verificadorCuentaGoogle;


    /**
     * Rutas según la convención de EST-7: {@code POST /}, {@code GET /}, {@code GET|PUT|DELETE /{id}} y
     * {@code PUT /{id}/estado}. {@code POST /login} se conserva (decisión del usuario, revisión 26).
     */
    @PostMapping
    @PreAuthorize("(hasRole('Administrador') or hasRole('Coordinador')) and @autorizacion.puedeAsignarRol(#peticion.id_rol)")
    public ResponseEntity<UsuarioDTORespuesta> crearUsuario(@RequestBody @Valid UsuarioDTOPeticion peticion) {
        // Los errores de dominio y los argumentos inválidos llegan al manejador global; el resto, 500 genérico
        return ErroresUsuario.alCrear(() -> {
            if (peticion == null) {
                throw new IllegalArgumentException("La petición no puede ser nula");
            }
            
            Usuario usuario = new Usuario();
            usuario.setNombreCompleto(peticion.getNombre_completo());
            usuario.setCodigo(peticion.getCodigo());
            usuario.setCedula(peticion.getCedula());
            usuario.setCorreo(peticion.getCorreo());
            usuario.setPassword(peticion.getPassword());
            usuario.setEstadoUsuario(peticion.isEstado_usuario());
            
            // Crear objetos Rol y Programa con solo el ID
            if (peticion.getId_rol() == null) {
                throw new IllegalArgumentException("El ID del rol es obligatorio");
            }
            Rol rol = new Rol();
            rol.setIdRol(peticion.getId_rol());
            usuario.setObjRol(rol);
            
            if (peticion.getId_programa() == null) {
                throw new IllegalArgumentException("El ID del programa es obligatorio");
            }
            Programa programa = new Programa();
            programa.setIdPrograma(peticion.getId_programa());
            usuario.setObjPrograma(programa);
            
            Usuario usuarioCreado = usuarioCUIntPort.crearUsuario(usuario);
            
            if (usuarioCreado == null) {
                throw new RuntimeException("Error al crear el usuario: el usuario creado es nulo");
            }
            
            // Validar que las relaciones estén presentes antes de mapear
            if (usuarioCreado.getObjRol() == null) {
                throw new RuntimeException("Error: el usuario creado no tiene rol asignado");
            }
            if (usuarioCreado.getObjPrograma() == null) {
                throw new RuntimeException("Error: el usuario creado no tiene programa asignado");
            }
            
            UsuarioDTORespuesta respuesta = ErroresUsuario.alMapearCreado(
                    () -> usuarioMapperDominio.mapearDeUsuarioAUsuarioDTORespuesta(usuarioCreado),
                    usuarioCreado);
            
            if (respuesta == null) {
                throw new RuntimeException("Error al mapear el usuario creado a DTO de respuesta: resultado nulo");
            }
            
            return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
        });
    }
    
    /**
     * El id de la ruta manda sobre el del cuerpo. La autorización comprueba ese mismo id ({@code #id}): si mirara
     * el del cuerpo, se podría pedir permiso para un usuario y modificar otro.
     */
    @PutMapping("/{id:\\d+}")
    @PreAuthorize("(hasRole('Administrador') or hasRole('Coordinador'))"
            + " and @autorizacion.puedeAsignarRol(#peticion.id_rol)"
            + " and @autorizacion.puedeModificarUsuario(#id)")
    public ResponseEntity<UsuarioDTORespuesta> actualizarrUsuario(@PathVariable Integer id,
            @RequestBody @Valid UsuarioDTOPeticion peticion) {
        peticion.setId_usuario(id);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(peticion.getId_usuario());
        usuario.setNombreCompleto(peticion.getNombre_completo());
        usuario.setCodigo(peticion.getCodigo());
        usuario.setCedula(peticion.getCedula());
        usuario.setCorreo(peticion.getCorreo());
        usuario.setEstadoUsuario(peticion.isEstado_usuario());
        
        // Solo actualizar password si se proporciona
        if (peticion.getPassword() != null && !peticion.getPassword().trim().isEmpty()) {
            usuario.setPassword(peticion.getPassword());
        }
        
        // Crear objetos Rol y Programa con solo el ID
        Rol rol = new Rol();
        rol.setIdRol(peticion.getId_rol());
        usuario.setObjRol(rol);
        
        Programa programa = new Programa();
        programa.setIdPrograma(peticion.getId_programa());
        usuario.setObjPrograma(programa);
        
        Usuario usuarioActualizado = usuarioCUIntPort.actualizarUsuario(usuario);
        return new ResponseEntity<>(
            usuarioMapperDominio.mapearDeUsuarioAUsuarioDTORespuesta(usuarioActualizado),
            HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ") or @autorizacion.esUsuarioActual(#id)")
    public ResponseEntity<UsuarioDTORespuesta> buscarUsuarioPorId(@Min(value = 1) @PathVariable Integer id) {
        Usuario usuario = usuarioCUIntPort.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(usuarioMapperDominio.mapearDeUsuarioAUsuarioDTORespuesta(usuario), HttpStatus.OK);
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Boolean> eliminarUsuario(@Min(value = 1) @PathVariable Integer id) {
        boolean respuesta = usuarioCUIntPort.eliminarUsuario(id);
        ResponseEntity<Boolean> objRespuesta = null;
        if(respuesta){
            objRespuesta = new ResponseEntity<Boolean>(HttpStatus.OK);
        }else{
            objRespuesta = new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        return objRespuesta;
    }

    @GetMapping
    @PreAuthorize("hasRole('Administrador') or hasRole('Coordinador')")
    public ResponseEntity<List<UsuarioDTORespuesta>> obtenerUsuarios() {
        List<Usuario> usuarios = usuarioCUIntPort.listarUsuarios();
        List<UsuarioDTORespuesta> respuesta = usuarios.stream()
                .map(usuarioMapperDominio::mapearDeUsuarioAUsuarioDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
    
    @Operation(
        summary = "Cambiar estado de un usuario",
        description = "Activa o desactiva un usuario. Si está activo lo desactiva y viceversa (sin cuerpo)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente",
            content = @Content(schema = @Schema(implementation = UsuarioDTORespuesta.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content)
    })
    @PutMapping("/{id:\\d+}/estado")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<UsuarioDTORespuesta> cambiarEstadoUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @Min(value = 1) @PathVariable Integer id) {
        Usuario usuario = usuarioCUIntPort.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Cambiar el estado (toggle)
        usuario.setEstadoUsuario(!usuario.isEstadoUsuario());
        
        Usuario usuarioActualizado = usuarioCUIntPort.actualizarUsuario(usuario);
        return new ResponseEntity<>(
            usuarioMapperDominio.mapearDeUsuarioAUsuarioDTORespuesta(usuarioActualizado),
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
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content),
        @ApiResponse(responseCode = "403", description = "Cuenta desactivada", content = @Content),
        @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTOPeticion request, HttpServletRequest httpRequest) {
        // Con server.forward-headers-strategy, getRemoteAddr ya es la IP del cliente aunque haya un proxy
        String ip = httpRequest.getRemoteAddr();
        String correo = request.getCorreo();

        Instant bloqueadoHasta = loginRateLimiter.checkAllowed(correo, ip);
        if (bloqueadoHasta != null) {
            long segundos = Math.max(1, Duration.between(Instant.now(), bloqueadoHasta).getSeconds());
            securityAuditService.logSecurityEvent(SecurityEventType.RATE_LIMIT_EXCEEDED,
                    "Login bloqueado temporalmente", correo, httpRequest);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(segundos))
                .body("Demasiados intentos fallidos. Intenta nuevamente en " + segundos + " segundos.");
        }

        Authentication autenticacion;
        try {
            autenticacion = authManager.authenticate(new UsernamePasswordAuthenticationToken(correo, request.getPassword()));
        } catch (BadCredentialsException ex) {
            loginRateLimiter.onFailure(correo, ip);
            securityAuditService.logSecurityEvent(SecurityEventType.LOGIN_FAILED, "Credenciales incorrectas",
                    correo, httpRequest);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        } catch (DisabledException ex) {
            // El manejador global responde 403
            securityAuditService.logSecurityEvent(SecurityEventType.ACCOUNT_DISABLED, "Login de una cuenta desactivada",
                    correo, httpRequest);
            throw ex;
        }

        loginRateLimiter.onSuccess(correo, ip);
        return ResponseEntity.ok(emitirSesion((UserDetails) autenticacion.getPrincipal(), "Login con contraseña", httpRequest));
    }

    @Operation(
        summary = "Iniciar sesión con Google",
        description = "Recibe el ID token de Google Identity Services, verifica que sea una cuenta institucional "
            + "(@unicauca.edu.co) registrada y activa, y devuelve el mismo token JWT que el login con contraseña",
        security = {}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = LoginDTORespuesta.class))),
        @ApiResponse(responseCode = "401", description = "Token de Google inválido o vencido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Cuenta de otro dominio, sin registro o desactivada",
            content = @Content),
        @ApiResponse(responseCode = "503", description = "Inicio con Google sin configurar o Google no responde",
            content = @Content)
    })
    @PostMapping("/login/google")
    public ResponseEntity<LoginDTORespuesta> loginGoogle(@RequestBody @Valid LoginGoogleDTOPeticion request,
            HttpServletRequest httpRequest) {
        String correo = verificadorCuentaGoogle.correoVerificado(request.getCredencial());

        UserDetails cuenta;
        try {
            cuenta = userDetailsService.loadUserByUsername(correo);
        } catch (UsernameNotFoundException ex) {
            securityAuditService.logSecurityEvent(SecurityEventType.LOGIN_FAILED, "Cuenta de Google sin registro",
                    correo, httpRequest);
            throw new InicioSesionRechazadoException(HttpStatus.FORBIDDEN, "La cuenta " + correo
                    + " no está registrada en el sistema. Contacte al coordinador de su programa para que la registre.");
        }
        if (!cuenta.isEnabled()) {
            securityAuditService.logSecurityEvent(SecurityEventType.ACCOUNT_DISABLED,
                    "Login con Google de una cuenta desactivada", correo, httpRequest);
            throw new DisabledException("Cuenta desactivada");
        }
        return ResponseEntity.ok(emitirSesion(cuenta, "Login con Google", httpRequest));
    }

    /** Token de sesión y datos del usuario, iguales para los dos tipos de inicio de sesión. */
    private LoginDTORespuesta emitirSesion(UserDetails cuenta, String detalle, HttpServletRequest httpRequest) {
        Usuario usuario = usuarioCUIntPort.buscarUsuarioPorCorreo(cuenta.getUsername());
        UsuarioDTORespuesta usuarioDTO = usuarioMapperDominio.mapearDeUsuarioAUsuarioDTORespuesta(usuario);
        securityAuditService.logSecurityEvent(SecurityEventType.LOGIN_SUCCESS, detalle, cuenta.getUsername(), httpRequest);
        return new LoginDTORespuesta(jwtUtil.generarToken(cuenta.getUsername()), usuarioDTO);
    }
}
