# PlantPotting

Houseplant Soil & Potting Guidance — Android app.

## Prerequisites

- JDK 17 (Temurin or any LTS-17 distribution). The Gradle wrapper expects Java 17.
- Android SDK with platform 34 installed. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`.
- For instrumentation tests on the Gradle Managed Device (`pixel6Api34`), the host needs KVM (Linux) or HAXM/WHPX (Windows/macOS) and ~6 GB of free disk for the AOSP system image.

## Build the debug APK

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Run unit tests

```bash
./gradlew testDebugUnitTest
```

## Run instrumentation tests on the managed device

```bash
./gradlew pixel6Api34DebugAndroidTest
```

The first run downloads the AOSP system image (~2 GB) and boots an emulator. Subsequent runs reuse the cached AVD.

## Lint and style

```bash
./gradlew lint ktlintCheck
```

## Full pre-PR check

Equivalent to CI:

```bash
./gradlew assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest verifyNoNetworking
```

Windows shortcut: `pwsh ./scripts/check-android.ps1`.

## Install on a connected device

```bash
./gradlew installDebug
```

Or `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
