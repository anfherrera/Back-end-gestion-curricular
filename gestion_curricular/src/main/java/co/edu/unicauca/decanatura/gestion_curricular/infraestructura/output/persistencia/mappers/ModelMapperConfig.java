package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

@Configuration
public class ModelMapperConfig {
    @Bean
    @Primary
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        
        // Configurar mapeo de Rol para evitar ciclos con la lista de usuarios
        mapper.typeMap(Rol.class, RolEntity.class)
            .addMappings(m -> m.skip(RolEntity::setUsuarios));
        
        mapper.typeMap(RolEntity.class, Rol.class)
            .addMappings(m -> m.skip(Rol::setUsuarios));
        
        // Configurar mapeo de Usuario para evitar problemas con listas anidadas
        mapper.typeMap(Usuario.class, UsuarioEntity.class)
            .addMappings(m -> {
                m.skip(UsuarioEntity::setSolicitudes);
                m.skip(UsuarioEntity::setCursosOfertadosInscritos);
            });
        
        mapper.typeMap(UsuarioEntity.class, Usuario.class)
            .addMappings(m -> {
                m.skip(Usuario::setSolicitudes);
                m.skip(Usuario::setCursosOfertadosInscritos);
            });
        
        return mapper;
    }
}
