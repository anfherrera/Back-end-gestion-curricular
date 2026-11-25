package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosDTORespuesta;

@Mapper(componentModel = "spring")
public interface DocumentosMapperDominio {

    /* ======== Dominio  →  DTO de respuesta ======== */
    @Mapping(source = "idDocumento",                   target = "id_documento")
    @Mapping(source = "nombre",                         target = "nombre")
    @Mapping(source = "rutaDocumento",                 target = "ruta_documento")
    @Mapping(source = "fechaDocumento",                target = "fecha_documento")
    @Mapping(source = "esValido",                       target = "esValido")
    @Mapping(source = "comentario",                     target = "comentario")
    DocumentosDTORespuesta mapearDeDocumentoADTORespuesta(Documento documento);

}
