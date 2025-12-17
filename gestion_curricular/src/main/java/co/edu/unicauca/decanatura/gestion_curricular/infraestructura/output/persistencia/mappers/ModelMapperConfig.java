package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

@Configuration
public class ModelMapperConfig {
    @Bean
    @Primary
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        
        // Configuración específica para mapeo de Usuario
        modelMapper.typeMap(UsuarioEntity.class, Usuario.class)
                .addMappings(mapper -> mapper.skip(Usuario::setCursosOfertadosInscritos))
                .addMappings(mapper -> mapper.skip(Usuario::setSolicitudes))
                .addMappings(mapper -> mapper.map(UsuarioEntity::getCedula, Usuario::setCedula))
                .setPostConverter(context -> {
                    UsuarioEntity source = context.getSource();
                    Usuario destination = context.getDestination();
                    
                    if (source == null || destination == null) {
                        return destination;
                    }
                    
                    // Asegurar que la cédula se mapee correctamente
                    if (source.getCedula() != null) {
                        destination.setCedula(source.getCedula());
                    }
                    
                    // Mapear objRol manualmente si existe
                    if (source.getObjRol() != null) {
                        try {
                            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol rol = 
                                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol();
                            rol.setId_rol(source.getObjRol().getId_rol());
                            if (source.getObjRol().getNombre() != null) {
                                rol.setNombre(source.getObjRol().getNombre());
                            }
                            destination.setObjRol(rol);
                        } catch (Exception e) {
                            // Si hay error, crear rol mínimo con solo el ID
                            try {
                                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol rol = 
                                    new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol();
                                rol.setId_rol(source.getObjRol().getId_rol());
                                destination.setObjRol(rol);
                            } catch (Exception e2) {
                                // Si aún falla, dejar null
                            }
                        }
                    }
                    
                    // Mapear objPrograma manualmente si existe
                    if (source.getObjPrograma() != null) {
                        try {
                            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa programa = 
                                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa();
                            programa.setId_programa(source.getObjPrograma().getId_programa());
                            if (source.getObjPrograma().getCodigo() != null) {
                                programa.setCodigo(source.getObjPrograma().getCodigo());
                            }
                            if (source.getObjPrograma().getNombre_programa() != null) {
                                programa.setNombre_programa(source.getObjPrograma().getNombre_programa());
                            }
                            destination.setObjPrograma(programa);
                        } catch (Exception e) {
                            // Si hay error, crear programa mínimo con solo el ID
                            try {
                                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa programa = 
                                    new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa();
                                programa.setId_programa(source.getObjPrograma().getId_programa());
                                destination.setObjPrograma(programa);
                            } catch (Exception e2) {
                                // Si aún falla, dejar null
                            }
                        }
                    }
                    
                    return destination;
                });
        
        modelMapper.typeMap(Usuario.class, UsuarioEntity.class)
                .addMappings(mapper -> mapper.skip(UsuarioEntity::setCursosOfertadosInscritos))
                .addMappings(mapper -> mapper.skip(UsuarioEntity::setSolicitudes))
                .addMappings(mapper -> mapper.map(Usuario::getCedula, UsuarioEntity::setCedula))
                .setPostConverter(context -> {
                    Usuario source = context.getSource();
                    UsuarioEntity destination = context.getDestination();
                    
                    if (source == null || destination == null) {
                        return destination;
                    }
                    
                    // Mapear objRol manualmente si existe
                    if (source.getObjRol() != null && source.getObjRol().getId_rol() != null) {
                        try {
                            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity rolEntity = 
                                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity();
                            rolEntity.setId_rol(source.getObjRol().getId_rol());
                            if (source.getObjRol().getNombre() != null) {
                                rolEntity.setNombre(source.getObjRol().getNombre());
                            }
                            destination.setObjRol(rolEntity);
                        } catch (Exception e) {
                            // Si hay error, intentar solo con el ID
                            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity rolEntity = 
                                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity();
                            rolEntity.setId_rol(source.getObjRol().getId_rol());
                            destination.setObjRol(rolEntity);
                        }
                    }
                    
                    // Mapear objPrograma manualmente si existe
                    if (source.getObjPrograma() != null && source.getObjPrograma().getId_programa() != null) {
                        try {
                            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity programaEntity = 
                                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity();
                            programaEntity.setId_programa(source.getObjPrograma().getId_programa());
                            if (source.getObjPrograma().getCodigo() != null) {
                                programaEntity.setCodigo(source.getObjPrograma().getCodigo());
                            }
                            if (source.getObjPrograma().getNombre_programa() != null) {
                                programaEntity.setNombre_programa(source.getObjPrograma().getNombre_programa());
                            }
                            destination.setObjPrograma(programaEntity);
                        } catch (Exception e) {
                            // Si hay error, intentar solo con el ID
                            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity programaEntity = 
                                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity();
                            programaEntity.setId_programa(source.getObjPrograma().getId_programa());
                            destination.setObjPrograma(programaEntity);
                        }
                    }
                    
                    return destination;
                });
        
        return modelMapper;
    }
}
