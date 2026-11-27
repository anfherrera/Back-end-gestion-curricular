# Back-end-gestion-curricular

API REST del **Sistema de Atención a Estudiantes de Pregrado** de la Decanatura de la FIET (Universidad del Cauca). Atiende las solicitudes de gestión curricular de los estudiantes y su revisión por parte de funcionarios, coordinadores, secretarios y el decano.

El frontend (Angular) está en un repositorio aparte: `Front-end-gestion-curricular`.

## Contenido

- [Qué hace](#qué-hace)
- [Tecnologías](#tecnologías)
- [Ejecutar en local](#ejecutar-en-local)
- [Arquitectura](#arquitectura)
- [Seguridad e inicio de sesión](#seguridad-e-inicio-de-sesión)
- [Inicio de sesión con Google](#inicio-de-sesión-con-google)
- [Base de datos y migraciones](#base-de-datos-y-migraciones)
- [Pruebas](#pruebas)
- [Despliegue](#despliegue)

## Qué hace

| Módulo | Ruta base | Resumen |
| --- | --- | --- |
| Homologación de asignaturas | `/api/solicitudes-homologacion` | Solicitud, revisión por etapas y oficio de respuesta |
| Reingreso | `/api/solicitudes-reingreso` | Solicitud, revisión por etapas y resolución |
| Paz y salvo | `/api/solicitudes-pazysalvo` | Documentos del estudiante, revisión y certificado |
| Pruebas ECAES (Saber Pro) | `/api/solicitudes-ecaes` | Fechas del periodo y preinscripción |
| Cursos de verano | `/api/cursos-verano` | Oferta, preinscripciones, inscripciones y reportes |
| Estadísticas | `/api/estadisticas` | Indicadores, tendencias y reportes en PDF y Excel |
| Documentos | `/api/documentos`, `/api/archivos`, `/api/documentos-generados` | Archivos adjuntos y oficios generados desde plantillas Word |
| Notificaciones | `/api/notificaciones` | Avisos de cambio de estado para cada rol |
| Usuarios | `/api/usuarios` | Cuentas, roles y activación; los crean el Administrador y el Coordinador |
| Catálogos | `/api/roles`, `/api/programas`, `/api/docentes`, `/api/periodos-academicos`, `/api/tipos-documento` | Datos maestros que cualquier usuario consulta; roles, programas y docentes solo los modifica el Administrador |

**Flujo de las solicitudes de homologación, reingreso y paz y salvo.** En cada paso, el rol que decide puede aprobar o rechazar. El Administrador puede hacer cualquier paso válido, pero no saltarse ninguno.

```
ENVIADA ─(Funcionario)─▶ APROBADA_FUNCIONARIO ─(Coordinador)─▶ APROBADA_COORDINADOR ─(Secretario)─▶ APROBADA
   └──────────────────────────────┴──────────────── RECHAZADA ◀───────────────┘
```

ECAES tiene un solo paso: `ENVIADA ─(Funcionario)─▶ PRE_REGISTRADO | RECHAZADA`.

**Roles:** Administrador, Estudiante, Coordinador, Secretario, Funcionario, Docente y Decano.

La documentación de cada endpoint está en Swagger: `http://localhost:5000/swagger-ui.html`.

## Tecnologías

- Java 17 (producción) y Java 25 (también se prueba con ella)
- Spring Boot 3.5: Web, Security, Data JPA, Validation, Actuator
- JWT (jjwt) para la sesión y `spring-security-oauth2-jose` para verificar los tokens de Google
- MySQL 8 con migraciones Flyway; H2 en memoria para las pruebas
- MapStruct y Lombok
- Apache POI (Word y Excel), iText (PDF) y Commons Math (proyecciones de demanda)
- springdoc-openapi (Swagger UI)

## Ejecutar en local

**Requisitos:** Java 17 o superior y MySQL 8. No hace falta instalar Maven: el proyecto trae `mvnw`.

1. Crear la base de datos (vacía; Flyway crea las tablas al arrancar):

   ```sql
   CREATE DATABASE bdcurricular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Arrancar con el perfil `dev`, que es el predeterminado:

   ```bash
   cd gestion_curricular
   ./mvnw spring-boot:run
   ```

   En Windows: `mvnw.cmd spring-boot:run`.

3. Abrir `http://localhost:5000/swagger-ui.html`.

El perfil `dev` (`application-dev.properties`) usa MySQL en `localhost:3306` con el usuario `root` sin contraseña. Para cambiar cualquier valor sin editar el archivo, se define la variable de entorno equivalente, por ejemplo `SPRING_DATASOURCE_PASSWORD=mi_clave` o `SPRING_DATASOURCE_URL=...`.

Al primer arranque se crea la cuenta `admin@unicauca.edu.co` con la contraseña `password123`, o la que indique `ADMIN_PASSWORD`. Para tener datos de ejemplo, se ejecuta una vez `gestion_curricular/scripts/datos_demo.sql` y se reinicia la aplicación.

## Arquitectura

El proyecto sigue la **arquitectura hexagonal** (puertos y adaptadores). Las dependencias apuntan siempre hacia el dominio: el dominio no conoce Spring, JPA ni los controladores.

```
co.edu.unicauca.decanatura.gestion_curricular
├── dominio/                 Reglas de negocio, sin dependencias de frameworks
│   ├── modelos/             Entidades del negocio y flujos de estado
│   ├── casosDeUso/          Implementación de los puertos de entrada (*CUAdapter)
│   ├── servicios/           Reglas compartidas (estadísticas, cupos, documentos)
│   └── excepciones/         Errores de negocio y sus códigos (GC-XXXX)
├── aplicacion/              Puertos (interfaces)
│   ├── input/               Lo que ofrece el dominio (*CUIntPort)
│   └── output/              Lo que el dominio necesita de afuera (*GatewayIntPort)
├── infraestructura/         Adaptadores
│   ├── input/               Controladores REST, DTO, validación y manejo global de errores
│   ├── output/              Persistencia JPA, generación de PDF, Excel y Word
│   └── configuracion/       Ensamblado de los casos de uso (BeanConfiguration)
└── seguridad/               JWT, inicio con Google, permisos por rol, límite de intentos y auditoría
```

Cómo viaja una petición: **controlador** → **puerto de entrada** → **caso de uso** (dominio) → **puerto de salida** → **gateway JPA**. Los errores de negocio llegan al manejador global (`RestApiExceptionHandler`), que siempre responde con la misma forma: `codigoError`, `mensaje`, `codigoHttp`, `url` y `metodo`.

## Seguridad e inicio de sesión

- **Sesión con JWT**: `POST /api/usuarios/login` (correo y contraseña) o `POST /api/usuarios/login/google` devuelven un token que se envía en `Authorization: Bearer <token>`. Dura una hora por defecto (`JWT_EXPIRATION`).
- **Solo cuentas activas**: un usuario desactivado no puede entrar (403) y el token que ya tenía deja de servir.
- **Permisos por rol** en cada ruta (`@PreAuthorize` y `SeguridadConfig`).
- **Límite de intentos**: 5 fallos en 15 minutos bloquean el par correo + IP durante 15 minutos (429).
- Contraseñas con **BCrypt**, encabezados de seguridad HTTP y CORS limitado a los orígenes del frontend.
- **Auditoría**: los ingresos fallidos, bloqueos y tokens inválidos quedan en el log (logger `SecurityAuditService`).

## Inicio de sesión con Google

Permite entrar con el botón "Iniciar sesión con Google" usando la cuenta institucional **@unicauca.edu.co**. Es opcional: **mientras no se configure, el endpoint responde 503, el botón no aparece en el frontend y el resto del sistema funciona igual.**

**Para activarlo:**

1. En [Google Cloud Console → Credenciales](https://console.cloud.google.com/apis/credentials), crear un **ID de cliente de OAuth** de tipo **Aplicación web**. Si es la primera vez, Google pide configurar antes la *pantalla de consentimiento*.
2. En **Orígenes de JavaScript autorizados**, agregar `http://localhost:4200` y la URL del frontend en producción (`https://front-end-gestion-curricular.vercel.app`). No hace falta URI de redirección.
3. Copiar el Client ID (termina en `.apps.googleusercontent.com`) en:
   - **Backend**: la variable de entorno `GOOGLE_CLIENT_ID` en Render. En local, la misma variable antes de `./mvnw spring-boot:run`.
   - **Frontend**: `googleClientId` en `src/environments/environment.ts` y en `environment.production.ts`.
4. Reiniciar el backend y volver a desplegar el frontend.

**Quién puede entrar.** El backend verifica la firma del token con las llaves públicas de Google, que sea para esta aplicación, que no haya vencido, que Google haya verificado el correo y que la cuenta sea del Google Workspace `unicauca.edu.co`. Además, la cuenta debe estar **registrada y activa** en el sistema. Una cuenta institucional sin registro recibe 403 con un mensaje para que el Administrador o el Coordinador la cree primero.

## Base de datos y migraciones

| Perfil | Base de datos | Esquema |
| --- | --- | --- |
| `dev` | MySQL local | Flyway migra al arrancar y Hibernate valida |
| `prod` | MySQL (variables de entorno) | Hibernate valida (`DDL_AUTO=validate`); los cambios se aplican a mano |
| `test` | H2 en memoria | Hibernate lo crea y carga `src/test/resources/test-data.sql` |

Las migraciones están en `gestion_curricular/src/main/resources/db/migration/`: `V1` crea el esquema y `V2` carga los roles y los programas. Para cambiar el esquema:

1. Agregar un archivo nuevo, por ejemplo `V3__descripcion.sql`, y ajustar las entidades si corresponde.
2. No modificar nunca un archivo ya aplicado.
3. Probar el arranque con una base vacía y con una que ya tenga las versiones anteriores.

Si tu base local viene de la época de `ddl-auto=create`, crea una base nueva para Flyway e indícala con `SPRING_DATASOURCE_URL`.

## Pruebas

```bash
cd gestion_curricular
./mvnw verify
```

- Más de 600 pruebas: unitarias, de integración con H2 y de caracterización.
- GitHub Actions (`.github/workflows/pruebas.yml`) corre la suite con Java 17 y Java 25 en cada push y en cada pull request a `main`. También arranca el perfil `dev` dos veces contra un MySQL 8 vacío para comprobar que las migraciones funcionan y conservan los datos. No se debe fusionar un pull request con pruebas en rojo.
- Las pruebas de caracterización comparan respuestas completas con archivos de `src/test/resources/referencias/`. Si un cambio en una respuesta es intencional, se regraban con `./mvnw test -Dtest='*CaracterizacionTest' -DgrabarReferencia=true` y se revisa el diff antes del commit.

## Despliegue

Producción corre en **Render** con el `Dockerfile` de la raíz: compila con Maven y ejecuta el JAR en Java 17.

### Variables de entorno

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Sí | `prod` |
| `SPRING_CONFIG_LOCATION` | Sí | `classpath:/application.properties,classpath:/application-prod.properties` |
| `DB_URL` | Sí | Cadena JDBC de MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | Sí | Credenciales de la base de datos |
| `JWT_SECRET` | Sí | Clave para firmar los tokens: mínimo 32 caracteres; sin ella la aplicación no arranca |
| `JWT_EXPIRATION` | No | Duración del token en milisegundos (por defecto `3600000`, una hora) |
| `CORS_ALLOWED_ORIGINS` | No | Orígenes del frontend separados por comas. Por defecto `http://localhost:4200,https://front-end-gestion-curricular.vercel.app`. Si se define, debe incluir la URL del frontend en producción |
| `GOOGLE_CLIENT_ID` | No | Activa el [inicio de sesión con Google](#inicio-de-sesión-con-google) |
| `ADMIN_CEDULA` / `ADMIN_PASSWORD` | No | Crean la cuenta `admin@unicauca.edu.co` si no existe; reiniciar no cambia su contraseña |
| `DDL_AUTO` / `SHOW_SQL` | No | `validate` y `false` por defecto |
| `PORT` | No | Lo asigna Render |

`gestion_curricular/env.example` tiene un ejemplo completo.

### Comprobar el despliegue

- `GET /actuator/health` debe responder `{"status":"UP"}`.
- La documentación queda en `/swagger-ui.html`.

### Compilar el JAR a mano

```bash
cd gestion_curricular
./mvnw -DskipTests package
java -jar target/gestion_curricular-0.0.1-SNAPSHOT.jar
```

## Licencia

Apache License 2.0. Ver [LICENSE](LICENSE).
