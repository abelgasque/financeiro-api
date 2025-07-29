# Etapa 1: build da aplicação
FROM maven:3.8.5-openjdk-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: imagem final (mais enxuta)
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY --from=build /app/target/Financeiro-api-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Xmx350m", "-Xms256m", "-XX:+UseContainerSupport", "-jar", "/app.jar"]