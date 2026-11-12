package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;

@Mapper(componentModel = "spring")
public interface DocumentosMapperDominio {

    /* ======== Dominio  →  DTO de respuesta ======== */
    @Mapping(source = "id_documento",                   target = "id_documento")
    @Mapping(source = "nombre",                         target = "nombre")
    @Mapping(source = "ruta_documento",                 target = "ruta_documento")
    @Mapping(source = "fecha_documento",                target = "fecha_documento")
    @Mapping(source = "esValido",                       target = "esValido")
    @Mapping(source = "comentario",                     target = "comentario")
    DocumentosDTORespuesta mappearDeDocumentoADTORespuesta(Documento documento);

}
