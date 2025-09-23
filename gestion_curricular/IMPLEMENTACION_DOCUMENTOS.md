# 📄 Implementación de Generación de Documentos

## ✅ **¿Qué se Implementó?**

Se ha implementado un sistema completo de generación automática de documentos Word para el sistema de gestión curricular.

### **Archivos Creados:**

#### **1. DTOs (Data Transfer Objects):**
- `DocumentRequest.java` - Para recibir datos del frontend
- `DocumentTemplate.java` - Para definir plantillas de documentos

#### **2. Service:**
- `DocumentGeneratorService.java` - Lógica de generación de documentos

#### **3. Controller:**
- `DocumentGeneratorController.java` - Endpoints REST para el frontend

#### **4. Dependencias:**
- Apache POI agregado al `pom.xml` para manejo de documentos Word

#### **5. Plantillas:**
- `oficio-homologacion-template.txt`
- `paz-salvo-template.txt`
- `resolucion-reingreso-template.txt`

## 🚀 **Pasos para Completar la Implementación:**

### **Paso 1: Crear Plantillas Word**
1. Ejecuta el script `crear-plantillas-word.bat`
2. Abre Microsoft Word
3. Crea 3 documentos nuevos:
   - `oficio-homologacion.docx`
   - `paz-salvo.docx`
   - `resolucion-reingreso.docx`
4. Copia el contenido de cada archivo `.txt` correspondiente
5. Guarda los archivos en `src/main/resources/templates/`

### **Paso 2: Compilar el Proyecto**
```bash
mvn clean compile
```

### **Paso 3: Ejecutar el Servidor**
```bash
mvn spring-boot:run
```

### **Paso 4: Probar desde el Frontend**
1. Ve a la vista de secretaría
2. Selecciona una solicitud aprobada
3. Completa el formulario de generación de documento
4. Descarga el documento Word generado

## 📋 **Endpoints Disponibles:**

### **Generar Documento:**
```
POST /api/documentos/generar
Content-Type: application/json

{
  "idSolicitud": 1,
  "tipoDocumento": "OFICIO_HOMOLOGACION",
  "datosDocumento": {
    "numeroDocumento": "001-2024",
    "fechaDocumento": "2024-01-15",
    "observaciones": "Documento generado automáticamente"
  },
  "datosSolicitud": {
    "nombreEstudiante": "Juan Pérez",
    "codigoEstudiante": "12345",
    "programa": "Ingeniería Sistemas",
    "fechaSolicitud": "2024-01-10"
  }
}
```

### **Obtener Plantillas:**
```
GET /api/documentos/templates/homologacion
GET /api/documentos/templates/paz-salvo
GET /api/documentos/templates/reingreso
```

## 🎯 **Tipos de Documentos Soportados:**

### **1. Oficio de Homologación:**
- **ID:** `OFICIO_HOMOLOGACION`
- **Descripción:** Documento oficial que aprueba la homologación de asignaturas
- **Campos requeridos:** número de documento, fecha
- **Campos opcionales:** observaciones

### **2. Paz y Salvo:**
- **ID:** `PAZ_SALVO`
- **Descripción:** Documento que certifica que el estudiante no tiene pendientes académicos
- **Campos requeridos:** número de documento, fecha
- **Campos opcionales:** observaciones, semestre

### **3. Resolución de Reingreso:**
- **ID:** `RESOLUCION_REINGRESO`
- **Descripción:** Documento que autoriza el reingreso del estudiante al programa
- **Campos requeridos:** número de documento, fecha
- **Campos opcionales:** observaciones, motivo de reingreso

## 🔧 **Placeholders Disponibles:**

Los siguientes placeholders serán reemplazados automáticamente en las plantillas:

- `[NOMBRE_ESTUDIANTE]` - Nombre completo del estudiante
- `[CODIGO_ESTUDIANTE]` - Código del estudiante
- `[PROGRAMA]` - Programa académico
- `[NUMERO_DOCUMENTO]` - Número del documento
- `[FECHA_DOCUMENTO]` - Fecha del documento
- `[FECHA_SOLICITUD]` - Fecha de la solicitud
- `[FECHA_ACTUAL]` - Fecha actual
- `[OBSERVACIONES]` - Observaciones adicionales
- `[TITULO_DOCUMENTO]` - Título del documento
- `[TIPO_PROCESO]` - Tipo de proceso
- `[NOMBRE_UNIVERSIDAD]` - Universidad del Cauca
- `[FACULTAD]` - Facultad de Ingeniería
- `[CIUDAD]` - Popayán

## 🎉 **¡Implementación Completada!**

El sistema está listo para generar documentos Word automáticamente. Solo necesitas crear las plantillas Word siguiendo las instrucciones del script.

### **Próximos Pasos:**
1. ✅ Crear plantillas Word
2. ✅ Compilar y ejecutar el backend
3. ✅ Probar desde el frontend
4. ✅ Extender a otros procesos (paz-salvo, reingreso)

### **Soporte:**
Si tienes algún problema, revisa los logs del servidor para ver los mensajes de debug que incluyen:
- 📄 Generando documento: [tipo]
- 📄 Datos del documento: [datos]
- ✅ Documento generado exitosamente: [nombre]
