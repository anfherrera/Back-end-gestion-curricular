package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;

public interface GestionarSalonesIntPort {

    Salon guardarSalon(Salon salon);

    Salon actualizarSalon(Salon salon);

    boolean eliminarSalon(Integer idSalon);

    Salon obtenerSalonPorId(Integer idSalon);

    Salon obtenerSalonPorNumero(String numeroSalon);

    boolean existeSalonPorNumero(String numeroSalon);

    List<Salon> listarSalones();

    List<Salon> listarSalonesActivos();

    List<Salon> buscarPorEdificio(String edificio);

}


