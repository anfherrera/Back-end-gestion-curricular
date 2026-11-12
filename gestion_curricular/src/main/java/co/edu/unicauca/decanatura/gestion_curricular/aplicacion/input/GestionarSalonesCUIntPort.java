package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;

public interface GestionarSalonesCUIntPort {

    Salon obtenerSalonPorId(Integer idSalon);

    Salon obtenerSalonPorNumero(String numeroSalon);

    List<Salon> listarSalonesActivos();

}









