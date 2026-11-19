package co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CodigoError;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class GestionCurricularException extends RuntimeException {

  protected CodigoError codigoError;

  /** Con el texto que devuelve {@code getMessage()}, además del código. */
  protected GestionCurricularException(final String mensaje, final CodigoError codigoError) {
    super(mensaje);
    this.codigoError = codigoError;
  }

  public abstract String formatException();
}
