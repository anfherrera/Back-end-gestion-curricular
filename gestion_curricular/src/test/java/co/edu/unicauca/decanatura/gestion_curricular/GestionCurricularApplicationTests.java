package co.edu.unicauca.decanatura.gestion_curricular;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Perfil "test": usa H2 en memoria y no depende de un MySQL local
@SpringBootTest
@ActiveProfiles("test")
class GestionCurricularApplicationTests {

	@Test
	void contextLoads() {
	}

}
