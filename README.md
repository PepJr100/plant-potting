# PlantPotting

Houseplant Soil & Potting Guidance — Android app.

## Prerequisites

- JDK 17 (Temurin or any LTS-17 distribution). The Gradle wrapper expects Java 17.
- Android SDK with platform 34 installed. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`.
- For instrumentation tests on the Gradle Managed Device (`pixel6Api34`), the host needs KVM (Linux) or WHPX/HAXM (Windows/macOS) and ~6 GB of free disk for the AOSP system image.

## First-time setup (Windows)

If you've never built this project before, follow these steps in order.

### 1. JDK 17

Install Temurin 17 from <https://adoptium.net/> or via winget:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

Verify:

```powershell
java -version    # should report 17.x
```

If `java` resolves to something else, set `JAVA_HOME` to the Temurin install dir and put `%JAVA_HOME%\bin` on `PATH`.

### 2. Android Studio (recommended) or standalone SDK

The simplest path is Android Studio — it bundles the SDK manager, emulator, and AVD manager into one installer:

```powershell
winget install Google.AndroidStudio
```

Launch it once and let the setup wizard install the default SDK. Then open **SDK Manager** (Settings → Languages & Frameworks → Android SDK) and install:

- **SDK Platforms** tab: *Android 14.0 ("UpsideDownCake") API 34*
- **SDK Tools** tab: *Android SDK Build-Tools 34*, *Android SDK Command-line Tools (latest)*, *Android SDK Platform-Tools*, *Android Emulator*

After install, set the environment variable so the Gradle build can find the SDK:

```powershell
setx ANDROID_HOME "$env:LOCALAPPDATA\Android\Sdk"
```

Open a fresh terminal (env-var changes need a new shell) and verify:

```powershell
echo $env:ANDROID_HOME
& "$env:ANDROID_HOME\platform-tools\adb.exe" version
```

### 3. Hardware acceleration (WHPX)

The emulator needs WHPX to boot in seconds instead of minutes. Check whether it's on:

```powershell
# Run this in an *elevated* PowerShell:
Get-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform | Select State
```

If `State` isn't `Enabled`, enable it from the same elevated shell and reboot:

```powershell
Enable-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform -All
```

WHPX coexists with Docker Desktop / WSL2; you don't have to choose.

### 4. Create the AVD

In Android Studio: **Device Manager → Create Virtual Device**.

- Hardware: *Pixel 6*
- System image: *API 34 ("UpsideDownCake") with **Google APIs***
  (the Google APIs image has a synthetic camera; the plain AOSP image, which Gradle Managed Device uses, doesn't)
- Click *Finish*, then the play button to boot it.

A first cold boot takes ~60 seconds. Leave the emulator running between gradle runs.

### 5. First build

From the repo root with the emulator running:

```powershell
./gradlew.bat assembleDebug
```

The first run downloads Gradle 8.9 and the AGP plugin chain — expect 5–10 minutes. Subsequent runs are seconds.

## Daily commands

### Build the debug APK

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

### Run JVM unit tests

```bash
./gradlew testDebugUnitTest
```

These don't need an emulator. They cover the KB validator, recommendation engine, ViewModels, and Robolectric-backed Compose UI tests.

### Install and run on the booted emulator

```bash
./gradlew installDebug
```

Then tap the app icon in the emulator's launcher. The §2.1 manual flow:

1. Tap **Grant camera access** on the permission screen.
2. On the camera preview, tap the shutter.
3. The result screen shows *Monstera deliciosa* (deterministic stub) + the "Stub identifier — replace in a later sprint" badge.
4. Tap **See potting mix** → recipe rows visible, proportions sum to 100.
5. Tap **Retake** to return to the camera.

### Run instrumentation tests against the booted emulator

```bash
./gradlew connectedDebugAndroidTest
```

Faster than the GMD path because the emulator is already warm. Use this for local iteration.

### Run instrumentation tests via Gradle Managed Device

```bash
./gradlew pixel6Api34DebugAndroidTest
```

What CI runs. Boots a headless AOSP `pixel6Api34` emulator from scratch each time. First run downloads the AOSP system image (~2 GB).

### Lint and style

```bash
./gradlew lint ktlintCheck
```

### Full pre-PR check

```bash
./gradlew assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest verifyNoNetworking
```

Windows shortcut: `pwsh ./scripts/check-android.ps1`.

### Install on a connected physical device

Same command — `adb` finds the device:

```bash
./gradlew installDebug
```

Or directly: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
