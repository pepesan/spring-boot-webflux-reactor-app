# Spring Boot WebFlux + MongoDB Reactive

Proyecto de ejemplo que demuestra el uso de Spring Boot 4 con programación reactiva usando Project Reactor, Spring WebFlux y MongoDB reactivo.

## Características

- **API REST reactiva** con `Mono` y `Flux` (Spring WebFlux + Spring MVC)
- **MongoDB reactivo** con `ReactiveMongoRepository` y queries derivadas con paginación
- **Relaciones entre colecciones** — cuatro ejemplos progresivos:
  - **1:1** referenciada `flatMap` + `zipWith` (Empleado → Contrato)
  - **1:N** con `flatMapMany` (Proyecto → Tareas)
  - **N:M** con colección intermedia, `flatMapMany` encadenado, `zipWith` y `collectList` (Estudiante ↔ Curso vía Matriculacion)
  - **Embedding** subdocumentos embebidos, `map` síncrono para cálculos, `flatMap` de escritura, query sobre campo embebido (Pedido ↔ LineaPedido)
- **Change Stream + SSE** — MongoDB emite cambios en tiempo real como `Flux` infinito; el controller los reenvía como Server-Sent Events sin polling (requiere Replica Set)
- **Aggregation Pipeline** — `ReactiveMongoTemplate.aggregate()` con `$group`, `$project`, `$sort`, `$limit` para estadísticas y rankings directamente en MongoDB
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

### `/api/relaciones/1a1` — Relación 1:1 referenciada (Empleado → Contrato)

Ejemplo de relación 1:1 entre dos colecciones MongoDB. `Empleado` almacena el campo `contratoId` apuntando al ID del documento en la colección `contratos`. Los tres patrones reactivos clave que ilustra:

| Patrón reactor        | Operación                                              |
| --------------------- | ------------------------------------------------------ |
| `flatMap` de lectura  | Resolver el contrato de un empleado (dos queries)      |
| `flatMap` de escritura| Crear empleado + contrato encadenando las dos escrituras |
| `zipWith`             | Asignar contrato existente (ambas queries en paralelo) |

#### Crear un contrato

```bash
CID=$(curl -s -X POST http://localhost:8080/api/relaciones/1a1/contratos \
  -H "Content-Type: application/json" \
  -d '{"tipo":"INDEFINIDO","fechaInicio":"2024-01-15","salario":35000.0}' | jq -r '.id')
echo "Contrato ID: $CID"
```

#### Crear un empleado sin contrato

```bash
EID=$(curl -s -X POST http://localhost:8080/api/relaciones/1a1/empleados \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana García","puesto":"Desarrolladora Senior"}' | jq -r '.id')
echo "Empleado ID: $EID"
```

#### Asignar el contrato al empleado (`zipWith`)

Recupera ambos documentos en paralelo y actualiza `contratoId` en el empleado:

```bash
curl -s -X PUT "http://localhost:8080/api/relaciones/1a1/empleados/$EID/contrato/$CID" | jq
# {
#   "empleado": { "id": "...", "nombre": "Ana García", "contratoId": "..." },
#   "contrato": { "id": "...", "tipo": "INDEFINIDO", "salario": 35000.0 }
# }
```

#### Obtener empleado con contrato resuelto (`flatMap` de lectura)

Dos queries encadenadas: busca el empleado, luego resuelve el contrato por `contratoId`:

```bash
curl -s "http://localhost:8080/api/relaciones/1a1/empleados/$EID/completo" | jq
```

Si el empleado no tiene `contratoId` asignado, el campo `contrato` aparece como `null` (no devuelve 404):

```bash
EID2=$(curl -s -X POST http://localhost:8080/api/relaciones/1a1/empleados \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Luis Pérez","puesto":"QA"}' | jq -r '.id')

curl -s "http://localhost:8080/api/relaciones/1a1/empleados/$EID2/completo" | jq
# { "empleado": { "id": "...", "nombre": "Luis Pérez", "contratoId": null }, "contrato": null }
```

#### Crear empleado y contrato en una sola llamada (`flatMap` de escritura)

Guarda primero el contrato (obtiene el ID generado), luego guarda el empleado con ese ID ya referenciado:

