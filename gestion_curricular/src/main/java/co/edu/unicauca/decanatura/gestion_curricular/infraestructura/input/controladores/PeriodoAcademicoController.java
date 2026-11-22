package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ResultadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ResultadoDTORespuesta.Fallo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ResultadoDTORespuesta.PeriodoDetalle;

/**
 * Controlador para manejar operaciones relacionadas con períodos académicos
 *
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@RestController
@RequestMapping("/api/periodos-academicos")
public class PeriodoAcademicoController {

    /**
     * Rutas según la convención de EST-7 (paso 2): {@code GET /}, {@code GET /?desde=AAAA}, {@code GET /actual} y
     * {@code GET /{periodo}}. Los antiguos {@code /futuros} y {@code /recientes} son {@code ?desde=} con el año
     * actual y con {@code max(2020, año actual - 4)}; {@code /validar} y {@code /ultimo} se retiraron (nadie los
     * usaba; {@code /ultimo} era {@code GET /{período actual}}).
     *
     * @param desde opcional: solo los períodos de ese año en adelante
     */
    @GetMapping
    public ResponseEntity<ResultadoDTORespuesta<String[]>> obtenerPeriodos(
            @RequestParam(name = "desde", required = false) Integer desde) {
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            desde == null ? PeriodoAcademicoEnum.getTodosLosValores() : PeriodoAcademicoEnum.desde(desde),
            "Períodos académicos obtenidos exitosamente"));
    }

    /**
     * Obtiene el período académico actual
     *
     * @return El período académico actual
     */
    @GetMapping("/actual")
    public ResponseEntity<ResultadoDTORespuesta<String>> obtenerPeriodoActual() {
        PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
        // Sin período conocido se responde success=true con data null, como antes.
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            periodoActual != null ? periodoActual.getValor() : null,
            periodoActual != null ?
                "Período actual obtenido exitosamente" :
                "No se pudo determinar el período actual"));
    }

    /**
     * Obtiene información detallada de un período académico; 400 si no está en el catálogo
     *
     * @param periodo El período del cual obtener información ("2025-2")
     * @return Información detallada del período
     */
    @GetMapping("/{periodo:\\d{4}-\\d+}")
    public ResponseEntity<?> obtenerInformacionPeriodo(@PathVariable String periodo) {
        if (!PeriodoAcademicoEnum.esValido(periodo)) {
            return ResponseEntity.badRequest().body(new Fallo("Período académico no válido: " + periodo));
        }

        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            detalle(PeriodoAcademicoEnum.fromValor(periodo)),
            "Información del período obtenida exitosamente"));
    }

    private static PeriodoDetalle detalle(PeriodoAcademicoEnum periodo) {
        return new PeriodoDetalle(periodo.getValor(), periodo.getAño(),
            periodo.getNumeroPeriodo(), periodo.getDescripcion());
    }
}
