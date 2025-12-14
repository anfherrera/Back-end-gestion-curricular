package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudReingresoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudReingresoDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudReingresoMapperDominio {

    @Mapping(target = "estadosSolicitud", ignore = true) // Lo controlas desde el caso de uso
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) // Propiedad no mapeada
    @Mapping(target = "fecha_ceremonia", ignore = true) // Se asigna desde el caso de uso si es necesario
    @Mapping(target = "objUsuario.cursosOfertadosInscritos", ignore = true)
    @Mapping(target = "objUsuario.objPrograma", ignore = true)
    @Mapping(target = "objUsuario.objRol", ignore = true)
    @Mapping(target = "objUsuario.solicitudes", ignore = true)
    SolicitudReingreso mappearDeSolicitudReingresoDTOPeticionASolicitudReingreso(SolicitudReingresoDTOPeticion peticion);

    SolicitudReingresoDTORespuesta mappearDeSolicitudReingresoASolicitudReingresoDTORespuesta(SolicitudReingreso solicitud);

    List<SolicitudReingresoDTORespuesta> mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(List<SolicitudReingreso> solicitudes);

    List<SolicitudReingreso> mappearDeListaSolicitudReingresoDTOPeticionASolicitudReingreso(List<SolicitudReingresoDTOPeticion> solicitudesDTO);
}
