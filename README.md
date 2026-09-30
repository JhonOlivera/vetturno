# VetTurno

API REST para la agenda de citas de **Veterinaria Huellitas**. Taller evaluativo del Módulo 3 (Java AI Engineer): backend profesional con Spring Boot.

**Autor:** [COMPLETA: tu nombre] · **Repositorio:** https://github.com/JhonOlivera/vetturno

## 1. La historia

Doña Marta abrió Veterinaria Huellitas hace seis años. Ella atiende la clínica con el doctor Andrés y con Paula, quien recibe llamadas y organiza las citas. La agenda vivía entre un cuaderno y conversaciones de WhatsApp, y eso provocaba citas duplicadas para el mismo veterinario, nombres de mascotas mal escritos y teléfonos perdidos.

VetTurno resuelve ese problema: permite registrar responsables, mascotas y veterinarios, agendar citas sin cruces de horario y consultar la agenda de forma clara y persistente.

## 2. Alcance del MVP

**Incluye**

- Registro y login con roles `USER` y `ADMIN`.
- Gestión de responsables (propietarios), mascotas, veterinarios y citas.
- Prevención de horarios duplicados por veterinario y rechazo de fechas pasadas.
- Consulta de citas por veterinario.
- Validaciones, manejo global de errores, Swagger/OpenAPI, persistencia en MySQL, README y GitHub.

**No incluye**

- Historia clínica, diagnósticos o fórmulas.
- Pagos, facturación, inventario o tienda.
- Recordatorios por correo o WhatsApp.
- Interfaz web o aplicación móvil.
- Despliegue en la nube ni Docker.

## 3. Tecnologías

| Tecnología | Uso |
|---|---|
| Java 17 | Lenguaje |
| Spring Boot 3.5.16 | Servidor embebido y autoconfiguración |
| Maven (`mvnw`) | Dependencias y construcción |
| Spring Data JPA / Hibernate | Persistencia y mapeo objeto-relacional |
| MySQL 8 | Base de datos |
| Spring Security + JWT (jjwt 0.12.6) | Autenticación y autorización |
| BCrypt | Hash de contraseñas |
| Bean Validation | Validación de datos de entrada |
| springdoc-openapi 2.8.9 | Swagger UI y documentación OpenAPI |

## 4. Arquitectura

El proyecto usa arquitectura por capas dentro del paquete base `com.huellitas.vetturno`:

| Paquete | Responsabilidad |
|---|---|
| `controller` | Recibe la petición HTTP, activa la validación y delega en el service. No accede a los repositorios. |
| `service` | Reglas de negocio: resuelve relaciones por id, valida fecha futura y disponibilidad del veterinario. |
| `repository` | Acceso a datos con `JpaRepository` y consultas derivadas. |
| `model` | Entidades JPA: `Propietario`, `Mascota`, `Veterinario`, `Cita`, `Usuario` y el enum `Rol`. |
| `dto` | Contratos de entrada (`...Request`) y salida (`...DTO`). La API nunca devuelve entidades completas. Se implementan como `record` de Java 17. |
| `security` | `JwtService`, `JwtAuthFilter`, `UsuarioDetailsService` y `SecurityConfig`. |
| `config` | `OpenApiConfig` (Swagger con esquema Bearer JWT). |
| `exception` | `ApiError`, `GlobalExceptionHandler` y `ReglaNegocioException`. |

Las dependencias se inyectan por constructor.

## 5. Modelo de datos

| Entidad | Campos | Reglas |
|---|---|---|
| Propietario | id, nombre, teléfono, email | Nombre y teléfono obligatorios; email válido. |
| Mascota | id, nombre, especie, raza, propietario | Nombre, especie y propietario obligatorios. Pertenece a un solo responsable. |
| Veterinario | id, nombre, especialidad | Nombre y especialidad obligatorios. Solo `ADMIN` puede registrarlo. |
| Cita | id, fechaHora, motivo, mascota, veterinario | Fecha futura; motivo obligatorio; no puede coincidir con otra cita del mismo veterinario. |
| Usuario | id, email, password, rol | Email único; contraseña con BCrypt; rol `USER` o `ADMIN` guardado como texto. |

