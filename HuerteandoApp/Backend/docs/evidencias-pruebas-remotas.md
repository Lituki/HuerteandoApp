# Evidencias de pruebas remotas

Este documento recoge la prueba realizada contra la API desplegada.

Fecha de ejecucion: 15/05/2026

API remota probada:

```text
https://huerteandoapp-1.onrender.com
```

---

## 1. Test automatico creado

Se ha añadido un test automatico para la API remota en:

```text
src/test/java/com/huerteando/huerteandoapp/api/BackendRemotoSmokeTest.java
```

Este test esta desactivado por defecto para que `mvn test` normal no dependa de
internet ni de que Render este despierto.

Para ejecutarlo manualmente:

```text
mvn -Dtest=BackendRemotoSmokeTest -Dremote.tests=true test
```

Si la URL de Render cambia, se puede indicar otra:

```text
mvn -Dtest=BackendRemotoSmokeTest -Dremote.tests=true -Dremote.api.base-url=https://TU-API.onrender.com test
```

---

## 2. Resultado obtenido

Comando ejecutado:

```text
mvn -Dtest=BackendRemotoSmokeTest test
```

Resultado:

```text
Tests run: 5, Failures: 2, Errors: 0, Skipped: 0
BUILD FAILURE
```

El fallo no significa que la API este completamente caida. Significa que hay
diferencias entre el backend local actual y la version desplegada.

---

## 3. Tabla de resultados

| Prueba | Endpoint | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|
| Listar observaciones | `GET /api/observaciones/` | `200 OK` | `200 OK` | OK |
| Listar tipos | `GET /api/tipos-observacion` | `200 OK` | `200 OK` | OK |
| Listar especies | `GET /api/especies` | `200 OK` | `200 OK` | OK |
| Disponibilidad de usuario | `GET /api/auth/disponibilidad` | `200 OK` | `404 Not Found` | KO |
| Crear observacion sin token | `POST /api/observaciones` | `401 Unauthorized` | `400 Bad Request` | KO |

---

## 4. Interpretacion

La API remota responde correctamente en endpoints publicos principales:

- observaciones;
- tipos de observacion;
- especies.

Pero se han detectado dos diferencias importantes:

1. `/api/auth/disponibilidad` devuelve `404`.
   Esto indica que la API desplegada probablemente no tiene todavia la ultima
   version del backend donde se implemento el flujo recomendado de registro.

2. `POST /api/observaciones` sin token devuelve `400` en vez de `401`.
   Esto indica que en la version remota la peticion esta llegando al controller
   y fallando por datos incompletos, en vez de ser bloqueada primero por
   seguridad.

---

## 5. Conclusion

La prueba remota sirve como evidencia de que el despliegue responde, pero tambien
demuestra que la version desplegada no esta alineada con la version local actual.

Accion recomendada:

1. Desplegar en Render la ultima version del backend.
2. Volver a ejecutar:

```text
mvn -Dtest=BackendRemotoSmokeTest -Dremote.tests=true test
```

3. Confirmar que todos los tests remotos terminan con:

```text
BUILD SUCCESS
```
