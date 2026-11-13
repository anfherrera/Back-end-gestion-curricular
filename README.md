# Back-end-gestion-curricular

Backend del sistema de atención a estudiantes de pregrado en los procesos relacionados con la gestión curricular (Propuesta 2) para el soporte administrativo en la FIET - Universidad del Cauca.

## Tecnologías

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Security** (JWT)
- **Spring Data JPA / Hibernate**
- **MySQL**
- **Maven**
- **Lombok**
- **MapStruct**
- **Swagger/OpenAPI**

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+ (o Maven Wrapper incluido `./mvnw`)
- MySQL 8.0+
- Git

## Configuración Local

### 1. Clonar el repositorio
```bash
git clone <url-del-repositorio>
cd Back-end-gestion-curricular
```

### 2. Configurar Base de Datos

Crear una base de datos MySQL:
```sql
CREATE DATABASE bdcurricular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar Variables de Entorno

Copiar el archivo de ejemplo y configurar:
```bash
cp gestion_curricular/env.example gestion_curricular/.env
```

Editar `.env` con tus credenciales:
```properties
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:mysql://localhost:3306/bdcurricular?useSSL=false&serverTimezone=GMT
DB_USERNAME=root
DB_PASSWORD=tu_contraseña
JWT_SECRET=clave_secreta_desarrollo
```

### 4. Ejecutar la Aplicación

```bash
cd gestion_curricular
./mvnw spring-boot:run
# o
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:5000`

## Documentación API

Una vez iniciada la aplicación, accede a:
- **Swagger UI**: `http://localhost:5000/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:5000/api-docs`

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal** (Ports & Adapters):

```
gestion_curricular/
├── dominio/              # Lógica de negocio (casos de uso)
├── aplicacion/           # Puertos (interfaces)
│   ├── input/           # Casos de uso (puertos de entrada)
│   └── output/          # Gateways (puertos de salida)
└── infraestructura/      # Adaptadores
    ├── input/           # Controladores REST
    └── output/          # Persistencia, servicios externos
```

## Seguridad

- Autenticación JWT
- Rate limiting en login
- Password hashing con BCrypt
- Headers de seguridad HTTP
- CORS configurado
- Auditoría de seguridad

## Despliegue

### Variables de Entorno Requeridas

| Variable | Descripción |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil a ejecutar (`prod` en despliegue) |
| `SPRING_CONFIG_LOCATION` | Rutas de los archivos de configuración (usar `classpath:/application.properties,classpath:/application-prod.properties`) |
| `DB_URL` | Cadena JDBC de la base de datos |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de la base de datos |
| `DDL_AUTO` | Estrategia de Hibernate (`validate` en producción) |
| `SHOW_SQL` | `false` en producción |
| `JWT_SECRET` | Clave secreta del token JWT (mínimo 256 bits) |
| `JWT_EXPIRATION` | Tiempo de expiración del token en milisegundos (ej. `3600000`) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS (ej. `https://tu-dominio.com`) |
| `PORT` | Puerto que asigna la plataforma (Railway/Render) |

Consulta `gestion_curricular/env.example` para un ejemplo completo.

### Build para Producción

```bash
cd gestion_curricular
./mvnw -DskipTests package
# o
mvn clean package -DskipTests
```

El artefacto empaquetado queda en `gestion_curricular/target/gestion_curricular-0.0.1-SNAPSHOT.jar`.

### Despliegue en Render

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

### Health Checks

- **Health**: `http://localhost:5000/actuator/health`
- **Info**: `http://localhost:5000/actuator/info`

## Perfiles

- **dev**: Desarrollo local con MySQL. Flyway migra el esquema al arrancar y Hibernate lo valida.
- **prod**: Producción (valida esquema, usa variables de entorno)
- **test**: Pruebas con H2 en memoria. Hibernate crea el esquema y Spring carga los datos compartidos de `test-data.sql`.

### Migraciones de desarrollo

Empieza con una base MySQL vacía. V1 crea el esquema y V2 carga roles y programas. Flyway registra las versiones aplicadas en `flyway_schema_history` y conserva los datos entre arranques.

Si tu base local viene de `ddl-auto=create`, conserva esa base y usa una nueva para empezar con Flyway. Puedes indicar su URL con `SPRING_DATASOURCE_URL`. Para poblar una base nueva con ejemplos, ejecuta una vez `gestion_curricular/scripts/datos_demo.sql` desde tu cliente MySQL después del primer arranque y reinicia la aplicación. El primer arranque crea la cuenta de administrador de desarrollo. El script agrega los demás datos de ejemplo.

Agrega el siguiente cambio como `gestion_curricular/src/main/resources/db/migration/V3__descripcion.sql` y actualiza las entidades si corresponde. Usa una versión nueva para cada cambio y conserva intactos los archivos ya aplicados. Comprueba el arranque tanto con una base vacía como con una que ya tenga las versiones anteriores.

El perfil `prod` mantiene la gestión manual del esquema.


## Pruebas

GitHub Actions también inicia el perfil dev dos veces contra un MySQL vacío y comprueba que Flyway conserva los datos.

```bash
cd gestion_curricular
./mvnw test
```

Compila y pasa las pruebas con Java 17 (producción) y Java 25. GitHub Actions corre la suite con ambas versiones
en cada push y en cada pull request a `main` (`.github/workflows/pruebas.yml`); un pull request con pruebas en rojo
no debería fusionarse.

Algunas pruebas comparan la respuesta completa de las estadísticas con archivos de referencia en
`src/test/resources/referencias/`. Si un cambio en la respuesta es intencional, se regraban con
`./mvnw test -Dtest='*CaracterizacionTest' -DgrabarReferencia=true` y se revisa el diff antes de hacer commit.

## Autores

Sistema de Gestión Curricular - FIET - Universidad del Cauca

## Licencia

MIT License
