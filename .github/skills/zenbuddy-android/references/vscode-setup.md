# VS Code Setup for Android (ZenBuddy) + Demo Guide

## 0. One-Command Project Setup (Start Here)

### Step A — Scaffold the entire codebase with Opus
In VS Code chat, type:
```
/scaffold-zenbuddy
```
Opus will generate every file (Gradle, Manifest, all Kotlin sources). Wait for it to finish.

### Step B — Bootstrap local Android SDK + Gradle (isolated, like venv)
```bash
chmod +x bootstrap.sh
./bootstrap.sh
```
This installs Android SDK into `.android-sdk/` and Gradle cache into `.gradle-home/` — **nothing goes to your home directory**.

### Step C — Activate the local environment in your terminal
```bash
source .env.local
```
Do this once per terminal session (or add to `.zshrc` for the project dir).

### Step D — Fill in your API keys
Copy and edit:
```bash
cp zenbuddy-app/local.properties.example zenbuddy-app/local.properties
# Edit local.properties — fill SUPABASE_URL, SUPABASE_ANON_KEY, GEMINI_API_KEY
```

### Step E — Build & run
```bash
emulator -avd ZenBuddy_Pixel8 &    # start the emulator created by bootstrap
cd zenbuddy-app
./gradlew installDebug             # first run downloads Gradle 8.11 into .gradle-home/
```

---

## 1. VS Code Extensions to Install

Install these once:

```bash
# Kotlin + Android
code --install-extension mathiasfrohlich.Kotlin
code --install-extension vscjava.vscode-java-pack
code --install-extension naco-siren.gradle-language
code --install-extension redhat.java

# Quality of life
code --install-extension usernamehw.errorlens
code --install-extension eamodio.gitlens
```

> **Note**: VS Code does not have the Android Layout Editor or full refactoring tools of Android Studio. Use it for Kotlin code editing + terminal build + GitHub Copilot coding. For XML-free Compose-only projects like ZenBuddy this works great.

## 2. Scaffold the Project (One-time, from Terminal)

If the project does not exist yet, create it with the Android CLI:

```bash
# Install Android command-line tools (if not installed)
# Download from: https://developer.android.com/studio#command-line-tools-only
# Then:
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" "emulator"

# Use Android Studio once just to generate the project skeleton, then open in VS Code
# OR use a Gradle-based template:
git clone https://github.com/android/nowinandroid.git zenbuddy
cd zenbuddy
code .
```

**Recommended: generate project in Android Studio, open folder in VS Code afterwards.**

## 3. Environment Variables

Add to `~/.zshrc` or `~/.bashrc`:
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

Reload: `source ~/.zshrc`

## 4. Building from VS Code Terminal

```bash
# Clean build
./gradlew clean assembleDebug

# Build + install on connected device / running emulator
./gradlew installDebug

# Run all unit tests
./gradlew test

# Run instrumented tests (emulator must be running)
./gradlew connectedAndroidTest

# Check for dependency updates
./gradlew dependencyUpdates
```

## 5. Running an Emulator from Terminal (no Android Studio)

```bash
# List available AVDs
emulator -list-avds

# Start an AVD (replace Pixel_8_API_35 with your AVD name)
emulator -avd Pixel_8_API_35 &

# Wait for device to boot, then install:
adb wait-for-device
./gradlew installDebug

# Open app
adb shell am start -n com.zenbuddy/.MainActivity
```

## 6. Connect a Physical Android Device

1. Enable **Developer Options**: Settings → About Phone → tap Build Number 7 times
2. Enable **USB Debugging** in Developer Options
3. Plug in via USB
4. Verify connection:
```bash
adb devices
# Should show:  emulator-5554   device
```
5. Install and run:
```bash
./gradlew installDebug && adb shell am start -n com.zenbuddy/.MainActivity
```

## 7. Live Development (Hot Reload Equivalent)

Android doesn't have Flutter hot reload, but use this workflow:

```bash
# Watch for changes and auto-build (keeps terminal running)
./gradlew installDebug --continuous
```

Or bind a VS Code task:

**`.vscode/tasks.json`**:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Build & Install Debug",
      "type": "shell",
      "command": "./gradlew installDebug",
      "group": { "kind": "build", "isDefault": true },
      "presentation": { "reveal": "always" }
    },
    {
      "label": "Run Tests",
      "type": "shell",
      "command": "./gradlew test",
      "group": { "kind": "test", "isDefault": true }
    }
  ]
}
```

Run with `Ctrl+Shift+B` (default build task) or `Ctrl+Shift+P` → "Tasks: Run Task".

## 8. Viewing Logs (Logcat)

```bash
# All logs
adb logcat

# Filter by app package only
adb logcat --pid=$(adb s\hell pidof -s com.zenbuddy)

# Filter by tag
adb logcat -s ZenBuddy:D
```

## 9. Demo Walkthrough: How to Demo ZenBuddy

### Prerequisites
- Emulator running OR physical device connected via ADB
- `local.properties` filled with real API keys (Supabase + Gemini)
- Build installed: `./gradlew installDebug`

### Demo Script

| Step | Action | What to Show |
|---|---|---|
| 1 | Open app | Splash / onboarding with ZenBuddy branding |
| 2 | Sign up with Supabase email | Auth flow, session persists |
| 3 | Log a mood (score 3/10) | Emoji slider, saved to Room instantly |
| 4 | Write a short journal entry | Text or voice-to-text input |
| 5 | Open AI Chat | Type "I'm feeling tired and overwhelmed" |
| 6 | Watch Gemini stream response | Token-by-token typing animation |
| 7 | Navigate to Quests | Show 3 AI-generated micro-tasks |
| 8 | Complete a quest | Checkbox → gamification animation |
| 9 | Turn on airplane mode | App still works (Room local-first) |
| 10 | Turn off airplane mode | WorkManager syncs to Supabase in background |

### Demo Tips
- Keep `adb logcat -s ZenBuddy` running in a second terminal to show live logs
- Use `adb shell settings put global animator_duration_scale 0.5` for faster animations during demo
- Screenshot: `adb exec-out screencap -p > demo.png`
- Record screen: `adb shell screenrecord /sdcard/demo.mp4` (max 3 min, Ctrl+C to stop, then `adb pull /sdcard/demo.mp4`)

## 10. Troubleshooting

| Problem | Fix |
|---|---|
| `adb: no devices` | Check USB cable, re-enable USB debugging, `adb kill-server && adb start-server` |
| Gradle build fails | `./gradlew --stacktrace assembleDebug` for full error |
| Room schema mismatch | Increment `version` in `@Database` and add Migration |
| Gemini 429 error | Quota exceeded — wait or use a different API key |
| Supabase auth fails | Check `SUPABASE_URL` and anon key in `local.properties` |
