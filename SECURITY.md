# Security Policy

## Supported Versions

MedBook is alpha software and has no released builds. Security fixes are
applied to the `main` branch only, and there are no maintained release
branches or backports.

| Version | Supported |
| --- | --- |
| `release/0.4` (0.4.0-alpha.1) | :white_check_mark: |
| Any earlier snapshot | :x: |

The application ships no network and no authentication code, and requests no
Android permissions at all, so its practical attack surface stays small. It does
now carry local data, which is worth stating precisely:

- The doctor directory is bundled in the APK, gzipped and AES-256-GCM sealed, and
  unpacked on first launch into the app's private storage. It is read-only and is
  excluded from cloud backup and device transfer.
- **The key is compiled into the app.** Sealing stops the directory being read by
  unzipping the APK, which is what it is for. It is not a defence against someone
  who reverse-engineers the binary, and it should not be described as one. The
  unpacked database is ordinary SQLite on disk, protected by the OS on a
  non-rooted device and nothing more.
- Everything in it is public professional information, published by its source.
  There are no credentials, tokens, or user data of any kind.

That will change as the [roadmap](README.md#roadmap) is implemented, and this
policy will be revised alongside it.

## Reporting a Vulnerability

**Please do not report security issues through public GitHub issues,
discussions, or pull requests.**

Report privately through GitHub's built-in advisory workflow:

1. Open the [Security tab](https://github.com/zZOKofficial/MedBook/security)
2. Select **Report a vulnerability**
3. Complete the advisory form

This opens a private channel visible only to the maintainer.

### What to include

A useful report generally contains:

- The type of issue, and the affected file, class, or component
- The commit or branch you tested against
- Step-by-step instructions to reproduce it
- Any proof-of-concept code, along with the configuration needed to run it
- Your assessment of the impact — what an attacker could achieve

Reports written in Bangla or English are equally welcome.

### What to expect

| Stage | Target |
| --- | --- |
| Acknowledgement of your report | Within 5 days |
| Initial assessment and severity triage | Within 14 days |
| Status update while a fix is in progress | Every 14 days |

This is a personal project maintained outside of working hours, so these are
good-faith targets rather than contractual guarantees.

If the report is accepted, you will be told when the fix lands and credited in
the resulting advisory unless you would rather remain anonymous. If it is
declined, you will be given the reasoning, and you are welcome to challenge it.

## Disclosure

Please allow 90 days from your initial report before disclosing publicly, so
that a fix can be prepared. If a vulnerability is being actively exploited, get
in touch and a shorter timeline will be agreed.

## Scope

In scope: everything in this repository — application source, build
configuration, and the Gradle wrapper.

Out of scope: vulnerabilities in Android itself, in AndroidX or Material
Components, or in the Gradle distribution. Report those upstream to the
projects concerned.
