package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.UsuarioMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
public class GestionarRolGatewayImplAdapter implements GestionarRolGatewayIntPort {

    private final RolRepositoryInt objrolRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    
    public GestionarRolGatewayImplAdapter(RolRepositoryInt objrolRepository, 
                                          UsuarioRepositoryInt usuarioRepository) {
        this.objrolRepository = objrolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Métodos originales (compatibilidad)
    @Override
    @Transactional(readOnly = true)
    public Rol buscarRolPorId(Integer idRol) {
        return UsuarioMapperPersistencia.aDominio(this.objrolRepository.findById(idRol).orElse(null));
    }

    // Nuevos métodos CRUD
    @Override
    @Transactional
    public Rol guardarRol(Rol rol) {
        RolEntity rolEntity = UsuarioMapperPersistencia.aEntidad(rol);
        RolEntity savedEntity = objrolRepository.save(rolEntity);
        return UsuarioMapperPersistencia.aDominio(savedEntity);
    }
    
    @Override
    @Transactional
    public Rol actualizarRol(Rol rol) {
        objrolRepository.findById(rol.getIdRol())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rol.getIdRol()));

        RolEntity rolEntity = UsuarioMapperPersistencia.aEntidad(rol);
        RolEntity savedEntity = objrolRepository.save(rolEntity);
        return UsuarioMapperPersistencia.aDominio(savedEntity);
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
            return UsuarioMapperPersistencia.aDominio(rolEntity.get());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerRolPorNombre(String nombre) {
        Rol rolRetornar = null;
        Optional<RolEntity> rolEntity = objrolRepository.buscarPorNombre(nombre);
        if (rolEntity.isPresent()) {
            rolRetornar = UsuarioMapperPersistencia.aDominio(rolEntity.get());
        }
        return rolRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        List<Rol> rolesRetornar = null;
        List<RolEntity> rolesEntity = objrolRepository.findAll();
        if (rolesEntity != null) {
            rolesRetornar = rolesEntity.stream().map(rolEntity -> {
                return UsuarioMapperPersistencia.aDominio(rolEntity);
            }).toList();
        }
        return rolesRetornar;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneUsuariosAsociados(Integer idRol) {
        return !usuarioRepository.buscarPorRol(idRol).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRolPorNombre(String nombre) {
        return objrolRepository.buscarPorNombre(nombre).isPresent();
    }
}
