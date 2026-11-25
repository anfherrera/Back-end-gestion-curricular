package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.constraints.Min;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * BUG-50: un controlador sin {@code @Validated} con restricciones en sus parámetros hace que Spring lance
 * {@code HandlerMethodValidationException}. El manejador global responde 400 GC-0004 con el mismo formato de mensaje
 * que una {@code ConstraintViolationException} ({@code metodo.parametro: mensaje}), y no 500.
 */
@DisplayName("Manejador global: validación de parámetros sin @Validated (BUG-50)")
class ValidacionSinValidatedTest {

    /**
     * Solo para {@code standaloneSetup}, que lo registra porque recibe la instancia. El perfil, que nunca se activa,
     * lo dejaba fuera de los contextos de Spring cuando el escaneo de la aplicación incluía las clases de prueba;
     * desde OPC-4 ya no las incluye, y el perfil se conserva.
     */
    @RestController
    @Profile("solo-standalone")
    static class ControladorSinValidated {
        @GetMapping("/prueba-validacion")
        ResponseEntity<String> porNumero(@Min(1) @RequestParam("numero") Integer numero) {
            return ResponseEntity.ok("ok " + numero);
        }
    }

    private final MockMvc mvc = SoporteMvc.standaloneSetup(new ControladorSinValidated())
            .setControllerAdvice(new RestApiExceptionHandler()).build();

    @Test
    @DisplayName("Un parámetro que viola su restricción responde 400 GC-0004 y no 500")
    void parametroInvalidoResponde400() throws Exception {
        mvc.perform(get("/prueba-validacion").param("numero", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                .andExpect(jsonPath("$.codigoHttp").value(400))
                .andExpect(jsonPath("$.mensaje", Matchers.startsWith("porNumero.numero: ")));
    }

    @Test
    @DisplayName("Un parámetro válido no se ve afectado")
    void parametroValido() throws Exception {
        mvc.perform(get("/prueba-validacion").param("numero", "3"))
                .andExpect(status().isOk());
    }
}
