package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;


@Service
@Transactional
public class GestionarProgramaGatewayImplAdapter implements GestionarProgramaGatewayIntPort {

    private final ProgramaRepositoryInt programaRepository;
    private final ModelMapper programaMapper;

    public GestionarProgramaGatewayImplAdapter(ProgramaRepositoryInt programaRepository, ModelMapper programaMapper) {
        this.programaRepository = programaRepository;
        this.programaMapper = programaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Programa buscarPorIdPrograma(Integer idPrograma) {
        return programaRepository.findById(idPrograma)
                .map(programaEntity -> programaMapper.map(programaEntity, Programa.class))
                .orElse(null); // Retorna null si no se encuentra el programa
    }
}
