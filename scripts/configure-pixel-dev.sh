#!/usr/bin/env bash
# Configure attached Pixel for AeroDue dev: adb root (when available) + Google WebView Dev.
set -euo pipefail

WEBVIEW_DEV="com.google.android.webview.dev"
WEBVIEW_STABLE="com.google.android.webview"
VANADIUM_WEBVIEW="app.vanadium.webview"

echo "==> Connected devices"
adb devices -l

if ! adb get-state >/dev/null 2>&1; then
  echo "No device ready. Enable USB debugging and authorize this host." >&2
  exit 1
fi

echo ""
echo "==> Device"
adb shell getprop ro.product.model
adb shell getprop ro.build.display.id
adb shell getprop ro.build.type

echo ""
echo "==> ADB root (requires userdebug/eng, unlocked bootloader, or OS 'Root for ADB')"
if adb root 2>&1; then
  sleep 2
  adb wait-for-device
  adb shell id
  if adb remount 2>&1; then
    echo "remount: ok"
  fi
else
  echo "adb root not available on this build (expected on production/GrapheneOS)."
  echo "GrapheneOS: enable 'Root access for ADB' in Developer options if your build supports it."
fi

echo ""
echo "==> WebView state (before)"
adb shell dumpsys webviewupdate 2>/dev/null | head -12 || true

echo ""
echo "==> Ensure Android System WebView Dev is installed"
if ! adb shell pm path "$WEBVIEW_DEV" >/dev/null 2>&1; then
  echo "Install WebView Dev from Play Store or APK, then re-run this script." >&2
  exit 1
fi
adb shell pm enable "$WEBVIEW_DEV" 2>/dev/null || true

if adb shell dumpsys webviewupdate 2>/dev/null | grep -q "$VANADIUM_WEBVIEW"; then
  echo ""
  echo "NOTE: GrapheneOS uses Vanadium as the only whitelisted WebView provider."
  echo "      adb cannot switch to $WEBVIEW_DEV without changing OS policy."
  echo "      Options:"
  echo "        1) Settings → System → Developer options → WebView implementation (if listed)"
  echo "        2) Use stock Pixel OS for Google WebView Dev testing"
  echo "        3) AeroDue still uses system WebView (Vanadium) with remote debugging enabled"
else
  echo ""
  echo "==> Switch system WebView to Dev channel"
  if adb shell cmd webviewupdate set-webview-implementation "$WEBVIEW_DEV" 2>&1; then
    echo "Switched to $WEBVIEW_DEV"
  else
    echo "Switch failed; trying stable provider $WEBVIEW_STABLE"
    adb shell cmd webviewupdate set-webview-implementation "$WEBVIEW_STABLE" 2>&1 || true
  fi
fi

echo ""
echo "==> WebView state (after)"
adb shell dumpsys webviewupdate 2>/dev/null | head -12 || true

echo ""
echo "==> Launch WebView DevTools (inspect in-app WebViews)"
adb shell am start -a "com.android.webview.SHOW_DEV_UI" 2>/dev/null || true

echo ""
echo "==> Install / refresh debug APK"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APK="$ROOT_DIR/android/app/build/outputs/apk/debug/app-debug.apk"
if [[ -f "$APK" ]]; then
  adb install -r "$APK"
else
  echo "APK not found ($APK). Run: cd android && ./gradlew :app:assembleDebug"
fi

echo "Done."