```bash
curl -s -X POST http://localhost:8080/api/relaciones/1a1/empleados/con-contrato \
  -H "Content-Type: application/json" \
  -d '{
    "empleado": {"nombre":"Carmen Ruiz","puesto":"Product Manager"},
    "contrato": {"tipo":"TEMPORAL","fechaInicio":"2024-06-01","fechaFin":"2025-05-31","salario":28000.0}
  }' | jq
# {
#   "empleado": { "id": "...", "nombre": "Carmen Ruiz", "contratoId": "..." },
#   "contrato": { "id": "...", "tipo": "TEMPORAL", "salario": 28000.0 }
# }
```

#### Listar empleados y contratos

```bash
curl -s http://localhost:8080/api/relaciones/1a1/empleados | jq
curl -s http://localhost:8080/api/relaciones/1a1/contratos | jq
```

#### Eliminar empleado o contrato

```bash
curl -s -X DELETE "http://localhost:8080/api/relaciones/1a1/empleados/$EID" | jq
curl -s -X DELETE "http://localhost:8080/api/relaciones/1a1/contratos/$CID" | jq
```

> **Validaciones:** `nombre` y `puesto` en Empleado no pueden estar vacíos. En Contrato: `tipo` no puede estar vacío, `fechaInicio` y `salario` son obligatorios. `fechaFin` es opcional (null para contratos indefinidos).

---

### `/api/relaciones/1an` — Relación 1:N (Proyecto → Tareas)

Ejemplo de relación 1:N entre dos colecciones MongoDB. Cada `Tarea` almacena un `proyectoId` que la vincula a su `Proyecto`. Los patrones reactivos clave:

| Patrón reactor   | Operación                                                        |
| ---------------- | ---------------------------------------------------------------- |
| `flatMapMany`    | `Mono<Proyecto>` → `Flux<Tarea>` (todas las tareas del proyecto) |
| `flatMap` + DTO  | Proyecto + lista de tareas agregada con `collectList()`          |

#### Crear un proyecto

```bash
PID=$(curl -s -X POST http://localhost:8080/api/relaciones/1an/proyectos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Portal de clientes","descripcion":"Migración del portal legacy a React"}' | jq -r '.id')
echo "Proyecto ID: $PID"
```

#### Añadir tareas al proyecto

```bash
TID1=$(curl -s -X POST http://localhost:8080/api/relaciones/1an/tareas \
  -H "Content-Type: application/json" \
  -d "{\"titulo\":\"Diseñar pantalla de login\",\"estado\":\"PENDIENTE\",\"proyectoId\":\"$PID\"}" | jq -r '.id')

TID2=$(curl -s -X POST http://localhost:8080/api/relaciones/1an/tareas \
  -H "Content-Type: application/json" \
  -d "{\"titulo\":\"Implementar API REST\",\"estado\":\"EN_CURSO\",\"proyectoId\":\"$PID\"}" | jq -r '.id')
```

#### Obtener tareas de un proyecto (`flatMapMany`)

`Mono<Proyecto>` → `Flux<Tarea>`: busca el proyecto y emite todas sus tareas como stream:

```bash
curl -s "http://localhost:8080/api/relaciones/1an/proyectos/$PID/tareas" | jq
# [
#   { "id": "...", "titulo": "Diseñar pantalla de login", "estado": "PENDIENTE", "proyectoId": "..." },
#   { "id": "...", "titulo": "Implementar API REST", "estado": "EN_CURSO", "proyectoId": "..." }
# ]
```

#### Obtener proyecto con todas sus tareas (DTO enriquecido)

`flatMap` + `collectList()`: resuelve el proyecto y agrega todas las tareas en un único DTO:

```bash
curl -s "http://localhost:8080/api/relaciones/1an/proyectos/$PID/completo" | jq
# {
#   "proyecto": { "id": "...", "nombre": "Portal de clientes" },
#   "tareas": [ { "titulo": "Diseñar pantalla de login", ... }, ... ]
# }
```

#### Actualizar estado de una tarea

```bash
curl -s -X PUT "http://localhost:8080/api/relaciones/1an/tareas/$TID1/estado?estado=HECHO" | jq
```

#### Listar proyectos

```bash
curl -s http://localhost:8080/api/relaciones/1an/proyectos | jq
```

