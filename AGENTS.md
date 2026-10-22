# AGENTS.md

This file gives coding agents (Claude Code, Codex CLI, and others) the context they need to work safely on this repository. Read it fully before making changes.

## 1. Project overview

Liner is a music player that pulls music and playlists from other platforms (for example YouTube) into one app, with synced lyrics.

This repository is a fork of the upstream desktop project `tryliner/desktop`. The fork adds an **Android version** built with Capacitor. The Android app is the main development focus of this fork. The desktop app must keep building and working.

- Product website: https://tryliner.fun
- Android package id (applicationId): `fun.tryliner.music`
- Desktop: Electron shell + React/TypeScript/Vite UI. Windows and Linux.
- Android: Capacitor shell (WebView) + native Java code in `android/`.

## 2. Repository layout

- `src/`: React + TypeScript UI, shared between desktop and Android. Most logic lives here.
- `src/features/player/engine/playerRuntime.ts`: player logic running inside the WebView; bridge to native media controls on Android.
- `electron/`: Electron main/preload code for the desktop app. Do not change unless the task is explicitly about desktop.
- `android/`: Capacitor Android project (Gradle). Contains Java code: a foreground service for playback and a `MediaSession` for the system media card, notification, and lock screen controls.
- `capacitor.config.ts`: Capacitor configuration (app id, web dir, plugins).
- `scripts/`: currently contains `register-deeplink-linux.sh` and `verify-native-addon.cjs`; package/build commands are defined in `package.json`.
- `signer/`: signing-related tooling. Treat as sensitive; do not modify signing keys or change how they are used without an explicit request.
- `build/`: desktop icon assets (`icon.ico`, `icon.png`, `icon.svg`, and PNG sizes in `build/icons/`).
- `public/`: static assets served by the web build.
- `.github/`: CI workflows. The `ci(release)` commits touch these. Do not change release workflows without an explicit request.
- `no-public/`: private owner materials, screenshots, and personal files. Never read it for a task unless the owner names a file from it, never commit it, never copy its contents into code, docs, or commit messages.
- `artifacts/`: local APK copies and other build outputs. Git-ignored.

## 3. Branches and upstream sync

- `main`: contains the original project history plus Android patches. Keep it as the base that mirrors upstream, plus fork-specific commits.
- `android`: the working branch for all Android development. Create new feature work from `android`.
- Never rewrite published history: no `git push --force` on `main` or `android`, no `git rebase` of commits already pushed, no `git reset --hard` that drops committed work.
- Never delete Android commits from `main`.
- Upstream sync: once the upstream remote is configured, pull upstream changes into `main` first, then merge `main` into `android`. Resolve conflicts carefully, especially in shared React UI files that also have Android branches (see section 5).
- Before a large merge or sync, create a backup branch, for example `git branch backup-before-sync-<date>`.

Before starting any task, run:

```bash
git status
git branch --show-current
```

If the working tree has uncommitted changes that you did not make, stop and ask the owner before doing anything else.

## 4. Working style for agents

- Work in small steps. One task per session: one screen, one bug, one feature.
- Before editing, read the relevant files. Do not guess the structure.
- Commit after each working step with a clear message. Use conventional prefixes like `feat(android):`, `fix(android):`, `docs:`, `chore:`, matching the existing history.
- Maintain and update `CHANGELOG.md` with notable changes and APK versions whenever features or fixes are added.
- Do not add dependencies without a clear reason. If you add one, say why in the commit message and the final report.
- Do not do large refactors, reformatting, or renaming unless the task asks for it. Large diffs in shared files make upstream merges painful.
- If requirements are unclear, ask one focused question instead of guessing.
- Explain your changes to the owner in plain language at the end: what changed, which files, why.
- Be honest about verification. Say exactly which commands ran and passed, and what was not checked.

## 5. Platform rules (desktop vs Android)

This is the most important part of the project. Mobile adaptations must never break the desktop app.

- Detect native Android with `Capacitor.isNativePlatform()` from `@capacitor/core`, following existing usage.
- Keep desktop behavior unchanged. If a change affects both platforms, make sure the desktop path is still the default and still works.
- Android-specific UI and runtime branches currently appear in `src/features/navigation/ui/AppFrame.tsx`, `src/features/player/ui/MiniPlayer.tsx`, `src/features/player/ui/FullscreenPlayer.tsx`, `src/features/library/ui/PlaylistPage.tsx`, `src/features/settings/ui/SettingsModal.tsx`, `src/features/player/engine/playerRuntime.ts`, `src/shared/api/requestSigner.ts`, and `src/shared/media/innertube/innertubeAudio.ts`. Search for `Capacitor.isNativePlatform()` before changing platform behavior; the list may evolve.
- Shared React components are a merge risk. Keep Android branches small and clearly separated. Prefer adding a small component or hook over rewriting a shared one.
- Do not change Electron code (`electron/`) for Android work. Do not change Capacitor-specific code for desktop work.
- Do not remove or rename existing exports used by desktop code.

## 6. Android media and playback

