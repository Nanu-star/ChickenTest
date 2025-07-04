# ChickenTest
Mi primer proyecto web en Java.

### Instrucciones 📋
Ejecutar el archivo sql script para cargar la base de datos con información, y luego ingresar al sistema con el usuario: admin/contraseña: admin.

### Construcción 🔧
El proyecto ha evolucionado y ahora utiliza Spring Boot para la ejecución principal, con componentes como `LoadSimulator` para simulación de carga y lógica de negocio. Se ha migrado la lógica principal desde Java EE Servlets y JSP a una arquitectura basada en Spring Boot.

### Requerimientos 🚀
Obligatorios:

- Una granja puede tener un número limitado de huevos y gallinas.
- Pueden comprarse huevos y gallinas si la granja posee el saldo suficiente.
- Pueden venderse huevos y gallinas de poseer artículos suficientes.
- Los huevos serán gallinas pasados [configurable] días.
- El sistema posee un reporte con la situación de la granja y más información relevante.

Opcionales:

- Los huevos pueden comprarse o ser depositados por gallinas.
- Las gallinas pueden comprarse o nacer desde un huevo.
- Los huevos pueden dejar la granja si se venden o si fallecen.
- Las gallinas fallecen pasados [configurable] días.
- Las gallinas ponen [configurable] huevos cada [configurable] días.

> **Nota:** Algunos parámetros del sistema, como los días de vida de gallinas/huevos o la frecuencia de puesta, ahora pueden configurarse fácilmente mediante el archivo `application.properties` de Spring Boot.

### Spring Boot
La aplicación utiliza Spring Boot como framework principal. Incluye componentes como `LoadSimulator` que pueden ser configurados mediante propiedades externas.

Para ejecutar la aplicación:

```bash
mvn spring-boot:run
```

Esto compilará el proyecto y levantará el servidor embebido de Spring Boot en el puerto 8080 por defecto.

#### Configuración

Las propiedades de configuración se definen en `src/main/resources/application.properties`. 

Puedes modificar estos valores para ajustar el comportamiento de los simuladores o lógica de negocio sin cambiar el código fuente.

#### Variables sensibles y configuración local

1. Crea un archivo `src/main/resources/application-local.properties` (este archivo debe estar en `.gitignore`).
2. Copia y personaliza las siguientes líneas según tus credenciales:

    ```properties
    openai.api.key=pon-tu-clave-aqui
    jwt.secret=pon-tu-secreto-aqui
    ```

3. Spring Boot cargará automáticamente este archivo si existe y sobreescribirá las propiedades del archivo principal.



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
