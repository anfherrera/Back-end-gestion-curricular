package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

/** El curso ya tiene todos sus cupos ocupados y no admite otra inscripción aceptada. */
public class CupoAgotadoException extends RuntimeException {

    private final int cupo;
    private final int ocupados;

    public CupoAgotadoException(int cupo, int ocupados) {
        super("El curso no tiene cupos disponibles (" + ocupados + " de " + cupo + " ocupados)");
        this.cupo = cupo;
        this.ocupados = ocupados;
    }

    public int getCupo() {
        return cupo;
    }

    public int getOcupados() {
        return ocupados;
    }
}
