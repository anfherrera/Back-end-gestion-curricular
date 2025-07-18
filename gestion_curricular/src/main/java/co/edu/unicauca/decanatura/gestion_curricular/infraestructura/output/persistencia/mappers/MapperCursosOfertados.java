package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

            modelMapper.typeMap(CursoOfertadoVerano.class, CursoOfertadoVeranoEntity.class)
            .addMappings(mapper -> mapper.skip(CursoOfertadoVeranoEntity::setEstudiantesInscritos))
            .addMappings(mapper -> mapper.skip(CursoOfertadoVeranoEntity::setEstadosCursoOfertados))
            .setPostConverter(context -> {
                CursoOfertadoVerano source = context.getSource();
                CursoOfertadoVeranoEntity destination = context.getDestination();

                if (source == null) {
                    return destination;
                }

                // Mapeo manual de usuarios SIN usar modelMapper.map
                List<UsuarioEntity> estudiantesInscritos = new ArrayList<>();
                if (source.getEstudiantesInscritos() != null) {
                    for (Usuario usuario : source.getEstudiantesInscritos()) {
                        UsuarioEntity usuarioEntity = new UsuarioEntity();
                        usuarioEntity.setId_usuario(usuario.getId_usuario());
                        usuarioEntity.setNombre_completo(usuario.getNombre_completo());
                        usuarioEntity.setCorreo(usuario.getCorreo());
                        usuarioEntity.setCodigo(usuario.getCodigo());
                        usuarioEntity.setEstado_usuario(usuario.isEstado_usuario());
                        usuarioEntity.setPassword(usuario.getPassword());
                        usuarioEntity.setSolicitudes(new ArrayList<>());
                        usuarioEntity.setCursosOfertadosInscritos(new ArrayList<>()); // evita ciclo

                        estudiantesInscritos.add(usuarioEntity);
                    }
                }
                destination.setEstudiantesInscritos(estudiantesInscritos);

                // Mapeo manual de estados SIN usar modelMapper.map
                List<EstadoCursoOfertadoEntity> estadosCurso = new ArrayList<>();
                if (source.getEstadosCursoOfertados() != null) {
                    for (EstadoCursoOfertado estado : source.getEstadosCursoOfertados()) {
                        EstadoCursoOfertadoEntity estadoEntity = new EstadoCursoOfertadoEntity();
                        estadoEntity.setId_estado(estado.getId_estado());
                        estadoEntity.setEstado_actual(estado.getEstado_actual());
                        estadoEntity.setFecha_registro_estado(estado.getFecha_registro_estado());
                        estadoEntity.setObjCursoOfertadoVerano(null); // corta la recursividad

                        estadosCurso.add(estadoEntity);
                    }
                }
                destination.setEstadosCursoOfertados(estadosCurso);

                return destination;
            });

            modelMapper.typeMap(EstadoCursoOfertado.class, EstadoCursoOfertadoEntity.class)
            .addMappings(mapper -> mapper.skip(EstadoCursoOfertadoEntity::setObjCursoOfertadoVerano));

            modelMapper.typeMap(EstadoCursoOfertadoEntity.class, EstadoCursoOfertado.class)
            .addMappings(mapper -> mapper.skip(EstadoCursoOfertado::setObjCursoOfertadoVerano));


            modelMapper.typeMap(UsuarioEntity.class, Usuario.class)
            .addMappings(mapper -> mapper.skip(Usuario::setCursosOfertadosInscritos));
            modelMapper.typeMap(Usuario.class, UsuarioEntity.class)
            .addMappings(mapper -> mapper.skip(UsuarioEntity::setCursosOfertadosInscritos));

            modelMapper.typeMap(Solicitud.class, SolicitudEntity.class)
                .addMappings(mapper -> mapper.skip(SolicitudEntity::setObjCursoOfertadoVerano))
                .addMappings(mapper -> mapper.skip(SolicitudEntity::setObjUsuario));
            modelMapper.typeMap(SolicitudEntity.class, Solicitud.class)
                .addMappings(mapper -> mapper.skip(Solicitud::setObjCursoOfertadoVerano))
                .addMappings(mapper -> mapper.skip(Solicitud::setObjUsuario));

            modelMapper.getConfiguration()
            .setAmbiguityIgnored(true)
            .setFieldMatchingEnabled(true) // permite matching por nombre de campos, no solo getters/setters
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

            modelMapper.typeMap(CursoOfertadoVeranoEntity.class, CursoOfertadoVerano.class)
            .addMappings(mapper -> mapper.skip(CursoOfertadoVerano::setEstadosCursoOfertados))
            .addMappings(mapper -> mapper.skip(CursoOfertadoVerano::setEstudiantesInscritos))
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


                List<UsuarioEntity> estudiantes = source.getEstudiantesInscritos();

                List<Usuario> usuarios = estudiantes
                    .stream()
                    .map(estudiante ->{
                    Usuario usuario = modelMapper.map(estudiante, Usuario.class);
                    usuario.getCursosOfertadosInscritos().clear();
                    return usuario;
                    } )
                    .toList();
                destination.setEstudiantesInscritos(usuarios);

                return destination;
            });

        return modelMapper;
    }

}