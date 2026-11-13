package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;

public interface GestionarSalonesCUIntPort {

    Salon obtenerSalonPorId(Integer idSalon);

    Salon obtenerSalonPorNumero(String numeroSalon);

    List<Salon> listarSalonesActivos();

    /** Salón informativo del curso o null si no se puede resolver.
     * BUG-14: solo el listado recorta el número y permite respaldo entre activos.
     */
    Salon resolverSalonDeCurso(String numeroSalon, boolean conRespaldoEnActivos);

}









