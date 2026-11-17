# El Tuncazo — Kotlin Multiplatform (Android + iOS)

Versión multiplataforma de la app cliente. Toda la lógica **y la UI** viven en `shared/commonMain`
con Compose Multiplatform; Android e iOS solo aportan el punto de entrada.

El proyecto original `../ElTuncazoCompose` no se modificó.

## Estructura

```
ElTuncazoKMP/
├── shared/                         ← módulo KMP (todo el código común)
│   └── src/
│       ├── commonMain/kotlin/…     ← UI, navegación, ViewModels, red, modelos, sesión
│       ├── commonMain/composeResources/  ← strings.xml, imágenes y fuentes (Res.*)
│       ├── androidMain/            ← actual: DataStore path, versión, ajustes
│       ├── iosMain/                ← actual + MainViewController()
│       └── androidHostTest/        ← pruebas contra la API real
├── androidApp/                     ← MainActivity + manifest + íconos
└── iosApp/                         ← SwiftUI que muestra la UI de Compose (XcodeGen)
```

| Capa | Antes (Android) | Ahora (KMP) |
|---|---|---|
| Red | Retrofit + Gson + RxJava3 | Ktor 3 (OkHttp / Darwin) + kotlinx.serialization |
| ViewModels | LiveData + Disposable | `PeticionViewModel<T>` con StateFlow + coroutines |
| Sesión | DataStore con `Context` | DataStore KMP (`TokenManager` object) |
| Imágenes | Coil 2 | Coil 3 |
| Recursos | `R.string`, `R.drawable`, `R.color` | `Res.string`, `Res.drawable`, `AppColors` |
| Toast | Toasty | `CustomToasty` + `ToastHost` propio |

Versiones: Kotlin 2.4.21 · Compose Multiplatform 1.12.2 · AGP 9.5.1 · Gradle 9.9.1.
La URL del servidor está en `shared/.../network/ApiClient.kt` (`ApiConfig`).

## Android (Windows / Mac)

Abrir la carpeta `ElTuncazoKMP` en Android Studio y ejecutar la configuración `androidApp`, o:

```bash
./gradlew :androidApp:assembleDebug
```

La app usa el mismo `applicationId` y la misma ruta de DataStore que la versión 2.0.0, así que
al instalarse encima conserva la sesión (requiere firmarla con el mismo keystore).

## iOS (requiere Mac con Xcode)

```bash
brew install xcodegen
cd iosApp && xcodegen generate
open iosApp.xcodeproj
```

En Xcode: elegir tu Team en *Signing & Capabilities* y ejecutar. La fase de build llama a
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`, que compila `ComposeApp.framework`.

> El servidor usa **HTTP**; `project.yml` incluye una excepción de ATS solo para `165.22.176.229`.
> Cuando el backend tenga HTTPS, borrar esa excepción y cambiar las URLs en `ApiConfig`.

## Pruebas

```bash
./gradlew :shared:testAndroidHostTest
```

Llama a endpoints de solo lectura del servidor real y verifica que el JSON se interpreta bien.

## Cambios de comportamiento respecto a la app anterior

- Las peticiones que fallan se reintentan 3 veces con espera (antes `.retry()` infinito).
- El tema ya no usa "dynamic color" de Android 12+, para verse igual en ambas plataformas.
- Los campos que faltan en el JSON toman valores por defecto en lugar de fallar.
