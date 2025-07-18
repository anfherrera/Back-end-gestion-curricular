package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

@Configuration
public class MapperCursosOfertados {
 
        
    @Bean(name = "mapperCurso")
    public ModelMapper crearMapper() {
        ModelMapper modelMapper = new ModelMapper();

            modelMapper.typeMap(CursoOfertadoVeranoEntity.class, CursoOfertadoVerano.class)
            .addMappings(mapper -> mapper.skip(CursoOfertadoVerano::setEstudiantesInscritos))
            .setPostConverter(context -> {
                CursoOfertadoVeranoEntity source = context.getSource();
                CursoOfertadoVerano destination = context.getDestination();

                    if (source == null || source.getEstadosCursoOfertados() == null) {
                        destination.setEstadosCursoOfertados(Collections.emptyList());
                        return destination;
                    }

                Set<UsuarioEntity> estudiantes = source.getEstudiantesInscritos();

                Set<Usuario> usuarios = new HashSet<Usuario>( estudiantes
                    .stream()
                    .map(estudiante ->{
                    Usuario usuario = modelMapper.map(estudiante, Usuario.class);
                    usuario.getCursosOfertadosInscritos().clear();
                    return usuario;
                    } )
                    .toList());
                destination.setEstudiantesInscritos(usuarios);
                return destination;
            });

            modelMapper.typeMap(UsuarioEntity.class, Usuario.class)
            .addMappings(mmapper -> mmapper.skip(Usuario::setCursosOfertadosInscritos));

            modelMapper.typeMap(SolicitudEntity.class, Solicitud.class)
            .addMappings(mmapper -> mmapper.skip(Solicitud::setObjUsuario))
            .addMappings(mmapper -> mmapper.skip(Solicitud::setObjCursoOfertadoVerano));

            modelMapper.getConfiguration()
            .setAmbiguityIgnored(true)
            .setFieldMatchingEnabled(true) // permite matching por nombre de campos, no solo getters/setters
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

            modelMapper.typeMap(CursoOfertadoVeranoEntity.class, CursoOfertadoVerano.class)
            .addMappings(mapper -> mapper.skip(CursoOfertadoVerano::setEstadosCursoOfertados))
            .setPostConverter(context -> {
                CursoOfertadoVeranoEntity source = context.getSource();
                CursoOfertadoVerano destination = context.getDestination();

                    if (source == null || source.getEstadosCursoOfertados() == null) {
                        destination.setEstadosCursoOfertados(Collections.emptyList());
                        return destination;
                    }

                List<EstadoCursoOfertadoEntity> estadosCursoEntities = source.getEstadosCursoOfertados();

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

        return modelMapper;
    }

}