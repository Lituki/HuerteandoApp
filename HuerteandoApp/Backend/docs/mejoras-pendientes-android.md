# Mejoras pendientes Android

Este documento recoge mejoras detectadas durante la revision de la app Android.
No son bloqueos graves, pero conviene apuntarlas para dejar la app mas clara,
estable y facil de defender en clase.

---

## 1. Ocultar la fila de especie cuando no hay especie asociada

### Que se ve ahora

En la pantalla de detalle de una observacion puede aparecer una fila vacia debajo
de la ubicacion. Se ve el icono de especie, pero no aparece texto al lado.

Ejemplo visual:

```text
📍 Los Almagros
▣

Descripcion
venenoso
```

### Por que pasa

En `DetalleObservacionActivity`, el texto de especie solo se rellena cuando la
observacion trae una especie asociada:

```java
if (o.getEspecie() != null) {
    tvDetalleEspecie.setText("🌿 " + o.getEspecie().getNombreComun());
    tvDetalleEspecie.setVisibility(View.VISIBLE);
}
```

Si `o.getEspecie()` viene como `null`, no se escribe ningun texto. El problema es
que el `LinearLayout` que contiene el icono sigue visible en el XML, por eso queda
una fila vacia.

### Comportamiento esperado

Si la observacion tiene especie:

```text
🌿 Cactus
```

Si no tiene especie:

```text
No mostrar esa fila.
```

### Solucion recomendada

La solucion mas limpia es ocultar el layout completo de especie cuando no haya
datos. Para eso conviene declarar una variable para el `LinearLayout` padre
(`layoutEspecie`) y mostrarlo u ocultarlo segun el caso.

Ejemplo:

```java
private LinearLayout layoutEspecie;
```

En `enlazarVistas()`:

```java
layoutEspecie = findViewById(R.id.layoutEspecie);
```

En `mostrarObservacion()`:

```java
if (o.getEspecie() != null) {
    tvDetalleEspecie.setText("🌿 " + o.getEspecie().getNombreComun());
    layoutEspecie.setVisibility(View.VISIBLE);
} else {
    layoutEspecie.setVisibility(View.GONE);
}
```

### Alternativa sencilla

Si se quiere mostrar algo aunque no haya especie oficial, se podria usar
`identificacionPropuesta` o `nombreTradicional`:

```java
if (o.getEspecie() != null) {
    tvDetalleEspecie.setText("🌿 " + o.getEspecie().getNombreComun());
    layoutEspecie.setVisibility(View.VISIBLE);
} else if (o.getIdentificacionPropuesta() != null && !o.getIdentificacionPropuesta().isBlank()) {
    tvDetalleEspecie.setText("🌿 " + o.getIdentificacionPropuesta());
    layoutEspecie.setVisibility(View.VISIBLE);
} else if (o.getNombreTradicional() != null && !o.getNombreTradicional().isBlank()) {
    tvDetalleEspecie.setText("🌿 " + o.getNombreTradicional());
    layoutEspecie.setVisibility(View.VISIBLE);
} else {
    layoutEspecie.setVisibility(View.GONE);
}
```

Para nivel DAM, la primera solucion es suficiente y mas facil de explicar:
si no hay dato, no se muestra la fila.

---

## 2. No mostrar "Editar perfil" hasta que exista edicion real

### Estado actual

La app tiene una pantalla de perfil (`PerfilActivity`), pero no una pantalla de
edicion completa del perfil.

Ahora mismo permite:

- ver nick, rol, nombre, apellidos, email y fecha de registro;
- cambiar el avatar;
- cerrar sesion.

Pero los datos personales se muestran con `TextView`, no con campos editables.
Tampoco existe un boton de "Guardar cambios" ni un endpoint Android claro para
actualizar nombre, apellidos, email o nick.

### Decision recomendada

En el menu superior de observaciones, cuando el usuario tenga sesion, mostrar:

```text
Mi perfil
Catalogo de especies
Cerrar sesion
```

No mostrar todavia:

```text
Editar perfil
```

### Por que

Si se muestra "Editar perfil", el usuario espera poder cambiar sus datos. Como
esa funcionalidad aun no existe, seria confuso y pareceria que la app esta
incompleta o rota.

Para nivel DAM es mejor ser claro:

- `Mi perfil` lleva a una pantalla de consulta.
- `Cerrar sesion` cierra la sesion.
- `Editar perfil` se deja como mejora futura.

