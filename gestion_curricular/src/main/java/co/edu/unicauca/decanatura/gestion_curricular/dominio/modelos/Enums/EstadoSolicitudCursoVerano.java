package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums;

/**
 * Estados que se guardan en las preinscripciones e inscripciones de cursos de verano.
 * {@link #texto()} es el valor exacto de {@code estado_actual} en la base de datos y en el JSON.
 *
 * <pre>
 * Preinscripción: Enviada ──▶ Aprobado | Rechazado
 * Inscripción:    Enviada ──▶ Pago_Validado ──▶ Inscripcion_Completada
 *                         └─▶ Pago_Rechazado
 * </pre>
 *
 * <p>Las etiquetas "Inscrito", "Preinscrito", "Pendiente" y "Enviado" que devuelven algunos
 * controladores no son estados guardados: son textos por defecto cuando la solicitud no tiene estados.
 */
public enum EstadoSolicitudCursoVerano {
    ENVIADA("Enviada"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    PAGO_VALIDADO("Pago_Validado"),
    PAGO_RECHAZADO("Pago_Rechazado"),
    INSCRIPCION_COMPLETADA("Inscripcion_Completada");

    private final String texto;

    EstadoSolicitudCursoVerano(String texto) {
        this.texto = texto;
    }

    /** Texto con el que este estado se guarda y se devuelve. */
    public String texto() {
        return texto;
    }
}
