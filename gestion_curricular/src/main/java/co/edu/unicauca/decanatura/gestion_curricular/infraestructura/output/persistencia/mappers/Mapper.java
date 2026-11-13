package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    private final ModelMapper modelMapper;

    public Mapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D, T> D map(final T source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }
}
