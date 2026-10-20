<div align="center">

# Liner

**Modern desktop music streaming player with syllable-synced lyrics and reactive dynamic visuals.**

[![Electron](https://img.shields.io/badge/Electron-44.0-47848F?style=flat-square&logo=electron&logoColor=black&labelColor=white)](https://electronjs.org)
[![React 19](https://img.shields.io/badge/React_19-19.2-61DAFB?style=flat-square&logo=react&logoColor=black&labelColor=white)](https://react.dev)
[![TypeScript 5](https://img.shields.io/badge/TypeScript_5-5.0-3178C6?style=flat-square&logo=typescript&logoColor=black&labelColor=white)](https://www.typescriptlang.org)
[![Tailwind CSS v4](https://img.shields.io/badge/Tailwind_CSS_v4-v4.0-06B6D4?style=flat-square&logo=tailwindcss&logoColor=black&labelColor=white)](https://tailwindcss.com)
[![WebAssembly](https://img.shields.io/badge/Rust-WebAssembly-654FF0?style=flat-square&logo=webassembly&logoColor=black&labelColor=white)](https://webassembly.org)
[![License: Source-Available](https://img.shields.io/badge/License-Source--Available-blueviolet?style=flat-square&labelColor=white)](./LICENSE)

[Preview](#preview) •
[Architecture](#architecture) •
[Getting Started](#getting-started) •
[Building](#building) •
[Contributing](#contributing) •
[License](#license)

<br/>

<img src="./.github/assets/lyrics_preview.png" alt="Liner Fullscreen Lyrics" width="100%" />

</div>

---

## Preview

<div align="center">
  <table>
    <tr>
      <td align="center" width="50%">
        <img src="./.github/assets/home.png" alt="Liner Home Page" />
        <br/>
        <sub><b>Home</b></sub>
      </td>
      <td align="center" width="50%">
        <img src="./.github/assets/demo.gif" alt="Liner Fullscreen Mode Demo" />
        <br/>
        <sub><b>Fullscreen mode</b></sub>
      </td>
    </tr>
  </table>
</div>

---

> [!NOTE]
> Liner client connects to the official Liner API backend for music catalog, streaming playback, and synchronized lyrics.

---

## Architecture

```
liner/desktop/
├── electron/
│   ├── main.ts              # Electron main process & IPC handlers
│   ├── preload.ts           # Context bridge & secure renderer bindings
│   ├── crypto.ts            # High-performance in-memory WebAssembly signer
│   └── signer.ts            # Embedded WebAssembly binary
├── src/
│   ├── assets/              # Static assets and media
│   ├── components/          # Shared reusable UI components
│   ├── features/            # Feature modules (player, lyrics, queue, library, search, auth)
│   ├── pages/               # Route views (Home, Search, Album, Artist, Playlist)
│   ├── shared/              # API client, state stores (Zustand), and data contracts
│   ├── App.tsx              # Application root
│   └── main.tsx             # Vite entry point
├── package.json
└── vite.config.ts
```

---

## Getting Started

### Prerequisites

- **Node.js** `v20+` and **pnpm** or **Bun**
- Modern OS (Windows 10/11, macOS, or Linux)

---

### Setup

```bash
# 1. Clone repository
git clone https://github.com/tryliner/desktop.git
cd desktop

# 2. Install dependencies (pnpm or Bun)
pnpm install
# or
bun install
```

---

### Running in Development

```bash
# Start Liner in development mode (Electron app with Vite hot-reload)
pnpm run dev
# or
bun run dev
```

---

## Testing

```bash
# Run test suite with Vitest
bun run test
# or
pnpm test
```

---

## Building

```bash
# Build desktop packages for your current OS:

# Linux (AppImage & .deb)
bun run electron:build:linux

# Windows (.exe installer)
bun run electron:build:win
```

Compiled distributions will be placed in the `release/` directory.

### Android port (in progress)

The renderer can be packaged in a Capacitor Android WebView. Install dependencies,
create the native project once, then sync the built web assets:

```bash
pnpm install
pnpm exec cap add android
pnpm android:sync
pnpm android:open
```

`pnpm build:android:web` builds the React app without starting the Electron
main/preload bundles. The existing catalog, library, auth screens, and HTML audio
player are still shared with desktop. This is an early test build, not a
production release. Android API signing and basic mobile navigation are
implemented. OAuth/deep-link return handling, encrypted token storage, and a
foreground media service with notification controls remain to be implemented.
Login tokens currently use WebView local storage. The signing key is obfuscated
in native app code like the existing desktop version; replace it with a
server-issued mobile flow before public distribution.

To install the debug APK on a phone, copy `artifacts/Liner-Android-debug.apk` to
the device and open it, allowing installation from that file source if Android
asks. From a connected computer with USB debugging enabled, run:

```bash
adb install -r artifacts/Liner-Android-debug.apk
```

The current native player uses the WebView audio element. It can play while the
app is open, but playback may stop when Android suspends the app; background
playback and lock-screen media controls are not included in this first APK.

---

## Contributing

Bug fixes, small features, and docs fixes are welcome. Read [CONTRIBUTING.md](./CONTRIBUTING.md) before you open a pull request. The [license](./LICENSE) limits what a fork is allowed to do.

---

## Special Thanks

- [`@braccato/core`](https://github.com/better-lyrics/braccato) & `@braccato/parsers` — Synced lyrics components and format parsers.
- [`@kawarp/react`](https://github.com/better-lyrics/kawarp) & `@kawarp/core` — WebGL background shader components.
- [Solar Icons](https://solar-icons.com) & [MingCute Icons](https://www.mingcute.com) — Icon collections.

---

## License

Liner is distributed under the **Liner Source-Available & Non-Commercial Contribution License**.

- **Allowed**: Personal non-commercial usage, local builds, and upstream contributions.
- **Prohibited**: Commercial distribution, public competing forks, or extracting core algorithms for third-party products.

See [LICENSE](./LICENSE) for details.