### Como se podria implementar mas adelante

Una version sencilla y defendible seria:

1. Mantener `PerfilActivity` como pantalla de consulta.
2. Anadir un boton "Editar perfil" dentro de `PerfilActivity`.
3. Crear una pantalla nueva, por ejemplo `EditarPerfilActivity`.
4. Usar campos editables para nombre, apellidos y avatar.
5. Crear un endpoint en backend, por ejemplo:

```text
PUT /api/usuarios/{id}
```

6. Al guardar, actualizar los datos en PostgreSQL y refrescar `SessionManager`.

### Alternativa mas simple

Si no se quiere crear una pantalla nueva, se podria convertir `PerfilActivity`
en modo lectura / modo edicion:

- por defecto muestra `TextView`;
- al pulsar "Editar", cambia a `TextInputEditText`;
- al pulsar "Guardar", llama al backend.

Para el proyecto actual, la opcion mas limpia es dejar solo `Mi perfil` y apuntar
la edicion como mejora futura.

---

## 3. Simplificar el menu de filtros de observaciones

### Que se ve ahora

En la pantalla principal hay cuatro filtros en dos filas:

```text
[Todos los tipos]        [Mas recientes]
[Todas las observacion..] [Todos los estados]
```

Visualmente ocupa bastante altura, algunos textos se cortan y el filtro de estado
no parece imprescindible para la pantalla principal.

### Decision recomendada

Quitar el filtro de estado y dejar solo tres filtros:

```text
[Tipo: Todos] [Recientes]
[Todas]
```

O, si se ajusta bien en pantalla:

```text
[Tipo: Todos] [Recientes] [Todas]
```

Para nivel DAM, la version de dos filas es mas facil porque mantiene `Spinner`
y no obliga a cambiar toda la logica por chips o menus personalizados.

### Que mejora

- La cabecera ocupa menos.
- Se evita texto cortado.
- La lista empieza antes.
- La pantalla queda mas limpia.
- Se mantiene la misma logica principal: tipo, orden y usuario.

### Cambio en `activity_observaciones.xml`

Ahora hay dos filas de filtros. La segunda fila tiene `spinnerUsuario` y
`spinnerEstado`. Se puede sustituir la segunda fila completa por una fila que
solo tenga `spinnerUsuario`.

#### Codigo actual que se puede sustituir

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:orientation="horizontal">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        app:cardCornerRadius="8dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="#33FFFFFF">

        <Spinner
            android:id="@+id/spinnerUsuario"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:paddingHorizontal="8dp"
            android:backgroundTint="@color/white" />
    </com.google.android.material.card.MaterialCardView>

    <com.google.android.material.card.MaterialCardView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="8dp"
        app:cardCornerRadius="8dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="#33FFFFFF">

        <Spinner
            android:id="@+id/spinnerEstado"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:paddingHorizontal="8dp"
            android:backgroundTint="@color/white" />
    </com.google.android.material.card.MaterialCardView>
</LinearLayout>
```

#### Sustituir por

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:orientation="horizontal">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        app:cardCornerRadius="8dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="#33FFFFFF">

        <Spinner
            android:id="@+id/spinnerUsuario"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:paddingHorizontal="8dp"
            android:backgroundTint="@color/white" />
    </com.google.android.material.card.MaterialCardView>

    <!-- Espacio libre para que el filtro no parezca aplastado.
         Si se prefiere, se puede quitar este Space y dejar el spinner a ancho completo. -->
    <Space
        android:layout_width="0dp"
        android:layout_height="1dp"
        android:layout_weight="1"
        android:layout_marginStart="8dp" />
</LinearLayout>
```

### Cambio en `ObservacionesActivity.java`

Tambien hay que quitar `spinnerEstado` y la variable `estadoSeleccionado`, porque
ya no se usa ese filtro.

#### Codigo actual

```java
private Spinner spinnerTipo, spinnerOrden, spinnerUsuario, spinnerEstado;
```

Sustituir por:

```java
private Spinner spinnerTipo, spinnerOrden, spinnerUsuario;
```

Quitar esta variable:

```java
private String estadoSeleccionado = null;
```

En `initViews()`, quitar:

```java
spinnerEstado = findViewById(R.id.spinnerEstado);
```

En `setupSpinners()`, quitar todo este bloque:

