# ChickenTest: revisión de código, refactor y cierre de la v1.0

**Proyecto:** aplicación Spring Boot 3.1.5 con Java 17 que simula una granja de huevos y gallinas: compra y venta entre usuarios, un libro mayor de movimientos, eclosión automática programada y métricas operativas.
**Fecha de cierre:** 5 de noviembre de 2026.
**Alcance:** limpieza, refactor y formato. Sin funcionalidades nuevas.

---

## Resumen: antes y después

| Área | Antes (revisión del 4 de noviembre) | Después (cierre del 5 de noviembre) |
|---|---|---|
| Organización | Paquetes por tipo técnico (`controller`, `service`, `domain`…) | Paquetes por funcionalidad: `auth`, `user`, `farm/*`, `common`, `config` |
| Formato | Sin formatter configurado | Spotless 2.43.0 con google-java-format 1.22.0 (estilo GOOGLE); `spotless:check` en verde |
| Secreto JWT | Clave fijada en el código (`JwtUtil`) | Clave leída de `app.jwt.secret` mediante `AppSettings` |
| Integración IA | Cliente de OpenAI y reporte con IA | Eliminada; reporte básico de la granja |
| Simulador de carga | Se ejecutaba en cada arranque | Solo con el perfil `load` |
| Configuración de eclosión | Constante `EGG_HATCH_DAYS` duplicada con `@Value` | Única fuente: `farm.egg-hatch-days` |
| Esquema de base de datos | Columnas y tabla sin uso; nombres desalineados con las entidades | SQL alineado con las entidades; base local regenerada |
| Errores HTTP | Trazas y mensajes internos en algunas respuestas | Manejador global, detalle solo en el log, mensaje genérico en los 500 |
| Tests | 28 tests en 2 clases | **36 tests en 5 clases, todos en verde** |
| Actuator | Público: `health`, `metrics` y `prometheus` expuestos sin autenticación | Solo `health` expuesto, y todo `/actuator/**` requiere autenticación |

---

## Verificación

Las pruebas las ejecutó Claude, como autor de los cambios, el 5 de noviembre de 2026 con JDK 17.

**Formato**
- `mvn spotless:apply` formateó los ficheros nuevos y modificados.
- `mvn spotless:check` termina con código 0.

**Suite completa (`mvn clean test`): BUILD SUCCESS**

| Clase de test | Tests | Fallos | Errores | Omitidos |
|---|---|---|---|---|
| `auth.JwtServiceTest` | 1 | 0 | 0 | 0 |
| `auth.JwtAuthenticationFilterTest` | 3 | 0 | 0 | 0 |
| `farm.exception.GlobalFarmExceptionHandlerTest` | 2 | 0 | 0 | 0 |
| `farm.service.FarmServiceImplTest` | 27 | 0 | 0 | 0 |
| `user.SystemUserInitializerTest` | 3 | 0 | 0 | 0 |
| **Total** | **36** | **0** | **0** | **0** |

El registro de error que aparece durante la ejecución (`500 - uri=/farm/test: SELECT * FROM users`) es esperado: lo produce el test que comprueba que el manejador global responde 500 sin exponer el detalle.

**Ejecución final.** `mvn clean test` da **BUILD SUCCESS** con 36 tests, 0 fallos y 0 errores.

**Prueba manual del autor.** El autor ha probado los endpoints de la aplicación en local y funcionan correctamente. Es una comprobación manual, no una prueba automatizada.

---

## Arquitectura

- **Capas:** los controladores llaman a la interfaz `FarmService`, implementada por `FarmServiceImpl`, que usa repositorios de Spring Data JPA.
- **Modelo de stock y libro mayor:** un `Article` es un lote de stock de un usuario. Cada operación de compra, venta o eclosión genera un `Movement`, de modo que el historial de cada usuario queda completo.
- **Usuario `system`:** actúa como mercado. Tiene el stock del que compran los usuarios y recibe los movimientos de eclosión.
- **Concurrencia:** compra y venta bloquean la fila del artículo (`PESSIMISTIC_WRITE`) antes de modificar saldos y unidades.
- **Eclosión:** un `@Scheduled` a medianoche envejece los lotes de huevos como máximo un día por ejecución (protegido por `lastAgedDate`) y convierte en gallinas los que alcanzan la edad configurada. Todo ocurre en una transacción.
- **Seguridad:** JWT sin estado, contraseñas con BCrypt y CORS limitado a orígenes locales. El filtro JWT registra los tokens rechazados y continúa sin autenticar, en lugar de devolver un 500.
- **Persistencia:** esquema gestionado por Flyway, con Hibernate en `ddl-auto=validate`.
- **Observabilidad:** contadores de Micrometer para operaciones y eclosiones.

