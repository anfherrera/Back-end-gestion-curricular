package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;



import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class Mapper {
    @Bean
    public ModelMapper creaMapper() {
        return new ModelMapper();
    }
}