#### Eliminar tarea o proyecto

```bash
curl -s -X DELETE "http://localhost:8080/api/relaciones/1an/tareas/$TID1" | jq
curl -s -X DELETE "http://localhost:8080/api/relaciones/1an/proyectos/$PID" | jq
```

> **Validaciones:** `nombre` del proyecto no puede estar vacío. En Tarea: `titulo`, `estado` y `proyectoId` son obligatorios. Estado recomendado: `PENDIENTE`, `EN_CURSO` o `HECHO`.

---

### `/api/relaciones/nm` — Relación N:M con colección intermedia (Estudiante ↔ Curso)

Ejemplo de relación N:M. La colección `matriculaciones` actúa de join table y puede almacenar datos propios de la relación (`fechaAlta`, `nota`). Los patrones reactivos clave:

| Patrón reactor                        | Operación                                                             |
| ------------------------------------- | --------------------------------------------------------------------- |
| `zipWith` de validación               | Verificar que estudiante y curso existen en paralelo antes de matricular |
| `flatMapMany` + `flatMap` encadenados | `Mono<Estudiante>` → `Flux<Matriculacion>` → `Flux<Curso>`            |
| `flatMap` + `zipWith` + `collectList` | DTO enriquecido: matrícula + curso resueltos y agregados en lista     |

#### Crear estudiantes y cursos

```bash
EID=$(curl -s -X POST http://localhost:8080/api/relaciones/nm/estudiantes \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Laura Martínez","email":"laura@ejemplo.com"}' | jq -r '.id')
echo "Estudiante ID: $EID"

CID1=$(curl -s -X POST http://localhost:8080/api/relaciones/nm/cursos \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Spring Boot con WebFlux","descripcion":"Programación reactiva con Project Reactor","plazas":30}' | jq -r '.id')

CID2=$(curl -s -X POST http://localhost:8080/api/relaciones/nm/cursos \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Docker y Kubernetes","plazas":20}' | jq -r '.id')
```

#### Matricular un estudiante en un curso (`zipWith` de validación)

Verifica en paralelo que estudiante y curso existen antes de guardar la matrícula:

```bash
curl -s -X POST http://localhost:8080/api/relaciones/nm/matriculaciones \
  -H "Content-Type: application/json" \
  -d "{\"estudianteId\":\"$EID\",\"cursoId\":\"$CID1\"}" | jq
# { "id": "...", "estudianteId": "...", "cursoId": "...", "fechaAlta": "2024-09-01", "nota": null }

curl -s -X POST http://localhost:8080/api/relaciones/nm/matriculaciones \
  -H "Content-Type: application/json" \
  -d "{\"estudianteId\":\"$EID\",\"cursoId\":\"$CID2\"}" | jq
```

#### Obtener cursos de un estudiante (`flatMapMany` + `flatMap`)

Dos niveles de encadenamiento: `Mono<Estudiante>` → `Flux<Matriculacion>` → `Flux<Curso>`:

```bash
curl -s "http://localhost:8080/api/relaciones/nm/estudiantes/$EID/cursos" | jq
# [
#   { "id": "...", "titulo": "Spring Boot con WebFlux", "plazas": 30 },
#   { "id": "...", "titulo": "Docker y Kubernetes", "plazas": 20 }
# ]
```

#### Obtener estudiante con todas sus matrículas resueltas (`flatMap` + `zipWith` + `collectList`)

Para cada matrícula combina el curso completo usando `zipWith` y agrega el resultado con `collectList`:

```bash
curl -s "http://localhost:8080/api/relaciones/nm/estudiantes/$EID/completo" | jq
# {
#   "estudiante": { "id": "...", "nombre": "Laura Martínez", "email": "laura@ejemplo.com" },
#   "matriculas": [
#     {
#       "matriculacion": { "id": "...", "fechaAlta": "2024-09-01", "nota": null },
#       "curso": { "titulo": "Spring Boot con WebFlux", "plazas": 30 }
#     },
#     ...
#   ]
# }
```

#### Obtener estudiantes de un curso (patrón simétrico)

```bash
curl -s "http://localhost:8080/api/relaciones/nm/cursos/$CID1/estudiantes" | jq
```

#### Actualizar la nota de una matrícula

