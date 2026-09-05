<img width="1024" height="1024" alt="MedBook" src="https://github.com/user-attachments/assets/318b6b23-d395-4039-83c4-465f21e06e6e" />

# MedBook

**Your Health, Your Schedule.**

[![Status](https://img.shields.io/badge/status-pre--alpha-orange)](#project-status)
[![Version](https://img.shields.io/badge/version-0.0.5%20pre__alpha-blue)](app/build.gradle)
[![Platform](https://img.shields.io/badge/platform-Android%205.0%2B-3DDC84?logo=android&logoColor=white)](#requirements)
[![License](https://img.shields.io/badge/license-AGPL--3.0-lightgrey)](LICENSE)

MedBook is an Android application intended to connect patients with healthcare
providers — browsing medical departments, finding doctors, and booking
appointments from a phone.

---

## Project Status

**Pre-alpha. The user interface exists; the behaviour behind it does not yet.**

This section is deliberately blunt so that contributors and users know exactly
what they are looking at.

| Area | State |
| --- | --- |
| Splash screen | Working — displays for 500 ms, then opens the home screen |
| Home screen layout | Working — renders 46 medical department buttons |
| Department buttons | **Not wired** — no click handlers are attached |
| Search | **Not wired** — the `SearchView` has no query listener |
| Doctor profiles | Not built |
| Appointment booking | Not built |
| Accounts and sign-in | Not built |
| Backend / data layer | Not built — the app ships no network or database code |

Everything in [Roadmap](#roadmap) is planned work, not shipped work.

## Screens

**Splash** (`MainActivity`) — app icon, wordmark, and tagline, shown briefly
before handing off to the home screen.

**Home** (`HomeActivity`) — the MedBook wordmark, a search field, and a
scrolling list of 46 medical departments, from Accident & Emergency through
Urology. Typography is applied at runtime from the bundled SF Pro Display faces.

## Tech Stack

| | |
| --- | --- |
| Language | Java |
| Min SDK | 21 (Android 5.0 Lollipop) |
| Compile / Target SDK | 36 (Android 16) |
| Build | Gradle 9.7.1 · Android Gradle Plugin 9.4.0 |
| Toolchain | JDK 25, resolved automatically by the Gradle daemon |
| UI | Material Components 1.12.0 · AndroidX AppCompat 1.7.1 |
| Theme | Material 3 DayNight, edge-to-edge, no action bar |
| Window insets | Handled on the home screen; required from API 35 |
| View access | View Binding |

The project has no backend, no analytics, and no third-party SDKs beyond
AndroidX and Material Components.

## Project Structure

```
MedBook/
├── app/
│   ├── build.gradle                  # Module config: SDK levels, deps, view binding
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/fonts/             # SF Pro Display and Kohinoor Bangla faces
│       ├── java/com/zzok/medbook/
│       │   ├── MainActivity.java     # Splash; forwards to HomeActivity
│       │   ├── HomeActivity.java     # Department list and search field
│       │   ├── FileUtil.java         # File I/O helpers
│       │   └── SketchwareUtil.java   # Toast, display metrics, sorting helpers
│       └── res/
│           ├── layout/               # main.xml, home.xml
│           ├── drawable-xhdpi/       # App and splash imagery
│           ├── mipmap-xhdpi/         # Launcher icon
│           └── values/               # colors.xml, strings.xml, styles.xml
├── gradle/wrapper/                   # Pinned Gradle distribution
├── build.gradle                      # Root build script
├── settings.gradle                   # Module and repository declarations
└── gradle.properties
```

`FileUtil` and `SketchwareUtil` are general-purpose helpers carried over from
the project's Sketchware origins. They are not currently called from either
activity and are retained for upcoming work.

## Requirements

- **Android Studio** — a release that supports **Android Gradle Plugin 9.4**.
  Check the [AGP compatibility table](https://developer.android.com/studio/releases#android_gradle_plugin_and_android_studio_compatibility)
  for the matching version; older releases will refuse to sync this project.
- **A JVM on `PATH` or `JAVA_HOME`** to start the Gradle wrapper. Android
  Studio's bundled JetBrains Runtime satisfies this. If you build from a
  terminal and hit `JAVA_HOME is not set`, point it at that runtime.
  Gradle then provisions the **JDK 25** toolchain itself via the Foojay
  resolver — see `gradle/gradle-daemon-jvm.properties`.
- **Android SDK Platform 36** — Gradle downloads it on first build if the
  SDK licences are already accepted.
- A device or emulator running **Android 5.0 (API 21)** or newer

## Building

Clone the repository and build with the included wrapper — do not use a
system-wide Gradle installation, as the wrapper pins the supported version.

```bash
git clone https://github.com/zZOKofficial/MedBook.git
cd MedBook
```

**Linux / macOS**

```bash
./gradlew assembleDebug
```

**Windows**

```powershell
.\gradlew.bat assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`. To build and install onto
a connected device in one step, use `installDebug` in place of `assembleDebug`.

Opening the project folder in Android Studio and pressing **Run** works
equally well; `local.properties` is generated on first sync and is
intentionally not tracked.

## Roadmap

Ordered roughly by dependency — each item builds on the ones above it.

1. **Wire the existing UI** — click handlers for the 46 department buttons, and
   a query listener for the search field
2. **Doctor directory** — list practitioners per department, with a data layer
   behind it
3. **Doctor profiles** — qualifications, biography, and availability
4. **Appointment booking** — slot selection and confirmation
5. **Accounts** — registration, sign-in, and per-patient appointment history
6. **Reminders** — notifications ahead of a booked appointment
7. **Clinic locations** — maps and directions
8. **Localisation** — Bangla support, for which the font assets are already bundled

Longer term: prescription management, medical-record storage, doctor ratings,
consultation payments, and telemedicine.

## Contributing

Contributions are welcome, and the roadmap above is the best place to start —
item 1 is self-contained and needs no backend.

1. Fork the repository and branch from `main`
2. Keep changes focused; one concern per pull request
3. Match the existing style — tabs for indentation, and the `binding.*`
   accessors rather than `findViewById`
4. Confirm `./gradlew assembleDebug` succeeds before opening the pull request
5. Describe what you changed and how you verified it

Bug reports and feature requests belong in
[Issues](https://github.com/zZOKofficial/MedBook/issues); templates are
provided for both. For security matters, follow [SECURITY.md](SECURITY.md)
instead of opening a public issue.

## License

Licensed under the **GNU Affero General Public License v3.0**. See
[LICENSE](LICENSE) for the full text.

The AGPL requires that anyone who runs a modified version of this software over
a network make their source available to its users.

## Author

Developed by **Md. Maruf Hossain** ([zZOK](https://github.com/zZOKofficial)).

---

*Android · Healthcare · Appointment Booking · Java · Material 3*
