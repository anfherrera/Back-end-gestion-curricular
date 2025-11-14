// package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import co.edu.unicauca.decanatura.gestion_curricular.Security.JwtUtil;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.LoginDTOPeticion;
// import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.LoginDTORespuesta;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/auth")
// @Validated
// public class AuthController {
//    private final AuthenticationManager authManager;
//    private final JwtUtil jwtUtil;
//    private final UserDetailsService userDetailsService;

//    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
//        this.authManager = authManager;
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginDTOPeticion request) {
//        try {
//            Authentication authentication = authManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
//            );

//            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
//            String token = jwtUtil.generarToken(userDetails.getUsername());

//            return ResponseEntity.ok(new LoginDTORespuesta(token));

//        } catch (BadCredentialsException ex) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
//        }
//    }
// }
