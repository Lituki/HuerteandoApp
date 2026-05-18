# Evidencias de pruebas

Este documento recoge evidencias sencillas de las pruebas realizadas en el
proyecto Huerteando.

Fecha de ejecucion: 15/05/2026

---

## 1. Pruebas automaticas de backend

### Comando ejecutado

```text
mvn test
```

### Resultado obtenido

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Archivos de test ejecutados

```text
src/test/java/com/huerteando/huerteandoapp/HuerteandoAppApplicationTests.java
src/test/java/com/huerteando/huerteandoapp/api/BackendHttpSmokeTest.java
```

---

## 2. Tests incluidos

### `HuerteandoAppApplicationTests`

Comprueba que el contexto de Spring Boot puede arrancar correctamente con el
perfil de pruebas `h2`.

| Prueba | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|
| `contextLoads` | El contexto de Spring arranca sin errores | Arranca correctamente | OK |

---

### `BackendHttpSmokeTest`

Comprueba endpoints reales del backend local arrancado en:

```text
http://localhost:8080
```

| Prueba | Endpoint | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|
| `listarObservacionesDevuelveOk` | `GET /api/observaciones/` | `200 OK` y lista JSON | Correcto | OK |
| `listarTiposObservacionDevuelveOk` | `GET /api/tipos-observacion` | `200 OK` y lista JSON | Correcto | OK |
| `listarEspeciesDevuelveOk` | `GET /api/especies` | `200 OK` y lista JSON | Correcto | OK |
| `disponibilidadDetectaUsuarioExistente` | `GET /api/auth/disponibilidad` | `200 OK` con campos de disponibilidad | Correcto | OK |
| `crearObservacionSinTokenDevuelveUnauthorized` | `POST /api/observaciones` | `401 Unauthorized` | Correcto | OK |

---

## 3. Que validan estas pruebas

Estas pruebas validan que:

- el backend compila correctamente;
- Spring Boot arranca con el perfil de pruebas;
- las rutas publicas principales responden;
- el endpoint de disponibilidad de registro esta accesible;
- una accion privada, como crear observacion, queda protegida si no hay token.

---

## 4. Observaciones

Los tests de `BackendHttpSmokeTest` dependen de que el backend este arrancado en:

```text
http://localhost:8080
```

Si el backend no esta levantado, esas pruebas se omiten para evitar que falle la
compilacion por un problema de entorno.

Tambien aparece un aviso de Maven indicando que `spring-boot-starter-security`
esta declarado dos veces en `pom.xml`. No impide ejecutar las pruebas, pero seria
conveniente limpiarlo mas adelante.
