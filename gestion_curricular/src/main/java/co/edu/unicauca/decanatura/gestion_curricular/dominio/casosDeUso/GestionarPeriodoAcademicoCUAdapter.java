package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarPeriodoAcademicoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPeriodoAcademicoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarConfiguracionSistemaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PeriodoAcademico;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de caso de uso para gestión de períodos académicos
 * Implementa fallback al enum si no hay datos en BD
 * Prioriza período activo configurado por admin sobre fecha automática
 * 
 * @author Sistema de Gestión Curricular
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GestionarPeriodoAcademicoCUAdapter implements GestionarPeriodoAcademicoCUIntPort {
    
    private final GestionarPeriodoAcademicoGatewayIntPort periodoGateway;
    private final GestionarConfiguracionSistemaGatewayIntPort configuracionGateway;
    
    @Override
    public Optional<PeriodoAcademico> obtenerPeriodoActual() {
        // PRIORIDAD 1: Verificar si hay un período activo configurado por el admin
        Optional<String> periodoActivoConfig = configuracionGateway.obtenerPeriodoActivo();
        if (periodoActivoConfig.isPresent()) {
            String periodoValor = periodoActivoConfig.get();
            log.info("Usando período activo configurado por admin: {}", periodoValor);
            
            // Obtener el período desde BD o enum
            Optional<PeriodoAcademico> periodoConfig = periodoGateway.obtenerPeriodoPorValor(periodoValor);
            if (periodoConfig.isPresent()) {
                return periodoConfig;
            }
            
            // Fallback al enum si no está en BD
            try {
                PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.fromValor(periodoValor);
                log.warn("Período activo configurado {} no encontrado en BD, usando enum", periodoValor);
                return Optional.of(convertirEnumAPeriodo(periodoEnum));
            } catch (IllegalArgumentException e) {
                log.error("Período activo configurado {} no es válido, usando modo automático", periodoValor);
            }
        }
        
        // PRIORIDAD 2: Obtener período actual basado en fecha (modo automático)
        log.debug("No hay período activo configurado, usando modo automático (basado en fecha)");
        Optional<PeriodoAcademico> periodoBD = periodoGateway.obtenerPeriodoActual();
        if (periodoBD.isPresent()) {
            log.debug("Período académico obtenido desde BD por fecha: {}", periodoBD.get().getValor());
            return periodoBD;
        }
        
        // Fallback al enum
        log.debug("No se encontró período en BD, usando enum como fallback");
        PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.getPeriodoActual();
        if (periodoEnum != null) {
            return Optional.of(convertirEnumAPeriodo(periodoEnum));
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<PeriodoAcademico> obtenerPeriodoPorFecha(LocalDate fecha) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        
        // Intentar obtener desde BD
        Optional<PeriodoAcademico> periodoBD = periodoGateway.obtenerPeriodoPorFecha(fecha);
        if (periodoBD.isPresent()) {
            log.debug("Período académico para fecha {} obtenido desde BD: {}", fecha, periodoBD.get().getValor());
            return periodoBD;
        }
        
        // Fallback al enum
        log.debug("No se encontró período en BD para fecha {}, usando enum como fallback", fecha);
        PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.getPeriodoParaFecha(fecha);
        if (periodoEnum != null) {
            return Optional.of(convertirEnumAPeriodo(periodoEnum));
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<PeriodoAcademico> obtenerPeriodoPorValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Intentar obtener desde BD
        Optional<PeriodoAcademico> periodoBD = periodoGateway.obtenerPeriodoPorValor(valor.trim());
        if (periodoBD.isPresent()) {
            log.debug("Período académico {} obtenido desde BD", valor);
            return periodoBD;
        }
        
        // Fallback al enum
        log.debug("No se encontró período {} en BD, usando enum como fallback", valor);
        try {
            PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.fromValor(valor.trim());
            return Optional.of(convertirEnumAPeriodo(periodoEnum));
        } catch (IllegalArgumentException e) {
            log.warn("Período académico no válido: {}", valor);
            return Optional.empty();
        }
    }
    
    @Override
    public String obtenerPeriodoActualComoString() {
        Optional<PeriodoAcademico> periodo = obtenerPeriodoActual();
        return periodo.map(PeriodoAcademico::getValor).orElse(null);
    }
    
    @Override
    public List<PeriodoAcademico> listarPeriodosActivos() {
        List<PeriodoAcademico> periodos = periodoGateway.listarPeriodosActivos();
        if (!periodos.isEmpty()) {
            log.debug("Se obtuvieron {} períodos activos desde BD", periodos.size());
            return periodos;
        }
        
        // Fallback: generar períodos desde el enum
        log.debug("No hay períodos en BD, generando desde enum");
        return generarPeriodosDesdeEnum();
    }
    
    @Override
    public List<PeriodoAcademico> listarPeriodosFuturos() {
        List<PeriodoAcademico> periodos = periodoGateway.listarPeriodosFuturos();
        if (!periodos.isEmpty()) {
            return periodos;
        }
        
        // Fallback: generar períodos futuros desde el enum
        return generarPeriodosFuturosDesdeEnum();
    }
    
    @Override
    public List<PeriodoAcademico> listarPeriodosRecientes() {
        List<PeriodoAcademico> periodos = periodoGateway.listarPeriodosRecientes();
        if (!periodos.isEmpty()) {
            return periodos;
        }
        
        // Fallback: generar períodos recientes desde el enum
        return generarPeriodosRecientesDesdeEnum();
    }
    
    /**
     * Convierte un enum a modelo de dominio
     */
    private PeriodoAcademico convertirEnumAPeriodo(PeriodoAcademicoEnum periodoEnum) {
        PeriodoAcademico periodo = new PeriodoAcademico();
        periodo.setAño(periodoEnum.getAño());
        periodo.setNumero_periodo(periodoEnum.getNumeroPeriodo());
        periodo.setNombre_periodo(periodoEnum.getDescripcion());
        periodo.setFecha_inicio(periodoEnum.getFechaInicio());
        periodo.setFecha_fin(periodoEnum.getFechaFin());
        periodo.setActivo(true);
        periodo.setEs_periodo_especial(false);
        periodo.setTipo_periodo("REGULAR");
        return periodo;
    }
    
    /**
     * Genera períodos desde el enum
     */
    private List<PeriodoAcademico> generarPeriodosDesdeEnum() {
        return java.util.Arrays.stream(PeriodoAcademicoEnum.values())
                .map(this::convertirEnumAPeriodo)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Genera períodos futuros desde el enum
     */
    private List<PeriodoAcademico> generarPeriodosFuturosDesdeEnum() {
        int añoActual = LocalDate.now().getYear();
        return java.util.Arrays.stream(PeriodoAcademicoEnum.values())
                .filter(p -> p.getAño() >= añoActual)
                .map(this::convertirEnumAPeriodo)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Genera períodos recientes desde el enum
     */
    private List<PeriodoAcademico> generarPeriodosRecientesDesdeEnum() {
        int añoActual = LocalDate.now().getYear();
        int añoInicio = Math.max(2020, añoActual - 4);
        return java.util.Arrays.stream(PeriodoAcademicoEnum.values())
                .filter(p -> p.getAño() >= añoInicio)
                .map(this::convertirEnumAPeriodo)
                .collect(java.util.stream.Collectors.toList());
    }
}