```java
ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(this,
        R.array.array_estados, R.layout.spinner_item);
adapterEstado.setDropDownViewResource(R.layout.spinner_dropdown_item);
spinnerEstado.setAdapter(adapterEstado);
spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 1: estadoSeleccionado = "ABIERTA"; break;
            case 2: estadoSeleccionado = "CERRADA"; break;
            default: estadoSeleccionado = null;
        }
        cargarObservaciones();
    }
    @Override public void onNothingSelected(AdapterView<?> parent) {}
});
```

En `cargarObservaciones()`, quitar esta parte:

```java
} else if (estadoSeleccionado != null) {
    Log.d(TAG, "Filtrando por estado: " + estadoSeleccionado);
    call = api.getObservacionesPorEstado(estadoSeleccionado);
```

Y dejar la decision de carga asi:

```java
if (idTipoSeleccionado != null) {
    Log.d(TAG, "Filtrando por tipo ID: " + idTipoSeleccionado);
    call = api.getObservacionesPorTipo(idTipoSeleccionado);
} else if (idUsuarioSeleccionado != null) {
    Log.d(TAG, "Filtrando por usuario ID: " + idUsuarioSeleccionado);
    call = api.getObservacionesPorUsuario(idUsuarioSeleccionado);
} else {
    Log.d(TAG, "Cargando todas las observaciones");
    call = api.getObservaciones();
}
```

### Cambio en `strings.xml`

Se pueden acortar los textos para evitar cortes:

```xml
<string name="tipo_todos">Tipo: Todos</string>
<string name="orden_recientes">Recientes</string>
<string name="filtro_todos">Todas</string>
<string name="filtro_mis_observaciones">Mias</string>
```

El array de estados puede quedarse sin uso o eliminarse si se quiere limpiar:

```xml
<string-array name="array_estados">
    <item>@string/estado_todos</item>
    <item>@string/estado_abierta</item>
    <item>@string/estado_cerrada</item>
</string-array>
```

Para una entrega de clase, se puede dejar el array aunque no se use. No molesta,
pero quitarlo deja el proyecto mas limpio.

---

## 4. Mostrar bien los likes en la lista de observaciones

### Que se ve ahora

En las tarjetas de la pantalla principal, el contador de likes aparece siempre
como `0`, aunque en el detalle de la observacion si pueda aparecer el total real.

Ejemplo:

```text
♡ 0
```

### Por que pasa

El `ObservacionAdapter` pinta el contador con este valor:

```java
tvMeGusta.setText(String.valueOf(obs.getNumMeGustas()));
```

Y `ObservacionesActivity` tambien ordena por likes usando:

```java
case "me gusta": return Integer.compare(o2.getNumMeGustas(), o1.getNumMeGustas());
```

El problema es que la lista de observaciones que llega desde el backend no esta
rellenando `numMeGustas`. Como `int` vale `0` por defecto, Android pinta `0`
en todas las tarjetas.

En cambio, la pantalla de detalle si tiene una llamada especifica:

```java
GET /api/observaciones/{id}/megustas/count
```

Por eso el contador puede estar bien en detalle pero mal en listado.

### Comportamiento esperado

Al listar observaciones, cada tarjeta debe mostrar su total real de likes:

```text
♡ 3
♡ 0
♡ 12
```

Y el filtro/orden "Mas gustadas" debe ordenar usando esos numeros reales, no
ceros.

### Solucion recomendada para nivel DAM

La opcion mas sencilla, sin tocar demasiado el backend, es cargar los contadores
despues de recibir la lista. Es decir:

1. `ObservacionesActivity` pide la lista normal de observaciones.
2. Cuando llega la lista, se muestra.
3. Para cada observacion, Android llama a `/megustas/count`.
4. Cuando llega cada contador, se actualiza `numMeGustas` y se refresca la lista.

No es la solucion mas eficiente del mundo si hay muchas observaciones, pero para
un proyecto DAM es clara, facil de explicar y aprovecha el endpoint que ya existe.

### Cambios propuestos en `ObservacionesActivity.java`

Primero, despues de recibir las observaciones, llamar a un metodo nuevo:

#### Codigo actual

```java
listaOriginal.clear();
listaOriginal.addAll(response.body());
procesarYMostrarLista();
```

#### Sustituir por

```java
listaOriginal.clear();
listaOriginal.addAll(response.body());
procesarYMostrarLista();
cargarContadoresMeGusta();
```

Despues, anadir este metodo en la misma clase:

