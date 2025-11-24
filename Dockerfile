FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY gestion_curricular/pom.xml .
COPY gestion_curricular/src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/gestion_curricular-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Configuración de memoria JVM optimizada para Render (ajusta según el plan)
# -Xmx512m: Máximo 512MB heap (ajusta según tu plan de Render)
# -Xms256m: Inicial 256MB heap
# -XX:+UseG1GC: Usar G1 garbage collector (mejor para aplicaciones con memoria limitada)
# -XX:MaxGCPauseMillis=200: Objetivo de pausa máxima de GC
# -XX:+UseContainerSupport: Usar límites del contenedor
# -XX:MaxRAMPercentage=75.0: Usar máximo 75% de la RAM disponible del contenedor
ENTRYPOINT ["java","-Xmx512m","-Xms256m","-XX:+UseG1GC","-XX:MaxGCPauseMillis=200","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]