```bash
curl -s -X PUT "http://localhost:8080/api/relaciones/nm/matriculaciones/$EID/$CID1/nota?nota=8.5" | jq
# { "id": "...", "estudianteId": "...", "cursoId": "...", "fechaAlta": "...", "nota": 8.5 }
```

#### Desmatricular un estudiante

```bash
curl -s -X DELETE "http://localhost:8080/api/relaciones/nm/matriculaciones/$EID/$CID2" | jq
```

#### Listar estudiantes y cursos

```bash
curl -s http://localhost:8080/api/relaciones/nm/estudiantes | jq
curl -s http://localhost:8080/api/relaciones/nm/cursos | jq
```

> **Validaciones:** `nombre` y `email` del estudiante son obligatorios (`email` debe tener formato válido). En Curso: `titulo` y `plazas` son obligatorios, `plazas` debe ser ≥ 1. En la petición de matrícula: `estudianteId` y `cursoId` no pueden estar vacíos. Si el estudiante ya está matriculado en el curso, devuelve `409 Conflict`.

---

### `/api/relaciones/embebido` — Embedding (Pedido con líneas embebidas)

Ejemplo de **embedding** en MongoDB: las `LineaPedido` se almacenan como subdocumentos directamente dentro del documento `Pedido`, sin colección separada. Un único `find` carga el pedido completo con todas sus líneas. Los patrones reactivos clave:

| Patrón reactor        | Operación                                                                        |
| --------------------- | -------------------------------------------------------------------------------- |
| `map` síncrono        | Calcular el total sobre líneas ya cargadas (sin I/O adicional)                   |
| `flatMap` de escritura| Añadir línea al array embebido y persistir el documento completo con un `save()` |
| Query sobre embebido  | `findByLineasProducto` — MongoDB filtra por campo de subdocumento                |

#### Crear un pedido (con líneas opcionales)

```bash
PID=$(curl -s -X POST http://localhost:8080/api/relaciones/embebido/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cliente":"David Vaquero","fecha":"2024-01-15","estado":"PENDIENTE"}' | jq -r '.id')
echo "Pedido ID: $PID"
```

También se puede crear el pedido con líneas ya incluidas:

```bash
PID2=$(curl -s -X POST http://localhost:8080/api/relaciones/embebido/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "Ana García",
    "fecha": "2024-03-10",
    "estado": "PENDIENTE",
    "lineas": [
      {"producto": "Teclado mecánico", "cantidad": 2, "precioUnitario": 89.99},
      {"producto": "Ratón inalámbrico", "cantidad": 1, "precioUnitario": 29.99}
    ]
  }' | jq -r '.id')
```

#### Añadir una línea a un pedido existente (`flatMap` de escritura)

Busca el pedido, añade la línea al array embebido y persiste el documento completo en un único `save()`:

```bash
curl -s -X POST "http://localhost:8080/api/relaciones/embebido/pedidos/$PID/lineas" \
  -H "Content-Type: application/json" \
  -d '{"producto":"Teclado mecánico","cantidad":2,"precioUnitario":89.99}' | jq

curl -s -X POST "http://localhost:8080/api/relaciones/embebido/pedidos/$PID/lineas" \
  -H "Content-Type: application/json" \
  -d '{"producto":"Ratón inalámbrico","cantidad":1,"precioUnitario":29.99}' | jq
# {
#   "id": "...",
#   "cliente": "David Vaquero",
#   "estado": "PENDIENTE",
#   "lineas": [
#     {"producto": "Teclado mecánico", "cantidad": 2, "precioUnitario": 89.99},
#     {"producto": "Ratón inalámbrico", "cantidad": 1, "precioUnitario": 29.99}
#   ]
# }
```

#### Obtener pedido con importe total calculado (`map` síncrono)

Como las líneas están embebidas, el total se calcula con `map` sobre el documento ya cargado — sin segunda query:

```bash
curl -s "http://localhost:8080/api/relaciones/embebido/pedidos/$PID/total" | jq
# {
#   "pedido": { "id": "...", "cliente": "David Vaquero", "lineas": [...] },
#   "total": 209.97
# }
```

#### Filtrar pedidos por estado

