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
    public List<Salon> listarSalonesActivos() {
        return salonesGateway.listarSalonesActivos();
    }

}









