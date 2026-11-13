package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.List;
import java.util.function.Supplier;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;

/**
 * Regla de cupo de los cursos de verano: qué inscripciones ocupan un cupo y cuándo el curso está lleno.
 * Quien la usa solo aporta el conteo (el gateway, además, bloquea el curso para contar sin carreras).
 */
public final class ReglasCupo {

    /**
     * Estados (último estado de la inscripción) que cuentan como cupo ocupado.
     * "APROBADA" y "Aprobada" son variantes antiguas que el código ya no escribe; se siguen aceptando al leer.
     */
    public static final List<String> ESTADOS_QUE_OCUPAN_CUPO = List.of(
            EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto(),
            EstadoSolicitudCursoVerano.INSCRIPCION_COMPLETADA.texto(),
            "APROBADA", "Aprobada",
            EstadoSolicitudCursoVerano.APROBADO.texto());

    private ReglasCupo() {
    }

    /** true si una inscripción cuyo último estado es {@code estado} ocupa un cupo. */
    public static boolean estadoOcupaCupo(String estado) {
        return estado != null && ESTADOS_QUE_OCUPAN_CUPO.contains(estado);
    }

    /** true si el último estado de la inscripción ya ocupa un cupo del curso. */
    public static boolean ocupaCupo(Solicitud inscripcion) {
        return estadoOcupaCupo(inscripcion.estadoActual());
    }

    /**
     * Exige que el curso tenga al menos un cupo libre. El cupo del curso es {@code cupo_estimado}.
     * Un curso sin cupo definido (null o &lt;= 0) no se limita, y entonces no se cuentan los ocupados.
     *
     * @param contarOcupados cuenta las inscripciones que ocupan cupo; un resultado null cuenta como 0
     * @throws CupoAgotadoException si los ocupados ya llegan al cupo
     */
    public static void exigirCupoLibre(Integer cupo, Supplier<Integer> contarOcupados) {
        if (cupo == null || cupo <= 0) {
            return;
        }
        Integer ocupados = contarOcupados.get();
        int n = ocupados != null ? ocupados : 0;
        if (n >= cupo) {
            throw new CupoAgotadoException(cupo, n);
        }
    }
}
