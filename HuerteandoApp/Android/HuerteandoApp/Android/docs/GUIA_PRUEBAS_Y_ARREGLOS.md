# 📋 Guía de Pruebas y Solución de Problemas - Huerteando

Esta guía detalla los pasos para verificar el funcionamiento completo de la aplicación, tanto en el emulador local como en un dispositivo móvil real, junto con las soluciones a los fallos más probables.

---

## 🚀 0. Preparación de Entorno

### En Emulador (PC)
- **URL Base:** Asegúrate de que `ApiClient.java` use `http://10.0.2.2:8080/`.
- **Backend:** El servidor Spring Boot debe estar corriendo en IntelliJ/Eclipse.

### En Móvil Real
- **Conexión:** El móvil y el PC deben estar en la **misma red Wi-Fi**.
- **URL Base:** Cambia `10.0.2.2` por la **IP local de tu PC** (ej: `http://192.168.1.15:8080/`).
- **Firewall:** Desactiva temporalmente el firewall de Windows o permite el puerto 8080.

---

## 🧪 1. Plan de Pruebas Funcionales

### A. Acceso y Sesión
| Prueba | Acción | Resultado Esperado | Si falla (Arreglo) |
| :--- | :--- | :--- | :--- |
| **P1: Registro** | Registrar nuevo usuario. | Redirige al Login. | Comprobar si el `nick` ya existe en la BD o si faltan campos. |
| **P2: Login** | Entrar con el usuario creado. | Entra a la lista de observaciones. | Verificar contraseña y que el Backend esté encendido. |
| **P3: Persistencia** | Cerrar la app (matar proceso) y reabrir. | No pide login (entra directo). | Borrar caché de la app o revisar `SessionManager`. |

### B. Observaciones
| Prueba | Acción | Resultado Esperado | Si falla (Arreglo) |
| :--- | :--- | :--- | :--- |
| **P4: Carga inicial** | Abrir la app. | Se ven observaciones de todos los usuarios. | Revisar `ApiService.getObservaciones()` (sin queries). |
| **P5: Geolocalización**| Pulsar "Obtener ubicación". | Coordenadas y zona se autorrellenan. | Activar GPS del móvil y dar permisos de ubicación. |
| **P6: Creación** | Guardar nueva observación. | Mensaje "¡Guardado!" y vuelve a la lista. | Verificar que `latitud/longitud` no sean 0 o nulos. |
| **P7: Subida Imagen** | Adjuntar foto en la creación. | La foto se ve en el detalle. | Comprobar tamaño de imagen (Base64 muy grande da error). |

### C. Interacción y Filtros
| Prueba | Acción | Resultado Esperado | Si falla (Arreglo) |
| :--- | :--- | :--- | :--- |
| **P8: Me Gusta** | Pulsar corazón en Detalle. | Corazón rojo y contador +1. | Verificar `idUsuario` en `SharedPreferences`. |
| **P9: Sincronía** | Volver a la lista tras dar like. | La tarjeta de la lista tiene corazón rojo. | Comprobar `onResume()` en `ObservacionesActivity`. |
| **P10: Filtro Tipo** | Cambiar spinner a "Planta". | Solo se ven plantas. | Revisar IDs de tipos en el Backend (1=Planta, 2=Rincón, 3=Denuncia). |

---

## 🛠️ 2. Solución de Errores Comunes (Troubleshooting)

### Error: "Error de conexión" o "onFailure"
- **Causa:** El móvil/emulador no llega al servidor.
- **Solución:**
    1. Abre el navegador del móvil y pon: `http://[TU_IP]:8080/api/observaciones`. Si no carga el JSON, es el **Firewall** o la **IP** que está mal.
    2. En `AndroidManifest.xml`, verifica `android:usesCleartextTraffic="true"`.

### Error: "No aparecen las fotos"
- **Causa:** URL mal formada o problemas de red en Supabase/Render.
- **Solución:** Mira el Logcat en Android Studio filtrando por "Glide". Si la URL no empieza por `http`, revisa la concatenación de `BASE_URL` en el `ObservacionAdapter`.

### Error: "El botón de atrás no hace nada"
- **Causa:** Toolbar no configurada como ActionBar.
- **Solución:** Asegúrate de que `setSupportActionBar(toolbar)` se llame antes de `getSupportActionBar().setDisplayHomeAsUpEnabled(true)`.

### Error: "El contador de Me Gusta no sube"
- **Causa:** Desincronización entre la clave del JSON (`megustas`) y el modelo Java.
- **Solución:** Revisa que en `Observacion.java` el campo se llame exactamente `numMeGustas` o usa `@SerializedName("megustas")`.

---

## 📱 3. Notas para la prueba en Móvil Real
1. Ve a **Ajustes > Información del teléfono**.
2. Pulsa 7 veces sobre **Número de compilación** para activar "Opciones de desarrollador".
3. Activa **Depuración USB**.
4. Conecta el cable al PC y selecciona tu móvil en la lista de dispositivos de Android Studio (arriba, junto al botón de Play).
5. Dale a **Run** y acepta el permiso de huella digital en la pantalla del móvil.
