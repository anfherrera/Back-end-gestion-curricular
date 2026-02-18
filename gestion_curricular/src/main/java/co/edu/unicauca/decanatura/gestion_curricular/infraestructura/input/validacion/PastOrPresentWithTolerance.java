package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que una fecha sea pasada o actual, permitiendo una tolerancia en segundos
 * para evitar fallos por diferencia de zona horaria o reloj entre cliente y servidor (ej. despliegue en UTC).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PastOrPresentWithToleranceValidator.class)
@Documented
public @interface PastOrPresentWithTolerance {

    String message() default "{Solicitud.fecha.pastorpresent}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Tolerancia en segundos: se acepta la fecha si no es más de este tiempo "en el futuro" respecto al servidor. Por defecto 120 (2 minutos). */
    int toleranceSeconds() default 120;
}
