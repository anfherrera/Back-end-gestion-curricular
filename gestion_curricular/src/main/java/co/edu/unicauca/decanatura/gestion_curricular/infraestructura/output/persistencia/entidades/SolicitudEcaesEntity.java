package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Solicitudes_Ecaes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudEcaesEntity extends SolicitudEntity {

    @Convert(converter = TipoDocumentoEcaesConverter.class)
    @Column(nullable = false, columnDefinition = "enum ('CC','CE')")
    private TipoDocumento tipoDocumento;
    @Column(nullable = false, length = 100)
    private String numero_documento;
    @Column(nullable = false)
    private Date fecha_expedicion;
    @Column(nullable = false)
    private Date fecha_nacimiento;

    /** Persiste los códigos del enum SQL existente sin derivar el esquema del catálogo. */
    @Converter
    public static class TipoDocumentoEcaesConverter implements AttributeConverter<TipoDocumento, String> {
        @Override
        public String convertToDatabaseColumn(TipoDocumento tipo) {
            return tipo == null ? null : tipo.name();
        }

        @Override
        public TipoDocumento convertToEntityAttribute(String codigo) {
            return codigo == null ? null : TipoDocumento.valueOf(codigo);
        }
    }
}