- Playback runs in the WebView. The native layer (Java foreground service + `MediaSession`) must reflect the real player state.
- System media controls (notification, lock screen, media card) must be updated from the actual state of the player: track metadata, cover art, playing/paused, position, duration. Do not set metadata from guesses or cached values that may be stale.
- Commands from the notification or media card (play, pause, next, previous, seek) must be forwarded to the WebView, where the player logic runs. Do not implement playback logic separately in Java.
- The foreground service must keep playback alive when the app is in the background. Changes to the service lifecycle need explicit testing on a device.
- YouTube playback on Android has had specific fixes (commit `54cc22e`, "handle YouTube playback without WebView"). Read that commit before changing YouTube playback code.
- Test media changes on a real device when possible. Emulators do not reproduce notification and background behavior reliably.

## 7. Build and verification

Use these commands, in this order, when you change the Android app. Run only the steps that are relevant to your change, but do not skip the final APK build when Android code or web code changed.

Package manager: **pnpm**. Do not use npm or yarn in this repository unless the owner asks. Do not create `package-lock.json` or `yarn.lock`.

1. Install dependencies if `package.json` or the lockfile changed:
```bash
   pnpm install
```
2. Build the web app for Android:
```bash
   pnpm build:android:web
```
3. Sync the web build into the Capacitor Android project:
```bash
   pnpm exec cap sync android
```
4. Build the debug APK:
```bash
   cd android
   ./gradlew assembleDebug
   cd ..
```
   Output: `android/app/build/outputs/apk/debug/app-debug.apk`.

Environment requirements for step 4:
- `JAVA_HOME` must point to a JDK 21 installation.
- `ANDROID_HOME` must point to the Android SDK.
- Do not hardcode these paths in the repository (no absolute paths in `build.gradle`, scripts, or docs). Read them from the environment. If they are missing, tell the owner which variable is missing instead of guessing a path.

Desktop verification: `pnpm build` runs `tsc -b && vite build` and is the shared web/type build. For a packaged desktop app, use the matching `pnpm electron:build`, `pnpm electron:build:linux`, `pnpm electron:build:win`, or `pnpm electron:build:mac` command.

Tests: `pnpm test` runs `vitest run`; the repository currently contains about 90 `*.test.*`/`*.spec.*` files. Run the relevant tests when behavior changes and report exactly what passed.

Lint: there is currently no lint script in `package.json`. TypeScript checking is included in `pnpm build` and `pnpm build:android:web` (`tsc -b`). Do not invent a lint command.

Do not claim the Android app is working unless the APK built successfully. Do not claim it works on a device unless it was installed and tested on one. Report what was actually run.

## 8. APK releases and versioning

- The current Android version is declared in `android/app/build.gradle` (`versionCode 4`, `versionName "1.0.3"` at the time this file was checked). Re-read that file before each release; never rely on these values remaining current.
- Any APK meant for installation over an existing build must have a higher `versionCode`. Increase `versionCode` by 1 for each new installable build. Update `versionName` when the owner asks for a new user-visible version.
- Keep the `applicationId` (`fun.tryliner.music`) unchanged. Changing it creates a separate app on the phone.
- Use the same debug signing key for every build. Android refuses to install an update signed with a different key. Do not regenerate, replace, or commit keystores.
- Debug signing certificate SHA-256 (use it to verify that an APK was signed with the expected key): `ec4f3393d61eb054fd129448a0827e0003a5f5d7644fa77d4f8df220626318ec`.
- Do not commit APK files. Local copies for transfer go to `artifacts/Liner-Android-debug.apk`, which is git-ignored.

Install on a connected device (requires `adb` and USB debugging):

```bash
adb devices -l
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

`-r` replaces the existing app and keeps its data. Installing over a build with a different signature or a lower `versionCode` will fail; if that happens, report it to the owner instead of uninstalling the app, because uninstalling deletes local app data.

## 9. Git hygiene and secrets

Never commit:
- APK, AAB, or other build outputs (`artifacts/`, `android/app/build/`, `dist/`, `build/` output folders, where applicable);
- keystores, signing keys, passwords, tokens, `.env` files (only `.env.example` is allowed);
- anything under `no-public/`;
- `node_modules/`, IDE files, local SDK paths (`local.properties`).

Before every commit, check what is staged:

```bash
git status
git diff --staged --stat
```

If a sensitive file shows up, unstage it and add it to `.gitignore` if it is not already covered. Do not commit it "temporarily".

Commit messages must not include secrets, private file names from `no-public/`, or personal data.

## 10. Environment notes

- The owner works on Arch Linux, KDE Plasma 6 on Wayland, with the fish shell.
- In fish, do not use the bash syntax `VAR=value command`. Use `set -x VAR value` first, then run the command. For example:
```fish
  set -x JAVA_HOME /usr/lib/jvm/java-21-openjdk
  set -x ANDROID_HOME /path/to/Android/Sdk
  cd android; ./gradlew assembleDebug; cd ..
```
- When writing scripts or documentation meant to be run by others, use POSIX-compatible `bash` syntax and mention the shell explicitly.
- Java 21 comes from the `jdk21-openjdk` package on Arch. Android tooling needs `android-tools` for `adb`.
- Do not write absolute paths from the owner's machine into any committed file. Machine-specific values belong in local, git-ignored files.

## 11. Known gaps and TODOs

- The upstream project is `tryliner/desktop`; no upstream Git remote is currently configured. Do not add a guessed remote URL; confirm the canonical URL before configuring it.
- Desktop packaging commands are listed above and in `package.json`; there is no separate release automation script in `scripts/`.
- Android release (signed, non-debug) build and Play Store process: not set up.

When you find something in this file that is wrong or outdated, fix it in the same commit as your change, or tell the owner about it.
