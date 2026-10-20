import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import electron from "vite-plugin-electron/simple";
import path from "path";
import pkg from "./package.json" with { type: "json" };

export default defineConfig(({ command, mode }) => {
  const isAndroid = mode === "android";
  if (command === "serve") {
    const devSigningSecret = loadEnv(mode, process.cwd(), "LINER_").LINER_DEV_SIGNING_SECRET;
    if (devSigningSecret) process.env.LINER_DEV_SIGNING_SECRET = devSigningSecret;
  }

  return {
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version),
  },
  plugins: [
    react(),
    tailwindcss(),
    ...(isAndroid ? [] : [
      electron({
      main: {
        entry: "electron/main.ts",
      },
      preload: {
        input: "electron/preload.ts",
        vite: {
          build: {
            rollupOptions: {
              output: {
                format: "cjs",
                entryFileNames: "preload.cjs",
              },
            },
          },
        },
      },
      }),
    ]),
  ],
  clearScreen: false,
  resolve: {
    alias: {
      "@": path.resolve(import.meta.dirname, "./src"),
      "next/navigation": path.resolve(import.meta.dirname, "./src/lib/navigation.tsx"),
      "next/link": path.resolve(import.meta.dirname, "./src/lib/navigation.tsx"),
      "next/image": path.resolve(import.meta.dirname, "./src/components/ui/AppImage.tsx"),
    },
  },
  server: {
    port: 5173,
    strictPort: true,
  },
  build: {
    outDir: "dist",
    emptyOutDir: true,
    // Capacitor serves the built SPA from its local WebView.
    ...(isAndroid ? { sourcemap: false } : {}),
  },
  };
});
