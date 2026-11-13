package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;

/** Regla de cupo de los cursos de verano: qué inscripciones ocupan un cupo del curso. */
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
}
