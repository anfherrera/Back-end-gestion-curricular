package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SalonEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SalonRepositoryInt;

@Service
@Transactional
public class GestionarSalonesGatewayImplAdapter implements GestionarSalonesIntPort {

    private final SalonRepositoryInt salonRepository;
    private final ModelMapper salonMapper;

    public GestionarSalonesGatewayImplAdapter(SalonRepositoryInt salonRepository, 
                                              ModelMapper salonMapper) {
        this.salonRepository = salonRepository;
        this.salonMapper = salonMapper;
    }
    
    @Override
    @Transactional
    public Salon guardarSalon(Salon salon) {
        SalonEntity salonEntity = salonMapper.map(salon, SalonEntity.class);
        SalonEntity savedEntity = salonRepository.save(salonEntity);
        return salonMapper.map(savedEntity, Salon.class);
    }
    
    @Override
    @Transactional
    public Salon actualizarSalon(Salon salon) {
        salonRepository.findById(salon.getId_salon())
            .orElseThrow(() -> new RuntimeException("Salón no encontrado con ID: " + salon.getId_salon()));

        SalonEntity salonEntity = salonMapper.map(salon, SalonEntity.class);
        SalonEntity savedEntity = salonRepository.save(salonEntity);
        return salonMapper.map(savedEntity, Salon.class);
    }

    @Override
    @Transactional
    public boolean eliminarSalon(Integer idSalon) {
        boolean existe = false;
        Optional<SalonEntity> salonEntity = salonRepository.findById(idSalon);
        if (salonEntity.isPresent()) {
            salonRepository.deleteById(idSalon);
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public Salon obtenerSalonPorId(Integer idSalon) {
        Optional<SalonEntity> salonEntity = salonRepository.findById(idSalon);
        if (salonEntity.isPresent()) {
            return salonMapper.map(salonEntity.get(), Salon.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Salon obtenerSalonPorNumero(String numeroSalon) {
        Optional<SalonEntity> salonEntity = salonRepository.buscarPorNumero(numeroSalon);
        if (salonEntity.isPresent()) {
            return salonMapper.map(salonEntity.get(), Salon.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSalonPorNumero(String numeroSalon) {
        Optional<SalonEntity> salonEntity = salonRepository.buscarPorNumero(numeroSalon);
        return salonEntity.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Salon> listarSalones() {
        return salonRepository.findAll().stream()
                .map(entity -> salonMapper.map(entity, Salon.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Salon> listarSalonesActivos() {
        return salonRepository.listarSalonesActivos().stream()
                .map(entity -> salonMapper.map(entity, Salon.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Salon> buscarPorEdificio(String edificio) {
        return salonRepository.buscarPorEdificio(edificio).stream()
                .map(entity -> salonMapper.map(entity, Salon.class))
                .collect(Collectors.toList());
    }

}




