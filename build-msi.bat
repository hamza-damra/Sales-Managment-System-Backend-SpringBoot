@echo off
echo Creating MSI installer for Sales Management System...
echo.

REM Set paths
set WIX_PATH=%~dp0wix-tools
set INSTALLER_DIR=%~dp0installer

REM Check if WiX exists
if not exist "%WIX_PATH%\candle.exe" (
    echo ERROR: WiX not found at %WIX_PATH%
    pause
    exit /b 1
)

REM Change to installer directory
cd /d "%INSTALLER_DIR%"

REM Create output directory
if not exist "output" mkdir "output"

REM Clean previous build
if exist "output\*.wixobj" del /q "output\*.wixobj"
if exist "output\*.msi" del /q "output\*.msi"

echo Compiling WiX files...
"%WIX_PATH%\candle.exe" -out "output\\" "Product.wxs" "Components.wxs" -ext WixUIExtension -ext WixUtilExtension

if %errorlevel% neq 0 (
    echo ERROR: Candle compilation failed
    pause
    exit /b 1
)

echo Linking MSI...
"%WIX_PATH%\light.exe" -out "output\Sales-Management-System-Setup.msi" "output\Product.wixobj" "output\Components.wixobj" -ext WixUIExtension -ext WixUtilExtension -cultures:en-US

if %errorlevel% neq 0 (
    echo ERROR: Light linking failed
    pause
    exit /b 1
)

echo.
echo SUCCESS: MSI installer created at %INSTALLER_DIR%\output\Sales-Management-System-Setup.msi
echo.

if exist "output\Sales-Management-System-Setup.msi" (
    dir "output\Sales-Management-System-Setup.msi"
    echo.
    echo You can now install using: output\Sales-Management-System-Setup.msi
) else (
    echo ERROR: MSI file not found
)

pause
