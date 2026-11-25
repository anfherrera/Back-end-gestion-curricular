package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

/** Comprobación común de los mappers de persistencia (DUP-10). */
final class Origen {

    private Origen() {
    }

    /** Lanza la misma excepción que {@code ModelMapper.map(null, ...)}. */
    static void exigir(Object origen) {
        if (origen == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
    }
}
