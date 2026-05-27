# Manual Build Script for Android App without Gradle

$SDK_ROOT = "C:\Users\rk107\AppData\Local\Android\Sdk"
$BUILD_TOOLS = "$SDK_ROOT\build-tools\37.0.0"
$PLATFORM_JAR = "$SDK_ROOT\platforms\android-36.1\android.jar"

$AAPT2 = "$BUILD_TOOLS\aapt2.exe"
$D8 = "$BUILD_TOOLS\d8.bat"
$APKSIGNER = "$BUILD_TOOLS\apksigner.bat"
$ZIPALIGN = "$BUILD_TOOLS\zipalign.exe"

# 1. Clean and Create build directory
Remove-Item -Recurse -Force build -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path build/obj -Force
New-Item -ItemType Directory -Path build/gen -Force

Write-Host "--- Compiling Resources ---"
& $AAPT2 compile --dir res -o build/res.zip

Write-Host "--- Linking Resources and Generating R.java ---"
& $AAPT2 link --manifest AndroidManifest.xml -I $PLATFORM_JAR -o build/hello_unsigned.apk build/res.zip --java build/gen --min-sdk-version 24 --target-sdk-version 34

Write-Host "--- Compiling Java Source ---"
# Note: Using build/gen for R.java
javac -cp $PLATFORM_JAR -d build/obj src/com/example/hello/MainActivity.java build/gen/com/example/hello/R.java

Write-Host "--- Converting to DEX ---"
& $D8 build/obj/com/example/hello/*.class --lib $PLATFORM_JAR --output build/

Write-Host "--- Adding DEX to APK ---"
# Using PowerShell to add classes.dex to the zip (apk)
$apkPath = Resolve-Path "build/hello_unsigned.apk"
$dexPath = Resolve-Path "build/classes.dex"

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($apkPath.Path, "Update")
[System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $dexPath.Path, "classes.dex")
$zip.Dispose()

Write-Host "--- Aligning APK ---"
& $ZIPALIGN -f 4 build/hello_unsigned.apk build/hello_aligned.apk

Write-Host "--- Signing APK ---"
Write-Host "Note: This uses a debug keystore if available, or you might need to generate one."
# If you don't have a debug keystore, you can generate one with keytool:
# keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000
$DEBUG_KEYSTORE = "$env:USERPROFILE\.android\debug.keystore"

if (Test-Path $DEBUG_KEYSTORE) {
    & $APKSIGNER sign --ks $DEBUG_KEYSTORE --ks-pass pass:android --key-pass pass:android --out build/hello_signed.apk build/hello_aligned.apk
    Write-Host "Success! APK created at build/hello_signed.apk"
} else {
    Write-Host "No debug.keystore found at $DEBUG_KEYSTORE. Please create one to sign the APK."
}
