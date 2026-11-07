# HotPages ⚡

A lightning-fast, minimalist Android application and WebView container (~29 KB APK) for exploring, previewing, and editing GitHub Pages apps and standalone HTML tools.

---

## 📱 Features

- **GitHub Pages Explorer**: Browse and launch your GitHub Pages and standalone HTML apps with one tap.
- **In-App Code Editor**: View source, edit code with line numbers, and commit modifications directly back to GitHub branches via the GitHub REST API.
- **Native Android Integration**:
  - Immersive `#0b2e2b` system status bar color coordination.
  - Native hardware back button interception (`window.phBack`) for navigation inside embedded apps and editor modals.
  - External links open cleanly in system browser via Android Intents.
  - Full DOM storage and database persistence enabled.
- **Zero Overhead**: Ultra-compact APK footprint (~29 KB) with zero heavy third-party dependencies.
- **On-Device Compilation**: Fully reproducible build toolchain executable directly on Android via Termux.

---

## 📥 Download

Pre-compiled and signed release APKs are available on the [Releases](https://github.com/hassanakhter079/HotPages/releases) page:
- **[HotPages.apk](https://github.com/hassanakhter079/HotPages/releases/latest/download/HotPages.apk)** (Direct APK download)

---

## 🛠️ Build On-Device (Termux)

You can build the APK directly on your Android phone using Termux without Gradle or Android Studio:

### Prerequisites
```bash
pkg install openjdk-21 aapt apksigner
```

Ensure Android platform `android.jar` (API 34) and `d8` (from Android SDK build-tools or Android Command Line Tools) are located in `~/android-sdk/` along with a signing keystore (`debug.keystore`).

### Compilation
Run the automated build script:
```bash
./build.sh
```

The script will:
1. Generate `R.java` via `aapt`
2. Compile Java sources using `javac` (Java 17)
3. Convert bytecode to `classes.dex` using `d8`
4. Package web assets and Android resources
5. Align APK boundaries using `zipalign`
6. Sign and verify using `apksigner` (v2 & v3 signature schemes)

The resulting APK will be saved in `build/` and copied to `/sdcard/Download/HotPages.apk`.

---

## 📄 License

MIT License. Designed and maintained by [@hassanakhter079](https://github.com/hassanakhter079).
