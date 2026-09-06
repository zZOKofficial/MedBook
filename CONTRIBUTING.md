# Contributing to MedBook

## Branching

`main` is the trunk and carries the shared baseline. Each minor version gets a
long-lived release branch where that version's work happens, and which merges
back into `main` when the version ships.

```
main         ──●──────────────────●───────────────────●───────────●─────▶
              │                  ╱                   ╱           ╱
              ├── release/0.1 ──●                   ╱           ╱
              │         │                          ╱           ╱
              │      v0.1.0-alpha.1               ╱           ╱
              ├────────── release/0.2 ───────────●           ╱
              │                   │                         ╱
              │             v0.2.0-alpha.1                  ╱
              ├────────────────── release/0.3 ─────────────●
              │                            │
              │                      v0.3.0-alpha.1
              └───────────────────────────── release/0.4 ──────────────▶
```

**The current development branch is `release/0.4`.** Day-to-day work goes there,
not on `main`.

Release branches are named for the **minor** version — `release/0.1`, not
`release/0.1.0` — because patch releases (0.1.1, 0.1.2) happen on the branch.

| Kind of work | Branch from | Name it |
| --- | --- | --- |
| A feature or roadmap stage | current release branch | `feature/wire-department-buttons` |
| A bug fix | current release branch | `fix/search-crash` |
| An urgent fix to a shipped version | that `release/x.y` | `hotfix/0.1.1-crash` |

When a new minor version starts, bump the version on `main` first, then cut the
release branch from it. If the bump lives only on the release branch, `main`
keeps the stale version and the next release branch inherits it.

## Versioning

[Semantic Versioning](https://semver.org) with a pre-release suffix:

```
0.1.0-alpha.1
│ │ │  │     └── iteration within this pre-release
│ │ │  └──────── stage: alpha -> beta -> (none)
│ │ └─────────── patch: backwards-compatible fixes
│ └───────────── minor: new functionality
└─────────────── major: 0 means unstable, anything may change
```

`0.1.0-alpha.1` sorts *before* `0.1.0` — a pre-release leads to that version,
it does not follow it.

Two fields in `app/build.gradle` move together:

- **`versionName`** is the human label, e.g. `"0.1.0-alpha.1"`.
- **`versionCode`** is an integer that **only ever increases**. Android and
  Google Play use it to order builds, and Play rejects an upload whose code is
  not higher than one already published. It is unrelated to `versionName` — it
  never resets when the name changes.

The version string also appears in `README.md` (two badges) and `SECURITY.md`
(prose and the supported-versions table). Change all of them together.

Tag each version as `v<version>`, e.g. `v0.1.0-alpha.1`. Tags do not travel with
`git push`; push them explicitly.

## Code style

- **Tabs** for indentation, matching the existing sources.
- Use the generated `binding.*` accessors, not `findViewById`.
- Typography comes from the type scale in `values/styles.xml`. Do not call
  `setTypeface` per view — that is what the project was rebuilt to remove.
- Keep user-facing strings in `values/strings.xml` so Bangla localisation stays
  possible.

## Before opening a pull request

- `./gradlew assembleDebug` succeeds.
- `./gradlew assembleRelease` succeeds if you touched anything R8 might strip.
- One concern per pull request.
- Say what you changed **and how you verified it**.

## Reporting problems

Bugs and feature requests belong in
[Issues](https://github.com/zZOKofficial/MedBook/issues); templates are provided
for both. For anything security-related, follow [SECURITY.md](SECURITY.md)
rather than opening a public issue.
