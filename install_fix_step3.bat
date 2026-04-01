@echo off
echo ========================================
echo INSTALL FIX STEP 3 APK
echo ========================================
echo.

set APK_PATH=mobile_android\app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" (
    echo ERROR: APK not found!
    echo Please run build_fix_step3.bat first
    echo.
    pause
    exit /b 1
)

echo Checking ADB connection...
adb devices
echo.

echo Installing APK...
adb install -r "%APK_PATH%"

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Installation failed!
    echo.
    echo Troubleshooting:
    echo 1. Make sure USB Debugging is enabled
    echo 2. Check if device is connected: adb devices
    echo 3. Try manual installation by copying APK to phone
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo INSTALLATION COMPLETE!
echo ========================================
echo.
echo Next Steps:
echo 1. Open app on phone
echo 2. Login as doctor
echo 3. Test theo HUONG_DAN_BUILD_VA_TEST_FIX_STEP3.md
echo.
pause
