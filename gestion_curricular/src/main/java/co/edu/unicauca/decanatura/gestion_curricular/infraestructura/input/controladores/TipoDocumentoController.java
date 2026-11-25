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
    @GetMapping
    public ResponseEntity<ResultadoDTORespuesta<TipoDocumento.TipoDocumentoInfo[]>> obtenerTodosLosTipos() {
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            TipoDocumento.getTodosLosTipos(), "Tipos de documento obtenidos exitosamente"));
    }

    /**
     * Obtiene información detallada de un tipo de documento; 400 si el código no existe. Rutas según la
     * convención de EST-7 (paso 2): {@code GET /} y {@code GET /{codigo}}; {@code /validar} se retiró (nadie lo usaba).
     * El código tiene de 2 a 4 letras, así que {@code /todos} no se toma por un código.
     *
     * @param codigo El código del tipo de documento
     * @return Información detallada del tipo de documento
     */
    @GetMapping("/{codigo:[A-Za-z]{2,4}}")
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
