# Norvexa OneStep

**OneStep** is an offline-first Android goal planner that turns a large goal into one clear next action.

## Current version

`0.1.0` — complete local MVP foundation.

### Implemented

- onboarding without mandatory registration;
- goal creation and editing;
- localized starter plans for Russian, Ukrainian, English, and Polish;
- manual stages and steps: create, edit, delete, and reorder;
- one active goal and one next-step focus;
- complete, postpone, skip, and split actions;
- focus timer with actual-time tracking;
- goal progress, completed-step statistics, and activity streak;
- daily reminders through WorkManager;
- light, dark, AMOLED, and system themes;
- in-app language selection;
- JSON backup and restore through Android Storage Access Framework;
- TalkBack-oriented labels and minimum Material touch targets;
- local persistence with DataStore; no server or account required;
- release shrinking with R8 and cleartext traffic disabled.

## Technology

- Kotlin 2.4.10 and AGP built-in Kotlin;
- Android Gradle Plugin 9.3.0;
- Jetpack Compose + Material 3;
- Navigation Compose;
- DataStore Preferences;
- WorkManager;
- Coroutines and Flow;
- minSdk 26, targetSdk 36, compileSdk 37;
- Java 17 toolchain.

## Build

Requirements:

- Android Studio with JDK 17;
- Android SDK Platform 37;
- Android SDK Build Tools 36.0.0.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

On the first command-line run, the launcher downloads the official Gradle 9.5.0 wrapper JAR and verifies its published SHA-256 checksum before execution.

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project structure

```text
app/src/main/java/com/norvexa/onestep/
├── data/             DataStore repository and JSON codec
├── model/            Goals, stages, steps, templates
├── notifications/    WorkManager reminder pipeline
├── ui/
│   ├── screens/      Compose screens
│   └── theme/        System/light/dark/AMOLED themes
├── MainActivity.kt
├── MainViewModel.kt
└── OneStepApplication.kt
```

## Product rule

The home screen does not overwhelm the user with the complete plan. It prioritizes the current goal and the next executable step. The full plan remains available for editing from the goal details screen.

## Privacy

OneStep stores user goals locally. The MVP has no analytics SDK, advertising SDK, account system, or remote AI request. Export is initiated explicitly by the user.

## Roadmap

See [`docs/ROADMAP.md`](docs/ROADMAP.md) and [`docs/TECHNICAL_SPEC.md`](docs/TECHNICAL_SPEC.md).
