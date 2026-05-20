# HuerteandoApp — Pruebas manuales (Postman Web + Desktop Agent / curl)

## 1) Objetivo
Documentar un set de pruebas manuales (MVP+ con imágenes) contra la API REST del backend, alineado con los endpoints reales del proyecto.

## 2) Entornos
### 2.1 Local (H2)
- URL base: `http://localhost:8080`
- Perfil recomendado: `h2`
- Seed de datos: `src/main/resources/db/demo-h2.sql` (incluye usuarios demo, observaciones, comentarios, “me gustas” e imágenes de ejemplo con URLs fake).

### 2.2 Remoto (Render)
- URL base: `https://huerteandoapp-1.onrender.com`
- Útil para evidencias “realistas” (red + servicios externos).
- Nota: la subida real de imágenes depende de credenciales/servicio (Supabase).

## 3) Herramienta: Postman Web + Desktop Agent
- Si usas Postman Web para llamar a `localhost`, necesitas el **Desktop Agent**.
- Alternativa: Postman Desktop o `curl`.

## 4) Convenciones de evidencias
- Guardar capturas en una carpeta tipo: `docs/evidencias/`
- Cada caso debe tener evidencia de:
  - Status code
  - Response body (o vacío si 204)
- Nombre sugerido: `CP-XX_nombre.png`

## 5) Variables (recomendado en Postman)
- `BASE_URL`:
  - local: `http://localhost:8080`
  - render: `https://huerteandoapp-1.onrender.com`
- `ID_USUARIO` (se obtiene en login)
- `ID_OBS` (de listado/creación)
- `ID_COMENT` (de listado/creación)
- `ID_IMAGEN` (de listado/creación)
- `ID_TIPO` (en H2 seed normalmente: Planta=1, Rincón=2, Incidencia=3)

---

# 6) Casos de prueba (CP)

> Importante: Los endpoints de comentarios/megustas/imagenes van anidados bajo `/api/observaciones/{idObservacion}` según los controllers:
> - Comentarios: `/api/observaciones/{idObservacion}/comentarios`
> - Me gustas: `/api/observaciones/{idObservacion}/megustas`
> - Imágenes: `/api/observaciones/{idObservacion}/imagenes`

## CP-01 — Registro OK (201)
- Endpoint: `POST {{BASE_URL}}/api/auth/register`
- Body (JSON):
  ```json
  {
    "nick": "test_user_01",
    "nombre": "Test",
    "apellidos": "User",
    "email": "test_user_01@demo.local",
    "avatarUrl": "",
    "password": "1234"
  }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/auth/register" -H "Content-Type: application/json" --data '{"nick":"test_user_01","nombre":"Test","apellidos":"User","email":"test_user_01@demo.local","avatarUrl":"","password":"1234"}'
  ```
- Esperado: `201 Created` + JSON de usuario (sin password).

## CP-02 — Registro nick duplicado (409)
- Precondición: en H2 existe `sergio` (seed).
- Endpoint: `POST {{BASE_URL}}/api/auth/register`
- Body (JSON):
  ```json
  { "nick": "sergio", "nombre": "X", "password": "1234" }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/auth/register" -H "Content-Type: application/json" --data '{"nick":"sergio","nombre":"X","password":"1234"}'
  ```
- Esperado: `409 Conflict`.

## CP-03 — Login OK (200)
- Endpoint: `POST {{BASE_URL}}/api/auth/login`
- Body (JSON):
  ```json
  { "nick": "sergio", "password": "1234" }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/auth/login" -H "Content-Type: application/json" --data '{"nick":"sergio","password":"1234"}'
  ```
- Esperado: `200 OK` + JSON con `id`, `nick`, `nombre`, `rol`, `avatarUrl`.
- Postman: guarda `id` como `ID_USUARIO`.

## CP-04 — Login password incorrecta (401)
- Endpoint: `POST {{BASE_URL}}/api/auth/login`
- Body:
  ```json
  { "nick": "sergio", "password": "xxxx" }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/auth/login" -H "Content-Type: application/json" --data '{"nick":"sergio","password":"xxxx"}'
  ```
