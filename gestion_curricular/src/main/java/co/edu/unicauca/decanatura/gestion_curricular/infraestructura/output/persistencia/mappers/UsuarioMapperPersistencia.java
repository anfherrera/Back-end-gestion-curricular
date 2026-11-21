package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

/**
 * Mapeo explícito entre usuarios y roles del dominio y sus entidades (DUP-10).
 *
 * Reproduce la configuración de {@code ModelMapperConfig}: el usuario no lleva sus solicitudes ni
 * sus cursos, y su rol y su programa son copias sin relaciones. La única excepción es un rol sin id
 * al pasar a entidad, que se copia completo, con sus usuarios. Un rol se mapea con sus usuarios:
 * una lista nula queda nula en el dominio y vacía en la entidad.
 */
public final class UsuarioMapperPersistencia {

    private UsuarioMapperPersistencia() {
    }

    public static Usuario aDominio(UsuarioEntity entidad) {
        Origen.exigir(entidad);
        Usuario usuario = new Usuario();
        usuario.setId_usuario(entidad.getId_usuario());
        usuario.setNombre_completo(entidad.getNombre_completo());
        usuario.setCodigo(entidad.getCodigo());
        usuario.setCedula(entidad.getCedula());
        usuario.setCorreo(entidad.getCorreo());
        usuario.setPassword(entidad.getPassword());
        usuario.setEstado_usuario(entidad.isEstado_usuario());
        RolEntity rol = entidad.getObjRol();
        if (rol != null) {
            usuario.setObjRol(new Rol(rol.getId_rol(), rol.getNombre(), null));
        }
        ProgramaEntity programa = entidad.getObjPrograma();
        if (programa != null) {
            usuario.setObjPrograma(new Programa(programa.getId_programa(), programa.getCodigo(),
                    programa.getNombre_programa()));
        }
        return usuario;
    }

    public static UsuarioEntity aEntidad(Usuario usuario) {
        Origen.exigir(usuario);
        return aEntidad(usuario, new IdentityHashMap<>());
    }

    public static Rol aDominio(RolEntity entidad) {
        Origen.exigir(entidad);
        Rol rol = new Rol();
        rol.setId_rol(entidad.getId_rol());
        rol.setNombre(entidad.getNombre());
        if (entidad.getUsuarios() != null) {
            List<Usuario> usuarios = new ArrayList<>();
            for (UsuarioEntity usuario : entidad.getUsuarios()) {
                usuarios.add(usuario == null ? null : aDominio(usuario));
            }
            rol.setUsuarios(usuarios);
        }
        return rol;
    }

    public static RolEntity aEntidad(Rol rol) {
        Origen.exigir(rol);
        return aEntidad(rol, new IdentityHashMap<>());
    }

    private static UsuarioEntity aEntidad(Usuario usuario, Map<Object, Object> mapeados) {
        UsuarioEntity entidad = new UsuarioEntity();
        entidad.setId_usuario(usuario.getId_usuario());
        entidad.setNombre_completo(usuario.getNombre_completo());
        entidad.setCodigo(usuario.getCodigo());
        entidad.setCedula(usuario.getCedula());
        entidad.setCorreo(usuario.getCorreo());
        entidad.setPassword(usuario.getPassword());
        entidad.setEstado_usuario(usuario.isEstado_usuario());
        Rol rol = usuario.getObjRol();
        if (rol != null) {
            if (rol.getId_rol() != null) {
                RolEntity copia = new RolEntity();
                copia.setId_rol(rol.getId_rol());
                copia.setNombre(rol.getNombre());
                entidad.setObjRol(copia);
            } else {
                entidad.setObjRol(aEntidad(rol, mapeados));
            }
        }
        Programa programa = usuario.getObjPrograma();
        if (programa != null) {
            ProgramaEntity copia = new ProgramaEntity();
            copia.setId_programa(programa.getId_programa());
            copia.setCodigo(programa.getCodigo());
            copia.setNombre_programa(programa.getNombre_programa());
            entidad.setObjPrograma(copia);
        }
        return entidad;
    }

    /** Copia completa del rol; {@code mapeados} corta los ciclos como la caché de ModelMapper. */
    private static RolEntity aEntidad(Rol rol, Map<Object, Object> mapeados) {
        Object previo = mapeados.get(rol);
        if (previo != null) {
            return (RolEntity) previo;
        }
        RolEntity entidad = new RolEntity();
        mapeados.put(rol, entidad);
        entidad.setId_rol(rol.getId_rol());
        entidad.setNombre(rol.getNombre());
        if (rol.getUsuarios() != null) {
            List<UsuarioEntity> usuarios = new ArrayList<>();
            for (Usuario usuario : rol.getUsuarios()) {
                usuarios.add(usuario == null ? null : aEntidad(usuario, mapeados));
            }
            entidad.setUsuarios(usuarios);
        }
        return entidad;
    }
}