---

## Puntos corregidos

1. **Secreto fuera del código.** La clave JWT dejó de estar en el código y se lee de configuración a través de `AppSettings`. El valor del histórico era de ejemplo y no se usa en ningún entorno.
2. **Simulador acotado.** `LoadSimulator` solo se activa con el perfil `load`, así que no llena la base de datos en cada arranque.
3. **Una sola fuente de configuración.** `farm.egg-hatch-days` se lee con `@Value`; desaparece la constante duplicada.
4. **Lógica de usuario sin repetición.** `findManagedUser(id)` sustituye tres búsquedas idénticas. El usuario `system` se resuelve una vez por ejecución de eclosión, y solo si hay eclosión.
5. **Esquema coherente con el modelo.** Se eliminan la columna `creation`, la columna `display_price` y la tabla `movement_types`, que no tenían uso. La base local se regenera desde cero.
6. **Entidades más limpias.** `Movement` pasa a depender solo de `@Data` de Lombok.
7. **Errores sin detalle interno.** En `application.properties` la propiedad `server.error.include-*` está en `never` o `false`. El manejador global extiende `ResponseEntityExceptionHandler`, responde 403 sin detalle a `AccessDeniedException` y registra el detalle completo en el log.
8. **Estructura de tests espejo.** Los tests siguen los mismos paquetes que el código que prueban.
**Decisión deliberada.** `buy` y `sell` siguen separados. Comparten la estructura de bloqueo, pero sus reglas de stock y saldo son distintas, y una versión común con ramas `if (isBuy)` empeoraría la lectura.

---

## Puntos fuertes

- **Lógica de negocio central sólida:** compra, venta, límites de stock por categoría y saldo, con bloqueo pesimista y transacciones.
- **Modelo auditable:** cada movimiento queda registrado en el libro mayor, incluidos los generados por el sistema.
- **Tests con sentido:** `FarmServiceImplTest` cubre casos felices, límites y errores de negocio con Mockito. Los tests de seguridad y de manejo de errores cubren el filtro JWT y el manejador global.
- **Stack moderno:** Spring Boot 3.1, Java 17, JPA con Hibernate, Flyway, Actuator, Micrometer y springdoc-openapi (Swagger UI).
- **Seguridad básica bien resuelta:** autenticación sin estado, BCrypt y el usuario del sistema inicializado al arrancar.

---

## Próximos pasos

**Verificación**
1. Comprobación automatizada de RS-04 por HTTP: pendiente, si se quiere dejar registrada como prueba.

**Limpieza y arquitectura**
2. Mover el mapeo de excepciones de `FarmController` (18 bloques `catch`) al manejador global, de modo que el código de error se decida en un único sitio.
3. Separar las propiedades en clases propias (`JwtProperties`, `SecurityProperties`) en lugar de concentrarlas en `AppSettings`.
4. Unificar los matchers de `SecurityConfig` y revisar los beans duplicados.
5. Definir roles de verdad si hacen falta. Hoy `@PreAuthorize` solo comprueba `isAuthenticated()`.
6. Cambiar la concatenación de los logs por parámetros `{}`.
7. Responder 404 en rutas inexistentes; hoy responden 403 porque la regla `anyRequest().authenticated()` las cubre también.
8. Hacer cualquier cambio futuro de esquema en una migración nueva, nunca editando una ya confirmada.

**Cobertura y funcionalidad**
9. Añadir tests de capa web (`@WebMvcTest`) para los controladores, y tests para `EggHatchScheduler`.
10. Implementar las mecánicas opcionales del README: muerte de huevos y de gallinas, y puesta de huevos por las gallinas.

**Solo si algún día se despliega.** Es un entorno personal de desarrollo, así que estos puntos no son necesarios ahora: consola H2 deshabilitada en un perfil de producción, `flyway.clean-disabled=true` y `show-sql=false`.
