
package co.edu.unicauca.decanatura.gestion_curricular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan(basePackages = "co.edu.unicauca.decanatura")
public class GestionCurricularApplication implements CommandLineRunner {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(GestionCurricularApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Verificar que los datos del import.sql se hayan cargado correctamente
		try {
			System.out.println("==========================================");
			System.out.println("VERIFICACIÓN DE CARGA DE DATOS INICIALES");
			System.out.println("==========================================");
			System.out.println("Perfil activo: " + String.join(", ", environment.getActiveProfiles()));
			System.out.println("DDL Auto: " + environment.getProperty("spring.jpa.hibernate.ddl-auto"));
			System.out.println("SQL Init Mode: " + environment.getProperty("spring.sql.init.mode"));
			System.out.println("SQL Init Data Locations: " + environment.getProperty("spring.sql.init.data-locations"));
			
			// Esperar un momento para que se complete la inicialización
			Thread.sleep(2000);
			
			// Verificar que las tablas existan
			boolean tablasExisten = verificarTablasExisten();
			if (!tablasExisten) {
				System.out.println("❌ ERROR: Las tablas no existen. Verificar configuración de base de datos.");
				return;
			}
			
			// Verificar conteo de solicitudes
			Integer countSolicitudes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Solicitudes", Integer.class);
			System.out.println("📊 Total de solicitudes cargadas: " + countSolicitudes);
			
			// Verificar conteo de estados de solicitudes
			Integer countEstados = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EstadosSolicitudes", Integer.class);
			System.out.println("📊 Total de estados de solicitudes cargados: " + countEstados);
			
			// Verificar conteo de usuarios
			Integer countUsuarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
			System.out.println("📊 Total de usuarios cargados: " + countUsuarios);
			
			// Verificar conteo de programas
			Integer countProgramas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM programas", Integer.class);
			System.out.println("📊 Total de programas cargados: " + countProgramas);
			
			// Verificar conteo de materias
			Integer countMaterias = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM materias", Integer.class);
			System.out.println("📊 Total de materias cargadas: " + countMaterias);
			
			if (countSolicitudes == 0 || countEstados == 0) {
				System.out.println("⚠️  ADVERTENCIA: Los datos del import.sql NO se ejecutaron correctamente");
				System.out.println("   - Verificar que spring.jpa.hibernate.ddl-auto=create-drop");
				System.out.println("   - Verificar que spring.sql.init.data-locations=classpath:import.sql");
				System.out.println("   - Verificar que el archivo import.sql esté en src/main/resources");
				System.out.println("   - Verificar logs de 'spring.boot.sql.init' para errores");
			} else {
				System.out.println("✅ Los datos del import.sql se cargaron correctamente");
				System.out.println("   - El endpoint /api/estadisticas/globales debería mostrar datos");
				System.out.println("   - Total de registros esperados: 46 solicitudes, 46 estados");
			}
			
			System.out.println("==========================================");
			
		} catch (Exception e) {
			System.out.println("❌ ERROR al verificar datos iniciales: " + e.getMessage());
			e.printStackTrace();
			System.out.println("   - Verificar que la base de datos esté conectada");
			System.out.println("   - Verificar que las tablas existan");
			System.out.println("   - Verificar configuración de import.sql");
		}
	}
	
	private boolean verificarTablasExisten() {
		try {
			// Verificar que las tablas principales existan
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'Solicitudes'", Integer.class);
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'EstadosSolicitudes'", Integer.class);
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'usuarios'", Integer.class);
			return true;
		} catch (Exception e) {
			System.out.println("❌ ERROR: No se pudieron verificar las tablas: " + e.getMessage());
			return false;
		}
	}
}
