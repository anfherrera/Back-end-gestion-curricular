package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarConfiguracionSistemaCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarConfiguracionSistemaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Adaptador de caso de uso para gestión de configuración del sistema
 * 
 * @author Sistema de Gestión Curricular
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GestionarConfiguracionSistemaCUAdapter implements GestionarConfiguracionSistemaCUIntPort {
    
    private final GestionarConfiguracionSistemaGatewayIntPort configuracionGateway;
    
    @Override
    public Optional<String> obtenerPeriodoActivo() {
        return configuracionGateway.obtenerPeriodoActivo();
    }
    
    @Override
    public void establecerPeriodoActivo(String periodoAcademico) {
        // Validar que el período sea válido si se proporciona
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            String periodoTrimmed = periodoAcademico.trim();
            
            // Validar formato básico
            if (!periodoTrimmed.matches("\\d{4}-[12]")) {
                throw new IllegalArgumentException("Formato de período académico inválido. Debe ser YYYY-P (ej: 2025-1)");
            }
            
            // Validar que el período exista (en enum o BD)
            if (!PeriodoAcademicoEnum.esValido(periodoTrimmed)) {
                log.warn("Período {} no encontrado en enum, pero se establecerá de todas formas", periodoTrimmed);
            }
            
            configuracionGateway.establecerPeriodoActivo(periodoTrimmed);
            log.info("Período activo establecido: {}", periodoTrimmed);
        } else {
            // Establecer en modo automático
            configuracionGateway.establecerPeriodoActivo(null);
            log.info("Período activo establecido en modo automático (basado en fecha)");
        }
    }
}



