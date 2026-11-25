package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

/**
 * Flujo de estados de un curso de verano:
 * <pre>
 * Sin_Estado → Borrador ⇄ Abierto ⇄ Publicado ⇄ Preinscripcion ⇄ Inscripcion → Cerrado
 *                                              Preinscripcion → Cerrado
 * </pre>
 * Cerrado es final. Para pasar a Abierto el curso debe estar completo (materia, docente,
 * cupo y salón).
 */
public final class TransicionEstadoCurso {

    public static final String SIN_ESTADO = "Sin_Estado";

    /** Motivo por el que se rechaza una transición. */
    public static final class TransicionInvalidaException extends RuntimeException {
        private final String error;

        public TransicionInvalidaException(String error, String mensaje) {
            super(mensaje);
            this.error = error;
        }

        /** Título corto del motivo (p. ej. "Transicion invalida", "Curso incompleto"). */
        public String getError() {
            return error;
        }
    }

    public static boolean esEstadoValido(String estado) {
        return estado != null && switch (estado) {
            case "Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado" -> true;
            default -> false;
        };
    }

    private TransicionEstadoCurso() {
    }

    /** Estado más reciente del curso, o {@link #SIN_ESTADO} si no tiene. */
    public static String estadoActual(CursoOfertadoVerano curso) {
        EstadoCursoOfertado ultimo = curso.ultimoRegistroEstado();
        return ultimo == null ? SIN_ESTADO : ultimo.getEstadoActual();
    }

    /**
     * Verifica que el curso pueda pasar de su estado actual a {@code nuevoEstado}.
     *
     * @throws TransicionInvalidaException si la transición no está permitida
     */
    public static void validar(CursoOfertadoVerano curso, String nuevoEstado) {
        String actual = estadoActual(curso);
        if (actual == null) {
            // BUG-53: un último estado sin texto se trata como un estado desconocido
            throw estadoDesconocido(actual);
        }
        if (actual.equals(nuevoEstado)) {
            return; // Sin cambio
        }
        switch (actual) {
            case SIN_ESTADO -> exigir("Borrador".equals(nuevoEstado),
                    "Desde 'Sin Estado' solo se puede cambiar a 'Borrador'");
            case "Borrador" -> {
                exigir("Abierto".equals(nuevoEstado), "Desde 'Borrador' solo se puede cambiar a 'Abierto'");
                if (!curso.estaCompleto()) {
                    throw new TransicionInvalidaException("Curso incompleto",
                            "El curso debe tener materia, docente y cupo estimado para pasar a 'Abierto'");
                }
            }
            case "Abierto" -> exigir("Publicado".equals(nuevoEstado) || "Borrador".equals(nuevoEstado),
                    "Desde 'Abierto' solo se puede cambiar a 'Publicado' o retroceder a 'Borrador'");
            // Hoy no se exige un mínimo de solicitudes para abrir preinscripciones
            case "Publicado" -> exigir("Preinscripcion".equals(nuevoEstado) || "Abierto".equals(nuevoEstado),
                    "Desde 'Publicado' solo se puede cambiar a 'Preinscripcion' o retroceder a 'Abierto'");
            // Hoy no se exige un mínimo de preinscripciones aprobadas para abrir inscripciones
            case "Preinscripcion" -> exigir("Inscripcion".equals(nuevoEstado) || "Cerrado".equals(nuevoEstado)
                            || "Publicado".equals(nuevoEstado),
                    "Desde 'Preinscripcion' solo se puede cambiar a 'Inscripcion', 'Cerrado' o retroceder a 'Publicado'");
            case "Inscripcion" -> exigir("Cerrado".equals(nuevoEstado) || "Preinscripcion".equals(nuevoEstado),
                    "Desde 'Inscripcion' solo se puede cambiar a 'Cerrado' o retroceder a 'Preinscripcion'");
            case "Cerrado" -> throw new TransicionInvalidaException("Estado final",
                    "El curso esta cerrado y no se puede cambiar su estado");
            default -> throw estadoDesconocido(actual);
        }
    }

    private static TransicionInvalidaException estadoDesconocido(String actual) {
        return new TransicionInvalidaException("Estado desconocido", "Estado actual '" + actual + "' no reconocido");
    }

    private static void exigir(boolean permitido, String mensaje) {
        if (!permitido) {
            throw new TransicionInvalidaException("Transicion invalida", mensaje);
        }
    }
}
