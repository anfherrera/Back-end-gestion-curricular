package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;

public interface GestionarProgramaGatewayIntPort {
    Programa buscarPorIdPrograma(Integer idPrograma);
}
