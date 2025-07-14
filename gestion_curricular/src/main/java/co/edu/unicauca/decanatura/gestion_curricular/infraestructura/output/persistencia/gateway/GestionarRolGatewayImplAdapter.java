package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;

@Service
@Transactional
public class GestionarRolGatewayImplAdapter implements GestionarRolGatewayIntPort {

    private final RolRepositoryInt objrolRepository;
    private final ModelMapper rolMapper;
    public GestionarRolGatewayImplAdapter(RolRepositoryInt objrolRepository, ModelMapper rolMapper) {
        this.objrolRepository = objrolRepository;
        this.rolMapper = rolMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Rol bucarRolPorId(Integer idRol) {
        return this.rolMapper.map(
            this.objrolRepository.findById(idRol).orElse(null), Rol.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol buscarRolPorNombre(String nombre) {
        return this.rolMapper.map(
            this.objrolRepository.buscarPorNombreParcial(nombre).orElse(null), Rol.class);
    }


    
}
