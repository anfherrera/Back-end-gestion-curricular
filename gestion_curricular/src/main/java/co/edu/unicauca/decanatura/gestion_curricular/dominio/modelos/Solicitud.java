package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

@Data
@AllArgsConstructor
public class Solicitud {

    
    private Integer idSolicitud;
    private String nombreSolicitud;
    private String periodoAcademico; // Período académico de la solicitud (ej: "2024-2")
    private Date fechaCeremonia; // Fecha de la ceremonia de graduación (para filtrar por ceremonias: marzo, junio, diciembre, etc.)
    private Date fechaRegistroSolicitud;

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
        return ultimo == null ? siNoHayEstados : ultimo.getEstadoActual();
    }

    /** true si algún registro del historial tiene un estado con texto. */
    public boolean tieneEstadoRegistrado() {
        return FlujoEstadosSolicitud.ultimoEstado(estadosSolicitud) != null;
    }

    /**
     * Agrega al historial el estado inicial (ENVIADA, con el texto que se guarda), con la fecha actual y el
     * vínculo a esta solicitud. Crea el historial si no hay lista.
     *
     * @return el registro agregado, para que se guarde
     */
    public EstadoSolicitud registrarEstadoInicial() {
        EstadoSolicitud inicial = new EstadoSolicitud();
        inicial.setEstadoActual(EstadosSolicitud.ENVIADA.textoGuardado());
        inicial.setFechaRegistroEstado(new Date());
        inicial.setObjSolicitud(this);
        if (estadosSolicitud == null) {
            estadosSolicitud = new ArrayList<>();
        }
        estadosSolicitud.add(inicial);
        return inicial;
    }

    /**
     * Prepara el paso de esta solicitud a {@code nuevoEstado} en {@code flujo}. Lo valida desde el último estado con
     * texto del historial y crea el registro del nuevo estado: su forma canónica, la fecha actual y el comentario
     * recortado, si trae texto. No lo agrega al historial; lo guarda el gateway.
     *
     * @throws IllegalArgumentException si el flujo no permite el paso
     *                                  (ver {@link FlujoEstadosSolicitud#validarYObtenerRolResponsable})
     */
    public FlujoEstadosSolicitud.Paso pasoA(FlujoEstadosSolicitud flujo, String nuevoEstado, String comentario) {
        String anterior = FlujoEstadosSolicitud.ultimoEstado(estadosSolicitud);
        String rolResponsable = flujo.validarYObtenerRolResponsable(anterior, nuevoEstado);
        EstadoSolicitud registro = new EstadoSolicitud();
        registro.setEstadoActual(flujo.normalizar(nuevoEstado));
        registro.setFechaRegistroEstado(new Date());
        if (comentario != null && !comentario.trim().isEmpty()) {
            registro.setComentario(comentario.trim());
        }
        return new FlujoEstadosSolicitud.Paso(anterior, rolResponsable, registro);
    }

}