- Esperado: `401 Unauthorized`.

## CP-05 — Perfil usuario (200)
- Endpoint: `GET {{BASE_URL}}/api/usuarios/{{ID_USUARIO}}`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/usuarios/{{ID_USUARIO}}"
  ```
- Esperado: `200 OK` + JSON usuario.

## CP-06 — Listar observaciones (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones"
  ```
- Esperado: `200 OK` + array JSON.
- Postman: elige una observación existente y guarda `id` como `ID_OBS`.

## CP-07 — Filtrar observaciones por tipo (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones?tipo={{ID_TIPO}}`
- Ejemplo (Planta=1):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones?tipo=1"
  ```
- Esperado: `200 OK` + array filtrado.

## CP-08 — Obtener observación por id (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones/{{ID_OBS}}`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones/{{ID_OBS}}"
  ```
- Esperado: `200 OK` + JSON observación.

## CP-09 — Crear observación (201)
- Endpoint: `POST {{BASE_URL}}/api/observaciones`
- Body (JSON) — mínimo viable (incluye campos obligatorios usados por el servicio):
  ```json
  {
    "usuario": { "id": 1 },
    "tipoObservacion": { "id": 1 },
    "titulo": "Observación de prueba",
    "descripcion": "Creada desde pruebas manuales",
    "fechaObservacion": "2026-05-11T10:00:00",
    "estadoObservacion": "ABIERTA",
    "latitud": 37.9901,
    "longitud": -1.1200
  }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/observaciones" -H "Content-Type: application/json" --data '{"usuario":{"id":1},"tipoObservacion":{"id":1},"titulo":"Observación de prueba","descripcion":"Creada desde pruebas manuales","fechaObservacion":"2026-05-11T10:00:00","estadoObservacion":"ABIERTA","latitud":37.9901,"longitud":-1.1200}'
  ```
- Esperado: `201 Created` + JSON con `id`.
- Postman: guarda el `id` devuelto como `ID_OBS` (para CP-10…CP-20).

## CP-10 — Actualizar observación (200)
- Endpoint: `PUT {{BASE_URL}}/api/observaciones/{{ID_OBS}}`
- Body (JSON):
  ```json
  {
    "usuario": { "id": 1 },
    "tipoObservacion": { "id": 1 },
    "titulo": "Observación de prueba (editada)",
    "descripcion": "Actualizada desde pruebas manuales",
    "fechaObservacion": "2026-05-11T10:00:00",
    "estadoObservacion": "ABIERTA",
    "latitud": 37.9901,
    "longitud": -1.1200
  }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X PUT "{{BASE_URL}}/api/observaciones/{{ID_OBS}}" -H "Content-Type: application/json" --data '{"usuario":{"id":1},"tipoObservacion":{"id":1},"titulo":"Observación de prueba (editada)","descripcion":"Actualizada desde pruebas manuales","fechaObservacion":"2026-05-11T10:00:00","estadoObservacion":"ABIERTA","latitud":37.9901,"longitud":-1.1200}'
  ```
- Esperado: `200 OK` + JSON actualizado.

## CP-11 — Eliminar observación (204)
- Endpoint: `DELETE {{BASE_URL}}/api/observaciones/{{ID_OBS}}`
- curl (PowerShell):
  ```powershell
  curl.exe -i -X DELETE "{{BASE_URL}}/api/observaciones/{{ID_OBS}}"
  ```
- Esperado: `204 No Content`.

> Nota: si vas a probar comentarios/megustas/imagenes sobre una observación, NO borres la observación antes de CP-12…CP-20.

## CP-12 — Listar comentarios (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones/{{ID_OBS}}/comentarios`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/comentarios"
  ```
- Esperado: `200 OK` + array (puede estar vacío).

## CP-13 — Crear comentario (201)
- Endpoint: `POST {{BASE_URL}}/api/observaciones/{{ID_OBS}}/comentarios`
- Body (JSON):
  ```json
  {
    "usuario": { "id": 2 },
    "contenido": "Comentario de prueba desde Postman/curl"
  }
  ```
- curl (PowerShell):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/comentarios" -H "Content-Type: application/json" --data '{"usuario":{"id":2},"contenido":"Comentario de prueba desde Postman/curl"}'
  ```