**Relaciones.** Las llaves foráneas están en el lado "muchos": `mascotas.propietario_id`, `citas.mascota_id` y `citas.veterinario_id`. La relación entre `Propietario` y `Mascota` es unidireccional (`@ManyToOne` solo desde `Mascota`): la API nunca necesita listar las mascotas desde el propietario, y así se evita la recursión JSON y la carga innecesaria de colecciones.

## 6. Endpoints

| Método | Ruta | Acceso | Resultado |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Registra un usuario `USER` y devuelve un token (200). |
| POST | `/api/auth/login` | Público | Verifica credenciales y devuelve un JWT (200). |
| POST | `/api/propietarios` | USER / ADMIN | Crea un responsable (201). |
| GET | `/api/propietarios` | USER / ADMIN | Lista responsables (200). |
| POST | `/api/mascotas` | USER / ADMIN | Crea una mascota de un propietario existente (201). |
| GET | `/api/mascotas` | USER / ADMIN | Lista mascotas (200). |
| POST | `/api/veterinarios` | **ADMIN** | Crea un veterinario (201); `USER` recibe 403. |
| GET | `/api/veterinarios` | USER / ADMIN | Lista veterinarios (200). |
| POST | `/api/citas` | USER / ADMIN | Agenda una cita válida y sin cruce (201). |
| GET | `/api/citas` | USER / ADMIN | Devuelve la agenda completa (200). |
| GET | `/api/citas/veterinario/{id}` | USER / ADMIN | Filtra citas por veterinario (200). |

## 7. Roles y seguridad

