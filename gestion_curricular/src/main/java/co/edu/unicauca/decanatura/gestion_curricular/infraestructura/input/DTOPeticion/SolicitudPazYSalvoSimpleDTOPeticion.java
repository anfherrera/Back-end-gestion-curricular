package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO simplificado para pruebas de aceptación de Paz y Salvo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudPazYSalvoSimpleDTOPeticion {
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer idUsuario;
    
    private Integer idTipoProceso;
    
    private LocalDate fecha_solicitud;
}

