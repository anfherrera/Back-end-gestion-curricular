FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY gestion_curricular/pom.xml .
COPY gestion_curricular/src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/gestion_curricular-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

