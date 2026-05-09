# Repository Guidelines

## Project Structure & Module Organization
DanboTV is a multi-module Android Gradle project with `app`, `quickjs`, and `pyramid` modules. Main app code is under `app/src/main/java`, resources under `app/src/main/res`, assets under `app/src/main/assets`, and native libraries under `app/src/main/jniLibs`. Room schema output is configured for `app/schemas`.

The `quickjs` module contains the Android QuickJS wrapper in `quickjs/src/main/java`. The `pyramid` module contains Python spider/runtime code in `pyramid/src/python`.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

```sh
./gradlew clean
./gradlew assembleDebug
./gradlew assembleArm64GenericNormalDebug
./gradlew assembleArmeabiGenericPythonDebug
./gradlew lint
./gradlew test
```

Flavor-specific tasks combine the `abi`, `brand`, and `mode` dimensions: `arm64` or `armeabi`, `generic` or `hisense`, and `normal` or `python`.

## Coding Style & Naming Conventions
Java and Kotlin sources target Java 8 compatibility and use AndroidX dependencies. Follow existing Gradle and IDE formatting, with 4-space indentation. Place classes in package-aligned directories, use `PascalCase` for classes, and use `camelCase` for methods and fields.

Android resource names should stay lowercase with underscores, such as `activity_main.xml` or `dialog_confirm.xml`. Keep Kotlin formatting consistent with current project style.

## Testing Guidelines
No `src/test` or `src/androidTest` source sets are currently present. Add JVM unit tests under `app/src/test` and Android instrumentation tests under `app/src/androidTest`. Name tests after the subject behavior, for example `CacheManagerTest`.

Before opening a PR, run the narrowest relevant Gradle task plus `./gradlew test` or `./gradlew lint` for shared code, resources, manifests, or dependencies.

## Commit & Pull Request Guidelines
The current history uses terse messages such as `update` alongside short Chinese descriptions. Prefer concise imperative commit titles and put context in the PR description when needed.

PRs should list build or test commands run, link related issues when applicable, and include screenshots or recordings for UI changes.

## Configuration Cautions
Do not change application IDs, flavor dimensions, native libraries, bundled assets, or Proguard files casually. The `python` mode depends on `pyramid` and extra Proguard rules, while Room schema generation writes to `app/schemas`; keep those outputs intentional.