```java
private void cargarContadoresMeGusta() {
    ApiService api = ApiClient.getClient().create(ApiService.class);

    for (Observacion observacion : listaOriginal) {
        if (observacion.getId() == null) {
            continue;
        }

        api.getMeGustasCount(observacion.getId()).enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long total = response.body().get("megustas");
                    observacion.setNumMeGustas(total != null ? total.intValue() : 0);

                    // Reprocesamos la lista para que tambien funcione el orden por "me gusta".
                    procesarYMostrarLista();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Long>> call, Throwable t) {
                Log.e(TAG, "No se pudo cargar el contador de likes", t);
            }
        });
    }
}
```

Tambien harian falta estos imports si no estan ya:

```java
import java.util.Map;
```

`Call`, `Callback` y `Response` ya suelen estar importados en esta Activity.

### Mejora para comentarios

Si los comentarios tambien salen siempre a `0`, pasara exactamente lo mismo:
`Observacion` tiene `numComentarios`, pero la lista no lo esta rellenando.

Se podria hacer algo parecido llamando a:

```java
GET /api/observaciones/{id}/comentarios
```

Y usando el tamano de la lista:

```java
observacion.setNumComentarios(response.body().size());
```

Pero antes conviene arreglar likes, probarlo, y luego repetir el mismo patron
con comentarios si hace falta.

### Alternativa mas limpia en backend

La solucion mas completa seria que el backend devolviera directamente un DTO de
observacion con los contadores ya calculados:

```java
public class ObservacionListadoResponse {
    private Long id;
    private String titulo;
    private int numMeGustas;
    private int numComentarios;
    // resto de campos necesarios para la tarjeta
}
```

Ventaja:

- Android recibe todo listo.
- Solo hay una llamada para cargar la lista.
- El orden por likes funciona desde el principio.

Inconveniente:

- Hay que crear DTO, mapear entidades y tocar mas backend.
- Para el nivel actual puede ser mas trabajo del necesario.

Por eso, como mejora inmediata, se recomienda la solucion Android con
`cargarContadoresMeGusta()`. Es menos perfecta, pero clara y suficiente para
arreglar el fallo visible.

---

## 5. Anadir pantalla de carga con el logo de Huerteando

### Idea

Crear una pantalla inicial sencilla que aparezca al abrir la app durante uno o
dos segundos. Esta pantalla mostraria:

```text
[Logo de Huerteando]
Huerteando
```

Despues, la app pasaria automaticamente a la pantalla principal de observaciones.

### Por que mejora la app

- La entrada a la app queda mas cuidada.
- Se refuerza la identidad visual de Huerteando.
- Da tiempo a comprobar si hay sesion guardada antes de mostrar la siguiente
  pantalla.
- Evita que la app arranque directamente en una pantalla con datos cargando.

### Comportamiento recomendado

Para el estado actual de la app, lo mas claro seria:

```text
SplashActivity
    ↓
ObservacionesActivity
```

No hace falta obligar al usuario a iniciar sesion al abrir, porque la app permite
ver observaciones sin estar logueado.

Mas adelante, si se quiere, desde la pantalla de carga se podria decidir:

```text
Si hay sesion guardada     → ObservacionesActivity
Si no hay sesion guardada  → ObservacionesActivity igualmente, pero sin acciones privadas
```

Es decir, en este proyecto la pantalla de carga no deberia bloquear el acceso
publico.

### Solucion sencilla para nivel DAM

Crear una Activity nueva llamada `SplashActivity`.

La Activity solo hace tres cosas:

1. Muestra un layout con el logo.
2. Espera un tiempo corto.
3. Abre `ObservacionesActivity` y se cierra.

### Archivo nuevo: `SplashActivity.java`

```java
package com.huerteando.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.huerteando.app.R;

public class SplashActivity extends AppCompatActivity {

    private static final long TIEMPO_SPLASH = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Esperamos un poco para mostrar el logo y despues abrimos la pantalla principal.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, ObservacionesActivity.class);
            startActivity(intent);
            finish();
        }, TIEMPO_SPLASH);
    }
}
```

### Archivo nuevo: `activity_splash.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:background="@color/verde_principal"
    android:padding="32dp">

    <ImageView
        android:id="@+id/imgLogoSplash"
        android:layout_width="140dp"
        android:layout_height="140dp"
        android:src="@drawable/ic_leaf"
        android:contentDescription="@string/app_name" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="@string/app_name"
        android:textColor="@android:color/white"
        android:textSize="32sp"
        android:textStyle="bold" />