```bash
curl -s "http://localhost:8080/api/relaciones/embebido/pedidos/porEstado/PENDIENTE" | jq
```

#### Buscar pedidos por producto en líneas embebidas (query sobre subdocumento)

MongoDB evalúa el filtro directamente sobre el array embebido `lineas.producto` sin JOIN ni colección secundaria:

```bash
curl -s "http://localhost:8080/api/relaciones/embebido/pedidos/porProducto/Teclado%20mec%C3%A1nico" | jq
# [
#   { "id": "...", "cliente": "David Vaquero", "lineas": [{ "producto": "Teclado mecánico", ... }] },
#   ...
# ]
```

#### Actualizar el estado de un pedido

```bash
curl -s -X PUT "http://localhost:8080/api/relaciones/embebido/pedidos/$PID/estado?estado=ENVIADO" | jq
# { "id": "...", "cliente": "David Vaquero", "estado": "ENVIADO", ... }
```

#### Listar todos los pedidos

```bash
curl -s http://localhost:8080/api/relaciones/embebido/pedidos | jq
```

#### Obtener pedido por ID

```bash
curl -s "http://localhost:8080/api/relaciones/embebido/pedidos/$PID" | jq
```

#### Eliminar un pedido

```bash
curl -s -X DELETE "http://localhost:8080/api/relaciones/embebido/pedidos/$PID" | jq
```

> **Validaciones:** `cliente`, `fecha` y `estado` del pedido son obligatorios. En `LineaPedido`: `producto` no puede estar en blanco, `cantidad` debe ser ≥ 1 y `precioUnitario` es obligatorio.

---

### `/api/changestream` — Change Stream + SSE (Notificaciones en tiempo real)

Ejemplo de **MongoDB Change Stream** combinado con **Server-Sent Events**. Cada vez que se crea o modifica un documento en la colección `notificaciones`, MongoDB emite un evento que el servidor reenvía al cliente como SSE sin necesidad de polling.

> **Requisito previo — Replica Set:** El Change Stream no funciona en MongoDB standalone. El `compose.yaml` del directorio `docker/` arranca MongoDB en modo standalone; usa los scripts numerados para hacer la transición antes de probar este ejemplo.

#### Activar Replica Set (ejecutar desde `docker/` una sola vez)

| Script | Acción |
| ------ | ------ |
| `21_stop_mongo.sh` | Para y elimina el contenedor standalone |
| `22_start_replica.sh` | Arranca mongo con `compose.replica.yaml` (Replica Set `rs0`) |
| `23_init_replicaset.sh` | Inicializa el Replica Set con `rs.initiate()` |
| `24_test_replicaset.sh` | Inserta una notificación de prueba para verificar el Change Stream |
| `25_restore_standalone.sh` | Vuelve al contenedor standalone original |

```bash
cd docker
./21_stop_mongo.sh
./22_start_replica.sh
./23_init_replicaset.sh
```

Para volver a standalone:

```bash
cd docker
./25_restore_standalone.sh
```

> Una vez inicializado el Replica Set la aplicación Spring Boot no necesita reiniciarse: el driver MongoDB reactivo detecta la topología automáticamente.

---

| Patrón                  | Descripción                                                          |
| ----------------------- | -------------------------------------------------------------------- |
| `changeStream()`        | `ReactiveMongoTemplate` devuelve un `Flux<ChangeStreamEvent<T>>` infinito |
| `mapNotNull(getBody())` | Extrae el documento del evento y filtra eventos sin cuerpo (invalidate, etc.) |
| `TEXT_EVENT_STREAM`     | El controller reenvía el `Flux<Notificacion>` como SSE               |

#### Abrir el stream en un terminal (queda escuchando)

```bash
curl -s -N -H "Accept: text/event-stream" http://localhost:8080/api/changestream/notificaciones/stream
# (queda bloqueado esperando eventos)
```

#### Crear notificaciones desde otro terminal (el primero las recibe al instante)

