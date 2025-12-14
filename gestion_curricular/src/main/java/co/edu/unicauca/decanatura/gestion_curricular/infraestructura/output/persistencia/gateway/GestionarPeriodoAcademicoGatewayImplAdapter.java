package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPeriodoAcademicoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PeriodoAcademico;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.PeriodoAcademicoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.PeriodoAcademicoRepositoryInt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de implementación del gateway para períodos académicos
 * 
 * @author Sistema de Gestión Curricular
 */
@Service
@RequiredArgsConstructor
public class GestionarPeriodoAcademicoGatewayImplAdapter implements GestionarPeriodoAcademicoGatewayIntPort {
    
    private final PeriodoAcademicoRepositoryInt periodoRepository;
    
    @Override
    public Optional<PeriodoAcademico> obtenerPeriodoActual() {
        return periodoRepository.findPeriodoActual()
                .map(this::entityToDomain);
    }
    
    @Override
    public Optional<PeriodoAcademico> obtenerPeriodoPorFecha(LocalDate fecha) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        return periodoRepository.findPeriodoActivoPorFecha(fecha)
                .map(this::entityToDomain);
    }
    
    @Override
    public Optional<PeriodoAcademico> obtenerPeriodoPorValor(String valor) {
        return periodoRepository.findByValor(valor)
                .map(this::entityToDomain);
    }
    
    @Override
    public List<PeriodoAcademico> listarPeriodosActivos() {
        return periodoRepository.findPeriodosRegularesActivos().stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PeriodoAcademico> listarPeriodosFuturos() {
        int añoActual = LocalDate.now().getYear();
        return periodoRepository.findPeriodosFuturos(añoActual).stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PeriodoAcademico> listarPeriodosRecientes() {
        int añoActual = LocalDate.now().getYear();
        int añoInicio = Math.max(2020, añoActual - 4);
        return periodoRepository.findPeriodosRecientes(añoInicio).stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public PeriodoAcademico guardar(PeriodoAcademico periodo) {
        PeriodoAcademicoEntity entity = domainToEntity(periodo);
        PeriodoAcademicoEntity saved = periodoRepository.save(entity);
        return entityToDomain(saved);
    }
    
    /**
     * Convierte una entidad a modelo de dominio
     */
    private PeriodoAcademico entityToDomain(PeriodoAcademicoEntity entity) {
        PeriodoAcademico periodo = new PeriodoAcademico();
        periodo.setId_periodo(entity.getId_periodo());
        periodo.setAño(entity.getAño());
        periodo.setNumero_periodo(entity.getNumero_periodo());
        periodo.setNombre_periodo(entity.getNombre_periodo());
        periodo.setFecha_inicio(entity.getFecha_inicio());
        periodo.setFecha_fin(entity.getFecha_fin());
        periodo.setFecha_inicio_clases(entity.getFecha_inicio_clases());
        periodo.setFecha_fin_clases(entity.getFecha_fin_clases());
        periodo.setActivo(entity.getActivo());
        periodo.setEs_periodo_especial(entity.getEs_periodo_especial());
        periodo.setTipo_periodo(entity.getTipo_periodo());
        return periodo;
    }
    
    /**
     * Convierte un modelo de dominio a entidad
     */
    private PeriodoAcademicoEntity domainToEntity(PeriodoAcademico periodo) {
        PeriodoAcademicoEntity entity = new PeriodoAcademicoEntity();
        entity.setId_periodo(periodo.getId_periodo());
        entity.setAño(periodo.getAño());
        entity.setNumero_periodo(periodo.getNumero_periodo());
        entity.setNombre_periodo(periodo.getNombre_periodo());
        entity.setFecha_inicio(periodo.getFecha_inicio());
        entity.setFecha_fin(periodo.getFecha_fin());
        entity.setFecha_inicio_clases(periodo.getFecha_inicio_clases());
        entity.setFecha_fin_clases(periodo.getFecha_fin_clases());
        entity.setActivo(periodo.getActivo());
        entity.setEs_periodo_especial(periodo.getEs_periodo_especial());
        entity.setTipo_periodo(periodo.getTipo_periodo());
        return entity;
    }
}


