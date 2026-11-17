package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.TipoDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ResultadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ResultadoDTORespuesta.Fallo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ResultadoDTORespuesta.TipoDocumentoDetalle;

/**
 * Controlador para manejar operaciones relacionadas con tipos de documento
 *
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@RestController
@RequestMapping("/api/tipos-documento")
public class TipoDocumentoController {

    /**
     * Obtiene todos los tipos de documento disponibles
     *
     * @return Lista de todos los tipos de documento
     */
    @GetMapping("/todos")
    public ResponseEntity<ResultadoDTORespuesta<TipoDocumento.TipoDocumentoInfo[]>> obtenerTodosLosTipos() {
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            TipoDocumento.getTodosLosTipos(), "Tipos de documento obtenidos exitosamente"));
    }

    /**
     * Valida si un código de tipo de documento es válido
     *
     * @param codigo El código a validar
     * @return true si es válido, false en caso contrario
     */
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<ResultadoDTORespuesta<Boolean>> validarTipoDocumento(@PathVariable String codigo) {
        boolean esValido = TipoDocumento.esValido(codigo);
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(esValido, esValido ?
            "Tipo de documento válido" :
            "Tipo de documento no válido"));
    }

    /**
     * Obtiene información detallada de un tipo de documento
     *
     * @param codigo El código del tipo de documento
     * @return Información detallada del tipo de documento
     */
    @GetMapping("/info/{codigo}")
    public ResponseEntity<?> obtenerInformacionTipoDocumento(@PathVariable String codigo) {
        if (!TipoDocumento.esValido(codigo)) {
            return ResponseEntity.badRequest().body(new Fallo("Tipo de documento no válido: " + codigo));
        }

        TipoDocumento tipoEnum = TipoDocumento.fromCodigo(codigo);
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            new TipoDocumentoDetalle(tipoEnum.getCodigo(), tipoEnum.getDescripcion()),
            "Información del tipo de documento obtenida exitosamente"));
    }
}
