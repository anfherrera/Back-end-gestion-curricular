package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;

public interface GestionarPreRegistroEcaesGatewayIntPort {

    SolicitudEcaes guardar(SolicitudEcaesEntity solicitud);

    List<SolicitudEcaes> listar();

    Optional<SolicitudEcaes> buscarPorId(Integer id);

}