</LinearLayout>
```

Si el proyecto ya tiene un logo propio en `drawable`, conviene usar ese logo en
vez de `@drawable/ic_leaf`.

### Cambio en `AndroidManifest.xml`

La pantalla inicial debe pasar a ser `SplashActivity`. La `ObservacionesActivity`
deja de tener el intent launcher.

Ejemplo:

```xml
<activity
    android:name=".ui.SplashActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity
    android:name=".ui.ObservacionesActivity"
    android:exported="false" />
```

### Detalle importante

La pantalla de carga no debe durar demasiado. Entre `1000` y `1500` ms es
suficiente. Si dura mas, puede parecer que la app va lenta.

Para una entrega de clase, esta mejora es buena porque es visual, facil de
explicar y no cambia la logica principal de la aplicacion.

---

## 6. Mostrar detalles botanicos solo si el tipo es Planta

### Que se ve ahora

En la pantalla de crear observacion aparecen campos de detalles botanicos aunque
la observacion no sea de tipo planta.

Por ejemplo, si el usuario quiere crear una incidencia o un rincon, puede ver
campos relacionados con plantas que no tienen sentido para ese caso.

### Por que conviene cambiarlo

- La pantalla queda mas limpia.
- El usuario solo ve los campos que necesita.
- Se evita que parezca obligatorio rellenar datos botanicos en observaciones que
  no son plantas.
- Es una mejora facil de explicar: la interfaz se adapta al tipo seleccionado.

### Comportamiento esperado

Si el usuario elige:

```text
Tipo: Planta
```

Se muestran los campos botanicos:

```text
Especie
Nombre tradicional
Identificacion propuesta
Detalles botanicos
```

Si el usuario elige otro tipo:

```text
Tipo: Rincon
Tipo: Incidencia
Tipo: Denuncia
```

La seccion botanica se oculta.

### Solucion recomendada para nivel DAM

Agrupar los campos botanicos en un `LinearLayout` padre y cambiar su visibilidad
cuando cambie el tipo seleccionado.

### Cambio en `activity_crear_observacion.xml`

La idea es envolver los campos botanicos en un contenedor con id:

```xml
<LinearLayout
    android:id="@+id/layoutDetallesBotanicos"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <!-- Aqui irian los campos de especie, nombre tradicional,
         identificacion propuesta y otros datos botanicos. -->

</LinearLayout>
```

No hace falta cambiar cada campo por separado. Es mejor ocultar el bloque entero.

### Cambio en `CrearObservacionActivity.java`

Declarar la variable:

```java
private LinearLayout layoutDetallesBotanicos;
```

En `enlazarVistas()` o donde se hacen los `findViewById`:

```java
layoutDetallesBotanicos = findViewById(R.id.layoutDetallesBotanicos);
```

Crear un metodo sencillo:

```java
private void actualizarVisibilidadDetallesBotanicos() {
    boolean esPlanta = false;

    if (tipoSeleccionado != null && tipoSeleccionado.getNombre() != null) {
        esPlanta = tipoSeleccionado.getNombre().equalsIgnoreCase("Planta");
    }

    layoutDetallesBotanicos.setVisibility(esPlanta ? View.VISIBLE : View.GONE);
}
```

Y llamarlo cuando cambie el tipo en el `Spinner`:

```java
spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tipoSeleccionado = (TipoObservacion) parent.getItemAtPosition(position);
        actualizarVisibilidadDetallesBotanicos();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
});
```

### Detalle importante al guardar

Si no es planta, conviene no enviar datos botanicos antiguos por error. Antes de
crear o editar la observacion se puede hacer:

```java
if (!esTipoPlanta()) {
    observacion.setEspecie(null);
    observacion.setNombreTradicional(null);
    observacion.setIdentificacionPropuesta(null);
}
```

Para evitar repetir logica, se puede extraer:

```java
private boolean esTipoPlanta() {
    return tipoSeleccionado != null
            && tipoSeleccionado.getNombre() != null
            && tipoSeleccionado.getNombre().equalsIgnoreCase("Planta");
}
```

### Resultado esperado

La pantalla queda mas clara:

```text
Tipo: Planta      → muestra detalles botanicos
Tipo: Incidencia  → oculta detalles botanicos
Tipo: Rincon      → oculta detalles botanicos
```

Esta mejora es recomendable porque no cambia el backend ni la base de datos. Es
solo una mejora de interfaz y de limpieza de datos enviados desde Android.
