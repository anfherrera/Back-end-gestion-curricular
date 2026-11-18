package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SalonEntity;

/**
 * Mapeo explícito entre los catálogos del dominio y sus entidades (DUP-10).
 *
 * Copia los campos que ambos lados tienen; lo que solo tiene la entidad (los usuarios de un
 * programa) queda con el valor de su constructor. Como el {@code ModelMapper} que reemplaza, un
 * origen nulo lanza {@link IllegalArgumentException}.
 */
public final class CatalogosMapperPersistencia {

    private CatalogosMapperPersistencia() {
    }

    public static Programa aDominio(ProgramaEntity entidad) {
        Origen.exigir(entidad);
        return new Programa(entidad.getId_programa(), entidad.getCodigo(), entidad.getNombre_programa());
    }

    public static ProgramaEntity aEntidad(Programa programa) {
        Origen.exigir(programa);
        ProgramaEntity entidad = new ProgramaEntity();
        entidad.setId_programa(programa.getId_programa());
        entidad.setCodigo(programa.getCodigo());
        entidad.setNombre_programa(programa.getNombre_programa());
        return entidad;
    }

    public static Docente aDominio(DocenteEntity entidad) {
        Origen.exigir(entidad);
        return new Docente(entidad.getId_docente(), entidad.getCodigo_docente(), entidad.getNombre_docente());
    }

    public static DocenteEntity aEntidad(Docente docente) {
        Origen.exigir(docente);
        DocenteEntity entidad = new DocenteEntity();
        entidad.setId_docente(docente.getId_docente());
        entidad.setCodigo_docente(docente.getCodigo_docente());
        entidad.setNombre_docente(docente.getNombre_docente());
        return entidad;
    }

    public static Materia aDominio(MateriaEntity entidad) {
        Origen.exigir(entidad);
        return new Materia(entidad.getId_materia(), entidad.getCodigo(), entidad.getNombre(), entidad.getCreditos());
    }

    public static MateriaEntity aEntidad(Materia materia) {
        Origen.exigir(materia);
        MateriaEntity entidad = new MateriaEntity();
        entidad.setId_materia(materia.getId_materia());
        entidad.setCodigo(materia.getCodigo());
        entidad.setNombre(materia.getNombre());
        entidad.setCreditos(materia.getCreditos());
        return entidad;
    }

    public static Salon aDominio(SalonEntity entidad) {
        Origen.exigir(entidad);
        return new Salon(entidad.getId_salon(), entidad.getNumero_salon(), entidad.getEdificio(), entidad.getActivo());
    }
}
