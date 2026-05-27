# Manual Build Script for VitaAI (No Gradle)

$SDK_ROOT = "C:\Users\rk107\AppData\Local\Android\Sdk"
$BUILD_TOOLS = "$SDK_ROOT\build-tools\37.0.0"
$PLATFORM_JAR = "$SDK_ROOT\platforms\android-36.1\android.jar"
$DEBUG_KEYSTORE = "$env:USERPROFILE\.android\debug.keystore"

$AAPT2 = "$BUILD_TOOLS\aapt2.exe"
$D8 = "$BUILD_TOOLS\d8.bat"
$APKSIGNER = "$BUILD_TOOLS\apksigner.bat"
$ZIPALIGN = "$BUILD_TOOLS\zipalign.exe"

# 1. Clean and Create build directory
Remove-Item -Recurse -Force build -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path build/obj -Force
New-Item -ItemType Directory -Path build/gen -Force

Write-Host "--- Compiling Resources ---"
# Creating an empty res folder if none exists to avoid aapt2 errors
if (-not (Test-Path res)) { New-Item -ItemType Directory -Path res }
& $AAPT2 compile --dir res -o build/res.zip

Write-Host "--- Linking Resources and Generating R.java ---"
& $AAPT2 link --manifest AndroidManifest.xml -I $PLATFORM_JAR -o build/vitaai_unsigned.apk build/res.zip --java build/gen --min-sdk-version 26 --target-sdk-version 34

Write-Host "--- Compiling Java Source ---"
$SOURCES = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
$R_JAVA = "build/gen/com/example/vitaai/R.java"
javac -cp $PLATFORM_JAR -d build/obj $SOURCES $R_JAVA

Write-Host "--- Converting to DEX ---"
$CLASSES = Get-ChildItem -Path build/obj -Filter *.class -Recurse | ForEach-Object { $_.FullName }
& $D8 $CLASSES --lib $PLATFORM_JAR --output build/

Write-Host "--- Adding DEX to APK ---"
$apkPath = Resolve-Path "build/vitaai_unsigned.apk"
$dexPath = Resolve-Path "build/classes.dex"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($apkPath.Path, "Update")
[System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $dexPath.Path, "classes.dex")
$zip.Dispose()

Write-Host "--- Aligning APK ---"
& $ZIPALIGN -f 4 build/vitaai_unsigned.apk build/vitaai_aligned.apk

Write-Host "--- Signing APK ---"
if (Test-Path $DEBUG_KEYSTORE) {
    & $APKSIGNER sign --ks $DEBUG_KEYSTORE --ks-pass pass:android --key-pass pass:android --out build/vitaai_signed.apk build/vitaai_aligned.apk
    Write-Host "Success! APK created at build/vitaai_signed.apk"
} else {
    Write-Host "No debug.keystore found. Please create one to sign the APK."
}
