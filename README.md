# Spring Boot WebFlux + MongoDB Reactive

Proyecto de ejemplo que demuestra el uso de Spring Boot 4 con programación reactiva usando Project Reactor, Spring WebFlux y MongoDB reactivo.

## Características

- **API REST reactiva** con `Mono` y `Flux` (Spring WebFlux + Spring MVC)
- **MongoDB reactivo** con `ReactiveMongoRepository` y queries derivadas con paginación
- **WebClient** como cliente HTTP reactivo entre servicios
- **Server-Sent Events (SSE)** para streaming de datos en tiempo real
- **WebSocket** para comunicación bidireccional
- **Swagger UI** para explorar la API de forma interactiva
- **Actuator** para monitorización

## Tecnologías

| Tecnología                  | Versión |
| --------------------------- | ------- |
| Java                        | 25      |
| Spring Boot                 | 4.0.6   |
| Spring WebFlux / Spring MVC | 7.0.x   |
| MongoDB Driver              | 5.6.x   |
| Project Reactor             | 3.8.x   |
| springdoc-openapi           | 3.0.3   |
| Testcontainers              | 2.0.5   |

---

## Requisitos previos

- **Java 25+**
- **Maven 3.9+**
- **Docker** (para MongoDB y para tests de integración)

---

## Arranque rápido

### 1. Levantar MongoDB con Docker

```bash
cd docker
./00_init.sh
./01_launch.sh
```

### 2. Arrancar la aplicación

```bash
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

---

## Interfaces web

| URL                                         | Descripción                           |
| ------------------------------------------- | ------------------------------------- |
| http://localhost:8080/swagger-ui/index.html | Swagger UI — explorar y probar la API |
| http://localhost:8080/v3/api-docs           | Especificación OpenAPI en JSON        |
| http://localhost:8080/index.html            | Cliente WebSocket                     |
| http://localhost:8080/sse.html              | Cliente Server-Sent Events            |
| http://localhost:8080/actuator/health       | Health check                          |

---

## Endpoints y ejemplos con curl

### `/api/persons-simple` — Personas en memoria

Datos en memoria, sin base de datos. Útil para probar el comportamiento reactivo sin infraestructura.

#### Listar todas las personas
```bash
curl -s http://localhost:8080/api/persons-simple | jq
```

#### Crear una persona
```bash
curl -s -X POST http://localhost:8080/api/persons-simple \
  -H "Content-Type: application/json" \
  -d '{"name": "Lorena", "lastName": "Reyes"}' | jq
```

#### Obtener persona por ID
```bash
curl -s http://localhost:8080/api/persons-simple/1L | jq
```

#### Actualizar persona por ID
```bash
curl -s -X PUT http://localhost:8080/api/persons-simple/1L \
  -H "Content-Type: application/json" \
  -d '{"name": "David", "lastName": "CowBoy"}' | jq