```bash
curl -s -X POST http://localhost:8080/api/changestream/notificaciones \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Sistema actualizado","mensaje":"Versión 2.0 disponible","tipo":"INFO","fecha":"2024-01-15T10:30:00"}' | jq

curl -s -X POST http://localhost:8080/api/changestream/notificaciones \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Disco casi lleno","mensaje":"Quedan menos de 5 GB libres","tipo":"ALERTA","fecha":"2024-01-15T11:00:00"}' | jq

curl -s -X POST http://localhost:8080/api/changestream/notificaciones \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Base de datos caída","mensaje":"No se puede conectar al servidor principal","tipo":"ERROR","fecha":"2024-01-15T11:15:00"}' | jq
```

El primer terminal mostrará cada notificación en el momento en que se inserte:

```
data:{"id":"...","titulo":"Sistema actualizado","tipo":"INFO","fecha":"2024-01-15T10:30:00"}

data:{"id":"...","titulo":"Disco casi lleno","tipo":"ALERTA","fecha":"2024-01-15T11:00:00"}

data:{"id":"...","titulo":"Base de datos caída","tipo":"ERROR","fecha":"2024-01-15T11:15:00"}
```

#### Listar todas las notificaciones almacenadas

```bash
curl -s http://localhost:8080/api/changestream/notificaciones | jq
```

> **Validaciones:** `titulo`, `mensaje`, `tipo` y `fecha` son obligatorios y no pueden estar en blanco.

---

### `/api/agregacion` — Aggregation Pipeline (Ventas)

Ejemplo de **MongoDB Aggregation Pipeline** con `ReactiveMongoTemplate.aggregate()`. En lugar de queries derivadas sobre documentos individuales, el pipeline procesa toda la colección en el servidor MongoDB y devuelve resultados calculados.

| Etapa del pipeline | Operación                                                       |
| ------------------ | --------------------------------------------------------------- |
| `$group`           | Agrupa documentos por campo y aplica acumuladores               |
| `$sum` / `$avg`    | Acumuladores para sumar e calcular media sobre el grupo         |
| `$count`           | Cuenta los documentos de cada grupo                             |
| `$project`         | Renombra `_id` a `categoria`/`producto` y excluye campos extra  |
| `$sort`            | Ordena los grupos resultantes                                   |
| `$limit`           | Limita el número de resultados (para rankings top-N)            |

#### Insertar ventas de ejemplo

```bash
for producto_cat_precio in \
  "Portátil gaming:ELECTRONICA:1299.99" \
  "Monitor 4K:ELECTRONICA:599.99" \
  "Teclado mecánico:ELECTRONICA:89.99" \
  "Portátil gaming:ELECTRONICA:1299.99" \
  "Zapatillas running:DEPORTE:129.99" \
  "Camiseta técnica:DEPORTE:39.99" \
  "Zapatillas running:DEPORTE:129.99" \
  "Sofá 3 plazas:HOGAR:899.99" \
  "Lámpara LED:HOGAR:49.99"; do
  IFS=':' read -r producto categoria importe <<< "$producto_cat_precio"
  curl -s -X POST http://localhost:8080/api/agregacion/ventas \
    -H "Content-Type: application/json" \
    -d "{\"producto\":\"$producto\",\"categoria\":\"$categoria\",\"importe\":$importe,\"fecha\":\"2024-01-15\"}" > /dev/null
done
echo "Ventas insertadas"
```

#### Resumen estadístico por categoría (`$group` + `$project` + `$sort`)

Calcula para cada categoría: suma total, media y número de ventas. Resultado ordenado de mayor a menor importe:

```bash
curl -s http://localhost:8080/api/agregacion/ventas/resumen | jq
# [
#   { "categoria": "ELECTRONICA", "totalVentas": 3289.96, "mediaImporte": 822.49, "numVentas": 4 },
#   { "categoria": "HOGAR",       "totalVentas": 949.98,  "mediaImporte": 474.99, "numVentas": 2 },
#   { "categoria": "DEPORTE",     "totalVentas": 299.97,  "mediaImporte": 99.99,  "numVentas": 3 }
# ]
```

#### Ranking top-N de productos por importe total (`$group` + `$sort` + `$limit`)

Agrupa por producto, acumula el importe total y devuelve los primeros N:

```bash
# Top 3 por defecto
curl -s "http://localhost:8080/api/agregacion/ventas/top?limite=3" | jq
# [
#   { "producto": "Portátil gaming", "totalImporte": 2599.98 },
#   { "producto": "Sofá 3 plazas",   "totalImporte": 899.99  },
#   { "producto": "Monitor 4K",      "totalImporte": 599.99  }
# ]

# Top 5 (valor por defecto si se omite el parámetro)
curl -s "http://localhost:8080/api/agregacion/ventas/top" | jq
```

