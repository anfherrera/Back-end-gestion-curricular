package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;


import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
public class GestionarUsuarioGatewayImplAdapter implements GestionarUsuarioGatewayIntPort, UserDetailsService {

    private final UsuarioRepositoryInt usuarioRepository;
    private final SolicitudRepositoryInt solicitudRepository;
    private final ModelMapper usuarioMapper;

    public GestionarUsuarioGatewayImplAdapter(UsuarioRepositoryInt usuarioRepository, SolicitudRepositoryInt solicitudRepository, ModelMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        UsuarioEntity entity = usuarioMapper.map(usuario, UsuarioEntity.class);
        UsuarioEntity saved = usuarioRepository.save(entity);
        return usuarioMapper.map(saved, Usuario.class);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
        usuarioRepository.findById(usuario.getId_usuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getId_usuario()));

        UsuarioEntity entity = usuarioMapper.map(usuario, UsuarioEntity.class);
        UsuarioEntity updated = usuarioRepository.save(entity);
        return usuarioMapper.map(updated, Usuario.class);
    }

    @Override
    @Transactional
    public boolean eliminarUsuario(Integer id_usuario) {
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.findById(id_usuario);
        if (usuarioEntity.isPresent()) {
            // usuarioRepository.delete(usuarioEntity.get());
            // usuarioRepository.deleteById(id_usuario);
            // usuarioRepository.flush(); // Asegura que la eliminación se realice inmediatamente
            usuarioRepository.eliminarPorId(usuarioEntity.get().getId_usuario());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Integer id_usuario) {
        return usuarioRepository.findById(id_usuario)
                .map(entity -> usuarioMapper.map(entity, Usuario.class))
                .orElse(null);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
            .map(entity -> Optional.of(usuarioMapper.map(entity, Usuario.class)))
            .orElse(Optional.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorCodigo(String codigo) {
        UsuarioEntity usuarioEntity = usuarioRepository.buscarIdUsuarioPorCodigo(codigo);
        return usuarioEntity != null ? usuarioMapper.map(usuarioEntity, Usuario.class) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorCorreo(String correo) {
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.buscarPorCorreo(correo);
        return usuarioEntity.map(entity -> usuarioMapper.map(entity, Usuario.class)).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.buscarPorCedula(cedula.trim());
        return usuarioEntity.map(entity -> usuarioMapper.map(entity, Usuario.class)).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorNombre(String nombre) {
        List<UsuarioEntity> usuarios = usuarioRepository.buscarPorNombreParcial(nombre);
        return usuarios.isEmpty() ? null : usuarioMapper.map(usuarios.get(0), Usuario.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarSolicitudesUsuarioPorCodigo(String codigo) {
        UsuarioEntity usuarioEntity = usuarioRepository.buscarIdUsuarioPorCodigo(codigo);
        if (usuarioEntity == null) return null;

        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarSolicitudesPorUsuario(usuarioEntity.getId_usuario());
        if(solicitudesEntity == null){
            solicitudesEntity = List.of();
        }

        
        usuarioEntity.setSolicitudes(solicitudesEntity);

        return usuarioMapper.map(usuarioEntity, Usuario.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuariosPorRol(Integer idRol) {
        List<UsuarioEntity> entities = usuarioRepository.buscarPorRol(idRol);
        return entities.stream()
                .map(entity -> usuarioMapper.map(entity, Usuario.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuariosPorPrograma(Integer idPrograma) {
        List<UsuarioEntity> entities = usuarioRepository.buscarPorPrograma(idPrograma);
        List<Usuario> usuarios = null;
        if(entities != null){
            usuarios = entities.stream()
                    .map(entity -> usuarioMapper.map(entity, Usuario.class))
                    .toList();
        }
        return usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        List<UsuarioEntity> entities = usuarioRepository.findAll();
        return entities.stream()
                .map(entity -> usuarioMapper.map(entity, Usuario.class))
                .toList();
    }

    @Override
    public Usuario buscarUsuarioPorSolicitud(Integer idSolicitud) {
        UsuarioEntity usuarioEntity = usuarioRepository.buscarUsuariosPorSolicitud(idSolicitud);
        Usuario usuario = null;
        if (usuarioEntity != null) {
            usuario = usuarioMapper.map(usuarioEntity, Usuario.class);
        }
        return usuario;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .map(entity -> usuarioMapper.map(entity, Usuario.class))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getObjRol().getNombre()));

        // SEGURIDAD: No loguear información sensible en producción
        // System.out.println("Usuario autenticado: " + usuario.getCorreo());
        // System.out.println("Contraseña encontrada en BD: " + usuario.getPassword());

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getPassword(),
                authorities
        );
    }
}
