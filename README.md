# ChickenTest
Mi primer proyecto web en Java.

### Instrucciones 📋
Ejecutar el archivo sql script para cargar la base de datos con información, y luego ingresar al sistema con el usuario: admin/contraseña: admin.

### Construcción 🔧
Se realizó el programa con Java EE Servlets y Eclipse IDE. Las páginas se realizaron con JSP y Bootstrap.

### Requerimientos 🚀
Obligatorios:

-Una granja puede tener un número limitado de huevos y gallinas. 

-Pueden comprarse huevos y gallinas si la granja posee el saldo suficiente.

-Pueden venderse huevos y gallinas de poseer artículos suficientes. 

-Los huevos serán gallinas pasados [] días. 

-El sistema debe poseer un reporte con la situación de la granja y más información relevante. 


Opcionales:

-Los huevos pueden comprarse o ser depositados por gallinas. 

-Las gallinas pueden comprarse o nacer desde un huevo. 

-Los huevos pueden dejar la granja si se venden o si fallecen. 

-Las gallinas fallecen pasados [] días. 

-Las gallinas ponen [] huevos cada [] días. 

### Spring Boot
Para ejecutar la aplicación utilizando Spring Boot simplemente ejecute:

```bash
mvn spring-boot:run
```

Esto compilará el proyecto y levantará el servidor embebido de Spring Boot.

---

## Ejecución en Dev Containers

Este proyecto es compatible con [Development Containers (devcontainers)](https://containers.dev/). Para comenzar:

1. **Requisitos**
   - Tener [Docker](https://www.docker.com/) instalado y en ejecución.
   - Tener [Visual Studio Code](https://code.visualstudio.com/) con la extensión [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers).

2. **Cómo iniciar**
   - Abre la carpeta del proyecto en VS Code.
   - Cuando se te solicite, selecciona **Reabrir en contenedor** (o usa la paleta de comandos: “Dev Containers: Reopen in Container”).

3. **Qué sucede automáticamente**
   - El contenedor se construye usando el `Dockerfile` proporcionado.
   - Se instalan las dependencias de Maven (`mvn clean install`).
   - La aplicación Spring Boot se inicia automáticamente con `mvn spring-boot:run` utilizando el perfil `dev`.
   - El puerto 8080 se reenvía, por lo que puedes acceder a la aplicación en `http://localhost:8080/swagger-ui/index.html#/` desde tu navegador.

4. **Persistencia**
   - Tu espacio de trabajo local se monta dentro del contenedor para edición en vivo del código.
   - Tu repositorio local de Maven (`~/.m2`) se monta para acelerar la resolución de dependencias.

---
