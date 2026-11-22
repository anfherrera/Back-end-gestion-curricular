package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.UsuarioDTOPeticion;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudMapperDominio {

    // Modelo → DTO Respuesta
    @Mapping(source = "idSolicitud", target = "id_solicitud")
    @Mapping(source = "nombreSolicitud", target = "nombre_solicitud")
    @Mapping(source = "periodoAcademico", target = "periodo_academico")
    @Mapping(source = "fechaCeremonia", target = "fecha_ceremonia")
    @Mapping(source = "fechaRegistroSolicitud", target = "fecha_registro_solicitud")
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud")
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos")
    SolicitudDTORespuesta mapearDeSolicitudARespuesta(Solicitud solicitud);

    List<SolicitudDTORespuesta> mapearListaDeSolicitudesARespuesta(List<Solicitud> solicitudes);

    // DTO Petición → Modelo
    @Mapping(target = "estadosSolicitud", ignore = true) // No viene en el DTO de petición
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) 
    @Mapping(target = "documentos", ignore = true)
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "idSolicitud", source = "id_solicitud")
    @Mapping(target = "nombreSolicitud", source = "nombre_solicitud")
    @Mapping(target = "periodoAcademico", source = "periodo_academico")
    @Mapping(target = "fechaCeremonia", source = "fecha_ceremonia")
    @Mapping(target = "fechaRegistroSolicitud", source = "fecha_registro_solicitud")
    Solicitud mapearDeSolicitudDTOPeticionASolicitud(SolicitudDTOPeticion peticion);

    CambioEstadoSolicitud mapearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(CambioEstadoSolicitudDTOPeticion solicitudPeticion);

    // OPC-2: el JSON conserva id_usuario, nombre_completo y estado_usuario
    @Mapping(target = "idUsuario", source = "id_usuario")
    @Mapping(target = "nombreCompleto", source = "nombre_completo")
    @Mapping(target = "estadoUsuario", source = "estado_usuario")
    Usuario usuarioDTOPeticionToUsuario(UsuarioDTOPeticion usuarioDTOPeticion);
}
