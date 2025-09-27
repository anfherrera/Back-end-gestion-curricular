package co.edu.unicauca.decanatura.gestion_curricular;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TestCursosVerano {

    @Test
    public void contextLoads() {
        // Test básico para verificar que la aplicación arranca
        System.out.println("✅ Aplicación arrancó correctamente");
    }
}
