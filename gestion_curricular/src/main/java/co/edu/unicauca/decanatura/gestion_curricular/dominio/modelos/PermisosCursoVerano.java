package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Matriz de operaciones por estado y rol de los cursos de verano. */
public final class PermisosCursoVerano {

    private PermisosCursoVerano() {
    }

    public static Map<String, Object> validar(String estadoCurso, String rolUsuario, String operacion) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("permitido", false);

        // Definir matriz de permisos por estado y rol
        Map<String, List<String>> permisosPorEstado = new HashMap<>();

        // Estado: Borrador
        permisosPorEstado.put("Borrador", List.of(
            "FUNCIONARIO:ver,editar_completo,eliminar,cambiar_estado",
            "COORDINADOR:ver,editar_completo,eliminar,cambiar_estado",
            "ESTUDIANTE:ninguno"
        ));

        // Estado: Abierto
        permisosPorEstado.put("Abierto", List.of(
            "FUNCIONARIO:ver,editar_parcial,cambiar_estado",
            "COORDINADOR:ver,editar_parcial,cambiar_estado",
            "ESTUDIANTE:ninguno"
        ));

        // Estado: Publicado
        permisosPorEstado.put("Publicado", List.of(
            "FUNCIONARIO:ver,gestionar_solicitudes,cambiar_estado",
            "COORDINADOR:ver,gestionar_solicitudes,cambiar_estado",
            "ESTUDIANTE:ver,solicitar_curso_nuevo"
        ));

        // Estado: Preinscripcion
        permisosPorEstado.put("Preinscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_preinscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_preinscripciones,cambiar_estado",
            "ESTUDIANTE:ver,preinscribirse"
        ));

        // Estado: Inscripcion
        permisosPorEstado.put("Inscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_inscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_inscripciones,cambiar_estado",
            "ESTUDIANTE:ver,inscribirse"
        ));

        // Estado: Cerrado
        permisosPorEstado.put("Cerrado", List.of(
            "FUNCIONARIO:ver,consultar",
            "COORDINADOR:ver,consultar",
            "ESTUDIANTE:ver,consultar"
        ));

        // Obtener permisos para el estado actual
        List<String> permisosEstado = permisosPorEstado.get(estadoCurso);
        if (permisosEstado == null) {
            resultado.put("error", "Estado desconocido");
            resultado.put("message", "No se pueden determinar permisos para el estado: " + estadoCurso);
            return resultado;
        }

        // Buscar permisos para el rol especifico
        String permisosRol = permisosEstado.stream()
            .filter(permiso -> permiso.startsWith(rolUsuario + ":"))
            .findFirst()
            .orElse(null);

        if (permisosRol == null) {
            resultado.put("error", "Rol no valido");
            resultado.put("message", "No se encontraron permisos para el rol: " + rolUsuario);
            return resultado;
        }

        // Extraer las operaciones permitidas
        String operacionesPermitidas = permisosRol.split(":")[1];

        if ("ninguno".equals(operacionesPermitidas)) {
            resultado.put("error", "Sin permisos");
            resultado.put("message", "Este rol no tiene permisos para realizar operaciones en el estado: " + estadoCurso);
            return resultado;
        }

        // Verificar si la operacion especifica esta permitida
        if (operacionesPermitidas.contains(operacion) || operacionesPermitidas.contains("todas")) {
            resultado.put("permitido", true);
            resultado.put("message", "Operacion permitida");
        } else {
            resultado.put("error", "Operacion no permitida");
            resultado.put("message", "El rol " + rolUsuario + " no puede realizar la operacion '" + operacion + "' en el estado " + estadoCurso);
        }

        return resultado;
    }
}