#### Listar todas las ventas

```bash
curl -s http://localhost:8080/api/agregacion/ventas | jq
```

> **Validaciones:** `producto`, `categoria`, `importe` y `fecha` son obligatorios. `importe` no puede ser nulo.

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

| Clase                            | Tipo                                | Tests |
| -------------------------------- | ----------------------------------- | ----- |
| `PersonTest`                     | Unitario — dominio                  | 14    |
| `PersonDTOTest`                  | Unitario — dto                      | 13    |
| `PersonaTest`                    | Unitario — dominio                  | 5     |
| `EmpleadoTest`                   | Unitario — dominio 1:1              | 9     |
| `ContratoTest`                   | Unitario — dominio 1:1              | 10    |
| `ProyectoTest`                   | Unitario — dominio 1:N              | 8     |
| `TareaTest`                      | Unitario — dominio 1:N              | 9     |
| `EstudianteTest`                 | Unitario — dominio N:M              | 9     |
| `CursoTest`                      | Unitario — dominio N:M              | 10    |
| `MatriculacionTest`              | Unitario — dominio N:M              | 10    |
| `MainControllerTest`             | Unitario — controller               | 3     |
| `PersonSimpleRestControllerTest` | Unitario — controller               | 9     |
| `PersonRestControllerTest`       | Unitario — controller               | 11    |
| `TemperatureControllerTest`      | Unitario — controller               | 2     |
| `EmpleadoControllerTest`         | Unitario — controller 1:1           | 13    |
| `ProyectoControllerTest`         | Unitario — controller 1:N           | 12    |
| `EstudianteControllerTest`       | Unitario — controller N:M           | 17    |
| `ClienteControllerTest`          | Unitario — controller               | 8     |
| `PersonServiceTest`              | Unitario — servicio                 | 7     |
| `EmpleadoServiceTest`            | Unitario — servicio 1:1             | 8     |
| `ProyectoServiceTest`            | Unitario — servicio 1:N             | 13    |
| `MatriculacionServiceTest`       | Unitario — servicio N:M             | 20    |
| `LineaPedidoTest`                | Unitario — dominio embebido         | 9     |
| `PedidoTest`                     | Unitario — dominio embebido         | 10    |
| `PedidoServiceTest`              | Unitario — servicio embebido        | 15    |
| `PedidoControllerTest`           | Unitario — controller embebido      | 14    |
| `NotificacionTest`               | Unitario — dominio change stream    | 10    |
| `NotificacionServiceTest`        | Unitario — servicio change stream   | 5     |
| `NotificacionControllerTest`     | Unitario — controller change stream | 6     |
| `VentaTest`                      | Unitario — dominio aggregation      | 10    |
| `VentaServiceTest`               | Unitario — servicio aggregation     | 6     |
| `VentaControllerTest`            | Unitario — controller aggregation   | 8     |
| `WebSocketHandlerTest`           | Unitario — websocket                | 3     |
| `GlobalExceptionHandlerTest`     | Unitario — manejo de errores        | 8     |
| `ClienteTest`                    | Unitario — dominio ejercicios       | 9     |
| `StringFluxCreatorTest`          | Unitario — reactor                  | 1     |
| `AceptacionTest`                 | Aceptación (servidor completo)      | 5     |
| `AceptacionReactivaTest`         | Aceptación reactiva (WebTestClient) | 5     |
| **Total**                        |                                     | **344** |

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
│   │   └── PersonClientRestController.java      # Proxy WebClient → /api/persons
│   ├── controllers/
│   │   ├── MainController.java                  # Ejemplos básicos Mono/Flux
│   │   ├── PersonRestController.java            # CRUD MongoDB reactivo
│   │   ├── PersonSimpleRestController.java      # CRUD en memoria
│   │   ├── TemperatureController.java           # SSE de temperaturas
│   │   ├── unoauno/
│   │   │   └── EmpleadoController.java          # 1:1 Empleados / Contratos
│   │   ├── unoaene/
│   │   │   └── ProyectoController.java          # 1:N Proyectos / Tareas
│   │   ├── naem/
│   │   │   └── EstudianteController.java        # N:M Estudiantes / Cursos / Matriculaciones
│   │   └── embebido/
│   │       └── PedidoController.java            # Embedding Pedidos / Líneas embebidas
│   ├── domain/
│   │   ├── Person.java                          # Entidad MongoDB
│   │   ├── Persona.java                         # Ejemplo de dominio simple
│   │   ├── unoauno/
│   │   │   ├── Empleado.java
│   │   │   └── Contrato.java
│   │   ├── unoaene/
│   │   │   ├── Proyecto.java
│   │   │   └── Tarea.java
│   │   ├── naem/
│   │   │   ├── Estudiante.java
│   │   │   ├── Curso.java
│   │   │   └── Matriculacion.java               # Colección intermedia N:M
│   │   └── embebido/
│   │       ├── Pedido.java                      # Documento raíz con lista embebida
│   │       └── LineaPedido.java                 # Subdocumento embebido (sin @Document)
│   ├── dto/
│   │   ├── PersonDTO.java
│   │   ├── unoauno/
│   │   │   ├── EmpleadoConContratoDTO.java
│   │   │   └── CrearEmpleadoConContratoRequest.java
│   │   ├── unoaene/
│   │   │   └── ProyectoConTareasDTO.java
│   │   ├── naem/
│   │   │   ├── MatriculacionConCursoDTO.java    # Matrícula + curso resuelto
│   │   │   ├── EstudianteConMatriculasDTO.java  # Estudiante + lista de matrículas resueltas
│   │   │   └── MatricularRequest.java
│   │   └── embebido/
│   │       └── PedidoConTotalDTO.java           # Pedido + total calculado con map
│   ├── repositories/
│   │   ├── ReactivePersonRepository.java
│   │   ├── unoauno/
│   │   │   ├── EmpleadoRepository.java
│   │   │   └── ContratoRepository.java
│   │   ├── unoaene/
│   │   │   ├── ProyectoRepository.java
│   │   │   └── TareaRepository.java
│   │   ├── naem/
│   │   │   ├── EstudianteRepository.java
│   │   │   ├── CursoRepository.java
│   │   │   └── MatriculacionRepository.java
│   │   └── embebido/
│   │       └── PedidoRepository.java            # findByEstado + findByLineasProducto
│   ├── services/
│   │   ├── PersonService.java
│   │   ├── unoauno/
│   │   │   └── EmpleadoService.java             # flatMap + zipWith (1:1)
│   │   ├── unoaene/
│   │   │   └── ProyectoService.java             # flatMapMany (1:N)
│   │   ├── naem/
│   │   │   └── MatriculacionService.java        # flatMapMany + zipWith + collectList (N:M)
│   │   └── embebido/
│   │       └── PedidoService.java               # map síncrono + flatMap escritura (embedding)
│   └── websocket/
│       ├── WebSocketConfiguration.java
│       └── WebSocketHandler.java
├── ejercicios/
│   ├── Cliente.java
│   ├── ClienteController.java
│   └── ClienteRepository.java
└── exception/
    ├── ErrorResponse.java
    └── GlobalExceptionHandler.java              # @RestControllerAdvice centralizado
```

---

## MongoDB — Scripts de administración

El directorio `docker/` contiene scripts para gestionar el servidor MongoDB:

```bash
docker/01_launch.sh              # Levantar MongoDB (standalone)
docker/02_check_ps.sh            # Ver contenedores activos
docker/03_check_logs.sh          # Ver logs de MongoDB
docker/04_connect.sh             # Conectar al shell de MongoDB
docker/05_stop.sh                # Parar MongoDB
docker/21_stop_mongo.sh          # Para y elimina el contenedor (previo a replica set)
docker/22_start_replica.sh       # Arranca MongoDB con Replica Set (compose.replica.yaml)
docker/23_init_replicaset.sh     # Inicializa el Replica Set (rs.initiate)
docker/24_test_replicaset.sh     # Inserta una notificación de prueba para verificar el Change Stream
docker/25_restore_standalone.sh  # Restaura MongoDB a modo standalone
docker/30_destroy.sh             # Eliminar contenedor y datos
```
