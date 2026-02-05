# PROMPT: Pruebas de seguridad para el backend de Gestión Curricular

## Objetivo

Implementar y completar las pruebas de seguridad en la carpeta `seguridad` del proyecto, siguiendo el mismo estilo que las pruebas existentes en `homologacion`, `ecaes`, `reingreso`, etc. (unitarias, funcionales, integración o aceptación según corresponda).

## Contexto del proyecto

- **Backend**: Spring Boot, arquitectura hexagonal.
- **Autenticación**: JWT; login en `POST /api/usuarios/login` (correo + password).
- **Seguridad**: `SeguridadConfig`, `JwtUtil`, `JwtAuthenticationFilter`, `LoginRateLimiter`, `SecurityAuditService`. Contraseñas con BCrypt (`PasswordEncoder`).
- **Listados por rol**: Los gateways/repositorios filtran por estado. Ejemplos:
  - **Funcionario**: solicitudes con último estado `"Enviada"` (ECAES, Homologación, Reingreso, Paz y Salvo).
  - **Coordinador**: solicitudes con último estado `"APROBADA_FUNCIONARIO"` (por programa y/o período según módulo).

## Tipos de pruebas a implementar

### 1. Pruebas unitarias (SeguridadUnidadTest – ya creado como base)

- **Ubicación**: `seguridad/SeguridadUnidadTest.java`.
- **Qué son**: Pruebas aisladas, sin Spring Context, con mocks si hace falta.
- **Qué validar**:
  - La contraseña no se guarda en texto plano: al codificar con `PasswordEncoder` el resultado no es igual al texto original.
  - BCrypt: el hash empieza con `$2` (p. ej. `$2a$` o `$2b$`).
  - `PasswordEncoder.matches(textoPlano, hash)` retorna `true` solo si la contraseña es correcta.
  - `PasswordEncoder.matches(contraseñaIncorrecta, hash)` retorna `false`.
  - Opcional: mismo texto plano genera hashes distintos en dos codificaciones (salt).

### 2. Pruebas funcionales / integración de seguridad (crear)

- **Ubicación sugerida**: `seguridad/SeguridadFuncionalTest.java` o `SeguridadIntegracionTest.java`.
- **Qué son**: Pruebas con `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, usando `MockMvc` y, si aplica, base de datos de prueba o datos de `import.sql`.
- **Qué validar**:

#### Login

- **Credenciales incorrectas (usuario o clave)**:
  - Request: `POST /api/usuarios/login` con cuerpo JSON `{ "correo": "noexiste@test.com", "password": "clave" }`.
  - Esperado: respuesta HTTP **401 Unauthorized** y mensaje acorde (p. ej. "Credenciales incorrectas"). No debe devolver token ni 200.
- **Usuario correcto, contraseña incorrecta**:
  - Usar un usuario existente en datos de test (p. ej. `import.sql`) y enviar `password` erróneo.
  - Esperado: **401** y mismo criterio que arriba.
- **Credenciales correctas** (opcional pero recomendable):
  - Request con correo y contraseña de un usuario de prueba.
  - Esperado: **200 OK**, cuerpo con token (p. ej. en `LoginDTORespuesta`) y que el token sea no nulo/no vacío.

#### Filtrado por rol y estado (reglas de negocio de seguridad)

Comprobar que los listados que dependen del rol solo devuelven solicitudes en el estado que define la regla de negocio (sin exigir en este prompt un usuario autenticado concreto; se puede usar mocks o perfiles que permitan llamar sin JWT en test).

- **Listado para Funcionario (ECAES, Homologación, Reingreso o Paz y Salvo, según lo que exista)**:
  - Llamar al endpoint que lista solicitudes para funcionario (p. ej. `GET /api/solicitudes-ecaes/listarSolicitudes-Ecaes/Funcionario` o el equivalente en homologación/reingreso).
  - Esperado: todas las solicitudes devueltas tienen **último estado** `"Enviada"` (o el valor exacto que use el backend, p. ej. `"Enviada"` con esa capitalización).
- **Listado para Coordinador**:
  - Llamar al endpoint que lista solicitudes para coordinador (por programa y/o período si aplica).
  - Esperado: todas las solicitudes devueltas tienen **último estado** `"APROBADA_FUNCIONARIO"`.

Se puede hacer un test por módulo (ECAES, Homologación, etc.) o un test parametrizado si se prefiere reutilizar lógica.

#### Contraseña cifrada en persistencia (opcional)

- Si en test se puede acceder al usuario guardado (repositorio o BD de test): comprobar que el campo `password` del usuario **no** es igual a la contraseña en texto plano que se usó al crear/actualizar. Y que `PasswordEncoder.matches(textoPlano, usuario.getPassword())` sea `true`.

## Requisitos técnicos

- **Estilo**: Misma convención que el resto del proyecto: `@DisplayName` descriptivo, bloques Arrange/Act/Assert o comentarios Given/When/Then si se usa estilo aceptación.
- **Perfil**: Usar `@ActiveProfiles("test")` en pruebas que levanten contexto Spring.
- **Datos de prueba**: Si los tests de login o listados dependen de usuarios/solicitudes, usar `import.sql` o scripts equivalentes en el perfil `test` para tener datos conocidos (correo y contraseña de test; solicitudes en estados "Enviada" y "APROBADA_FUNCIONARIO").
- **No romper arquitectura**: Las pruebas solo validan comportamiento (login, filtros de listados, cifrado); no piden cambiar la arquitectura hexagonal.

## Estructura de archivos sugerida

```
gestion_curricular/src/test/java/.../gestion_curricular/
├── seguridad/
│   ├── SeguridadUnidadTest.java      (ya creado – ampliar si hace falta)
│   ├── SeguridadFuncionalTest.java   (crear – login + filtrado por rol/estado)
│   └── PROMPT_PRUEBAS_SEGURIDAD.md   (este archivo)
```

## Resumen de lo que debe quedar validado

| Aspecto | Tipo de prueba | Qué validar |
|--------|-----------------|-------------|
| Contraseña no en texto plano / BCrypt | Unitaria | Encode + matches como arriba |
| Login con credenciales incorrectas | Funcional/Integración | 401, sin token |
| Login con credenciales correctas | Funcional/Integración | 200, token presente |
| Listado funcionario = solo estado "Enviada" | Funcional/Integración | Todas las solicitudes con último estado "Enviada" |
| Listado coordinador = solo "APROBADA_FUNCIONARIO" | Funcional/Integración | Todas con último estado "APROBADA_FUNCIONARIO" |
| Contraseña guardada cifrada (opcional) | Integración/Unitaria | Campo password != texto plano; matches true |

Implementar estas pruebas en la carpeta `seguridad` sin cambiar la lógica de negocio existente; solo añadir tests que verifiquen que el comportamiento de seguridad es el esperado.
