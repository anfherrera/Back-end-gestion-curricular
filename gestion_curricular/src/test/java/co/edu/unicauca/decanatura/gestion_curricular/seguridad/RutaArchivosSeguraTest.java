package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarArchivosGatewayImplAdapter;

@DisplayName("Seguridad - Rutas de archivos")
class RutaArchivosSeguraTest {

    @Test
    @DisplayName("No permite leer archivos fuera de la carpeta Archivos con ../")
    void rechazaPathTraversal() throws IOException {
        GestionarArchivosGatewayImplAdapter gateway = new GestionarArchivosGatewayImplAdapter();

        assertThrows(IOException.class, () -> gateway.getFile("../pom.xml"));
        assertThrows(IOException.class, () -> gateway.getFile("paz_y_salvo/../../pom.xml"));
        assertThrows(IOException.class, () -> gateway.getFileByPath("../../../../Windows/win.ini"));
        assertThrows(IOException.class, () -> gateway.getFileAsPath("..\\pom.xml"));
    }

    @Test
    @DisplayName("No permite rutas absolutas")
    void rechazaRutaAbsoluta() throws IOException {
        GestionarArchivosGatewayImplAdapter gateway = new GestionarArchivosGatewayImplAdapter();
        String absoluta = new java.io.File("pom.xml").getAbsolutePath();

        assertThrows(IOException.class, () -> gateway.getFile(absoluta));
    }
}
