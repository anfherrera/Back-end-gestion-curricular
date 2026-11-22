package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;

public interface GestionarSalonesCUIntPort {

    Salon obtenerSalonPorId(Integer idSalon);

    Salon obtenerSalonPorNumero(String numeroSalon);

    List<Salon> listarSalonesActivos();

    /** Salón informativo del curso o null si no se puede resolver.
     * Recorta el número y, si no lo encuentra, lo busca entre los salones activos (BUG-14).
     */
    Salon resolverSalonDeCurso(String numeroSalon);

}









