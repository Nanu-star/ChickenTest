# Imagen base con Maven y JDK 17 ya incluidos
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos todo y construimos
COPY . .

RUN mvn clean package -DskipTests

# Imagen final, más liviana
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
