-- Migración: dueño de los documentos subidos (MySQL)
--
-- Producción usa spring.jpa.hibernate.ddl-auto=validate, así que esta columna debe
-- crearse ANTES de desplegar el backend nuevo, o la aplicación no arranca.
--
-- Motivo: al crear una solicitud se asociaban TODOS los documentos sin solicitud del
-- sistema, y el PDF de un estudiante podía terminar en la solicitud de otro. Ahora solo
-- se asocian los documentos que subió el dueño de la solicitud.

ALTER TABLE Documentos ADD COLUMN idUsuarioSubio INT NULL;

-- Los documentos que hoy están sin solicitud no tienen dueño conocido y ya no se
-- asociarán automáticamente a ninguna solicitud. Para revisarlos:
--   SELECT idDocumento, nombre, ruta_documento, fecha_documento
--   FROM Documentos WHERE idSolicitud IS NULL;
