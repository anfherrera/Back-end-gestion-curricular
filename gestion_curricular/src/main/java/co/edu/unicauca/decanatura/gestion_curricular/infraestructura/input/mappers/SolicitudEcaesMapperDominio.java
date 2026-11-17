package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.TipoDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.FechasEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.FechaEcaesDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudEcaesDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudEcaesMapperDominio {
   // DTO de petición → Dominio
    @Mapping(target = "estadosSolicitud", ignore = true) // Lo controlas desde el caso de uso
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) // Propiedad no mapeada
    @Mapping(target = "tipoDocumento", qualifiedByName = "tipoDocumentoEcaes")
    SolicitudEcaes mapearDeSolicitudEcaesDTOPeticionASolicitudEcaes(SolicitudEcaesDTOPeticion peticion);

    @Named("tipoDocumentoEcaes")
    default TipoDocumento tipoDocumentoEcaes(String codigo) {
        if (codigo == null) {
            return null;
        }
        if (codigo.equals("CC") || codigo.equals("CE")) {
            return TipoDocumento.valueOf(codigo);
        }

        // ECAES conserva el mensaje del enum anterior para los códigos que rechaza.
        throw new IllegalArgumentException("No enum constant co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumento." + codigo);
    }

    // Dominio → DTO Respuesta
    @Mapping(source = "id_solicitud", target = "id_solicitud")
    @Mapping(source = "nombre_solicitud", target = "nombre_solicitud")
    @Mapping(source = "periodo_academico", target = "periodo_academico")
    @Mapping(source = "fecha_ceremonia", target = "fecha_ceremonia")
    @Mapping(source = "fecha_registro_solicitud", target = "fecha_registro_solicitud")
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud")
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos")
    SolicitudEcaesDTORespuesta mapearDeSolicitudEcaesARespuesta(SolicitudEcaes solicitudEcaes);

    List<SolicitudEcaesDTORespuesta> mapearListaDeSolicitudEcaesARespuesta(List<SolicitudEcaes> solicitudes);

    //DTO de peticion  → fechas ecaes dominio    
    FechaEcaes mapearDeFechasEcaesDTOPeticionAFechaEcaes(FechasEcaesDTOPeticion fechasEcaes);

    // Fechas ecaes dominio → DTO respuesta
    FechaEcaesDTORespuesta mapearDeFechaEcaesAFechaEcaesDTORespuesta(FechaEcaes fechaEcaes);

    // Lista de fechas ecaes dominio → Lista de DTO respuesta
    List<FechaEcaesDTORespuesta> mapearListaDeFechaEcaesAFechaEcaesDTORespuesta(List<FechaEcaes> fechasEcaes);
    
   
}
