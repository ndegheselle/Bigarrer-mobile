# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bigarrer is an early-stage Android app (Kotlin + Jetpack Compose) being developed with AR functionality as the goal. The developer is currently studying the [ARCore hello_ar_kotlin sample](https://github.com/google-ar/arcore-android-sdk/blob/main/samples/hello_ar_kotlin/app/src/main/java/com/google/ar/core/examples/kotlin/helloar/HelloArActivity.kt) as a reference.

All Android source lives under the `Bigarrer/` subdirectory. All Gradle commands must be run from there.

## Commands

```bash
cd Bigarrer

# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Unit tests (JVM, no device needed)
./gradlew test
./gradlew test --tests "com.lubriciel.bigarrer.ExampleUnitTest.addition_isCorrect"

# Instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Clean
./gradlew clean
```

## Architecture

**Single-module** Android app. Package: `com.lubriciel.bigarrer`.

- `MainActivity` — the only activity; uses `ComponentActivity` with `enableEdgeToEdge()` and sets Compose content via `setContent {}`.
- `ui/theme/` — Compose theming. `BigarrerTheme` uses Material You dynamic colors on Android 12+ (API 31+), falling back to a static purple/pink Material3 palette. Typography lives in `Type.kt`, colors in `Color.kt`.

## Key Versions

| Component | Version |
|---|---|
| AGP | 9.2.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.02.01 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 (Android 7.0) |
| Java compatibility | 11 |

Dependencies are managed via the version catalog at `Bigarrer/gradle/libs.versions.toml`.
