import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "fun.tryliner.music",
  appName: "Liner",
  webDir: "dist",
  // Keep the app fully local; API and media requests use the normal network stack.
  android: {
    allowMixedContent: false,
  },
  plugins: {
    CapacitorHttp: { enabled: true },
  },
};

export default config;
