FROM eclipse-temurin:17-jdk

# Crea el directorio de trabajo
WORKDIR /app

# Copia el código fuente (todo el proyecto)
COPY . .

# Compila el proyecto con Maven
RUN ./mvnw clean package -DskipTests

# Expone el puerto si querés (opcional)
EXPOSE 8080

# Corre la app
ENTRYPOINT ["java", "-jar", "target/chickentest-0.0.1-SNAPSHOT.jar"]
