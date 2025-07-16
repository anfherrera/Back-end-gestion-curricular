package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;

@Configuration
public class MapperCursosOfertados {
 
        
    @Bean(name = "mapperCurso")
    public ModelMapper crearMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(EstadoCursoOfertadoEntity.class, EstadoCursoOfertado.class)
            .addMappings(mapper -> mapper.skip(EstadoCursoOfertado::setObjCursoOfertadoVerano));
            
            modelMapper.typeMap(CursoOfertadoVeranoEntity.class, CursoOfertadoVerano.class)
            .addMappings(mapper -> mapper.skip(CursoOfertadoVerano::setEstadosCursoOfertados))
            .setPostConverter(context -> {
                CursoOfertadoVeranoEntity source = context.getSource();
                CursoOfertadoVerano destination = context.getDestination();

                List<EstadoCursoOfertadoEntity> estadosCursoEntities = source.getEstadosCursoOfertados();

                // Mapear manualmente los estados
                List<EstadoCursoOfertado> estados = estadosCursoEntities
                    .stream()
                    .map(estadoEntity ->{
                    EstadoCursoOfertado estadoCurso = modelMapper.map(estadoEntity, EstadoCursoOfertado.class);
                    estadoCurso.setObjCursoOfertadoVerano(null);
                    return estadoCurso;
                    } )
                    .toList();

                destination.setEstadosCursoOfertados(estados);
                return destination;
            });

        // modelMapper.typeMap(CursoOfertadoVeranoEntity.class, CursoOfertadoVerano.class)
        //     .addMappings(mapper -> mapper.skip(CursoOfertadoVerano::setEstadosCursoOfertados));

        return modelMapper;
    }

}
