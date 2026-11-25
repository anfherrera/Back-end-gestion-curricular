package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.UsuarioMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
public class GestionarUsuarioGatewayImplAdapter implements GestionarUsuarioGatewayIntPort, UserDetailsService {

    private final UsuarioRepositoryInt usuarioRepository;
    private final SolicitudRepositoryInt solicitudRepository;

    public GestionarUsuarioGatewayImplAdapter(UsuarioRepositoryInt usuarioRepository, SolicitudRepositoryInt solicitudRepository) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        
        // Validar que las relaciones no sean null antes de mapear
        if (usuario.getObjRol() == null) {
            throw new IllegalArgumentException("El rol del usuario no puede ser nulo");
        }
        if (usuario.getObjRol().getIdRol() == null) {
            throw new IllegalArgumentException("El ID del rol no puede ser nulo");
        }
        if (usuario.getObjPrograma() == null) {
            throw new IllegalArgumentException("El programa del usuario no puede ser nulo");
        }
        if (usuario.getObjPrograma().getIdPrograma() == null) {
            throw new IllegalArgumentException("El ID del programa no puede ser nulo");
        }
        UsuarioEntity entity;
        try {
            entity = UsuarioMapperPersistencia.aEntidad(usuario);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("source cannot be null")) {
                throw new IllegalArgumentException("Error al mapear usuario: el objeto usuario o alguna de sus propiedades es nula. " +
                    "Rol ID: " + (usuario.getObjRol() != null ? usuario.getObjRol().getIdRol() : "null") + 
                    ", Programa ID: " + (usuario.getObjPrograma() != null ? usuario.getObjPrograma().getIdPrograma() : "null"), e);
            }
            throw e;
        }
        if (entity == null) {
            throw new IllegalArgumentException("Error al mapear el usuario a entidad");
        }
        
        // Asegurar que la cédula se mapee correctamente
        if (usuario.getCedula() != null && !usuario.getCedula().trim().isEmpty()) {
            entity.setCedula(usuario.getCedula().trim());
        }
        
        // Validar que las relaciones se mapearon correctamente
        if (entity.getObjRol() == null) {
            throw new IllegalArgumentException("Error: el rol no se mapeó correctamente a la entidad");
        }
        if (entity.getObjPrograma() == null) {
            throw new IllegalArgumentException("Error: el programa no se mapeó correctamente a la entidad");
        }
        
        UsuarioEntity saved = usuarioRepository.save(entity);
        if (saved == null) {
            throw new IllegalArgumentException("Error al guardar el usuario");
        }
        
        // Recargar la entidad con las relaciones cargadas usando JOIN FETCH
        UsuarioEntity reloaded = usuarioRepository.findByIdWithRelations(saved.getId_usuario())
                .orElseThrow(() -> new RuntimeException("Error al recargar el usuario guardado"));
        
        // Validar que las relaciones estén cargadas
        if (reloaded.getObjRol() == null) {
            throw new RuntimeException("Error: el rol no se cargó correctamente después de guardar");
        }
        if (reloaded.getObjPrograma() == null) {
            throw new RuntimeException("Error: el programa no se cargó correctamente después de guardar");
        }
        
        Usuario resultado = UsuarioMapperPersistencia.aDominio(reloaded);
        if (resultado == null) {
            throw new IllegalArgumentException("Error al mapear la entidad guardada a usuario");
        }
        return resultado;
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
        UsuarioEntity existente = usuarioRepository.findById(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getIdUsuario()));

        UsuarioEntity entity = UsuarioMapperPersistencia.aEntidad(usuario);
        if (entity.getPassword() == null) {
            // BUG-46: el dominio no lleva el hash; sin contraseña nueva se conserva la guardada.
            entity.setPassword(existente.getPassword());
        }
        UsuarioEntity updated = usuarioRepository.save(entity);
        return UsuarioMapperPersistencia.aDominio(updated);
    }

    @Override
    @Transactional
    public boolean eliminarUsuario(Integer id_usuario) {
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.findById(id_usuario);
        if (usuarioEntity.isPresent()) {
            usuarioRepository.eliminarPorId(usuarioEntity.get().getId_usuario());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Integer id_usuario) {
        // Usar findByIdWithRelations para cargar todas las relaciones, incluyendo la cédula
        return usuarioRepository.findByIdWithRelations(id_usuario)
                .map(entity -> UsuarioMapperPersistencia.aDominio(entity))
                .orElse(null);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
            .map(entity -> Optional.of(UsuarioMapperPersistencia.aDominio(entity)))
            .orElse(Optional.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorCorreo(String correo) {
        // Usar buscarPorCorreoWithRelations para cargar programa y rol (necesario para filtros de coordinador)
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.buscarPorCorreoWithRelations(correo);
        return usuarioEntity.map(entity -> UsuarioMapperPersistencia.aDominio(entity)).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.buscarPorCedula(cedula.trim());
        return usuarioEntity.map(entity -> UsuarioMapperPersistencia.aDominio(entity)).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuariosPorRol(Integer idRol) {
        List<UsuarioEntity> entities = usuarioRepository.buscarPorRol(idRol);
        return entities.stream()
                .map(entity -> UsuarioMapperPersistencia.aDominio(entity))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        List<UsuarioEntity> entities = usuarioRepository.findAll();
        return entities.stream()
                .map(entity -> UsuarioMapperPersistencia.aDominio(entity))
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // BUG-46: el login es lo único que necesita el hash, y lo lee de la entidad, no del dominio.
        UsuarioEntity usuario = usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (usuario.getObjRol() == null || usuario.getObjRol().getNombre() == null) {
            throw new UsernameNotFoundException("El usuario no tiene un rol asignado: " + correo);
        }

        String rolNombre = usuario.getObjRol().getNombre().trim();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rolNombre));

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getPassword() != null ? usuario.getPassword() : "",
                authorities
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorCodigo(String codigo) {
        UsuarioEntity usuarioEntity = usuarioRepository.buscarIdUsuarioPorCodigo(codigo);
        return usuarioEntity != null ? UsuarioMapperPersistencia.aDominio(usuarioEntity) : null;
    }
}
