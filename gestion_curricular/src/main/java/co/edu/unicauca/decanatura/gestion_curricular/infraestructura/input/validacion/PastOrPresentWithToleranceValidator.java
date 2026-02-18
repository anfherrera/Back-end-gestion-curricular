package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Calendar;
import java.util.Date;

public class PastOrPresentWithToleranceValidator implements ConstraintValidator<PastOrPresentWithTolerance, Date> {

    private int toleranceSeconds;

    @Override
    public void initialize(PastOrPresentWithTolerance annotation) {
        this.toleranceSeconds = annotation.toleranceSeconds();
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        Calendar now = Calendar.getInstance();
        Calendar maxAllowed = (Calendar) now.clone();
        maxAllowed.add(Calendar.SECOND, toleranceSeconds);
        return !value.after(maxAllowed.getTime());
    }
}
