# Cambios en frontend: comentarios acumulativos por documento

El backend ya está listo: al llamar a **añadir comentario** se **concatena** el texto nuevo al existente (no se sobrescribe) y se añade fecha y usuario automáticamente. Para que la experiencia sea correcta, el front debe hacer lo siguiente.

---

## 1. Al abrir el recuadro de “Añadir comentario”

- **Qué hacer:** Antes de mostrar el cuadro de texto (o al abrirlo), obtener el documento actual con:
  - **GET** `/api/documentos/buscarPorId/{id}`  
  donde `{id}` es el `idDocumento` del documento al que se le va a añadir el comentario.
- **Qué mostrar:** En el cuadro de texto, mostrar el valor de **`comentario`** que devuelve esa respuesta (puede ser una sola línea o varias, según el historial).
- **Objetivo:** Que el usuario vea los comentarios anteriores y pueda escribir el nuevo debajo o en la siguiente línea.

Si ya tienen el documento cargado en el contexto de la solicitud (por ejemplo en el listado de documentos), pueden usar ese mismo objeto y mostrar su campo `comentario`; si no, deben hacer el GET para tener el valor actualizado.

---

## 2. Al enviar (guardar) el comentario

- **Qué enviar:** En la petición **PUT** `/api/documentos/añadirComentario`, en el body enviar **solo el texto nuevo** que el usuario escribió en esta vez, **no** todo el contenido del cuadro (no reenviar el historial).
- **Body igual que hasta ahora:**  
  `{ "idDocumento": <number>, "comentario": "<solo el texto nuevo>" }`
- **Objetivo:** El backend concatena ese texto al comentario existente y le añade fecha y “Rol - Nombre” del usuario autenticado. Si el front envía todo el cuadro, se duplicaría el historial.

---

## Resumen práctico

| Momento              | Acción en front                                                                 |
|----------------------|----------------------------------------------------------------------------------|
| Abrir recuadro       | Obtener documento (GET por id) y mostrar `comentario` en el cuadro de texto.   |
| Usuario escribe      | Ve el historial arriba y escribe su nuevo comentario debajo.                    |
| Enviar               | Enviar en `comentario` **solo el texto nuevo** (lo que acaba de escribir).      |

---

## Opciones de UX (recomendable una de las dos)

- **Opción A:** Un solo cuadro: se muestra el historial (lectura o editable) y el usuario escribe debajo. Al guardar, el front envía **solo las líneas nuevas** (por ejemplo comparando con el `comentario` que se cargó al abrir, o usando un campo separado “nuevo comentario” internamente).
- **Opción B:** Dos zonas: una de solo lectura con el historial (`comentario` actual) y un segundo campo “Nuevo comentario” que es el que se envía en el PUT.

En ambos casos, el **body del PUT debe llevar solo el comentario nuevo**, no todo el texto del cuadro.

---

## API (sin cambios)

- **PUT** `/api/documentos/añadirComentario`  
  Body: `{ "idDocumento": number, "comentario": "string" }`  
  El backend concatena `comentario` al existente y añade fecha y usuario. No hace falta enviar usuario ni rol desde el front.

- **GET** `/api/documentos/buscarPorId/{id}`  
  Devuelve el documento; el campo **`comentario`** puede ser multilínea (varios comentarios con formato `[fecha] Rol - Nombre: texto`).
