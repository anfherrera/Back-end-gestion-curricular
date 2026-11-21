package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Solicitud {

    
    private Integer id_solicitud;
    private String nombre_solicitud;
    private String periodo_academico; // Período académico de la solicitud (ej: "2024-2")
    private Date fecha_ceremonia; // Fecha de la ceremonia de graduación (para filtrar por ceremonias: marzo, junio, diciembre, etc.)
    private Date fecha_registro_solicitud;

    private List<EstadoSolicitud> estadosSolicitud;

    private Usuario objUsuario;

    private List<Documento> documentos;

    private CursoOfertadoVerano objCursoOfertadoVerano;

    public Solicitud () {
        this.documentos = new ArrayList<Documento>();
        this.estadosSolicitud = new ArrayList<EstadoSolicitud>();
    }

    /**
     * Último registro del historial de estados, o null si no hay ninguno.
     * A diferencia de {@link FlujoEstadosSolicitud#ultimoEstado}, no salta registros con estado vacío.
     */
    public EstadoSolicitud ultimoRegistroEstado() {
        if (estadosSolicitud == null || estadosSolicitud.isEmpty()) {
            return null;
        }
        return estadosSolicitud.get(estadosSolicitud.size() - 1);
    }

    /** Texto del último registro del historial de estados, o null si no hay ninguno. */
    public String estadoActual() {
        return estadoActual(null);
    }

    /** Como {@link #estadoActual()}, pero devuelve {@code siNoHayEstados} cuando el historial está vacío. */
    public String estadoActual(String siNoHayEstados) {
        EstadoSolicitud ultimo = ultimoRegistroEstado();
        return ultimo == null ? siNoHayEstados : ultimo.getEstado_actual();
    }

}
