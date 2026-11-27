package co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CodigoError {

        ERROR_GENERICO("GC-0001", "ERROR GENERICO"),
        ENTIDAD_YA_EXISTE("GC-0002", "ERROR ENTIDAD YA EXISTE"),
        ENTIDAD_NO_ENCONTRADA("GC-0003", "Entidad no encontrada"),
        VIOLACION_REGLA_DE_NEGOCIO("GC-0004", "Regla de negocio violada"),
        CREDENCIALES_INVALIDAS("GC-0005", "Error al iniciar sesión, compruebe sus credenciales y vuelva a intentarlo"),
        USUARIO_DESHABILITADO("GC-0006", "Su cuenta está desactivada. Contacte al administrador."),
        INICIO_SESION_RECHAZADO("GC-0007", "No fue posible iniciar sesión con la cuenta institucional");

        private final String codigo;
        private final String llaveMensaje;
}
