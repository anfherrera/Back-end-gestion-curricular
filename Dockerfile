# Etapa 1: compilar el JAR (las pruebas ya corren en GitHub Actions)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY gestion_curricular/pom.xml .
# Descarga las dependencias en una capa propia: si solo cambia el código, no se vuelven a bajar
RUN mvn -B -ntp dependency:go-offline
COPY gestion_curricular/src ./src
RUN mvn -B -ntp -DskipTests package

# Etapa 2: solo el JRE y el JAR
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/gestion_curricular-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Java ajusta la memoria al límite del contenedor
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
