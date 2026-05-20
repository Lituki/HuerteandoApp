FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos solo lo necesario para aprovechar cache de dependencias.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

# Ahora copiamos código y generamos el jar.
COPY src src
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/HuerteandoApp-0.0.1-SNAPSHOT.jar app.jar

# Render inyecta PORT en runtime.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
