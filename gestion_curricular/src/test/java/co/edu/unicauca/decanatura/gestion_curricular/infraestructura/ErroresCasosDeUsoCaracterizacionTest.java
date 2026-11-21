package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort.DatosCreacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarArchivosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocentesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocumentosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarMateriasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarProgramasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarRolesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSalonesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCursoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudEcaesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudHomologacionCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudPazYSalvoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudReingresoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarUsuarioCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización de los errores que lanzan los 16 casos de uso (ARQ-12): ejecuta cada camino de error con los
 * gateways simulados detrás de un controlador de prueba y fija el estado y el cuerpo HTTP que arma
 * RestApiExceptionHandler, además de la clase de la excepción.
 */
class ErroresCasosDeUsoCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Callable<?>> casos = new LinkedHashMap<>();
    private final Map<String, String> excepciones = new LinkedHashMap<>();

    @RestController
    class Disparador {
        @GetMapping("/caso")
        JsonNode ejecutar(@RequestParam String nombre) throws Exception {
            try {
                return GrafoObjetos.volcar(casos.get(nombre).call());
            } catch (Exception e) {
                excepciones.put(nombre, e.getClass().getSimpleName());
                throw e;
            }
        }
    }

    @Test
    void conservaLasRespuestasDeErrorDeLosCasosDeUso() throws Exception {
        catalogos();
        solicitudYArchivos();
        documentosYUsuarios();
        estadisticas();
        solicitudesConFlujo();
        cursosOfertados();
        solicitudesCursoVerano();

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new Disparador())
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        ObjectNode resultados = json.createObjectNode();
        for (String nombre : casos.keySet()) {
            MvcResult respuesta = mvc.perform(get("/caso").param("nombre", nombre)).andReturn();
            ObjectNode caso = resultados.putObject(nombre);
            caso.put("estado", respuesta.getResponse().getStatus());
            caso.put("excepcion", excepciones.get(nombre));
            String cuerpo = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
            JsonNode nodo = cuerpo.isEmpty() ? null : json.readTree(cuerpo);
            if (nodo != null) {
                normalizar(nodo);
            }
            caso.set("cuerpo", nodo);
        }
        // BUG-39: el salón inexistente lanza el DatosCursoInvalidosException que el controlador de cursos traduce
        // a 400; aquí, sin controlador, llega al manejador global como error inesperado
        // BUG-38: el grupo en uso lleva separador antes del grupo y reingreso ya no dice "homologación"; en los
        // catálogos, usuarios, estadísticas, documentos, solicitudes y cursos de verano, lo que no se encuentra
        // responde 404 (también el usuario de ECAES, Homologación, Reingreso y de la preinscripción e inscripción de
        // verano, antes 400 o 409) y un dato que falta o no es válido, 400
        GrafoObjetos.compararConReferencia(resultados, "errores/casos-de-uso.json",
            "errores/correcciones/casos-de-uso-bug39.json", "errores/correcciones/casos-de-uso-bug38.json",
            "errores/correcciones/casos-de-uso-bug38-catalogos.json",
            "errores/correcciones/casos-de-uso-bug38-usuarios.json",
            "errores/correcciones/casos-de-uso-bug38-estadisticas.json",
            "errores/correcciones/casos-de-uso-bug38-solicitudes.json",
            "errores/correcciones/casos-de-uso-bug38-verano.json");
    }

    private void caso(String nombre, Callable<?> llamada) {
        casos.put(nombre, llamada);
    }

    // ---------------------------------------------------------------- catálogos

    private void catalogos() {
        caso("docentes guardar nulo", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).guardarDocente(null));
        caso("docentes guardar codigo repetido", () -> {
            var g = mock(GestionarDocenteGatewayIntPort.class);
            when(g.existeDocentePorCodigo(any())).thenReturn(true);
            return docentes(g).guardarDocente(new Docente());
        });
        caso("docentes actualizar nulo", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).actualizarDocente(null));
        caso("docentes actualizar sin id", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).actualizarDocente(new Docente()));
        caso("docentes actualizar inexistente", () -> {
            var docente = new Docente();
            docente.setId_docente(4);
            return docentes(mock(GestionarDocenteGatewayIntPort.class)).actualizarDocente(docente);
        });
        caso("docentes eliminar sin id", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).eliminarDocente(null));
        caso("docentes eliminar inexistente", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).eliminarDocente(4));
        caso("docentes eliminar con cursos", () -> {
            var g = mock(GestionarDocenteGatewayIntPort.class);
            when(g.obtenerDocentePorId(4)).thenReturn(new Docente());
            when(g.tieneCursosAsignados(4)).thenReturn(true);
            return docentes(g).eliminarDocente(4);
        });
        caso("docentes obtener sin id", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).obtenerDocentePorId(null));
        caso("docentes obtener inexistente", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).obtenerDocentePorId(4));
        caso("docentes por codigo blanco", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).obtenerDocentePorCodigo(" "));
        caso("docentes por codigo inexistente", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).obtenerDocentePorCodigo("D1"));
        caso("docentes nombre parcial blanco", () -> docentes(mock(GestionarDocenteGatewayIntPort.class)).buscarPorNombreParcial(""));

        caso("programas guardar nulo", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).guardarPrograma(null));
        caso("programas guardar codigo repetido", () -> {
            var g = mock(GestionarProgramaGatewayIntPort.class);
            when(g.existeProgramaPorCodigo(any())).thenReturn(true);
            return programas(g).guardarPrograma(new Programa());
        });
        caso("programas actualizar nulo", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).actualizarPrograma(null));
        caso("programas actualizar sin id", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).actualizarPrograma(new Programa()));
        caso("programas actualizar inexistente", () -> {
            var programa = new Programa();
            programa.setId_programa(2);
            return programas(mock(GestionarProgramaGatewayIntPort.class)).actualizarPrograma(programa);
        });
        caso("programas eliminar sin id", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).eliminarPrograma(null));
        caso("programas eliminar inexistente", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).eliminarPrograma(2));
        caso("programas eliminar con usuarios", () -> {
            var g = mock(GestionarProgramaGatewayIntPort.class);
            when(g.obtenerProgramaPorId(2)).thenReturn(new Programa());
            when(g.tieneUsuariosAsociados(2)).thenReturn(true);
            return programas(g).eliminarPrograma(2);
        });
        caso("programas obtener sin id", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).obtenerProgramaPorId(null));
        caso("programas obtener inexistente", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).obtenerProgramaPorId(2));
        caso("programas por codigo blanco", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).obtenerProgramaPorCodigo(null));
        caso("programas por codigo inexistente", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).obtenerProgramaPorCodigo("P1"));
        caso("programas nombre parcial blanco", () -> programas(mock(GestionarProgramaGatewayIntPort.class)).buscarPorNombreParcial(" "));

        caso("roles guardar nulo", () -> roles(mock(GestionarRolGatewayIntPort.class)).guardarRol(null));
        caso("roles guardar nombre repetido", () -> {
            var g = mock(GestionarRolGatewayIntPort.class);
            when(g.existeRolPorNombre(any())).thenReturn(true);
            return roles(g).guardarRol(new Rol());
        });
        caso("roles actualizar nulo", () -> roles(mock(GestionarRolGatewayIntPort.class)).actualizarRol(null));
        caso("roles actualizar sin id", () -> roles(mock(GestionarRolGatewayIntPort.class)).actualizarRol(new Rol()));
        caso("roles actualizar inexistente", () -> {
            var rol = new Rol();
            rol.setId_rol(3);
            return roles(mock(GestionarRolGatewayIntPort.class)).actualizarRol(rol);
        });
        caso("roles eliminar sin id", () -> roles(mock(GestionarRolGatewayIntPort.class)).eliminarRol(null));
        caso("roles eliminar inexistente", () -> roles(mock(GestionarRolGatewayIntPort.class)).eliminarRol(3));
        caso("roles eliminar con usuarios", () -> {
            var g = mock(GestionarRolGatewayIntPort.class);
            when(g.obtenerRolPorId(3)).thenReturn(new Rol());
            when(g.tieneUsuariosAsociados(3)).thenReturn(true);
            return roles(g).eliminarRol(3);
        });
        caso("roles obtener sin id", () -> roles(mock(GestionarRolGatewayIntPort.class)).obtenerRolPorId(null));
        caso("roles obtener inexistente", () -> roles(mock(GestionarRolGatewayIntPort.class)).obtenerRolPorId(3));
        caso("roles por nombre blanco", () -> roles(mock(GestionarRolGatewayIntPort.class)).obtenerRolPorNombre(""));
        caso("roles por nombre inexistente", () -> roles(mock(GestionarRolGatewayIntPort.class)).obtenerRolPorNombre("Admin"));

        caso("materias obtener sin id", () -> materias(mock(GestionarMateriasIntPort.class)).obtenerMateriaPorId(null));
        caso("materias obtener inexistente", () -> materias(mock(GestionarMateriasIntPort.class)).obtenerMateriaPorId(8));

        caso("salones obtener sin id", () -> salones(mock(GestionarSalonesIntPort.class)).obtenerSalonPorId(null));
        caso("salones obtener inexistente", () -> salones(mock(GestionarSalonesIntPort.class)).obtenerSalonPorId(6));
        caso("salones por numero blanco", () -> salones(mock(GestionarSalonesIntPort.class)).obtenerSalonPorNumero(" "));
        caso("salones por numero inexistente", () -> salones(mock(GestionarSalonesIntPort.class)).obtenerSalonPorNumero("101"));
        caso("salones resolver inexistente sin respaldo",
            () -> salones(mock(GestionarSalonesIntPort.class)).resolverSalonDeCurso("101", false));
        caso("salones resolver inexistente con respaldo",
            () -> salones(mock(GestionarSalonesIntPort.class)).resolverSalonDeCurso(" 101 ", true));
    }

    // ---------------------------------------------------------------- solicitud genérica y archivos

    private void solicitudYArchivos() {
        caso("solicitud obtener sin id", () -> solicitud(mock(GestionarSolicitudGatewayIntPort.class)).obtenerSolicitudPorId(null));
        caso("solicitud eliminar sin id", () -> solicitud(mock(GestionarSolicitudGatewayIntPort.class)).eliminarSolicitud(null));
        caso("solicitud eliminar inexistente", () -> solicitud(mock(GestionarSolicitudGatewayIntPort.class)).eliminarSolicitud(5));
        caso("solicitud por nombre blanco", () -> solicitud(mock(GestionarSolicitudGatewayIntPort.class)).obtenerSolicitudesPorNombre(" "));

        ArchivoSubido archivo = mock(ArchivoSubido.class);
        caso("archivos guardar sin archivo", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(null, "a.pdf", "pdf"));
        caso("archivos guardar sin nombre", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(archivo, null, "pdf"));
        caso("archivos guardar sin tipo", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(archivo, "a.pdf", null));
        caso("archivos guardar falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.saveFile(any(), any(), any())).thenThrow(new IOException("disco lleno"));
            return archivos(g).saveFile(archivo, "a.pdf", "pdf");
        });
        caso("archivos guardar carpeta sin archivo",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(null, "a.pdf", "pdf", "reingreso", 1));
        caso("archivos guardar carpeta sin nombre",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(archivo, null, "pdf", "reingreso", 1));
        caso("archivos guardar carpeta sin tipo",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(archivo, "a.pdf", null, "reingreso", 1));
        caso("archivos guardar carpeta sin tipo de solicitud",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(archivo, "a.pdf", "pdf", " ", 1));
        caso("archivos guardar carpeta sin id",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).saveFile(archivo, "a.pdf", "pdf", "reingreso", null));
        caso("archivos guardar carpeta falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.saveFile(any(), any(), any(), any(), any())).thenThrow(new IOException("sin permisos"));
            return archivos(g).saveFile(archivo, "a.pdf", "pdf", "reingreso", 1);
        });
        caso("archivos leer sin nombre", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).getFile(null));
        caso("archivos leer falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.getFile(any())).thenThrow(new IOException("no se encontró"));
            return archivos(g).getFile("a.pdf");
        });
        caso("archivos leer ruta blanca", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).getFileByPath(" "));
        caso("archivos leer ruta falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.getFileByPath(any())).thenThrow(new IOException("ruta inválida"));
            return archivos(g).getFileByPath("x/a.pdf");
        });
        caso("archivos mover sin ruta",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).moverArchivoAOrganizado("", "a.pdf", "reingreso", 1));
        caso("archivos mover sin nombre",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).moverArchivoAOrganizado("x", " ", "reingreso", 1));
        caso("archivos mover sin tipo de solicitud",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).moverArchivoAOrganizado("x", "a.pdf", null, 1));
        caso("archivos mover sin id",
            () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).moverArchivoAOrganizado("x", "a.pdf", "reingreso", null));
        caso("archivos mover falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.moverArchivoAOrganizado(any(), any(), any(), any())).thenThrow(new IOException("no se pudo mover"));
            return archivos(g).moverArchivoAOrganizado("x", "a.pdf", "reingreso", 1);
        });
        caso("archivos ruta sin nombre", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).getFileAsPath(null));
        caso("archivos ruta falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.getFileAsPath(any())).thenThrow(new IOException("no existe"));
            return archivos(g).getFileAsPath("a.pdf");
        });
        caso("archivos ruta relativa blanca", () -> archivos(mock(GestionarArchivosGatewayIntPort.class)).getFileByPathAsPath(null));
        caso("archivos ruta relativa falla", () -> {
            var g = mock(GestionarArchivosGatewayIntPort.class);
            when(g.getFileByPathAsPath(any())).thenThrow(new IOException("fuera de la raíz"));
            return archivos(g).getFileByPathAsPath("../a.pdf");
        });
    }

    // ---------------------------------------------------------------- documentos y usuarios

    private void documentosYUsuarios() {
        caso("documentos registrar nulo", () -> documentos(mock(GestionarDocumentosGatewayIntPort.class)).registrarDocumento(null));
        caso("documentos buscar sin id", () -> documentos(mock(GestionarDocumentosGatewayIntPort.class)).buscarDocumentoId(null));
        caso("documentos buscar inexistente", () -> documentos(mock(GestionarDocumentosGatewayIntPort.class)).buscarDocumentoId(9));
        caso("documentos comentar sin id", () -> {
            documentos(mock(GestionarDocumentosGatewayIntPort.class)).añadirComentario(null, "hola", "Ana", "Funcionario");
            return "sin error";
        });
        caso("documentos comentar vacio", () -> {
            documentos(mock(GestionarDocumentosGatewayIntPort.class)).añadirComentario(9, "  ", "Ana", "Funcionario");
            return "sin error";
        });
        caso("documentos comentar inexistente", () -> {
            documentos(mock(GestionarDocumentosGatewayIntPort.class)).añadirComentario(9, "hola", "Ana", "Funcionario");
            return "sin error";
        });

        caso("usuarios crear correo repetido", () -> {
            var g = mock(GestionarUsuarioGatewayIntPort.class);
            when(g.buscarUsuarioPorCorreo(any())).thenReturn(new Usuario());
            return usuarios(g, mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class))
                .crearUsuario(usuario(5, 2, 3));
        });
        caso("usuarios crear codigo repetido", () -> {
            var g = mock(GestionarUsuarioGatewayIntPort.class);
            when(g.buscarUsuarioPorCodigo(any())).thenReturn(new Usuario());
            return usuarios(g, mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class))
                .crearUsuario(usuario(5, 2, 3));
        });
        caso("usuarios crear cedula repetida", () -> {
            var g = mock(GestionarUsuarioGatewayIntPort.class);
            when(g.buscarUsuarioPorCedula("123")).thenReturn(new Usuario());
            return usuarios(g, mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class))
                .crearUsuario(usuario(5, 2, 3));
        });
        caso("usuarios crear sin programa", () -> {
            var u = usuario(5, 2, 3);
            u.setObjPrograma(null);
            return usuarios(mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarProgramaGatewayIntPort.class),
                mock(GestionarRolGatewayIntPort.class)).crearUsuario(u);
        });
        caso("usuarios crear programa inexistente", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).crearUsuario(usuario(5, 2, 3)));
        caso("usuarios crear rol inexistente", () -> {
            var p = mock(GestionarProgramaGatewayIntPort.class);
            when(p.buscarPorIdPrograma(2)).thenReturn(new Programa());
            return usuarios(mock(GestionarUsuarioGatewayIntPort.class), p, mock(GestionarRolGatewayIntPort.class))
                .crearUsuario(usuario(5, 2, 3));
        });
        caso("usuarios actualizar nulo", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).actualizarUsuario(null));
        caso("usuarios actualizar sin id", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class))
            .actualizarUsuario(usuario(null, 2, 3)));
        caso("usuarios actualizar inexistente", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class))
            .actualizarUsuario(usuario(5, 2, 3)));
        caso("usuarios actualizar sin programa", () -> {
            var u = usuario(5, 2, 3);
            u.setObjPrograma(null);
            return actualizarConExistente(u, false, false);
        });
        caso("usuarios actualizar programa sin id", () -> actualizarConExistente(usuario(5, null, 3), false, false));
        caso("usuarios actualizar programa inexistente", () -> actualizarConExistente(usuario(5, 2, 3), false, false));
        caso("usuarios actualizar sin rol", () -> {
            var u = usuario(5, 2, 3);
            u.setObjRol(null);
            return actualizarConExistente(u, true, false);
        });
        caso("usuarios actualizar rol sin id", () -> actualizarConExistente(usuario(5, 2, null), true, false));
        caso("usuarios actualizar rol inexistente", () -> actualizarConExistente(usuario(5, 2, 3), true, false));
        caso("usuarios actualizar cedula de otro", () -> {
            var g = mock(GestionarUsuarioGatewayIntPort.class);
            when(g.obtenerUsuarioPorId(5)).thenReturn(new Usuario());
            when(g.buscarUsuarioPorCedula("123")).thenReturn(usuario(6, 2, 3));
            return usuarios(g, programaExistente(), rolExistente()).actualizarUsuario(usuario(5, 2, 3));
        });
        caso("usuarios actualizar falla el gateway", () -> {
            var g = mock(GestionarUsuarioGatewayIntPort.class);
            when(g.obtenerUsuarioPorId(5)).thenReturn(new Usuario());
            when(g.actualizarUsuario(any())).thenThrow(new IllegalStateException("bloqueo"));
            return usuarios(g, programaExistente(), rolExistente()).actualizarUsuario(usuario(5, 2, 3));
        });
        caso("usuarios eliminar sin id", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).eliminarUsuario(null));
        caso("usuarios eliminar inexistente", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).eliminarUsuario(5));
        caso("usuarios obtener sin id", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).obtenerUsuarioPorId(null));
        caso("usuarios obtener inexistente", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).obtenerUsuarioPorId(5));
        caso("usuarios por correo blanco", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class)).buscarUsuarioPorCorreo(" "));
        caso("usuarios por correo inexistente", () -> usuarios(mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarProgramaGatewayIntPort.class), mock(GestionarRolGatewayIntPort.class))
            .buscarUsuarioPorCorreo("ana@unicauca.edu.co"));
    }

    private static Usuario actualizarConExistente(Usuario usuario, boolean conPrograma, boolean conRol) {
        var g = mock(GestionarUsuarioGatewayIntPort.class);
        when(g.obtenerUsuarioPorId(5)).thenReturn(new Usuario());
        return usuarios(g, conPrograma ? programaExistente() : mock(GestionarProgramaGatewayIntPort.class),
            conRol ? rolExistente() : mock(GestionarRolGatewayIntPort.class)).actualizarUsuario(usuario);
    }

    private static GestionarProgramaGatewayIntPort programaExistente() {
        var p = mock(GestionarProgramaGatewayIntPort.class);
        when(p.buscarPorIdPrograma(any())).thenReturn(new Programa());
        return p;
    }

    private static GestionarRolGatewayIntPort rolExistente() {
        var r = mock(GestionarRolGatewayIntPort.class);
        var rol = new Rol();
        rol.setUsuarios(new ArrayList<>());
        when(r.buscarRolPorId(any())).thenReturn(rol);
        return r;
    }

    private static Usuario usuario(Integer id, Integer idPrograma, Integer idRol) {
        var programa = new Programa();
        programa.setId_programa(idPrograma);
        var rol = new Rol();
        rol.setId_rol(idRol);
        var usuario = new Usuario();
        usuario.setId_usuario(id);
        usuario.setCodigo("104");
        usuario.setCedula("123");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setObjPrograma(programa);
        usuario.setObjRol(rol);
        return usuario;
    }

    // ---------------------------------------------------------------- estadísticas

    private void estadisticas() {
        Date antes = new Date(0);
        Date despues = new Date(86_400_000L);
        caso("estadisticas crear nula", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).crearEstadistica(null));
        caso("estadisticas actualizar sin id",
            () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).actualizarEstadistica(new Estadistica()));
        caso("estadisticas actualizar inexistente", () -> {
            var e = new Estadistica();
            e.setId_estadistica(7);
            return estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).actualizarEstadistica(e);
        });
        caso("estadisticas eliminar sin id", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).eliminarEstadistica(null));
        caso("estadisticas eliminar inexistente", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).eliminarEstadistica(7));
        caso("estadisticas obtener sin id", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).obtenerEstadisticaPorId(null));
        caso("estadisticas obtener inexistente", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).obtenerEstadisticaPorId(7));
        caso("estadisticas solicitud sin proceso", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(7, " ", antes, despues, "Aprobada", 1));
        caso("estadisticas solicitud incompleta", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(7, "Reingreso", antes, null, "Aprobada", 1));
        caso("estadisticas periodo estado programa incompleta", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPeriodoEstadoYPrograma(antes, despues, null));
        caso("estadisticas por proceso vacio", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).obtenerEstadisticasPorProceso(""));
        caso("estadisticas por estado vacio", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).obtenerEstadisticasPorEstado(" "));
        caso("estadisticas por programa invalido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class)).obtenerEstadisticasPorPrograma(0));
        caso("estadisticas por periodo sin fechas", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorPeriodo((Date) null, despues));
        caso("estadisticas por periodo invertido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorPeriodo(despues, antes));
        caso("estadisticas programa y periodo programa invalido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorProgramaYPeriodo(null, antes, despues));
        caso("estadisticas programa y periodo sin fechas", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorProgramaYPeriodo(1, antes, null));
        caso("estadisticas programa y periodo invertido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorProgramaYPeriodo(1, despues, antes));
        caso("estadisticas proceso y estado sin proceso", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorProcesoYEstado(null, "Aprobada"));
        caso("estadisticas proceso y estado sin estado", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorProcesoYEstado("Reingreso", ""));
        caso("estadisticas tendencias sin fechas", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerTendenciasPorPeriodo(antes, despues, null, despues));
        caso("estadisticas tendencias invertidas", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerTendenciasPorPeriodo(antes, despues, despues, antes));
        caso("estadisticas historial inicio invalido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerHistorialEstadisticas(2019, null));
        caso("estadisticas historial fin invalido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerHistorialEstadisticas(null, 2031));
        caso("estadisticas historial invertido", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerHistorialEstadisticas(2025, 2024));
        caso("estadisticas periodo academico vacio", () -> estadisticas(mock(GestionarEstadisticasGatewayIntPort.class))
            .obtenerEstadisticasPorPeriodoAcademico(" "));
    }

    // ---------------------------------------------------------------- ECAES, homologación, reingreso y paz y salvo

    private void solicitudesConFlujo() {
        caso("ecaes guardar nula", () -> ecaes(mock(GestionarPreRegistroEcaesGatewayIntPort.class), mock(GestionarUsuarioGatewayIntPort.class))
            .guardar(null));
        caso("ecaes guardar id repetido", () -> {
            var g = mock(GestionarPreRegistroEcaesGatewayIntPort.class);
            when(g.buscarOpcionalPorId(11)).thenReturn(Optional.of(new SolicitudEcaes()));
            return ecaes(g, mock(GestionarUsuarioGatewayIntPort.class)).guardar(conUsuario(new SolicitudEcaes(), 11, 5));
        });
        caso("ecaes guardar sin usuario", () -> ecaes(mock(GestionarPreRegistroEcaesGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).guardar(conUsuario(new SolicitudEcaes(), null, null)));
        caso("ecaes guardar usuario inexistente", () -> ecaes(mock(GestionarPreRegistroEcaesGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).guardar(conUsuario(new SolicitudEcaes(), null, 5)));
        caso("ecaes cambiar estado no permitido", () -> {
            var g = mock(GestionarPreRegistroEcaesGatewayIntPort.class);
            when(g.buscarPorId(11)).thenReturn(Optional.of(conEstado(new SolicitudEcaes(), "Enviada")));
            ecaes(g, mock(GestionarUsuarioGatewayIntPort.class)).cambiarEstadoSolicitud(11, "Inventado");
            return "sin error";
        });

        caso("homologacion guardar nula", () -> homologacion(mock(GestionarSolicitudHomologacionGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).guardar(null));
        caso("homologacion guardar id repetido", () -> {
            var g = mock(GestionarSolicitudHomologacionGatewayIntPort.class);
            when(g.buscarPorId(12)).thenReturn(Optional.of(new SolicitudHomologacion()));
            return homologacion(g, mock(GestionarUsuarioGatewayIntPort.class)).guardar(conUsuario(new SolicitudHomologacion(), 12, 5));
        });
        caso("homologacion guardar sin usuario", () -> homologacion(mock(GestionarSolicitudHomologacionGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).guardar(conUsuario(new SolicitudHomologacion(), null, null)));
        caso("homologacion guardar usuario inexistente", () -> homologacion(mock(GestionarSolicitudHomologacionGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).guardar(conUsuario(new SolicitudHomologacion(), null, 5)));
        caso("homologacion cambiar estado no permitido", () -> {
            var g = mock(GestionarSolicitudHomologacionGatewayIntPort.class);
            when(g.buscarPorId(12)).thenReturn(Optional.of(conEstado(new SolicitudHomologacion(), "Enviada")));
            homologacion(g, mock(GestionarUsuarioGatewayIntPort.class)).cambiarEstadoSolicitud(12, "Inventado");
            return "sin error";
        });

        caso("reingreso crear nula", () -> reingreso(mock(GestionarSolicitudReingresoGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).crearSolicitudReingreso(null));
        caso("reingreso crear id repetido", () -> {
            var g = mock(GestionarSolicitudReingresoGatewayIntPort.class);
            when(g.buscarPorId(13)).thenReturn(Optional.of(new SolicitudReingreso()));
            return reingreso(g, mock(GestionarUsuarioGatewayIntPort.class))
                .crearSolicitudReingreso(conUsuario(new SolicitudReingreso(), 13, 5));
        });
        caso("reingreso crear sin usuario", () -> reingreso(mock(GestionarSolicitudReingresoGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).crearSolicitudReingreso(conUsuario(new SolicitudReingreso(), null, null)));
        caso("reingreso crear usuario inexistente", () -> reingreso(mock(GestionarSolicitudReingresoGatewayIntPort.class),
            mock(GestionarUsuarioGatewayIntPort.class)).crearSolicitudReingreso(conUsuario(new SolicitudReingreso(), null, 5)));
        caso("reingreso cambiar estado no permitido", () -> {
            var g = mock(GestionarSolicitudReingresoGatewayIntPort.class);
            when(g.buscarPorId(13)).thenReturn(Optional.of(conEstado(new SolicitudReingreso(), "Enviada")));
            reingreso(g, mock(GestionarUsuarioGatewayIntPort.class)).cambiarEstadoSolicitudReingreso(13, "Inventado");
            return "sin error";
        });

        caso("paz y salvo guardar nula", () -> pazYSalvo(mock(GestionarSolicitudPazYSalvoGatewayIntPort.class)).guardar(null));
        caso("paz y salvo guardar sin usuario",
            () -> pazYSalvo(mock(GestionarSolicitudPazYSalvoGatewayIntPort.class)).guardar(conUsuario(new SolicitudPazYSalvo(), null, null)));
        caso("paz y salvo cambiar estado no permitido", () -> {
            var g = mock(GestionarSolicitudPazYSalvoGatewayIntPort.class);
            when(g.buscarPorId(14)).thenReturn(Optional.of(conEstado(new SolicitudPazYSalvo(), "Enviada")));
            pazYSalvo(g).cambiarEstadoSolicitud(14, "Inventado", null);
            return "sin error";
        });
    }

    private static <T extends Solicitud> T conUsuario(T solicitud, Integer id, Integer idUsuario) {
        solicitud.setId_solicitud(id);
        if (idUsuario != null) {
            var usuario = new Usuario();
            usuario.setId_usuario(idUsuario);
            solicitud.setObjUsuario(usuario);
        }
        return solicitud;
    }

    private static <T extends Solicitud> T conEstado(T solicitud, String estado) {
        var registro = new EstadoSolicitud();
        registro.setEstado_actual(estado);
        registro.setFecha_registro_estado(new Date(0));
        solicitud.setEstadosSolicitud(new ArrayList<>(List.of(registro)));
        return solicitud;
    }

    // ---------------------------------------------------------------- cursos ofertados de verano

    private void cursosOfertados() {
        caso("curso actualizar nulo", () -> new Curso().cu().actualizarCurso(null, estadoCurso(1, "Abierto")));
        caso("curso actualizar sin id", () -> new Curso().cu().actualizarCurso(new CursoOfertadoVerano(), estadoCurso(1, "Abierto")));
        caso("curso actualizar inexistente", () -> new Curso().cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Abierto")));
        caso("curso actualizar sin estados", () -> {
            var c = new Curso();
            when(c.cursos.obtenerCursoPorId(3)).thenReturn(cursoConId(3));
            return c.cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Abierto"));
        });
        caso("curso actualizar estado nulo", () -> new Curso().conCurso("Abierto").cu().actualizarCurso(cursoConId(3), null));
        caso("curso actualizar estado sin id",
            () -> new Curso().conCurso("Abierto").cu().actualizarCurso(cursoConId(3), estadoCurso(null, "Abierto")));
        caso("curso actualizar estado sin texto",
            () -> new Curso().conCurso("Abierto").cu().actualizarCurso(cursoConId(3), estadoCurso(1, null)));
        caso("curso preinscripcion sin publicar",
            () -> new Curso().conCurso("Publicado").cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Preinscripcion")));
        caso("curso preinscripcion sin solicitudes",
            () -> new Curso().conCurso("Abierto").cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Preinscripcion")));
        caso("curso preinscripcion bajo el minimo", () -> {
            var c = new Curso().conCurso("Abierto");
            when(c.solicitudes.buscarSolicitudPorNombreCursoPre(3)).thenReturn(List.of(new Solicitud()));
            return c.cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Preinscripcion"));
        });
        caso("curso preinscripcion sin asociar", () -> {
            var c = new Curso().conCurso("Abierto");
            when(c.solicitudes.buscarSolicitudPorNombreCursoPre(3))
                .thenReturn(List.of(conUsuario(new Solicitud(), 21, 5), conUsuario(new Solicitud(), 22, 6)));
            when(c.solicitudes.actualizarSolicitud(any(), any())).thenAnswer(i -> i.getArgument(0));
            when(c.usuarios.buscarUsuarioPorSolicitud(anyInt())).thenReturn(usuario(5, 2, 3));
            return c.cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Preinscripcion"));
        });
        caso("curso inscripcion sin publicar",
            () -> new Curso().conCurso("Preinscripcion").cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Inscripcion")));
        caso("curso inscripcion sin solicitudes",
            () -> new Curso().conCurso("Abierto").cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Inscripcion")));
        caso("curso inscripcion bajo el punto de equilibrio", () -> {
            var c = new Curso().conCurso("Abierto");
            when(c.solicitudes.buscarSolicitudPorNombreCursoIns(3)).thenReturn(List.of(new Solicitud()));
            return c.cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Inscripcion"));
        });
        caso("curso actualizar sin resultado",
            () -> new Curso().conCurso("Abierto").cu().actualizarCurso(cursoConId(3), estadoCurso(1, "Abierto")));

        caso("curso crear nulo", () -> new Curso().cu().crearCurso((CursoOfertadoVerano) null));
        caso("curso crear sin materia", () -> new Curso().cu().crearCurso(new CursoOfertadoVerano()));
        caso("curso crear materia sin id", () -> new Curso().cu().crearCurso(cursoNuevo(null, 4)));
        caso("curso crear materia inexistente", () -> new Curso().cu().crearCurso(cursoNuevo(8, 4)));
        caso("curso crear sin docente", () -> {
            var c = new Curso();
            when(c.materias.obtenerMateriaPorId(8)).thenReturn(new Materia());
            var curso = cursoNuevo(8, 4);
            curso.setObjDocente(null);
            return c.cu().crearCurso(curso);
        });
        caso("curso crear docente sin id", () -> {
            var c = new Curso();
            when(c.materias.obtenerMateriaPorId(8)).thenReturn(new Materia());
            return c.cu().crearCurso(cursoNuevo(8, null));
        });
        caso("curso crear docente inexistente", () -> {
            var c = new Curso();
            when(c.materias.obtenerMateriaPorId(8)).thenReturn(new Materia());
            return c.cu().crearCurso(cursoNuevo(8, 4));
        });
        caso("curso crear grupo en uso", () -> {
            var c = new Curso();
            when(c.materias.obtenerMateriaPorId(8)).thenReturn(new Materia());
            when(c.docentes.buscarDocentePorId(4)).thenReturn(new Docente());
            when(c.cursos.listarTodos()).thenReturn(List.of(cursoNuevo(8, 4)));
            return c.cu().crearCurso(cursoNuevo(8, 4));
        });
        caso("curso crear datos materia inexistente", () -> new Curso().cu().crearCurso(datosCurso(null)));
        caso("curso crear datos docente inexistente", () -> {
            var c = new Curso();
            when(c.materias.obtenerMateriaPorId(8)).thenReturn(new Materia());
            return c.cu().crearCurso(datosCurso(null));
        });
        caso("curso crear datos salon inexistente", () -> new Curso().cu().crearCurso(datosCurso(6)));
        caso("curso resolver salon inexistente", () -> new Curso().cu().resolverSalon(6, "Aula 1"));
        caso("curso eliminar inexistente", () -> new Curso().cu().eliminarCurso(3));
        caso("curso obtener inexistente", () -> new Curso().cu().obtenerCursoPorId(3));
    }

    /** Gateways simulados del caso de uso de cursos ofertados. */
    private static final class Curso {
        final GestionarCursoOfertadoVeranoGatewayIntPort cursos = mock(GestionarCursoOfertadoVeranoGatewayIntPort.class);
        final GestionarSolicitudGatewayIntPort solicitudes = mock(GestionarSolicitudGatewayIntPort.class);
        final GestionarUsuarioGatewayIntPort usuarios = mock(GestionarUsuarioGatewayIntPort.class);
        final GestionarMateriasIntPort materias = mock(GestionarMateriasIntPort.class);
        final GestionarDocenteGatewayIntPort docentes = mock(GestionarDocenteGatewayIntPort.class);
        final GestionarSalonesIntPort salones = mock(GestionarSalonesIntPort.class);

        Curso conCurso(String primerEstado) {
            var curso = cursoConId(3);
            curso.setCupo_estimado(5);
            curso.getEstadosCursoOfertados().add(estadoCurso(1, primerEstado));
            when(cursos.obtenerCursoPorId(3)).thenReturn(curso);
            return this;
        }

        GestionarCursoOfertadoVeranoCUAdapter cu() {
            return new GestionarCursoOfertadoVeranoCUAdapter(cursos, solicitudes, usuarios, materias, docentes, salones,
                mock(GestionarNotificacionCUIntPort.class));
        }
    }

    private static CursoOfertadoVerano cursoConId(Integer id) {
        var curso = new CursoOfertadoVerano();
        curso.setId_curso(id);
        return curso;
    }

    private static CursoOfertadoVerano cursoNuevo(Integer idMateria, Integer idDocente) {
        var materia = new Materia();
        materia.setId_materia(idMateria);
        var docente = new Docente();
        docente.setId_docente(idDocente);
        var curso = new CursoOfertadoVerano();
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);
        curso.setGrupo(GrupoCursoVerano.B);
        return curso;
    }

    private static EstadoCursoOfertado estadoCurso(Integer id, String estado) {
        var e = new EstadoCursoOfertado();
        e.setId_estado(id);
        e.setEstado_actual(estado);
        return e;
    }

    private static DatosCreacion datosCurso(Integer idSalon) {
        return new DatosCreacion(8, 4, 20, new Date(0), new Date(86_400_000L), "2025-2", idSalon, "Aula 1", "A", "Abierto");
    }

    // ---------------------------------------------------------------- solicitudes de cursos de verano

    private void solicitudesCursoVerano() {
        caso("preinscripcion nula", () -> new Verano().cu().crearSolicitudCursoVeranoPreinscripcion(null));
        caso("preinscripcion sin usuario",
            () -> new Verano().cu().crearSolicitudCursoVeranoPreinscripcion(new SolicitudCursoVeranoPreinscripcion()));
        caso("preinscripcion usuario sin id", () -> {
            var s = new SolicitudCursoVeranoPreinscripcion();
            s.setObjUsuario(new Usuario());
            return new Verano().cu().crearSolicitudCursoVeranoPreinscripcion(s);
        });
        caso("preinscripcion usuario inexistente", () -> new Verano().cu().crearSolicitudCursoVeranoPreinscripcion(preinscripcion(3)));
        caso("preinscripcion con documentos", () -> {
            var s = preinscripcion(3);
            s.setDocumentos(new ArrayList<>(List.of(new Documento())));
            return new Verano().conUsuario().cu().crearSolicitudCursoVeranoPreinscripcion(s);
        });
        caso("preinscripcion sin curso", () -> {
            var s = preinscripcion(3);
            s.setObjCursoOfertadoVerano(null);
            return new Verano().conUsuario().cu().crearSolicitudCursoVeranoPreinscripcion(s);
        });
        caso("preinscripcion curso sin id",
            () -> new Verano().conUsuario().cu().crearSolicitudCursoVeranoPreinscripcion(preinscripcion(null)));
        caso("preinscripcion curso inexistente",
            () -> new Verano().conUsuario().cu().crearSolicitudCursoVeranoPreinscripcion(preinscripcion(3)));
        caso("preinscripcion curso en otra fase",
            () -> new Verano().conUsuario().conCurso("Inscripcion").cu().crearSolicitudCursoVeranoPreinscripcion(preinscripcion(3)));
        caso("preinscripcion curso sin estado",
            () -> new Verano().conUsuario().conCurso(null).cu().crearSolicitudCursoVeranoPreinscripcion(preinscripcion(3)));
        caso("preinscripcion sin condicion", () -> {
            var s = preinscripcion(0);
            s.setCodicion_solicitud(null);
            return new Verano().conUsuario().cu().crearSolicitudCursoVeranoPreinscripcion(s);
        });
        caso("preinscripcion repetida", () -> {
            var v = new Verano().conUsuario().conCurso("Preinscripcion");
            when(v.solicitudes.buscarSolicitudesPorUsuarioYCursoPre(5, 3)).thenReturn(new Solicitud());
            return v.cu().crearSolicitudCursoVeranoPreinscripcion(preinscripcion(3));
        });

        caso("inscripcion nula", () -> new Verano().cu().crearSolicitudCursoVeranoInscripcion(null));
        caso("inscripcion sin usuario",
            () -> new Verano().cu().crearSolicitudCursoVeranoInscripcion(new SolicitudCursoVeranoInscripcion()));
        caso("inscripcion usuario sin id", () -> {
            var s = new SolicitudCursoVeranoInscripcion();
            s.setObjUsuario(new Usuario());
            return new Verano().cu().crearSolicitudCursoVeranoInscripcion(s);
        });
        caso("inscripcion usuario inexistente", () -> new Verano().cu().crearSolicitudCursoVeranoInscripcion(inscripcion(3)));
        caso("inscripcion sin curso", () -> {
            var s = inscripcion(3);
            s.setObjCursoOfertadoVerano(null);
            return new Verano().conUsuario().cu().crearSolicitudCursoVeranoInscripcion(s);
        });
        caso("inscripcion curso sin id", () -> new Verano().conUsuario().cu().crearSolicitudCursoVeranoInscripcion(inscripcion(null)));
        caso("inscripcion curso inexistente", () -> new Verano().conUsuario().cu().crearSolicitudCursoVeranoInscripcion(inscripcion(3)));
        caso("inscripcion curso en otra fase",
            () -> new Verano().conUsuario().conCurso("Preinscripcion").cu().crearSolicitudCursoVeranoInscripcion(inscripcion(3)));
        caso("inscripcion sin condicion", () -> {
            var s = inscripcion(3);
            s.setCodicion_solicitud(null);
            return new Verano().conUsuario().conCurso("Inscripcion").cu().crearSolicitudCursoVeranoInscripcion(s);
        });
        caso("inscripcion sin preinscripcion",
            () -> new Verano().conUsuario().conCurso("Inscripcion").cu().crearSolicitudCursoVeranoInscripcion(inscripcion(3)));
        caso("inscripcion preinscripcion sin aprobar", () -> {
            var v = new Verano().conUsuario().conCurso("Inscripcion");
            when(v.solicitudes.buscarSolicitudesPorUsuarioYCursoPre(5, 3)).thenReturn(conEstado(new Solicitud(), "Enviada"));
            return v.cu().crearSolicitudCursoVeranoInscripcion(inscripcion(3));
        });
        caso("inscripcion ya activa", () -> {
            var v = new Verano().conUsuario().conCurso("Inscripcion");
            when(v.solicitudes.buscarSolicitudesPorUsuarioYCursoPre(5, 3)).thenReturn(conEstado(new Solicitud(), "Aprobada"));
            when(v.solicitudes.buscarInscripcionesPorUsuarioYCurso(5, 3))
                .thenReturn(List.of(conEstado(new SolicitudCursoVeranoInscripcion(), "Enviada")));
            return v.cu().crearSolicitudCursoVeranoInscripcion(inscripcion(3));
        });

        caso("verano solicitudes por usuario sin id", () -> new Verano().cu().buscarSolicitudesPorUsuario(null));
        caso("verano solicitud por id sin id", () -> new Verano().cu().buscarSolicitudPorId(null));
        caso("verano inscripcion por id sin id", () -> new Verano().cu().buscarPorIdInscripcion(null));
        caso("verano preinscripciones por curso sin id", () -> new Verano().cu().buscarPreinscripcionesPorCurso(null));
        caso("verano inscripciones por curso sin id", () -> new Verano().cu().buscarInscripcionesPorCurso(null));
        caso("verano listar estudiantes sin curso", () -> new Verano().cu().listarEstudiantesPorCurso(null));

        caso("aprobar sin id", () -> new Verano().cu().aprobarPreinscripcion(null, "ok"));
        caso("aprobar inexistente", () -> new Verano().cu().aprobarPreinscripcion(30, "ok"));
        caso("aprobar en otro estado", () -> {
            var v = new Verano();
            when(v.solicitudes.buscarSolicitudPorId(30)).thenReturn(conEstado(new SolicitudCursoVeranoPreinscripcion(), "Aprobado"));
            return v.cu().aprobarPreinscripcion(30, "ok");
        });
        caso("rechazar sin id", () -> new Verano().cu().rechazarPreinscripcion(null, "no"));
        caso("rechazar sin motivo", () -> new Verano().cu().rechazarPreinscripcion(30, " "));
        caso("rechazar inexistente", () -> new Verano().cu().rechazarPreinscripcion(30, "no"));
        caso("rechazar en otro estado", () -> {
            var v = new Verano();
            when(v.solicitudes.buscarSolicitudPorId(30)).thenReturn(conEstado(new SolicitudCursoVeranoPreinscripcion(), "Rechazado"));
            return v.cu().rechazarPreinscripcion(30, "no");
        });
        caso("validar pago sin id", () -> new Verano().cu().validarPago(null, true, "ok"));
        caso("validar pago inexistente", () -> new Verano().cu().validarPago(31, true, "ok"));
        caso("validar pago en otro estado", () -> {
            var v = new Verano();
            when(v.solicitudes.buscarSolicitudInscripcionPorId(31)).thenReturn(conEstado(new SolicitudCursoVeranoInscripcion(), "Pago_Rechazado"));
            return v.cu().validarPago(31, true, "ok");
        });
        caso("validar pago sin estado", () -> {
            var v = new Verano();
            when(v.solicitudes.buscarSolicitudInscripcionPorId(31)).thenReturn(new SolicitudCursoVeranoInscripcion());
            return v.cu().validarPago(31, false, "no");
        });
        caso("completar sin id", () -> new Verano().cu().completarInscripcion(null));
        caso("completar inexistente", () -> new Verano().cu().completarInscripcion(31));
        caso("completar sin pago validado", () -> {
            var v = new Verano();
            when(v.solicitudes.buscarSolicitudInscripcionPorId(31)).thenReturn(conEstado(new SolicitudCursoVeranoInscripcion(), "Enviada"));
            return v.cu().completarInscripcion(31);
        });
        caso("observaciones preinscripcion nula", () -> new Verano().cu().actualizarObservacionesPreinscripcion(null));
        caso("observaciones preinscripcion sin id",
            () -> new Verano().cu().actualizarObservacionesPreinscripcion(new SolicitudCursoVeranoPreinscripcion()));
    }

    /** Gateways simulados del caso de uso de solicitudes de cursos de verano. */
    private static final class Verano {
        final GestionarSolicitudCursoVeranoGatewayIntPort solicitudes = mock(GestionarSolicitudCursoVeranoGatewayIntPort.class);
        final GestionarCursoOfertadoVeranoGatewayIntPort cursos = mock(GestionarCursoOfertadoVeranoGatewayIntPort.class);
        final GestionarUsuarioGatewayIntPort usuarios = mock(GestionarUsuarioGatewayIntPort.class);

        Verano conUsuario() {
            when(usuarios.obtenerUsuarioPorId(5)).thenReturn(usuario(5, 2, 3));
            return this;
        }

        Verano conCurso(String ultimoEstado) {
            var curso = cursoConId(3);
            curso.setCupo_estimado(5);
            if (ultimoEstado != null) {
                curso.getEstadosCursoOfertados().add(estadoCurso(1, "Abierto"));
                curso.getEstadosCursoOfertados().add(estadoCurso(2, ultimoEstado));
            }
            when(cursos.obtenerCursoPorId(3)).thenReturn(curso);
            return this;
        }

        GestionarSolicitudCursoVeranoCUAdapter cu() {
            return new GestionarSolicitudCursoVeranoCUAdapter(solicitudes, cursos, usuarios,
                mock(GestionarDocumentosGatewayIntPort.class), mock(GestionarArchivosCUIntPort.class),
                mock(GestionarNotificacionCUIntPort.class));
        }
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(Integer idCurso) {
        var s = conUsuario(new SolicitudCursoVeranoPreinscripcion(), null, 5);
        s.setObjCursoOfertadoVerano(cursoConId(idCurso));
        s.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        return s;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(Integer idCurso) {
        var s = conUsuario(new SolicitudCursoVeranoInscripcion(), null, 5);
        s.setObjCursoOfertadoVerano(cursoConId(idCurso));
        s.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        return s;
    }

    // ---------------------------------------------------------------- montaje de los casos de uso

    private static GestionarDocentesCUAdapter docentes(GestionarDocenteGatewayIntPort g) {
        return new GestionarDocentesCUAdapter(g);
    }

    private static GestionarProgramasCUAdapter programas(GestionarProgramaGatewayIntPort g) {
        return new GestionarProgramasCUAdapter(g);
    }

    private static GestionarRolesCUAdapter roles(GestionarRolGatewayIntPort g) {
        return new GestionarRolesCUAdapter(g);
    }

    private static GestionarMateriasCUAdapter materias(GestionarMateriasIntPort g) {
        return new GestionarMateriasCUAdapter(g);
    }

    private static GestionarSalonesCUAdapter salones(GestionarSalonesIntPort g) {
        return new GestionarSalonesCUAdapter(g);
    }

    private static GestionarSolicitudCUAdapter solicitud(GestionarSolicitudGatewayIntPort g) {
        return new GestionarSolicitudCUAdapter(g, mock(GestionarUsuarioGatewayIntPort.class));
    }

    private static GestionarArchivosCUAdapter archivos(GestionarArchivosGatewayIntPort g) {
        return new GestionarArchivosCUAdapter(g);
    }

    private static GestionarDocumentosCUAdapter documentos(GestionarDocumentosGatewayIntPort g) {
        return new GestionarDocumentosCUAdapter(g, mock(GestionarArchivosCUIntPort.class));
    }

    private static GestionarUsuarioCUAdapter usuarios(GestionarUsuarioGatewayIntPort g, GestionarProgramaGatewayIntPort p,
            GestionarRolGatewayIntPort r) {
        return new GestionarUsuarioCUAdapter(g, p, r, clave -> "codificada");
    }

    private static GestionarEstadisticasCUAdapter estadisticas(GestionarEstadisticasGatewayIntPort g) {
        return new GestionarEstadisticasCUAdapter(g);
    }

    private static GestionarSolicitudEcaesCUAdapter ecaes(GestionarPreRegistroEcaesGatewayIntPort g, GestionarUsuarioGatewayIntPort u) {
        return new GestionarSolicitudEcaesCUAdapter(g, mock(GestionarDocumentosGatewayIntPort.class),
            mock(GestionarEstadoSolicitudGatewayIntPort.class), u, mock(GestionarNotificacionCUIntPort.class),
            mock(GestionarArchivosCUIntPort.class));
    }

    private static GestionarSolicitudHomologacionCUAdapter homologacion(GestionarSolicitudHomologacionGatewayIntPort g,
            GestionarUsuarioGatewayIntPort u) {
        return new GestionarSolicitudHomologacionCUAdapter(g, u, mock(GestionarDocumentosGatewayIntPort.class),
            mock(GestionarEstadoSolicitudGatewayIntPort.class), mock(GestionarNotificacionCUIntPort.class),
            mock(GestionarArchivosCUIntPort.class));
    }

    private static GestionarSolicitudReingresoCUAdapter reingreso(GestionarSolicitudReingresoGatewayIntPort g,
            GestionarUsuarioGatewayIntPort u) {
        return new GestionarSolicitudReingresoCUAdapter(u, mock(GestionarDocumentosGatewayIntPort.class),
            mock(GestionarEstadoSolicitudGatewayIntPort.class), g, mock(GestionarNotificacionCUIntPort.class),
            mock(GestionarArchivosCUIntPort.class));
    }

    private static GestionarSolicitudPazYSalvoCUAdapter pazYSalvo(GestionarSolicitudPazYSalvoGatewayIntPort g) {
        return new GestionarSolicitudPazYSalvoCUAdapter(g, mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarDocumentosGatewayIntPort.class), mock(GestionarEstadoSolicitudGatewayIntPort.class),
            mock(GestionarArchivosCUIntPort.class), mock(GestionarNotificacionCUIntPort.class));
    }

    private static void normalizar(JsonNode nodo) {
        if (nodo.isObject()) {
            ObjectNode objeto = (ObjectNode) nodo;
            List<String> claves = new ArrayList<>();
            objeto.fieldNames().forEachRemaining(claves::add);
            for (String clave : claves) {
                if ((clave.startsWith("fecha") || clave.equals("timestamp")) && objeto.hasNonNull(clave)) {
                    objeto.put(clave, "<fecha>");
                }
            }
        }
        nodo.elements().forEachRemaining(ErroresCasosDeUsoCaracterizacionTest::normalizar);
    }
}
