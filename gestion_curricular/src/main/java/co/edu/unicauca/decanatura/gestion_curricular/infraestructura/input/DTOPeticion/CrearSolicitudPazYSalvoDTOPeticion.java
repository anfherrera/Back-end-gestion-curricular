package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Cuerpo de {@code crearSolicitud-PazYSalvo}. Admite las dos formas que envían los clientes:
 * <ul>
 *   <li>con {@code idUsuario} (y opcionalmente {@code fecha_solicitud}, {@code periodo_academico},
 *       {@code titulo_trabajo_grado} y {@code director_trabajo_grado});</li>
 *   <li>con la forma de {@link SolicitudPazYSalvoDTOPeticion} ({@code objUsuario}, {@code nombre_solicitud}…).</li>
 * </ul>
 * Recibe el cuerpo tal como lo lee Jackson para conservar las coerciones de los clientes antiguos:
 * cualquier valor escalar o anidado se lee con su {@code toString()}, y un cuerpo que no es objeto JSON
 * se informa como formato no soportado. Las validaciones y sus mensajes dependen de la forma y siguen
 * en el controlador.
 */
public final class CrearSolicitudPazYSalvoDTOPeticion {
    static final String ID_USUARIO = "idUsuario";

    /** null si el cuerpo no es un objeto JSON. */
    private final Map<String, Object> campos;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    @SuppressWarnings("unchecked")
    public CrearSolicitudPazYSalvoDTOPeticion(Object cuerpo) {
        campos = cuerpo instanceof Map<?, ?> mapa ? new LinkedHashMap<>((Map<String, Object>) mapa) : null;
    }

    public boolean esObjeto() { return campos != null; }

    public boolean estaVacia() { return campos.isEmpty(); }

    /** El estudiante solo crea a su nombre: se ignora el usuario del cuerpo y se usa el del token. */
    public void asignarUsuario(Integer idUsuario) {
        campos.remove("objUsuario");
        campos.put(ID_USUARIO, idUsuario);
    }

    /** Forma con {@code idUsuario}: basta con que la clave exista, aunque su valor sea null. */
    public boolean traeIdUsuario() { return campos.containsKey(ID_USUARIO); }

    public String getIdUsuario() { return texto(ID_USUARIO); }
    public String getFechaSolicitud() { return texto("fecha_solicitud"); }
    public String getPeriodoAcademico() { return texto("periodo_academico"); }
    public String getTituloTrabajoGrado() { return texto("titulo_trabajo_grado"); }
    public String getDirectorTrabajoGrado() { return texto("director_trabajo_grado"); }

    /** Campos para convertir a {@link SolicitudPazYSalvoDTOPeticion} en la forma DTO. */
    public Map<String, Object> getCampos() { return Collections.unmodifiableMap(campos); }

    private String texto(String clave) {
        Object valor = campos.get(clave);
        return valor != null ? valor.toString() : null;
    }
}
