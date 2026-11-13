package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Crea la cuenta de administrador la primera vez que arranca la aplicación, si se configuraron
 * {@code app.bootstrap-admin.cedula} y {@code app.bootstrap-admin.password}.
 *
 * <p>Si la cuenta ya existe no se modifica: la contraseña que el administrador haya cambiado se conserva.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    static final String CORREO_ADMINISTRADOR = "admin@unicauca.edu.co";
    private static final int ID_ROL_ADMINISTRADOR = 1;
    private static final int ID_PROGRAMA_POR_DEFECTO = 1;

    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarRolGatewayIntPort rolGateway;
    private final GestionarProgramaGatewayIntPort programaGateway;
    private final GestionarUsuarioCUIntPort usuarioCU;

    @Value("${app.bootstrap-admin.cedula:}")
    private String cedulaAdministrador;

    @Value("${app.bootstrap-admin.password:}")
    private String passwordAdministrador;

    @Override
    public void run(String... args) {
        if (cedulaAdministrador.isBlank() || passwordAdministrador.isBlank()) {
            return;
        }
        try {
            if (usuarioGateway.buscarUsuarioPorCorreo(CORREO_ADMINISTRADOR) == null) {
                crearAdministrador();
            }
        } catch (RuntimeException e) {
            log.warn("No se pudo inicializar la cuenta de administrador", e);
        }
    }

    private void crearAdministrador() {
        Rol rolAdmin = rolGateway.bucarRolPorId(ID_ROL_ADMINISTRADOR);
        if (rolAdmin == null) {
            log.error("No se puede crear el administrador: el rol Administrador (ID={}) no existe", ID_ROL_ADMINISTRADOR);
            return;
        }
        Programa programa = programaGateway.buscarPorIdPrograma(ID_PROGRAMA_POR_DEFECTO);
        if (programa == null) {
            log.error("No se puede crear el administrador: el programa (ID={}) no existe", ID_PROGRAMA_POR_DEFECTO);
            return;
        }

        Usuario admin = new Usuario();
        admin.setCodigo("ADMIN001");
        admin.setCedula(cedulaAdministrador);
        admin.setNombre_completo("Administrador del Sistema");
        admin.setCorreo(CORREO_ADMINISTRADOR);
        admin.setPassword(passwordAdministrador); // el caso de uso la guarda con BCrypt
        admin.setEstado_usuario(true);
        admin.setObjRol(rolAdmin);
        admin.setObjPrograma(programa);

        usuarioCU.crearUsuario(admin);
        log.info("Cuenta de administrador {} creada", CORREO_ADMINISTRADOR);
    }
}
