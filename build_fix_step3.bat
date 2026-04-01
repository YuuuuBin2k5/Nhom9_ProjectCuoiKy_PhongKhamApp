@echo off
echo ========================================
echo BUILD FIX STEP 3 AUTO-ADVANCE
echo ========================================
echo.

echo [1/2] Building Mobile APK...
echo.
cd mobile_android
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Mobile build failed!
    pause
    exit /b 1
)
cd ..

echo.
echo ========================================
echo BUILD COMPLETE!
echo ========================================
echo.
echo APK Location:
echo mobile_android\app\build\outputs\apk\debug\app-debug.apk
echo.
echo Next Steps:
echo 1. Restart backend trong IntelliJ IDEA
echo 2. Install APK: adb install -r mobile_android\app\build\outputs\apk\debug\app-debug.apk
echo 3. Test theo huong dan trong HUONG_DAN_BUILD_VA_TEST_FIX_STEP3.md
echo.
pause
