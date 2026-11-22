package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

class DescargasCaracterizacionTest {
    @TempDir Path temporal;

    @Test
    void conservaCabecerasYBytesEnStreamingRecursosYArrays() throws Exception {
        byte[] contenido = "%PDF-contenido fijo".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Path archivo = temporal.resolve("archivo.pdf");
        Files.write(archivo, contenido);
        var archivos = mock(GestionarArchivosCUIntPort.class);
        when(archivos.getFileAsPath(anyString())).thenReturn(archivo);
        when(archivos.getFileByPathAsPath(anyString())).thenReturn(archivo);
        when(archivos.getFile(anyString())).thenReturn(contenido);
        when(archivos.getFileByPath(anyString())).thenReturn(contenido);
        var verano = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        var homo = mock(GestionarSolicitudHomologacionCUIntPort.class);
        var reingreso = mock(GestionarSolicitudReingresoCUIntPort.class);
        var auth = new Autorizacion(null, null, null, null) {
            @Override public Optional<String> rutaArchivoVisible(String nombre) { return Optional.of("carpeta/" + nombre); }
        };
        var mvc = SoporteMvc.standaloneSetup(
            new ArchivosRestController(archivos, mock(GestionarDocumentosCUIntPort.class), mock(GestionarSolicitudPazYSalvoCUIntPort.class), verano, mock(GestionarSolicitudCUIntPort.class), auth),
            new SolicitudHomologacionRestController(homo, mock(SolicitudHomologacionMapperDominio.class), mock(SolicitudMapperDominio.class), archivos, mock(GestionarDocumentosCUIntPort.class), auth, null),
            new SolicitudReingresoRestController(mock(SolicitudReingresoMapperDominio.class), reingreso, mock(SolicitudMapperDominio.class), archivos, auth, null),
            new InscripcionesVeranoRestController(mock(GestionarCursoOfertadoVeranoCUIntPort.class), verano, archivos, mock(GestionarSolicitudCUIntPort.class)))
            .build();
        var json = new ObjectMapper();
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (String nombre : new String[]{"oficio.pdf", "oficio á +\".pdf"}) {
            var documento = new Documento();
            documento.setNombre(nombre);
            documento.setRutaDocumento("carpeta/" + nombre);
            documento.setComentario("[OFICIO_HOMOLOGACION]");
            var inscripcion = new SolicitudCursoVeranoInscripcion();
            inscripcion.setDocumentos(List.of(documento));
            when(verano.buscarPorIdInscripcion(7)).thenReturn(inscripcion);
            var solicitudH = new SolicitudHomologacion();
            solicitudH.setDocumentos(List.of(documento));
            when(homo.buscarPorId(7)).thenReturn(solicitudH);
            var solicitudR = new SolicitudReingreso();
            solicitudR.setDocumentos(List.of(documento));
            when(reingreso.obtenerSolicitudReingresoPorId(7)).thenReturn(solicitudR);
            // La clave conserva la ruta con la que se grabó la referencia; el paso 3 de EST-7 cambió las dos de archivos
            Map<String, String> rutas = new LinkedHashMap<>();
            rutas.put("/api/archivos/descargar/pdf", "/api/archivos?nombre=");
            rutas.put("/api/archivos/descargar/pdf/inscripcion/7", "/api/archivos/inscripciones/7");
            rutas.put("/api/cursos-intersemestrales/inscripciones/7/comprobante", "/api/cursos-intersemestrales/inscripciones/7/comprobante");
            rutas.put("/api/solicitudes-homologacion/descargarOficio/7", "/api/solicitudes-homologacion/descargarOficio/7");
            rutas.put("/api/solicitudes-reingreso/descargarOficio/7", "/api/solicitudes-reingreso/descargarOficio/7");
            for (var entrada : rutas.entrySet()) {
                String ruta = entrada.getKey();
                var peticion = entrada.getValue().endsWith("?nombre=")
                    ? get(entrada.getValue().replace("?nombre=", "")).param("nombre", nombre)
                    : get(entrada.getValue()).param("filename", nombre);
                var resultado = mvc.perform(peticion).andReturn();
                if (resultado.getRequest().isAsyncStarted()) {
                    resultado.getAsyncResult(10000);
                    resultado = mvc.perform(asyncDispatch(resultado)).andReturn();
                }
                var respuesta = resultado.getResponse();
                Map<String, List<String>> cabeceras = new TreeMap<>();
                for (String clave : respuesta.getHeaderNames()) cabeceras.put(clave, new ArrayList<>(respuesta.getHeaders(clave)));
                assertThat(respuesta.getStatus()).isEqualTo(200);
                assertThat(respuesta.getContentAsByteArray()).isEqualTo(contenido);
                resultados.put(ruta + " " + nombre, Map.of("status", respuesta.getStatus(), "headers", cabeceras,
                    "body", Base64.getEncoder().encodeToString(respuesta.getContentAsByteArray())));
            }
        }
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/documentos/descargas.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
