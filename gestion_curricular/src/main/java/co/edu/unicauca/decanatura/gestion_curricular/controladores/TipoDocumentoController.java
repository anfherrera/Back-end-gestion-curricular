package co.edu.unicauca.decanatura.gestion_curricular.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumentoEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar operaciones relacionadas con tipos de documento
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@RestController
@RequestMapping("/api/tipos-documento")
@CrossOrigin(origins = "*")
public class TipoDocumentoController {

    /**
     * Obtiene todos los tipos de documento disponibles
     * 
     * @return Lista de todos los tipos de documento
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, Object>> obtenerTodosLosTipos() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", TipoDocumentoEnum.getTodosLosTipos());
            response.put("message", "Tipos de documento obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener tipos de documento: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene solo los códigos de los tipos de documento
     * 
     * @return Lista de códigos de tipos de documento
     */
    @GetMapping("/codigos")
    public ResponseEntity<Map<String, Object>> obtenerCodigos() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", TipoDocumentoEnum.getTodosLosCodigos());
            response.put("message", "Códigos de tipos de documento obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener códigos: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Valida si un código de tipo de documento es válido
     * 
     * @param codigo El código a validar
     * @return true si es válido, false en caso contrario
     */
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<Map<String, Object>> validarTipoDocumento(@PathVariable String codigo) {
        try {
            boolean esValido = TipoDocumentoEnum.esValido(codigo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", esValido);
            response.put("message", esValido ? 
                "Tipo de documento válido" : 
                "Tipo de documento no válido");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al validar tipo de documento: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene información detallada de un tipo de documento
     * 
     * @param codigo El código del tipo de documento
     * @return Información detallada del tipo de documento
     */
    @GetMapping("/info/{codigo}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionTipoDocumento(@PathVariable String codigo) {
        try {
            if (!TipoDocumentoEnum.esValido(codigo)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Tipo de documento no válido: " + codigo);
                
                return ResponseEntity.badRequest().body(errorResponse);
            }

            TipoDocumentoEnum tipoEnum = TipoDocumentoEnum.fromCodigo(codigo);
            
            Map<String, Object> info = new HashMap<>();
            info.put("codigo", tipoEnum.getCodigo());
            info.put("descripcion", tipoEnum.getDescripcion());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", info);
            response.put("message", "Información del tipo de documento obtenida exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener información del tipo de documento: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}


