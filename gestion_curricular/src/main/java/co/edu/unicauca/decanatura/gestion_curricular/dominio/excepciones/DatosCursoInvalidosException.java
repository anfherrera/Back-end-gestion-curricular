package co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones;

public class DatosCursoInvalidosException extends RuntimeException {

    public enum Motivo {
        FECHAS_INVALIDAS, SALON_REQUERIDO, SALON_NO_ENCONTRADO, CURSO_DUPLICADO
    }

    private final Motivo motivo;

    public DatosCursoInvalidosException(Motivo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
