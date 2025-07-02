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

### Actualización de la base de datos
Si cuentas con una base de datos existente creada antes de esta versión,
ejecuta la migración de Flyway `V1_0_2__add_article_fields.sql` para añadir las
columnas `age`, `production` y `display_price` a la tabla `articles`.
