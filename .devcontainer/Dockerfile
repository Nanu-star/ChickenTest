# Imagen base con JDK 17
FROM eclipse-temurin:17-jdk

# Crea directorio de trabajo
WORKDIR /app

# Copia el jar compilado (usado después del build)
COPY target/*.jar app.jar

# Comando para iniciar tu app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]