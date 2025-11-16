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
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos que se ejecuta al iniciar la aplicación
 * Crea el usuario administrador si no existe
 */
@Component
@Order(1) // Ejecutar después de que Spring Security esté configurado
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarRolGatewayIntPort rolGateway;
    private final GestionarProgramaGatewayIntPort programaGateway;
    private final GestionarUsuarioCUIntPort usuarioCU;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Esperar un poco para asegurar que la BD esté lista
        Thread.sleep(1000);
        crearUsuarioAdminSiNoExiste();
    }

    private void crearUsuarioAdminSiNoExiste() {
        try {
            // Buscar si el usuario admin ya existe
            Usuario adminExistente = usuarioGateway.buscarUsuarioPorCorreo("admin@unicauca.edu.co");
            
            if (adminExistente == null) {
                log.info("🔧 Usuario administrador no encontrado. Creando usuario administrador por defecto...");
                
                // Verificar que el rol y programa existan
                Rol rolAdmin = rolGateway.bucarRolPorId(1);
                Programa programa = programaGateway.buscarPorIdPrograma(1);
                
                if (rolAdmin == null) {
                    log.error("❌ No se puede crear el usuario admin: El rol Administrador (ID=1) no existe en la base de datos");
                    return;
                }
                
                if (programa == null) {
                    log.error("❌ No se puede crear el usuario admin: El programa (ID=1) no existe en la base de datos");
                    return;
                }
                
                // Crear el usuario administrador usando el caso de uso
                Usuario admin = new Usuario();
                admin.setCodigo("ADMIN001");
                admin.setNombre_completo("Administrador del Sistema");
                admin.setCorreo("admin@unicauca.edu.co");
                admin.setPassword("password123"); // El caso de uso lo hashea automáticamente
                admin.setEstado_usuario(true);
                
                // Configurar rol Administrador (idRol=1)
                admin.setObjRol(rolAdmin);
                
                // Configurar programa (idPrograma=1 - Ingenieria de Sistemas)
                admin.setObjPrograma(programa);
                
                // Crear el usuario usando el caso de uso (tiene validaciones y hashea la password)
                Usuario adminCreado = usuarioCU.crearUsuario(admin);
                
                log.info("✅ Usuario administrador creado exitosamente:");
                log.info("   Correo: admin@unicauca.edu.co");
                log.info("   Password: password123");
                log.info("   Rol: Administrador");
                log.info("   ID: {}", adminCreado.getId_usuario());
            } else {
                log.info("ℹ️  Usuario administrador ya existe (ID: {}, Correo: {})", 
                    adminExistente.getId_usuario(), adminExistente.getCorreo());
                
                // Verificar si el usuario tiene el rol correcto
                if (adminExistente.getObjRol() == null || adminExistente.getObjRol().getId_rol() != 1) {
                    log.warn("⚠️  El usuario admin existe pero no tiene rol Administrador. Actualizando rol...");
                    Rol rolAdmin = rolGateway.bucarRolPorId(1);
                    if (rolAdmin != null) {
                        adminExistente.setObjRol(rolAdmin);
                        usuarioGateway.actualizarUsuario(adminExistente);
                        log.info("✅ Rol Administrador asignado correctamente");
                    }
                }
                
                // Verificar si la contraseña está hasheada correctamente (BCrypt empieza con $2a$ o $2b$)
                String passwordActual = adminExistente.getPassword();
                boolean passwordCorrectamenteHasheada = passwordActual != null && 
                    (passwordActual.startsWith("$2a$") || passwordActual.startsWith("$2b$"));
                
                if (!passwordCorrectamenteHasheada) {
                    log.warn("⚠️  La contraseña del usuario admin no está hasheada correctamente. Actualizando contraseña...");
                    // Actualizar la contraseña hasheada
                    adminExistente.setPassword(passwordEncoder.encode("password123"));
                    usuarioGateway.actualizarUsuario(adminExistente);
                    log.info("✅ Contraseña actualizada correctamente (hash BCrypt)");
                } else {
                    // Verificar que la contraseña funcione
                    boolean passwordValida = passwordEncoder.matches("password123", passwordActual);
                    if (!passwordValida) {
                        log.warn("⚠️  La contraseña hasheada no coincide. Actualizando contraseña...");
                        adminExistente.setPassword(passwordEncoder.encode("password123"));
                        usuarioGateway.actualizarUsuario(adminExistente);
                        log.info("✅ Contraseña actualizada correctamente");
                    } else {
                        log.info("✅ Contraseña verificada correctamente");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("⚠️  No se pudo crear/verificar el usuario administrador: {}", e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detalles del error:", e);
            }
        }
    }
}

