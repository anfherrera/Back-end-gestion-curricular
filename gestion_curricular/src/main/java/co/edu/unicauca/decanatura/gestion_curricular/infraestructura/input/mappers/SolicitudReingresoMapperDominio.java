package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudReingresoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudReingresoDTORespuesta;

@Mapper(
    componentModel = "spring",
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
    @Mapping(target = "objUsuario.objRol", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.objPrograma", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.solicitudes", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.cursosOfertadosInscritos", ignore = true) // Propiedad no mapeada en Usuario
    // OPC-2: el JSON conserva id_usuario, nombre_completo y estado_usuario
    @Mapping(target = "objUsuario.idUsuario", source = "objUsuario.id_usuario")
    @Mapping(target = "objUsuario.nombreCompleto", source = "objUsuario.nombre_completo")
    @Mapping(target = "objUsuario.estadoUsuario", source = "objUsuario.estado_usuario")
    SolicitudReingreso mapearDeSolicitudReingresoDTOPeticionASolicitudReingreso(SolicitudReingresoDTOPeticion peticion);

    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "tipo_solicitud", ignore = true)
    SolicitudReingresoDTORespuesta mapearDeSolicitudReingresoASolicitudReingresoDTORespuesta(SolicitudReingreso solicitud);

    List<SolicitudReingresoDTORespuesta> mapearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(List<SolicitudReingreso> solicitudes);
}
