# Back-end-gestion-curricular
Back end de Prototipo de sistema de atención a estudiantes de pregrado en los procesos relacionados con la gestión curricular (Propuesta 2) para el soporte administrativo en la FIET

## Requisitos

- Java 17
- Maven Wrapper incluido (`./mvnw`)
- MySQL 8.x (en local)

## Ejecución local

```bash
cp gestion_curricular/env.example gestion_curricular/.env
# edita el archivo .env con tu configuración local
cd gestion_curricular
./mvnw spring-boot:run
```

El API se expone en `http://localhost:5000` por defecto y la documentación en `http://localhost:5000/swagger-ui.html`.

## Variables de entorno

| Variable | Descripción |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil a ejecutar (`dev` por defecto, usar `prod` en despliegue) |
| `SPRING_CONFIG_LOCATION` | Rutas de los archivos de configuración (usar `classpath:/application.properties,classpath:/application-prod.properties`) |
| `DB_URL` | Cadena JDBC de la base de datos |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de la base de datos |
| `DDL_AUTO` | Estrategia de Hibernate (`validate`, `update`, etc.) |
| `SHOW_SQL` | `true` o `false` para mostrar sentencias SQL |
| `DB_POOL_MAX`, `DB_POOL_MIN`, `DB_TIMEOUT`, `DB_IDLE_TIMEOUT` | Ajustes opcionales de HikariCP |
| `JWT_SECRET` | Clave secreta del token JWT (mínimo 256 bits) |
| `JWT_EXPIRATION` | Tiempo de expiración del token en milisegundos (ej. `3600000`) |
| `PORT` | Puerto que asigna la plataforma (Railway/Render) |

Consulta `gestion_curricular/env.example` para un ejemplo completo.

## Construcción

```bash
cd gestion_curricular
./mvnw -DskipTests package
```

El artefacto empaquetado queda en `gestion_curricular/target/gestion_curricular-0.0.1-SNAPSHOT.jar`.

## Despliegue recomendado (Render)

1. Crear un servicio **Web Service** en [render.com](https://render.com) y conectar el repositorio.
2. Seleccionar la rama `backend`.
3. Configurar:
   - **Build Command**: `./mvnw -DskipTests package`
   - **Start Command**: `java -jar target/gestion_curricular-0.0.1-SNAPSHOT.jar`
   - **Environment**: `Docker` / `Native` (cualquiera funciona)
4. Definir las variables de entorno listadas arriba.
5. Conectar la base de datos (MySQL externo o un servicio existente).
6. Desplegar y verificar la URL pública (`/swagger-ui.html`).

> También puedes usar Railway o Fly.io siguiendo la misma configuración de comandos y variables.