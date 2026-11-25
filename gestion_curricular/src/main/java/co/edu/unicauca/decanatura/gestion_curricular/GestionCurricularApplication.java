package co.edu.unicauca.decanatura.gestion_curricular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * El escaneo empieza en {@code co.edu.unicauca.decanatura}, como antes, pero con los filtros de
 * {@code @SpringBootApplication}: sin ellos entraban en el contexto las {@code @TestConfiguration} anidadas de las
 * pruebas (OPC-4).
 */
@SpringBootApplication(scanBasePackages = "co.edu.unicauca.decanatura")
public class GestionCurricularApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionCurricularApplication.class, args);
	}
}
