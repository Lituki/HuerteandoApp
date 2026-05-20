# Evidencias de pruebas — Supabase Auth + Spring Boot
## Proyecto Huerteando — 13 de mayo de 2026

---

## Resumen del sistema probado

- **Backend**: Spring Boot 4.0.2 + Java 21
- **Autenticación**: Supabase Auth (tokens JWT firmados con ES256 / curva P-256)
- **Perfil activo**: `postgres` (PostgreSQL en Render)
- **JWKS endpoint**: `https://moqignnvjhcxnhtzcsgx.supabase.co/auth/v1/.well-known/jwks.json`

> Nota actualizada: el endpoint usado por Android para validar sesión y obtener el perfil local es
> `POST /api/auth/login-jwt`. Si aparece `GET /api/auth/me` en pruebas antiguas, era un endpoint
> de diagnóstico y no forma parte del backend actual.

---

## Prueba 1 — Ruta pública sin token

**Objetivo**: Verificar que `GET /api/observaciones` devuelve datos sin necesidad de token.

**Comando**:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/observaciones" -SkipHttpErrorCheck | Select-Object StatusCode
```

**Resultado**:
```
StatusCode
----------
       200
```

**Conclusión**: ✅ La ruta pública funciona correctamente sin token.

---

## Prueba 2 — Ruta privada sin token (POST)

**Objetivo**: Verificar que `POST /api/observaciones` sin token devuelve 401.

**Comando**:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/observaciones" -Method POST -ContentType "application/json" -Body '{}' -SkipHttpErrorCheck | Select-Object StatusCode
```

**Resultado**:
```
StatusCode
----------
       401
```

**Conclusión**: ✅ Spring Security bloquea correctamente las peticiones sin token.

---

## Prueba 3 — Endpoint login-jwt sin token

**Objetivo**: Verificar que `POST /api/auth/login-jwt` sin token devuelve 401.

**Comando**:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login-jwt" -Method POST -SkipHttpErrorCheck | Select-Object StatusCode
```

**Resultado**:
```
StatusCode
----------
       401
