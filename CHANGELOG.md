# Changelog

All notable changes to the Android version of Liner will be documented in this file.

## [1.0.5-vc10] — 2026-10-22
### Added
- Restored shuffle and repeat/repeat-one buttons in the bottom control bar of the mobile fullscreen player.

### Changed
- Aligned the lyrics source selector pill horizontally and vertically with the top-left close/down arrow in `FullscreenPlayer.tsx`.

---

## [1.0.5-vc9] — 2026-10-22
### Fixed
- Fixed status bar header overlap on Android by adding top safe area padding to the main scroll container in `AppFrame.tsx`.
- Removed the desktop-only "Miniplayer" glassmorphism customization tab option in Settings -> Customization on mobile.

---

## [1.0.5-vc8] — 2026-10-22
### Added
- Enabled full-screen edge-to-edge display mode on Android with transparent system bars and automatic safe area insets (`env(safe-area-inset-top)` / `env(safe-area-inset-bottom)`).

---

## [1.0.5-vc7] — 2026-10-22
### Changed
- Streamlined the Android bottom music mini-player panel to display only the play/pause button (right edge) and the like (heart) button (with fixed left offset from play/pause), independent of track title length.
- Added strict text truncation with ellipses (`...`) for long track titles and artist names on the mini-player panel while preserving all background scrubbing and playback mechanics.

---

## [1.0.4] — 2026-10-21
### Added
- Initial background playback support via Android foreground service and `MediaSession` integration.
- Swiped-away notification handling to keep notifications gone until playback resumes.
- Stop media service and clear active notification when the app is swiped away from recent tasks.

---

## [1.0.3-patch] — 2026-10-20
### Fixed
- Handled YouTube playback directly without WebView CORS limitations on Android.

### Added
- Initial Capacitor Android application port, native Android build configuration, app icon assets, and foundational mobile UI adaptations.

---

## Android APK Builds Reference (`artifacts/`)

| Filename | Version | VersionCode | Description |
| :--- | :--- | :--- | :--- |
| `Liner-Android-debug-1.0.5-vc10.apk` | 1.0.5 | 10 | Current build with aligned lyrics header and restored shuffle/repeat controls in fullscreen player. |
| `Liner-Android-debug-1.0.5-vc9.apk` | 1.0.5 | 9 | Status bar overlap fixes and mobile customization tab cleanup. |
| `Liner-Android-debug-1.0.5-vc8.apk` | 1.0.5 | 8 | Full-screen edge-to-edge mode implementation. |
| `Liner-Android-v1.0.5-debug-vc7.apk` | 1.0.5 | 7 | Minimized mini-player UI redesign with strict truncation and clean layout. |
| `Liner-Android-v1.0.5-debug.apk` | 1.0.5 | 6 | Base 1.0.5 debug build. |
| `Liner-Android-v1.0.4-debug.apk` | 1.0.4 | 5 | Background playback, media controls update, and swipe-away cleanup. |
| `Liner-Android-v1.0.3-patch-debug.apk` | 1.0.3 | 4 | YouTube CORS playback fixes for Android. |
| `Liner-Android-v1.0.3-debug.apk` | 1.0.3 | 3 | Initial Capacitor port. |
