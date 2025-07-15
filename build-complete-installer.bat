@echo off
REM Complete build script for Sales Management System
REM This script builds the application and creates the MSI installer

echo ========================================
echo Sales Management System Complete Build
echo ========================================
echo.

set PROJECT_DIR=%~dp0
set INSTALLER_DIR=%PROJECT_DIR%installer
set WIX_PATH=%PROJECT_DIR%wix-tools

echo Step 1: Building Spring Boot application...
echo.

REM Build the Spring Boot application
call "%PROJECT_DIR%mvnw.cmd" clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build Spring Boot application
    pause
    exit /b 1
)

echo.
echo SUCCESS: Spring Boot application built successfully!
echo.

echo Step 2: Checking prerequisites for installer...
echo.

REM Check if WiX is installed
if not exist "%WIX_PATH%\candle.exe" (
    echo ERROR: WiX Toolset not found at %WIX_PATH%
    echo.
    echo Please install WiX Toolset v3.11 or higher from:
    echo https://wixtoolset.org/releases/
    echo.
    echo Alternative installation locations to check:
    echo - C:\Program Files (x86)\Windows Installer XML v3.5\bin
    echo - C:\Program Files\WiX Toolset v3.11\bin
    echo - C:\Program Files (x86)\WiX Toolset v3.10\bin
    echo.
    echo You can also try updating the WIX_PATH variable in this script
    echo to point to your WiX installation location.
    echo.
    pause
    exit /b 1
)

echo WiX Toolset found at: %WIX_PATH%
echo.

echo Step 3: Verifying required files...
echo.

REM Check if JAR file exists
if not exist "%PROJECT_DIR%target\sales-management-backend-0.0.1-SNAPSHOT.jar" (
    echo ERROR: JAR file not found at target\sales-management-backend-0.0.1-SNAPSHOT.jar
    echo Please make sure the Maven build completed successfully.
    pause
    exit /b 1
)

REM Check if installer files exist
if not exist "%INSTALLER_DIR%\Product.wxs" (
    echo ERROR: Product.wxs not found
    pause
    exit /b 1
)

if not exist "%INSTALLER_DIR%\Components.wxs" (
    echo ERROR: Components.wxs not found
    pause
    exit /b 1
)

if not exist "%INSTALLER_DIR%\Variables.wxi" (
    echo ERROR: Variables.wxi not found
    pause
    exit /b 1
)

echo All required files found.
echo.

echo Step 4: Building MSI installer...
echo.

REM Change to installer directory
cd /d "%INSTALLER_DIR%"

REM Create output directory
if not exist "output" mkdir "output"

REM Clean previous build
if exist "output\*.wixobj" del /q "output\*.wixobj"
if exist "output\*.msi" del /q "output\*.msi"
if exist "output\*.wixpdb" del /q "output\*.wixpdb"

echo Compiling WiX source files...

REM Compile WiX source files
"%WIX_PATH%\candle.exe" -out "output\\" "Product.wxs" "Components.wxs" -ext WixUIExtension -ext WixUtilExtension
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile WiX source files
    echo.
    echo Common issues:
    echo - Missing WiX extensions
    echo - Syntax errors in WiX files
    echo - Missing referenced files
    echo.
    pause
    exit /b 1
)

echo Linking MSI package...

REM Link MSI package
"%WIX_PATH%\light.exe" -out "output\Sales-Management-System-Setup.msi" "output\Product.wixobj" "output\Components.wixobj" -ext WixUIExtension -ext WixUtilExtension -cultures:en-US -spdb
if %errorlevel% neq 0 (
    echo WARNING: light.exe failed, this might be due to missing files or configuration issues
    echo Attempting to continue with reduced functionality...
    echo.
    echo Common issues:
    echo - Missing source files referenced in Components.wxs
    echo - Icon files not found
    echo - Service executable not found
    echo.
    echo You may need to:
    echo 1. Replace placeholder icon files with actual ICO files
    echo 2. Ensure all referenced files exist
    echo 3. Check file paths in Components.wxs
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.

REM Display results
if exist "output\Sales-Management-System-Setup.msi" (
    echo MSI installer created successfully:
    echo Location: %INSTALLER_DIR%\output\Sales-Management-System-Setup.msi
    echo.
    dir "output\Sales-Management-System-Setup.msi"
    echo.
    echo Installation Commands:
    echo - Interactive install: output\Sales-Management-System-Setup.msi
    echo - Silent install: msiexec /i "output\Sales-Management-System-Setup.msi" /quiet
    echo - Silent install with log: msiexec /i "output\Sales-Management-System-Setup.msi" /quiet /l*v install.log
    echo.
    echo The installer includes:
    echo - Spring Boot application JAR
    echo - Windows service configuration
    echo - Configuration templates
    echo - Documentation
    echo - Service management scripts
    echo.
    echo Next steps:
    echo 1. Test the installer on a clean system
    echo 2. Verify service installation works
    echo 3. Check database connectivity
    echo 4. Test application functionality
    echo.
) else (
    echo WARNING: MSI file not found after build
    echo Check the build output above for errors
)

echo Build process completed!
pause
