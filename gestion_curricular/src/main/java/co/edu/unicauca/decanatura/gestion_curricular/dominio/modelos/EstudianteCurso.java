package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;
public record EstudianteCurso(Usuario usuario, SolicitudCursoVeranoPreinscripcion preinscripcion,
        SolicitudCursoVeranoInscripcion inscripcion, String estadoPreinscripcion, String estadoInscripcion) {
    public boolean inscrito() { return EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoInscripcion); }
}
