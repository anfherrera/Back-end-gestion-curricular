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
     * Obtiene todos los períodos académicos disponibles
     *
     * @return Lista de todos los períodos académicos
     */
    @GetMapping("/todos")
    public ResponseEntity<ResultadoDTORespuesta<String[]>> obtenerTodosLosPeriodos() {
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            PeriodoAcademicoEnum.getTodosLosValores(), "Períodos académicos obtenidos exitosamente"));
    }

    /**
     * Obtiene los períodos académicos futuros (año actual en adelante)
     *
     * @return Lista de períodos académicos futuros
     */
    @GetMapping("/futuros")
    public ResponseEntity<ResultadoDTORespuesta<String[]>> obtenerPeriodosFuturos() {
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            PeriodoAcademicoEnum.getPeriodosFuturos(), "Períodos académicos futuros obtenidos exitosamente"));
    }

    /**
     * Obtiene los períodos académicos recientes (últimos 5 años)
     *
     * @return Lista de períodos académicos recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<ResultadoDTORespuesta<String[]>> obtenerPeriodosRecientes() {
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            PeriodoAcademicoEnum.getPeriodosRecientes(), "Períodos académicos recientes obtenidos exitosamente"));
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
     * Valida si un período académico es válido
     *
     * @param periodo El período a validar
     * @return true si es válido, false en caso contrario
     */
    @GetMapping("/validar/{periodo}")
    public ResponseEntity<ResultadoDTORespuesta<Boolean>> validarPeriodo(@PathVariable String periodo) {
        boolean esValido = PeriodoAcademicoEnum.esValido(periodo);
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(esValido, esValido ?
            "Período válido" :
            "Período no válido"));
    }

    /**
     * Obtiene información detallada de un período académico
     *
     * @param periodo El período del cual obtener información
     * @return Información detallada del período
     */
    @GetMapping("/info/{periodo}")
    public ResponseEntity<?> obtenerInformacionPeriodo(@PathVariable String periodo) {
        if (!PeriodoAcademicoEnum.esValido(periodo)) {
            return ResponseEntity.badRequest().body(new Fallo("Período académico no válido: " + periodo));
        }

        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            detalle(PeriodoAcademicoEnum.fromValor(periodo)),
            "Información del período obtenida exitosamente"));
    }

    /**
     * Obtiene el último período académico (más reciente)
     * Basado en el período actual calculado por fecha
     *
     * @return El último período académico
     */
    @GetMapping("/ultimo")
    public ResponseEntity<ResultadoDTORespuesta<PeriodoDetalle>> obtenerUltimoPeriodo() {
        PeriodoAcademicoEnum ultimoPeriodo = PeriodoAcademicoEnum.getPeriodoActual();
        if (ultimoPeriodo == null) {
            // A diferencia de /info, el fallo conserva la clave data con null y responde 200.
            return ResponseEntity.ok(new ResultadoDTORespuesta<>(null, false,
                "No se pudo determinar el último período académico"));
        }
        return ResponseEntity.ok(ResultadoDTORespuesta.exito(
            detalle(ultimoPeriodo), "Último período académico obtenido exitosamente"));
    }

    private static PeriodoDetalle detalle(PeriodoAcademicoEnum periodo) {
        return new PeriodoDetalle(periodo.getValor(), periodo.getAño(),
            periodo.getNumeroPeriodo(), periodo.getDescripcion());
    }
}
