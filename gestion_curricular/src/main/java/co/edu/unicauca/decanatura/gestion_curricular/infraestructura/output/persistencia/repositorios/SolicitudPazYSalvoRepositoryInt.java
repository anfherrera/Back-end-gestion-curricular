package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;

public interface SolicitudPazYSalvoRepositoryInt extends JpaRepository<SolicitudPazYSalvoEntity, Integer> {

    //aqui se debe implementar el metodo que retorna las listas de solicitudes que solo liste solicitudes cuyo ultimo estado sea "En revision" o "Aprobado"
}
