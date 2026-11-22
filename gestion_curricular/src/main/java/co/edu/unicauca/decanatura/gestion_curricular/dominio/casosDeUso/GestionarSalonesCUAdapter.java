package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;

public class GestionarSalonesCUAdapter implements GestionarSalonesCUIntPort {

    private final GestionarSalonesIntPort salonesGateway;

    public GestionarSalonesCUAdapter(GestionarSalonesIntPort salonesGateway) {
        this.salonesGateway = salonesGateway;
    }

    @Override
    public Salon obtenerSalonPorId(Integer idSalon) {
        if (idSalon == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Salon salon = salonesGateway.obtenerSalonPorId(idSalon);
        if (salon == null) {
            throw new EntidadNoExisteException("Salón no encontrado.");
        }

        return salon;
    }

    @Override
    public Salon obtenerSalonPorNumero(String numeroSalon) {
        if (numeroSalon == null || numeroSalon.isBlank()) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un número de salón válido.");
        }

        Salon salon = salonesGateway.obtenerSalonPorNumero(numeroSalon);
        if (salon == null) {
            throw new EntidadNoExisteException("Salón no encontrado con ese número.");
        }

        return salon;
    }

    @Override
    public Salon resolverSalonDeCurso(String numeroSalon) {
        if (numeroSalon == null || numeroSalon.trim().isEmpty()) {
            return null;
        }
        // BUG-14: listado, detalle y edición recortan el número y, si la consulta falla
        // o no encuentra nada, buscan una coincidencia exacta entre los salones activos.
        String numero = numeroSalon.trim();
        try {
            try {
                return obtenerSalonPorNumero(numero);
            } catch (Exception e) {
                return listarSalonesActivos().stream()
                        .filter(s -> s.getNumeroSalon() != null && s.getNumeroSalon().equals(numero))
                        .findFirst().orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Salon> listarSalonesActivos() {
        return salonesGateway.listarSalonesActivos();
    }

}









