package co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones;

import lombok.Getter;


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
