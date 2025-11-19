package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
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
    @Mapping(target = "fecha_inicio", ignore = true) // Se asigna manualmente en el controlador
    @Mapping(target = "fecha_fin", ignore = true) // Se asigna manualmente en el controlador
    @Mapping(target = "periodo_academico", ignore = true) // Se asigna manualmente en el controlador
    CursoOfertadoVerano mappearDeDTOPeticionACursoOfertado(CursosOfertadosDTOPeticion peticion);

    // Dominio → DTO Respuesta
    @Mapping(source = "id_curso", target = "id_curso")
    @Mapping(target = "idCurso", ignore = true) // Se asignará en post-mapping
    @Mapping(source = "objMateria.codigo", target = "codigo_curso")
    @Mapping(source = "objMateria.nombre", target = "nombre_curso")
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo", qualifiedByName = "convertirGrupoAString")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(source = "salon", target = "espacio_asignado")
    @Mapping(source = "cupo_estimado", target = "cupo_maximo")
    @Mapping(source = ".", target = "cupo_disponible", qualifiedByName = "calcularCupoDisponible")
    @Mapping(source = "estadosCursoOfertados", target = "estadosCursoOfertados")
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    @Mapping(source = "objMateria.nombre", target = "descripcion", qualifiedByName = "crearDescripcion")
    @Mapping(source = "fecha_inicio", target = "fecha_inicio", qualifiedByName = "formatearFecha")
    @Mapping(source = ".", target = "fecha_fin", qualifiedByName = "calcularFechaFin")
    @Mapping(source = "estadosCursoOfertados", target = "estado", qualifiedByName = "obtenerEstadoActual")
    @Mapping(source = ".", target = "periodo", qualifiedByName = "calcularPeriodoDesdeCurso")
    CursosOfertadosDTORespuesta mappearDeCursoOfertadoARespuesta(CursoOfertadoVerano curso);
    
    // Método post-mapping para asignar idCurso desde id_curso
    default CursosOfertadosDTORespuesta postMapCurso(CursosOfertadosDTORespuesta dto) {
        if (dto != null && dto.getId_curso() != null) {
            dto.setIdCurso(dto.getId_curso());
        }
        return dto;
    }
    
    // Mapper específico para cursos disponibles (estado "Disponible")
    @Mapping(source = "id_curso", target = "id_curso")
    @Mapping(target = "idCurso", ignore = true) // Se asignará en post-mapping
    @Mapping(source = "objMateria.codigo", target = "codigo_curso")
    @Mapping(source = "objMateria.nombre", target = "nombre_curso")
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo", qualifiedByName = "convertirGrupoAString")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(source = "salon", target = "espacio_asignado")
    @Mapping(source = "cupo_estimado", target = "cupo_maximo")
    @Mapping(source = ".", target = "cupo_disponible", qualifiedByName = "calcularCupoDisponible")
    @Mapping(source = "estadosCursoOfertados", target = "estadosCursoOfertados")
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    @Mapping(source = "objMateria.nombre", target = "descripcion", qualifiedByName = "crearDescripcion")
    @Mapping(source = "fecha_inicio", target = "fecha_inicio", qualifiedByName = "formatearFecha")
    @Mapping(source = ".", target = "fecha_fin", qualifiedByName = "calcularFechaFin")
    @Mapping(source = "estadosCursoOfertados", target = "estado", qualifiedByName = "obtenerEstadoActual")
    @Mapping(source = ".", target = "periodo", qualifiedByName = "calcularPeriodoDesdeCurso")
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
    default String obtenerEstadoActual(List<EstadoCursoOfertado> estados) {
        if (estados == null || estados.isEmpty()) {
            return "Abierto";
        }
        // Obtener el estado más reciente
        return estados.get(estados.size() - 1).getEstado_actual();
    }
    
    @Named("obtenerFechaInicio")
    default String obtenerFechaInicio(List<EstadoCursoOfertado> estados) {
        if (estados == null || estados.isEmpty()) {
            return "2024-06-01T08:00:00Z";
        }
        // Obtener la fecha del estado más reciente
        java.util.Date fecha = estados.get(estados.size() - 1).getFecha_registro_estado();
        if (fecha == null) {
            return "2024-06-01T08:00:00Z";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(fecha);
    }
    
    // Métodos obsoletos eliminados: obtenerFechaFin y calcularPeriodoAcademico
    // Ya no son necesarios porque fecha_fin y periodo_academico ahora están directamente en el curso
    
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
    
    @Named("formatearFecha")
    default String formatearFecha(java.util.Date fecha) {
        if (fecha == null) {
            // Para cursos antiguos que no tienen fecha, calcular una por defecto (6 meses desde ahora)
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            cal.add(java.util.Calendar.MONTH, 6);
            fecha = cal.getTime();
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(fecha);
    }
    
    @Named("calcularFechaFin")
    default String calcularFechaFin(CursoOfertadoVerano curso) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        
        // Si hay fecha_fin guardada, usarla
        if (curso.getFecha_fin() != null) {
            return sdf.format(curso.getFecha_fin());
        }
        // Si no hay fecha_fin pero hay fecha_inicio, calcular fecha_inicio + 6 semanas (para cursos antiguos)
        if (curso.getFecha_inicio() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            cal.setTime(curso.getFecha_inicio());
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 6);
            return sdf.format(cal.getTime());
        }
        // Si no hay ninguna fecha, calcular por defecto (7 meses desde ahora)
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        cal.add(java.util.Calendar.MONTH, 7);
        return sdf.format(cal.getTime());
    }
    
    @Named("calcularPeriodoDesdeCurso")
    default String calcularPeriodoDesdeCurso(CursoOfertadoVerano curso) {
        // Si hay período académico guardado, usarlo
        if (curso.getPeriodo_academico() != null && !curso.getPeriodo_academico().trim().isEmpty()) {
            return curso.getPeriodo_academico().trim();
        }
        // Si no hay período pero hay fecha_inicio, calcular desde la fecha (para cursos antiguos)
        if (curso.getFecha_inicio() != null) {
            java.time.LocalDate fecha = curso.getFecha_inicio().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            int año = fecha.getYear();
            int mes = fecha.getMonthValue();
            int numeroPeriodo = (mes <= 6) ? 1 : 2;
            return año + "-" + numeroPeriodo;
        }
        // Si no hay nada, calcular desde la fecha actual
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int año = ahora.getYear();
        int mes = ahora.getMonthValue();
        int numeroPeriodo = (mes <= 6) ? 1 : 2;
        return año + "-" + numeroPeriodo;
    }
    
    @Named("convertirGrupoAString")
    default String convertirGrupoAString(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano grupo) {
        if (grupo == null) {
            return "A"; // Valor por defecto
        }
        return grupo.toString();
    }
}
