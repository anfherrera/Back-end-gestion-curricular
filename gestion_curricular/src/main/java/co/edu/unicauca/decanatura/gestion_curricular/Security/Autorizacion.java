package co.edu.unicauca.decanatura.gestion_curricular.Security;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocumentoRepositoryInt;
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

    private static final String ROL_ADMINISTRADOR = "Administrador";

    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarRolGatewayIntPort rolGateway;
    private final SolicitudRepositoryInt solicitudRepository;
    private final DocumentoRepositoryInt documentoRepository;

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

    /** true si el usuario autenticado puede asignar ese rol: solo el Administrador asigna el rol Administrador. */
    public boolean puedeAsignarRol(Integer idRol) {
        if (tieneRol("Administrador")) {
            return true;
        }
        Rol rol = idRol != null ? rolGateway.obtenerRolPorId(idRol) : null;
        return rol == null || !ROL_ADMINISTRADOR.equalsIgnoreCase(rol.getNombre());
    }

    /** true si el usuario autenticado puede modificar a ese usuario: solo el Administrador modifica administradores. */
    public boolean puedeModificarUsuario(Integer idUsuario) {
        if (tieneRol("Administrador")) {
            return true;
        }
        if (idUsuario == null) {
            return true;
        }
        return usuarioGateway.buscarUsuarioPorId(idUsuario)
                .map(u -> u.getObjRol() == null || !ROL_ADMINISTRADOR.equalsIgnoreCase(u.getObjRol().getNombre()))
                .orElse(true);
    }

    /**
     * Usuario a nombre del cual se crea una solicitud. Solo el Administrador puede crearla a nombre
     * de otra persona; para cualquier otro usuario se ignora el id enviado y se usa el del token.
     */
    public Integer idSolicitante(Integer idEnviado) {
        if (idEnviado != null && tieneRol("Administrador")) {
            return idEnviado;
        }
        return idUsuarioActual();
    }

    /** true si el usuario autenticado puede ver el documento: staff, quien lo subió o el dueño de su solicitud. */
    @Transactional(readOnly = true)
    public boolean puedeVerDocumento(Integer idDocumento) {
        if (esStaff()) {
            return true;
        }
        if (idDocumento == null) {
            return false;
        }
        return documentoRepository.findById(idDocumento).map(this::esDelUsuarioActual).orElse(false);
    }

    /**
     * Ruta en disco del archivo con ese nombre que el usuario autenticado puede ver.
     * El staff ve cualquiera; los demás solo los suyos (si otro estudiante tiene un archivo
     * con el mismo nombre, no se devuelve).
     */
    @Transactional(readOnly = true)
    public Optional<String> rutaArchivoVisible(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return Optional.empty();
        }
        boolean staff = esStaff();
        return documentoRepository.findAllByNombre(nombreArchivo).stream()
                .filter(d -> staff || esDelUsuarioActual(d))
                .findFirst()
                .map(d -> d.getRuta_documento() != null && !d.getRuta_documento().isBlank()
                        ? d.getRuta_documento() : d.getNombre());
    }

    private boolean esDelUsuarioActual(DocumentoEntity documento) {
        Integer actual = idUsuarioActual();
        if (actual == null) {
            return false;
        }
        if (actual.equals(documento.getIdUsuarioSubio())) {
            return true;
        }
        SolicitudEntity solicitud = documento.getObjSolicitud();
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

    /**
     * Programa del coordinador autenticado, para filtrar lo que ve.
     * @return id del programa, o null si no hay sesión, no es coordinador o no tiene programa
     */
    public Integer idProgramaCoordinadorActual() {
        Usuario usuario = usuarioActual();
        if (usuario == null || usuario.getObjRol() == null
                || !"Coordinador".equals(usuario.getObjRol().getNombre())
                || usuario.getObjPrograma() == null) {
            return null;
        }
        return usuario.getObjPrograma().getId_programa();
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
