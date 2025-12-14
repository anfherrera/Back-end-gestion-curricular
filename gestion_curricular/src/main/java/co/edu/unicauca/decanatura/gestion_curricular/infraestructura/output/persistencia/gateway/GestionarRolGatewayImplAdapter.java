package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
public class GestionarRolGatewayImplAdapter implements GestionarRolGatewayIntPort {

    private final RolRepositoryInt objrolRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final ModelMapper rolMapper;
    
    public GestionarRolGatewayImplAdapter(RolRepositoryInt objrolRepository, 
                                          UsuarioRepositoryInt usuarioRepository,
                                          ModelMapper rolMapper) {
        this.objrolRepository = objrolRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolMapper = rolMapper;
    }

    // Métodos originales (compatibilidad)
    @Override
    @Transactional(readOnly = true)
    public Rol bucarRolPorId(Integer idRol) {
        var rolEntity = this.objrolRepository.findById(idRol).orElse(null);
        if (rolEntity == null) {
            return null;
        }
        return this.rolMapper.map(rolEntity, Rol.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol buscarRolPorNombre(String nombre) {
        return this.rolMapper.map(
            this.objrolRepository.buscarPorNombre(nombre).orElse(null), Rol.class);
    }
    
    // Nuevos métodos CRUD
    @Override
    @Transactional
    public Rol guardarRol(Rol rol) {
        RolEntity rolEntity = rolMapper.map(rol, RolEntity.class);
        RolEntity savedEntity = objrolRepository.save(rolEntity);
        return rolMapper.map(savedEntity, Rol.class);
    }
    
    @Override
    @Transactional
    public Rol actualizarRol(Rol rol) {
        objrolRepository.findById(rol.getId_rol())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rol.getId_rol()));

        RolEntity rolEntity = rolMapper.map(rol, RolEntity.class);
        RolEntity savedEntity = objrolRepository.save(rolEntity);
        return rolMapper.map(savedEntity, Rol.class);
    }

    @Override
    @Transactional
    public boolean eliminarRol(Integer idRol) {
        boolean existe = false;
        Optional<RolEntity> rolEntity = objrolRepository.findById(idRol);
        if (rolEntity.isPresent()) {
            objrolRepository.eliminarPorId(idRol);
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerRolPorId(Integer idRol) {
        Optional<RolEntity> rolEntity = objrolRepository.findById(idRol);
        if (rolEntity.isPresent()) {
            return rolMapper.map(rolEntity.get(), Rol.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerRolPorNombre(String nombre) {
        Rol rolRetornar = null;
        Optional<RolEntity> rolEntity = objrolRepository.buscarPorNombre(nombre);
        if (rolEntity.isPresent()) {
            rolRetornar = rolMapper.map(rolEntity.get(), Rol.class);
        }
        return rolRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRolPorNombre(String nombre) {
        boolean existe = false;
        Optional<RolEntity> rolEntity = objrolRepository.buscarPorNombre(nombre);
        if (rolEntity.isPresent()) {
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        List<Rol> rolesRetornar = null;
        List<RolEntity> rolesEntity = objrolRepository.findAll();
        if (rolesEntity != null) {
            rolesRetornar = rolesEntity.stream().map(rolEntity -> {
                return this.rolMapper.map(rolEntity, Rol.class);
            }).toList();
        }
        return rolesRetornar;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneUsuariosAsociados(Integer idRol) {
        return !usuarioRepository.buscarPorRol(idRol).isEmpty();
    }
}
