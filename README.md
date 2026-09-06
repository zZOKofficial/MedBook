<p align="center">
  <img src="medbook.svg" alt="MedBook" width="128">
</p>

# MedBook

**Your Health, Your Schedule.**

[![Status](https://img.shields.io/badge/status-alpha-orange)](#project-status)
[![Version](https://img.shields.io/badge/version-0.3.0--alpha.1-blue)](app/build.gradle)
[![Platform](https://img.shields.io/badge/platform-Android%205.0%2B-3DDC84?logo=android&logoColor=white)](#requirements)
[![License](https://img.shields.io/badge/license-AGPL--3.0-lightgrey)](LICENSE)
[![Size](https://img.shields.io/badge/apk-28.2%20MB-blue)](#about-the-data)

MedBook is an Android application intended to connect patients with healthcare
providers — browsing medical departments, finding doctors, and booking
appointments from a phone.

---

## Project Status

**Alpha. The directory works offline; booking and accounts do not exist.**

This section is deliberately blunt so that contributors and users know exactly
what they are looking at.

| Area | State |
| --- | --- |
| Splash screen | Working — the MedBook mark, held 800ms, with a fade hand-off |
| Home screen | Working — 45 departments with live counts, light and dark |
| Grouping | Working — the 45 sit under twelve headings, by body system |
| **Doctor directory** | **Working — 7,438 doctors and 9,350 chambers, offline** |
| Doctor profiles | Working — degrees, chambers, verbatim hours, tap to dial |
| Search | Working — full-text over names, specialties, workplaces and cities |
| Settings | Working — reached from the home screen, theme and language |
| Light and dark | Working — follows the device, or overridden per app |
| Bangla | Working — the interface and all 45 departments; the directory stays English |
| Launcher icon | Working — adaptive, with a monochrome layer for themed icons |
| Data layer | Working — a prebuilt SQLite database packed into the APK |
| Appointment booking | Not built |
| Accounts and sign-in | Not built |
| Reviews and ratings | Read-only — ratings are shown as published, never collected |

Everything in [Roadmap](#roadmap) is planned work, not shipped work.

### About the data

The directory is a snapshot of publicly listed doctor profiles, built offline and
bundled with the app. There is no server and no network call: the app requests no
permissions at all, not even internet access.

The dataset itself is **not in this repository**, and neither is the pipeline that
builds it. It covers thousands of named practitioners along with their chamber
addresses, appointment numbers and BMDC registration numbers, which is not something
to publish as a downloadable file. A checkout without it still builds and runs — the
directory is simply empty.

Carrying it is most of the app's size. The release APK is **28.2 MB**, against about
6 MB before the directory existed: 21.4 MB of that is 6,134 doctor portraits at 160 px
WebP, and 3.7 MB is the database itself — 7,438 doctors, 9,350 chambers, 1,690
hospitals and a full-text index, gzipped and sealed, unpacked to private storage on
first launch.

The seal keeps the directory from being readable by unzipping the APK, which is the
point of it. It is not protection against a reverse engineer: the key is compiled into
the app. Preventing bulk extraction outright would need the data to live on a server,
which MedBook deliberately does not have — the directory works with no connection at
all.

## Screens

**Home** (`HomeActivity`) — the MedBook wordmark, a search field, and a
scrolling list of 45 medical departments grouped under twelve headings by
the part of the body involved, from Urgent & critical care through General &
diagnostic services. Each row carries its doctor count. Searching queries
doctors and departments together, and returns departments first.

**Department** (`DepartmentDoctorsActivity`) — every doctor in one department,
verified profiles first, then the most reviewed. Paged fifty at a time; the
largest department holds 1,420.

**Doctor** (`DoctorDetailActivity`) — portrait, degrees, designation, workplace,
experience and BMDC registration, then a card per chamber with its verbatim
address and opening hours and a tap-to-dial appointment number. Nothing is
inferred: a fact the source does not state is simply absent, an unrated doctor
shows no rating rather than a zero, and the 1,304 profiles with no portrait get
their initials rather than a stock photo.

**Settings** (`SettingsActivity`) — reached from the gear beside the wordmark.
Theme is Follow the device, Light or Dark, so someone on a light phone can still
read MedBook in dark. Language is Follow the device, English or বাংলা. Both are
remembered, and both survive a restart.

Typography comes from the type scale in `values/styles.xml` — Gabarito for the
wordmark, Hind Siliguri for everything else.

## Bangla

The interface is fully Bengali: every string, all 45 department names and the
twelve family headings. **The directory itself stays English**, and that is a
property of the data rather than unfinished work.

The source publishes almost no Bengali. Measured across the dataset:

| Field | Rows | Contains Bengali |
| --- | --- | --- |
| Doctor names | 7,438 | 0 (0.0%) |
| Chamber names | 9,350 | 0 (0.0%) |
| Chamber addresses | 9,348 | 5 (0.1%) |
| Biographies (`about_bn`) | 347 | 347 (100%) |

A Bengali directory would mean inventing 4,404 distinct degree strings, 3,542
visiting-hours strings and 2,349 addresses with nothing to translate from — and
most of it should not be translated anyway. Doctor and hospital names, BMDC
numbers and degrees such as MBBS and FCPS are written in Latin script in
Bangladeshi practice, and a translated chamber address is harder to find, not
easier. The one Bengali field the source does publish, `about_bn`, has been shown
on the profile since 0.2.

Department names are the exception because they are a closed vocabulary the app
owns rather than data it received. They are resolved from `departments.key`
through a compile-time map — deliberately not `Resources.getIdentifier`, which
`shrinkResources` would strip from release builds only, leaving every debug build
looking correct.

Search matches both languages at once, so `cardiology` still finds হৃদরোগ কেন্দ্র
while the interface is in Bengali.

Counts and ratings render in Bengali digits, which is correct Bengali typography.
Phone numbers and BMDC registrations deliberately do not.

## Tech Stack

| | |
| --- | --- |
| Language | Java |
| Min SDK | 21 (Android 5.0 Lollipop) |
| Compile / Target SDK | 36 (Android 16) |
| Build | Gradle 9.7.1 · Android Gradle Plugin 9.4.0 |
| Toolchain | JDK 25, resolved automatically by the Gradle daemon |
| UI | Material Components 1.12.0 · AndroidX AppCompat 1.7.1 · RecyclerView 1.4.0 |
| Data | Prebuilt SQLite, opened read-only. FTS4 for search |
| Theme | Material 3 DayNight, edge-to-edge, no action bar |
| Colour | Generated from the mark: primary `#1C5B6C`, secondary `#42A4AB` |
| Typography | Gabarito + Hind Siliguri (both SIL OFL 1.1), as a type scale |
| Window insets | Handled on every screen; required from API 35 |
| View access | View Binding |
| Languages | English and বাংলা, switchable in-app; per-app locales on API 33+ |
| Icon | Adaptive, with a monochrome layer for Android 13 themed icons |
| Tests | JUnit 4 unit tests over department search; no device needed |

The project has no backend, no analytics, and no third-party SDKs beyond AndroidX
and Material Components. The directory needs none: the database is prebuilt and never
written to, so there are no migrations for Room to manage, and portraits are decoded
straight from the APK's assets rather than fetched, so there is nothing for an image
loading library to do.

## Project Structure

```
MedBook/
├── app/
│   ├── build.gradle                  # SDK levels, deps, signing, dataset wiring
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/oxyorb/medbook/
│       │   ├── MedBookApp.java                # Applies theme and language at startup
│       │   ├── HomeActivity.java              # Departments, search, splash handoff
│       │   ├── SettingsActivity.java          # Theme and language
│       │   ├── DepartmentDoctorsActivity.java # One department, paged
│       │   ├── DoctorDetailActivity.java      # One profile and its chambers
│       │   ├── DirectoryAdapter.java          # Headings, departments and doctors
│       │   ├── settings/
│       │   │   ├── SettingsStore.java         # Preferences, split by what backup carries
│       │   │   └── LocaleController.java      # Applies and reconciles the language
│       │   └── data/
│       │       ├── DatasetUnpacker.java       # Unseals the bundled directory
│       │       ├── DepartmentNames.java       # Department names in the current locale
│       │       ├── DoctorRepository.java      # Read-only queries and search
│       │       ├── PortraitLoader.java        # Decodes portraits from assets
│       │       ├── InitialsDrawable.java      # Fallback when there is no portrait
│       │       └── model/                     # Department, Doctor, Chamber, summary
│       └── res/
│           ├── font/                 # Gabarito + Hind Siliguri, subsetted
│           ├── layout/               # Home, department, profile, and row layouts
│           ├── drawable/             # Splash mark, launcher layers, search, back arrow
│           ├── mipmap-anydpi-v26/    # Adaptive launcher icon
│           ├── mipmap-*/             # Legacy launcher bitmaps, API 21-25
│           ├── values/               # colors.xml, colors_m3.xml, styles.xml, strings
│           ├── values-night/         # Dark overrides for both colour files
│           ├── values-bn/            # Bengali interface and department names
│           └── xml/                  # Backup rules and the locale config
├── gradle/wrapper/                   # Pinned Gradle distribution
├── build.gradle                      # Root build script
├── settings.gradle                   # Module and repository declarations
└── gradle.properties

# Not in the repository, and required only for the doctor directory:
#   dataset.properties                # Points at the private archive, plus its key
#   keystore.properties               # Release signing
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

This builds without the doctor directory, which is what a clone gets: there is no
`dataset.properties`, so no directory data is packaged and the app opens on an
empty state. Everything else — the departments, the search field, the theme —
behaves normally. Release signing degrades the same way, producing an unsigned
release rather than failing.

Opening the project folder in Android Studio and pressing **Run** works
equally well; `local.properties` is generated on first sync and is
intentionally not tracked.

## Roadmap

Ordered roughly by dependency — each item builds on the ones above it.

1. ~~**Wire the existing UI**~~ — done in 0.1.0-alpha.2
2. ~~**Data-driven departments**~~ — done in 0.2.0-alpha.1
3. ~~**Doctor directory**~~ — done in 0.2.0-alpha.1
4. ~~**Doctor profiles**~~ — done in 0.2.0-alpha.1
5. **Better discovery** — map symptoms to departments, filter by city, and sort
   by who is available soonest rather than by standing
6. **Appointment booking** — slot selection and confirmation
7. **Accounts** — registration, sign-in, and per-patient appointment history
8. ~~**Localisation**~~ — done in 0.3.0-alpha.1. The interface and all 45
   department names are Bangla; the directory itself stays English, for the
   reasons set out under [Bangla](#bangla)

Longer term: prescription management, medical-record storage, doctor ratings,
consultation payments, and telemedicine.

## Contributing

Contributions are welcome, and the roadmap above is the best place to start —
item 5 is self-contained and needs no backend.

Development happens on release branches — **`release/0.3`** is the current one,
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

Hind Siliguri draws Latin and Bengali as one family, so the Bangla interface
needs no second face and has no visual mismatch. It comes from Indian Type Foundry,
and the subsets here retain the full Indic layout-feature set, without which
Bengali conjuncts would not form.

Only subsets are redistributed — 388 KB on disk for all four files, 195 KB once
packed into the APK, against ~890 KB for the full faces. Coverage is verified
against every character the app renders.

Typography is defined once as a scale in `values/styles.xml`, and each role
names a concrete font file, so no weight is ever synthesised.

## License

Licensed under the **GNU Affero General Public License v3.0**. See
[LICENSE](LICENSE) for the full text.

The AGPL requires that anyone who runs a modified version of this software over
a network make their source available to its users.

## Author

Developed by **Md. Maruf Hossain** ([zZOK](https://github.com/zZOKofficial)),
and published under **OxyOrb**, the consultancy he founded on 1 January 2024.
The application identifier is `com.oxyorb.medbook`.

---

*Android · Healthcare · Appointment Booking · Java · Material 3*
