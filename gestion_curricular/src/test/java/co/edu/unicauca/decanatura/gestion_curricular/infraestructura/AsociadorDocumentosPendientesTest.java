package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;

class AsociadorDocumentosPendientesTest {

    private GestionarDocumentosGatewayIntPort documentos;
    private GestionarArchivosCUIntPort archivos;
    private AsociadorDocumentosPendientes asociador;
    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        documentos = mock(GestionarDocumentosGatewayIntPort.class);
        archivos = mock(GestionarArchivosCUIntPort.class);
        asociador = new AsociadorDocumentosPendientes(documentos, archivos);
        solicitud = new Solicitud();
        solicitud.setId_solicitud(10);
    }

    private static Documento documento(String nombre, String ruta) {
        Documento d = new Documento();
        d.setNombre(nombre);
        d.setRuta_documento(ruta);
        return d;
    }

    @Test
    @DisplayName("Mueve los documentos de la raíz a la carpeta de la solicitud y los asocia")
    void mueveYAsocia() {
        Documento enRaiz = documento("a.pdf", "a.pdf");
        when(documentos.buscarDocumentosSinSolicitud(7)).thenReturn(List.of(enRaiz));
        when(archivos.moverArchivoAOrganizado("a.pdf", "a.pdf", "homologacion", 10))
                .thenReturn("homologacion/solicitud_10/a.pdf");

        int asociados = asociador.asociar(solicitud, 7, "homologacion");

        assertThat(asociados).isEqualTo(1);
        assertThat(enRaiz.getObjSolicitud()).isSameAs(solicitud);
        assertThat(enRaiz.getRuta_documento()).isEqualTo("homologacion/solicitud_10/a.pdf");
        verify(documentos).actualizarDocumento(enRaiz);
    }

    @Test
    @DisplayName("Solo pide los documentos del dueño de la solicitud")
    void soloDocumentosDelDueno() {
        when(documentos.buscarDocumentosSinSolicitud(7)).thenReturn(List.of());

        asociador.asociar(solicitud, 7, "reingreso");

        verify(documentos).buscarDocumentosSinSolicitud(7);
        verify(documentos, never()).actualizarDocumento(any());
    }

    @Test
    @DisplayName("No mueve documentos que ya están en una carpeta")
    void noMueveLosYaOrganizados() {
        Documento organizado = documento("b.pdf", "ecaes/solicitud_3/b.pdf");
        when(documentos.buscarDocumentosSinSolicitud(7)).thenReturn(List.of(organizado));

        asociador.asociar(solicitud, 7, "ecaes");

        verify(archivos, never()).moverArchivoAOrganizado(anyString(), anyString(), anyString(), anyInt());
        verify(documentos, times(1)).actualizarDocumento(organizado);
    }

    @Test
    @DisplayName("Si no se puede mover el archivo, igual lo asocia con su ruta actual")
    void toleraErrorAlMover() {
        Documento enRaiz = documento("c.pdf", "c.pdf");
        when(documentos.buscarDocumentosSinSolicitud(7)).thenReturn(List.of(enRaiz));
        when(archivos.moverArchivoAOrganizado(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("archivo no encontrado"));

        asociador.asociar(solicitud, 7, "pazysalvo");

        assertThat(enRaiz.getObjSolicitud()).isSameAs(solicitud);
        assertThat(enRaiz.getRuta_documento()).isEqualTo("c.pdf");
        verify(documentos).actualizarDocumento(enRaiz);
    }
}
