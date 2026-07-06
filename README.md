# Task Tracker — Android

A native Android task manager built around a category-balance view (Work / Auxiliary / Personal), stackable reminders that ask for a status update when they fire, and a running timeline of updates per task.

## What's in v1

- **Category board** — three cards at the top show open / due-today / overdue counts per category. The card turns rust-red when anything is overdue. Tap to filter the list to that category.
- **Task fields** — title, category, assignor (autocompletes from a saved list you build up), priority, deadline, notes.
- **Deadline** — quick-picks (end of today, end of tomorrow, in 3/7/14 days) or a full date + time picker.
- **Stackable reminders** — pick any combination of 1 week, 3 days, 1 day, 3 hours, 1 hour before, or at deadline. Each fires as its own notification.
- **Notification actions** — "Mark done" (silent) and "Update status" (opens a status + note sheet over whatever you're doing).
- **Status pick** — Not started / In progress / Blocked / Deferred / Done, with an optional note. Every update lands on the task's timeline with a timestamp.
- **Overdue nudges** — a background worker checks every 6 hours; if a task is overdue and hasn't been nudged in the last 24 hours, it re-fires the notification.
- **Reboot safety** — pending reminders are re-scheduled with the OS after device reboot or app update.
- **Local-only** — all data lives in a Room database on the device. No account, no cloud, no analytics.

## Prerequisites

- **Android Studio** — Ladybug (2024.2.1) or newer.
- **JDK 17** — Android Studio bundles this; if you build from the command line, install JDK 17 separately.
- **Android SDK** — SDK Platform 34, Build Tools 34.x. Android Studio prompts on first sync.
- **Test device or emulator** — Android 8.0 (API 26) or newer. To feel the notification behavior properly, use a real phone.

## Getting it running (Android Studio)

1. **Unzip** `TaskTracker.zip` somewhere convenient — e.g. `~/AndroidStudioProjects/TaskTracker/`.
2. **Open the folder in Android Studio** (`File → Open`, pick the `TaskTracker` folder, not the zip). First open triggers Gradle sync, which downloads the Android Gradle Plugin, Kotlin, Compose, Room, and WorkManager. This can take 5–15 minutes on the first run.
3. **Generate the Gradle wrapper** (only needed once, only if command-line builds are wanted): in the terminal inside Android Studio, run `gradle wrapper --gradle-version 8.9`. Not required if you'll build via the IDE.
4. **Connect your Android phone** with USB debugging turned on (`Settings → About phone → tap Build number 7 times → Developer options → USB debugging`), or start an emulator via `Tools → Device Manager`.
5. **Run** — hit the green ▶ button (or `Shift+F10`). Android Studio builds, installs, and launches on the selected device.

## Getting it onto your phone (APK sideload)

If you want to install without leaving the app open in the IDE:

1. In Android Studio: `Build → Build App Bundle(s) / APK(s) → Build APK(s)`.
2. When the notification appears in the bottom-right, click **locate**. It'll be at `app/build/outputs/apk/debug/app-debug.apk`.
3. Copy that APK to your phone (USB, Google Drive, WhatsApp to self, whatever). On the phone, tap it. Android will ask you to allow installation from unknown sources for the source app (e.g. Files or Chrome).
4. Install. Launch. The first launch requests notification permission — grant it.

## Permissions the app asks for

- **Post notifications** (Android 13+) — asked at runtime on first launch. Required for reminders to appear.
- **Exact alarms** (`USE_EXACT_ALARM`) — auto-granted at install time on Android 12+ because this is a task-management app.
- **Boot completed** — silent, no user prompt. Used to re-schedule your pending reminders after a reboot.

On some manufacturer skins (Xiaomi/MIUI, Oppo/ColorOS, Vivo/FuntouchOS) you may also need to turn on "Autostart" for Task Tracker in the system settings, or add it to the battery-optimization allow-list, otherwise the OS will kill background alarms aggressively. On stock Android and Samsung One UI this usually isn't needed.

## Testing notifications quickly

1. Add a task with a deadline **~2 minutes from now**.
2. Tick the "At deadline" reminder.
3. Save. Lock your phone.
4. When the alarm fires you should see the notification with **Mark done** and **Update status** actions.
5. Tap **Update status** — a small dialog appears over the lock screen or app you're in. Pick a status, add a note, save. Open the task detail — the update is in the timeline.

## Project layout

```
app/src/main/java/com/miraj/tasktracker/
├── TaskTrackerApp.kt          — Application, holds the repository
├── MainActivity.kt            — hosts the Compose nav graph
├── data/
│   ├── model/                 — Task, Assignor, Reminder, StatusUpdate, enums
│   ├── db/                    — Room database, DAOs, type converters
│   └── repository/            — TaskRepository, the one API the UI talks to
├── notification/
│   ├── NotificationHelper.kt  — channel + notification builder
│   ├── ReminderScheduler.kt   — AlarmManager wrapper
│   ├── ReminderReceiver.kt    — receives alarms, posts notifications
│   ├── MarkDoneReceiver.kt    — handles the "Mark done" action
│   ├── BootReceiver.kt        — reschedules alarms after reboot
│   └── OverdueNudgeWorker.kt  — periodic overdue check
└── ui/
    ├── theme/                 — colors, typography, MaterialTheme wrapper
    ├── nav/                   — Compose navigation graph
    ├── home/                  — home screen + category board
    ├── edit/                  — add/edit task form
    ├── detail/                — task detail + timeline
    ├── settings/              — manage assignors
    ├── statusupdate/          — transparent activity for notification action
    └── common/                — shared composables (badges, chips)
```

## Known parked items (not in v1)

- Recurring tasks (daily/weekly/monthly)
- Sub-tasks / checklists inside a task
- Backup and restore, export to CSV
- Widget for the home screen
- Dark theme
- Cross-device sync (needs a backend)

If you want any of these added, open the source and search for the "parked" comments — most have hook points already set up.

## Troubleshooting

**Gradle sync fails on first open.** Check that Android Studio's embedded JDK is version 17 (`File → Settings → Build → Gradle → Gradle JDK`).

**Notifications don't appear on Xiaomi/Oppo/Vivo phones.** Turn on Autostart and disable battery optimization for the app in the phone's system settings. This is a manufacturer restriction, not an app bug.

**"Reminder didn't fire at the exact time."** Doze mode can delay non-exact alarms. The app uses `setExactAndAllowWhileIdle` which is designed to fire even during Doze; if it still slips, check that battery optimization is off for the app.

**"App crashed on launch."** Check Logcat in Android Studio (`View → Tool Windows → Logcat`) filtered by `com.miraj.tasktracker` — send me the stack trace.
