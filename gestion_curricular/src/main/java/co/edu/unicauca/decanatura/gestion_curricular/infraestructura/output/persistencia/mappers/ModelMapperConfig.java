package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
public class ModelMapperConfig {
    @Bean
    @Primary
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
