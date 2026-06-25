# Installing Wattt! ⚡

There are two ways to get Wattt! on your device:

1. [Download a prebuilt APK](#option-1-download-the-apk-recommended) (easiest)
2. [Build from source](#option-2-build-from-source)

---

## Requirements

- A phone running **Android 12 (API 31)** or newer.
- For building: **JDK 17** and **Android Studio** (or the Android SDK + Gradle).

---

## Option 1: Download the APK (recommended)

Each tagged release publishes a signed APK on the **GitHub Releases** page.

1. Go to **[Releases](https://github.com/imrmnabil/Wattt/releases)**.
2. Download the latest `Wattt-vX.Y.Z.apk` under **Assets**.
3. Transfer it to your phone (or download it directly on the phone).
4. Open the APK. Android will ask permission to install apps from this source:
   - Tap **Settings** on the prompt, enable **Allow from this source**, then go back and tap **Install**.
5. Open **Wattt!** and follow the on-screen calibration steps.

> ⚠️ Because the APK is self-signed (not distributed via the Play Store), Android may warn that the app is from an "unknown developer." This is expected for sideloaded apps.

---

## Option 2: Build from source

### 1. Clone the repository

```bash
git clone https://github.com/imrmnabil/Wattt.git
cd Wattt
```

### 2. Build a debug APK

The debug build needs no signing setup:

```bash
./gradlew assembleDebug
```

The APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

### 3. Install on a connected device

With a device connected over USB (USB debugging enabled) or an emulator running:

```bash
./gradlew installDebug
```

Or install the APK manually:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Build with Android Studio

1. **File → Open** and select the project folder.
2. Let Gradle sync finish.
3. Select your device/emulator and press **Run ▶**.

---

## Building a signed release (maintainers)

Release builds are signed via a keystore supplied through environment variables. The GitHub Actions workflow (`.github/workflows/release.yml`) builds and publishes a signed APK automatically whenever a tag matching `v*` is pushed:

```bash
git tag v1.0.0
git push origin v1.0.0
```

To build a signed release locally, set these environment variables before running `./gradlew assembleRelease`:

| Variable | Description |
|---|---|
| `KEYSTORE_PATH` | Path to your `.jks` keystore file |
| `STORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

```bash
KEYSTORE_PATH=/path/to/keystore.jks \
STORE_PASSWORD=*** \
KEY_ALIAS=*** \
KEY_PASSWORD=*** \
./gradlew assembleRelease
```

The signed APK is written to:

```
app/build/outputs/apk/release/app-release.apk
```

> 🔒 Never commit keystore files or passwords to the repository.

---

## Troubleshooting

- **App shows `N/A` while charging** — unplug the charger for at least 5 seconds to let it calibrate, then plug back in.
- **Wattage seems off** — close heavy background apps during the unplugged calibration phase for a more accurate system-consumption reading.
- **`adb: command not found`** — install Android platform-tools and ensure `adb` is on your `PATH`.
