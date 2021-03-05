# ChickenTest
Mi primer proyecto web en Java.

### Instrucciones 📋
Ejecutar el archivo sql script para cargar la base de datos con información, y luego ingresar al sistema con el usuario: admin/contraseña: admin.

### Construcción 🔧
Se realizó el programa con Java EE Servlets y Eclipse IDE. Las páginas se realizaron con JSP y Bootstrap.

### Requerimientos 🚀
Obligatorios:

-Una granja puede tener un número limitado de huevos y gallinas. Se definió en el método calculoStock() el límite de 2000 huevos y 1500 gallinas.

-Pueden comprarse huevos y gallinas si la granja posee el saldo suficiente. Se realiza acción de compra y de no poseer el saldo suficiente va a una página de error.

-Pueden venderse huevos y gallinas de poseer artículos suficientes. Se realiza acción venta y de no poseer los artículos va a una página de error.

-Los huevos serán gallinas pasados [] días. verificoDatos() cuando leo los artículos y los huevos van a eclosionar() en gallinas pasados los 21 días de edad.

-El sistema debe poseer un reporte con la situación de la granja y más información relevante.  Se encuentra en la sección Reporte.


Opcionales:

-Los huevos pueden comprarse o ser depositados por gallinas. Acción compra y también se resolvió generando un lote diario de huevos de 1 huevo por cada gallina en la granja.

-Las gallinas pueden comprarse o nacer desde un huevo. Acción compra y resolvió con que los huevos van a eclosionar() en gallinas pasados los 21 días de edad.

-Los huevos pueden dejar la granja si se venden o si fallecen. Acción venta, y verificoDatos() para que las gallinas fallezcan.

-Las gallinas fallecen pasados [] días. Se definió que fallecen pasados los 200 días de edad.

-Las gallinas ponen [] huevos cada [] días. Se resolvió generando un lote diario de huevos de 1 huevo por cada gallina en la granja.