```

#### Eliminar persona por ID
```bash
curl -s -X DELETE http://localhost:8080/api/persons-simple/1L | jq
```

#### Resetear los datos
```bash
curl -s http://localhost:8080/api/persons-simple/clear
```

> **Validaciones:** `name` debe tener entre 4 y 20 caracteres y no ser nulo. `lastName` no puede ser nulo.

---

### `/api/persons` — Personas en MongoDB

CRUD completo persistido en MongoDB. Requiere MongoDB en marcha.

#### Listar todas las personas
```bash
curl -s http://localhost:8080/api/persons | jq
```

#### Crear una persona
```bash
curl -s -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name": "David", "lastName": "Vaquero"}' | jq
```

Guarda el `id` devuelto para usarlo en las siguientes peticiones:
```bash
ID=$(curl -s -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name": "Maria", "lastName": "Lopez"}' | jq -r '.id')
echo "ID creado: $ID"
```

#### Obtener persona por ID
```bash
curl -s http://localhost:8080/api/persons/$ID | jq
```

#### Actualizar persona por ID
```bash
curl -s -X PUT http://localhost:8080/api/persons/$ID \
  -H "Content-Type: application/json" \
  -d '{"name": "Maria", "lastName": "Garcia"}' | jq
```

#### Eliminar persona por ID
```bash
curl -s -X DELETE http://localhost:8080/api/persons/$ID | jq
```

#### Buscar por nombre (con paginación)

> **Nota:** Requiere datos en la base de datos. Si no hay ninguna persona con ese nombre, devuelve `[]`.

Primero inserta varias personas con el mismo nombre para tener datos de prueba:

```bash
for apellido in Vaquero Garcia Lopez Martinez Ruiz Fernandez; do
  curl -s -X POST http://localhost:8080/api/persons \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"David\", \"lastName\": \"$apellido\"}" | jq -r '.id'
done
```

Sin datos (o sin coincidencias) la respuesta es un array vacío:

```bash
curl -s "http://localhost:8080/api/persons/byName/David?page=0&size=10" | jq
# []
```

Con datos insertados:

```bash
# Primera página, hasta 10 resultados
curl -s "http://localhost:8080/api/persons/byName/David?page=0&size=10" | jq
# [
#   { "id": "...", "name": "David", "lastName": "Vaquero" },
#   { "id": "...", "name": "David", "lastName": "Garcia" },
#   ...
# ]

# Segunda página, hasta 5 resultados
curl -s "http://localhost:8080/api/persons/byName/David?page=1&size=5" | jq
```

#### Buscar por nombre ordenado por apellido

```bash
curl -s "http://localhost:8080/api/persons/byNameResponseEntity/David?page=0&size=10" | jq
# []   ← si no hay datos
# [{ "id": "...", "name": "David", "lastName": "Fernandez" }, ...]  ← con datos, ordenado A→Z
```

#### Streaming SSE de todas las personas

Emite todas las personas de la BD como eventos SSE cada 2 segundos de forma continua. Requiere datos previos. Pulsa `Ctrl+C` para parar.

```bash
# Inserta datos si aún no los tienes
for apellido in Vaquero Garcia Lopez; do
  curl -s -X POST http://localhost:8080/api/persons \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"David\", \"lastName\": \"$apellido\"}" > /dev/null
done

# Inicia el stream SSE
curl -s -N -H "Accept: text/event-stream" http://localhost:8080/api/persons/stream
# data:{"id":"...","name":"David","lastName":"Vaquero"}
# data:{"id":"...","name":"David","lastName":"Garcia"}
# data:{"id":"...","name":"David","lastName":"Lopez"}
#
# data:{"id":"...","name":"David","lastName":"Vaquero"}   ← repite cada 2 s
# ...
```

---

### `/api/persons/client` — Proxy WebClient

Controller que actúa como proxy hacia `/api/persons` usando WebClient reactivo.

#### Listar vía WebClient
```bash
curl -s http://localhost:8080/api/persons/client | jq
```

#### Crear vía WebClient
```bash
ID=$(curl -s -X POST http://localhost:8080/api/persons/client \
  -H "Content-Type: application/json" \
  -d '{"name": "Carlos", "lastName": "Ruiz"}' | jq -r '.id')
echo "ID creado: $ID"
```

#### Obtener por ID vía WebClient
```bash
curl -s http://localhost:8080/api/persons/client/$ID | jq
```

#### Actualizar por ID vía WebClient
```bash
curl -s -X PUT http://localhost:8080/api/persons/client/$ID \
  -H "Content-Type: application/json" \
  -d '{"name": "Carlos", "lastName": "Ruiz Actualizado"}' | jq
```

#### Eliminar por ID vía WebClient
```bash
curl -s -X DELETE http://localhost:8080/api/persons/client/$ID | jq
```

---

### `/api/main` — Ejemplos básicos Mono/Flux

Ejemplos didácticos de retorno de `Mono` y `Flux` desde un controller.

#### Obtener una persona (Mono)
```bash
curl -s http://localhost:8080/api/main | jq
```

#### Obtener lista de personas (Flux<List>)
```bash
curl -s http://localhost:8080/api/main/list | jq
```

#### Obtener personas como iterable (Flux<Person>)
```bash
curl -s http://localhost:8080/api/main/list-iterable | jq
```

---

### `/api/clientes` — Clientes en MongoDB

CRUD de clientes persistidos en MongoDB (colección `clientes`).

#### Listar todos los clientes
```bash
curl -s http://localhost:8080/api/clientes | jq
```

#### Crear un cliente
```bash
curl -s -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Empresa ABC", "direccion": "Calle Mayor 1", "tlf": "600000000", "correo": "abc@empresa.com"}' | jq

CLI=$(curl -s -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Empresa XYZ", "direccion": "Av. Diagonal 100", "tlf": "611111111", "correo": "xyz@empresa.com"}' | jq -r '.id')
```

#### Obtener cliente por ID
```bash
curl -s http://localhost:8080/api/clientes/$CLI | jq
```

#### Actualizar cliente por ID
```bash
curl -s -X POST http://localhost:8080/api/clientes/$CLI \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Empresa XYZ Actualizada", "direccion": "Av. Diagonal 200", "tlf": "622222222", "correo": "nuevo@empresa.com"}' | jq
```

#### Eliminar cliente por ID
```bash
curl -s -X DELETE http://localhost:8080/api/clientes/$CLI | jq
```

> **Validaciones:** `nombre` es obligatorio y no puede estar vacío.

---

### `/temperatures` — Streaming de temperaturas (SSE)

Genera un stream infinito de temperaturas aleatorias (0–49 °C) cada segundo. No requiere datos en base de datos.

#### Desde el navegador

Abre `http://localhost:8080/sse.html`: la página se conecta automáticamente al endpoint `/temperatures` usando la API `EventSource` del navegador y muestra cada temperatura recibida en pantalla en tiempo real.

#### Desde la CLI

```bash
# Ver eventos en crudo (Ctrl+C para parar)
curl -s -N -H "Accept: text/event-stream" http://localhost:8080/temperatures

# Capturar solo los primeros 5 valores
curl -s -N -H "Accept: text/event-stream" http://localhost:8080/temperatures | head -10
# data:23
# 
# data:41
# 
# data:7
# ...
```

---

### WebSocket — Canal bidireccional

El servidor expone un WebSocket de eco en `ws://localhost:8080/socket`. Cualquier mensaje de texto o imagen binaria que envíes es devuelto tal cual por el servidor.

#### Desde el navegador

Abre `http://localhost:8080/index.html`:

- **Texto:** escribe un mensaje en el campo de texto y pulsa *Send* — verás el eco en el panel de mensajes.
- **Imagen:** selecciona un fichero de imagen y pulsa *Send Image* — el servidor la devuelve y se renderiza en el panel.

#### Desde la CLI con `wscat`

```bash
# Instalar wscat (requiere Node.js)
npm install -g wscat

# Conectar y enviar mensajes
wscat -c ws://localhost:8080/socket
# Connected (press CTRL+C to quit)
# > hola mundo
# < hola mundo
# > action
# < action
```

#### Desde la CLI con `websocat`

```bash
# websocat: https://github.com/vi/websocat
# Ubuntu/Debian: apt install websocat

websocat ws://localhost:8080/socket
# hola mundo     ← escribe y pulsa Enter
# hola mundo     ← eco del servidor
```

> El handler también devuelve mensajes binarios intactos — útil para enviar imágenes o cualquier payload binario.

---

### Actuator

```bash
# Estado de la aplicación
curl -s http://localhost:8080/actuator/health | jq

# Links disponibles
curl -s http://localhost:8080/actuator | jq
```

---

## Tests

El proyecto tiene tres niveles de tests:

### Tests unitarios y de aceptación

```bash
mvn test
```

| Clase                            | Tipo                                | Tests  |
| -------------------------------- | ----------------------------------- | ------ |
| `PersonTest`                     | Unitario — dominio                  | 14     |
| `PersonDTOTest`                  | Unitario — dominio                  | 13     |
| `PersonaTest`                    | Unitario — dominio                  | 5      |
| `MainControllerTest`             | Unitario — controller               | 3      |
| `PersonSimpleRestControllerTest` | Unitario — controller               | 9      |
| `PersonRestControllerTest`       | Unitario — controller               | 10     |
| `PersonServiceTest`              | Unitario — servicio                 | 7      |
| `TemperatureControllerTest`      | Unitario — controller               | 2      |
| `AceptacionTest`                 | Aceptación (servidor completo)      | 5      |
| `AceptacionReactivaTest`         | Aceptación reactiva (WebTestClient) | 5      |
| **Total**                        |                                     | **73** |

### Tests de integración (requieren Docker)

```bash
mvn verify
```

| Clase                        | Tipo                                                      | Tests |
| ---------------------------- | --------------------------------------------------------- | ----- |
| `ReactivePersonRepositoryIT` | Integración — repositorio + MongoDB real (Testcontainers) | 8     |

Los tests de integración levantan un contenedor MongoDB automáticamente mediante Testcontainers y verifican las queries derivadas reales (`findByName`, `findByNameOrderByLastName`) contra una base de datos real.

---

## Estructura del proyecto

```
src/main/java/com/cursosdedesarrollo/webfluxapp/
├── WebfluxAppApplication.java
├── ejemplo/
│   ├── client/
│   │   └── PersonClientRestController.java   # Proxy WebClient → /api/persons
│   ├── controllers/
│   │   ├── MainController.java               # Ejemplos básicos Mono/Flux
│   │   ├── PersonRestController.java         # CRUD MongoDB reactivo
│   │   ├── PersonSimpleRestController.java   # CRUD en memoria
│   │   └── TemperatureController.java        # SSE de temperaturas
│   ├── domain/
│   │   ├── Person.java                       # Entidad MongoDB
│   │   ├── PersonDTO.java                    # DTO de entrada
│   │   └── Persona.java                      # Ejemplo de dominio simple
│   ├── repositories/
│   │   └── ReactivePersonRepository.java     # ReactiveMongoRepository
│   ├── services/
│   │   └── PersonService.java                # Functional endpoints handler
│   └── websocket/
│       ├── WebSocketConfiguration.java
│       └── WebSocketHandler.java
└── ejercicios/
    ├── Cliente.java
    ├── ClienteController.java                # CRUD MongoDB reactivo
    └── ClienteRepository.java
```

---

## MongoDB — Scripts de administración

El directorio `docker/` contiene scripts para gestionar el servidor MongoDB:

```bash
docker/01_launch.sh         # Levantar MongoDB
docker/02_check_ps.sh       # Ver contenedores activos
docker/03_check_logs.sh     # Ver logs de MongoDB
docker/04_connect.sh        # Conectar al shell de MongoDB
docker/05_stop.sh           # Parar MongoDB
docker/30_destroy.sh        # Eliminar contenedor y datos
```
