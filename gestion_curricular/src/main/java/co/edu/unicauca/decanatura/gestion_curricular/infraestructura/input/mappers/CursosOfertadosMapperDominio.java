package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;

@Mapper(componentModel = "spring", uses = {
    MateriaMapperDominio.class,
    DocenteMapperDominio.class,
    UsuarioMapperDominio.class,
    EstadoCursoOfertadoMapper.class
})
public interface CursosOfertadosMapperDominio {

    // DTO Petición → Dominio
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(target = "estadosCursoOfertados", ignore = true)
    @Mapping(target = "estudiantesInscritos", ignore = true)
    @Mapping(target = "solicitudes" , ignore = true)
    CursoOfertadoVerano mappearDeDTOPeticionACursoOfertado(CursosOfertadosDTOPeticion peticion);

    // Dominio → DTO Respuesta
    @Mapping(source = "objMateria.codigo", target = "codigo_curso")
    @Mapping(source = "objMateria.nombre", target = "nombre_curso")
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(source = "salon", target = "espacio_asignado")
    @Mapping(source = "cupo_estimado", target = "cupo_maximo")
    @Mapping(source = ".", target = "cupo_disponible", qualifiedByName = "calcularCupoDisponible")
    @Mapping(source = "estadosCursoOfertados", target = "estadosCursoOfertados")
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    @Mapping(source = "objMateria.nombre", target = "descripcion", qualifiedByName = "crearDescripcion")
    @Mapping(source = "estadosCursoOfertados", target = "fecha_inicio", qualifiedByName = "obtenerFechaInicio")
    @Mapping(source = "estadosCursoOfertados", target = "fecha_fin", qualifiedByName = "obtenerFechaFin")
    @Mapping(source = "estadosCursoOfertados", target = "estado", qualifiedByName = "obtenerEstadoActual")
    @Mapping(source = "estadosCursoOfertados", target = "periodo", qualifiedByName = "calcularPeriodoAcademico")
    CursosOfertadosDTORespuesta mappearDeCursoOfertadoARespuesta(CursoOfertadoVerano curso);
    
    // Método post-mapping para asignar idCurso desde id_curso
    default CursosOfertadosDTORespuesta postMapCurso(CursosOfertadosDTORespuesta dto) {
        if (dto != null && dto.getId_curso() != null) {
            dto.setIdCurso(dto.getId_curso());
        }
        return dto;
    }
    
    // Mapper específico para cursos disponibles (estado "Disponible")
    @Mapping(source = "objMateria.codigo", target = "codigo_curso")
    @Mapping(source = "objMateria.nombre", target = "nombre_curso")
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(source = "salon", target = "espacio_asignado")
    @Mapping(source = "cupo_estimado", target = "cupo_maximo")
    @Mapping(source = ".", target = "cupo_disponible", qualifiedByName = "calcularCupoDisponible")
    @Mapping(source = "estadosCursoOfertados", target = "estadosCursoOfertados")
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    @Mapping(source = "objMateria.nombre", target = "descripcion", qualifiedByName = "crearDescripcion")
    @Mapping(source = "estadosCursoOfertados", target = "fecha_inicio", qualifiedByName = "obtenerFechaInicio")
    @Mapping(source = "estadosCursoOfertados", target = "fecha_fin", qualifiedByName = "obtenerFechaFin")
    @Mapping(source = "estadosCursoOfertados", target = "estado", qualifiedByName = "obtenerEstadoActual")
    @Mapping(source = "estadosCursoOfertados", target = "periodo", qualifiedByName = "calcularPeriodoAcademico")
    @Named("mappearDeCursoOfertadoARespuestaDisponible")
    CursosOfertadosDTORespuesta mappearDeCursoOfertadoARespuestaDisponible(CursoOfertadoVerano curso);

    // Listas - mapeo manual para evitar problemas con métodos auxiliares
    default List<CursosOfertadosDTORespuesta> mappearListaDeCursoOfertadoARespuesta(List<CursoOfertadoVerano> cursos) {
        if (cursos == null) {
            return null;
        }
        return cursos.stream()
                .map(this::mappearDeCursoOfertadoARespuesta)
                .map(this::postMapCurso) // Asignar idCurso
                .collect(java.util.stream.Collectors.toList());
    }
    
