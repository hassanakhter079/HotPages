#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SDK_DIR="$HOME/android-sdk"
ANDROID_JAR="$SDK_DIR/android-34/android.jar"
KEYSTORE="$SDK_DIR/debug.keystore"
OUT_APK="$PROJECT_DIR/build/HotPages.apk"
DOWNLOAD_APK="/sdcard/Download/HotPages.apk"

cd "$PROJECT_DIR"
rm -rf build gen && mkdir -p build/obj build/apk gen

echo "1. Generating R.java..."
aapt package -f -m -J gen \
    -M app/src/main/AndroidManifest.xml \
    -S app/src/main/res \
    -I "$ANDROID_JAR"

echo "2. Compiling Java sources..."
javac -source 17 -target 17 -cp "$ANDROID_JAR" \
    -d build/obj $(find app/src/main/java gen -name "*.java")

echo "3. Converting bytecode to DEX..."
d8 --release --min-api 24 --lib "$ANDROID_JAR" \
    --output build/apk $(find build/obj -name "*.class")

echo "4. Packaging resources and assets..."
aapt package -f \
    -M app/src/main/AndroidManifest.xml \
    -S app/src/main/res \
    -A app/src/main/assets \
    -I "$ANDROID_JAR" \
    -F build/unaligned.apk

(cd build/apk && aapt add ../unaligned.apk classes.dex >/dev/null)

echo "5. Aligning APK..."
zipalign -f -p 4 build/unaligned.apk build/aligned.apk

echo "6. Signing APK..."
apksigner sign --ks "$KEYSTORE" --ks-pass pass:android --key-pass pass:android \
    --out "$OUT_APK" build/aligned.apk

echo "7. Verifying APK signature..."
apksigner verify -v "$OUT_APK"

echo "8. Copying APK to /sdcard/Download and /sdcard/Download/HotPages..."
cp "$OUT_APK" "/sdcard/Download/HotPages.apk"
cp "$OUT_APK" "/sdcard/Download/PagesHub.apk"
if [ -d "/sdcard/Download/HotPages" ]; then
    cp "$OUT_APK" "/sdcard/Download/HotPages/HotPages.apk"
    cp "$OUT_APK" "/sdcard/Download/HotPages/PagesHub.apk"
fi

echo "Build complete! APK saved to:"
echo "  - $DOWNLOAD_APK ($(du -h "$DOWNLOAD_APK" | cut -f1))"
echo "  - /sdcard/Download/HotPages.apk"
if [ -d "/sdcard/Download/HotPages" ]; then
    echo "  - /sdcard/Download/HotPages/HotPages.apk"
fi
