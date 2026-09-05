# Security Policy

## Supported Versions

MedBook is pre-alpha software and has no released builds. Security fixes are
applied to the `main` branch only, and there are no maintained release
branches or backports.

| Version | Supported |
| --- | --- |
| `main` (0.0.5 pre_alpha) | :white_check_mark: |
| Any earlier snapshot | :x: |

Because the application currently ships no network, storage, or authentication
code, its practical attack surface is small. That will change as the
[roadmap](README.md#roadmap) is implemented, and this policy will be revised
alongside it.

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
