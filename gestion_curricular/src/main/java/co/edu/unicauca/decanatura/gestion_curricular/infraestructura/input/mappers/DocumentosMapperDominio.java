package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.DocumentosDTOPeticion;

@Mapper(componentModel = "spring")
public interface DocumentosMapperDominio {

    /* ======== Dominio  →  DTO de respuesta ======== */
    @Mapping(source = "id_documento",                   target = "id_documento")
    @Mapping(source = "nombre",                         target = "nombre")
    @Mapping(source = "ruta_documento",                 target = "ruta_documento")
    @Mapping(source = "fecha_documento",                target = "fecha_documento")
    @Mapping(source = "esValido",                       target = "esValido")
    @Mapping(source = "comentario",                     target = "comentario")
    @Mapping(source = "tipoDocumentoSolicitudPazYSalvo",target = "tipoDocumentoSolicitudPazYSalvo")
    DocumentosDTORespuesta mappearDeDocumentoADTORespuesta(Documento documento);

    List<DocumentosDTORespuesta> mappearListaDeDocumentoADTORespuesta(List<Documento> documentos);

    /* ======== DTO de petición  →  Dominio ======== */
    @Mapping(source = "id_documento",                   target = "id_documento")
    @Mapping(source = "nombre",                         target = "nombre")
    @Mapping(source = "ruta_documento",                 target = "ruta_documento")
    @Mapping(source = "fecha_documento",                target = "fecha_documento")
    @Mapping(source = "esValido",                       target = "esValido")
    @Mapping(source = "comentario",                     target = "comentario")
    /* Si tu dominio usa un enum (TipoDocumentoSolicitudPazYSalvo) y
       en el DTO recibes un String, necesitarás lógica adicional o
       un mapper específico; por ahora lo ignoramos: */
    @Mapping(target = "tipoDocumentoSolicitudPazYSalvo", ignore = true)
    @Mapping(target = "objSolicitud", ignore = true) // Ignoramos el usuario, si no es necesario
    /* -------------  */
    Documento mappearDeDTOPeticionADocumento(DocumentosDTOPeticion peticion);

    List<Documento> mappearListaDeDTOPeticionADocumento(List<DocumentosDTOPeticion> peticiones);
}
