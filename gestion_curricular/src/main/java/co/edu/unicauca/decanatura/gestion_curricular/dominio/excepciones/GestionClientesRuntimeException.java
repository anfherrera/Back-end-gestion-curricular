package co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CodigoError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class GestionClientesRuntimeException extends RuntimeException {

  protected CodigoError codigoError;

  public abstract String formatException();
}