- Esperado: `201 Created` + JSON comentario con `id`.
- Guarda `id` como `ID_COMENT`.

## CP-14 — Borrar comentario (204)
- Endpoint: `DELETE {{BASE_URL}}/api/observaciones/{{ID_OBS}}/comentarios/{{ID_COMENT}}`
- curl (PowerShell):
  ```powershell
  curl.exe -i -X DELETE "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/comentarios/{{ID_COMENT}}"
  ```
- Esperado: `204 No Content`.

## CP-15 — Contar “me gustas” (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas/count`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas/count"
  ```
- Esperado: `200 OK` + JSON tipo `{ "megustas": <numero> }`.

## CP-16 — Dar like (201) y quitar like (204)
- Dar like:
  - Endpoint: `POST {{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas?idUsuario={{ID_USUARIO}}`
  - curl (PowerShell):
    ```powershell
    curl.exe -i -X POST "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas?idUsuario={{ID_USUARIO}}"
    ```
  - Esperado: `201 Created`
- Quitar like:
  - Endpoint: `DELETE {{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas?idUsuario={{ID_USUARIO}}`
  - curl (PowerShell):
    ```powershell
    curl.exe -i -X DELETE "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas?idUsuario={{ID_USUARIO}}"
    ```
  - Esperado: `204 No Content`

## CP-17 — Existe like (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas/existe?idUsuario={{ID_USUARIO}}`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/megustas/existe?idUsuario={{ID_USUARIO}}"
  ```
- Esperado: `200 OK` + JSON `{ "yaMeGusta": true/false }`.

## CP-18 — Listar imágenes de observación (200)
- Endpoint: `GET {{BASE_URL}}/api/observaciones/{{ID_OBS}}/imagenes`
- curl (PowerShell):
  ```powershell
  curl.exe -i "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/imagenes"
  ```
- Esperado: `200 OK` + array de imágenes.

## CP-19 — Subir imagen (201) (multipart/form-data)
- Endpoint: `POST {{BASE_URL}}/api/observaciones/{{ID_OBS}}/imagenes`
- Form-data:
  - key `file` (tipo File) → selecciona un .jpg/.png
  - key `titulo` (opcional) → texto
- curl (PowerShell, ejemplo):
  ```powershell
  curl.exe -i -X POST "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/imagenes" -F "file=@C:\ruta\a\imagen.jpg" -F "titulo=Prueba imagen"
  ```
- Esperado: `201 Created` + JSON imagen con `id` y `urlArchivo`.
- Guarda `id` como `ID_IMAGEN`.

> Nota: este caso depende de que el backend pueda subir a Supabase (credenciales y red).
> Si estás en local y no tienes `SUPABASE_URL/SUPABASE_KEY/SUPABASE_BUCKET` válidos, este caso puede fallar aunque el resto funcione.

## CP-20 — Borrar imagen (204)
- Endpoint: `DELETE {{BASE_URL}}/api/observaciones/{{ID_OBS}}/imagenes/{{ID_IMAGEN}}`
- curl (PowerShell):
  ```powershell
  curl.exe -i -X DELETE "{{BASE_URL}}/api/observaciones/{{ID_OBS}}/imagenes/{{ID_IMAGEN}}"
  ```
- Esperado: `204 No Content`.

---

## 7) Checklist de entrega (memoria)
- Tabla de CP completada (OK/KO)
- Capturas por cada CP (status + body)
- Indicar entorno usado (Local H2 o Render)
- Observaciones: incidencias encontradas (p.ej. casos donde un error devuelve 409 aunque el problema sea “no existe”)