    default List<CursosOfertadosDTORespuesta> mappearListaDeCursoOfertadoARespuestaDisponible(List<CursoOfertadoVerano> cursos) {
        if (cursos == null) {
            return null;
        }
        return cursos.stream()
                .map(this::mappearDeCursoOfertadoARespuestaDisponible)
                .map(this::postMapCurso) // Asignar idCurso
                .collect(java.util.stream.Collectors.toList());
    }
    
    List<CursoOfertadoVerano> mappearListaDeDTOPeticionACursoOfertado(List<CursosOfertadosDTOPeticion> peticiones);
    
    // Métodos auxiliares para mapear datos reales
    @Named("crearDescripcion")
    default String crearDescripcion(String nombreMateria) {
        if (nombreMateria == null || nombreMateria.trim().isEmpty()) {
            return "Curso de verano";
        }
        return "Curso de " + nombreMateria;
    }
    
    @Named("obtenerEstadoActual")
    default String obtenerEstadoActual(List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado> estados) {
        if (estados == null || estados.isEmpty()) {
            return "Abierto";
        }
        // Obtener el estado más reciente
        return estados.get(estados.size() - 1).getEstado_actual();
    }
    
    @Named("obtenerFechaInicio")
    default String obtenerFechaInicio(List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado> estados) {
        if (estados == null || estados.isEmpty()) {
            return "2024-06-01T08:00:00Z";
        }
        // Obtener la fecha del estado más reciente
        java.util.Date fecha = estados.get(estados.size() - 1).getFecha_registro_estado();
        if (fecha == null) {
            return "2024-06-01T08:00:00Z";
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(fecha);
    }
    
    @Named("obtenerFechaFin")
    default String obtenerFechaFin(List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado> estados) {
        if (estados == null || estados.isEmpty()) {
            return "2024-07-15T17:00:00Z";
        }
        // Calcular fecha fin basada en la fecha de inicio + 6 semanas
        java.util.Date fechaInicio = estados.get(estados.size() - 1).getFecha_registro_estado();
        if (fechaInicio == null) {
            return "2024-07-15T17:00:00Z";
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaInicio);
        cal.add(java.util.Calendar.WEEK_OF_YEAR, 6); // 6 semanas después
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime());
    }
    
    @Named("calcularCupoDisponible")
    default Integer calcularCupoDisponible(CursoOfertadoVerano curso) {
        if (curso == null) {
            return 0;
        }
        
        Integer cupoMaximo = curso.getCupo_estimado();
        if (cupoMaximo == null) {
            return 0;
        }
        
        // Calcular estudiantes inscritos
        int estudiantesInscritos = 0;
        if (curso.getEstudiantesInscritos() != null) {
            estudiantesInscritos = curso.getEstudiantesInscritos().size();
        }
        
        // Cupo disponible = cupo máximo - estudiantes inscritos
        int cupoDisponible = cupoMaximo - estudiantesInscritos;
        
        // Asegurar que no sea negativo
        return Math.max(0, cupoDisponible);
    }
    
    @Named("calcularPeriodoAcademico")
    default String calcularPeriodoAcademico(List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado> estados) {
        if (estados == null || estados.isEmpty()) {
            return "N/A";
        }
        
        // Obtener la fecha del estado más reciente (fecha de inicio del curso)
        java.util.Date fechaInicio = estados.get(estados.size() - 1).getFecha_registro_estado();
        if (fechaInicio == null) {
            return "N/A";
        }
        
        // Convertir Date a LocalDate para facilitar el cálculo
        java.time.LocalDate fecha = fechaInicio.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
        
        int año = fecha.getYear();
        int mes = fecha.getMonthValue();
        
        // Lógica para determinar el período académico:
        // Primer período: Enero a Junio (meses 1-6)
        // Segundo período: Julio a Diciembre (meses 7-12)
        int numeroPeriodo = (mes <= 6) ? 1 : 2;
        
        // Retornar en formato "YYYY-P" (ej: "2025-1", "2025-2")
        return año + "-" + numeroPeriodo;
    }
}
