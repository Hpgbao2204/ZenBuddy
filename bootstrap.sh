#!/usr/bin/env bash
# =============================================================================
# ZenBuddy Bootstrap Script
# Isolates Gradle + Android SDK inside the project folder (like Python venv).
# Usage:
#   chmod +x bootstrap.sh && ./bootstrap.sh
#   source .env.local   # activate the local SDK/Gradle env in current shell
# =============================================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SDK_DIR="$PROJECT_DIR/.android-sdk"
GRADLE_HOME="$PROJECT_DIR/.gradle-home"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
ENV_FILE="$PROJECT_DIR/.env.local"

# ── Colors ────────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC} $*"; }
success() { echo -e "${GREEN}[OK]  ${NC} $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $*"; }

# =============================================================================
# 1. Detect OS  (Linux / macOS)
# =============================================================================
OS="$(uname -s)"
if [[ "$OS" == "Darwin" ]]; then
    CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip"
elif [[ "$OS" != "Linux" ]]; then
    echo "Unsupported OS: $OS"; exit 1
fi

# =============================================================================
# 2. Install Android Command-Line Tools into .android-sdk/
# =============================================================================
if [[ ! -d "$SDK_DIR/cmdline-tools/latest/bin" ]]; then
    info "Downloading Android command-line tools..."
    mkdir -p "$SDK_DIR/cmdline-tools"
    TMP_ZIP="/tmp/cmdline-tools.zip"
    curl -fSL "$CMDLINE_TOOLS_URL" -o "$TMP_ZIP"
    unzip -q "$TMP_ZIP" -d "$SDK_DIR/cmdline-tools"
    # The zip extracts to cmdline-tools/, rename to latest/
    mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest" 2>/dev/null || true
    rm "$TMP_ZIP"
    success "Command-line tools installed at $SDK_DIR/cmdline-tools/latest"
else
    success "Command-line tools already present"
fi

export ANDROID_HOME="$SDK_DIR"
export PATH="$SDK_DIR/cmdline-tools/latest/bin:$SDK_DIR/platform-tools:$SDK_DIR/emulator:$PATH"

# =============================================================================
# 3. Accept licenses + install SDK packages
# =============================================================================
info "Accepting Android SDK licenses..."
yes | sdkmanager --sdk_root="$SDK_DIR" --licenses > /dev/null 2>&1 || true

info "Installing SDK packages (platform-tools, android-35, build-tools, emulator)..."
sdkmanager --sdk_root="$SDK_DIR" \
    "platform-tools" \
    "platforms;android-35" \
    "build-tools;35.0.0" \
    "emulator" \
    "system-images;android-35;google_apis_playstore;x86_64" > /dev/null
success "SDK packages installed"

# =============================================================================
# 4. Create a local AVD (emulator) if none exists
# =============================================================================
if ! avdmanager list avd 2>/dev/null | grep -q "ZenBuddy_Pixel8"; then
    info "Creating emulator AVD: ZenBuddy_Pixel8 (Pixel 8, API 35)..."
    echo "no" | avdmanager create avd \
        --name "ZenBuddy_Pixel8" \
        --package "system-images;android-35;google_apis_playstore;x86_64" \
        --device "pixel_8" \
        --sdcard 512M 2>/dev/null
    success "AVD created: ZenBuddy_Pixel8"
else
    success "AVD ZenBuddy_Pixel8 already exists"
fi

# =============================================================================
# 5. Isolate Gradle → .gradle-home/ (like venv)
# =============================================================================
mkdir -p "$GRADLE_HOME"
success "Gradle home isolated at .gradle-home/"

# =============================================================================
# 6. Write local.properties for SDK path
# =============================================================================
LOCAL_PROPS="$PROJECT_DIR/zenbuddy-app/local.properties"
if [[ -f "$LOCAL_PROPS" ]]; then
    # Update sdk.dir line
    sed -i "s|^sdk.dir=.*|sdk.dir=$SDK_DIR|" "$LOCAL_PROPS"
else
    warn "zenbuddy-app/local.properties not found — run the scaffold prompt first, then re-run bootstrap."
fi

# =============================================================================
# 7. Write .env.local — source this to activate the local SDK in your shell
# =============================================================================
cat > "$ENV_FILE" <<EOF
# Source this file to use the local Android SDK + Gradle in your shell:
#   source .env.local
export ANDROID_HOME="$SDK_DIR"
export GRADLE_USER_HOME="$GRADLE_HOME"
export PATH="\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator:\$PATH"
EOF
success ".env.local written"

# =============================================================================
# 8. Activate Gradle wrapper (download if missing)
# =============================================================================
GRADLEW="$PROJECT_DIR/zenbuddy-app/gradlew"
if [[ -f "$GRADLEW" ]]; then
    info "Warming up Gradle wrapper (first run downloads Gradle 8.11 into .gradle-home)..."
    GRADLE_USER_HOME="$GRADLE_HOME" \
    ANDROID_HOME="$SDK_DIR" \
    bash "$GRADLEW" --project-dir "$PROJECT_DIR/zenbuddy-app" --no-daemon help --quiet 2>/dev/null
    success "Gradle wrapper ready"
else
    warn "gradlew not found in zenbuddy-app/ — generate the project first with the scaffold prompt."
fi

# =============================================================================
# Done
# =============================================================================
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN} ZenBuddy environment ready!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "  Activate env in current shell:"
echo "    source .env.local"
echo ""
echo "  Build & install:"
echo "    cd zenbuddy-app && ./gradlew installDebug"
echo ""
echo "  Start emulator:"
echo "    emulator -avd ZenBuddy_Pixel8 &"
echo ""
echo "  View logs:"
echo '    adb logcat --pid=$(adb shell pidof -s com.zenbuddy)'
echo ""
