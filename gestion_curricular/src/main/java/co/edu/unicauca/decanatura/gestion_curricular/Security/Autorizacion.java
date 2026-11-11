package co.edu.unicauca.decanatura.gestion_curricular.Security;

import java.util.Set;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import lombok.RequiredArgsConstructor;

/**
 * Reglas de autorización reutilizables desde {@code @PreAuthorize}.
 *
 * Ejemplo: {@code @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")}
 */
@Component("autorizacion")
@RequiredArgsConstructor
public class Autorizacion {

    /** Roles administrativos que gestionan solicitudes. */
    public static final String ROLES_STAFF =
            "'Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Decano'";

    private static final Set<String> STAFF = Set.of(
            "ROLE_Administrador", "ROLE_Funcionario", "ROLE_Coordinador", "ROLE_Secretario", "ROLE_Decano");

    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final SolicitudRepositoryInt solicitudRepository;

    /** true si el usuario autenticado tiene un rol administrativo. */
    public boolean esStaff() {
        Authentication auth = autenticacion();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (STAFF.contains(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /** true si el usuario autenticado tiene el rol indicado (sin el prefijo ROLE_). */
    public boolean tieneRol(String rol) {
        Authentication auth = autenticacion();
        if (auth == null || rol == null) {
            return false;
        }
        String buscado = "ROLE_" + rol;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (buscado.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /** true si el id recibido corresponde al usuario autenticado. */
    public boolean esUsuarioActual(Integer idUsuario) {
        Integer actual = idUsuarioActual();
        return actual != null && actual.equals(idUsuario);
    }

    /** true si la solicitud existe y pertenece al usuario autenticado. */
    @Transactional(readOnly = true)
    public boolean esDuenoSolicitud(Integer idSolicitud) {
        Integer actual = idUsuarioActual();
        if (actual == null || idSolicitud == null) {
            return false;
        }
        SolicitudEntity solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        return solicitud != null
                && solicitud.getObjUsuario() != null
                && actual.equals(solicitud.getObjUsuario().getId_usuario());
    }

    /** Usuario autenticado (cargado desde la BD) o null si no hay sesión. */
    public Usuario usuarioActual() {
        Authentication auth = autenticacion();
        if (auth == null) {
            return null;
        }
        try {
            return usuarioGateway.buscarUsuarioPorCorreo(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }

    /** Id del usuario autenticado o null si no hay sesión. */
    public Integer idUsuarioActual() {
        Usuario usuario = usuarioActual();
        return usuario != null ? usuario.getId_usuario() : null;
    }

    private Authentication autenticacion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
