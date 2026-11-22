package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosDTORespuesta;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        CursosOfertadosMapperDominio.class
    }
)

public interface SolicitudCursoDeVeranoPreinscripcionMapperDominio {
    @Mapping(target = "estadosSolicitud", source = "estadosSolicitud")
    @Mapping(target = "objUsuario", source = "objUsuario")
    @Mapping(target = "objCursoOfertado", source = "objCursoOfertadoVerano")
    @Mapping(target = "codicion_solicitud", source = "codicion_solicitud")
    @Mapping(target = "nombre_estudiante", source = "nombre_estudiante")
    @Mapping(target = "observacion", source = "observacion")
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "id_solicitud", source = "idSolicitud")
    @Mapping(target = "nombre_solicitud", source = "nombreSolicitud")
    @Mapping(target = "periodo_academico", source = "periodoAcademico")
    @Mapping(target = "fecha_ceremonia", source = "fechaCeremonia")
    @Mapping(target = "fecha_registro_solicitud", source = "fechaRegistroSolicitud")
    SolicitudCursoVeranoPreinscripcionDTORespuesta mapearDeSolicitudCursoVeranoPreinscripcionARespuesta(
        SolicitudCursoVeranoPreinscripcion solicitud);

    // OPC-2: el DTO conserva id_documento, ruta_documento y fecha_documento
    @Mapping(target = "id_documento", source = "idDocumento")
    @Mapping(target = "ruta_documento", source = "rutaDocumento")
    @Mapping(target = "fecha_documento", source = "fechaDocumento")
    DocumentosDTORespuesta documentoToDocumentosDTORespuesta(Documento documento);
}
