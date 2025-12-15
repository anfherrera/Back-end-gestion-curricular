package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarConfiguracionSistemaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ConfiguracionSistemaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ConfiguracionSistemaRepositoryInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de implementación del gateway para configuración del sistema
 * 
 * @author Sistema de Gestión Curricular
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GestionarConfiguracionSistemaGatewayImplAdapter implements GestionarConfiguracionSistemaGatewayIntPort {
    
    private final ConfiguracionSistemaRepositoryInt configuracionRepository;
    
    @Override
    public Optional<String> obtenerPeriodoActivo() {
        Optional<ConfiguracionSistemaEntity> configOpt = configuracionRepository.findFirst();
        if (configOpt.isPresent()) {
            String periodo = configOpt.get().getPeriodo_academico_activo();
            if (periodo != null && !periodo.trim().isEmpty()) {
                log.debug("Período activo configurado: {}", periodo);
                return Optional.of(periodo.trim());
            }
        }
        log.debug("No hay período activo configurado, usando modo automático");
        return Optional.empty();
    }
    
    @Override
    @Transactional
    public void establecerPeriodoActivo(String periodoAcademico) {
        Optional<ConfiguracionSistemaEntity> configOpt = configuracionRepository.findFirst();
        
        ConfiguracionSistemaEntity config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
        } else {
            // Crear nueva configuración si no existe
            config = new ConfiguracionSistemaEntity();
        }
        
        config.setPeriodo_academico_activo(periodoAcademico != null ? periodoAcademico.trim() : null);
        configuracionRepository.save(config);
        
        if (periodoAcademico != null) {
            log.info("Período activo establecido manualmente: {}", periodoAcademico);
        } else {
            log.info("Período activo establecido en modo automático (basado en fecha)");
        }
    }
}



