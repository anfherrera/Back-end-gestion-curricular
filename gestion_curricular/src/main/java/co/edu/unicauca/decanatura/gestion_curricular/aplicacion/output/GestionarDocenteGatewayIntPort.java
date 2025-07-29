package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;

public interface GestionarDocenteGatewayIntPort {
    Docente buscarDocentePorId(Integer idDocente);
}
