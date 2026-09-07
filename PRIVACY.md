# Privacy

**MedBook collects nothing, sends nothing, and asks for no permissions.**

This is the same notice the app shows under **Settings → Privacy**, and an excerpt
of it is shown once before first use. The two are kept in step: the wording lives
in `app/src/main/res/values/strings.xml` under the `privacy_` keys, and materially
changing it means raising `PRIVACY_NOTICE_VERSION` in
[`SettingsStore`](app/src/main/java/com/oxyorb/medbook/settings/SettingsStore.java),
which shows everyone the new text once.

Everything claimed below is enforced somewhere you can check it, and each section
says where.

## What MedBook collects

Nothing. There is no account, no sign-in, no analytics and no crash reporting.
MedBook has never asked you for a name, a number or an address, and has nowhere
to send one.

## What leaves your phone

Nothing. MedBook holds no internet permission, so it cannot make a network call
even if it were asked to. The directory is read from the app's own storage, with
no connection at all.

> Checkable in [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) — the
> permission list is empty, and the file says so at the top.

## What is stored on your phone

Three things, all in MedBook's private storage:

| What | Where | Backed up? |
| --- | --- | --- |
| The doctor directory | `medbook.db`, unpacked from the app on first launch | No — rebuilt from the app |
| Your theme and language | `settings.xml` | Yes |
| Demo bookings, if you turned demo mode on | `demo.xml` | No |
| That this notice was shown | `local.xml` | Not to the cloud; yes on device transfer |

## Where the directory came from

Publicly listed professional profiles — names, qualifications, chamber addresses,
visiting hours, appointment numbers and BMDC registration numbers, as their
sources published them. It is published information about doctors, not records
about you, and nothing in it is a medical record.

## Backups

Your theme and language follow you to a new phone, which is what you would expect
them to do. The directory does not: it is rebuilt from the app. Demo bookings and
the record that this notice was shown are kept out of cloud backup, so a restore
onto someone else's phone shows them this notice rather than skipping it. A
device transfer is the same person with the old phone in their hand, so the
record of the notice carries across there.

> Checkable in [`backup_rules.xml`](app/src/main/res/xml/backup_rules.xml) and
> [`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml).

## Calling a chamber

Tapping an appointment number hands it to your phone's own dialer. MedBook places
no call, and keeps no record that you tapped it.

## Demo mode

Off unless you switch it on. Everything behind it is invented: no appointment is
made, no chamber is told anything, and the patients in the chamber view are made
up. They name nobody. See [Demo mode](README.md#demo-mode).

## Corrections

If you are listed in the directory and something about you is wrong, or you would
rather not be listed at all, open an issue at
[Issues](https://github.com/zZOKofficial/MedBook/issues) and it will be corrected
in the next dataset.

For anything security-related, follow [SECURITY.md](SECURITY.md) rather than
opening a public issue.