- El registro es público y **siempre asigna `USER`**, aunque el cliente intente enviar otro rol.
- Las contraseñas se guardan con BCrypt (hash irreversible).
- El login devuelve un JWT con el email y una expiración de una hora. Se envía en cada petición como `Authorization: Bearer <token>`.
- La API es *stateless*: el servidor no guarda sesiones y el filtro reconstruye la identidad desde el token en cada petición.
- Solo `POST /api/veterinarios` exige el rol `ADMIN`. El resto de rutas de negocio aceptan `USER` y `ADMIN`.
- **El primer `ADMIN`** se habilita de forma controlada en la base de datos: se registra la cuenta (nace como `USER`), se actualiza el rol con SQL y se inicia sesión de nuevo para obtener un token nuevo.

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE email = 'marta@huellitas.com';
```

## 8. Cómo ejecutar

### Requisitos

- JDK 17
- MySQL 8 en ejecución (puerto 3306)
- Git

### Pasos

1. Clona el repositorio:

   ```
   git clone https://github.com/JhonOlivera/vetturno.git
   cd vetturno
   ```

2. Crea la base de datos (opcional: la URL incluye `createDatabaseIfNotExist=true` y la crea sola):

   ```sql
   CREATE DATABASE vetturno;
   ```

3. Define las variables de entorno. **No escribas contraseñas ni secretos en el código.**

   | Variable | Descripción |
   |---|---|
   | `DB_USER` | Usuario de MySQL (por defecto `root`) |
   | `DB_PASSWORD` | Contraseña de MySQL |
   | `JWT_SECRET` | Clave de firma del JWT, de **al menos 32 caracteres** |

   En PowerShell (Windows):

   ```
   $env:DB_PASSWORD="tu_contraseña"
   $env:JWT_SECRET="una-clave-larga-de-minimo-32-caracteres"
   ```

   En macOS/Linux:

   ```
   export DB_PASSWORD="tu_contraseña"
   export JWT_SECRET="una-clave-larga-de-minimo-32-caracteres"
   ```

4. Inicia la aplicación:

   ```
   .\mvnw.cmd spring-boot:run     # Windows
   ./mvnw spring-boot:run         # macOS / Linux
   ```

5. Abre Swagger UI: http://localhost:8080/swagger-ui/index.html

Para empaquetar sin ejecutar pruebas: `.\mvnw.cmd clean package -DskipTests`.

## 9. Cómo probar el flujo

Orden recomendado desde Swagger UI:

1. `POST /api/auth/register` con el email y la contraseña de Paula. Copia el token de la respuesta (o usa `POST /api/auth/login`).
2. Pulsa **Authorize**, pega solo el token (sin la palabra `Bearer`) y confirma.
3. Crea un propietario, luego una mascota con ese `propietarioId`.
4. Intenta `POST /api/veterinarios` con el usuario `USER`: debe responder **403**.
5. Registra a Doña Marta, actualiza su rol a `ADMIN` en MySQL, inicia sesión de nuevo y repite el paso 4 con su token: debe responder **201**.
6. Agenda una cita con fecha futura (`POST /api/citas`) y consulta la agenda y el filtro por veterinario.
7. Repite la misma cita para ver el 400 por horario ocupado.

## 10. Reglas de negocio y errores

Todos los errores usan el mismo formato `ApiError`:

```json
{
  "status": 400,
  "mensaje": "Datos inválidos",
  "errores": { "email": "El email no es válido" },
  "timestamp": "2026-09-29T19:37:37.7523835"
}
```

| Estado | Cuándo ocurre |
|---|---|
| 200 | Consulta o autenticación correcta |
| 201 | Creación correcta |
| 400 | Datos inválidos, referencia inexistente, fecha pasada o cita cruzada |
| 401 | Sin token, token inválido o credenciales incorrectas |
| 403 | Usuario autenticado sin permiso para la acción |
| 500 | Error interno; el mensaje es genérico y no revela detalles |

Las reglas de formato (campos obligatorios, email válido, fecha futura) viven en los DTO de entrada con Bean Validation y se activan con `@Valid`. Las reglas de agenda (fecha futura y horario libre) viven en `CitaService`, porque son decisiones del negocio y no dependen de HTTP. Un manejador global (`GlobalExceptionHandler`) traduce las excepciones a respuestas controladas, pero no reemplaza las reglas de seguridad, que se definen en `SecurityConfig`.

## 11. Matriz de pruebas manuales

Las pruebas se ejecutaron de forma manual desde Swagger UI y Postman. Todas las capturas están reunidas en un único documento: [Evidencias_de_Pruebas_Vetturno.pdf](docs/Evidencias_de_Pruebas_Vetturno.pdf). La última columna indica la página del PDF donde se ve cada prueba.

> **Nota sobre las pruebas 8, 9 y 11:** sus capturas son de Postman y se tomaron en los hitos de las partes 3 y 4, antes de activar JWT. [BORRA ESTA NOTA si las repites en Swagger con token.]

| # | Escenario | Resultado esperado | Resultado real | HTTP | Evidencia (PDF) |
|---|-----------|--------------------|----------------|------|-----------------|
| 1 | La aplicación inicia con MySQL disponible | Servidor activo y esquema accesible | La aplicación arrancó con Spring Boot 3.5.16 y Java 17.0.12, se conectó a MySQL 8.0.45, detectó los 5 repositorios JPA y quedó disponible en el puerto 8080 (`Started VetturnoApplication`). | — | Pág. 14 |
| 2 | Registro válido de un usuario nuevo | 200 y token; contraseña hasheada | `POST /api/auth/register` respondió 200 con un token. En la tabla `usuarios` la contraseña aparece como hash BCrypt (`$2a$10$...`) y el rol es `USER`. | 200 | Pág. 13 |
| 3 | Registro con email inválido y clave corta | 400 con errores por campo | Respondió 400 con "Datos inválidos" y dos errores por campo: `email` ("El email no es válido") y `password` ("La contraseña debe tener al menos 6 caracteres"). | 400 | Pág. 12 |
| 4 | Login con credenciales válidas | 200 y JWT vigente | `POST /api/auth/login` con las credenciales de Paula respondió 200 y devolvió un JWT. | 200 | Pág. 12 |
| 5 | GET /api/citas sin token | Acceso rechazado | Sin cabecera `Authorization`, respondió 401 con el JSON `ApiError` y el mensaje "Debes iniciar sesión con un token válido". | 401 | Pág. 3 |
| 6 | POST /api/veterinarios con USER | 403 Forbidden | Con el token de Paula (`USER`), respondió 403 con el mensaje "No tienes permiso para realizar esta acción". No se creó el veterinario. | 403 | Pág. 5 |
| 7 | POST /api/veterinarios con ADMIN | 201 y veterinario persistido | Con el token de Marta (`ADMIN`), respondió 201 y devolvió el veterinario creado (id 3, "Dr. carlos", Cirugía). | 201 | Pág. 4 |
| 8 | Creación válida de propietario | 201 y DTO sin colecciones anidadas | Respondió 201 con un JSON plano: id, nombre, teléfono y email de Yaritxa Duarte, sin colecciones anidadas. | 201 | Pág. 10 |
| 9 | Creación de mascota con propietario existente | 201 y relación correcta | Respondió 201 con `propietarioId` 1 y `propietarioNombre` "Yaritxa Duarte". En la tabla `mascotas`, `propietario_id` vale 1. | 201 | Págs. 10 y 5 |
| 10 | Mascota con propietario inexistente | 400 controlado; no se inserta fila | Con `propietarioId` 999 respondió 400 con el mensaje "El propietario con id 999 no existe". El listado de mascotas siguió mostrando solo a pipo. | 400 | Págs. 8 y 9 |
| 11 | Cita futura con referencias válidas | 201 y cita persistida | Respondió 201 con la cita id 1 del 2026-12-15 a las 10:00, mascota "pipo", propietario "Yaritxa Duarte" y veterinario "Dr. Andrés". | 201 | Pág. 9 |
| 12 | Cita con fecha pasada | 400 con mensaje claro | Con una fecha de 2022 respondió 400 con "Datos inválidos" y el error de campo `fechaHora`: "La fecha debe ser futura". | 400 | Pág. 1 |
| 13 | Segundo intento con mismo veterinario y horario | 400; se conserva una sola cita | Repetir la cita respondió 400 con "El veterinario ya tiene una cita en ese horario". `GET /api/citas` mostró después una sola cita. | 400 | Págs. 1 y 2 |
| 14 | Filtro de citas por veterinario | 200 y solo coincidencias | `GET /api/citas/veterinario/1` respondió 200 con la única cita del Dr. Andrés. | 200 | Págs. 2 y 8 |
| 15 | Reinicio y prueba desde Swagger con Authorize | Datos persisten y flujo protegido funciona | Tras reiniciar, `GET /api/citas` sin token respondió 401. Con un token nuevo de Paula respondió 200 con las mismas citas, propietarios y mascotas. | 200 | Págs. 3, 4, 9 y 11 |

### Descripción de las capturas del PDF

Cada página del PDF se describe aquí para que la evidencia no dependa solo de la imagen:

- **Página 1:** Swagger con `POST /api/citas`. La primera captura muestra el código 400 al repetir una cita con el mismo veterinario y hora ("El veterinario ya tiene una cita en ese horario"); la segunda, el código 400 con el error de campo "La fecha debe ser futura".
- **Página 2:** Swagger con el código 200 en `GET /api/citas` (una sola cita) y en `GET /api/citas/veterinario/1`.
- **Página 3:** Swagger con el código 401 en `GET /api/citas` sin token, antes y después de reiniciar la aplicación.
- **Página 4:** Swagger con el código 200 en `GET /api/citas` con token, y el código 201 al crear un veterinario con el usuario Marta (`ADMIN`).
- **Página 5:** Swagger con el código 403 al crear un veterinario con el usuario Paula (`USER`), y las tablas `mascotas` y `propietarios` en MySQL Workbench.
- **Página 6:** Tabla `veterinarios` en MySQL Workbench y `GET /api/citas` en Postman con código 200.
- **Página 7:** `GET /api/mascotas`, `GET /api/propietarios` y `GET /api/veterinarios` en Postman con código 200.
- **Página 8:** `GET /api/citas/veterinario/1` en Postman con código 200, y Swagger con el código 400 al crear una mascota con un propietario inexistente.
- **Página 9:** Swagger con `GET /api/mascotas` (200) y Postman con el código 201 al crear una cita.
- **Página 10:** Postman con el código 201 al crear una mascota y un propietario.
- **Página 11:** Postman con el código 201 al crear el veterinario Dr. Andrés, y Swagger con `GET /api/propietarios` (200).
- **Página 12:** Swagger con el código 400 en un registro con email inválido y clave corta, y el código 200 en el login de Paula.
- **Página 13:** MySQL Workbench con la tabla `usuarios` (contraseñas como hash BCrypt, roles `USER` y `ADMIN`) y Swagger con el código 200 en un registro válido.
- **Página 14:** Consola de IntelliJ con el arranque de VetTurno: banner de Spring Boot 3.5.16, `Starting VetturnoApplication using Java 17.0.12`, conexión a MySQL 8.0.45 y el mensaje final `Started VetturnoApplication`.

## 12. Evidencias adicionales de las partes 2 a 4

Estas capturas del PDF muestran la persistencia y el modelo relacional:

| Evidencia | Qué demuestra |
|---|---|
| Tablas `propietarios`, `mascotas` y `veterinarios` (págs. 5 y 6) | Los datos quedaron guardados en MySQL. `mascotas.propietario_id` muestra la llave foránea en el lado "muchos". |
| `GET` de propietarios, mascotas, veterinarios y citas en Postman (págs. 6 a 8) | Respuestas 200 con JSON plano, tomadas antes de activar JWT. |
| `POST` de veterinario en Postman (pág. 11) | Creación con respuesta 201, tomada antes de activar JWT. |

## 13. Errores frecuentes

| Problema | Causa probable | Solución |
|---|---|---|
| `Access denied for user 'root'@'localhost'` al iniciar | `DB_USER` o `DB_PASSWORD` no definidos o incorrectos | Revisa las variables de entorno |
| La aplicación no arranca por `jwt.secret` | `JWT_SECRET` no definido o con menos de 32 caracteres | Define una clave más larga |
| `403` con un usuario que ya es `ADMIN` | El token se emitió antes de cambiar el rol | Inicia sesión de nuevo y usa el token nuevo |
| `400 El cuerpo de la petición no es válido` | JSON vacío o mal formado | Envía el cuerpo como JSON válido |
| Swagger no muestra el botón Authorize | `OpenApiConfig` no se cargó | Reinicia la aplicación |
| `401` en todas las rutas | Falta el token o venció (dura una hora) | Inicia sesión otra vez y autoriza con el token nuevo |

## 14. Uso de inteligencia artificial

Me apoyé principalmente en la IA para resolver consultas y dudas sobre cómo debía ser el funcionamiento correcto de todas las clases y paquetes que iba a implementar. También la usé para revisar las relaciones entre las entidades, para diagnosticar la conexión con la base de datos y las variables de entorno, y para la sección de pruebas, con el fin de comprobar que todo saliera correcto, tanto en Postman como en Swagger. En todo momento me apoyé además en el material de las clases que hemos venido trabajando.

## 15. Mejoras futuras

Recordatorios de cita por correo o WhatsApp, historia clínica de cada mascota y una interfaz web para recepción. Quedaron fuera del MVP porque no forman parte del problema central de la agenda y requieren integraciones externas.
