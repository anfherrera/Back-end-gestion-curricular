package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;

public interface GestionarEstadoCursoOfertadoGatewayIntPort {
    EstadoCursoOfertado buscarPorIEstadoCursoOfertado(Integer idEstadoCursoOfertado);
}
