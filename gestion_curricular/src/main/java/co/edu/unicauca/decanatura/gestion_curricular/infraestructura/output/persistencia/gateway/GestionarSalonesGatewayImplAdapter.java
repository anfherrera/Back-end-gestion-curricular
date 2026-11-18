package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SalonEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.CatalogosMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SalonRepositoryInt;

@Service
@Transactional
public class GestionarSalonesGatewayImplAdapter implements GestionarSalonesIntPort {

    private final SalonRepositoryInt salonRepository;

    public GestionarSalonesGatewayImplAdapter(SalonRepositoryInt salonRepository) {
        this.salonRepository = salonRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Salon obtenerSalonPorId(Integer idSalon) {
        Optional<SalonEntity> salonEntity = salonRepository.findById(idSalon);
        if (salonEntity.isPresent()) {
            return CatalogosMapperPersistencia.aDominio(salonEntity.get());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Salon obtenerSalonPorNumero(String numeroSalon) {
        Optional<SalonEntity> salonEntity = salonRepository.buscarPorNumero(numeroSalon);
        if (salonEntity.isPresent()) {
            return CatalogosMapperPersistencia.aDominio(salonEntity.get());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Salon> listarSalonesActivos() {
        return salonRepository.listarSalonesActivos().stream()
                .map(entity -> CatalogosMapperPersistencia.aDominio(entity))
                .collect(Collectors.toList());
    }

}









