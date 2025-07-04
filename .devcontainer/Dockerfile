FROM mcr.microsoft.com/devcontainers/java:17-bullseye

# Instala Maven (si querés versión custom, sino ya viene con el image)
RUN apt-get update && apt-get install -y maven

# Setea el working directory
WORKDIR /workspace

CMD ["./mvnw", "spring-boot:run"]
