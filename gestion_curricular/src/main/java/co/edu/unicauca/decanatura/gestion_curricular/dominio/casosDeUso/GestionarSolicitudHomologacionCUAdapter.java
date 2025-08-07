package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;

public class GestionarSolicitudHomologacionCUAdapter implements GestionarSolicitudHomologacionCUIntPort {

    private final FormateadorResultadosIntPort objFormateadorResultados;
    
    public GestionarSolicitudHomologacionCUAdapter(FormateadorResultadosIntPort formateadorResultados) {
        this.objFormateadorResultados = formateadorResultados;
    }
    @Override
    public SolicitudHomologacion guardar(SolicitudHomologacion solicitud) {
       if(solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Solicitud de homologación no puede ser nula");
        }
        //return null para poder cambiar de rama, eliminar y poner logica
        return null;
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarSolicitudes'");
    }

    @Override
    public SolicitudHomologacion buscarPorId(Integer idSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cambiarEstadoSolicitud'");
    }

}
