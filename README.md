<img width="1024" height="1024" alt="MedBook" src="https://github.com/user-attachments/assets/318b6b23-d395-4039-83c4-465f21e06e6e" />

# MedBook

**Your Health, Your Schedule.**

[![Status](https://img.shields.io/badge/status-alpha-orange)](#project-status)
[![Version](https://img.shields.io/badge/version-0.1.0--alpha.2-blue)](app/build.gradle)
[![Platform](https://img.shields.io/badge/platform-Android%205.0%2B-3DDC84?logo=android&logoColor=white)](#requirements)
[![License](https://img.shields.io/badge/license-AGPL--3.0-lightgrey)](LICENSE)

MedBook is an Android application intended to connect patients with healthcare
providers — browsing medical departments, finding doctors, and booking
appointments from a phone.

---

## Project Status

**Pre-alpha. The interface works; there is no medical data behind it yet.**

This section is deliberately blunt so that contributors and users know exactly
what they are looking at.

| Area | State |
| --- | --- |
| Splash screen | Working — the system splash screen, with no artificial delay |
| Home screen | Working — 46 medical departments, light and dark |
| Department buttons | Working — tappable, but say doctors are coming soon |
| Search | Working — filters departments, debounced, `&` matches `and` |
| **Doctor directory** | **Not built — the app contains no doctors** |
| Doctor profiles | Not built |
| Appointment booking | Not built |
| Accounts and sign-in | Not built |
| Backend / data layer | Not built — the app ships no network or database code |

Everything in [Roadmap](#roadmap) is planned work, not shipped work.

## Screens

**Home** (`HomeActivity`) — the MedBook wordmark, a search field, and a
scrolling list of 46 medical departments, from Accident & Emergency through
Urology. Typography comes from the type scale in `values/styles.xml` — Gabarito
for the wordmark, Hind Siliguri for everything else.

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
| Typography | Gabarito + Hind Siliguri (both SIL OFL 1.1), as a type scale |
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
│       ├── java/com/zzok/medbook/
│       │   └── HomeActivity.java     # Department list, search, splash handoff
│       └── res/
│           ├── font/                 # Gabarito + Hind Siliguri, subsetted
│           ├── layout/               # home.xml
│           ├── drawable-xhdpi/       # App and splash imagery
│           ├── mipmap-xhdpi/         # Launcher icon
│           └── values/               # colors.xml, strings.xml, styles.xml
├── gradle/wrapper/                   # Pinned Gradle distribution
├── build.gradle                      # Root build script
├── settings.gradle                   # Module and repository declarations
└── gradle.properties
```

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

1. ~~**Wire the existing UI**~~ — done in 0.1.0-alpha.2
2. **Data-driven departments** — move the 46 out of the layout and into data,
   behind a RecyclerView
3. **Doctor directory** — list practitioners per department, stored in Room and
   seeded from a bundled dataset
4. **Doctor profiles** — qualifications, biography, and chamber times
5. **Better discovery** — group the departments, map symptoms to them, and sort
   by who is available soonest rather than alphabetically
6. **Appointment booking** — slot selection and confirmation
7. **Accounts** — registration, sign-in, and per-patient appointment history
8. **Localisation** — Bangla, which the Hind Siliguri interface face already
   supports

Longer term: prescription management, medical-record storage, doctor ratings,
consultation payments, and telemedicine.

## Contributing

Contributions are welcome, and the roadmap above is the best place to start —
item 1 is self-contained and needs no backend.

Development happens on release branches — **`release/0.1`** is the current one,
so branch from there rather than from `main`. See
[CONTRIBUTING.md](CONTRIBUTING.md) for the branching model, the versioning
scheme, and the code style.

Bug reports and feature requests belong in
[Issues](https://github.com/zZOKofficial/MedBook/issues); templates are
provided for both. For security matters, follow [SECURITY.md](SECURITY.md)
instead of opening a public issue.

## Fonts

MedBook uses two typefaces, both under the
[SIL Open Font License 1.1](https://scripts.sil.org/OFL):

| Role | Face | Weights | Licence |
| --- | --- | --- | --- |
| Wordmark | **Gabarito** ExtraBold | 800 | [OFL](licenses/Gabarito-OFL.txt) |
| Interface | **Hind Siliguri** | Regular 400, SemiBold 600, Bold 700 | [OFL](licenses/HindSiliguri-OFL.txt) |

Hind Siliguri draws Latin and Bengali as one family, so Bangla localisation
needs no second face and no visual mismatch. It comes from Indian Type Foundry,
and the subsets here retain the full Indic layout-feature set, without which
Bengali conjuncts would not form.

Only subsets are redistributed — 195 KB for all four files, against ~890 KB for
the full faces. Coverage is verified against every character the app renders.

Typography is defined once as a scale in `values/styles.xml`, and each role
names a concrete font file, so no weight is ever synthesised.

## License

Licensed under the **GNU Affero General Public License v3.0**. See
[LICENSE](LICENSE) for the full text.

The AGPL requires that anyone who runs a modified version of this software over
a network make their source available to its users.

## Author

Developed by **Md. Maruf Hossain** ([zZOK](https://github.com/zZOKofficial)).

---

*Android · Healthcare · Appointment Booking · Java · Material 3*
