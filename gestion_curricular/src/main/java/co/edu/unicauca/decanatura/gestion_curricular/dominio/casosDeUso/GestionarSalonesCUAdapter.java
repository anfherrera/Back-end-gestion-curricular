package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;

public class GestionarSalonesCUAdapter implements GestionarSalonesCUIntPort {

    private final GestionarSalonesIntPort salonesGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarSalonesCUAdapter(GestionarSalonesIntPort salonesGateway,
                                     FormateadorResultadosIntPort formateadorResultados) {
        this.salonesGateway = salonesGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Salon guardarSalon(Salon salon) {
        if (salon == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El salón no puede ser nulo.");
        }

        salon.setId_salon(null); // Asegurarse de que el ID sea nulo para una nueva creación

        if (salon.getNumero_salon() == null || salon.getNumero_salon().isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El número del salón es obligatorio.");
        }

        if (salonesGateway.existeSalonPorNumero(salon.getNumero_salon())) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe un salón con este número.");
        }

        return salonesGateway.guardarSalon(salon);
    }

    @Override
    public Salon actualizarSalon(Salon salon) {
        if (salon == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El salón no puede ser nulo.");
        }
        if (salon.getId_salon() == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido para actualizar.");
        }
        Salon existente = salonesGateway.obtenerSalonPorId(salon.getId_salon());
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el salón a actualizar.");
        }
        return salonesGateway.actualizarSalon(salon);
    }

    @Override
    public boolean eliminarSalon(Integer idSalon) {
        if (idSalon == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Salon existente = salonesGateway.obtenerSalonPorId(idSalon);
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el salón a eliminar.");
        }

        return salonesGateway.eliminarSalon(idSalon);
    }

    @Override
    public Salon obtenerSalonPorId(Integer idSalon) {
        if (idSalon == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Salon salon = salonesGateway.obtenerSalonPorId(idSalon);
        if (salon == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Salón no encontrado.");
        }

        return salon;
    }

    @Override
    public Salon obtenerSalonPorNumero(String numeroSalon) {
        if (numeroSalon == null || numeroSalon.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un número de salón válido.");
        }

        Salon salon = salonesGateway.obtenerSalonPorNumero(numeroSalon);
        if (salon == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Salón no encontrado con ese número.");
        }

        return salon;
    }

    @Override
    public boolean existeSalonPorNumero(String numeroSalon) {
        if (numeroSalon == null || numeroSalon.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un número de salón válido.");
        }

        return salonesGateway.existeSalonPorNumero(numeroSalon);
    }

    @Override
    public List<Salon> listarSalones() {
        return salonesGateway.listarSalones();
    }

    @Override
    public List<Salon> listarSalonesActivos() {
        return salonesGateway.listarSalonesActivos();
    }

    @Override
    public List<Salon> buscarPorEdificio(String edificio) {
        if (edificio == null || edificio.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un nombre de edificio válido.");
        }

        return salonesGateway.buscarPorEdificio(edificio);
    }

}









