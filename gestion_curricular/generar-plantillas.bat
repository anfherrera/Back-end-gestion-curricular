@echo off
echo ========================================
echo    CREANDO PLANTILLAS WORD
echo ========================================
echo.

echo Creando plantilla de homologacion...
echo UNIVERSIDAD DEL CAUCA > "src\main\resources\templates\oficio-homologacion.docx"
echo FACULTAD DE INGENIERIA ELECTRONICA Y TELECOMUNICACIONES >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo [TITULO_DOCUMENTO] >> "src\main\resources\templates\oficio-homologacion.docx"
echo No. [NUMERO_DOCUMENTO] >> "src\main\resources\templates\oficio-homologacion.docx"
echo Fecha: [FECHA_DOCUMENTO] >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo Por la cual se resuelve la solicitud de [TIPO_PROCESO] >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo EL COORDINADOR ACADEMICO DE LA [FACULTAD] >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo En uso de sus facultades legales y reglamentarias, y >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo CONSIDERANDO: >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo 1. Que el estudiante [NOMBRE_ESTUDIANTE] con codigo [CODIGO_ESTUDIANTE] del programa de [PROGRAMA] ha presentado solicitud de [TIPO_PROCESO]. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo 2. Que la solicitud ha sido revisada y aprobada por los funcionarios competentes. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo 3. Que se cumplen todos los requisitos establecidos en el reglamento estudiantil. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo 4. Que la solicitud fue presentada el [FECHA_SOLICITUD]. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo RESUELVE: >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo ARTICULO PRIMERO: Aprobar la [TIPO_PROCESO] solicitada por el estudiante [NOMBRE_ESTUDIANTE] con codigo [CODIGO_ESTUDIANTE]. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo ARTICULO SEGUNDO: La presente resolucion rige a partir de la fecha de su expedicion. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo ARTICULO TERCERO: Comuniquese y cumplase. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo Dada en [CIUDAD], a los [FECHA_ACTUAL]. >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo [OBSERVACIONES] >> "src\main\resources\templates\oficio-homologacion.docx"
echo. >> "src\main\resources\templates\oficio-homologacion.docx"
echo _________________________ >> "src\main\resources\templates\oficio-homologacion.docx"
echo COORDINADOR ACADEMICO >> "src\main\resources\templates\oficio-homologacion.docx"
echo [FACULTAD] >> "src\main\resources\templates\oficio-homologacion.docx"
echo [NOMBRE_UNIVERSIDAD] >> "src\main\resources\templates\oficio-homologacion.docx"

echo.
echo Creando plantilla de paz y salvo...
echo UNIVERSIDAD DEL CAUCA > "src\main\resources\templates\paz-salvo.docx"
echo FACULTAD DE INGENIERIA ELECTRONICA Y TELECOMUNICACIONES >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo [TITULO_DOCUMENTO] >> "src\main\resources\templates\paz-salvo.docx"
echo No. [NUMERO_DOCUMENTO] >> "src\main\resources\templates\paz-salvo.docx"
echo Fecha: [FECHA_DOCUMENTO] >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo Por la cual se certifica el [TIPO_PROCESO] >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo EL COORDINADOR ACADEMICO DE LA [FACULTAD] >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo En uso de sus facultades legales y reglamentarias, y >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo CONSIDERANDO: >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo 1. Que el estudiante [NOMBRE_ESTUDIANTE] con codigo [CODIGO_ESTUDIANTE] del programa de [PROGRAMA] ha cumplido con todos los requisitos academicos. >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo 2. Que no tiene pendientes academicos ni administrativos. >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo 3. Que la solicitud fue presentada el [FECHA_SOLICITUD]. >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo RESUELVE: >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo ARTICULO PRIMERO: Certificar que el estudiante [NOMBRE_ESTUDIANTE] con codigo [CODIGO_ESTUDIANTE] tiene [TIPO_PROCESO] al [FECHA_DOCUMENTO]. >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo ARTICULO SEGUNDO: La presente certificacion rige a partir de la fecha de su expedicion. >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo Dada en [CIUDAD], a los [FECHA_ACTUAL]. >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo [OBSERVACIONES] >> "src\main\resources\templates\paz-salvo.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo _________________________ >> "src\main\resources\templates\paz-salvo.docx"
echo COORDINADOR ACADEMICO >> "src\main\resources\templates\paz-salvo.docx"
echo [FACULTAD] >> "src\main\resources\templates\paz-salvo.docx"
echo [NOMBRE_UNIVERSIDAD] >> "src\main\resources\templates\paz-salvo.docx"

echo.
echo Creando plantilla de reingreso...
echo UNIVERSIDAD DEL CAUCA > "src\main\resources\templates\resolucion-reingreso.docx"
echo FACULTAD DE INGENIERIA ELECTRONICA Y TELECOMUNICACIONES >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo [TITULO_DOCUMENTO] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo No. [NUMERO_DOCUMENTO] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo Fecha: [FECHA_DOCUMENTO] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo Por la cual se autoriza el [TIPO_PROCESO] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo EL COORDINADOR ACADEMICO DE LA [FACULTAD] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo En uso de sus facultades legales y reglamentarias, y >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\paz-salvo.docx"
echo CONSIDERANDO: >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo 1. Que el estudiante [NOMBRE_ESTUDIANTE] con codigo [CODIGO_ESTUDIANTE] del programa de [PROGRAMA] ha presentado solicitud de [TIPO_PROCESO]. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo 2. Que la solicitud ha sido revisada y cumple con los requisitos establecidos. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo 3. Que la solicitud fue presentada el [FECHA_SOLICITUD]. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo RESUELVE: >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo ARTICULO PRIMERO: Autorizar el [TIPO_PROCESO] del estudiante [NOMBRE_ESTUDIANTE] con codigo [CODIGO_ESTUDIANTE] al programa de [PROGRAMA]. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo ARTICULO SEGUNDO: La presente resolucion rige a partir de la fecha de su expedicion. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo Dada en [CIUDAD], a los [FECHA_ACTUAL]. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo [OBSERVACIONES] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo. >> "src\main\resources\templates\resolucion-reingreso.docx"
echo _________________________ >> "src\main\resources\templates\resolucion-reingreso.docx"
echo COORDINADOR ACADEMICO >> "src\main\resources\templates\resolucion-reingreso.docx"
echo [FACULTAD] >> "src\main\resources\templates\resolucion-reingreso.docx"
echo [NOMBRE_UNIVERSIDAD] >> "src\main\resources\templates\resolucion-reingreso.docx"

echo.
echo ========================================
echo    PLANTILLAS CREADAS EXITOSAMENTE
echo ========================================
echo.
echo Las plantillas han sido creadas como archivos de texto.
echo Para que funcionen correctamente, necesitas convertirlas a Word.
echo.
echo SIGUIENTE PASO:
echo 1. Abre Microsoft Word
echo 2. Abre cada archivo .docx creado
echo 3. Guarda como documento Word real
echo 4. Ejecuta: mvn clean compile
echo 5. Ejecuta: mvn spring-boot:run
echo.
pause

