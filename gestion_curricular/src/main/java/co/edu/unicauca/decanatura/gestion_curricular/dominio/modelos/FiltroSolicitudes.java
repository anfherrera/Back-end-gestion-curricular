package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Optional;

/**
 * Qué solicitudes listar de un tipo (homologación, reingreso, paz y salvo, ECAES).
 *
 * <ul>
 *   <li>Estudiante: todas las suyas, de cualquier estado.</li>
 *   <li>Funcionario, Coordinador o Secretario: las que esperan su decisión ({@link #pendientesDe}) o las
 *       que ya aprobó alguna vez ({@link #procesadasPor}). El estado que corresponde a cada rol lo decide
 *       {@link FlujoEstadosSolicitud}.</li>
 * </ul>
 *
 * Un {@code idPrograma} o un {@code periodo} null no filtran.
 *
 * @param rol        {@link #ESTUDIANTE}, {@link FlujoEstadosSolicitud#FUNCIONARIO},
 *                   {@link FlujoEstadosSolicitud#COORDINADOR} o {@link FlujoEstadosSolicitud#SECRETARIO}
 * @param idPrograma programa del estudiante dueño de la solicitud
 * @param periodo    período académico de la solicitud ("2025-2")
 * @param idUsuario  dueño de la solicitud; solo para {@link #ESTUDIANTE}
 * @param procesadas true: las que el rol ya aprobó (historial); false: las que esperan su decisión
 */
public record FiltroSolicitudes(String rol, Integer idPrograma, String periodo, Integer idUsuario, boolean procesadas) {

    public static final String ESTUDIANTE = "Estudiante";

    /** Solicitudes que esperan la decisión de {@code rol}. */
    public static FiltroSolicitudes pendientesDe(String rol, Integer idPrograma, String periodo) {
        return new FiltroSolicitudes(rol, idPrograma, periodo, null, false);
    }

    /** Solicitudes que {@code rol} ya aprobó, aunque después hayan avanzado o se hayan rechazado. */
    public static FiltroSolicitudes procesadasPor(String rol, Integer idPrograma, String periodo) {
        return new FiltroSolicitudes(rol, idPrograma, periodo, null, true);
    }

    /** Solicitudes del estudiante {@code idUsuario}. */
    public static FiltroSolicitudes deEstudiante(Integer idUsuario, String periodo) {
        return new FiltroSolicitudes(ESTUDIANTE, null, periodo, idUsuario, false);
    }

    /**
     * Filtro de los listados "por rol" ({@code listarSolicitudesPorRol}), que reciben el rol en mayúsculas
     * y sin programa ni período: ESTUDIANTE, FUNCIONARIO, COORDINADOR o SECRETARIA.
     *
     * @return vacío para cualquier otro texto (el listado queda vacío)
     * @throws NullPointerException si {@code rol} es null, como el {@code switch} al que reemplaza
     */
    public static Optional<FiltroSolicitudes> porRol(String rol, Integer idUsuario) {
        return switch (rol) {
            case "ESTUDIANTE" -> Optional.of(deEstudiante(idUsuario, null));
            case "FUNCIONARIO" -> Optional.of(pendientesDe(FlujoEstadosSolicitud.FUNCIONARIO, null, null));
            case "COORDINADOR" -> Optional.of(pendientesDe(FlujoEstadosSolicitud.COORDINADOR, null, null));
            case "SECRETARIA" -> Optional.of(pendientesDe(FlujoEstadosSolicitud.SECRETARIO, null, null));
            default -> Optional.empty();
        };
    }

    public boolean esDeEstudiante() {
        return ESTUDIANTE.equals(rol);
    }
}
