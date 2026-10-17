This is a Kotlin Multiplatform project targeting Android and Web (Kotlin/Wasm).

* [/androidApp](./androidApp/src) contains the Android application entry point.
* [/shared](./shared/src) is for code shared across your Compose Multiplatform applications:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - [androidMain](./shared/src/androidMain/kotlin) is for Android-specific Kotlin code.
  - [wasmJsMain](./shared/src/wasmJsMain/kotlin) is for Web (Kotlin/Wasm) entry point and platform-specific code.

### Running the apps

You can use `just` from the root (`just app <recipe>`) or inside `app/` (`just <recipe>`), or run Gradle tasks directly:

- Web app (dev server): `just app run-web` (`./gradlew :shared:wasmJsBrowserDevelopmentRun --continuous`)
- Android app: `just app build-android` (`./gradlew :androidApp:assembleDebug`)

### Formatting & Tests

- Format code (Spotless): `just app format` (`./gradlew spotlessApply`)
- Check formatting: `just app format-check` (`./gradlew spotlessCheck`)
- Android unit tests: `just app test` (`./gradlew :shared:testAndroidHostTest`)