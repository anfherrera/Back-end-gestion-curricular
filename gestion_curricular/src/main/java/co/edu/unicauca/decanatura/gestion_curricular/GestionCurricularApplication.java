package co.edu.unicauca.decanatura.gestion_curricular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "co.edu.unicauca.decanatura")
@EnableCaching
public class GestionCurricularApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionCurricularApplication.class, args);
	}
}