```

**Conclusión**: ✅ El endpoint protegido rechaza peticiones sin token.

---

## Prueba 4 — Endpoint login-jwt con token válido de Supabase

**Objetivo**: Verificar que Spring Boot valida correctamente un JWT de Supabase (ES256) y devuelve el perfil local del usuario.

**Paso 1 — Obtener token de Supabase**:
```powershell
$apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1vcWlnbm52amhjeG5odHpjc2d4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njk0ODE3NDMsImV4cCI6MjA4NTA1Nzc0M30.o6j4MseA2MlcplFgUGCgZZGZ371y1_X-WfXogOOvOmE"
$body = '{"email":"test@test.com","password":"123456"}'
$r = Invoke-RestMethod -Uri "https://moqignnvjhcxnhtzcsgx.supabase.co/auth/v1/token?grant_type=password" -Method Post -Headers @{ "apikey" = $apiKey; "Content-Type" = "application/json" } -Body $body
$t = $r.access_token
```

**Paso 2 — Llamar a la API con el token**:
```powershell
curl -s -w "`nHTTP: %{http_code}" -X POST "http://localhost:8080/api/auth/login-jwt" -H "Authorization: Bearer $t"
```

**Resultado**:
```json
{
  "id": 1,
  "nick": "test",
  "nombre": "Test",
  "apellidos": "",
  "email": "test@test.com",
  "fechaRegistro": "2026-05-13T18:00:00",
  "rol": "USER",
  "avatarUrl": ""
}
HTTP: 200
```

**Token obtenido** (recortado):
```
eyJhbGciOiJFUzI1NiIsImtpZCI6ImMwZjQ4YmIwLWEzZDYtNGEzMi04NzUwLTZlN2U1ODJkNzBlOCIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL21vcWlnbm52amhjeG5odHpjc2d4LnN1cGFiYXNlLmNvL2F1dGgvdjEiLCJzdWIiOiI3NzkzZjE4Mi1iZTQyLTQyNDUtYTliNC0wYzBjYWU4NGZmZTQiLCJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzc4NzA1NDcxLCJpYXQiOjE3Nzg3MDE4NzEsImVtYWlsIjoidGVzdEB0ZXN0LmNvbSIsInBob25lIjoiIiwiYXBwX21ldGFkYXRhIjp7InByb3ZpZGVyIjoiZW1haWwiLCJwcm92aWRlcnMiOlsiZW1haWwiXX0sInVzZXJfbWV0YWRhdGEiOnsiZW1haWxfdmVyaWZpZWQiOnRydWV9LCJyb2xlIjoiYXV0aGVudGljYXRlZCIsImFhbCI6ImFhbDEiLCJhbXIiOlt7Im1ldGhvZCI6InBhc3N3b3JkIiwidGltZXN0YW1wIjoxNzc4NzAxODcxfV0sInNlc3Npb25faWQiOiIxNWMyYzVlNy0yZTQxLTQ0MzItYjU0NC0xN2RkNjIyYWRiMTgiLCJpc19hbm9ueW1vdXMiOmZhbHNlfQ.c9wX2PWIjZcmq9G-W1dLUSs0UUiiMtvzCvDREME-6yc57jhYIPI2uXbg0gPC2mmmejGXKZUZA6ZalYFZJb5-7Q
```

**Conclusión**: ✅ Spring Boot valida el JWT de Supabase (ES256) correctamente y localiza el perfil por email para test@test.com.

---

## Tabla resumen

| # | Petición | Token | Código esperado | Código obtenido | Estado |
|---|----------|-------|-----------------|-----------------|--------|
| 1 | GET /api/observaciones | Sin token | 200 | 200 | ✅ |
| 2 | POST /api/observaciones | Sin token | 401 | 401 | ✅ |
| 3 | POST /api/auth/login-jwt | Sin token | 401 | 401 | ✅ |
| 4 | POST /api/auth/login-jwt | Token válido Supabase (test@test.com) | 200 | 200 | ✅ |

---

## Configuración clave aplicada

### application-postgres.properties
```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://moqignnvjhcxnhtzcsgx.supabase.co/auth/v1/.well-known/jwks.json

# CRÍTICO: Supabase usa ES256 (curva elíptica P-256), no RS256.
# Sin esta línea Spring Boot rechaza el token aunque la clave sea correcta.
spring.security.oauth2.resourceserver.jwt.jws-algorithms=ES256
```

### SecurityConfig.java — reglas de acceso
```
GET /api/observaciones/**    → público
GET /api/especies/**         → público
GET /api/tipos-observacion/** → público
POST/PUT/DELETE en cualquier ruta → requiere token válido
POST /api/auth/login-jwt     → requiere token válido
```

---

## Problema encontrado durante las pruebas

**Error**: una ruta protegida con token válido devolvía 401.

**Causa**: Spring Boot 4 por defecto solo acepta algoritmos RSA (RS256).
Supabase firma sus JWT con ES256 (curva elíptica P-256).
Aunque el `kid` del token coincidía con el JWKS, Spring rechazaba el token.

**Solución**: Añadir `jws-algorithms=ES256` en `application-postgres.properties`.

**Evidencia del JWKS de Supabase** (confirmación del algoritmo):
```json
{
  "keys": [{
    "alg": "ES256",
    "crv": "P-256",
    "kty": "EC",
    "use": "sig",
    "kid": "c0f48bb0-a3d6-4a32-8750-6e7e582d70e8",
    "x": "Ke8du5RGu8-SmhR4k9T7a-zFS_mMi8YnvLaMLcXCT8Y",
    "y": "LxpLmKiOQNEfl8LCDdgqNBP_MJmD2JgxiC2Sf7Z_22M"
  }]
}
```
