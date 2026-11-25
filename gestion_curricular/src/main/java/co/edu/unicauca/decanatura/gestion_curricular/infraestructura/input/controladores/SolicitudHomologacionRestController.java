package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudHomologacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosSolicitudDTORespuesta.Oficio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosSolicitudDTORespuesta.ResolucionSubida;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RechazoCambioEstadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudHomologacionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudHomologacionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ArchivoMultipart;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;




@RestController
@RequestMapping("/api/solicitudes-homologacion")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudHomologacionRestController {
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final SolicitudHomologacionMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final Autorizacion autorizacion;
    private final ValidadorCambioEstado validadorCambioEstado;
    private static final String MARCADOR_OFICIO_HOMOLOGACION = "[OFICIO_HOMOLOGACION]";

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping
    public ResponseEntity<SolicitudHomologacionDTORespuesta> crearSolicitudHomologacion(@Valid @RequestBody SolicitudHomologacionDTOPeticion peticion) {
        // El solicitante sale del token, no de lo que envía el cliente
        peticion.getObjUsuario().setId_usuario(autorizacion.idSolicitante(peticion.getObjUsuario().getId_usuario()));
        SolicitudHomologacion solicitud = solicitudMapperDominio.mapearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(peticion);
        SolicitudHomologacion solicitudCreada = solicitudHomologacionCU.guardar(solicitud);
        ResponseEntity<SolicitudHomologacionDTORespuesta> respuesta = new ResponseEntity<>
        (solicitudMapperDominio.mapearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    /**
     * Sin {@code rol} no hay listado: responde el mismo 400 que da un parámetro obligatorio que falta. El listado
     * completo sin filtros se retiró en EST-7 (no tenía uso).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Void> listarSinRol() throws MissingServletRequestParameterException {
        throw new MissingServletRequestParameterException("rol", "String");
    }

    /**
     * Bandeja de un rol (EST-7): {@code bandeja=pendientes} son las que esperan su revisión (antes
     * {@code /listarSolicitud-Homologacion/{Rol}}) y {@code bandeja=procesadas}, las que ya aprobó alguna vez (antes
     * {@code /{Rol}/Aprobadas}).
     *
     * @param rol              funcionario, coordinador o secretaria
     * @param bandeja          pendientes o procesadas
     * @param periodoAcademico opcional ("2025-2"). En pendientes, si falta, el actual; en procesadas, vacío o
     *                         "todos" muestra todos los períodos. El coordinador sin programa ve todas, de todos los
     *                         períodos.
     */
    @GetMapping(params = {"rol", "bandeja"})
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarBandeja(
            @RequestParam String rol,
            @RequestParam String bandeja,
            @RequestParam(required = false) String periodoAcademico) {
        boolean procesadas = switch (bandeja) {
            case "pendientes" -> false;
            case "procesadas" -> true;
            default -> throw new IllegalArgumentException("Bandeja no válida: use pendientes o procesadas");
        };
        String paso = switch (rol) {
            case "funcionario" -> FlujoEstadosSolicitud.FUNCIONARIO;
            case "coordinador" -> FlujoEstadosSolicitud.COORDINADOR;
            case "secretaria" -> FlujoEstadosSolicitud.SECRETARIO;
            default -> throw new IllegalArgumentException("Rol no válido: use funcionario, coordinador o secretaria");
        };
        String periodo = procesadas ? PeriodoDeConsulta.oTodos(periodoAcademico)
                : PeriodoDeConsulta.oActual(periodoAcademico);
        Integer idPrograma = null;
        if (FlujoEstadosSolicitud.COORDINADOR.equals(paso)) {
            idPrograma = autorizacion.idProgramaCoordinadorActual();
            if (idPrograma == null) {
                // Si no se puede obtener el programa, se devuelven todas, de todos los períodos (fallback)
                log.warn("No se pudo obtener el programa del coordinador, retornando todas las solicitudes");
                periodo = null;
            }
        }
        return responder(solicitudHomologacionCU.listar(procesadas
                ? FiltroSolicitudes.procesadasPor(paso, idPrograma, periodo)
                : FiltroSolicitudes.pendientesDe(paso, idPrograma, periodo)));
    }

    /** Antes {@code /listarSolicitud-Homologacion/porRol}. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = {"rol", "!bandeja"})
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudPorRol(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String periodoAcademico) {

        // Quien no es staff solo puede ver sus propias solicitudes
        if (!autorizacion.esStaff()) {
            rol = "ESTUDIANTE";
            idUsuario = autorizacion.idUsuarioActual();
            if (idUsuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        String periodo = PeriodoDeConsulta.oActual(periodoAcademico);
        if ("ESTUDIANTE".equals(rol) && idUsuario != null && periodo != null) {
            return responder(solicitudHomologacionCU.listar(FiltroSolicitudes.deEstudiante(idUsuario, periodo)));
        }
        return responder(solicitudHomologacionCU.listarSolicitudesPorRol(rol, idUsuario));
    }

    private ResponseEntity<List<SolicitudHomologacionDTORespuesta>> responder(List<SolicitudHomologacion> solicitudes) {
        return ResponseEntity.ok(solicitudMapperDominio.mapearListaDeSolicitudHomologacionARespuesta(solicitudes));
    }

    /** La solicitud es la de la ruta: un {@code idSolicitud} en el cuerpo se ignora. */
    @PutMapping("/{idSolicitud:\\d+}/estado")
    public ResponseEntity<RechazoCambioEstadoDTORespuesta> actualizarEstadoSolicitudHomologacion(
            @PathVariable Integer idSolicitud, @RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        peticion.setIdSolicitud(idSolicitud);
        CambioEstadoSolicitud solicitud = solicitudMapper.mapearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        // Solo el rol responsable del paso puede hacerlo (Funcionario → Coordinador → Secretario)
        String estadoActual = FlujoEstadosSolicitud.ultimoEstado(
                solicitudHomologacionCU.buscarPorId(solicitud.getIdSolicitud()).getEstadosSolicitud());
        ResponseEntity<RechazoCambioEstadoDTORespuesta> rechazo = validadorCambioEstado.validar(
                FlujoEstadosSolicitud.TRES_APROBACIONES, estadoActual, solicitud.getNuevoEstado());
        if (rechazo != null) {
            return rechazo;
        }
        solicitudHomologacionCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    /**
     * Descargar oficio por ID de solicitud
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/{idSolicitud:\\d+}/oficios/archivo")
    public ResponseEntity<byte[]> descargarOficio(@PathVariable Integer idSolicitud) {
        // Obtener la solicitud con sus documentos
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Prioridad 1: documentos marcados explícitamente como oficio/resolución de homologación (subidos por secretaría)
        Documento documentoSeleccionado = null;
        for (Documento d : documentos) {
            if (d == null) continue;
            String comentario = d.getComentario();
            if (comentario != null && comentario.contains(MARCADOR_OFICIO_HOMOLOGACION)) {
                if (documentoSeleccionado == null) {
                    documentoSeleccionado = d;
                } else {
                    Date fSel = documentoSeleccionado.getFechaDocumento();
                    Date fNew = d.getFechaDocumento();
                    if (fSel == null && fNew != null) {
                        documentoSeleccionado = d;
                    } else if (fSel != null && fNew != null && fNew.after(fSel)) {
                        documentoSeleccionado = d;
                    }
                }
            }
        }

        // Prioridad 2 (fallback): heurística por nombre (resolucion/oficio) excluyendo formulario del estudiante
        if (documentoSeleccionado == null) {
            for (Documento d : documentos) {
                if (d != null && d.getNombre() != null && esDocumentoOficioHomologacion(d)) {
                    documentoSeleccionado = d;
                    break;
                }
            }
        }

        if (documentoSeleccionado == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String nombreDescarga = documentoSeleccionado.getNombre();
            String rutaDocumento = documentoSeleccionado.getRutaDocumento() != null
                    ? documentoSeleccionado.getRutaDocumento()
                    : nombreDescarga;
            byte[] archivo;
            if (rutaDocumento != null && rutaDocumento.contains("/")) {
                archivo = gestionarArchivos.getFileByPath(rutaDocumento);
            } else {
                archivo = gestionarArchivos.getFile(nombreDescarga);
            }
            String contentDisposition = RespuestasArchivo.adjunto(nombreDescarga);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(archivo);
        } catch (Exception e) {
            log.debug("No se pudo cargar el oficio/resolución seleccionado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtener oficios disponibles para una solicitud
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/{idSolicitud:\\d+}/oficios")
    public ResponseEntity<List<Oficio>> obtenerOficios(@PathVariable Integer idSolicitud) {
        // Obtener la solicitud con sus documentos
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
        }
        
        // Crear lista de oficios basada en los documentos reales.
        // Prioridad: marcados explícitamente como oficio/resolución de homologación, fallback a heurística por nombre.
        List<Oficio> oficios = new ArrayList<>();
        for (Documento documento : documentos) {
            if (documento == null || documento.getNombre() == null) continue;
            boolean esMarcado = documento.getComentario() != null
                    && documento.getComentario().contains(MARCADOR_OFICIO_HOMOLOGACION);
            if (esMarcado || esDocumentoOficioHomologacion(documento)) {
                oficios.add(new Oficio(idSolicitud, documento.getNombre(), documento.getNombre(),
                        documento.getRutaDocumento()));
            }
        }
        
        return ResponseEntity.ok(oficios);
    }

    /**
     * Indica si el documento es un oficio/resolución (subido por secretaría).
     * Prioridad: si el comentario indica "secretaría", se considera oficio (cualquier nombre).
     * Fallback: por nombre (resolucion/oficio) excluyendo formulario del estudiante (solicitud+homologacion).
     */
    private static boolean esDocumentoOficioHomologacion(Documento documento) {
        if (documento == null) return false;
        // Nota: NO usar "secretar" como heurística: los comentarios de documentos pueden incluir roles (SECRETARIA)
        // por el historial de comentarios acumulativos, lo que produce falsos positivos.
        String nombre = documento.getNombre();
        if (nombre == null) return false;
        String nombreLower = nombre.toLowerCase(Locale.ROOT);
        boolean pareceFormularioEstudiante = nombreLower.contains("solicitud") && nombreLower.contains("homologacion");
        boolean esResolucionUOficio = nombreLower.contains("resolucion") || nombreLower.contains("oficio");
        return esResolucionUOficio && !pareceFormularioEstudiante;
    }

    /**
     * Subir resolución de homologación (secretaría). El documento se marca con comentario
     * "Archivo subido por secretaría" para que se liste y permita descargar al estudiante,
     * independiente del nombre del archivo.
     */
    @PostMapping("/{idSolicitud:\\d+}/resoluciones")
    public ResponseEntity<?> subirResolucion(
            @PathVariable Integer idSolicitud,
            @RequestParam("file") MultipartFile file) {
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        ValidadorArchivoPdf.Rechazo rechazo = ValidadorArchivoPdf.validar(file, ValidadorArchivoPdf.MAXIMO_10MB);
        if (rechazo != null) {
            return ResponseEntity.status(rechazo.estado()).body(Map.of("error", rechazo.mensaje()));
        }
        String nombreOriginal = file.getOriginalFilename();
        Documento guardado = documentosCU.adjuntarASolicitud(ArchivoMultipart.de(file), solicitud,
                "homologacion", MARCADOR_OFICIO_HOMOLOGACION + " Archivo subido por secretaría", true);
        return ResponseEntity.ok(new ResolucionSubida(true, "Resolución subida correctamente",
                guardado.getIdDocumento(), guardado.getNombre(), idSolicitud));
    }

}
