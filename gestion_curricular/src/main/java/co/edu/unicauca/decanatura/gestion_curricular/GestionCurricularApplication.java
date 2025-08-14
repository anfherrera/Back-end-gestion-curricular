package co.edu.unicauca.decanatura.gestion_curricular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "co.edu.unicauca.decanatura")
public class GestionCurricularApplication {
//hola
	public static void main(String[] args) {
		SpringApplication.run(GestionCurricularApplication.class, args);
	}

}
