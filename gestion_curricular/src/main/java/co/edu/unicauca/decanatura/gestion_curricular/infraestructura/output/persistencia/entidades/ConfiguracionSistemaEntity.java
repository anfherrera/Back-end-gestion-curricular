package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para configuración del sistema
 * Almacena configuraciones globales como el período académico activo
 * Solo debe existir una fila en esta tabla (singleton)
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@Entity
@Table(name = "ConfiguracionSistema")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionSistemaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Integer id_configuracion;
    
    /**
     * Período académico activo del sistema
     * Si es null, el sistema usa el período actual basado en la fecha
     * Si tiene un valor (ej: "2024-1"), el sistema usa ese período para todas las operaciones
     */
    @Column(name = "periodo_academico_activo", length = 10)
    private String periodo_academico_activo;
    
    /**
     * Nota: Esta tabla debe tener solo una fila
     * Se puede agregar más configuraciones aquí en el futuro
     */
}



