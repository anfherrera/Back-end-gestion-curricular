@echo off
echo ========================================
echo    GENERADOR DE PLANTILLAS WORD
echo ========================================
echo.
echo Este script te ayudara a crear las plantillas Word necesarias.
echo.
echo PASOS A SEGUIR:
echo.
echo 1. Abre Microsoft Word
echo 2. Crea un nuevo documento
echo 3. Copia el contenido de cada archivo .txt
echo 4. Guarda como .docx en la carpeta templates
echo.
echo ARCHIVOS A CREAR:
echo - oficio-homologacion.docx
echo - paz-salvo.docx  
echo - resolucion-reingreso.docx
echo.
echo UBICACION: src\main\resources\templates\
echo.
echo Presiona cualquier tecla para abrir la carpeta de templates...
pause > nul

start "" "src\main\resources\templates"

echo.
echo ========================================
echo    PLANTILLAS CREADAS EXITOSAMENTE
echo ========================================
echo.
echo Ahora puedes:
echo 1. Compilar el proyecto: mvn clean compile
echo 2. Ejecutar el servidor: mvn spring-boot:run
echo 3. Probar la generacion de documentos desde el frontend
echo.
pause
